package com.forsakendragon.android.bartalarm.XML;

import android.os.AsyncTask;
import android.util.Log;

import com.forsakendragon.android.bartalarm.Config;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Arion on 8/26/2017.
 */

//Page 494 return types, 496 for progress updates
public abstract class downloadXML<T> extends AsyncTask<Void, Void, ArrayList<T>> {
    private String mURL;
    protected abstract ArrayList<T> parse(InputStream in) throws IOException, XmlPullParserException;
    protected abstract void post(ArrayList<T> list);

    public downloadXML(String url) {
        super();
        mURL = url;
    }

    @Override
    protected ArrayList<T> doInBackground(Void... params) {
        Log.d(Config.LOG_TAG, "downloadXML<T>.doInBackground(Void... params)");
        try {
            return downloadAndParse();
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
    protected void onPostExecute(ArrayList<T> list) {
        //Runs in main thread, not async, update UI
        Log.d(Config.LOG_TAG, "downloadXML<T>.onPostExecute");
        post(list);
    }

    private ArrayList<T> downloadAndParse() throws IOException, XmlPullParserException {
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
                throw new IOException(connection.getResponseMessage() + ": with " + Config.STATION_LIST_COMMAND);
            }

            in = connection.getInputStream();
            //publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);

            return parse(in);
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
}
