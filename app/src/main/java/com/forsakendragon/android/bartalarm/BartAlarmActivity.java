package com.forsakendragon.android.bartalarm;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BartAlarmActivity extends AppCompatActivity {
    private static final String TAG = "BART_ALARM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bart_alarm);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new ChooseRouteFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}
