package com.fairphone.privacyimpact.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fairphone.privacyimpact.NotificationHandler;
import com.fairphone.privacyimpact.R;

/**
 * Created by jpascoal on 29/06/2015.
 */
public class TripleSwitchView {
    private static final String TAG = TripleSwitchStates.class.getSimpleName();
    private final Context mContext;
    private final FrameLayout mTripleSwitchGroup;
    private final ApplicationInfo mAppInfo;
    private final NotificationHandler mNotificationHandler;
    private View mToggleBackground;
    private ToggleButton mOffToggleButton;
    private ToggleButton mOnToggleButton;
    private ToggleButton mPriorityToggleButton;
    private TextView mStatusText;

    public enum TripleSwitchStates {
        OFF,
        ON,
        PRIORITY
    }

    private TripleSwitchStates mCurrentTripleSwitchState = TripleSwitchStates.OFF;

    public TripleSwitchView(Context context, View view, ApplicationInfo appInfo) {
        mTripleSwitchGroup = (FrameLayout) view;
        mContext = context;
        mAppInfo = appInfo;
        mNotificationHandler = new NotificationHandler(mContext);
        setupViews();
    }


    private void setupViews() {
        View mNotificationGroup = mTripleSwitchGroup.findViewById(R.id.notification_group);
        mStatusText = (TextView) mTripleSwitchGroup.findViewById(R.id.status);
        FrameLayout mTripleSwitch = (FrameLayout) mTripleSwitchGroup.findViewById(R.id.triple_switch_group);
        mToggleBackground = mTripleSwitchGroup.findViewById(R.id.toggle_background);
        mOffToggleButton = (ToggleButton) mTripleSwitchGroup.findViewById(R.id.off_toggle_button);
        mOnToggleButton = (ToggleButton) mTripleSwitchGroup.findViewById(R.id.on_toggle_button);
        mPriorityToggleButton = (ToggleButton) mTripleSwitchGroup.findViewById(R.id.priority_toggle_button);

        setCurrentTripleSwitchState();

        mOffToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTripleSwitchState();
            }
        });
        mOnToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTripleSwitchState();
            }
        });
        mPriorityToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTripleSwitchState();
            }
        });

        mNotificationGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources resources = mContext.getResources();
                switch (mCurrentTripleSwitchState) {
                    case ON:
                        Toast.makeText(mContext, resources.getString(R.string.notifications_on).toUpperCase() + "\n" + resources.getString(R.string.notifications_on_description), Toast.LENGTH_SHORT).show();
                        break;
                    case OFF:
                        Toast.makeText(mContext, resources.getString(R.string.notifications_off).toUpperCase() + "\n" + resources.getString(R.string.notifications_off_description), Toast.LENGTH_SHORT).show();
                        break;
                    case PRIORITY:
                        Toast.makeText(mContext, resources.getString(R.string.notifications_priority).toUpperCase() + "\n" + resources.getString(R.string.notifications_priority_description), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Log.wtf(TAG, "Unknow triple switch state: " + mCurrentTripleSwitchState);
                        break;
                }
            }
        });
    }

    private void changeTripleSwitchState() {
        Resources resources = mContext.getResources();

        switch (mCurrentTripleSwitchState) {
            case OFF:
                setTripleSwitchOn(resources);

                if (mAppInfo != null) {
                    mNotificationHandler.setNotificationsBanned(mAppInfo.packageName, mAppInfo.uid, false);
                    mNotificationHandler.setHighPriority(mAppInfo.packageName, mAppInfo.uid, false);
                }
                break;
            case ON:
                setTripleSwitchPriority(resources);

                if (mAppInfo != null) {
                    mNotificationHandler.setNotificationsBanned(mAppInfo.packageName, mAppInfo.uid, false);
                    mNotificationHandler.setHighPriority(mAppInfo.packageName, mAppInfo.uid, true);
                }
                break;
            case PRIORITY:
                setTripleSwitchOff(resources);

                if (mAppInfo != null) {
                    mNotificationHandler.setNotificationsBanned(mAppInfo.packageName, mAppInfo.uid, true);
                    mNotificationHandler.setHighPriority(mAppInfo.packageName, mAppInfo.uid, false);
                }
                break;
            default:
                Log.wtf(TAG, "Unknow triple switch state: " + mCurrentTripleSwitchState);
                break;
        }
    }

    private void setCurrentTripleSwitchState() {
        Resources resources = mContext.getResources();

        if ((!mNotificationHandler.getNotificationsBanned(mAppInfo.packageName, mAppInfo.uid)) &&
                mNotificationHandler.getHighPriority(mAppInfo.packageName, mAppInfo.uid)) {
            setTripleSwitchPriority(resources);
        } else if (mNotificationHandler.getNotificationsBanned(mAppInfo.packageName, mAppInfo.uid)) {
            setTripleSwitchOff(resources);
        } else {
            setTripleSwitchOn(resources);
        }
    }

    private void setTripleSwitchOff(Resources resources) {
        mToggleBackground.setBackgroundResource(R.drawable.toggle_switch_background_grey);
        mOffToggleButton.setChecked(true);
        mOnToggleButton.setChecked(false);
        mPriorityToggleButton.setChecked(false);
        mStatusText.setTextColor(resources.getColor(R.color.text_grey_dark));
        mStatusText.setText(resources.getString(R.string.notifications_off));
        mCurrentTripleSwitchState = TripleSwitchStates.OFF;
    }

    private void setTripleSwitchPriority(Resources resources) {
        mToggleBackground.setBackgroundResource(R.drawable.toggle_switch_background_green);
        mOffToggleButton.setChecked(false);
        mOnToggleButton.setChecked(false);
        mPriorityToggleButton.setChecked(true);
        mStatusText.setTextColor(resources.getColor(R.color.green));
        mStatusText.setText(resources.getString(R.string.notifications_priority));
        mCurrentTripleSwitchState = TripleSwitchStates.PRIORITY;
    }

    private void setTripleSwitchOn(Resources resources) {
        mToggleBackground.setBackgroundResource(R.drawable.toggle_switch_background_blue);
        mOffToggleButton.setChecked(false);
        mOnToggleButton.setChecked(true);
        mPriorityToggleButton.setChecked(false);
        mStatusText.setTextColor(resources.getColor(R.color.blue));
        mStatusText.setText(resources.getString(R.string.notifications_on));
        mCurrentTripleSwitchState = TripleSwitchStates.ON;
    }
}
