package com.aingerusanchez.healthybeat;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Analizar extends BaseActivity {

    //Atributos de la clase
    private static final String TAG = Analizar.class.getSimpleName();
    Context context = Analizar.this;

    // Variables de acceso a UI
    TextView tvPleth;
    ToggleButton btnAnalizar;

    //-------MUESTRA DE PLETHDATA-------//
    final PlethData plethData = PlethData.getInstance();


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
        btnAnalizar = findViewById(R.id.btn_analizar);

        btnAnalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AnalizarDatosPleth().execute();
            }
        });

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

    private class AnalizarDatosPleth extends AsyncTask<Void, Integer, Boolean> {

        private int datoPleth = 0;

        @Override
        protected void onPreExecute() {
            tvPleth.setText("-");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //btnAnalizar.setActivated(true);
            while(btnAnalizar.isChecked()) {
                datoPleth = plethData.getData();
                publishProgress(datoPleth);
            }
            if(!btnAnalizar.isChecked()) {
                cancel(true);
            }
            return true;
        }

        protected void onProgressUpdate(int datoPleth) {
            // TODO 1: No publica el progreso, hace falta una llamada desde fuera del AsyncTask?
            tvPleth.setText(Integer.toString(datoPleth));
            Log.v(TAG, "Dato Pleth = " + datoPleth);
        }

        protected void onPostExecute() {
            //btnAnalizar.setActivated(false);
            Toast.makeText(context, "Se ha terminado el análisis", Toast.LENGTH_SHORT ).show();

        }

        @Override
        protected void onCancelled() {
            Toast.makeText(context, "Se ha detenido el análisis", Toast.LENGTH_SHORT ).show();
        }
    }


}
