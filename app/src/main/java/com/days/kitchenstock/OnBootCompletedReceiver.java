package com.days.kitchenstock;

import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;
        if (scheduler.getPendingJob(DailyJobService.JOB_ID) == null) {
            Log.println(Log.INFO, "omprak", "scheduling job on boot completed");
            DailyJobService.schedule(context);
        } else {
            Log.println(Log.INFO, "omprak", "not scheduling job on boot completed");
        }
    }
}
