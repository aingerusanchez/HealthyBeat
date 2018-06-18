package com.aingerusanchez.healthybeat;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

public class Aplicacion extends Application {

    // Variables globales
    private static ArrayList<Integer> frame = new ArrayList<Integer>();
    private static ArrayList<int[]> paquete = new ArrayList<int[]>();
    private static ArrayList<Long> puntosGrafico = new ArrayList<Long>();
    private static Context context = null;
    private static boolean modoDebug = false;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static ArrayList<int[]> getPaquete() {
        return paquete;
    }

    public static ArrayList<Integer> getFrame() { return frame; }

    public static ArrayList<Long> getPuntosGrafico() { return puntosGrafico; }

    public static boolean isModoDebug() { return modoDebug; }

}
