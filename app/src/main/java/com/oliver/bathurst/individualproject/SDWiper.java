package com.oliver.bathurst.individualproject;

import android.os.Environment;
import java.io.File;

/**
 * Created by Oliver on 05/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

/**
 * This class simply wipes all files and directories on the SD card (if one available)
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
class SDWiper {

    SDWiper(){}

    void wipeSD(){
        File deleteMatchingFile = new File(Environment.getExternalStorageDirectory().toString());
        try {
            File[] filenames = deleteMatchingFile.listFiles();//get all files in array
            if(filenames != null && filenames.length > 0) {//if null or empty (no SD or empty SD)
                for(File tempFile : filenames) {//iterate over all files
                    if (tempFile.isDirectory()) {//if a directory...
                        wipeDirectory(tempFile.toString());//call wipe dir with directory path
                        tempFile.delete();//afterwards delete dir
                    }else{
                        tempFile.delete();//if not a directory, just delete
                    }
                }
            }else{
                deleteMatchingFile.delete();//delete if no files
            }
        } catch (Exception ignored) {}
    }
    private void wipeDirectory(String name) {
        File directoryFile = new File(name);
        File[] filenames = directoryFile.listFiles();//list files in dir
        if (filenames != null && filenames.length > 0) {//if there are files...
            for (File tempFile : filenames) {//iterate over them
                if (tempFile.isDirectory()) {
                    wipeDirectory(tempFile.toString());//if another dir, recurse
                    tempFile.delete();
                }else{
                    tempFile.delete();//otherwise delete file
                }
            }
        }else{
            directoryFile.delete();
        }
    }
}
