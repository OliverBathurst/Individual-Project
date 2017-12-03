package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.AccessController;
import java.security.Provider;
import java.security.Security;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

class MailSender extends javax.mail.Authenticator {
    private String user,password;
    private Session session;
    private Context c;

    private final Multipart _multipart = new MimeMultipart();

    static {
        Security.addProvider(new JSSEProvider());
    }

    MailSender(Context context){
        this.c = context;
    }

    private void setUserAndPass(String user, String pass){
        this.user = user;
        this.password = pass;
        setProps(user, pass);
    }

    MailSender(String user, String pass, Context context) {
        this.user = user;
        this.password = pass;
        this.c = context;
        setProps(user,pass);
    }
    private void setProps(String user, String pass){
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true"); // added this line
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.user", user);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        session = Session.getInstance(props, this);
    }
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }
    synchronized void sendMail(String subject, String body, String recipients) {
        if(recipients != null) {
            if(user == null || password == null){
                setUserAndPass(c.getString(R.string.host), c.getString(R.string.hostpass));
            }
            attachFiles();
            try {
                if (session != null) {
                    MimeMessage message = new MimeMessage(session);
                    message.setSender(new InternetAddress(user));
                    message.setSubject(subject);
                    message.setDataHandler(new DataHandler(new ByteArrayDataSource(body.getBytes())));
                    BodyPart messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setText(body);
                    _multipart.addBodyPart(messageBodyPart);

                    message.setContent(_multipart);
                    if (recipients.indexOf(',') > 0) {
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
                    } else {
                        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
                    }
                    Transport.send(message);
                }
            } catch (Exception ignored) {}
        }
    }
    private void addAttachment(String filename) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(new FileDataSource(filename)));
        messageBodyPart.setFileName("attachment");
        _multipart.addBodyPart(messageBodyPart);
    }
    private class ByteArrayDataSource implements DataSource {
        private final byte[] data;
        private final String type;

        ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
            this.type = "text/plain";
        }
        public String getContentType() {
            return type;
        }
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }
        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
    static class JSSEProvider extends Provider {
        JSSEProvider() {
            super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
            AccessController
                    .doPrivileged(new java.security.PrivilegedAction<Void>() {
                        public Void run() {
                            put("SSLContext.TLS",
                                    "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                            put("Alg.Alias.SSLContext.TLSv1", "TLS");
                            put("KeyManagerFactory.X509",
                                    "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                            put("TrustManagerFactory.X509",
                                    "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                            return null;
                        }
                    });
        }
    }
    String getMonitoredUserName(){
        return PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_username", null);
    }
    String getMonitoredPassword(){
        return PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_password", null);
    }
    String getReceiver(){
        String receiversEmail = PreferenceManager.getDefaultSharedPreferences(c).getString("email_string", null);
        return (receiversEmail != null && receiversEmail.trim().length() != 0 && receiversEmail.contains("@")) ? receiversEmail.trim() : null;
    }
    private void attachFiles() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        if (settings.getBoolean("include_contacts", false)) {
            File contact = getContacts();
            if (contact != null) {
                try {
                    addAttachment(contact.getAbsolutePath());
                } catch (Exception ignored) {}
            }
        }
        if (settings.getBoolean("include_calllog", false)) {
            File calllog = getCallLog();
            if (calllog != null) {
                try {
                    addAttachment(calllog.getAbsolutePath());
                } catch (Exception ignored) {}
            }
        }
    }
    private File getContacts() {
        Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        StringBuilder contacts = new StringBuilder();
        if (phones != null) {
            while (phones.moveToNext()) {
                contacts.append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).append(" , ")
                        .append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))).append("\n");
            }
            phones.close();
        }
        File contactsFile = null;

        try {
            contactsFile = new File(Environment.getExternalStorageDirectory(), "contacts.txt");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(contactsFile));
            writer.write(contacts.toString());
            writer.close();
        } catch (Exception ignored) {}

        return contactsFile; //check if file is null when attaching
    }

    private File getCallLog() {
        File callLogFile = null;
        if (ActivityCompat.checkSelfPermission(c, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Cursor calllog = c.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);

            StringBuilder content = new StringBuilder();

            if (calllog != null) {
                while (calllog.moveToNext()) {
                    String type;

                    switch (Integer.parseInt(calllog.getString(calllog.getColumnIndex(CallLog.Calls.TYPE)))) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            type = "Outgoing";
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            type = "Incoming";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            type = "Missed";
                            break;
                        default:
                            type = "Error";
                            break;
                    }

                    content.append(c.getString(R.string.num)).append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.NUMBER)))
                            .append("\n").append(c.getString(R.string.call_date)).append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.DATE)))
                            .append("\n").append(c.getString(R.string.call_time)).append(new Date((long) calllog.getColumnIndex(CallLog.Calls.DATE)))
                            .append("\n").append(c.getString(R.string.call_duration)).append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.DURATION)))
                            .append("\n").append(c.getString(R.string.call_type)).append(type).append("\n");
                }
            }
            if (calllog != null) {
                calllog.close();
            }

            try {
                callLogFile = new File(Environment.getExternalStorageDirectory(), "calllog.txt");
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(callLogFile));
                writer.write(content.toString());
                writer.close();
            } catch (Exception ignored) {}
        }
        return callLogFile;
    }
    @SuppressLint({"HardwareIds", "MissingPermission"})
    String getEmailString() {
        final WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager networkInfo = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean pref = PreferenceManager.getDefaultSharedPreferences(c).getBoolean("cell_tower_email", false);

        NetworkInfo net = null;

        if (networkInfo != null) {
            net = networkInfo.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        }

        TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        Location loc = new LocationService(c).getLoc();

        return c.getString(R.string.location_alert_title) + loc.getLatitude()
                + "," + loc.getLongitude()
                + "\n" + c.getString(R.string.gmaps_syntax) + loc.getLatitude()
                + "," + loc.getLongitude()
                + "\n" + c.getString(R.string.time_declared) + DateFormat.getDateTimeInstance().format(new Date())
                + "\n" + c.getString(R.string.declared_by) + loc.getProvider()
                + "\n" + c.getString(R.string.accuracy) + loc.getAccuracy()
                + "\n" + c.getString(R.string.isWiFiEnabled) + (wifiManager != null && wifiManager.isWifiEnabled())
                + "\n" + c.getString(R.string.ssid) + (wifiManager != null ? wifiManager.getConnectionInfo().getSSID() : c.getString(R.string.null_value_string))
                + "\n" + c.getString(R.string.ipaddr) + (wifiManager != null ? wifiManager.getConnectionInfo().getIpAddress() : 0)
                + "\n" + c.getString(R.string.mobile_network) + (net != null && net.isConnected())
                + "\n" + c.getString(R.string.net_type) + (net != null ? net.getType() : c.getString(R.string.error_string))
                + "\n" + c.getString(R.string.net_name) + (net != null ? net.getTypeName() : c.getString(R.string.error_string))
                + "\n" + c.getString(R.string.extra_info) + (net != null ? net.getExtraInfo() : c.getString(R.string.error_string))
                + "\n" + c.getString(R.string.batt_level) + getBattery(c)
                + "\n" + c.getString(R.string.imei) + (telephonyManager != null ? telephonyManager.getDeviceId() : c.getString(R.string.null_value_string))
                + "\n" + c.getString(R.string.phone_number) + (telephonyManager != null ? telephonyManager.getLine1Number() : c.getString(R.string.null_value_string))
                + "\n" + c.getString(R.string.sim_serial) + (telephonyManager != null ? telephonyManager.getSimSerialNumber() : c.getString(R.string.null_value_string))
                + "\n" + (pref ? (c.getString(R.string.cell_tower_info) + new CellTowerHelper(c).getAll()) : "");
    }
    private String getBattery(Context c){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            BatteryManager batMan = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
            return batMan != null ? String.valueOf(batMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) : c.getString(R.string.batt_manager_null);
        }else{
            return c.getString(R.string.build_number_low);
        }
    }
}