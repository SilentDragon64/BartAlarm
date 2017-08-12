package com.forsakendragon.android.bartalarm.XML;

import android.util.Xml;

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
    private static final String entryRoot = "root";
    private static final String entryStations = "stations";
    private static final String entryStation = "station";
    private static final String entryName = "name";
    private static final String entryAbbreviation = "abbr";
    private static final String entryLatitude = "gtfs_latitude";
    private static final String entryLongitude = "gtfs_longitude";

    private static final String ns = null;

    // This class represents a single entry (post) in the XML feed.
    // It includes the data members: name, abbr, GPS Lat and Long
    public static class Station {
        public final String name;
        public final String abbreviation;
        public final double latitude;
        public final double longitude;

        private Station(String name, String abbreviation, double latitude, double longitude) {
            this.name = name;
            this.abbreviation = abbreviation;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public List<Station> parse(InputStream in) throws XmlPullParserException, IOException {

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<Station> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Station> entries = null;

        //Tests for first node being root, if not throws error
        parser.require(XmlPullParser.START_TAG, ns, entryRoot);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(entryStations)) {
                entries = readStations(parser);
            } else {
                parseXML.skip(parser);
            }
        }
        return entries;
    }


    private List<Station> readStations(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Station> entries = new ArrayList<>();

        //Tests for first node being the collection of stations, if not throws error
        parser.require(XmlPullParser.START_TAG, ns, entryStations);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(entryStation)) {
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
        parser.require(XmlPullParser.START_TAG, ns, entryStation);
        String name = null;
        String abbreviation = null;
        String latitude = null;
        String longitude = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String entry = parser.getName();
            if (entry.equals(entryName)) {
                name = readString(parser, entryName);
            } else if (entry.equals(entryAbbreviation)) {
                abbreviation = readString(parser, entryAbbreviation);
            } else if (entry.equals(entryLatitude)) {
                latitude = readString(parser, entryLatitude);
            } else if (entry.equals(entryLongitude)) {
                longitude = readString(parser, entryLongitude);
            } else {
                parseXML.skip(parser);
            }
        }
        return new Station(name, abbreviation, Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    // Processes Strings entries in the feed with the value title.
    private String readString(XmlPullParser parser, String title) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, title);
        String entry = parseXML.readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, title);
        return entry;
    }

    // Processes compound link tags in the feed.
//    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
//        String link = "";
//        parser.require(XmlPullParser.START_TAG, ns, "link");
//        String tag = parser.getName();
//        String relType = parser.getAttributeValue(null, "rel");
//        if (tag.equals("link")) {
//            if (relType.equals("alternate")) {
//                link = parser.getAttributeValue(null, "href");
//                parser.nextTag();
//            }
//        }
//        parser.require(XmlPullParser.END_TAG, ns, "link");
//        return link;
//    }
}
