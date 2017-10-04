package com.aingerusanchez.healthybeat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class Archivo extends BaseActivity {

    @Override
    int getContentViewId() {
        return R.layout.activity_archivo;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_archivo_menu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


}