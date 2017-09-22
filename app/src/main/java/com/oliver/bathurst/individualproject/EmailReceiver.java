package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import java.util.Properties;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by Oliver on 18/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Properties properties = getServerProperties();
        Session session = Session.getDefaultInstance(properties);
        try {
            Store store = session.getStore("imap");
            store.connect(user, pass);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            int count = inbox.getMessageCount();
            Message[] messages = inbox.getMessages(1, count);
            for (Message message : messages) {
                if (!message.getFlags().contains(Flags.Flag.SEEN)) {
                    Address[] fromAddresses = message.getFrom();
                    String[] parts = fromAddresses[0].toString().split("<");
                    String[] part2 = parts[1].split(">");
                    switchEmailSubject(part2[0], message.getSubject().trim());
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

        if(subject.contains("speak:")){
            String[] parts = subject.split(":");
            SMSReceiver.toSpeak = (parts[1]);
            c.startActivity(new Intent(c,TxtToSpeech.class));
        }
        if(gmailWipeSD!=null){
            if(subject.equals(gmailWipeSD)){
                SMSReceiver.wipeSD();
            }
        }
        if(remoteLock!=null) {
            if (subject.equals(remoteLock)) {
                PolicyManager polMan = new PolicyManager(c);
                polMan.lockPhone();
            }
        }
        if(gmailLoc!=null) {
            if (subject.equals(gmailLoc)) {
                sendLocationBack(sender.trim());
            }
        }
        if(gmailWipe!=null){
            if(subject.equals(gmailWipe)){
                PolicyManager polMan = new PolicyManager(c);
                polMan.wipePhone();
            }
        }
    }

    private void sendLocationBack(String sender){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        GMailSender gmail = new GMailSender(user,pass);
        EmailAttachmentHelper help = new EmailAttachmentHelper(c);
        help.attachFiles(gmail);

        gmail.sendMail(user, "Location Update", help.getEmailString(), sender);
    }
}