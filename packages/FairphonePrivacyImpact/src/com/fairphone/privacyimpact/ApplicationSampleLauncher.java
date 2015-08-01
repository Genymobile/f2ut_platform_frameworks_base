package com.fairphone.privacyimpact;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fairphone.privacyimpact.appdata.AppDataUtils;
import com.fairphone.privacyimpact.appdata.AppInfo;

import java.util.List;

/**
 * Created by Tiago Costa on 17/03/15.
 */
public class ApplicationSampleLauncher extends Activity {

    private final static String TAG = ApplicationSampleLauncher.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup the layout
        setupLayout();
    }

    private void setupLayout() {
        setContentView(R.layout.activity_application_sample_launcher);

        ListView mAppList = (ListView) findViewById(R.id.app_list);

        List<AppInfo> installedApps = AppDataUtils.getAllInstalledApps(this, false);
        mAppList.setAdapter(new AppListAdapter(this, installedApps));

    }

    private void LaunchApplication(String packageName, String activityName) {

        Intent newIntent = new Intent();
        newIntent.setComponent(new ComponentName(this, GrantAccessActivity.class));

        Intent originalIntent = new Intent();
        originalIntent.setComponent(new ComponentName(packageName, activityName));

        newIntent.putExtra("originalIntent", originalIntent);
        newIntent.putExtra("originalOptions", new Bundle());

        startActivity(newIntent);
    }

    public class AppListAdapter extends ArrayAdapter<AppInfo> {

        private final PackageManager mPm;

        public AppListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);

            mPm = getContext().getPackageManager();
        }

        public AppListAdapter(Context context, List<AppInfo> items) {
            super(context, R.layout.activity_launcher_list_item, items);

            mPm = getContext().getPackageManager();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if(v == null) {

                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.activity_launcher_list_item, parent, false);
            }
            ImageView icon = (ImageView) v.findViewById(R.id.icon);
            TextView name = (TextView)v.findViewById(R.id.name);
            View button = v.findViewById(R.id.launch_app_button);

            final AppInfo p = getItem(position);

            if(p != null) {
                icon.setImageDrawable(p.getIcon());
                name.setText(p.getAppName());


                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PackageManager pm = ApplicationSampleLauncher.this.getPackageManager();

                        try {
                            PackageInfo packageInfo = pm.getPackageInfo(p.getPackageName(), 0);

                            LaunchApplication(packageInfo.packageName, p.getClassName());
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }

            return v;

        }
    }


}
