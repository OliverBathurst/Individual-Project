package com.oliver.bathurst.individualproject;

/**
 * Created by Oliver on 06/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class GCMHandler {
    private String toExamine;

    GCMHandler(String str){
        this.toExamine = str;
    }
    void examine(){
        ///TEST
        if(toExamine.contains("This is push for video!")){
            System.out.println("true");
        }
    }
}
