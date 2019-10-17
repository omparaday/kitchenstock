package com.days.kitchenstock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.days.kitchenstock.data.StockContentHelper;
import com.days.kitchenstock.data.StockContentProvider;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Button mAddItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        mAddItem = findViewById(R.id.add_item);
        mAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddItemDialog(HomeActivity.this).show();
            }
        });
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
