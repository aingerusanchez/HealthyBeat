package com.aingerusanchez.healthybeat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Analizar extends BaseActivity implements BluetoothDataReceiver.OnPlethDataUpdatingListener {

    // Variables de la clase
    // TODO: Mostrar clase --> método en el TAG
    private static final String TAG = Analizar.class.getSimpleName();
    Context context = Analizar.this;

    // Variables de AsyncTask
    private AnalizarDatosPleth analizador = null;

    // Variables del RECEIVER
    Intent intentReceiver = null;
    BluetoothDataReceiver receiver = null;

    // Variables de acceso a UI
    TextView tvPleth;
    TextView tvHR;
    TextView tvSPO2;
    TextView tvE_HR;
    TextView tvE_SPO2;
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
        tvE_HR = findViewById(R.id.tv_EHR);
        tvE_SPO2 = findViewById(R.id.tv_ESPO2);
        btnAnalizar = findViewById(R.id.btn_analizar);

        btnAnalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnAnalizar.isChecked()) {
                    // Reiniciar UI de datos hasta mostrar su valor actualizado
                    tvPleth.setText("...");
                    tvHR.setText("...");
                    tvSPO2.setText("...");
                    tvE_HR.setText("...");
                    tvE_SPO2.setText("...");
                    // Llamar a la tarea asincrona para procesar y mostrar datos
                    if (analizador == null) {
                        Toast.makeText(context, "Analizando datos", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Analizando datosPleth en la tarea asincrona 'AnalizarDatosPleth'");
                        analizador = new AnalizarDatosPleth();
                        analizador.execute();
                    } else {
                        Toast.makeText(context, "La tarea asincrona 'AnalizarDatosPleth' ya está creada", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "El AsyncTask 'AnalizarDatosPleth' ya existe");
                    }
                } else {
                    if (analizador != null) {
                        analizador.cancel(true);
                        analizador = null;
                        Log.i(TAG, "El AsyncTask 'AnalizarDatosPleth' se ha cancelado pulsando el botón de DETENER");
                    } else {
                        Toast.makeText(context, "La tarea asincrona 'AnalizarDatosPleth' no está creada", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "El AsyncTask 'AnalizarDatosPleth' no existe");
                    }

                }
            }
        });

        receiver = new BluetoothDataReceiver();
        receiver.setOnPlethDataUpdatingListener(context);

        // TODO: Pintar gráfico con los datos Pleth
        /*// ------------------------------------ GRAPHVIEW -----------------------------------------
        // Variable del gráfico
        GraphView graph = (GraphView) findViewById(R.id.graph);

        // Generar puntos del gráfico
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        series.setColor(Color.RED);
        graph.addSeries(series);
        // ------------------------------------ END GRAPHVIEW -----------------------------------------*/
    }

    // Método para actualizar la UI con los datos procesados en BluetoothDataReceiver
    @Override
    public void onPlethDataUpdatingListener(String key, String value) {

        switch (key) {
            case "Pleth":
                tvPleth.setText(value);
                Log.d(TAG, "Pleth: " + value);
                break;
            case "HR":
                tvHR.setText(value);
                Log.d(TAG, "HR: " + value);
                break;
            case "SPO2":
                tvSPO2.setText(value);
                Log.d(TAG, "SPO2: " + value);
                break;
            case "E_HR":
                tvE_HR.setText(value);
                Log.d(TAG, "E_HR: " + value);
                break;
            case "E_SP02":
                tvE_SPO2.setText(value);
                Log.d(TAG, "E_SPO2: " + value);
                break;
            default:
                break;
        }

    }

    private class AnalizarDatosPleth extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Boolean doInBackground(Void... voids) {

            while(btnAnalizar.isChecked()) {

                if(isCancelled()) {
                    break;
                }

                String[] datos = CSVReader();
                // Enviar datos al RECEIVER para procesarlos
                procesarDatos(datos);

                // Mostrar los datos periodicamente en el Hilo Principal
                //publishProgress(datos);

            }

            if(!btnAnalizar.isChecked() || isCancelled() ) {
                cancel(true);
            }

            return true;
        }

        private void procesarDatos(String[] datos) {

            // Mandar 1.dato al RECEIVER parar inicializar las estrucutras de datos
            registerReceiver(receiver, new IntentFilter("com.aingerusanchez.healthybeat.RECIBIR_DATOS"));
            intentReceiver = new Intent("com.aingerusanchez.healthybeat.RECIBIR_DATOS");
            intentReceiver.putExtra("Dato", "-1");
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
                // Ralentizar el envio de datos para simular la transferencia vía Bluetooth
                /*try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }

        }

        // Método para publicar periodicamente en UI los datos actualizados
        /*protected void onProgressUpdate(int datoPleth) {
            tvPleth.setText(Integer.toString(datoPleth));
            Log.v(TAG, "Dato Pleth = " + datoPleth);
        }*/

        protected void onPostExecute() {
            //btnAnalizar.setActivated(false);
            Toast.makeText(context, "Se ha terminado el análisis", Toast.LENGTH_SHORT ).show();
            Log.i(TAG, "(POSTEXECUTE) Se ha finalizado el análisis de datos");

        }

        @Override
        protected void onCancelled() {
            Toast.makeText(context, "Se ha detenido el análisis", Toast.LENGTH_SHORT ).show();
            Log.i(TAG, "(CANCEL) Se ha detenido el análisis de datos");
        }
    }

    public String[] CSVReader() {

        // TODO: Cambiar el path de recogida de datos al receiver del Bluetooth
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



}
