package com.days.kitchenstock;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.days.kitchenstock.data.StockContentHelper;

public class DailyJobService extends JobService {
    private static final int JOB_ID = 1;
    private static final long ONE_DAY_INTERVAL = 24 * 60 * 60 * 1000L; // 1 Day

    public static void schedule(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName =
                new ComponentName(context, DailyJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, componentName);
        builder.setPeriodic(ONE_DAY_INTERVAL);
        jobScheduler.schedule(builder.build());
        Log.println(Log.INFO, "omprak", "successful in scheduling job");
    }

    public static void cancel(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.println(Log.INFO, "omprak", "starting job");
        StockContentHelper.moveStockToShopAutoAddItems(getApplicationContext());
        int expired = StockContentHelper.getExpiredSinceCount(getApplicationContext(), null);
        int expiringSoon = StockContentHelper.getExpiringSoonSinceCount(getApplicationContext(), null);
        Log.println(Log.INFO, "omprak", "Expired " + expired);
        Log.println(Log.INFO, "omprak", "Expiring soon " + expiringSoon);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}