package com.aingerusanchez.healthybeat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.graphics.Color.TRANSPARENT;

public class Analizar extends BaseActivity implements BluetoothDataReceiver.OnPlethDataUpdatingListener {

    // CONSTANTES
    private static final String TAG = Analizar.class.getSimpleName();
    public static final String RESET_DATA = "RESET";
    public static final String FLUSH_DATA = "FLUSH";

    // Variables de la clase
    Context context = Analizar.this;

    // Variables de GraphView
    GraphView graph = null;
    LineGraphSeries<DataPoint> series = null;
    DataPoint newPoint = null;
    final static int INTERVALO = 100;
    int lastX = 0;
    int contDatos = 0;

    // Variables de AsyncTask
    private AnalizarDatosPleth analizador = null;
    String[] datos = null;

    // Variables del RECEIVER
    Intent intentReceiver = null;
    BluetoothDataReceiver receiver = null;
    long longPleth = 0;

    // Variables de acceso a UI
    TextView tvHR;
    TextView tvSPO2;
    /*TextView tvPleth;
    TextView tvE_HR;
    TextView tvE_SPO2;*/
    ToggleButton btnAnalizar;

    // Variables para API Google Drive
    private GoogleApiClient apiClient;
    private DriveId carpetaDriveId = null;
    private ArrayList<String> muestra = null;

    // Fecha y formato
    DateFormat df = null;
    String date = "";

    @Override
    int getContentViewId() {
        return R.layout.activity_analizar;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_analizar_menu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Definir variables UI
        tvHR = findViewById(R.id.tv_HR);
        tvSPO2 = findViewById(R.id.tv_SP02);
        // CHANGES: ya no se muuestran en UI
        /*tvPleth = findViewById(R.id.tv_PLETH);
        tvE_HR = findViewById(R.id.tv_EHR);
        tvE_SPO2 = findViewById(R.id.tv_ESPO2);*/
        btnAnalizar = findViewById(R.id.btn_analizar);

        // --------------------------------- GRAPHVIEW ------------------------------------------ //
        // GraphView: editar aspecto del gráfico en UI
        graph = (GraphView) findViewById(R.id.graph);
        // VIEWPORT: Segmento de datos que se ve por pantalla
        graph.getViewport().setMaxX(200);                       // MAX: Rango de 200 datos en el EJE X (default=200)
        graph.getViewport().setXAxisBoundsManual(true);         // Calcula los límites del eje X automáticamente (default=true)
        graph.getViewport().setScalable(false);                 // No activar (default=false)
        graph.getViewport().setScrollable(true);                // Activar el scroll horizontal (default=true)
        graph.getViewport().setBackgroundColor(TRANSPARENT);    // Color de fondo (default=TRANSPARENT)
        // GRIDLABELRENDERER: Estilos de la cuadricula que se usa como canvas (o View) del gráfico

        /* Estilo de las rejillas de fondo
            GridLabelRenderer.GridStyle.
            NONE: Ninguna
            HORIZONTAL
            VERTICAL
            BOTH: Horizontal y Vertical
        */
        if (Aplicacion.isModoDebug()) {
            graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(true);
            graph.getGridLabelRenderer().setHighlightZeroLines(true);       // Remarcar ejes en el valor 0
        } else {
            graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graph.getGridLabelRenderer().setHighlightZeroLines(false);      // Remarcar ejes en el valor 0
        }

        // --------------------------------- END GRAPHVIEW ------------------------------------- //

        // Crear DRIVE API CLIENT
        apiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(this, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                /* Para manejar los errores manualmente
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                */
                .build();

        // Definir formato de fechas
        df = new SimpleDateFormat("yyyy-MM-dd");
        date = df.format(Calendar.getInstance().getTime());

        // Fichero para guardar muestra
        muestra = new ArrayList<>();



        btnAnalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(btnAnalizar.isChecked()) {
                    // Al pulsar ANALIZAR: Llamar a la tarea asincrona para procesar y mostrar datos
                    if (analizador == null) {
                        Toast.makeText(context, "Analizando datos", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Analizando datosPleth en la tarea asincrona 'AnalizarDatosPleth'");
                        analizador = new AnalizarDatosPleth();
                        analizador.execute();
                    } else {
                        Toast.makeText(context, "La tarea asincrona 'AnalizarDatosPleth' ya está creada", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "El AsyncTask 'AnalizarDatosPleth' ya existe");
                    }
                } else {
                    // Al pulsar DETENER:
                    // Guadar muestra en Google Drive
                    createFolder("Muestra Healthybeat " + date);
                    subirMuestraDrive();
                    // Detener análisis
                    if (analizador != null) {
                        analizador.cancel(true);
                        analizador = null;
                        Log.i(TAG, "El AsyncTask 'AnalizarDatosPleth' se ha cancelado pulsando el botón de DETENER");
                    } else {
                        Toast.makeText(context, "La tarea asincrona 'AnalizarDatosPleth' no está creada", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "El AsyncTask 'AnalizarDatosPleth' no existe");
                    }

                }
            }
        });

        // Inicializar el RECEIVER y llamar al Listener para que recoja los datos a mostrar en la UI
        // @see: https://stackoverflow.com/questions/48559566/how-to-pass-data-to-broadcastreceiver-to-activity-using-pendingintent?noredirect=1#comment84115752_48559566
        // @see: https://trinitytuts.com/pass-data-from-broadcast-receiver-to-activity-without-reopening-activity/
        receiver = new BluetoothDataReceiver();
        receiver.setOnPlethDataUpdatingListener(context);

    }

    @Override
    public void onPause() {
        super.onPause();

        // Al salir de la actividad, detener el análisis
        btnAnalizar.setChecked(false);
    }

    // Método para actualizar la UI con los datos procesados en BluetoothDataReceiver
    @Override
    public void onPlethDataUpdatingListener(String key, String value) {

            switch (key) {
                /*
                Para guardar todos los datos en crudo
                case "Dato":
                    //Log.d(TAG, "Dato: " + value);
                    muestra.add(value);
                    break;*/
                case "Pleth":
                    // Añadir a muestra para subir a Drive
                    muestra.add(value);
                    // Dibujar gráfico
                    if ( !Aplicacion.getPuntosGrafico().isEmpty() ) {
                        if(Aplicacion.isModoDebug()) {
                            longPleth = Long.parseLong(value);
                            // tvPleth.setText(String.valueOf(longPleth));
                            Log.d(TAG, "PLETH: " + longPleth);
                        }
                        pintarGrafico();
                    }
                    break;
                case "HR":
                    tvHR.setText(value);
                    Log.d(TAG, "HR: " + value);
                    break;
                case "SPO2":
                    tvSPO2.setText(value);
                    Log.d(TAG, "SPO2: " + value);
                    break;
            // CHANGES: ya no se muestran estos datos en UI
            /*case "E_HR":
                tvE_HR.setText(value);
                //Log.d(TAG, "E_HR: " + value);
                break;
            case "E_SP02":
                tvE_SPO2.setText(value);
                //Log.d(TAG, "E_SPO2: " + value);
                break;
                default:
                    break;*/
            }
    }

/*    private void rellenarMuestra(String dato) {
        try {
            out.write(dato + ",");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    //------------------ METODOS PARA PINTAR EL GRAFICO -----------------//

    // Método para reiniciar las variables de pintado del gráfico
    private void iniciarVariables() {
        lastX = 0;
        contDatos = 0;
        // Editar atributos de los valores de la gráfica
        graph.removeAllSeries();
        series = new LineGraphSeries<>();
        series.setColor(Color.RED);
        graph.addSeries(series);
    }

    // Metodo para insertar los nuevos puntos del eje Y
    private void pintarGrafico() {

        long graphPoint = Aplicacion.getPuntosGrafico().remove(0);
        series.appendData(new DataPoint(lastX, graphPoint), true, 1000);
        lastX++;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //------------------- FIN MÉTODOS PARA PINTAR EL GRÁFICO ----------------//

    /** ----- CLASE ASÍNCRONA QUE PROCESA LOS DATOS EN UN RECEIVER -----
     *  public class TareaAsyncTask extends AsyncTask<params, progress, result>
     *      @params: Tipo de parámetro que se recibirá como entrada para la tarea en el método doInBackground(Params).
     *      @progress: Parámetros para actualizar el hilo principal o UIThread.
     *      @result: Es el resultado devuelto por el procesamiento en segundo plano. También es el parametro de postExecute(result)
     */
    private class AnalizarDatosPleth extends AsyncTask<Void, Integer, Boolean> {

        /**
        // Método llamado antes de iniciar el procesamiento en segundo plano.
        **/
        @Override
        protected void onPreExecute() {

            // Reiniciar UI de datos hasta mostrar su valor actualizado
            tvHR.setText("...");
            tvSPO2.setText("...");
            /*
            tvPleth.setText("...");
            tvE_HR.setText("...");
            tvE_SPO2.setText("...");
            */

            iniciarVariables();
        }

        /**
        * Método en el que se define el código que se ejecutará en segundo plano.
        * Recibe como parámetros los declarados al llamar al método execute(Params).
        **/
        @Override
        protected Boolean doInBackground(Void... voids) {

            while(btnAnalizar.isChecked()) {

                // Leer el archivo "datos.csv" para simular la entrada de datos
                String csvFile = "datos.csv";
                String[] datos = CSVReader(csvFile);
                // Procesar los datos recibidos
                procesarDatosRecevier(datos);

                // Mostrar los datos periodicamente en el UIThread
                //publishProgress();

            }

            if(!btnAnalizar.isChecked() || isCancelled() ) {
                cancel(true);
            }

            return true;
        }

        /**
        *   Método llamado por publishProgress(), dentro de doInBackground(Params)
        *   (su uso es muy común para por ejemplo actualizar el porcentaje de un componente ProgressBar).
        **/
        protected void onProgressUpdate() {
            Log.v(TAG, "AnalizarDatosPleth.onProgressUpdate");
        }

        /**
        // Método llamado tras finalizar doInBackground(Params).
        // Recibe como parámetro el resultado devuelto por doInBackground(Params).
        **/
        protected void onPostExecute(Boolean correcto) {
            // Mandar dato para finalizar registros y subirlo a Google Drive
            procesarDatosRecevier(new String[]{FLUSH_DATA});
            // Desactivar el RECEIVER
            unregisterReceiver(receiver);
            Toast.makeText(context, "Se ha terminado el análisis", Toast.LENGTH_SHORT ).show();
            Log.i(TAG, "(POSTEXECUTE) Se ha finalizado el análisis de datos");
        }

        /**
        * Método que se ejecutará cuando se cancele la ejecución de la tarea antes de su finalización normal.
        **/
        @Override
        protected void onCancelled() {
            // Mandar dato para finalizar registros y subirlo a Google Drive
            procesarDatosRecevier(new String[]{FLUSH_DATA});

            // Desactivar el RECEIVER
            unregisterReceiver(receiver);
            Toast.makeText(context, "Se ha detenido el análisis", Toast.LENGTH_SHORT ).show();
            Log.i(TAG, "(CANCEL) Se ha detenido el análisis de datos");
        }


        /**
         * Método que procesa los datos recividos externamente en un RECEIVER
         * @datos: lista datos sin parsear
         */
        private void procesarDatosRecevier(String[] datos) {

            // Definir el INTENT(RECEIVER)
            registerReceiver(receiver, new IntentFilter("com.aingerusanchez.healthybeat.RECIBIR_DATOS"));
            intentReceiver = new Intent("com.aingerusanchez.healthybeat.RECIBIR_DATOS");
            // Mandar 1.dato al RECEIVER parar inicializar las estrucutras de datos
            intentReceiver.putExtra("Dato", RESET_DATA);
            sendBroadcast(intentReceiver);

            for(String dato : datos) {

                if(isCancelled()) {
                    break;
                }

                registerReceiver(receiver, new IntentFilter("com.aingerusanchez.healthybeat.RECIBIR_DATOS"));
                intentReceiver.putExtra("Dato", dato);
                sendBroadcast(intentReceiver);
            }
        }
    }


    /**
     * Método para leer datos de un archivo.csv
     * @return : array de datos leidos y separados por ","
     */
    public String[] CSVReader(String fileName) {

        String cvsSplitBy = ",";
        String[] receivedData = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(fileName)));) {

            // use comma as separator
            receivedData = br.readLine().split(cvsSplitBy);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedData;

    }
    // -------------------- FIN CLASE ASINCRONA (AsyncTask) --------------------

    // ------------------- API GOOGLE DRIVE ---------------------
    private void subirMuestraDrive() {
        new Thread() {
            @Override
            public void run() {
                createFile("Muestra_" + date);
            }
        }.start();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "OnConnectionFailed: " + connectionResult);
    }

    private void createFolder(final String foldername) {

        MetadataChangeSet changeSet =
                new MetadataChangeSet.Builder()
                        .setTitle(foldername)
                        .build();

        // Directorio raíz de Google Drive
        DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);

        // Opción 2: Carpeta de Aplicación (App Folder)
        //DriveFolder folder = Drive.DriveApi.getAppFolder(apiClient);

        folder.createFolder(apiClient, changeSet).setResultCallback(
                new ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFolderResult result) {
                        if (result.getStatus().isSuccess()) {
                            Log.i(TAG, "Carpeta creada con ID = " + result.getDriveFolder().getDriveId());
                            carpetaDriveId = result.getDriveFolder().getDriveId();
                        } else {
                            Log.e(TAG, "Error al crear carpeta");
                        }
                    }
                });
    }

    private void createFile(final String filename) {

        Drive.DriveApi.newDriveContents(apiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (result.getStatus().isSuccess()) {

                            writeData(result.getDriveContents());

                            MetadataChangeSet changeSet =
                                    new MetadataChangeSet.Builder()
                                            .setTitle(filename)
                                            .setMimeType("text/plain")
                                            // TODO: .setMimeType("text/excel")
                                            .build();

                            // Carpeta creada al inicio del muestreo
                            DriveFolder folder = carpetaDriveId.asDriveFolder();

                            folder.createFile(apiClient, changeSet, result.getDriveContents())
                                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                        @Override
                                        public void onResult(DriveFolder.DriveFileResult result) {
                                            if (result.getStatus().isSuccess()) {
                                                Log.i(TAG, "Fichero creado con ID = " + result.getDriveFile().getDriveId());
                                            } else {
                                                Log.e(TAG, "Error al crear el fichero");
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Error al crear DriveContents");
                        }
                    }
                });
    }

    private void writeData(DriveContents driveContents) {
        OutputStream outputStream = driveContents.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);

        try {
            for(String dato : muestra) {
                writer.write(dato + ",");
            }
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error al escribir en el fichero: " + e.getMessage());
        }
    }

}
