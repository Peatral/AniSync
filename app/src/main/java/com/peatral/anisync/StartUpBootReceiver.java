package com.peatral.anisync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartUpBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SyncJobService.scheduleByPreference();
        }
    }
}