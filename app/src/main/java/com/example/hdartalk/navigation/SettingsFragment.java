package com.example.hdartalk.navigation;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.hdartalk.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
