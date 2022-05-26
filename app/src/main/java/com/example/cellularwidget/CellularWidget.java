package com.example.cellularwidget;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * Cellular widget that can open com.android.phone.settings.PreferredNetworkTypeListPreference
 * activity to change preferred_network_mode2
 */
public class CellularWidget extends AppWidgetProvider {
    private static final String OnClick = "OnClickTag";
    private static final int APP_WIDGET_UPDATE_JOB_ID = 100000073;

    /**
     * Return id of network logo image
     * @param context
     * @return
     */
    private int getLogoNetworkImage(Context context) {
        try {
            int network_mode2 = Settings.Global.getInt(context.getContentResolver(), "preferred_network_mode2");
            switch (network_mode2) {
                case 1:
                    return R.drawable.ic_logo_network_2g;
                case 18:
                    return R.drawable.ic_logo_network_3g;
                case 20:
                    return R.drawable.ic_logo_network_4g;
                default:
                    return R.drawable.ic_logo_network;
            }
        }
        catch (Settings.SettingNotFoundException e)
        {
            e.printStackTrace();
        }

        return R.drawable.ic_logo_network;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (OnClick.equals(intent.getAction())) {

            updateWidget(context);
            scheduleJob(context);

            Intent intentActionMain = new Intent(Intent.ACTION_MAIN);
            intentActionMain.setComponent(new ComponentName("com.android.phone",
                    "com.android.phone.settings.PreferredNetworkTypeListPreference"));
            intentActionMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK  | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentActionMain);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        scheduleJob(context);
    }

    /**
     * Schedule job from the context to update the widget on preferred_network_mode2 changed
     * @param context
     */
    private void scheduleJob(Context context)
    {
        ComponentName componentName = new ComponentName(context, CellularWidgetUpdateService.class);
        JobInfo.TriggerContentUri triggerContentUri = new JobInfo.TriggerContentUri(
                Settings.Global.getUriFor("preferred_network_mode2"),
                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS);

        JobInfo jobInfo  = new JobInfo.Builder(APP_WIDGET_UPDATE_JOB_ID, componentName)
                .addTriggerContentUri(triggerContentUri)
                .setTriggerContentUpdateDelay(1)
                .setTriggerContentMaxDelay(100)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();

        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.schedule(jobInfo);
    }


    /**
     * Update widget from the context
     * @param context
     */
    private void updateWidget(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds =
                appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int id : appWidgetIds) {
            RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
            widgetView.setImageViewResource(R.id.logo_network, getLogoNetworkImage(context));
            widgetView.setOnClickPendingIntent(R.id.logo_network, getPendingSelfIntent(context, OnClick));

            appWidgetManager.updateAppWidget(id, widgetView);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName = new ComponentName(context.getPackageName(),getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);

        if (appWidgetIds.length == 0) {

            JobScheduler jobScheduler = (JobScheduler)
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            jobScheduler.cancel(APP_WIDGET_UPDATE_JOB_ID);
        }
    }

    /**
     * Returns PendingIntent for the action from the context
     * @param context
     * @param action
     * @return
     */
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
