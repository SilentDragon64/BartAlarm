package com.forsakendragon.android.bartalarm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Arion on 11/15/2017.
 */

public class SetTimeFragment extends Fragment {
    private static final String LOG_HEAD = "SetTimeFragment: ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Config.LOG_TAG, LOG_HEAD + "onCreate() being executed");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_manually_set_alarm, container, false);

        return v;
    }
}
