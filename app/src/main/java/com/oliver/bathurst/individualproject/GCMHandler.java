package com.oliver.bathurst.individualproject;

import android.content.Context;

/**
 * Created by Oliver on 06/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class GCMHandler {
    private final String toExamine;
    private final Context context;

    GCMHandler(String str,Context c){
        this.toExamine = str;
        this.context = c;
    }

    void examine(){
        ///TEST
        if(toExamine.contains("testing")) {

        }
    }
}
