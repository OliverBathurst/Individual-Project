package com.oliver.bathurst.individualproject;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import static android.app.Activity.RESULT_OK;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class SettingsFragment extends PreferenceFragment {
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int REQUEST_ENABLE_BT = 23;
    private final String[] saltValues = new String[]{"0","1","2","3","4","5","6","7","8","9"};
    private final SecureRandom randomSaltSelector = new SecureRandom();//generate crypto-secure randoms

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);
        final SharedPreferences.Editor settings = getDefaultSharedPreferences(getActivity()).edit();
        final SharedPreferences settingsView = getDefaultSharedPreferences(getActivity());

        try{
            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.getAction() != null) {
                        if (intent.getAction().equals(RegistrationIntentService.REGISTRATION_SUCCESS)) {
                            final String token = intent.getStringExtra("token");
                            new android.support.v7.app.AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.your_gcm_token) + " \n" + token)
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.OK), (dialog, id) -> dialog.dismiss()).setNegativeButton(getString(R.string.copy), (dialog, id) -> {
                                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        if(clipboard != null) {
                                            clipboard.setPrimaryClip(ClipData.newPlainText("token", token));
                                            Toast.makeText(getActivity(), getString(R.string.token_clipboard), Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getActivity(), getString(R.string.error_clipboard), Toast.LENGTH_SHORT).show();
                                        }
                                    }).create().show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.gcm_reg_error), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            };

            findPreference("share_token").setOnPreferenceClickListener(preference -> {
                shareToken();
                return false;
            });

            findPreference("hide_app").setOnPreferenceClickListener(preference -> {
                HideApp hidden = new HideApp(getActivity());
                hidden.toggle();
                Toast.makeText(getActivity(), String.valueOf(hidden.getStatus()), Toast.LENGTH_SHORT).show();
                return false;
            });
            findPreference("grant_device_admin").setOnPreferenceClickListener(preference -> {
                getDeviceAdmin();
                return true;
            });
            findPreference("get_token").setOnPreferenceClickListener(preference -> {
                getCurrentGCM();
                return false;
            });
            findPreference("register").setOnPreferenceClickListener(preference -> {
                registerGCM();
                return false;
            });
            findPreference("update_now").setOnPreferenceClickListener(preference -> {
                new UpdateDatabase(new LocationService(getActivity()).getLocation(), getActivity()).update();
                return false;
            });
            findPreference("open_interface").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.my_website))));
                return false;
            });
            findPreference("fingerprinting").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), WiFiScanner.class));
                return false;
            });
            findPreference("sign_up").setOnPreferenceClickListener(preference -> {
                String username = settingsView.getString("registered_user",null);
                String password = settingsView.getString("registered_pass",null);

                if(username == null && password == null){
                    startActivity(new Intent(getActivity(), SignUpActivity.class));
                }else{
                    new android.support.v7.app.AlertDialog.Builder(getActivity())
                            .setMessage("Only one user account is allowed per device: " + "\n" + getString(R.string.your_details) + "\n"
                            + getString(R.string.username) + username + "\n" + getString(R.string.password) + password)
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.OK), (dialog, id) -> dialog.dismiss()).create().show();
                }
                return false;
            });

            findPreference("cell_towers").setOnPreferenceClickListener(preference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    startActivity(new Intent(getActivity(), CellTowers.class));
                }else{
                    Toast.makeText(getActivity(),getString(R.string.build_number_low),Toast.LENGTH_SHORT).show();
                }
                return false;
            });

            ((CheckBoxPreference) findPreference("enable_triggers")).setChecked(settingsView.getBoolean("enable_triggers", true));

            findPreference("sms_ringtone_volume").setSummary(getString(R.string.current_volume) + settingsView.getString("sms_ringtone_volume", "90") + "%");

            findPreference("battery_percent").setSummary(getString(R.string.current_percentage) + settingsView.getString("battery_percent", "5") + "%");

            findPreference("un-stolen").setOnPreferenceClickListener(preference -> {
                settings.putBoolean("stolen", false).apply();
                Toast.makeText(getActivity(), getString(R.string.unflag_device), Toast.LENGTH_SHORT).show();
                return false;
            });
            findPreference("backup_shared_pref").setOnPreferenceClickListener(preference -> {
                actionSharedPref();
                return false;
            });

            findPreference("check_perm").setOnPreferenceClickListener(preference -> {
                new PermissionsManager(getActivity()).permissionsCheckup();
                return false;
            });
            findPreference("geo_fence").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(SettingsFragment.super.getActivity(), GeoFencing.class));
                return false;
            });
            findPreference("beacon").setOnPreferenceClickListener(preference -> {
                startBeaconActivity();
                return false;
            });
            findPreference("sort_loc").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(SettingsFragment.super.getActivity(), Reorder.class));
                return false;
            });
            Preference.OnPreferenceChangeListener listener = (preference, newValue) -> {
                preference.setSummary(getString(R.string.trigger_value) + newValue.toString());
                return true;
            };
            Preference.OnPreferenceChangeListener simpleList = (preference, newValue) -> {
                preference.setSummary(getString(R.string.current_value) + newValue.toString());
                return true;
            };
            Preference.OnPreferenceChangeListener secondsListener = (preference, newValue) -> {
                preference.setSummary(getString(R.string.current_value) + newValue.toString() + getString(R.string.seconds));
                return true;
            };
            Preference.OnPreferenceChangeListener listenerSeconds = (preference, newValue) -> {
                preference.setSummary(getString(R.string.current_volume) + newValue.toString() + "%");
                return true;
            };
            Preference.OnPreferenceChangeListener listenerPercentage = (preference, newValue) -> {
                preference.setSummary(getString(R.string.current_percentage) + newValue.toString() + "%");
                return true;
            };

            ListPreference ringList = (ListPreference) findPreference("ringtone_select");
            try {
                HashMap<String, String> list = new HashMap<>();
                RingtoneManager ringtoneManager = new RingtoneManager(getActivity());
                Cursor cursor = ringtoneManager.getCursor();
                while (cursor.moveToNext()) {
                    list.put(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX), ringtoneManager.getRingtoneUri(cursor.getPosition()).toString());
                }
                ringList.setEntries(list.keySet().toArray(new CharSequence[0]));
                ringList.setEntryValues(list.values().toArray(new CharSequence[0]));
            }catch(Exception ignored){}


            EditTextPreference gcm_fingerprint_relay = (EditTextPreference) findPreference("gcm_fingerprint_relay");
            updateValue(gcm_fingerprint_relay, settings, "GCMFinger", "gcm_fingerprint_relay");
            EditTextPreference gcm_cell_tower_relay = (EditTextPreference) findPreference("gcm_cell_tower_relay");
            updateValue(gcm_cell_tower_relay, settings, "GCMCellTowers", "gcm_cell_tower_relay");
            EditTextPreference gcm_contacts_relay = (EditTextPreference) findPreference("gcm_contacts_relay");
            updateValue(gcm_contacts_relay, settings, "GCMContacts", "gcm_contacts_relay");
            EditTextPreference gcm_calls_relay = (EditTextPreference) findPreference("gcm_calls_relay");
            updateValue(gcm_calls_relay, settings, "GCMCalls", "gcm_calls_relay");
            EditTextPreference gcm_beacon_relay = (EditTextPreference) findPreference("gcm_beacon_relay");
            updateValue(gcm_beacon_relay, settings, "GCMBluetooth", "gcm_beacon_relay");
            EditTextPreference wipe_sd_gcm = (EditTextPreference) findPreference("wipe_sd_gcm");
            updateValue(wipe_sd_gcm, settings, "GCMWipeSD", "wipe_sd_gcm");
            EditTextPreference gcm_toggle_hide = (EditTextPreference) findPreference("toggle_hiding_gcm");
            updateValue(gcm_toggle_hide, settings, "GCMHideToggle", "toggle_hiding_gcm");
            EditTextPreference gcm_relay_location = (EditTextPreference) findPreference("gcm_location_relay");
            updateValue(gcm_relay_location, settings, "GCMLocationRelay", "gcm_location_relay");
            EditTextPreference gcmSMS = (EditTextPreference) findPreference("get_gcm_sms");
            updateValue(gcmSMS, settings, "smsGCM", "get_gcm_sms");
            EditTextPreference volumeRinger = (EditTextPreference) findPreference("sms_ringtone_volume");
            updateValue(volumeRinger, settings, "90", "sms_ringtone_volume");
            EditTextPreference batteryPercentage = (EditTextPreference) findPreference("battery_percent");
            updateValue(batteryPercentage, settings, "5", "battery_percent");
            EditTextPreference GCMTorch = (EditTextPreference) findPreference("turn_torch_on_gcm");
            updateValue(GCMTorch, settings, "GCMTorch", "turn_torch_on_gcm");
            EditTextPreference SMSTorch = (EditTextPreference) findPreference("turn_torch_on_sms");
            updateValue(SMSTorch, settings, "SMSTorch", "turn_torch_on_sms");
            EditTextPreference smsGCM = (EditTextPreference) findPreference("send_sms_gcm");
            updateValue(smsGCM, settings, "GCMSMS", "send_sms_gcm");
            EditTextPreference wifiGCM = (EditTextPreference) findPreference("enable_wifi_gcm");
            updateValue(wifiGCM, settings, "GCMWiFi", "enable_wifi_gcm");
            EditTextPreference wipeGCM = (EditTextPreference) findPreference("wipe_gcm");
            updateValue(wipeGCM, settings, "GCMWipe", "wipe_gcm");
            EditTextPreference stolenGCM = (EditTextPreference) findPreference("sms_stolen_gcm");
            updateValue(stolenGCM, settings, "GCMStolen", "sms_stolen_gcm");
            EditTextPreference lockGCM = (EditTextPreference) findPreference("lock_gcm");
            updateValue(lockGCM, settings, "lock", "lock_gcm");
            EditTextPreference ringGCM = (EditTextPreference) findPreference("gcm_ring");
            updateValue(ringGCM, settings, "ring", "gcm_ring");
            EditTextPreference emailBeacon = (EditTextPreference) findPreference("email_relay_beacon");
            updateValue(emailBeacon, settings, "beacon", "email_relay_beacon");
            EditTextPreference smsBeacon = (EditTextPreference) findPreference("sms_relay_beacon");
            updateValue(smsBeacon, settings, "beacon", "sms_relay_beacon");
            EditTextPreference smsRing = (EditTextPreference) findPreference("sms_ring");
            updateValue(smsRing, settings, "ring", "sms_ring");
            EditTextPreference smsStolen = (EditTextPreference) findPreference("sms_stolen");
            updateValue(smsStolen, settings, "stolen", "sms_stolen");
            EditTextPreference emailStolen = (EditTextPreference) findPreference("email_stolen");
            updateValue(emailStolen, settings, "stolen", "email_stolen");
            EditTextPreference smsEmail = (EditTextPreference) findPreference("sms_relay_email");
            updateValue(smsEmail, settings, "email", "sms_relay_email");
            EditTextPreference smsText = (EditTextPreference) findPreference("sms_relay_text");
            updateValue(smsText, settings, "text", "sms_relay_text");
            EditTextPreference smsLoc = (EditTextPreference) findPreference("sms_loc_services");
            updateValue(smsLoc, settings, "remoteEnableLocation", "sms_loc_services");
            EditTextPreference smsLock = (EditTextPreference) findPreference("sms_remote_lock");
            updateValue(smsLock, settings, "remoteEnableLock", "sms_remote_lock");
            EditTextPreference smsWipe = (EditTextPreference) findPreference("sms_wipe");
            updateValue(smsWipe, settings, "remoteEnableWIPE", "sms_wipe");
            EditTextPreference gmailLock = (EditTextPreference) findPreference("gmail_remote_lock");
            updateValue(gmailLock, settings, "remoteLockGmail", "gmail_remote_lock");
            EditTextPreference gmailLoc = (EditTextPreference) findPreference("gmail_loc");
            updateValue(gmailLoc, settings, "remoteLocationGmail", "gmail_loc");
            EditTextPreference gmailWipe = (EditTextPreference) findPreference("gmail_wipe");
            updateValue(gmailWipe, settings, "remoteWipeGmail", "gmail_wipe");
            EditTextPreference SDWipe = (EditTextPreference) findPreference("wipe_sdcard");
            updateValue(SDWipe, settings, "remoteWipeSDCard", "wipe_sdcard");
            EditTextPreference SDWipeGmail = (EditTextPreference) findPreference("wipe_sdcard_gmail");
            updateValue(SDWipeGmail, settings, "wipeSDCardGmail", "wipe_sdcard_gmail");
            EditTextPreference hide = (EditTextPreference) findPreference("sms_hide_app");
            updateValue(hide, settings, "unHide", "sms_hide_app");

            EditTextPreference ringDur = (EditTextPreference) findPreference("ring_duration");
            if (ringDur.getText() != null && ringDur.getText().trim().length() != 0) {
                ringDur.setSummary(getString(R.string.current_value) + ringDur.getText() + "s");
            }else{
                ringDur.setSummary(getString(R.string.default_ring_dur));
            }

            EditTextPreference fingerScans = (EditTextPreference) findPreference("total_scans");
            if (fingerScans.getText() != null && fingerScans.getText().trim().length() != 0) {
                fingerScans.setSummary(getString(R.string.current_value) + fingerScans.getText());
            }else{
                fingerScans.setSummary(R.string.current_scans);
            }

            EditTextPreference emailUpdates = (EditTextPreference) findPreference("email_string");
            if (emailUpdates.getText() != null && emailUpdates.getText().trim().length() != 0) {
                emailUpdates.setSummary(emailUpdates.getText());
            }

            gcm_fingerprint_relay.setOnPreferenceChangeListener(listener);
            fingerScans.setOnPreferenceChangeListener(simpleList);
            gcm_cell_tower_relay.setOnPreferenceChangeListener(listener);
            gcm_contacts_relay.setOnPreferenceChangeListener(listener);
            gcm_calls_relay.setOnPreferenceChangeListener(listener);
            gcm_beacon_relay.setOnPreferenceChangeListener(listener);
            wipe_sd_gcm.setOnPreferenceChangeListener(listener);
            gcm_toggle_hide.setOnPreferenceChangeListener(listener);
            gcm_relay_location.setOnPreferenceChangeListener(listener);
            gcmSMS.setOnPreferenceChangeListener(listener);
            SMSTorch.setOnPreferenceChangeListener(listener);
            GCMTorch.setOnPreferenceChangeListener(listener);
            volumeRinger.setOnPreferenceChangeListener(listenerSeconds);
            batteryPercentage.setOnPreferenceChangeListener(listenerPercentage);
            smsGCM.setOnPreferenceChangeListener(listener);
            wifiGCM.setOnPreferenceChangeListener(listener);
            smsRing.setOnPreferenceChangeListener(listener);
            smsStolen.setOnPreferenceChangeListener(listener);
            smsEmail.setOnPreferenceChangeListener(listener);
            smsText.setOnPreferenceChangeListener(listener);
            smsLoc.setOnPreferenceChangeListener(listener);
            smsLock.setOnPreferenceChangeListener(listener);
            smsWipe.setOnPreferenceChangeListener(listener);
            emailStolen.setOnPreferenceChangeListener(listener);
            hide.setOnPreferenceChangeListener(listener);
            wipeGCM.setOnPreferenceChangeListener(listener);
            stolenGCM.setOnPreferenceChangeListener(listener);
            lockGCM.setOnPreferenceChangeListener(listener);
            ringGCM.setOnPreferenceChangeListener(listener);
            emailBeacon.setOnPreferenceChangeListener(listener);
            smsBeacon.setOnPreferenceChangeListener(listener);
            SDWipeGmail.setOnPreferenceChangeListener(listener);
            SDWipe.setOnPreferenceChangeListener(listener);
            gmailWipe.setOnPreferenceChangeListener(listener);
            gmailLoc.setOnPreferenceChangeListener(listener);
            gmailLock.setOnPreferenceChangeListener(listener);
            emailUpdates.setOnPreferenceChangeListener(simpleList);
            ringDur.setOnPreferenceChangeListener(secondsListener);

            Preference.OnPreferenceClickListener generalInfo = preference -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Gmail triggers: once Gmail credentials are entered into the username and password boxes, this app will monitor that inbox for incoming\n" +
                        "mail every time the battery level changes. On a battery level change, it will attempt to connect to that Gmail account and search for unread emails and scans their subject\n" +
                        "line for triggers to perform actions. E.g. if a trigger to send the location is &#39;loc12345&#39; then sending that as a subject in an email to the monitored account will trigger that action\n" +
                        "on a battery change i.e a charger is connected/disconnected, battery level rises/decreases. Additionally, this feature may require you to enable a Gmail setting called 'Enable Access From\n" +
                        "Less Secure Apps' on the monitored account.")
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.OK), (dialog, id) -> dialog.dismiss());
                builder.create().show();
                return false;
            };
            findPreference("general_info").setOnPreferenceClickListener(generalInfo);
        }catch(Exception ignored){}
    }
    private void updateValue(EditTextPreference edit, SharedPreferences.Editor sh, String defValue, String tag){
        if (edit.getText() != null && edit.getText().trim().length() != 0) {
            edit.setSummary(getString(R.string.trigger_value) + edit.getText());
        }else{
            String toApply = defValue + randomSalt();//add salt for security
            edit.setSummary(getString(R.string.trigger_value) + toApply);
            sh.putString(tag, toApply).apply();
        }
    }
    private String randomSalt(){
        StringBuilder returnVal = new StringBuilder();
        for(int i = 0; i < 5; i++) {
            returnVal.append(randomSaltSelector.nextInt(saltValues.length));
        }
        return returnVal.toString();
    }
    private void actionSharedPref(){
        try {
            File prefTxt = new File(Environment.getExternalStorageDirectory(), "prefs.txt");

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(prefTxt));
            StringBuilder total = new StringBuilder();
            for (Map.Entry<String, ?> entry : getDefaultSharedPreferences(getActivity()).getAll().entrySet()) {
                total.append("Key: ").append(entry.getKey()).append(" Value: ").append(entry.getValue().toString()).append("\n");
            }
            writer.write(total.toString());
            writer.close();

            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("file/*")
                    .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(prefTxt)), getString(R.string.save)));
        }catch(Exception e){
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || requestCode != PolicyManager.DPM_ACTIVATION_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == REQUEST_ENABLE_BT) { //if the request is to enable Bluetooth
            if (resultCode == RESULT_OK) { //if enabled by user
                startActivity(new Intent(SettingsFragment.super.getActivity(), BTActivity.class));//start activity
            }
        }
    }
    private void registerGCM(){
        getActivity().startService(new Intent(getActivity(), RegistrationIntentService.class));
    }
    private void startBeaconActivity(){
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter(); //get default adapter
        if(bt != null) { //if bluetooth not available
            if (!bt.isEnabled()) {//if not enabled
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT); //request enable
            }else{//if bluetooth is enabled already
                startActivity(new Intent(SettingsFragment.super.getActivity(), BTActivity.class)); //start the activity
            }
        }else{
            Toast.makeText(getActivity(),getString(R.string.bluetooth_not_available),Toast.LENGTH_SHORT).show();
        }
    }
    private void getCurrentGCM(){
        final String currToken = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("GCM_Token", null);
        new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.your_gcm_token) + "\n" + (currToken != null ? currToken : getString(R.string.no_gcm_token)))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.OK), (dialog, id) -> dialog.dismiss()).setNegativeButton(getString(R.string.copy), (dialog, id) -> {
                    if(currToken != null) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(ClipData.newPlainText("token", currToken));
                            Toast.makeText(getActivity(), getString(R.string.token_clipboard), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.error_clipboard), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.error_clipboard), Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
    }
    private void shareToken(){
        final String currToken = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("GCM_Token", null);
        if(currToken != null) {
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(Intent.EXTRA_STREAM, currToken), getString(R.string.gcm_warning)));
        }else{
            Toast.makeText(getActivity(), getString(R.string.no_gcm_token), Toast.LENGTH_SHORT).show();
        }
    }
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(RegistrationIntentService.REGISTRATION_SUCCESS));
    }
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
    private void getDeviceAdmin(){
        if(!new PolicyManager(getActivity()).isAdminActive()) {
            startActivityForResult(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    new ComponentName(getActivity(), DeviceAdmin.class)), PolicyManager.DPM_ACTIVATION_REQUEST_CODE);
        }else{
            Toast.makeText(getActivity(), getString(R.string.admin_already_active), Toast.LENGTH_SHORT).show();
        }
    }
}