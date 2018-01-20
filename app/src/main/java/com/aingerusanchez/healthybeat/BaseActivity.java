package com.aingerusanchez.healthybeat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView navigationView;
    private int backpress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        setTitle(this.getLocalClassName());
        backpress = 0;

        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed(){
        backpress = (backpress + 1);
        //Toast.makeText(getApplicationContext(), " Press Back again to Exit ", Toast.LENGTH_SHORT).show();

        if (backpress>1) {
            backpress = 0;

            new AlertDialog.Builder(this)
                    .setTitle(R.string.salir_healthybeat)
                    .setMessage(R.string.confirmar_salida)
                    .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    //.setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_bluetooth_menu) {
            Intent bluetoothIntent = new Intent(getApplicationContext(), Bluetooth.class);
            startActivity(bluetoothIntent);
        } else if (itemId == R.id.navigation_archivo_menu) {
            Intent archivoIntent = new Intent(getApplicationContext(), Archivo.class);
            startActivity(archivoIntent);
        } else if (itemId == R.id.navigation_analizar_menu) {
            Intent analizarIntent = new Intent(getApplicationContext(), Analizar.class);
            startActivity(analizarIntent);
        } else if (itemId == R.id.navigation_resultado_menu) {
            Intent resultadoIntent = new Intent(getApplicationContext(), Resultado.class);
            startActivity(resultadoIntent);
        } else if (itemId == R.id.navigation_perfil_menu) {
            Intent perfilIntent = new Intent(getApplicationContext(), Perfil.class);
            startActivity(perfilIntent);
        }
        finish();
        return true;
    }

    private void updateNavigationBarState(){
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    private void selectBottomNavigationBarItem(int itemId) {
        Menu menu = navigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

    abstract int getContentViewId();

    abstract int getNavigationMenuItemId();

}
