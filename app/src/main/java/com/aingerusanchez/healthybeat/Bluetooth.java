package com.aingerusanchez.healthybeat;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

public class Bluetooth extends BaseActivity {

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

        // Verficar si el dispositivo soporta Bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast toast = Toast.makeText(getApplicationContext(), R.string.bluetooth_not_supported, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
            toast.show();
        }

        // Verificar si el Bluetooth está activado
        /*if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }*/

    }


}