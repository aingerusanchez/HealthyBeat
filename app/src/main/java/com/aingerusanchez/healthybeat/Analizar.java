package com.aingerusanchez.healthybeat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.graphics.Color.TRANSPARENT;

public class Analizar extends BaseActivity implements BluetoothDataReceiver.OnPlethDataUpdatingListener {

    // Variables de la clase
    private static final String TAG = Analizar.class.getSimpleName();
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
                case "Pleth":
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

    //------------------ MÉTODOS PARA PINTAR EL GRÁFICO -----------------//

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

    // Método para insertar los nuevos puntos del eje Y
    private void pintarGrafico() {

        long graphPoint = Aplicacion.getPuntosGrafico().remove(0);
        series.appendData(new DataPoint(lastX, graphPoint), true, 1000);
        lastX++;

        // Para simular el renderizado del gráfico en tiempo real. Comentado junto a 'addEntry()'
        /*new Thread(new Runnable() {

            @Override
            public void run() {
                // we add 100 new entries
                for (int i = 0; i < 100; i++) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(1000); // default 600
                    } catch (InterruptedException e) {
                        Log.e(TAG + "pintarGrafico() ", "InterruptedException: " + e);
                    }
                }
            }
        }).start();*/
    }

    // CHANGES: Descomentar para simular el renderizado el tiempo real
    /*private void addEntry() {
        if ( !Aplicacion.getPuntosGrafico().isEmpty() ) {
            long graphPoint = Aplicacion.getPuntosGrafico().remove(0);
            // here, we choose to display max 10 points on the viewport and we scroll to end
            series.appendData(new DataPoint(lastX++, graphPoint), true, 1000);
        }
    }*/

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

                if(isCancelled()) {
                    break;
                }

                // Leer el archivo "datos.csv" para simular la entrada de datos
                String csvFile = "datos.csv";
                String[] datos = CSVReader(csvFile);
                // Procesar los datos recividos
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
            intentReceiver.putExtra("Dato", "reset");
            sendBroadcast(intentReceiver);

            for(String dato : datos) {

                if(isCancelled()) {
                    break;
                }
                // (INITIAL CODE) Mandar datos al Receiver
                /*registerReceiver(receiver, new IntentFilter("com.aingerusanchez.healthybeat.RECIBIR_DATOS"));
                intentReceiver = new Intent("com.aingerusanchez.healthybeat.RECIBIR_DATOS");
                intentReceiver.putExtra("Dato", dato);
                sendBroadcast(intentReceiver);*/

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

}
