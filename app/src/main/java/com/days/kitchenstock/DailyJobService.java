package com.days.kitchenstock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.days.kitchenstock.data.StockContentHelper;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DailyJobService extends JobService {
    public static final int JOB_ID = 5;
    private static final long HALF_DAY_INTERVAL = 12 * 60 * 60 * 1000L; // 1 Day
    private static final String LAST_CHECKED = "last_checked";
    public static final String DATE_KEY = "date";

    public static void schedule(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName =
                new ComponentName(context, DailyJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, componentName);
        builder.setPeriodic(HALF_DAY_INTERVAL);
        jobScheduler.schedule(builder.build());
        Log.println(Log.INFO, "omprak", "successful in scheduling job "+ HALF_DAY_INTERVAL);
    }

    public static void cancel(Context context) {
        Log.println(Log.INFO, "omprak", "cancelled");
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.println(Log.INFO, "omprak", "onStartJob");
        SharedPreferences lastCheckedSharedPref = getSharedPreferences(LAST_CHECKED, MODE_PRIVATE);
        String lastCheckedString = lastCheckedSharedPref.getString(DATE_KEY, null);
        Date lastChecked;
        try {
            lastChecked = lastCheckedString == null ? null : StockContentHelper.DATE_FORMATTER.parse(lastCheckedString);
        } catch (ParseException e) {
            lastChecked = null;
        }
        int expired = StockContentHelper.getExpiredSinceCount(getApplicationContext(), lastChecked);
        int expiringSoon = StockContentHelper.getExpiringSoonSinceCount(getApplicationContext(), lastChecked);
        if (expired != 0 || expiringSoon != 0) {
            Intent intent = new Intent().setComponent(new ComponentName("com.days.kitchenstock", "com.days.kitchenstock.HomeActivity"));
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.expiry_notification_title))
                    .setContentText(getString(R.string.expiry_notification_message, expired, expiringSoon))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true).build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
            Log.println(Log.INFO, "omprak", "Notified");
        }
        SharedPreferences.Editor editor = lastCheckedSharedPref.edit();
        editor.putString(DATE_KEY, StockContentHelper.DATE_FORMATTER.format(Calendar.getInstance().getTime()));
        editor.apply();
        editor.commit();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}