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

/**
 * Alarm rings or sounds a user-chosen ringtone
 */
class Alarm {
    private final String duration, ringtone;//ringtone and string version of int
    private final Context c;
    private final int volume;

    /**
     * Initialised with ringtone volume, duration, and ringtone URI string
     */
    Alarm(Context context, int ringVol, String ringDur, String ringtone){
        this.c = context;
        this.volume = ringVol;
        this.duration = ringDur;
        this.ringtone = ringtone;
    }

    /**
     * Attempt to make sound
     */
    void ring(){
        int durationInt = 20;
        if(duration != null) {//if duration exists
            try {
                durationInt = Integer.parseInt(duration);//try parsing into a number
            } catch (NumberFormatException e) {
                durationInt = 20;//default to 20s
            }
        }

        Uri ringtoneUri;//URI of ringtone
        try{//try parsing URI, else get default alarm ringtone
            ringtoneUri = (ringtone != null && !ringtone.equals("error")) ? Uri.parse(ringtone) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }catch(Exception e){
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        AudioManager audMan = ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE));
        if(audMan != null){
            audMan.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }//get audio service and set volume
        try {
            final MediaPlayer mp = new MediaPlayer();//setup media player for playing
            mp.setDataSource(c, ringtoneUri);//set source with context and the URI
            mp.setVolume(100, 100);//set volume to be 100% (100% of the stream volume)
            mp.prepare();//prepare
            mp.start();//play
            new Handler().postDelayed(mp::stop, durationInt * 1000);//stop after (seconds * 1000ms)
        }catch(IOException ignored){}
    }
}
