package com.oliver.bathurst.individualproject;

import android.os.Environment;
import java.io.File;

/**
 * Created by Oliver on 05/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
class SDWiper {

    SDWiper(){}

    void wipeSD(){
        File deleteMatchingFile = new File(Environment.getExternalStorageDirectory().toString());
        try {
            File[] filenames = deleteMatchingFile.listFiles();
            if (filenames != null && filenames.length > 0) {
                for (File tempFile : filenames) {
                    if (tempFile.isDirectory()) {
                        wipeDirectory(tempFile.toString());
                        tempFile.delete();
                    } else {
                        tempFile.delete();
                    }
                }
            } else {
                deleteMatchingFile.delete();
            }
        } catch (Exception ignored) {}
    }
    private void wipeDirectory(String name) {
        File directoryFile = new File(name);
        File[] filenames = directoryFile.listFiles();
        if (filenames != null && filenames.length > 0) {
            for (File tempFile : filenames) {
                if (tempFile.isDirectory()) {
                    wipeDirectory(tempFile.toString());
                    tempFile.delete();
                } else {
                    tempFile.delete();
                }
            }
        } else {
            directoryFile.delete();
        }
    }
}
