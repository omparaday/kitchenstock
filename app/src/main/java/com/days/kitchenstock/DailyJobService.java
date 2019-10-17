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
import android.util.Log;

import com.days.kitchenstock.data.StockContentHelper;

public class DailyJobService extends JobService {
    public static final int JOB_ID = 4;
    private static final long ONE_DAY_INTERVAL = 60 * 1000L; // 1 Day

    public static void schedule(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName =
                new ComponentName(context, DailyJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, componentName);
        builder.setPeriodic(ONE_DAY_INTERVAL);
        jobScheduler.schedule(builder.build());
        Log.println(Log.INFO, "omprak", "successful in scheduling job "+ ONE_DAY_INTERVAL);
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
        int expired = StockContentHelper.getExpiredSinceCount(getApplicationContext(), null);
        int expiringSoon = StockContentHelper.getExpiringSoonSinceCount(getApplicationContext(), null);
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
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}