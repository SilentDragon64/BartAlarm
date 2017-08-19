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
    private static final String EXTRA_ALARM_MINUTES = "EXTRA_ALARM_MINUTES";
    private static final String EXTRA_ALARM_TYPE = "EXTRA_ALARM_TYPE";
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager mAlarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent mAlarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        int minutesToAlarmIn = extras.getInt(EXTRA_ALARM_MINUTES);
        String alarmType = extras.getString(EXTRA_ALARM_TYPE);

        Log.d(Config.LOG_TAG, "Alarming after " + minutesToAlarmIn + " minutes");
        Toast.makeText(context, "Alarm!", Toast.LENGTH_SHORT).show();
    }

    public void setAlarm(Context context, int minutesToAlarmIn) {
        Log.d(Config.LOG_TAG, "Alarm in " + minutesToAlarmIn + " minutes");

        mAlarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SimpleAlarm.class);
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_ALARM_MINUTES, minutesToAlarmIn);
        extras.putString(EXTRA_ALARM_TYPE, "test");
        intent.putExtras(extras);
        mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

//        mAlarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() +
//                        60 * 1000 * minutesToAlarmIn, mAlarmIntent);

        // Changed from minutes to seconds for testing
        mAlarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000 * minutesToAlarmIn, mAlarmIntent);
    }

    public void cancelAlarm() {
        Log.d(Config.LOG_TAG, "Cancelling Alarm");
        if (mAlarmMgr!= null) {
            mAlarmMgr.cancel(mAlarmIntent);
            Log.d(Config.LOG_TAG, "Alarm Cancelled");
        }

    }
}
