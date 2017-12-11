package com.forsakendragon.android.bartalarm.XML;

import android.util.Log;
import android.util.Xml;

import com.forsakendragon.android.bartalarm.Config;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Arion on 8/24/2017.
 */


public class parseScheduleBetweenStations {
    private static final String nameSpace = null;
    private static final String LOG_HEAD = "parseScheduleBetweenStations: ";


    // This class represents a single entry (post) in the XML feed.
    // It includes the data members: name, abbr, GPS Lat and Long
    public static class ScheduleTrip {
        private static final String LOG_HEAD = "parseScheduleBetweenStations.ScheduleTrip: ";
        public static class Leg {
            public final String mOrigin;
            public final String mOriginTime;
            public final String mOriginDate;
            public final String mDestination;
            public final String mDestinationTime;
            public final String mDestinationDate;
            public final String mTrainFinalDestination;

            public Leg(String origin, String destination, String originTime, String destinationTime,
                       String originDate, String destinationDate, String trainFinalDestination) {
                mOrigin = origin;
                mDestination = destination;
                mOriginTime = originTime;
                mDestinationTime = destinationTime;
                mOriginDate = originDate;
                mDestinationDate = destinationDate;
                mTrainFinalDestination = trainFinalDestination;
            }

            // Needed for AppCompatSpinner Arraylist to display station names
            @Override
            public String toString() {
                return mOrigin + "@" + mOriginDate + " " + mOriginTime + " to " + mDestination + "@" +
                        mDestinationDate + " " + mDestinationTime + " FinalDest: " + mTrainFinalDestination;
            }
        }

        public final String mOrigin;
        public final String mDestination;
        public final ArrayList<Leg> mList;

        private ScheduleTrip(String origin, String destination, ArrayList<Leg> list) {
            mOrigin = origin;
            mDestination = destination;
            mList = list;

            if (mList == null || mList.size() == 0 || mList.size() > 2) {
                Log.e(Config.LOG_TAG, LOG_HEAD + "Invalid list of train legs: " + mList);
            }
        }

        // Needed for AppCompatSpinner Arraylist to display station names
        @Override
        public String toString() {
            if (mList.size() == 1)
                return "Departing: " + mOrigin + " at " + mList.get(0).mOriginTime +
                        "\nArriving: " + mDestination + " at " + mList.get(0).mDestinationTime;
            else
                return "Departing: " + mOrigin + " at " + mList.get(0).mOriginTime +
                        "\nArriving at Tranfer: " + mList.get(0).mDestination + " at " + mList.get(0).mDestinationTime +
                        "\nDeparting Transfer: " + mList.get(1).mOrigin + " at " + mList.get(1).mOriginTime +
                        "\nArriving: " + mDestination + " at " + mList.get(1).mDestinationTime;
        }
    }

    // TODO: Testing Method
    public static void printScheduleList(ArrayList<ScheduleTrip> list) {
        Log.d(Config.LOG_TAG, LOG_HEAD + "printScheduleList(): ");
        if (list != null) {
            for (ScheduleTrip s : list) {
                Log.d(Config.LOG_TAG, LOG_HEAD + s.toString());
                if (s.mList != null) {
                    int i = 1;
                    for (ScheduleTrip.Leg l : s.mList) {
                        Log.d(Config.LOG_TAG, LOG_HEAD + "Leg " + i++ + ": " + l.toString());
                    }
                }
                else
                    Log.d(Config.LOG_TAG, LOG_HEAD + "Empty Schedule Leg List!");
            }
        }
        else
            Log.d(Config.LOG_TAG, LOG_HEAD + "Empty Schedule List!");
    }

    public ArrayList<ScheduleTrip> parse(InputStream in) throws XmlPullParserException, IOException {
        Log.d(Config.LOG_TAG, LOG_HEAD + "parse(): ");
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        parser.nextTag();
        return readFeed(parser);
    }

    private ArrayList<ScheduleTrip> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<ScheduleTrip> entries = null;

        //Tests for first node being root, if not throws error
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_ROOT);

        while (parser.next() != XmlPullParser.END_TAG) {
            String name = parser.getName();
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // Starts by looking for the entry tag
            if (name.equals(Config.XML_ENTRY_SCHEDULE)) {
                entries = readSchedule(parser);
            } else {
                parseXML.skip(parser);
            }
        }
        return entries;
    }

    private ArrayList<ScheduleTrip> readSchedule(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<ScheduleTrip> entries = null;

        //Tests for first node being root, if not throws error
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_SCHEDULE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(Config.XML_ENTRY_REQUEST)) {
                entries = readRequest(parser);
            } else {
                parseXML.skip(parser);
            }
        }
        return entries;
    }


    private ArrayList<ScheduleTrip> readRequest(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<ScheduleTrip> entries = new ArrayList<>();

        //Tests for first node being the collection of stations, if not throws error
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_REQUEST);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(Config.XML_ENTRY_TRIP)) {
                entries.add(readTripEntry(parser));
            } else {
                parseXML.skip(parser);
            }
        }
        return entries;
    }

    // Parses the contents of an Station entry. If it encounters a name, abbreviation, latitude, or
    // longitude tag, it hands it off to the readString method for processing. Otherwise, it skips
    // the tag.
    private ScheduleTrip readTripEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_TRIP);
        String tag = null;
        int type;
        String origin;
        String destination;
        String originTime;
        String destinationTime;

        ArrayList<ScheduleTrip.Leg> scheduleLegs = new ArrayList<>();

        tag = parser.getName();
        origin = parser.getAttributeValue(null, Config.XML_TAG_ORIGIN);
        destination = parser.getAttributeValue(null, Config.XML_TAG_DESTINATION);

        parser.next();
        tag = parser.getName();
        type = parser.getEventType();
        if (parser.getName().equals(Config.XML_ENTRY_FARES) && parser.getEventType() == XmlPullParser.START_TAG) {
            parseXML.skip(parser);
        }

        parser.next();
        tag = parser.getName();
        type = parser.getEventType();
        while (parser.getName().equals(Config.XML_ENTRY_LEG) && parser.getEventType() == XmlPullParser.START_TAG) {
            String legOrigin = parser.getAttributeValue(null, Config.XML_TAG_ORIGIN);
            String legDestination = parser.getAttributeValue(null, Config.XML_TAG_DESTINATION);
            String legOriginTime = parser.getAttributeValue(null, Config.XML_TAG_ORIGIN_TIME);
            String legDestinationTime = parser.getAttributeValue(null, Config.XML_TAG_DESTINATION_TIME);
            String legOriginDate = parser.getAttributeValue(null, Config.XML_TAG_ORIGIN_DATE);
            String legDestinationDate = parser.getAttributeValue(null, Config.XML_TAG_DESTINATION_DATE);
            String legFinalTrainDestination = parser.getAttributeValue(null, Config.XML_TAG_TRAIN_DESTINATION);

            if (legOriginTime == null || legDestinationTime == null || legOriginDate == null || legDestinationDate == null)
                Log.e(Config.LOG_TAG, LOG_HEAD + "Null Time Found - Origin: " + legOriginDate + " " + legOriginTime +
                        " Destination: " + legDestinationDate + " " + legDestinationTime);


            scheduleLegs.add(new ScheduleTrip.Leg(legOrigin, legDestination, legOriginTime,
                    legDestinationTime, legOriginDate, legDestinationDate, legFinalTrainDestination));

            parser.next();
            tag = parser.getName();
            type = parser.getEventType();
            parser.require(XmlPullParser.END_TAG, nameSpace, Config.XML_ENTRY_LEG);

            parser.next();
            tag = parser.getName();
            type = parser.getEventType();
        }


        tag = parser.getName();
        type = parser.getEventType();
        parser.require(XmlPullParser.END_TAG, nameSpace, Config.XML_ENTRY_TRIP);
        return new ScheduleTrip(origin, destination, scheduleLegs);
    }
}