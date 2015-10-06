package com.fairphone.privacyimpact;

import android.app.Activity;
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

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Log.wtf(TAG, "GrantAccessActivity - onCreate");
        try {
            Intent startIntent = getIntent();
            
            if (showPrivacyImpact(startIntent)) {
                 Log.wtf(TAG, "if");
                try {
                    // setup the layout
                    setupLayout(startIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to setup Privacy Impact screen", e);
                    startApplication(startIntent);
                }
            } else {
                Log.wtf(TAG, "else");
                startApplication(startIntent);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Privacy Impact", e);
        }
    }

    private boolean showPrivacyImpact(final Intent startIntent) {
        Log.wtf(TAG, "GrantAccessActivity - start showPrivacyImpact");
        
        Bundle extras = startIntent.getExtras();
        Intent originalIntent = (Intent) extras.get("originalIntent");
        Bundle originalOptions = (Bundle) extras.get("originalOptions");
        int originalRequestCode = (int) extras.get("originalRequestCode");

        // log intent extras
        if (DEBUG) {
            Log.d(TAG, "GrantAccessActivity - Count : " + extras.size());
    
            for (String key : extras.keySet()) {
                String result = "";
                if (extras.get(key) != null) {
                    result = extras.get(key).toString();
                }
    
                Log.d(TAG, "Key : " + key + " type " + result);
            }
        }

        Log.i(TAG, "GrantAccessActivity - " + originalIntent.getComponent().getPackageName());

        // Is the hide popup preference checked?
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean popupDisabled = defaultSharedPreferences.getBoolean(HIDE_PRIVACY_IMPACT_PREFERENCE, false);

        // Has it been validated already?
        boolean impactAccepted =
            AppSettingsDatabaseHelper.isPackageEnable(originalIntent.getComponent().getPackageName());
        
        // Is it a system app?
        boolean isSystemApp = (originalIntent.resolveActivityInfo(getPackageManager(), 0).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
        
        boolean showPrivacyImpact = !popupDisabled && !impactAccepted && !isSystemApp;
        
        Log.d(TAG, "popupDisabled "+popupDisabled);
        Log.d(TAG, "impactAccepted "+impactAccepted);
        Log.d(TAG, "isSystemApp "+isSystemApp);
        
        Log.i(TAG, "GrantAccessActivity - "+(showPrivacyImpact ? "show popup" : "skip popup"));
        return showPrivacyImpact;
    }

    private void setupLayout(Intent startIntent) {
        setContentView(R.layout.activity_grant_access);

        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_PRIVACY_IMPACT, Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean(SHOW_PRIVACY_IMPACT_OOBE, true)) {
            View privacyOobeButton = findViewById(R.id.privacy_got_it);
            privacyOobeButton.setOnClickListener(new OnClickListenerInActivity(this) {
                @Override
                public void onClick(View v) {
                    mActivity.findViewById(R.id.privacy_oobe_popup).setVisibility(View.GONE);
                    mActivity.findViewById(R.id.notifications_oobe_popup).setVisibility(View.VISIBLE);
                }
            });

            View notificationsOobeButton = findViewById(R.id.notifications_got_it);
            notificationsOobeButton.setOnClickListener(new OnClickListenerInActivity(this) {
                @Override
                public void onClick(View v) {
                    mActivity.findViewById(R.id.notifications_oobe_popup).setVisibility(View.GONE);
                    mActivity.findViewById(R.id.oobe_group).setVisibility(View.GONE);
                    SharedPreferences sharedPrefs = mActivity.getSharedPreferences(PREFS_PRIVACY_IMPACT, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean(SHOW_PRIVACY_IMPACT_OOBE, false);
                    editor.apply();
                }
            });
        } else {
            View oobeGroup = findViewById(R.id.oobe_group);
            oobeGroup.setVisibility(View.GONE);
        }


        Intent originalIntent = (Intent) startIntent.getExtras().get("originalIntent");
        ComponentName component = originalIntent.getComponent();

        PackageManager pm = getPackageManager();

        // setup the app icon
        ImageView mAppIcon = (ImageView) findViewById(R.id.app_icon);

        try {
            mAppIcon.setImageDrawable(pm.getActivityIcon(originalIntent));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        // setup the app name
        TextView appName = (TextView) findViewById(R.id.app_name);
        try {
            appName.setText(pm.getActivityInfo(component, 0).loadLabel(pm));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        View launchApp = findViewById(R.id.start_the_app_button);

        launchApp.setOnClickListener(new View.OnClickListener() {
            private Intent mStartIntent;
            private View.OnClickListener setup(Intent si) {
                mStartIntent = si;
                return this;
            }
            @Override
            public void onClick(View v) {
                Intent originalIntent = ((Intent) mStartIntent.getExtras().get("originalIntent"));
                // Launch Intent to App Settings
                AppSettingsDatabaseHelper.addPackageName(originalIntent.getComponent().getPackageName());
                startApplication(mStartIntent);
            }
        }.setup(startIntent));

        View takeMeBackBtn = findViewById(R.id.take_me_back_button);

        takeMeBackBtn.setOnClickListener(new View.OnClickListener() {
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


        Pair<PackageInfo, List<String>> permissionInfo = OperationsManager.getValidPermissionListForPackage(pm, component.getPackageName());

        Pair<PrivacyImpactCalculator.PRIVACY_LEVEL, Double> privacy = PrivacyImpactCalculator.calculateLevel(permissionInfo.second);
        PrivacyImpactCalculator.PRIVACY_LEVEL privacyLevel = privacy.first;

        View privacyImpactGroup = findViewById(R.id.privacy_group);
        privacyImpactGroup.setBackgroundResource(PrivacyImpactCalculator.getPrivacyBackground(privacyLevel));

        TextView privacyLevelText = (TextView) findViewById(R.id.privacy_level);
        privacyLevelText.setText(PrivacyImpactCalculator.getPrivacyName(this, privacyLevel) + (DEBUG ? " -- " + privacy.second : ""));

        privacyImpactGroup.setOnClickListener(new View.OnClickListener() {
            private Pair<PackageInfo, List<String>> mPermissionInfo; 
            private PrivacyImpactCalculator.PRIVACY_LEVEL mPrivacyLevel;
            public View.OnClickListener setup(PrivacyImpactCalculator.PRIVACY_LEVEL lvl, Pair<PackageInfo, List<String>> pi){
                mPermissionInfo = pi;
                mPrivacyLevel = lvl;
                return this;
            }
            @Override
            public void onClick(View v) {
                //Toast.makeText(GrantAccessActivity.this, "Priority is " + PrivacyImpactCalculator.getPrivacyName(GrantAccessActivity.this, mPrivacyLevel), Toast.LENGTH_SHORT).show();
                PrivacyImpactPopupDialog popupDialog = new PrivacyImpactPopupDialog();
                popupDialog.setPrivacyLevel(mPrivacyLevel);
                popupDialog.setPackageInfoAndPermissionList(mPermissionInfo.first, mPermissionInfo.second);
                FragmentManager fm = getSupportFragmentManager();
                popupDialog.show(fm, mPrivacyLevel.name());
            }
        }.setup(privacyLevel, permissionInfo));

        CheckBox hidePrivacyImpactCheckbox = (CheckBox) findViewById(R.id.hide_privacy_impact_checkbox);

        hidePrivacyImpactCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private Context mContext;
            private CompoundButton.OnCheckedChangeListener setup(Context ctx) {
                mContext = ctx;
                return this;
            }
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {        
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                editor.putBoolean(HIDE_PRIVACY_IMPACT_PREFERENCE, isChecked);
                editor.apply();
            }
        }.setup(this));
    }

    private void startApplication(Intent startIntent) {
        Bundle extras = startIntent.getExtras();
        Intent originalIntent = (Intent) extras.get("originalIntent");
        Bundle originalOptions = (Bundle) extras.get("originalOptions");
        int originalRequestCode = (int) extras.get("originalRequestCode");
        try {
            originalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (originalOptions != null) {
                startActivityForResult(originalIntent, originalRequestCode, originalOptions);
            } else {
                // Note we want to go through this call for compatibility with
                // applications that may have overridden the method.
                startActivityForResult(originalIntent, originalRequestCode);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Could not launch application", e);
        }
        finish();
    }
    
    private static abstract class OnClickListenerInActivity implements View.OnClickListener {
        protected Activity mActivity;
        OnClickListenerInActivity(Activity ctx) {
            mActivity = ctx;
        }
        public abstract void onClick (View v);
    }
}
