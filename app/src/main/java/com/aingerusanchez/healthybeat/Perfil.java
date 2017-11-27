package com.aingerusanchez.healthybeat;

import android.os.Bundle;

public class Perfil extends BaseActivity {

    @Override
    int getContentViewId() {
        return R.layout.activity_perfil;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_perfil_menu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}