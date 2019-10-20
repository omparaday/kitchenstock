package com.days.kitchenstock;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.days.kitchenstock.data.StockContentHelper;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private static final String LAST_OPENED = "last_opened";
    private static final String DATE_KEY = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        JobScheduler scheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;
        if (scheduler.getPendingJob(DailyJobService.JOB_ID) == null) {
            Log.println(Log.INFO, "omprak", "job scheduling on create");
            DailyJobService.schedule(getApplicationContext());
        } else {
            Log.println(Log.INFO, "omprak", "job exist already on create");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences lastOpenedSharedPref = getSharedPreferences(LAST_OPENED, MODE_PRIVATE);
        String lastCheckedString = lastOpenedSharedPref.getString(DATE_KEY, null);
        String todayString = StockContentHelper.DATE_FORMATTER.format(Calendar.getInstance().getTime());
        if (!todayString.equals(lastCheckedString)) {
            Log.println(Log.INFO, "omprak", "date changed " + todayString + lastCheckedString);
            SharedPreferences.Editor editor = lastOpenedSharedPref.edit();
            editor.putString(DATE_KEY, todayString);
            editor.apply();
            editor.commit();
            StockContentHelper.moveStockToShopAutoAddItems(this);
        }
    }
}
