package com.oliver.bathurst.individualproject;

/**
 * Created by Oliver on 24/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class Cell implements java.io.Serializable{
    private final int cid, lac, mcc, mnc, signalStrength;
    private final boolean isRegistered;

    Cell(int CID, int LAC, int MCC, int MNC, int signal, boolean isReg){
        this.cid = CID;
        this.lac = LAC;
        this.mcc = MCC;
        this.mnc = MNC;
        this.signalStrength = signal;
        this.isRegistered = isReg;
    }
    int getCID(){
        return cid;
    }
    int getLAC(){
        return lac;
    }
    int getMCC(){
        return mcc;
    }
    int getMNC(){
        return mnc;
    }
    int getSignal(){
        return signalStrength;
    }
    boolean isActive(){
        return isRegistered;
    }
}
