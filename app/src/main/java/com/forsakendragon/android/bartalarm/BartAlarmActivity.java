package com.forsakendragon.android.bartalarm;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.forsakendragon.android.bartalarm.XML.parseBARTStations;

import java.util.ArrayList;

public class BartAlarmActivity extends AppCompatActivity implements SplashFragment.OnFragmentInteractionListener {
    private static final String LOG_HEAD = "BartAlarmActivity: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Config.LOG_TAG, LOG_HEAD + "onCreate()");
        setContentView(R.layout.activity_bart_alarm);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (fragment == null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            fragment = new SplashFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public void onFragmentInteraction(ArrayList<parseBARTStations.Station> list, boolean isConnected) {
        Log.d(Config.LOG_TAG, LOG_HEAD + "onFragmentInteraction() - StationList & isConnected handed back from splash");

        // Create fragment and give it an argument specifying the list of stations
        Fragment newFragment = new ChooseRouteFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Config.ARGS_STATION_LIST, list);
        bundle.putBoolean(Config.ARGS_IS_CONNECTED, isConnected);
        newFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        //Adds fragment to stack, back button goes to the previous fragment, without app closes
        //transaction.addToBackStack(null);

// Commit the transaction
        transaction.commit();
    }
}
