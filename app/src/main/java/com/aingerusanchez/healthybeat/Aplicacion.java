package com.aingerusanchez.healthybeat;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Aingeru on 21/01/2018.
 */

public class Aplicacion extends Application {

    // Variables globales
    private static ArrayList<Integer> frame = new ArrayList<Integer>();
    private static ArrayList<int[]> paquete = new ArrayList<int[]>();
    private static Context context = null;

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

    public void setPaquete(ArrayList<int[]> paquete) {
        this.paquete = paquete;
    }

    public static ArrayList<Integer> getFrame() {
        return frame;
    }

    public void setFrame(ArrayList<Integer> frame) {
        this.frame = frame;
    }



}
