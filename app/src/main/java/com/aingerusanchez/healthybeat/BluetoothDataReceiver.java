package com.aingerusanchez.healthybeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Aingeru on 21/01/2018.
 */

public class BluetoothDataReceiver extends BroadcastReceiver {

    private static final boolean modoDebug = true;
    private static final String TAG = "BluetoothDataReceiver";
    int[] frame = null;

    // 25-01-2018: Añadir Listener para actualizar información en UI
    public interface OnPlethDataUpdatingListener {
        public void onPlethDataUpdatingListener(String key, String value);
    }

    private OnPlethDataUpdatingListener listener = null;

    // Método del Listener para devolver datos a la actividad principal (Analizar)
    public void setOnPlethDataUpdatingListener(Context context) {
        this.listener = (OnPlethDataUpdatingListener) context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getExtras() != null) {
            // Recive nuevo dato
            String dato = intent.getExtras().getString("Dato");
            // Se envia el valor '-1' en el primer dato al comenzar el análisis
            if(dato == null || "-1".equals(dato)){
                // Se reinician todos las estructuras de datos utilizadas seguidamente
                frame = null;
                borrarFrame();
                borrarPaquete();
            } else {
                Aplicacion.getFrame().add(Integer.parseInt(dato));
                sincronizarFrame(context);
            }
        }
    }

    private void sincronizarFrame(Context context) {

        // Esperar hasta que el ArrayList de Frame tenga 5 valores
        if (Aplicacion.getFrame().size() == 5) {
            int byte1 = Aplicacion.getFrame().get(0);
            int byte2 = Aplicacion.getFrame().get(1);
            int byte3 = Aplicacion.getFrame().get(2);
            int byte4 = Aplicacion.getFrame().get(3);
            int byte5 = Aplicacion.getFrame().get(4);

            // Si el RESTO de la suma de los 4 primeros Bytes entre 256, es igual al 5.Byte, tenemos un FRAME sincronizado
            if ((byte1+byte2+byte3+byte4) % 256 == byte5) {
                frame = new int[]{byte1, byte2, byte3, byte4, byte5};
                // Sí el 1.Byte del Frame es '129' o '131', significa que es el primer Frame del paquete
                if((byte1 == 129 || byte1 == 131) || Aplicacion.getPaquete().size() >= 1) {
                    if ((byte1 == 129 || byte1 == 131) && Aplicacion.getPaquete().size() == 25) {

                        // DEBUG: Dibujar contenido del PAQUETE
                        if(modoDebug) {
                            String datosPaquete = "";
                            ArrayList<int[]> paquete = Aplicacion.getPaquete();
                            for(int current_frame = 0; current_frame < 25; current_frame++) {
                                for(int byte_actual = 0; byte_actual < 5; byte_actual++) {
                                    datosPaquete += paquete.get(current_frame)[byte_actual] +",";
                                }
                                datosPaquete += "\r\n";
                            }
                            Log.d(TAG, "PAQUETE: " + datosPaquete);
                        }

                        // Reiniciar Paquete
                        borrarPaquete();
                    }
                    Aplicacion.getPaquete().add(frame);
                    sincronizarPaquete();
                }
                //Log.d(TAG, "FRAME: " + byte1 + " " + byte2 + " " + byte3 + " " + byte4 + " " + byte5);

                // Devolver los datos Pleth (Byte2 y Byte3) para mostrar el HRV en UI
                // Juntar los 2 Bytes de Pleth y mostrarlos en la UI
                int puntoPleth = (frame[1]*256) + frame[2];
                if (listener != null) {
                    listener.onPlethDataUpdatingListener("Pleth", String.valueOf(puntoPleth));
                }

                // Reiniciar el frame
                borrarFrame();

            } else {
                // Desplazar FRAME
                desplazarFrame(byte2, byte3, byte4, byte5);
            }

        }
    }

    private void borrarFrame() {
        if (Aplicacion.getFrame().size() > 1) {
            for(int bytes = Aplicacion.getFrame().size(); bytes > 0; bytes--) {
                Aplicacion.getFrame().remove(0);
            }
        }
    }

    // Método para desplazar los Bytes del FRAME actual una posición a la izquerda, hasta sincronizar FRAME
    private void desplazarFrame(int byte2, int byte3, int byte4, int byte5) {
        Aplicacion.getFrame().set(0, byte2);
        Aplicacion.getFrame().set(1, byte3);
        Aplicacion.getFrame().set(2, byte4);
        Aplicacion.getFrame().set(3, byte5);
        Aplicacion.getFrame().remove(4);
    }

    // Método para reiniciar el PAQUETE
    private void borrarPaquete() {
        if (Aplicacion.getPaquete().size() > 1) {
            for(int frames = Aplicacion.getPaquete().size(); frames > 0; frames--) {
                Aplicacion.getPaquete().remove(0);
            }
        }
    }

    /*Método para rellenar los Paquetes con 25 Frames*/
    private void sincronizarPaquete() {

        if (Aplicacion.getPaquete().size() == 25) {
            // Devolver los datos significativos (HR, SpO2...) para mostrar en UI
            int HR_MSB = Aplicacion.getPaquete().get(0)[3];
            int HR_LSB = Aplicacion.getPaquete().get(1)[3];
            int HR = HR_MSB*256 + HR_LSB;
            int SPO2 = Aplicacion.getPaquete().get(2)[3];
            int E_HR_MSB = Aplicacion.getPaquete().get(13)[3];
            int E_HR_LSB = Aplicacion.getPaquete().get(14)[3];
            int E_HR = E_HR_MSB*256 + E_HR_LSB;
            int E_SP02 = Aplicacion.getPaquete().get(15)[3];

            if (listener != null) {
                listener.onPlethDataUpdatingListener("HR", String.valueOf(HR));
                listener.onPlethDataUpdatingListener("SPO2", String.valueOf(SPO2));
                listener.onPlethDataUpdatingListener("E_HR", String.valueOf(E_HR));
                listener.onPlethDataUpdatingListener("E_SP02", String.valueOf(E_SP02));
            }
        }
    }


}
