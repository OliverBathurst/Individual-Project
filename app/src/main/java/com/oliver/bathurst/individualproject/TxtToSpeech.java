package com.oliver.bathurst.individualproject;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

/**
 * Created by Oliver on 19/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class TxtToSpeech extends Activity implements android.speech.tts.TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(this, this);
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.speak(SMSReceiver.toSpeak, TextToSpeech.QUEUE_ADD, null);
        }
    }
}
