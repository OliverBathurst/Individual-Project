package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import java.lang.ref.WeakReference;
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

class EmailFetcher extends AsyncTask<Void,Void,Void>{
    private final WeakReference<Context> weakContext;
    private final String user, pass;
    private Message currMessageObj;
    private String currMessageSubject, currMessageSender;

    EmailFetcher(WeakReference<Context> context, String user, String pass){
        this.user = user;
        this.pass = pass;
        this.weakContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(user != null && pass != null) {
            try {
                Store store = Session.getDefaultInstance(getServerProperties()).getStore("imap");
                store.connect(user, pass);//connect with supplied credentials

                Folder inbox = store.getFolder("INBOX");//get main inbox folder
                inbox.open(Folder.READ_WRITE); //write access to set "seen" flag

                for (Message message : inbox.getMessages(1, inbox.getMessageCount())) { //iterate over messages
                    if (!message.getFlags().contains(Flags.Flag.SEEN)) {//if the message is unseen
                        currMessageObj = message;
                        currMessageSubject = message.getSubject();
                        currMessageSender = message.getFrom()[0].toString().split("<")[1].split(">")[0];
                        analyseEmailSubject(); //start analyser
                    }
                }
                inbox.close(false);
                store.close();
            } catch (Exception ignored) {}
        }
        return null;
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

    private void analyseEmailSubject(){
        try {
            Context c = weakContext.get();
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
            String remoteLock = settings.getString("gmail_remote_lock", null);
            String gmailLoc = settings.getString("gmail_loc", null);
            String gmailWipe = settings.getString("gmail_wipe", null);
            String gmailWipeSD = settings.getString("wipe_sdcard_gmail", null);
            String stolen = settings.getString("email_stolen", null);
            String emailBeacon = settings.getString("email_relay_beacon", null);
            boolean hasTriggered = false;

            if (validate(stolen)) {
                hasTriggered = true;
                settings.edit().putBoolean("stolen", true).apply();
            }
            if (validate(gmailWipeSD)) {
                hasTriggered = true;
                new SDWiper().wipeSD();
            }
            if (validate(remoteLock)) {
                hasTriggered = true;
                new PolicyManager(c).lockPhone();
            }
            if (validate(gmailLoc)) {
                hasTriggered = true;
                PostPHP php = new PostPHP(c);
                php.execute(new String[]{currMessageSender.trim(), c.getString(R.string.location_update_title), php.getEmailString()});
            }
            if (validate(gmailWipe)) {
                hasTriggered = true;
                new PolicyManager(c).wipePhone();
            }
            if (validate(emailBeacon)) {
                hasTriggered = true;
                new PostPHP(c).execute(new String[]{currMessageSender.trim(), c.getString(R.string.beacon_update_title), new BTNearby(c).run()});
            }
            if (currMessageObj.getSubject().contains("speak:")) {
                hasTriggered = true;
                c.startActivity(new Intent(c, TxtToSpeech.class).putExtra("SPEECH", (currMessageObj.getSubject().split(":")[1])));
            }
            if (hasTriggered) {
                currMessageObj.setFlag(Flags.Flag.SEEN, true); //set flag to seen
                if (settings.getBoolean("delete_after_trigger", false)) {
                    currMessageObj.setFlag(Flags.Flag.DELETED, true);
                }
            }
        }catch(Exception ignored){}
    }
    private boolean validate(String message){
        return message != null && currMessageSubject.equals(message);
    }
}