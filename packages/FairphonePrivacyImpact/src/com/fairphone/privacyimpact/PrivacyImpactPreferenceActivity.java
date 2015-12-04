package com.fairphone.privacyimpact;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.fairphone.privacyimpact.database.AppSettingsDatabaseHelper;

/**
 * Created by jpascoal on 13/07/2015.
 */
public class PrivacyImpactPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.privacy_impact_preference);
            Preference p = findPreference(GrantAccessActivity.HIDE_PRIVACY_IMPACT_PREFERENCE);
            p.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange (Preference preference, Object newValue) {
                        final boolean val = (Boolean) newValue;
                        AppSettingsDatabaseHelper.setPrivacyImpactStatus(!val);
                        return true;
                    }
                }
            );
        }
    }
}
