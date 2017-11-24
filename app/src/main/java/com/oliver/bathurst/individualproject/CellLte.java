package com.oliver.bathurst.individualproject;

/**
 * Created by Oliver on 24/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class CellLte implements java.io.Serializable{
    private final int id, mcc, mnc, pci, tac;
    /**
     *Cell identity
     *Mobile country code "
     *Mobile network code
     *Physical cell
     *Tracking area code"
     */
    CellLte(int CI, int MCC, int MNC, int pci, int tac){
        this.id = CI;
        this.mcc = MCC;
        this.mnc = MNC;
        this.pci = pci;
        this.tac = tac;
    }
    int getID(){
        return id;
    }
    int getMCC(){
        return mcc;
    }
    int getMNC(){
        return mnc;
    }
    int getPCI(){
        return pci;
    }
    int getTac(){
        return tac;
    }
}
