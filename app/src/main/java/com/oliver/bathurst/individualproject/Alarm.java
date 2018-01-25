package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import java.io.IOException;

/**
 * Created by Oliver on 08/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class Alarm {
    private final String duration, ringtone;
    private final Context c;
    private final int volume;

    Alarm(Context context, int ringVol, String ringDur, String ringtone){
        this.c = context;
        this.volume = ringVol;
        this.duration = ringDur;
        this.ringtone = ringtone;
    }

    void ring(){
        int durationInt;
        if(duration != null) {
            try {
                durationInt = Integer.parseInt(duration);
            } catch (NumberFormatException e) {
                durationInt = 20;
            }
        }else{
            durationInt = 20;
        }

        Uri ringtoneUri;
        try{
            ringtoneUri = (ringtone != null && !ringtone.equals("error")) ? Uri.parse(ringtone) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }catch(Exception e){
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        AudioManager audMan = ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE));
        if(audMan != null){
            audMan.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }
        try {
            final MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(c, ringtoneUri);
            mp.setVolume(100, 100);
            mp.prepare();
            mp.start();

            new Handler().postDelayed(mp::stop, durationInt * 1000);
        }catch(IOException ignored){}
    }
}
