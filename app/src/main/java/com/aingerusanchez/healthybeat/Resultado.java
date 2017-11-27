package com.aingerusanchez.healthybeat;

import android.os.Bundle;

public class Resultado extends BaseActivity {

    @Override
    int getContentViewId() {
        return R.layout.activity_resultado;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_resultado_menu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}