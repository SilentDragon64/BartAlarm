package com.forsakendragon.android.bartalarm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import java.util.ArrayList;


public class ChooseRouteActivity extends AppCompatActivity {
    private static final String TAG = "BART_ALARM";

    private AppCompatSpinner mFromStationSpinner;
    private AppCompatSpinner mToStationSpinner;
    private Button mChooseRoute;
    private Button mCancelAlarm;

    private SimpleAlarm mAlarm = new SimpleAlarm();

    private ArrayList<ArrayList<Integer>> mTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_route);
        Log.d(TAG, "ChooseRouteActivity.onCreate() being executed");

        mFromStationSpinner = (AppCompatSpinner) findViewById(R.id.fromStation);
        mFromStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, parent.getItemAtPosition(position).toString() + " selected as From station, Position: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "None selected for From station");
            }
        });

        mToStationSpinner = (AppCompatSpinner) findViewById(R.id.toStation);
        mToStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, parent.getItemAtPosition(position).toString() + " selected as To station, Position: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "None selected for To station");
            }
        });

        mChooseRoute = (Button) findViewById(R.id.chooseRouteButton);
        mChooseRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int timeToAlarm = mTimes.get(mFromStationSpinner.getSelectedItemPosition()).get(mToStationSpinner.getSelectedItemPosition());
                Toast.makeText(ChooseRouteActivity.this,
                               mFromStationSpinner.getSelectedItem().toString() + " to " +
                               mToStationSpinner.getSelectedItem().toString() + " will take " + timeToAlarm,
                               Toast.LENGTH_SHORT).show();
                mAlarm.setAlarm(ChooseRouteActivity.this, timeToAlarm);

            }
        });

        mCancelAlarm = (Button) findViewById(R.id.cancelAlarm);
        mCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlarm.cancelAlarm();
            }
        });

        setMatrix();
    }

    private void setMatrix() {
        int m = 0;
        // rows (From Station)
        mTimes = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < mFromStationSpinner.getCount(); i++) {

            // columns (To Station)
            ArrayList<Integer> row = new ArrayList<Integer>();
            for (int j = 0; j < mToStationSpinner.getCount(); j++) {
                row.add(m++);
            }
            mTimes.add(row);
        }

        Log.d(TAG, mTimes.toString());
    }
}
