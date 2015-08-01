package com.fairphone.privacyimpact;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fairphone.privacyimpact.appdata.OperationsManager;
import com.fairphone.privacyimpact.appdata.PrivacyImpactCalculator;
import com.fairphone.privacyimpact.database.AppSettingsDatabaseHelper;
import com.fairphone.privacyimpact.ui.PrivacyImpactPopupDialog;
import com.fairphone.privacyimpact.ui.TripleSwitchView;

import java.util.List;

public class GrantAccessActivity extends FragmentActivity {

    private final static String TAG = GrantAccessActivity.class.getSimpleName();
    private static final String PREFS_PRIVACY_IMPACT = "com.fairphone.privacyimpact.PREFS_PRIVACY_IMPACT";
    private static final String SHOW_PRIVACY_IMPACT_OOBE = "com.fairphone.privacyimpact.SHOW_PRIVACY_OOBE";
    private static final boolean DEBUG = false;
    public static final String HIDE_PRIVACY_IMPACT_PREFERENCE = "hide_privacy_impact_preference";

    private AppSettingsDatabaseHelper mDatabase;
    private Intent mOriginalIntent;
    private Bundle mOptions;

    private PrivacyImpactCalculator.PRIVACY_LEVEL mPrivacyLevel;
    private SharedPreferences mSharedPrefs;
    private SharedPreferences mDefaultSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	try {
	        // setup the database
	        mDatabase = new AppSettingsDatabaseHelper(this);

	        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	        // treat the launch intent
	        processLaunchIntent(getIntent());

	        mSharedPrefs = getSharedPreferences(PREFS_PRIVACY_IMPACT, Context.MODE_PRIVATE);
	        // setup the layout
	        setupLayout();
	} catch (Exception e) {
		Log.e(TAG, "Failed to create Privacy Impact screen", e);
	}
    }

    private void processLaunchIntent(Intent intent) {
        Log.wtf(TAG, "GrantAccessActivity - start processLaunchIntent --------------------------------------");
        Intent mLastPermissionsIntent = intent;

        Bundle extras = mLastPermissionsIntent.getExtras();

        Log.wtf(TAG, "GrantAccessActivity - Count : " + extras.size());

        for (String key : extras.keySet()) {
            String result = "";
            if (extras.get(key) != null) {
                result = extras.get(key).toString();
            }

            Log.wtf(TAG, "Key : " + key + " type " + result);
        }


        mOriginalIntent = (Intent) extras.get("originalIntent");

        Log.wtf(TAG, "GrantAccessActivity - " + mOriginalIntent.getComponent().getPackageName());
        ;

        boolean hidePrivacyImpact = mDefaultSharedPreferences.getBoolean(HIDE_PRIVACY_IMPACT_PREFERENCE, false);
        if (mDatabase.isPackageEnable(mOriginalIntent.getComponent().getPackageName()) ||
                hidePrivacyImpact ||
                (mOriginalIntent.resolveActivityInfo(getPackageManager(), 0).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {

            Log.i(TAG, "GrantAccessActivity - Package is enabled ------------------------------ ");

            mOptions = (Bundle) extras.get("originalOptions");

            startApplication(mOriginalIntent, mOptions);
        }

        Log.wtf(TAG, "GrantAccessActivity - end processLaunchIntent --------------------------------------");
    }

    private void setupLayout() {
        setContentView(R.layout.activity_grant_access);

        final FrameLayout oobeGroup = (FrameLayout) findViewById(R.id.oobe_group);
        if (mSharedPrefs.getBoolean(SHOW_PRIVACY_IMPACT_OOBE, true)) {
            final View privacyOobePopup = findViewById(R.id.privacy_oobe_popup);
            TextView privacyOobeButton = (TextView) findViewById(R.id.privacy_got_it);
            final View notificationsOobePopup = findViewById(R.id.notifications_oobe_popup);
            TextView notificationsOobeButton = (TextView) findViewById(R.id.notifications_got_it);

            privacyOobeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    privacyOobePopup.setVisibility(View.GONE);
                    notificationsOobePopup.setVisibility(View.VISIBLE);
                }
            });

            notificationsOobeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notificationsOobePopup.setVisibility(View.GONE);
                    oobeGroup.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = mSharedPrefs.edit();
                    editor.putBoolean(SHOW_PRIVACY_IMPACT_OOBE, false);
                    editor.apply();
                }
            });
        } else {
            oobeGroup.setVisibility(View.GONE);
        }

        final ComponentName component = mOriginalIntent.getComponent();

        PackageManager pm = getPackageManager();

        // setup the app icon
        ImageView mAppIcon = (ImageView) findViewById(R.id.app_icon);

        try {
            mAppIcon.setImageDrawable(pm.getActivityIcon(mOriginalIntent));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        // setup the app name
        TextView mAppName = (TextView) findViewById(R.id.app_name);
        try {
            mAppName.setText(pm.getActivityInfo(component, 0).loadLabel(pm));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView mLaunchApp = (TextView) findViewById(R.id.start_the_app_button);

        mLaunchApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch Intent to App Settings
                mDatabase.addPackageName(component.getPackageName());

                startApplication(mOriginalIntent, mOptions);
            }
        });

        TextView mTakeMeBackBtn = (TextView) findViewById(R.id.take_me_back_button);

        mTakeMeBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(component.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TripleSwitchView mNotificationSwitch = new TripleSwitchView(this, findViewById(R.id.notification_switch), appInfo);


        final Pair<PackageInfo, List<String>> permissionInfo = OperationsManager.getValidPermissionListForPackage(pm, component.getPackageName());

        Pair<PrivacyImpactCalculator.PRIVACY_LEVEL, Double> privacy = PrivacyImpactCalculator.calculateLevel(permissionInfo.second);
        mPrivacyLevel = privacy.first;

        View mPrivacyImpactGroup = findViewById(R.id.privacy_group);
        mPrivacyImpactGroup.setBackgroundResource(PrivacyImpactCalculator.getPrivacyBackground(mPrivacyLevel));
        TextView mPrivacyLevelText = (TextView) findViewById(R.id.privacy_level);
        mPrivacyLevelText.setText(PrivacyImpactCalculator.getPrivacyName(this, mPrivacyLevel) + (DEBUG ? " -- " + privacy.second : ""));
        mPrivacyImpactGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(GrantAccessActivity.this, "Priority is " + PrivacyImpactCalculator.getPrivacyName(GrantAccessActivity.this, mPrivacyLevel), Toast.LENGTH_SHORT).show();
                PrivacyImpactPopupDialog popupDialog = new PrivacyImpactPopupDialog();
                popupDialog.setPrivacyLevel(mPrivacyLevel);
                popupDialog.setPackageInfoAndPermissionList(permissionInfo.first, permissionInfo.second);
                FragmentManager fm = getSupportFragmentManager();
                popupDialog.show(fm, mPrivacyLevel.name());
            }
        });

        CheckBox hidePrivacyImpactCheckbox = (CheckBox) findViewById(R.id.hide_privacy_impact_checkbox);

        hidePrivacyImpactCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = mDefaultSharedPreferences.edit();
                editor.putBoolean(HIDE_PRIVACY_IMPACT_PREFERENCE, isChecked);
                editor.apply();
            }
        });
    }

    private void startApplication(Intent originalIntent, Bundle options) {
        try {
            originalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (options != null) {
                startActivityForResult(originalIntent, -1, options);
                finish();
            } else {
                // Note we want to go through this call for compatibility with
                // applications that may have overridden the method.
                startActivityForResult(originalIntent, -1);
                finish();
            }
        } catch (RuntimeException ren) {
            Log.e(TAG, "Could not launch application", ren);
        }
    }
}
