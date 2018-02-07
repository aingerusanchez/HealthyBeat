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
    // TODO: Mostrar clase --> método en el TAG
    private static final String TAG = Analizar.class.getSimpleName();
    Context context = Analizar.this;

    // Variables de GraphView
    GraphView graph = null;
    //RealtimeUpdates mGraphViewFragment = null;
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
    TextView tvPleth;
    TextView tvHR;
    TextView tvSPO2;
    /*TextView tvE_HR;
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
        tvPleth = findViewById(R.id.tv_PLETH);
        tvHR = findViewById(R.id.tv_HR);
        tvSPO2 = findViewById(R.id.tv_SP02);
        /*tvE_HR = findViewById(R.id.tv_EHR);
        tvE_SPO2 = findViewById(R.id.tv_ESPO2);*/
        btnAnalizar = findViewById(R.id.btn_analizar);

        /********************** GraphView *********************/
        // Objeto UI del gráfico
        graph = (GraphView) findViewById(R.id.graph);
        // VIEWPORT: Segmento de datos que se ve por pantalla
        graph.getViewport().setMaxX(200);                       // MAX: Rango de 200 datos en el EJE X
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScalable(false);                 // No activar
        graph.getViewport().setScrollable(true);                // Activar el scroll horizontal
        graph.getViewport().setBackgroundColor(TRANSPARENT);    // Color de fondo
        // GRIDLABELRENDERER: Estilos de la cuadricula que se usa como canvas (o View) del gráfico
        // TODO (Debug): descomentar siguiente linea al finalizar pruebas
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHighlightZeroLines(false);
        /* Estilo de las rejillas de fondo
            GridLabelRenderer.GridStyle.
            NONE: Ninguna
            HORIZONTAL
            VERTICAL
            BOTH: Horizontal y Vertical
        */
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);

        /********************** END GraphView *********************/

        btnAnalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnAnalizar.isChecked()) {
                    // Llamar a la tarea asincrona para procesar y mostrar datos
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

        // Inicializar el RECEIVER y llamar al Listener para que recoja los datos a enseñar en la UI
        receiver = new BluetoothDataReceiver();
        receiver.setOnPlethDataUpdatingListener(context);

    }

    // Método para actualizar la UI con los datos procesados en BluetoothDataReceiver
    @Override
    public void onPlethDataUpdatingListener(String key, String value) {

        switch (key) {
            case "Pleth":
                longPleth = Long.parseLong(value);
                tvPleth.setText(String.valueOf(longPleth));
                //Log.d(TAG, "Pleth: " + longPleth);
                pintarGrafico(longPleth);
                break;
            case "HR":
                tvHR.setText(value);
                Log.d(TAG, "HR: " + value);
                break;
            case "SPO2":
                tvSPO2.setText(value);
                Log.d(TAG, "SPO2: " + value);
                break;
            /*case "E_HR":
                tvE_HR.setText(value);
                //Log.d(TAG, "E_HR: " + value);
                break;
            case "E_SP02":
                tvE_SPO2.setText(value);
                //Log.d(TAG, "E_SPO2: " + value);
                break;*/
            default:
                break;
        }

    }

    /********** MÉTODOS PARA PINTAR EL GRÁFICO **********/

    // Método para reiniciar las variables de pintado del gráfico
    private void iniciarVariables() {
        lastX = 0;
        contDatos = 0;
        // Editar atributos de los valores de la gráfica
        series = new LineGraphSeries<>();
        series.setColor(Color.RED);
    }

    // Método para insertar los nuevos puntos del eje Y
    private void pintarGrafico(final long longPleth) {

        // CÓDIGO INICIAL:
        series.appendData(new DataPoint(lastX, longPleth), true, 100000);
        lastX++;
        contDatos++;
        // Cada intervalo nuevos datos, se pintan en el gráfico
        //if(contDatos == /*INTERVALO*/ ) {
            graph.addSeries(series);
            contDatos = 0;
        //}
        /*try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


        /* PRUEBA 1
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                series.appendData(new DataPoint(lastX, longPleth), true, 100000);
                lastX++;
                graph.addSeries(series);
            }
        }, 2000);*/


        /* PRUEBA 2
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                series.appendData(new DataPoint(lastX, longPleth), true, 100000);
                lastX++;
                graph.addSeries(series);
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(r, 1000);*/

        // PRUEBA 3: Con Thread
        /*final Handler handler = new Handler();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        series.appendData(new DataPoint(lastX, longPleth), true, 100000);
                        lastX++;
                        graph.addSeries(series);
                        sleep(1000);
                        handler.post(this);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();*/

    }

    /********** FIN MÉTODOS PARA PINTAR EL GRÁFICO **********/

    /* ----- CLASE ASÍNCRONA QUE PROCESA LOS DATOS EN UN RECEIVER -----
        public class TareaAsyncTask extends AsyncTask<params, progress, result>
            params: Tipo de parámetro que se recibirá como entrada para la tarea en el método doInBackground(Params).
            progress: Parámetros para actualizar el hilo principal o UIThread.
            result: Es el resultado devuelto por el procesamiento en segundo plano.
    */
    private class AnalizarDatosPleth extends AsyncTask<Void, Integer, Boolean> {

        // Método llamado antes de iniciar el procesamiento en segundo plano.
        @Override
        protected void onPreExecute() {
            // Reiniciar UI de datos hasta mostrar su valor actualizado
            tvPleth.setText("...");
            tvHR.setText("...");
            tvSPO2.setText("...");
            /*tvE_HR.setText("...");
            tvE_SPO2.setText("...");*/

            iniciarVariables();
        }

        // Método en el que se define el código que se ejecutará en segundo plano.
        // Recibe como parámetros los declarados al llamar al método execute(Params).
        @Override
        protected Boolean doInBackground(Void... voids) {

            while(btnAnalizar.isChecked()) {

                if(isCancelled()) {
                    break;
                }

                String[] datos = CSVReader();
                procesarDatosRecevier(datos);

                /* TODO (Optimización de procesado): Lo ideal sería recibir los valores a actualizar en la UI y mostrarlos: Investigar sobre el Handler del Receiver
                    publisPogress(HR, SPO2, Pleth) -->  onProgressUpdate(HR, SPO2, Pleth)
                */
                // Mostrar los datos periodicamente en el UIThread
                publishProgress();

            }

            if(!btnAnalizar.isChecked() || isCancelled() ) {
                cancel(true);
            }

            return true;
        }

        // Método es llamado por publishProgress(), dentro de doInBackground(Params)
        // (su uso es muy común para por ejemplo actualizar el porcentaje de un componente ProgressBar).
        protected void onProgressUpdate() {}

        // Método llamado tras finalizar doInBackground(Params).
        // Recibe como parámetro el resultado devuelto por doInBackground(Params).
        protected void onPostExecute() {
            //btnAnalizar.setActivated(false);
            // Desactivar el RECEIVER
            unregisterReceiver(receiver);
            /*Toast.makeText(context, "Se ha terminado el análisis", Toast.LENGTH_SHORT ).show();
            Log.i(TAG, "(POSTEXECUTE) Se ha finalizado el análisis de datos");*/
        }

        // Método que se ejecutará cuando se cancele la ejecución de la tarea antes de su finalización normal.
        @Override
        protected void onCancelled() {
            // Desactivar el RECEIVER
            unregisterReceiver(receiver);
            Toast.makeText(context, "Se ha detenido el análisis", Toast.LENGTH_SHORT ).show();
            Log.i(TAG, "(CANCEL) Se ha detenido el análisis de datos");
        }



        private void procesarDatosRecevier(String[] datos) {

            // Mandar 1.dato al RECEIVER parar inicializar las estrucutras de datos
            registerReceiver(receiver, new IntentFilter("com.aingerusanchez.healthybeat.RECIBIR_DATOS"));
            intentReceiver = new Intent("com.aingerusanchez.healthybeat.RECIBIR_DATOS");
            intentReceiver.putExtra("Dato", "reset");
            sendBroadcast(intentReceiver);

            for(String dato : datos) {
                if(isCancelled()) {
                    break;
                }
                // Mandar datos al Receiver
                //receiver.setOnPlethDataUpdatingListener(context);
                registerReceiver(receiver, new IntentFilter("com.aingerusanchez.healthybeat.RECIBIR_DATOS"));
                intentReceiver = new Intent("com.aingerusanchez.healthybeat.RECIBIR_DATOS");
                intentReceiver.putExtra("Dato", dato);
                sendBroadcast(intentReceiver);


            }
        }
    }


    // Método para leer datos de un archivo.csv
    public String[] CSVReader() {

        String cvsSplitBy = ",";
        String[] receivedData = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("datos.csv")));) {

            // use comma as separator
            receivedData = br.readLine().split(cvsSplitBy);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedData;

    }

    // -------------------- FIN CLASE ASINCRONA (AsyncTask) --------------------

}
