package com.days.kitchenstock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.println(Log.INFO, "omprak", "scheduling job on boot completed");
        DailyJobService.schedule(context);
    }
}
