package com.forsakendragon.android.bartalarm.XML;

import android.util.Log;
import android.util.Xml;

import com.forsakendragon.android.bartalarm.Config;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arion on 5/8/2017.
 */

public class parseBARTStations {
    private static final String nameSpace = null;

    // This class represents a single entry (post) in the XML feed.
    // It includes the data members: name, abbr, GPS Lat and Long
    public static class Station {
        public final String name;
        public final String abbreviation;
        public final double latitude;
        public final double longitude;

        // TODO: Change back to private after testing splash
        public Station(String name, String abbreviation, double latitude, double longitude) {
            this.name = name;
            this.abbreviation = abbreviation;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Needed for AppCompatSpinner Arraylist to display station names
        @Override
        public String toString() {
            return name;
        }
    }

    // TODO: Testing Method
    public static void printStationList(ArrayList<Station> list) {
        Log.d(Config.LOG_TAG, "Station List: ");
        for (Station s: list) {
            Log.d(Config.LOG_TAG, s.name + " " + s.abbreviation + " " + s.latitude + " " + s.latitude);
        }
    }

    public ArrayList<Station> parse(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        parser.nextTag();
        return readFeed(parser);
    }

    private ArrayList<Station> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Station> entries = null;

        //Tests for first node being root, if not throws error
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_ROOT);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(Config.XML_ENTRY_STATIONS)) {
                entries = readStations(parser);
            } else {
                parseXML.skip(parser);
            }
        }
        return entries;
    }


    private ArrayList<Station> readStations(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Station> entries = new ArrayList<>();

        //Tests for first node being the collection of stations, if not throws error
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_STATIONS);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(Config.XML_ENTRY_STATION)) {
                entries.add(readStationEntry(parser));
            } else {
                parseXML.skip(parser);
            }
        }
        return entries;
    }

    // Parses the contents of an Station entry. If it encounters a name, abbreviation, latitude, or
    // longitude tag, it hands it off to the readString method for processing. Otherwise, it skips
    // the tag.
    private Station readStationEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, nameSpace, Config.XML_ENTRY_STATION);
        String name = null;
        String abbreviation = null;
        String latitude = null;
        String longitude = null;

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
        return new Station(name, abbreviation, Double.parseDouble(latitude), Double.parseDouble(longitude));
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
