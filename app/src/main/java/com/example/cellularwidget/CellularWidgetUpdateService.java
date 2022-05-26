package com.example.cellularwidget;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

/**
 * Service to update widget from JobScheduler (to call action ACTION_APPWIDGET_UPDATE)
 */
public class CellularWidgetUpdateService extends JobService {
    private static final String TAG = CellularWidgetUpdateService.class.getSimpleName();
    private static final int UPDATE_JOB_ID = 100000073;

    public CellularWidgetUpdateService() {
    }

    /**
     * Start job service from the context
     * @param context
     */
    public static void startJobService(Context context) {
        Log.d(TAG, "JobService init");

        ComponentName componentName = new ComponentName(context, CellularWidgetUpdateService.class);
        JobInfo.TriggerContentUri triggerContentUri = new JobInfo.TriggerContentUri(
                Settings.Global.getUriFor("preferred_network_mode2"),
                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS);

        JobInfo jobInfo  = new JobInfo.Builder(UPDATE_JOB_ID, componentName)
                .addTriggerContentUri(triggerContentUri)
                .setTriggerContentUpdateDelay(1)
                .setTriggerContentMaxDelay(100)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();

        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        int result = jobScheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "JobScheduler OK");
        } else {
            Log.d(TAG, " JobScheduler fails " + result);
        }
    }

    /**
     * Stop job service from the context
     * @param context
     */
    public static void stopJobService(Context context) {
        Log.d(TAG, "JobService cancel");

        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.cancel(UPDATE_JOB_ID);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob");

        Intent intent = new Intent(this, CellularWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, CellularWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        sendBroadcast(intent);

        this.jobFinished(jobParameters, false); // false = do not reschedule

        // manual reschedule
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(UPDATE_JOB_ID);
        startJobService(getApplicationContext());

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "onStopJob");

        return true;
    }
}