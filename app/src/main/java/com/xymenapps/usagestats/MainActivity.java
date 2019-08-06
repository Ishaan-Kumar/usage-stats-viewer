package com.xymenapps.usagestats;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Joiner;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    String currentApp;
    UsageStatsManager usm = null;
    Map<String, String> appsOpened;
    long todayInMs;
    long hourToMs = 60 * 60 * 1000;
    TextView result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
        boolean usage_permission = mode == MODE_ALLOWED;
        appsOpened = new HashMap<>();
        result = findViewById(R.id.result_textview);
        result.setMovementMethod(new ScrollingMovementMethod());
        result.setTextSize(15);

        Calendar today = Calendar.getInstance();
        long elapsed = today.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000
                + today.get(Calendar.MINUTE) * 60 * 1000
                + today.get(Calendar.MILLISECOND) * 1000;
        todayInMs = today.getTimeInMillis() - elapsed;
        Log.d(TAG, "onCreate: Today in ms : " + todayInMs);
        Log.d(TAG, "onCreate: Today: " + new Date(todayInMs));


        if (usage_permission) {
            Log.d(TAG, "onCreate: Permission granted");
            usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
//            findAppsOpened(System.currentTimeMillis() - 3600 * 1000, System.currentTimeMillis());
        } else {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            Log.d(TAG, "onCreate: Permission denied");
            Toast.makeText(this, "Grant access and relaunch activity", Toast.LENGTH_LONG).show();
        }
    }

    private void findAppsOpened(long startTime, long endTime) {
        Log.d(TAG, "findAppsOpened() called with: startTime = [" + startTime + "], endTime = [" + endTime + "]");
//            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
//            if (appList != null && appList.size() > 0) {
//                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//                for (UsageStats usageStats : appList) {
//                    if (usageStats.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                        Log.d(TAG, "First TimeStamp: " + new Date(usageStats.getFirstTimeStamp()));
//                        Log.d(TAG, "Last TimeStamp: " + new Date(usageStats.getLastTimeStamp()));
//                        Log.d(TAG, "Last Time Used: " + new Date(usageStats.getLastTimeUsed()));
//                        Log.d(TAG, "Totol foreground time: " + usageStats.getTotalTimeInForeground() / 1000 + " seconds");
//                        Log.d(TAG, "describeContents: " + usageStats.describeContents());
//                    }
//                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
//                }
//                if (mySortedMap != null && !mySortedMap.isEmpty()) {
//                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
//                }
//        Log.e("CURRENT APP", "Current App in foreground is: " + currentApp);
//            }
        Log.d(TAG, "findAppsOpened: FROM: " + new Date(startTime) + " TO: " + new Date(endTime));
        try {
            UsageEvents usageEvents = usm.queryEvents(startTime, endTime);
            UsageEvents.Event event = new UsageEvents.Event();
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == 1) {
                    String packageName = event.getPackageName();
                    long timestamp = event.getTimeStamp();
                    Calendar eventTime = Calendar.getInstance();
                    eventTime.setTimeInMillis(timestamp);
//                    Log.d(TAG, packageName + " : " + new Date(timestamp));
                    appsOpened.put(packageName, eventTime.get(Calendar.HOUR) + ":" + eventTime.get(Calendar.MINUTE) + " " + (eventTime.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM"));
                }
            }
            displayResult();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Error!!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void displayResult() {
        Log.d(TAG, "displayResult() called");
        result.setText(Joiner.on("\n").withKeyValueSeparator(" : ").join(appsOpened));
    }

    public void find(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button2:
                findAppsOpened(todayInMs, todayInMs + 4 * hourToMs);
                break;
            case R.id.button3:
                findAppsOpened(todayInMs + 4 * hourToMs, todayInMs + 8 * hourToMs);
                break;
            case R.id.button4:
                findAppsOpened(todayInMs + 8 * hourToMs, todayInMs + 12 * hourToMs);
                break;
            case R.id.button5:
                findAppsOpened(todayInMs + 12 * hourToMs, todayInMs + 16 * hourToMs);
                break;
            case R.id.button6:
                findAppsOpened(todayInMs + 16 * hourToMs, todayInMs + 20 * hourToMs);
                break;
            case R.id.button7:
                findAppsOpened(todayInMs + 20 * hourToMs, todayInMs + 24 * hourToMs);
                break;
        }
    }
}
