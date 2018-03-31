package com.oliver.bathurst.individualproject;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class GCMLocationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            String[] extra = bundle.getStringArray("TO");
            if(extra != null && extra[0] != null) {
                new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{extra[0],
                        new PostPHP(this).getEmailString()});
            }
        }
        return START_NOT_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
