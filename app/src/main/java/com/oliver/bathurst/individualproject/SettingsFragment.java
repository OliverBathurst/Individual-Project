package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
    private int setVolProg = 90, battProg = 5;
    private Server server = null;
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
                                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    }).setNegativeButton(getString(R.string.copy), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                            if(clipboard != null) {
                                                clipboard.setPrimaryClip(ClipData.newPlainText("token", token));
                                                Toast.makeText(getActivity(), getString(R.string.token_clipboard), Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(getActivity(), getString(R.string.error_clipboard), Toast.LENGTH_SHORT).show();
                                            }
                                        }}).create().show();
                        } else if (intent.getAction().equals(RegistrationIntentService.REGISTRATION_ERROR)) {
                            Toast.makeText(getActivity(), getString(R.string.gcm_reg_error), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            };

            (findPreference("hide_app")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    hideApp(preference);
                    return false;
                }
            });
            findPreference("sms_ringtone_volume").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    smsRingtoneVol(settings);
                    return false;
                }
            });
            findPreference("battery_percent").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    batteryPercent(settings);
                    return false;
                }
            });
            findPreference("grant_device_admin").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getDeviceAdmin();
                    return true;
                }
            });
            ((SwitchPreference) findPreference("local_server")).setChecked(Server.running);

            findPreference("local_server").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    startServer(preference);
                    return true;
                }
            });
            findPreference("get_token").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getCurrentGCM();
                    return false;
                }
            });

            findPreference("register").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    registerGCM();
                    return false;
                }
            });
            findPreference("open_interface").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.my_website))));
                    return false;
                }
            });

            findPreference("sign_up").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), SignUpActivity.class));
                    return false;
                }
            });

            findPreference("cell_towers").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        startActivity(new Intent(getActivity(), CellTowers.class));
                    }else{
                        Toast.makeText(getActivity(),getString(R.string.build_number_low),Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            ((CheckBoxPreference) findPreference("hide_sms")).setChecked(settingsView.getBoolean("hide_sms", true));
            ((CheckBoxPreference) findPreference("enable_triggers")).setChecked(settingsView.getBoolean("enable_triggers", true));

            findPreference("sms_ringtone_volume").setSummary(getString(R.string.current_volume) + settingsView.getInt("seek_bar_volume", 90) + "%");

            findPreference("battery_percent").setSummary(getString(R.string.current_percentage) + settingsView.getInt("seek_bar_battery", 5) + "%");

            findPreference("un-stolen").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    settings.putBoolean("stolen", false).apply();
                    Toast.makeText(getActivity(), getString(R.string.unflag_device), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            findPreference("backup_shared_pref").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    actionSharedPref();
                    return false;
                }
            });

            findPreference("check_perm").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new PermissionsManager(getActivity()).permissionsCheckup();
                    return false;
                }
            });
            findPreference("geo_fence").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(SettingsFragment.super.getActivity(), GeoFencing.class));
                    return false;
                }
            });
            findPreference("beacon").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startBeaconActivity();
                    return false;
                }
            });
            findPreference("sort_loc").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(SettingsFragment.super.getActivity(), Reorder.class));
                    return false;
                }
            });
            Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(getString(R.string.trigger_value) + newValue.toString());
                    return true;
                }
            };
            Preference.OnPreferenceChangeListener simpleList = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(getString(R.string.current_value) + newValue.toString());
                    return true;
                }
            };
            Preference.OnPreferenceChangeListener secondsListener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(getString(R.string.current_value) + newValue.toString() + getString(R.string.seconds));
                    return true;
                }
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


            EditTextPreference emailGCM = (EditTextPreference) findPreference("send_email_gcm");
            updateValue(emailGCM, settings, "GCMEmail12345", "send_email_gcm");
            EditTextPreference smsGCM = (EditTextPreference) findPreference("send_sms_gcm");
            updateValue(smsGCM, settings, "GCMSMS12345", "send_sms_gcm");
            EditTextPreference wifiGCM = (EditTextPreference) findPreference("enable_wifi_gcm");
            updateValue(wifiGCM, settings, "GCMWiFi12345", "enable_wifi_gcm");
            EditTextPreference wipeGCM = (EditTextPreference) findPreference("wipe_gcm");
            updateValue(wipeGCM, settings, "GCMWipe12345", "wipe_gcm");
            EditTextPreference stolenGCM = (EditTextPreference) findPreference("sms_stolen_gcm");
            updateValue(stolenGCM, settings, "GCMStolen", "sms_stolen_gcm");
            EditTextPreference lockGCM = (EditTextPreference) findPreference("lock_gcm");
            updateValue(lockGCM, settings, "lock12345", "lock_gcm");
            EditTextPreference ringGCM = (EditTextPreference) findPreference("gcm_ring");
            updateValue(ringGCM, settings, "ring12345", "gcm_ring");
            EditTextPreference emailBeacon = (EditTextPreference) findPreference("email_relay_beacon");
            updateValue(emailBeacon, settings, "beacon12345", "email_relay_beacon");
            EditTextPreference smsBeacon = (EditTextPreference) findPreference("sms_relay_beacon");
            updateValue(smsBeacon, settings, "beacon12345", "sms_relay_beacon");
            EditTextPreference smsRing = (EditTextPreference) findPreference("sms_ring");
            updateValue(smsRing, settings, "ring12345", "sms_ring");
            EditTextPreference smsStolen = (EditTextPreference) findPreference("sms_stolen");
            updateValue(smsStolen, settings, "stolen", "sms_stolen");
            EditTextPreference emailStolen = (EditTextPreference) findPreference("email_stolen");
            updateValue(emailStolen, settings, "stolen", "email_stolen");
            EditTextPreference smsEmail = (EditTextPreference) findPreference("sms_relay_email");
            updateValue(smsEmail, settings, "email12345", "sms_relay_email");
            EditTextPreference smsText = (EditTextPreference) findPreference("sms_relay_text");
            updateValue(smsText, settings, "text12345", "sms_relay_text");
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

            EditTextPreference emailUpdates = (EditTextPreference) findPreference("email_string");
            if (emailUpdates.getText() != null && emailUpdates.getText().trim().length() != 0) {
                emailUpdates.setSummary(emailUpdates.getText());
            }
            emailGCM.setOnPreferenceChangeListener(listener);
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

            Preference.OnPreferenceClickListener generalInfo = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Gmail triggers: once Gmail credentials are entered into the username and password boxes, this app will monitor that inbox for incoming\n" +
                            "mail every time the battery level changes. On a battery level change, it will attempt to connect to that Gmail account and search for unread emails and scans their subject\n" +
                            "line for triggers to perform actions. E.g. if a trigger to send the location is &#39;loc12345&#39; then sending that as a subject in an email to the monitored account will trigger that action\n" +
                            "on a battery change i.e a charger is connected/disconnected, battery level rises/decreases. Additionally, this feature may require you to enable a Gmail setting called 'Enable Access From\n" +
                            "Less Secure Apps' on the monitored account.")
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                    return false;
                }
            };
            findPreference("general_info").setOnPreferenceClickListener(generalInfo);
        }catch(Exception ignored){}
    }

    private void updateValue(EditTextPreference edit, SharedPreferences.Editor sh, String defValue, String tag){
        if (edit.getText() != null && edit.getText().trim().length() != 0) {
            edit.setSummary(getString(R.string.trigger_value) + edit.getText());
        }else{
            edit.setSummary(getString(R.string.trigger_value) + defValue);
            sh.putString(tag, defValue).apply();
        }
    }
    private void startServer(Preference pref){
        if (!((SwitchPreference) pref).isChecked()){
            server = new Server(getActivity());
            server.start();
            Toast.makeText(getActivity(), getString(R.string.server_started_on) + server.getIP() + ":" + server.getPort(), Toast.LENGTH_SHORT).show();
        }else{
            if(server != null){
                server.stop();
            }
        }
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
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                BluetoothAdapter.getDefaultAdapter().enable();
                startActivity(new Intent(SettingsFragment.super.getActivity(), BeaconActivity.class));
            }
        }
    }
    private void registerGCM(){
        getActivity().startService(new Intent(getActivity(), RegistrationIntentService.class));
    }
    private void startBeaconActivity(){
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if(bt != null) {
            if (!bt.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
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
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).setNegativeButton(getString(R.string.copy), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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
            }}).create().show();
    }
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(RegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(RegistrationIntentService.REGISTRATION_ERROR));
    }
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
    private void smsRingtoneVol(final SharedPreferences.Editor settings){
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle(getString(R.string.set_volume));
        dialog.setCancelable(true);
        dialog.show();
        final TextView tv_dialog_size = (TextView) dialog.findViewById(R.id.set_size_help_text);

        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                settings.putInt("seek_bar_volume", setVolProg).apply();
                findPreference("sms_ringtone_volume").setSummary(getString(R.string.current_volume) + setVolProg + getString(R.string.percentage_symbol));
            }
        });
        dialog.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                settings.putInt("seek_bar_volume", setVolProg).apply();
                findPreference("sms_ringtone_volume").setSummary(getString(R.string.current_volume) + setVolProg + getString(R.string.percentage_symbol));
            }
        });
        ((SeekBar) dialog.findViewById(R.id.size_seekbar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                tv_dialog_size.setText(String.format("%s%d%s", getString(R.string.vol), progress, getString(R.string.per)));
                setVolProg = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    private void batteryPercent(final SharedPreferences.Editor settings){
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog2);
        dialog.setTitle(getString(R.string.set_battery_percent));
        dialog.setCancelable(true);
        dialog.show();
        final TextView tv_dialog_size = (TextView) dialog.findViewById(R.id.set_size_help_text2);

        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                settings.putInt("seek_bar_battery", battProg).apply();
                findPreference("battery_percent").setSummary(getString(R.string.current_percentage)+ battProg + getString(R.string.percentage_symbol));
            }
        });
        dialog.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                settings.putInt("seek_bar_battery", battProg).apply();
                findPreference("battery_percent").setSummary(getString(R.string.current_percentage)+ battProg + getString(R.string.percentage_symbol));
            }
        });
        ((SeekBar) dialog.findViewById(R.id.size_seekbar2)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                tv_dialog_size.setText(String.format("%s%d%s", getString(R.string.plsselectpercent), progress, getString(R.string.per)));
                battProg = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    private void hideApp(Preference preference){
        if(preference.getSharedPreferences().getBoolean("hide_app",false)){
            Toast.makeText(getActivity(), getString(R.string.warning_hide_app), Toast.LENGTH_SHORT).show();
            getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getActivity(), Login.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Toast.makeText(getActivity(), getString(R.string.hidden), Toast.LENGTH_SHORT).show();
        }else{
            getActivity().getPackageManager().setComponentEnabledSetting(new ComponentName(getActivity(), Login.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Toast.makeText(getActivity(), getString(R.string.visible), Toast.LENGTH_SHORT).show();
        }
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