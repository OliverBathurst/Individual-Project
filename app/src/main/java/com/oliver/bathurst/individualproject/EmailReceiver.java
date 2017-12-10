package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by Oliver on 18/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class EmailReceiver {
    private final Context c;
    private final String user, pass;

    EmailReceiver(Context context,String user,String pass){
        this.user = user;
        this.pass = pass;
        this.c = context;
    }

    private Properties getServerProperties() {
        Properties properties = new Properties();
        properties.put(String.format("mail.%s.host", "imap"), "imap.gmail.com");
        properties.put(String.format("mail.%s.port", "imap"), "993");
        properties.setProperty(String.format("mail.%s.socketFactory.class", "imap"), "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(String.format("mail.%s.socketFactory.fallback", "imap"), "false");
        properties.setProperty(String.format("mail.%s.socketFactory.port", "imap"), String.valueOf("993"));
        properties.put("mail.smtp.starttls.enable", "true");
        return properties;
    }

    void getNewEmails() {
        @SuppressLint("StaticFieldLeak")
        class getNew extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if(user != null && pass != null) {
                    try {
                        Store store = Session.getDefaultInstance(getServerProperties()).getStore("imap");
                        store.connect(user, pass);

                        Folder inbox = store.getFolder("INBOX");
                        inbox.open(Folder.READ_WRITE);

                        for (Message message : inbox.getMessages(1, inbox.getMessageCount())) {
                            if (!message.getFlags().contains(Flags.Flag.SEEN)) {
                                switchEmailSubject(message.getFrom()[0].toString().split("<")[1].split(">")[0], message.getSubject().trim(), message);
                                message.setFlag(Flags.Flag.SEEN, true);
                            }
                        }
                        inbox.close(false);
                        store.close();
                    } catch (Exception ignored) {}
                }
                return null;
            }
        }
        new getNew().execute();
    }

    private void switchEmailSubject(String sender, String subject, Message message){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        String remoteLock = settings.getString("gmail_remote_lock",null);
        String gmailLoc = settings.getString("gmail_loc",null);
        String gmailWipe = settings.getString("gmail_wipe",null);
        String gmailWipeSD = settings.getString("wipe_sdcard_gmail",null);
        String stolen = settings.getString("email_stolen", null);
        String emailBeacon = settings.getString("email_relay_beacon", null);
        boolean hasTriggered = false;

        if(stolen != null && subject.equals(stolen)){
            hasTriggered = true;
            PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean("stolen", true).apply();
        }
        if(gmailWipeSD != null && subject.equals(gmailWipeSD)){
            hasTriggered = true;
            new SDWiper().wipeSD();
        }
        if(remoteLock != null && subject.equals(remoteLock)) {
            hasTriggered = true;
            new PolicyManager(c).lockPhone();
        }
        if(gmailLoc != null && subject.equals(gmailLoc)) {
            hasTriggered = true;
            sendLocationBack(c, sender.trim());
        }
        if(gmailWipe != null && subject.equals(gmailWipe)){
            hasTriggered = true;
            new PolicyManager(c).wipePhone();
        }
        if(emailBeacon != null && subject.equals(emailBeacon)){
            hasTriggered = true;
            sendBeaconInfoBack(c, sender.trim());
        }
        if(subject.contains("speak:")){
            hasTriggered = true;
            c.startActivity(new Intent(c,TxtToSpeech.class).putExtra("SPEECH",(subject.split(":")[1])));
        }
        if(hasTriggered && settings.getBoolean("delete_after_trigger", false)){
            try {
                message.setFlag(Flags.Flag.DELETED, true);
            }catch(Exception ignored){}
        }
    }
    private void sendBeaconInfoBack(final Context c, final String sender){
        @SuppressLint("StaticFieldLeak")
        class sendBeaconInfo extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                Looper.prepare();
                new PostPHP(c).execute(new String[]{sender, c.getString(R.string.beacon_update_title), new NearbyBeacons(c).run()});
                Looper.loop();
                return null;
            }
        }
        new sendBeaconInfo().execute();
    }

    private void sendLocationBack(final Context c, final String sender){
        @SuppressLint("StaticFieldLeak")
        class sendLoc extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                Looper.prepare();
                PostPHP php = new PostPHP(c);
                php.execute(new String[]{sender, c.getString(R.string.location_update_title), php.getEmailString()} );
                Looper.loop();
                return null;
            }
        }
        new sendLoc().execute();
    }
}