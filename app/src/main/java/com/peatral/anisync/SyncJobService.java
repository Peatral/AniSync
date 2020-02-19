package com.peatral.anisync;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.peatral.anisync.graphql.Sync;

public class SyncJobService extends JobService {
    private static final int JOB_ID = 1;
    public static final long FIFTEEN_MINUTES_INTERVAL = 15 * 60 * 1000L; // 5 Minutes
    public static final long THIRTY_MINUTES_INTERVAL = 30 * 60 * 1000L; // 30 Minutes
    public static final long ONE_HOUR_INTERVAL = 60 * 60 * 1000L; // 30 Minutes
    public static final long SIX_HOUR_INTERVAL = 6 * 60 * 60 * 1000L; // 30 Minutes
    public static final long ONE_DAY_INTERVAL = 24 * 60 * 60 * 1000L; // 1 Day
    public static final long ONE_WEEK_INTERVAL = 7 * 24 * 60 * 60 * 1000L; // 1 Week

    public static void schedule(Context context, long intervalMillis) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName =
                new ComponentName(context, SyncJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(intervalMillis)
                .setPersisted(true);
        jobScheduler.schedule(builder.build());
    }

    public static void cancel(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        Sync.getInstance().sync();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public static void scheduleByPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        String value = prefs.getString(Settings.PREF_PERIODIC_SYNC, "none");
        if (value.equals("none")) {
            SyncJobService.cancel(App.getContext());
        } else if (value.equals("15_min")) {
            SyncJobService.schedule(App.getContext(), SyncJobService.FIFTEEN_MINUTES_INTERVAL);
        }else if (value.equals("30_min")) {
            SyncJobService.schedule(App.getContext(), SyncJobService.THIRTY_MINUTES_INTERVAL);
        }else if (value.equals("hourly")) {
            SyncJobService.schedule(App.getContext(), SyncJobService.ONE_HOUR_INTERVAL);
        }else if (value.equals("6_hour")) {
            SyncJobService.schedule(App.getContext(), SyncJobService.SIX_HOUR_INTERVAL);
        }else if (value.equals("daily")) {
            SyncJobService.schedule(App.getContext(), SyncJobService.ONE_DAY_INTERVAL);
        } else if (value.equals("weekly")) {
            SyncJobService.schedule(App.getContext(), SyncJobService.ONE_WEEK_INTERVAL);
        }
    }
}
