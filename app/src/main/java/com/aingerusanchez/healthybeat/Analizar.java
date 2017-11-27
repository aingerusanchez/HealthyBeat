package com.aingerusanchez.healthybeat;

import android.graphics.Color;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Analizar extends BaseActivity {

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
