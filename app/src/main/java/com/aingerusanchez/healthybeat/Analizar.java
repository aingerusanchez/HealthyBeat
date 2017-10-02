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

public class Analizar extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analizar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        // Colocar el 2 item del menú como seleccionado
        navigation.getMenu().getItem(2).setChecked(true);

        // ------------------------------------ GRAPHVIEW -----------------------------------------
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
    // ------------------------------------ END GRAPHVIEW -----------------------------------------


}
