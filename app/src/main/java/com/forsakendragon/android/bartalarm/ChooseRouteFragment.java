package com.forsakendragon.android.bartalarm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.forsakendragon.android.bartalarm.XML.parseBARTStations;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ChooseRouteFragment extends Fragment {
    private static final String TAG = "BART_ALARM";

    private AppCompatSpinner mFromStationSpinner;
    private AppCompatSpinner mToStationSpinner;
    private Button mChooseRoute;
    private Button mCancelAlarm;
    private Button mTestXMLDownload;
    private SimpleAlarm mAlarm = new SimpleAlarm();

    private ArrayList<ArrayList<Integer>> mTimes;
    private List<parseBARTStations.Station> mStationList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ChooseRouteFragment.onCreate() being executed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose_route, container, false);

        mFromStationSpinner = (AppCompatSpinner) v.findViewById(R.id.fromStation);
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

        mToStationSpinner = (AppCompatSpinner) v.findViewById(R.id.toStation);
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

        mTestXMLDownload = (Button) v.findViewById(R.id.testXMLDownload);
        mTestXMLDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Before execute List: " + mStationList);

                ChooseRouteFragment.downloadStationXMLFile down = new ChooseRouteFragment.downloadStationXMLFile();
                down.execute();
            }
        });

        setMatrix();

        return v;


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

    //Page 494 return types, 496 for progress updates
    public class downloadStationXMLFile extends AsyncTask<Void, Void, List<parseBARTStations.Station>> {
        private static final String mURL = "http://api.bart.gov/api/stn.aspx?cmd=stns&key=MW9S-E7SL-26DU-VV8V";
        @Override
        protected List<parseBARTStations.Station> doInBackground(Void... params) {
            try {
                return downloadAndParse();
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Failed to parse: " + e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "Failed to fetch URL: " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<parseBARTStations.Station> list) {
            //Runs in main thread, not async, update UI
            mStationList = list;
            //Log.d(TAG, "After execute List: " + mStationList);
            for (parseBARTStations.Station s: mStationList) {
                Log.d(TAG, "After execute, Stations: " + s.name);
            }
        }

        public List<parseBARTStations.Station> downloadAndParse() throws IOException, XmlPullParserException {
            URL url = new URL(mURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try {
                InputStream in = connection.getInputStream();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(connection.getResponseMessage() + ": with " + mURL);
                }
                parseBARTStations parseBART = new parseBARTStations();
                return parseBART.parse(in);
            } finally {
                connection.disconnect();
            }
        }
    }


}
