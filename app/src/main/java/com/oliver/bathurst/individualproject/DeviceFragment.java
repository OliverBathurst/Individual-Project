package com.oliver.bathurst.individualproject;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.provider.Settings;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class DeviceFragment extends PreferenceFragment {
    private static final String ANDROID_INFO = "androidInfo", DEVICE_INFO = "deviceInfo", CELL_INFO = "cellInfo", APP_INFO = "projectVersion";

    public DeviceFragment() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_device);
        PermissionsManager permMan = new PermissionsManager(getActivity());
        findPreference(ANDROID_INFO).setSummary(permMan.getAndroidVersion());
        findPreference(DEVICE_INFO).setSummary(permMan.getDeviceAttributes());
        findPreference(CELL_INFO).setSummary(permMan.getCellInfo());
        findPreference(APP_INFO).setSummary(permMan.getAppInfo());

        findPreference("settings_location_settings").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return false;
        });

        findPreference("screen_lock_settings").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD));
            return false;
        });
    }
}