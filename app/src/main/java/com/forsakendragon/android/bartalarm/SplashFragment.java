package com.forsakendragon.android.bartalarm;

import com.forsakendragon.android.bartalarm.XML.downloadXML;
import com.forsakendragon.android.bartalarm.XML.parseBARTStations;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.ArrayList;


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

    private static int mAttempts = 1;

    public SplashFragment() {
        // Required empty public constructor
        Log.d(Config.LOG_TAG, "SplashFragment.onCreate() being executed");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_splash, container, false);
        boolean isConnected;

        mStationList = new ArrayList<>();

        mSplashStatus = (TextView) v.findViewById(R.id.splash_status);
        mSplashStatus.setText(R.string.splash_download_stations);

        if (testIsConnected()) {
            mDownloadStationXMLFile = new downloadStationXMLFile(Config.STATION_LIST_COMMAND);
            mDownloadStationXMLFile.execute();
        } else {
            mSplashStatus.setText(R.string.splash_done_local);
            // TODO; Set station list locally
            mStationList.add(new parseBARTStations.Station("Name 1", "1", 1.0, 1.0));
            mStationList.add(new parseBARTStations.Station("Name 2", "2", 1.0, 1.0));
            mListener.onFragmentInteraction(mStationList, false);

//            private ArrayList<parseBARTStations.Station> generateLocalStationList() {
//                ArrayList<parseBARTStations.Station> stations = new ArrayList<>();
//
//                for (String s: getResources().getStringArray(R.array.bart_stations)) {
//                    stations.add(new parseBARTStations.Station(s, "", 0.0, 0.0));
//                }
//
//                return stations;
//            }

        }

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

    private boolean testIsConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
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
        void onFragmentInteraction(ArrayList<parseBARTStations.Station> list, boolean isConnected);
    }

    public class downloadStationXMLFile extends downloadXML<parseBARTStations.Station> {
        public downloadStationXMLFile(String url) {
            super(url);
        }

        @Override
        protected ArrayList<parseBARTStations.Station> parse(InputStream in) throws IOException, XmlPullParserException{
            parseBARTStations parseBART = new parseBARTStations();
            return parseBART.parse(in);
        }

        @Override
        protected void post(ArrayList<parseBARTStations.Station> list) {
            mStationList = list;
            Log.d(Config.LOG_TAG, "downloadStationXMLFile.post Station List: ");
            if (mStationList != null) {
                mSplashStatus.setText(R.string.splash_done);
                mListener.onFragmentInteraction(mStationList, true);
            } else {
                Log.d(Config.LOG_TAG, "Null station list recieved!");
                mSplashStatus.setText(R.string.splash_error + mAttempts++);
                if (mAttempts <= 3) {
                    mDownloadStationXMLFile = new downloadStationXMLFile(Config.STATION_LIST_COMMAND);
                    mDownloadStationXMLFile.execute();
                } else {
                    mSplashStatus.setText(R.string.splash_error_local);
                    // TODO; Set station list locally
                    mStationList.add(new parseBARTStations.Station("Error", "err", 1.0, 1.0));
                    mListener.onFragmentInteraction(mStationList, false);
                }

            }
        }

    }
}
