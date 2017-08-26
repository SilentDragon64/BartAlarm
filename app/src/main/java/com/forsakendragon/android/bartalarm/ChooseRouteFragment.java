package com.forsakendragon.android.bartalarm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.forsakendragon.android.bartalarm.XML.downloadXML;
import com.forsakendragon.android.bartalarm.XML.parseBARTStations;
import com.forsakendragon.android.bartalarm.XML.parseScheduleBetweenStations;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ChooseRouteFragment extends Fragment {
    private AppCompatSpinner mFromStationSpinner;
    private AppCompatSpinner mToStationSpinner;
    private Button mChooseRoute;
    private Button mCancelAlarm;
    private Button mDownloadSchedualXML;
    private CheckedTextView mTripList;
    private SimpleAlarm mAlarm = new SimpleAlarm();

    private ArrayList<ArrayList<Integer>> mTimes;
    private ArrayList<parseBARTStations.Station> mStationList;
    private ArrayList<parseScheduleBetweenStations.Schedule> mScheduleList;
    private downloadScheduleXMLFile mDownloadScheduleXMLFile;
    private int mFromStationID;
    private int mToStationID;
    private boolean mIsConnected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Config.LOG_TAG, "ChooseRouteFragment.onCreate() being executed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose_route, container, false);

        Bundle args = getArguments();
        if (args != null) {
            Log.d(Config.LOG_TAG, "ChooseRouteFragment.onCreateView recieved stations:");
            mStationList = (ArrayList<parseBARTStations.Station>) args.getSerializable(Config.ARGS_STATION_LIST);
            mIsConnected = args.getBoolean(Config.ARGS_IS_CONNECTED);
            parseBARTStations.printStationList(mStationList);
        }

        // Save code to set from local array, but if no internet, local station list passed as argument
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(), R.array.bart_stations, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        mFromStationSpinner.setAdapter(adapter);

        ArrayAdapter<parseBARTStations.Station> arrayAdapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_spinner_item, mStationList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mFromStationSpinner = (AppCompatSpinner) v.findViewById(R.id.fromStation);
        mFromStationSpinner.setAdapter(arrayAdapter);
        mFromStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(Config.LOG_TAG, parent.getItemAtPosition(position).toString() + " selected as From station, Position: " + position);
                mFromStationID = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(Config.LOG_TAG, "None selected for From station");
            }
        });

        mToStationSpinner = (AppCompatSpinner) v.findViewById(R.id.toStation);
        mToStationSpinner.setAdapter(arrayAdapter);
        mToStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(Config.LOG_TAG, parent.getItemAtPosition(position).toString() + " selected as To station, Position: " + position);
                mToStationID = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(Config.LOG_TAG, "None selected for To station");
            }
        });

        mChooseRoute = (Button) v.findViewById(R.id.chooseRouteButton);
        mChooseRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int timeToAlarm = mTimes.get(mFromStationSpinner.getSelectedItemPosition()).get(mToStationSpinner.getSelectedItemPosition());
                Toast.makeText(v.getContext(),
                        mFromStationSpinner.getSelectedItem().toString() + " to " +
                                mToStationSpinner.getSelectedItem().toString() + " will take " + timeToAlarm,
                        Toast.LENGTH_SHORT).show();
                mAlarm.setAlarm(v.getContext(), timeToAlarm);

            }
        });

        mCancelAlarm = (Button) v.findViewById(R.id.cancelAlarm);
        mCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlarm.cancelAlarm();
            }
        });

        mDownloadSchedualXML = (Button) v.findViewById(R.id.downloadSchedualXML);
        mDownloadSchedualXML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(Config.LOG_TAG, "Downloading Schedual");
                if (mFromStationID != mToStationID && mIsConnected) {
                    Log.d(Config.LOG_TAG, "From " + mStationList.get(mFromStationID) + " and To " + mStationList.get(mToStationID) + " stations");
                    String url = Config.SCHEDUAL_DEPART_COMMAND_1 + mStationList.get(mFromStationID).abbreviation +
                            Config.SCHEDUAL_DEPART_COMMAND_2 + mStationList.get(mToStationID).abbreviation +
                            Config.SCHEDUAL_DEPART_COMMAND_3;
                    mDownloadScheduleXMLFile = new downloadScheduleXMLFile(url);
                    mDownloadScheduleXMLFile.execute();
                }
                else
                    Log.d(Config.LOG_TAG, "From and To stations are the same or not connected");
            }
        });

        mTripList = (CheckedTextView) v.findViewById(R.id.tripList);

        setMatrix();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
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

        //Log.d(Config.LOG_TAG, mTimes.toString());
    }

    public class downloadScheduleXMLFile extends downloadXML<parseScheduleBetweenStations.Schedule> {
        public downloadScheduleXMLFile(String url) {
            super(url);
        }

        @Override
        protected ArrayList<parseScheduleBetweenStations.Schedule> parse(InputStream in) throws IOException, XmlPullParserException {
            parseScheduleBetweenStations parseSchedule = new parseScheduleBetweenStations();
            return parseSchedule.parse(in);
        }

        @Override
        protected void post(ArrayList<parseScheduleBetweenStations.Schedule> list) {
            mScheduleList = list;
            Log.d(Config.LOG_TAG, "downloadScheduleXMLFile.post schedule List: ");
            parseScheduleBetweenStations.printScheduleList(mScheduleList);
        }
    }
}
