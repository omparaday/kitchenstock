package com.days.kitchenstock;

import android.app.AlertDialog;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.days.kitchenstock.data.StockContentHelper;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private static final String LAST_OPENED = "last_opened";
    private static final String DATE_KEY = "date";

    private static final String RATING_PREFS = "rating_prefs";
    private static final String IS_APP_RATED = "is_app_rated";
    private static final String LAST_REQUESTED_DATE = "last_requested";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        if (Build.VERSION.SDK_INT >= 24) {
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler.getPendingJob(DailyJobService.JOB_ID) == null) {
                Log.println(Log.INFO, "omprak", "job scheduling on create");
                DailyJobService.schedule(getApplicationContext());
            } else {
                Log.println(Log.INFO, "omprak", "job exist already on create");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences lastOpenedSharedPref = getSharedPreferences(LAST_OPENED, MODE_PRIVATE);
        String lastCheckedString = lastOpenedSharedPref.getString(DATE_KEY, null);
        String todayString = StockContentHelper.DATE_FORMATTER.format(Calendar.getInstance().getTime());
        if (!todayString.equals(lastCheckedString)) {
            SharedPreferences.Editor editor = lastOpenedSharedPref.edit();
            editor.putString(DATE_KEY, todayString);
            editor.apply();
            editor.commit();
            StockContentHelper.moveStockToShopAutoAddItems(this);
            if (lastCheckedString != null) {
                checkAndShowRateDialog();
            }
        }
    }

    private void checkAndShowRateDialog() {
        if (new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())).resolveActivity(getPackageManager()) == null) {
            return;
        }
        final SharedPreferences ratingPrefs = getSharedPreferences(RATING_PREFS, MODE_PRIVATE);
        boolean isAppRated = ratingPrefs.getBoolean(IS_APP_RATED, false);
        if (!isAppRated) {
            boolean canRequest = false;
            String lastRequested = ratingPrefs.getString(LAST_REQUESTED_DATE, null);
            if (lastRequested == null) {
                canRequest = true;
            } else {
                try {
                    long elapsedDays = TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime() - StockContentHelper.DATE_FORMATTER.parse(lastRequested).getTime(), TimeUnit.MILLISECONDS);
                    if (Math.abs(elapsedDays) > 7) {
                        canRequest = true;
                    }
                } catch (ParseException e) {
                    Log.println(Log.ERROR, "omprak:HomeActivity", "error parsing last requested date.");
                    canRequest = true;
                }
            }
            if (canRequest) {
                new AlertDialog.Builder(this).setTitle(R.string.rate_app_title)
                        .setMessage(R.string.rate_app_message)
                        .setNegativeButton(R.string.remind_later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor editor = ratingPrefs.edit();
                                editor.putString(LAST_REQUESTED_DATE, StockContentHelper.DATE_FORMATTER.format(Calendar.getInstance().getTime()));
                                editor.apply();
                                editor.commit();
                            }
                        })
                        .setNeutralButton(R.string.never_ask, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor editor = ratingPrefs.edit();
                                editor.putBoolean(IS_APP_RATED, true);
                                editor.apply();
                                editor.commit();
                            }
                        })
                        .setPositiveButton(R.string.rate_now, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor editor = ratingPrefs.edit();
                                editor.putBoolean(IS_APP_RATED, true);
                                editor.apply();
                                editor.commit();
                                launchPlayStoreForReview();
                            }
                        })
                        .show();
            }

        }
    }



    private void launchPlayStoreForReview() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
