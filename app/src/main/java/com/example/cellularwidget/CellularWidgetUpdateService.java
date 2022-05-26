package com.example.cellularwidget;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;

/**
 * Service to update widget from JobScheduler (to call action ACTION_APPWIDGET_UPDATE)
 */
public class CellularWidgetUpdateService extends JobService {
    public CellularWidgetUpdateService() {
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Intent intent = new Intent(this, CellularWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, CellularWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        sendBroadcast(intent);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}