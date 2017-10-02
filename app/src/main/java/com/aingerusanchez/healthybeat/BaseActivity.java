package com.aingerusanchez.healthybeat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class BaseActivity extends AppCompatActivity {

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }*/

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
