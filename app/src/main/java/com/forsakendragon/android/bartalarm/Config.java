package com.forsakendragon.android.bartalarm;

/**
 * Created by Arion on 8/12/2017.
 */

public class Config {
    public static final String LOG_TAG = "BART_ALARM";

    public static final String ARGS_STATION_LIST = "ARGS_STATION_LIST";
    public static final String ARGS_IS_CONNECTED = "ARGS_IS_CONNECTED";

    public static final String BART_KEY = "MW9S-E7SL-26DU-VV8V";
    public static final String BART_API = "http://api.bart.gov/api/";

    public static final String STATION_LIST_COMMAND = BART_API + "stn.aspx?cmd=stns&key=" + BART_KEY;
    public static final String XML_ENTRY_ROOT = "root";
    public static final String XML_ENTRY_STATIONS = "stations";
    public static final String XML_ENTRY_STATION = "station";
    public static final String XML_ENTRY_NAME = "name";
    public static final String XML_ENTRY_ABBREVIATION = "abbr";
    public static final String XML_ENTRY_LATITUDE = "gtfs_latitude";
    public static final String XML_ENTRY_LONGITUDE = "gtfs_longitude";

    public static final String SCHEDUAL_DEPART_COMMAND_1 = BART_API + "sched.aspx?cmd=depart&orig=";
    public static final String SCHEDUAL_DEPART_COMMAND_2 = "&dest=";
    public static final String SCHEDUAL_DEPART_COMMAND_3 = "&a=4&b=1&time=now&date=today&key=" + BART_KEY;
    public static final String XML_ENTRY_SCHEDULE = "schedule";
    public static final String XML_ENTRY_REQUEST = "request";
    public static final String XML_ENTRY_TRIP = "trip";
    public static final String XML_TAG_ORIGIN = "origin";
    public static final String XML_TAG_DESTINATION = "destination";
    public static final String XML_TAG_ORIGIN_TIME = "origTimeMin";
    public static final String XML_TAG_ORIGIN_DATE = "origTimeDate";
    public static final String XML_TAG_DESTINATION_TIME = "destTimeMin";
    public static final String XML_TAG_DESTINATION_DATE = "destTimeDate";
    public static final String XML_ENTRY_LEG = "leg";
    public static final String XML_TAG_TRAIN_DESTINATION = "trainHeadStation";
    public static final String XML_ENTRY_FARES = "fares";
}
