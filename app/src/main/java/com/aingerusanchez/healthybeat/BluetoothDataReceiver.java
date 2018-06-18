package com.aingerusanchez.healthybeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class BluetoothDataReceiver extends BroadcastReceiver {

    // CONSTANTES
    private static final String TAG = "BluetoothDataReceiver";
    private static final int sizeFrame = 5;
    private static final int sizePaquete = 25;
    protected static final int REQ_CREATE_FILE = 1001;

    // Variables de clase
    private Context context;

    // Variables para datos pletismograficos
    private int[] frame = null;
    FileOutputStream fos = null;

    // LISTENER para actualizar información en UI
    public interface OnPlethDataUpdatingListener extends GoogleApiClient.OnConnectionFailedListener {
        public void onPlethDataUpdatingListener(String key, String value);

        void onConnectionFailed(@NonNull ConnectionResult connectionResult);
    }

    private OnPlethDataUpdatingListener listener = null;

    // Método del Listener para devolver datos a la actividad principal (Analizar)
    public void setOnPlethDataUpdatingListener(Context context) {
        this.listener = (OnPlethDataUpdatingListener) context;
    }

    @Override
    public void onReceive(Context pContext, Intent intent) {

        // Guardar Contexto en variable de clase para usarlo en el Receiver
        context = pContext;

        // Crear archivo para guardar registro de la muestra



        if (intent.getExtras() != null) {
            // Recibe nuevo dato
            String dato = intent.getExtras().getString("Dato");
            // Si el dato recibido == 'reset' se inicializan las estructuras de datos
            if(dato == null || Analizar.RESET_DATA.equals(dato)){
                frame = null;
                borrarFrame();
                borrarPaquete();
                Aplicacion.getPuntosGrafico().clear();
            } else {
                // Enviar dato para guardar todos los datos preprocesados
                /*
                if (listener != null) {
                    listener.onPlethDataUpdatingListener("Dato", String.valueOf(dato));
                }*/
                // Parsear datos
                Aplicacion.getFrame().add(Integer.parseInt(dato));
                sincronizarFrame(context, intent);
            }
        }
    }

    private void sincronizarFrame(Context context, Intent intent) {

        // Empezar cuado el ArrayList de Frame tenga 5 valores
        if (Aplicacion.getFrame().size() == sizeFrame) {
            int byte1 = Aplicacion.getFrame().get(0);
            int byte2 = Aplicacion.getFrame().get(1);
            int byte3 = Aplicacion.getFrame().get(2);
            int byte4 = Aplicacion.getFrame().get(3);
            int byte5 = Aplicacion.getFrame().get(4);

            // CHK: Si el resto de la suma de los 4 primeros bytes entre 256, es igual al 5.byte: estamos sincronizados a nivel de Frame
            if ((byte1+byte2+byte3+byte4) % 256 == byte5) {
                frame = new int[]{byte1, byte2, byte3, byte4, byte5};
                // Sí el 1.byte del Frame es '129' o '131': estamos sincronizados a nivel de Paquete
                if((byte1 == 129 || byte1 == 131) || Aplicacion.getPaquete().size() >= 1) {
                    // Si ya hemos procesado un paquete
                    if ((byte1 == 129 || byte1 == 131) && Aplicacion.getPaquete().size() == sizePaquete) {

                        // DEBUG: Dibujar contenido del PAQUETE
                        if(Aplicacion.isModoDebug()) {
                            StringBuilder datosPaquete = new StringBuilder();
                            ArrayList<int[]> paquete = Aplicacion.getPaquete();
                            for(int current_frame = 0; current_frame < sizePaquete; current_frame++) {
                                for(int byte_actual = 0; byte_actual < sizeFrame; byte_actual++) {
                                    datosPaquete.append(paquete.get(current_frame)[byte_actual]).append(",");
                                }
                                datosPaquete.append("\r\n");
                            }
                            Log.d(TAG, "PAQUETE: " + datosPaquete);
                        }

                        // Reiniciar Paquete
                        borrarPaquete();
                    }
                    Aplicacion.getPaquete().add(frame);
                    sincronizarPaquete();
                }
                if (Aplicacion.isModoDebug()) {
                    Log.d(TAG, "FRAME: " + byte1 + " " + byte2 + " " + byte3 + " " + byte4 + " " + byte5);
                }

                // Devolver los datos Pleth (Byte2 y Byte3) para mostrar el HRV en UI
                // Juntar los 2 Bytes de Pleth y mostrarlos en la UI
                long puntoPleth = (frame[1]*256) + frame[2];

                // Mandar dato pleth para dibujar el gráfico a la UIThread mediante Listener
                if (listener != null) {
                    listener.onPlethDataUpdatingListener("Pleth", String.valueOf(puntoPleth));
                }

                // guardar los datos Pleth procesados en la clase padre 'Aplicacion'
                Aplicacion.getPuntosGrafico().add(puntoPleth);


                // Reiniciar el frame
                borrarFrame();

            } else {
                // Desplazar FRAME
                desplazarFrame(byte2, byte3, byte4, byte5);
            }

        }
        // ELSE: esperar que se rellene el FRAME con 5 datos
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

    // Método para rellenar los Paquetes con 25 Frames
    private void sincronizarPaquete() {

        if (Aplicacion.getPaquete().size() == sizePaquete) {
            // Devolver los datos significativos (HR, SpO2...) para mostrar en UI
            int HR_MSB = Aplicacion.getPaquete().get(0)[3];
            int HR_LSB = Aplicacion.getPaquete().get(1)[3];
            long HR = HR_MSB*256 + HR_LSB;
            int SPO2 = Aplicacion.getPaquete().get(2)[3];
            // CHANGES: No se muestran en UI
            /*int E_HR_MSB = Aplicacion.getPaquete().get(13)[3];
            int E_HR_LSB = Aplicacion.getPaquete().get(14)[3];
            long E_HR = E_HR_MSB*256 + E_HR_LSB;
            int E_SP02 = Aplicacion.getPaquete().get(15)[3];*/

            if (listener != null) {
                listener.onPlethDataUpdatingListener("HR", String.valueOf(HR));
                listener.onPlethDataUpdatingListener("SPO2", String.valueOf(SPO2));
                // CHANGES: No se muestran en UI
                //listener.onPlethDataUpdatingListener("E_HR", String.valueOf(E_HR));
                //listener.onPlethDataUpdatingListener("E_SP02", String.valueOf(E_SP02));
            }
        }
    }
}
