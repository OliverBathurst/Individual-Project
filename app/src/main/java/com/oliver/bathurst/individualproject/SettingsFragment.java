package com.oliver.bathurst.individualproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class SettingsFragment extends PreferenceFragment {
    private int setVolProg = 90, battProg = 5;
    private Server server = null;
    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);
        final PolicyManager pol = new PolicyManager(getActivity());
        final PermissionsManager newPerm = new PermissionsManager(getActivity());
        final SharedPreferences.Editor settings = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        final SharedPreferences settingsView = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try{
            CheckBoxPreference hideCheck = (CheckBoxPreference) findPreference("hide_app");
            hideCheck.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean a = preference.getSharedPreferences().getBoolean("hide_app",false);
                    if(a){
                        Toast.makeText(getActivity(), "WARNING: note down SMS trigger now!", Toast.LENGTH_SHORT).show();
                        PackageManager p = getActivity().getPackageManager();
                        ComponentName componentName = new ComponentName(getActivity(), Login.class);
                        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                        Toast.makeText(getActivity(), "Hidden", Toast.LENGTH_SHORT).show();
                    }else{
                        PackageManager p = getActivity().getPackageManager();
                        ComponentName componentName = new ComponentName(getActivity(), Login.class);
                        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        Toast.makeText(getActivity(), "Visible", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
            Preference vol = findPreference("sms_ringtone_volume");
            vol.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Dialog dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.dialog);
                    dialog.setTitle("Set volume");
                    dialog.setCancelable(true);
                    dialog.show();
                    SeekBar seek = (SeekBar) dialog.findViewById(R.id.size_seekbar);
                    final TextView tv_dialog_size = (TextView) dialog.findViewById(R.id.set_size_help_text);

                    dialog.setOnCancelListener(new Dialog.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            settings.putInt("seek_bar_volume", setVolProg);
                            settings.apply();
                            Preference volPref = findPreference("sms_ringtone_volume");
                            volPref.setSummary("Current volume: " + setVolProg + "%");
                        }
                    });
                    dialog.setOnDismissListener(new Dialog.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            settings.putInt("seek_bar_volume", setVolProg);
                            settings.apply();
                            Preference volPref = findPreference("sms_ringtone_volume");
                            volPref.setSummary("Current volume: " + setVolProg + "%");
                        }
                    });
                    seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                            tv_dialog_size.setText("Please select volume: (" + progress+ "%)");
                            setVolProg = progress;
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar arg0) {}
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                    return false;
                }
            });

            Preference battPercent = findPreference("battery_percent");
            battPercent.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Dialog dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.dialog2);
                    dialog.setTitle("Set battery percent");
                    dialog.setCancelable(true);
                    dialog.show();
                    SeekBar seek = (SeekBar) dialog.findViewById(R.id.size_seekbar2);
                    final TextView tv_dialog_size = (TextView) dialog.findViewById(R.id.set_size_help_text2);

                    dialog.setOnCancelListener(new Dialog.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            settings.putInt("seek_bar_battery", battProg);
                            settings.apply();
                            Preference volPref = findPreference("battery_percent");
                            volPref.setSummary("Current percentage: " + battProg + "%");
                        }
                    });
                    dialog.setOnDismissListener(new Dialog.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            settings.putInt("seek_bar_battery", battProg);
                            settings.apply();
                            Preference volPref = findPreference("battery_percent");
                            volPref.setSummary("Current percentage: " + battProg + "%");
                        }
                    });
                    seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                            tv_dialog_size.setText("Please select percentage: (" + progress+ "%)");
                            battProg = progress;
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar arg0) {}
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                    return false;
                }
            });


            Preference devAdmin = findPreference("grant_device_admin");
            devAdmin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(!pol.isAdminActive()) {
                        Intent activateDeviceAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(getActivity(), DeviceAdmin.class));
                        startActivityForResult(activateDeviceAdmin, PolicyManager.DPM_ACTIVATION_REQUEST_CODE);
                    }else{
                        Toast.makeText(getActivity(), "Admin already active", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            SwitchPreference serve = (SwitchPreference) findPreference("local_server");
            serve.setChecked(Server.running);

            serve.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean switched = ((SwitchPreference) preference).isChecked();
                    if (!switched){
                        server = new Server(getActivity());
                        server.start();
                        Toast.makeText(getActivity(), "Server started on: " + server.getIP() + ":" + server.getPort(), Toast.LENGTH_SHORT).show();
                    }else{
                        if(server != null){
                            server.stop();
                        }
                    }
                    return true;
                }
            });

            Preference reg = findPreference("register");
            reg.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //register();
                    return false;
                }
            });
            Preference unreg = findPreference("unregister");
            unreg.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //unregister();
                    return false;
                }
            });


            EditTextPreference emailUpdates = (EditTextPreference) findPreference("email_string");
            if (emailUpdates.getText() != null && emailUpdates.getText().trim().length() != 0) {
                emailUpdates.setSummary(emailUpdates.getText());
            }

            ////////TRIGGERS/////////////////////
            EditTextPreference smsRing = (EditTextPreference) findPreference("sms_ring");
            if (smsRing.getText() != null && smsRing.getText().trim().length() != 0) {
                smsRing.setSummary("Trigger: " + smsRing.getText());
            }else{
                savePref(settings, smsRing, "ring12345", "sms_ring");
            }

            EditTextPreference smsEmail = (EditTextPreference) findPreference("sms_relay_email");
            if (smsEmail.getText() != null && smsEmail.getText().trim().length() != 0) {
                smsEmail.setSummary("Trigger: " + smsEmail.getText());
            }else{
                savePref(settings, smsEmail, "email12345", "sms_relay_email");
            }

            EditTextPreference smsText = (EditTextPreference) findPreference("sms_relay_text");
            if (smsText.getText() != null && smsText.getText().trim().length() != 0) {
                smsText.setSummary("Trigger: " + smsText.getText());
            }else{
                savePref(settings, smsText, "text12345", "sms_relay_text");
            }
            //sms_loc_services
            EditTextPreference smsLoc = (EditTextPreference) findPreference("sms_loc_services");
            if (smsLoc.getText() != null && smsLoc.getText().trim().length() != 0) {
                smsLoc.setSummary("Trigger: " + smsLoc.getText());
            }else{
                savePref(settings, smsLoc, "remoteEnableLocation", "sms_loc_services");
            }

            //sms_remote_lock
            EditTextPreference smsLock = (EditTextPreference) findPreference("sms_remote_lock");
            if (smsLock.getText() != null && smsLock.getText().trim().length() != 0) {
                smsLock.setSummary("Trigger: " + smsLock.getText());
            }else{
                savePref(settings, smsLock, "remoteEnableLock", "sms_remote_lock");
            }
            //sms_wipe
            EditTextPreference smsWipe = (EditTextPreference) findPreference("sms_wipe");
            if (smsWipe.getText() != null && smsWipe.getText().trim().length() != 0) {
                smsWipe.setSummary("Trigger: " + smsWipe.getText());
            }else{
                savePref(settings, smsWipe, "remoteEnableWIPE", "sms_wipe");
            }
            ////////////////////GMAIL//////////////////////////////////////
            EditTextPreference gmailLock = (EditTextPreference) findPreference("gmail_remote_lock");
            if (gmailLock.getText() != null && gmailLock.getText().trim().length() != 0) {
                gmailLock.setSummary("Trigger: " + gmailLock.getText());
            }else{
                savePref(settings, gmailLock, "remoteLockGmail", "gmail_remote_lock");
            }

            EditTextPreference gmailLoc = (EditTextPreference) findPreference("gmail_loc");
            if (gmailLoc.getText() != null && gmailLoc.getText().trim().length() != 0) {
                gmailLoc.setSummary("Trigger: " + gmailLoc.getText());
            }else{
                savePref(settings, gmailLoc, "remoteLocationGmail", "gmail_loc");
            }

            EditTextPreference gmailWipe = (EditTextPreference) findPreference("gmail_wipe");
            if (gmailWipe.getText() != null && gmailWipe.getText().trim().length() != 0) {
                gmailWipe.setSummary("Trigger: " + gmailWipe.getText());
            }else{
                savePref(settings, gmailWipe, "remoteWipeGmail", "gmail_wipe");
            }
            EditTextPreference SDWipe = (EditTextPreference) findPreference("wipe_sdcard");
            if (SDWipe.getText() != null && SDWipe.getText().trim().length() != 0) {
                SDWipe.setSummary("Trigger: " + SDWipe.getText());
            }else{
                savePref(settings, SDWipe, "remoteWipeSDCard", "wipe_sdcard");
            }

            EditTextPreference SDWipeGmail = (EditTextPreference) findPreference("wipe_sdcard_gmail");
            if (SDWipeGmail.getText() != null && SDWipeGmail.getText().trim().length() != 0) {
                SDWipeGmail.setSummary("Trigger: " + SDWipeGmail.getText());
            }else{
                savePref(settings, SDWipeGmail, "wipeSDCardGmail", "wipe_sdcard_gmail");
            }

            EditTextPreference hide = (EditTextPreference) findPreference("sms_hide_app");
            if (hide.getText() != null && hide.getText().trim().length() != 0) {
                hide.setSummary("Trigger: " + hide.getText());
            }else{
                savePref(settings, hide, "unHide", "sms_hide_app");
            }

            EditTextPreference ringDur = (EditTextPreference) findPreference("ring_duration");
            if (ringDur.getText() != null && ringDur.getText().trim().length() != 0) {
                ringDur.setSummary("Current: " + ringDur.getText() + "s");
            }else{
                ringDur.setSummary("Current: 20s (default)");
            }

            Preference volPref = findPreference("sms_ringtone_volume");
            volPref.setSummary("Current volume: " + settingsView.getInt("seek_bar_volume", 90) + "%");

            Preference battPref = findPreference("battery_percent");
            battPref.setSummary("Current percentage: " + settingsView.getInt("seek_bar_battery", 5) + "%");

            Preference backupPrefs = findPreference("backup_shared_pref");
            backupPrefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    actionSharedPref();
                    return false;
                }
            });

            Preference checkPerms = findPreference("check_perm");
            checkPerms.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    newPerm.permissionsCheckup();
                    return false;
                }
            });

            Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary("Trigger: " + newValue.toString());
                    return true;
                }
            };
            Preference geoFence = findPreference("geo_fence");
            geoFence.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(SettingsFragment.super.getActivity(), GeoFencing.class));
                    return false;
                }
            });
            Preference beacon = findPreference("beacon");
            beacon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(SettingsFragment.super.getActivity(), BeaconActivity.class));
                    return false;
                }
            });

            Preference sort_loc = findPreference("sort_loc");
            sort_loc.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(SettingsFragment.super.getActivity(), Reorder.class));
                    return false;
                }
            });

            Preference.OnPreferenceChangeListener simpleList = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary("Current: " + newValue.toString());
                    return true;
                }
            };
            Preference.OnPreferenceChangeListener secondsListener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary("Current: " + newValue.toString() + "s");
                    return true;
                }
            };

            ListPreference ringList = (ListPreference) findPreference("ringtone_select");
            try {
                HashMap<String, String> list = new HashMap<>();
                RingtoneManager ringtoneManager = new RingtoneManager(getActivity());
                Cursor cursor = ringtoneManager.getCursor();
                while (cursor.moveToNext()) {
                    String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                    String notificationUri = ringtoneManager.getRingtoneUri(cursor.getPosition()).toString();
                    list.put(notificationTitle, notificationUri);
                }
                ringList.setEntries(list.keySet().toArray(new CharSequence[0]));
                ringList.setEntryValues(list.values().toArray(new CharSequence[0]));
            }catch(Exception ignored){}

            emailUpdates.setOnPreferenceChangeListener(simpleList);
            smsRing.setOnPreferenceChangeListener(listener);
            smsEmail.setOnPreferenceChangeListener(listener);
            smsText.setOnPreferenceChangeListener(listener);
            smsLoc.setOnPreferenceChangeListener(listener);
            smsLock.setOnPreferenceChangeListener(listener);
            smsWipe.setOnPreferenceChangeListener(listener);
            hide.setOnPreferenceChangeListener(listener);
            ringDur.setOnPreferenceChangeListener(secondsListener);

            Preference genInfo = findPreference("general_info");
            Preference.OnPreferenceClickListener generalInfo = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Gmail triggers: once Gmail credentials are entered " +
                            "into the username and password boxes, this app will monitor that inbox for incoming " +
                            "mail every time the battery level changes. On a battery level change, it will attempt " +
                            "to connect to that Gmail account and search for unread emails and scans their subject " +
                            "line for triggers to perform actions. E.g. if a trigger to send the location is 'loc12345' " +
                            "then sending that as a subject in an email to the monitored account will trigger that action " +
                            "on a battery change i.e a charger is connected/disconnected, battery level rises/decreases. " +
                            " Additionally, this feature may require you to enable a Gmail setting called 'Enable Access From " +
                            "Less Secure Apps' on the monitored account.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return false;
                }
            };
            genInfo.setOnPreferenceClickListener(generalInfo);

        }catch(Exception ignored){}
    }
    private void savePref(SharedPreferences.Editor settings, Preference pref, String defaultVal, String prefTag){
        pref.setSummary("Trigger: " + defaultVal);
        settings.putString(prefTag, defaultVal);
        settings.apply();
    }
    private void actionSharedPref(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            File prefTxt = new File(Environment.getExternalStorageDirectory(), "prefs.txt");

            FileOutputStream fileInput = new FileOutputStream(prefTxt);
            OutputStreamWriter writer = new OutputStreamWriter(fileInput);
            String total="";
            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                total += "Key: "+ entry.getKey() + " Value: " + entry.getValue().toString()+"\n";
            }
            writer.write(total);
            writer.close();
            fileInput.close();

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            Uri fileUri = Uri.fromFile(prefTxt);
            sharingIntent.setType("file/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            startActivity(Intent.createChooser(sharingIntent, "Save"));
        }catch(Exception e){
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || requestCode != PolicyManager.DPM_ACTIVATION_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}