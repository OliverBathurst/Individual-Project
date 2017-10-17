package com.oliver.bathurst.individualproject;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class DeviceFragment extends PreferenceFragment {
    private static final String ANDROID_INFO = "androidInfo", DEVICE_INFO = "deviceInfo", CELL_INFO = "cellInfo";

    public DeviceFragment() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_device);
        PermissionsManager permMan = new PermissionsManager(getActivity());
        findPreference(ANDROID_INFO).setSummary(permMan.getAndroidVersion());
        findPreference(DEVICE_INFO).setSummary(permMan.getDeviceAttributes());
        findPreference(CELL_INFO).setSummary(permMan.getCellInfo());

        Preference locationIntent = findPreference("settings_location_settings");
        locationIntent.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return false;
            }
        });

        Preference screenLock = findPreference("screen_lock_settings");
        screenLock.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD));
                return false;
            }
        });
    }
}