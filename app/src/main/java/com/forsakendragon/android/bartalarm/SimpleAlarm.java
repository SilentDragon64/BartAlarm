package com.forsakendragon.android.bartalarm;

import android.content.Context;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Arion on 4/25/2017.
 */

public class SimpleAlarm extends WakefulBroadcastReceiver {
    private static final String EXTRA_ALARM_SECONDS = "EXTRA_ALARM_SECONDS";
    private static final String EXTRA_ALARM_TYPE = "EXTRA_ALARM_TYPE";
    private static final String EXTRA_ALARM_TYPE_TOAST = "EXTRA_ALARM_TYPE_TOAST";

    private static final String LOG_HEAD = "SimpleAlarm: ";

    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager mAlarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent mAlarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Config.LOG_TAG, LOG_HEAD + "onReceive()");
        Bundle extras = intent.getExtras();
        long secondsToAlarmIn = extras.getLong(EXTRA_ALARM_SECONDS);
        String alarmType = extras.getString(EXTRA_ALARM_TYPE);

        Log.d(Config.LOG_TAG, LOG_HEAD + "Alarming after " + secondsToAlarmIn + " seconds");
        Log.d(Config.LOG_TAG, LOG_HEAD + "Alarm type:  " + alarmType);
        if (alarmType.equals(EXTRA_ALARM_TYPE_TOAST))
            Toast.makeText(context, "Alarm!", Toast.LENGTH_SHORT).show();
    }

    public void setAlarm(Context context, long secondsToAlarmIn) {
        Log.d(Config.LOG_TAG, LOG_HEAD + "setAlarm() Alarm in " + secondsToAlarmIn + " seconds");
        if (secondsToAlarmIn <= 0) {
            Log.e(Config.LOG_TAG, LOG_HEAD + "Alarm not valid, value 0 or less " + secondsToAlarmIn);
            return;
        }

        mAlarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SimpleAlarm.class);
        Bundle extras = new Bundle();
        extras.putLong(EXTRA_ALARM_SECONDS, secondsToAlarmIn);
        extras.putString(EXTRA_ALARM_TYPE, EXTRA_ALARM_TYPE_TOAST);
        intent.putExtras(extras);
        mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        mAlarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000 * secondsToAlarmIn, mAlarmIntent);
    }

    public void cancelAlarm() {
        Log.d(Config.LOG_TAG, "Cancelling Alarm");
        if (mAlarmMgr!= null) {
            mAlarmMgr.cancel(mAlarmIntent);
            Log.d(Config.LOG_TAG, "Alarm Cancelled");
        }

    }
}
