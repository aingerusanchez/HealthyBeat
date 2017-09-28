package com.aingerusanchez.healthybeat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Analizar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analizar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /*@Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.navigation, menu);
            return true;
        }*/

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //private TextView mTextMessage;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_bluetooth:
                    //mTextMessage.setText(R.string.title_bluetooth);
                    Log.i("NavigationBar", String.valueOf(R.string.title_bluetooth));
                    Intent bluetoothIntent = new Intent(getApplicationContext(), Bluetooth.class);
                    startActivity(bluetoothIntent);
                    return true;
                case R.id.navigation_archivo:
                    //mTextMessage.setText(R.string.title_archivo);
                    Log.i("NavigationBar", String.valueOf(R.string.title_archivo));
                    Intent archivoIntent = new Intent(getApplicationContext(), Archivo.class);
                    startActivity(archivoIntent);
                    return true;
                case R.id.navigation_analizar:
                    //mTextMessage.setText(R.string.title_analizar);
                    Log.i("NavigationBar", String.valueOf(R.string.title_analizar));
                    Intent analizarIntent = new Intent(getApplicationContext(), Analizar.class);
                    startActivity(analizarIntent);
                    return true;
                case R.id.navigation_resultado:
                    //mTextMessage.setText(R.string.title_resultado);
                    Log.i("NavigationBar", String.valueOf(R.string.title_resultado));
                    Intent resultadoIntent = new Intent(getApplicationContext(), Resultado.class);
                    startActivity(resultadoIntent);
                    return true;
                case R.id.navigation_perfil:
                    //mTextMessage.setText(R.string.title_perfil);
                    Log.i("NavigationBar", String.valueOf(R.string.title_perfil));
                    Intent perfilIntent = new Intent(getApplicationContext(), Perfil.class);
                    startActivity(perfilIntent);
                    return true;
            }
            return false;
        }

    };

}
