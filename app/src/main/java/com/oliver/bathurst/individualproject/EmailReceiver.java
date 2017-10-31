package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by Oliver on 18/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class EmailReceiver {
    private final Context c;
    private final String user;
    private final String pass;

    EmailReceiver(Context context,String user,String pass){
        this.user = user;
        this.pass = pass;
        c = context;
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
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        try {
            Store store = Session.getDefaultInstance(getServerProperties()).getStore("imap");
            store.connect(user, pass);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages(1, inbox.getMessageCount());
            for (Message message : messages) {
                if (!message.getFlags().contains(Flags.Flag.SEEN)) {
                    switchEmailSubject(message.getFrom()[0].toString().split("<")[1].split(">")[0], message.getSubject().trim());
                    message.setFlag(Flags.Flag.SEEN, true);
                }
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    private void switchEmailSubject(String sender, String subject){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        String remoteLock = settings.getString("gmail_remote_lock",null);
        String gmailLoc = settings.getString("gmail_loc",null);
        String gmailWipe = settings.getString("gmail_wipe",null);
        String gmailWipeSD = settings.getString("wipe_sdcard_gmail",null);
        String stolen = settings.getString("email_stolen", null);

        if(stolen != null && subject.equals(stolen)){
            PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean("stolen", true).apply();
        }
        if(subject.contains("speak:")){
            SMSReceiver.toSpeak = (subject.split(":")[1]);
            c.startActivity(new Intent(c,TxtToSpeech.class));
        }
        if(gmailWipeSD != null && subject.equals(gmailWipeSD)){
            SMSReceiver.wipeSD();
        }
        if(remoteLock != null && subject.equals(remoteLock)) {
            new PolicyManager(c).lockPhone();
        }
        if(gmailLoc != null && subject.equals(gmailLoc)) {
            sendLocationBack(sender.trim());
        }
        if(gmailWipe != null && subject.equals(gmailWipe)){
            new PolicyManager(c).wipePhone();
        }
    }
    private void sendLocationBack(String sender){
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        GMailSender gmail = new GMailSender(user,pass);
        EmailAttachmentHelper help = new EmailAttachmentHelper(c);
        help.attachFiles(gmail);
        gmail.sendMail(user, "Location Update", help.getEmailString(), sender);
    }
}