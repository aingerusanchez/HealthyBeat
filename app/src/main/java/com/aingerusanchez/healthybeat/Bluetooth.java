package com.aingerusanchez.healthybeat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.widget.Toast;

public class Bluetooth extends BaseActivity implements DeviceListFragment.OnFragmentInteractionListener {

    // Variables
    private BluetoothAdapter BTAdapter;
    private DeviceListFragment mDeviceListFragment;
    // Constantes
    public static int REQUEST_BLUETOOTH = 1;


    @Override
    int getContentViewId() {
        return R.layout.activity_bluetooth;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_bluetooth_menu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        // Verficar si el dispositivo soporta Bluetooth
        if (BTAdapter == null) {
            // Device does not support Bluetooth
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dispositivo_incompatible)
                    .setMessage(R.string.bluetooth_no_soportado)
                    .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            // TODO: Al finalizar los test en el emulador de Android Studio, cambiar el Toast por el Dialog
            /*Toast toast = Toast.makeText(getApplicationContext(), R.string.bluetooth_no_soportado, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toast.show();*/
        }

        // Método para veríficar que el Bluetooth esté activado
        comprobarBluetooth(BTAdapter);

        FragmentManager fragmentManager = getSupportFragmentManager();

        mDeviceListFragment = DeviceListFragment.newInstance(BTAdapter);
        fragmentManager.beginTransaction().replace(R.id.container, mDeviceListFragment).commit();

        /*Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);*/

    }

    @Override
    public void onFragmentInteraction(String id) {
        Toast tstConnecting = Toast.makeText(this, "Conectando...", Toast.LENGTH_LONG);
        tstConnecting.show();
    }

    public void comprobarBluetooth(BluetoothAdapter BTAdapter) {

        // Verificar si el Bluetooth está activado
        if (!BTAdapter.isEnabled()) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.bluetooth_desactivado)
                    .setMessage(R.string.desea_activar)
                    // Si acepta se activara el Bluetooth una vez habilitado el permiso
                    .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
                        }
                    })
                    // Si no acepta, se mostrará un mensaje
                    .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.debe_activar_bluetooth, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    })
                    .setIcon(R.drawable.ic_bluetooth_black_24dp)
                    .show();
        }
    }



}