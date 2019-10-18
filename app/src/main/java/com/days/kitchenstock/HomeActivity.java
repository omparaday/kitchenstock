package com.days.kitchenstock;

import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.days.kitchenstock.data.StockContentHelper;

public class HomeActivity extends AppCompatActivity {


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
        StockContentHelper.moveStockToShopAutoAddItems(this);
    }
}
