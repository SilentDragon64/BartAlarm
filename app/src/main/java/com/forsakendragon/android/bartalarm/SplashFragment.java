package com.forsakendragon.android.bartalarm;

import com.forsakendragon.android.bartalarm.XML.parseBARTStations;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SplashFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SplashFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ArrayList<parseBARTStations.Station> mStationList;
    private TextView mSplashStatus;
    private downloadStationXMLFile mDownloadStationXMLFile;

    public SplashFragment() {
        // Required empty public constructor
        Log.d(Config.LOG_TAG, "SplashFragment.onCreate() being executed");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_splash, container, false);

        mStationList = new ArrayList<>();

        mSplashStatus = (TextView) v.findViewById(R.id.splash_status);
        mSplashStatus.setText(R.string.splash_download_stations);

        mDownloadStationXMLFile = new downloadStationXMLFile();
        mDownloadStationXMLFile.execute();

//        mSplashStatus.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                mStationList.add(new parseBARTStations.Station("name", "abbr", 1.0, 1.0));
//                mListener.onFragmentInteraction(mStationList);
//            }
//        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(ArrayList<parseBARTStations.Station> list);
    }


    //Page 494 return types, 496 for progress updates
    public class downloadStationXMLFile extends AsyncTask<Void, Void, ArrayList<parseBARTStations.Station>> {
        private static final String mURL = "http://api.bart.gov/api/stn.aspx?cmd=stns&key=MW9S-E7SL-26DU-VV8V";
        private boolean isConnected = false;
        @Override
        protected ArrayList<parseBARTStations.Station> doInBackground(Void... params) {
            try {
                if (isConnected = testIsConnected())
                    return downloadAndParse();
                else
                    return generateLocalStationList();
            } catch (XmlPullParserException e) {
                Log.e(Config.LOG_TAG, "Failed to parse: " + e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(Config.LOG_TAG, "Failed to fetch URL: " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<parseBARTStations.Station> list) {
            //Runs in main thread, not async, update UI
            if(isConnected)
                mSplashStatus.setText(R.string.splash_done);
            else
                mSplashStatus.setText(R.string.splash_done_local);

            mStationList = list;
            Log.d(Config.LOG_TAG, "SplashFragment.onPostExecute Station List: ");
            parseBARTStations.printStationList(mStationList);


            mListener.onFragmentInteraction(mStationList);
        }

        private ArrayList<parseBARTStations.Station> downloadAndParse() throws IOException, XmlPullParserException {
            URL url = new URL(mURL);
            HttpURLConnection connection = null;
            InputStream in = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                // Timeout for reading InputStream arbitrarily set to 3000ms.
                connection.setReadTimeout(3000);
                // Timeout for connection.connect() arbitrarily set to 3000ms.
                connection.setConnectTimeout(3000);

                // Open communications link (network traffic occurs here).
                connection.connect();
                // TODO: Callback method
                //publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(connection.getResponseMessage() + ": with " + mURL);
                }

                in = connection.getInputStream();
                //publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);

                parseBARTStations parseBART = new parseBARTStations();
                return parseBART.parse(in);
            } finally {
                // Close Stream and disconnect HTTPS connection.
                if (in != null) {
                    in.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        private boolean testIsConnected() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        private ArrayList<parseBARTStations.Station> generateLocalStationList() {
            ArrayList<parseBARTStations.Station> stations = new ArrayList<>();

            for (String s: getResources().getStringArray(R.array.bart_stations)) {
                stations.add(new parseBARTStations.Station(s, "", 0.0, 0.0));
            }

            return stations;
        }
    }
}
