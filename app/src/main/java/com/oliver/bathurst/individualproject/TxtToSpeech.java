package com.oliver.bathurst.individualproject;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

/**
 * Created by Oliver on 19/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

/**
 * This class accepts a string extra from the intent and queues it on the TextToSpeech queue
 */

public class TxtToSpeech extends Activity implements android.speech.tts.TextToSpeech.OnInitListener {
    private TextToSpeech tts;//initialise TextToSpeech object
    private String toSpeak = "";//text to output

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.toSpeak = getIntent().getStringExtra("SPEECH"); //get string extra and setup the speech
        tts = new TextToSpeech(this, this); //rebind tts to new object
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) { //if successful...
            tts.speak(toSpeak, TextToSpeech.QUEUE_ADD, null);//speak
        }
    }
}
