package com.aingerusanchez.healthybeat;

import android.os.Bundle;

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