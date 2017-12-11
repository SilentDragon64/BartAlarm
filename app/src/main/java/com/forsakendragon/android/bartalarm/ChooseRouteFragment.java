package com.forsakendragon.android.bartalarm;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.forsakendragon.android.bartalarm.XML.downloadXML;
import com.forsakendragon.android.bartalarm.XML.parseBARTStations;
import com.forsakendragon.android.bartalarm.XML.parseScheduleBetweenStations;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChooseRouteFragment extends Fragment {
    private AppCompatSpinner mFromStationSpinner;
    private AppCompatSpinner mToStationSpinner;
    private Button mChooseRoute;
    private Button mCancelAlarm;
    private Button mDownloadSchedualXML;
    private Button mManuallySetAlarm;
    private RecyclerView mScheduleList;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mScheduleListAdaptor;
    private SimpleAlarm mAlarm = new SimpleAlarm();

    private ArrayList<ArrayList<Integer>> mTimes;
    private ArrayList<parseBARTStations.Station> mStationList = null;
    private ArrayList<parseScheduleBetweenStations.ScheduleTrip> mScheduleTripList = null;
    private downloadScheduleXMLFile mDownloadScheduleXMLFile = null;
    private int mFromStationID;
    private int mToStationID;
    private boolean mIsConnected;
    private int mScheduleSelected = NONE_SELECTED;

    private static final int NONE_SELECTED = -1;
    private static final String LOG_HEAD = "ChooseRouteFragment: ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Config.LOG_TAG, LOG_HEAD + "onCreate() being executed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose_route, container, false);

        Bundle args = getArguments();
        if (args != null) {
            Log.d(Config.LOG_TAG, LOG_HEAD + "onCreateView() - recieved arguments");
            mStationList = (ArrayList<parseBARTStations.Station>) args.getSerializable(Config.ARGS_STATION_LIST);
            mIsConnected = args.getBoolean(Config.ARGS_IS_CONNECTED);
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
                Log.d(Config.LOG_TAG, LOG_HEAD + parent.getItemAtPosition(position).toString() + " selected as From station, Position: " + position);
                mFromStationID = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(Config.LOG_TAG, LOG_HEAD + "None selected for From station");
            }
        });

        mToStationSpinner = (AppCompatSpinner) v.findViewById(R.id.toStation);
        mToStationSpinner.setAdapter(arrayAdapter);
        mToStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(Config.LOG_TAG, LOG_HEAD + parent.getItemAtPosition(position).toString() + " selected as To station, Position: " + position);
                mToStationID = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(Config.LOG_TAG, LOG_HEAD + "None selected for To station");
            }
        });

        mChooseRoute = (Button) v.findViewById(R.id.chooseRouteButton);
        mChooseRoute.setVisibility(Button.INVISIBLE);
        mChooseRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeToAlarm = 0;
                if (mScheduleSelected != NONE_SELECTED) {
                    String time = null;
                    if (mScheduleTripList.get(mScheduleSelected).mList.size() == 1)
                        time = mScheduleTripList.get(mScheduleSelected).mList.get(0).mDestinationDate + " " +
                                mScheduleTripList.get(mScheduleSelected).mList.get(0).mDestinationTime;
                    else
                        time = mScheduleTripList.get(mScheduleSelected).mList.get(1).mDestinationDate + " " +
                                mScheduleTripList.get(mScheduleSelected).mList.get(1).mDestinationTime;

                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    Date destDate = null;
                    try {
                        destDate = format.parse(time);
                    } catch (ParseException e) {
                        Log.e(Config.LOG_TAG, LOG_HEAD + "Invalid Date Format!" + e);
                        e.printStackTrace();
                    }

                    Date now = new Date();
                    Log.d(Config.LOG_TAG, LOG_HEAD + "Now: " + now + " Dest: " + destDate);
                    long diff = destDate.getTime() - now.getTime();

                    if (diff < 0) {
                        Log.d(Config.LOG_TAG, LOG_HEAD + "Invalide time, before now. " + diff);
                        return;
                    }

                    Log.d(Config.LOG_TAG, LOG_HEAD + "Time diff in MS: " + diff + " seconds: " + diff/1000 +
                            " minutes: " + diff/60000);

                    timeToAlarm = diff/1000;
                } else {
                    // TODO: Testing only, if no route selected, use time matrix
                    timeToAlarm = mTimes.get(mFromStationSpinner.getSelectedItemPosition()).get(mToStationSpinner.getSelectedItemPosition());
                }
                Toast.makeText(v.getContext(),
                        mFromStationSpinner.getSelectedItem().toString() + " to " +
                        mToStationSpinner.getSelectedItem().toString() + " will take " + timeToAlarm +
                        " seconds or " + timeToAlarm/60 + " minutes",
                        Toast.LENGTH_SHORT).show();
                mAlarm.setAlarm(v.getContext(), timeToAlarm);
            }
        });

        mCancelAlarm = (Button) v.findViewById(R.id.cancelAlarm);
        mCancelAlarm.setVisibility(Button.INVISIBLE);
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

                Log.d(Config.LOG_TAG, LOG_HEAD + "Downloading Schedual");
                if (mFromStationID != mToStationID && mIsConnected) {
                    Log.d(Config.LOG_TAG, LOG_HEAD + "From " + mFromStationSpinner.getSelectedItem().toString() +
                            " and To " + mToStationSpinner.getSelectedItem().toString() + " stations");
                    String url = Config.SCHEDUAL_DEPART_COMMAND_1 + mStationList.get(mFromStationID).abbreviation +
                            Config.SCHEDUAL_DEPART_COMMAND_2 + mStationList.get(mToStationID).abbreviation +
                            Config.SCHEDUAL_DEPART_COMMAND_3;
                    mDownloadScheduleXMLFile = new downloadScheduleXMLFile(url);
                    mDownloadScheduleXMLFile.execute();
                }
                else
                    Log.d(Config.LOG_TAG, LOG_HEAD + "From and To stations are the same or not connected");
            }
        });

        mManuallySetAlarm = (Button) v.findViewById(R.id.downloadSchedualXML);
        mManuallySetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Config.LOG_TAG, LOG_HEAD + "Manually Setting Alarm");
            }
        });

        mScheduleList = (RecyclerView) v.findViewById(R.id.scheduleList);
        //mScheduleList.setVisibility(RecyclerView.INVISIBLE);

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
        Log.d(Config.LOG_TAG, LOG_HEAD + "onStart() being executed");
    }

    private void setCheckedList() {
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mScheduleList.setLayoutManager(mLayoutManager);

        mScheduleListAdaptor = new CustomRecycleAdapter(mScheduleTripList);
        mScheduleList.setAdapter(mScheduleListAdaptor);

        mScheduleList.setVisibility(RecyclerView.VISIBLE);
        mChooseRoute.setVisibility(Button.VISIBLE);
        mCancelAlarm.setVisibility(Button.VISIBLE);
    }

    private void setMatrix() {
        int m = 0;
        // rows (From Station)
        mTimes = new ArrayList<>();
        for (int i = 0; i < mFromStationSpinner.getCount(); i++) {

            // columns (To Station)
            ArrayList<Integer> row = new ArrayList<>();
            for (int j = 0; j < mToStationSpinner.getCount(); j++) {
                row.add(m++);
            }
            mTimes.add(row);
        }
    }

    public class downloadScheduleXMLFile extends downloadXML<parseScheduleBetweenStations.ScheduleTrip> {
        private static final String LOG_HEAD = "ChooseRouteFragment.downloadScheduleXMLFile: ";
        public downloadScheduleXMLFile(String url) {
            super(url);
            Log.d(Config.LOG_TAG, LOG_HEAD + "Constructor() URL: " + url);
        }

        @Override
        protected ArrayList<parseScheduleBetweenStations.ScheduleTrip> parse(InputStream in) throws IOException, XmlPullParserException {
            parseScheduleBetweenStations parseSchedule = new parseScheduleBetweenStations();
            return parseSchedule.parse(in);
        }

        @Override
        protected void post(ArrayList<parseScheduleBetweenStations.ScheduleTrip> list) {
            mScheduleTripList = list;
            Log.d(Config.LOG_TAG, LOG_HEAD + "post() schedule List: ");
            if (mScheduleTripList != null) {
                parseScheduleBetweenStations.printScheduleList(mScheduleTripList);

                setCheckedList();
            } else
                Log.e(Config.LOG_TAG, LOG_HEAD + "Null schedule list recieved!");
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private static final String LOG_HEAD = "ChooseRouteFragment.ViewHolder: ";
        // each data item is just a string in this case
        private TextView mTextView;

        public ViewHolder(View v) {
            super(v);

            v.setOnClickListener(this);
            mTextView = (TextView) itemView.findViewById(R.id.scheduleItem);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(Config.LOG_TAG, LOG_HEAD + "List Clicked @ " + pos);
            mScheduleSelected = pos;

            mScheduleListAdaptor.notifyDataSetChanged();
        }

        public void bindSchedule(String scheduleToDisplay, int positionInList) {
            mTextView.setText(scheduleToDisplay);

            if(mScheduleSelected != NONE_SELECTED && positionInList == mScheduleSelected)
                mTextView.setBackgroundColor(Color.LTGRAY);
            else
                mTextView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private class CustomRecycleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private ArrayList<parseScheduleBetweenStations.ScheduleTrip> mDataset;

        // Provide a suitable constructor (depends on the kind of dataset)
        public CustomRecycleAdapter(ArrayList<parseScheduleBetweenStations.ScheduleTrip> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            // create a new view
//            TextView view = (TextView) LayoutInflater.from(parent.getContext())
//                    .inflate(android.R.layout.simple_selectable_list_item, parent, false);
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            // set the view's size, margins, paddings and layout parameters

            view.setClickable(true);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.bindSchedule(mDataset.get(position).toString(), position);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}

