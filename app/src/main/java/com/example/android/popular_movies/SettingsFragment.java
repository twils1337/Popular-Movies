package com.example.android.popular_movies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingsFragment extends PreferenceFragmentCompat implements
        OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_main);

        PreferenceScreen prefScreen = getPreferenceScreen();

        ListPreference p = (ListPreference) prefScreen.getPreference(0);
        p.setSummary(p.getEntry());
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ListPreference p = (ListPreference) findPreference(key);
        if (p != null) {
            p.setSummary(p.getEntry());
        }
    }
}