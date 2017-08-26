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

    // This class represents a single entry (post) in the XML feed.
    // It includes the data members: name, abbr, GPS Lat and Long
    public static class Schedule {
        public final String originTime;
        public final String destinationTime;


        // TODO: Change back to private after testing splash
        public Schedule(String originTime, String destinationTime) {
            this.originTime = originTime;
            this.destinationTime = destinationTime;

        }

        // Needed for AppCompatSpinner Arraylist to display station names
        @Override
        public String toString() {
            return originTime + " " + destinationTime;
        }
    }

    // TODO: Testing Method
    public static void printScheduleList(ArrayList<Schedule> list) {
        Log.d(Config.LOG_TAG, "Schedule List: ");
        for (Schedule s: list) {
            Log.d(Config.LOG_TAG, s.toString());
        }
    }

    public ArrayList<Schedule> parse(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        parser.nextTag();
        return readFeed(parser);
    }

    private ArrayList<Schedule> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Schedule> entries = null;

        //Tests for first node being root, if not throws error
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_ROOT);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(Config.XML_ENTRY_SCHEDULE)) {
                entries = readSchedule(parser);
            } else {
                parseXML.skip(parser);
            }
        }
        return entries;
    }

    private ArrayList<Schedule> readSchedule(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Schedule> entries = null;

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


    private ArrayList<Schedule> readRequest(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Schedule> entries = new ArrayList<>();

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
    private Schedule readTripEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_TRIP);
        String name = null;
        String abbreviation = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String entry = parser.getName();
            if (entry.equals(Config.XML_ENTRY_NAME)) {
                name = readString(parser, Config.XML_ENTRY_NAME);
            } else if (entry.equals(Config.XML_ENTRY_ABBREVIATION)) {
                abbreviation = readString(parser, Config.XML_ENTRY_ABBREVIATION);
            } else if (entry.equals(Config.XML_ENTRY_LATITUDE)) {
                latitude = readString(parser, Config.XML_ENTRY_LATITUDE);
            } else if (entry.equals(Config.XML_ENTRY_LONGITUDE)) {
                longitude = readString(parser, Config.XML_ENTRY_LONGITUDE);
            } else {
                parseXML.skip(parser);
            }
        }
        return new Schedule(name, abbreviation);
    }

    // Processes Strings entries in the feed with the value title.
    private String readString(XmlPullParser parser, String title) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, nameSpace, title);
        String entry = parseXML.readText(parser);
        parser.require(XmlPullParser.END_TAG, nameSpace, title);
        return entry;
    }

    // Processes compound link tags in the feed.
//    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
//        String link = "";
//        parser.require(XmlPullParser.START_TAG, nameSpace, "link");
//        String tag = parser.getName();
//        String relType = parser.getAttributeValue(null, "rel");
//        if (tag.equals("link")) {
//            if (relType.equals("alternate")) {
//                link = parser.getAttributeValue(null, "href");
//                parser.nextTag();
//            }
//        }
//        parser.require(XmlPullParser.END_TAG, nameSpace, "link");
//        return link;
//    }
}
