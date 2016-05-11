package com.android.keyguard;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.BatteryManager;
import android.os.Build;
import android.os.UserHandle;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rrocha on 8/24/15.
 */
public class FairphoneClockView extends LinearLayout
{
	private static final String TAG = FairphoneClockView.class.getSimpleName();
	private static final String BOARD_DATE_FILE = "/persist/board_date.bin";
	private ViewGroup[] CLOCK_WIDGET_VIEWS = null;

	private TextView mAmPmText, mAlarmText, mBatteryDaysLeft, mBatteryAmPmIndicator, mHoursText,
			mMinutesText, mBatteryDescriptionText, mPomCurrentText, mPomRecordText, mElapsedYearsText,
			mYearsText, mElapsedMonthsText, mMonthsText, mElapsedDaysText, mDaysText;
	private View mDayIndicator, mBatteryTimeGroup, mLastLongerButton, mChargedText, mUnplugChargerText;
	private ImageView mBatteryLevelImage;
	private BroadcastReceiver mReceiver;
	private Runnable onDismissRunnable;

	Intent serviceIntent;
	private final OnClickListener viewClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			FairphoneClockData.getCurrentLayoutIdx(getContext());
			int currentLayoutIdx = (1 + FairphoneClockData.getCurrentLayoutIdx(getContext())) % CLOCK_WIDGET_VIEWS.length;
			FairphoneClockData.setCurrentLayoutIdx(getContext(), currentLayoutIdx);
			update(currentLayoutIdx);
		}
	};

	private static final int ATTR_CLOCK = 0x1;
	private static final int ATTR_POM = 0x2;
	private static final int ATTR_BATTERY = 0x4;
	private static final int ATTR_YOURS = 0x8;
	private static final int ATTR_ALL = 0xf;

	private int[] CLOCK_WIDGET_LAYOUTS = {R.id.clock_widget_main, R.id.clock_widget_peace_of_mind, R.id.clock_widget_battery, R.id.clock_widget_yours_since};

	@Override
	protected void onAttachedToWindow()
	{
		Log.w(TAG, "onAttachedToWindow");
		super.onAttachedToWindow();
		mReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				init(null);
			}
		};
		getContext().registerReceiver(mReceiver, new IntentFilter(FairphoneClockData.VIEW_UPDATE));
		serviceIntent = new Intent(getContext(), ClockScreenService.class);
		getContext().startService(serviceIntent);
	}

	@Override
	protected void onDetachedFromWindow()
	{
		Log.w(TAG, "onDetachedFromWindow");
		getContext().unregisterReceiver(mReceiver);
		getContext().stopService(serviceIntent);
		super.onDetachedFromWindow();
	}

        private final OnClickListener editClickListener = new OnClickListener()
        {
                @Override
                public void onClick(View v)
                {
			Intent launchIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
			dismissKeyguardOnNextActivity();
			UserHandle user = new UserHandle(UserHandle.USER_CURRENT);
			getContext().startActivityAsUser(launchIntent, null, user);
                }
        };

	private final OnClickListener shareClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int active_layout = CLOCK_WIDGET_LAYOUTS[FairphoneClockData.getCurrentLayoutIdx(getContext())];
			String shareText = null;
			if (active_layout == R.id.clock_widget_peace_of_mind) {
				shareText = getPeaceOfMindShareText();
			}
			else if (active_layout == R.id.clock_widget_yours_since) {
				shareText = getYourFairphoneSinceShareText();
			}
			else {
				Log.w(TAG, "Unknown Share button: " + active_layout);
			}
			if (!TextUtils.isEmpty(shareText))
			{
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.setType("text/plain");

				sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
				sendIntent = Intent.createChooser(sendIntent, getResources().getString(R.string.share_to));
				sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				dismissKeyguardOnNextActivity();
				onDismissRunnable = new Runnable(){
					private Intent mIntent;
					public Runnable setup(Intent intent){
						mIntent = intent;
						return this;
					}
					public void run(){
						UserHandle user = new UserHandle(UserHandle.USER_CURRENT);
						getContext().startActivityAsUser(mIntent, null, user);
					}	
				}.setup(sendIntent);
			}
		}
	};
	@Override
	public  void onWindowFocusChanged (boolean hasWindowFocus){
		Log.d(TAG, "onWindowFocusChanged "+hasWindowFocus);
		if (!hasWindowFocus && onDismissRunnable != null){
			onDismissRunnable.run();
			onDismissRunnable = null;
		}
	}

	private String getPeaceOfMindShareText()
	{
		long pom_current = FairphoneClockData.getPeaceOfMindCurrent(getContext());
		long pom_record = FairphoneClockData.getPeaceOfMindRecord(getContext());
		String shareText = String.format("%s %d %s!", getResources().getString(R.string.been_in_peace_for), pom_record, getResources().getString(R.string.minutes));
		return shareText;
	}

	private String getYourFairphoneSinceShareText()
	{
		Resources resources = getResources();
		Period p = getPeriod();
		StringBuilder s = new StringBuilder();
		if (p.diffYears != 0)
		{
			s.append(String.format("%02d ", p.diffYears));
			s.append(p.diffYears == 1 ? resources.getString(R.string.year) : resources.getString(R.string.years));
			s.append(", ");
			s.append(String.format("%02d ", p.diffMonths));
			s.append(p.diffMonths == 1 ? resources.getString(R.string.month) : resources.getString(R.string.months));
			s.append(", ");
			s.append(String.format("%02d ", p.diffMonthDays));
			s.append(p.diffMonthDays == 1 ? resources.getString(R.string.day) : resources.getString(R.string.days));
		}
		else if (p.diffMonths != 0)
		{
			s.append(String.format("%02d ", p.diffMonths));
			s.append(p.diffMonths == 1 ? resources.getString(R.string.month) : resources.getString(R.string.months));
			s.append(", ");
			s.append(String.format("%02d ", p.diffWeeks));
			s.append(p.diffWeeks == 1 ? resources.getString(R.string.week) : resources.getString(R.string.weeks));
			s.append(", ");
			s.append(String.format("%02d ", p.diffMonthWeekDays));
			s.append(p.diffMonthWeekDays == 1 ? resources.getString(R.string.day) : resources.getString(R.string.days));
		}
		else
		{
			s.append(String.format("%02d ", p.diffWeeks));
			s.append(p.diffWeeks == 1 ? resources.getString(R.string.week) : resources.getString(R.string.weeks));
			s.append(", ");
			s.append(String.format("%02d ", p.diffMonthWeekDays));
			s.append(p.diffMonthWeekDays == 1 ? resources.getString(R.string.day) : resources.getString(R.string.days));
			s.append(", ");
			s.append(String.format("%02d ", p.diffHours));
			s.append(p.diffHours == 1 ? resources.getString(R.string.hour) : resources.getString(R.string.hours));
		}
		return String.format("%s %s %s", getResources().getString(R.string.my_fairphone_is), s.toString(), getResources().getString(R.string.old));
	}

	public FairphoneClockView(Context context)
	{
		super(context);
		init(null);
	}

	public FairphoneClockView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs);
	}

	public FairphoneClockView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public FairphoneClockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);
	}


	private void init(AttributeSet attrs)
	{
		if (attrs != null)
		{
			TypedArray a = getContext().getTheme().obtainStyledAttributes(
					attrs,
					R.styleable.FairphoneClockView,
					0, 0);

			int showOnly = ATTR_ALL;
			try
			{
				showOnly = a.getInteger(R.styleable.FairphoneClockView_showOnly, ATTR_ALL);
			}
			finally
			{
				a.recycle();
			}
			if (showOnly != ATTR_ALL)
			{
				List<Integer> availableLayouts = new ArrayList<>();
				if ((showOnly&ATTR_CLOCK) == ATTR_CLOCK)
				{
					availableLayouts.add(R.id.clock_widget_main);
				}
				if ((showOnly&ATTR_POM) == ATTR_POM)
				{
					availableLayouts.add(R.id.clock_widget_peace_of_mind);
				}
				if ((showOnly&ATTR_BATTERY) == ATTR_BATTERY)
				{
					availableLayouts.add(R.id.clock_widget_battery);
				}
				if ((showOnly&ATTR_YOURS) == ATTR_YOURS)
				{
					availableLayouts.add(R.id.clock_widget_yours_since);
				}
				CLOCK_WIDGET_LAYOUTS  = new int[availableLayouts.size()];
				Iterator<Integer> iterator = availableLayouts.iterator();
				for (int i=0; i < CLOCK_WIDGET_LAYOUTS.length; i++)
				{
					CLOCK_WIDGET_LAYOUTS[i] = iterator.next();
				}
				int currentIdx = FairphoneClockData.getCurrentLayoutIdx(getContext());
				if (currentIdx >= CLOCK_WIDGET_LAYOUTS.length)
				{
					// somehow, the config changed. Let's reset
					FairphoneClockData.setCurrentLayoutIdx(getContext(), 0);
				}
			}
		}
		removeAllViews();
		ViewGroup mRootView = (ViewGroup) inflate(getContext(), R.layout.widget_main, this);
		CLOCK_WIDGET_VIEWS = new ViewGroup[CLOCK_WIDGET_LAYOUTS.length];
		for (int i = 0; i < CLOCK_WIDGET_LAYOUTS.length; ++i)
		{
			CLOCK_WIDGET_VIEWS[i] = (ViewGroup) mRootView.findViewById(CLOCK_WIDGET_LAYOUTS[i]);
			CLOCK_WIDGET_VIEWS[i].setOnClickListener(viewClickListener);
		}
		mAmPmText = (TextView) mRootView.findViewById(R.id.ampm_text);
		mAlarmText = (TextView) mRootView.findViewById(R.id.alarm_text);
		mDayIndicator = mRootView.findViewById(R.id.day_indicator);
		mBatteryTimeGroup = mRootView.findViewById(R.id.battery_time_group);
		mBatteryDaysLeft = (TextView) mRootView.findViewById(R.id.battery_days_left);
		mBatteryAmPmIndicator = (TextView) mRootView.findViewById(R.id.battery_am_pm_indicator);
		mHoursText = (TextView) mRootView.findViewById(R.id.hours_text);
		mMinutesText = (TextView) mRootView.findViewById(R.id.minutes_text);
		mBatteryLevelImage = (ImageView) mRootView.findViewById(R.id.battery_level_image);
		mBatteryDescriptionText = (TextView) mRootView.findViewById(R.id.battery_description);
		mLastLongerButton = mRootView.findViewById(R.id.last_longer_button);
		mChargedText = mRootView.findViewById(R.id.charged_text);
		mUnplugChargerText = mRootView.findViewById(R.id.unplug_charger_text);
		mPomCurrentText = (TextView) mRootView.findViewById(R.id.text_pom_current);
		mPomRecordText = (TextView) mRootView.findViewById(R.id.text_pom_record);

		mElapsedYearsText = (TextView) mRootView.findViewById(R.id.eleapsed_years_text);
		mYearsText = (TextView) mRootView.findViewById(R.id.years_text);
		mElapsedMonthsText = (TextView) mRootView.findViewById(R.id.eleapsed_months_text);
		mMonthsText = (TextView) mRootView.findViewById(R.id.months_text);
		mElapsedDaysText = (TextView) mRootView.findViewById(R.id.eleapsed_days_text);
		mDaysText = (TextView) mRootView.findViewById(R.id.days_text);

		mRootView.findViewById(R.id.clock_edit_button).setOnClickListener(editClickListener);

		mRootView.findViewById(R.id.peace_share_button).setOnClickListener(shareClickListener);
		mRootView.findViewById(R.id.yours_since_share_button).setOnClickListener(shareClickListener);
		mLastLongerButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				FairphoneClockData.sendLastLongerBroadcast(getContext());
			}
		});

		update();
	}

	public void update()
	{
		int currentLayoutIdx = FairphoneClockData.getCurrentLayoutIdx(getContext());
		update(currentLayoutIdx);
	}

	private void update(int currentLayoutIdx)
	{
		makeViewgroupVisible(CLOCK_WIDGET_VIEWS[currentLayoutIdx]);
		setupActiveView(currentLayoutIdx);
	}

	private void makeViewgroupVisible(ViewGroup viewGroup)
	{
		for (ViewGroup vg : CLOCK_WIDGET_VIEWS)
		{
			vg.setVisibility(vg.equals(viewGroup) ? VISIBLE : GONE);
		}
	}

	private void setupActiveView(int currentLayoutIdx)
	{
		int active_layout = CLOCK_WIDGET_LAYOUTS[currentLayoutIdx];
		if (active_layout == R.id.clock_widget_main) {
			setClockAmPm();
			setNextScheduledAlarm();
		} else if (active_layout == R.id.clock_widget_peace_of_mind) {
			setupPeaceOfMind();
		} else if (active_layout == R.id.clock_widget_battery) {
			setupBatteryLayout();
		} else if (active_layout == R.id.clock_widget_yours_since) {
			setYourFairphoneSince();
		} else {
			Log.e(TAG, "Unknown layout: " + active_layout);
		}
	}

	private void setClockAmPm()
	{
		//if (DateFormat.is24HourFormat(getContext()))
		//{
			mAmPmText.setVisibility(View.GONE);
		//}
		//else
		//{
		//	mAmPmText.setVisibility(View.VISIBLE);
		//	Calendar currentCalendar = Calendar.getInstance();

		//	int hour = currentCalendar.get(Calendar.HOUR_OF_DAY);

		//	if (hour < 12)
		//	{
		//		mAmPmText.setText(getContext().getResources().getString(R.string.time_am_default));
		//	}
		//	else
		//	{
		//		mAmPmText.setText(getContext().getResources().getString(R.string.time_pm_default));
		//	}
		//}
	}

	private void setNextScheduledAlarm()
	{
		String nextAlarm = getNextAlarm(getContext());

		if (TextUtils.isEmpty(nextAlarm))
		{
			mAlarmText.setVisibility(View.INVISIBLE);
		}
		else
		{
			mAlarmText.setText(nextAlarm);
			mAlarmText.setVisibility(View.VISIBLE);
		}
	}

	private static String getNextAlarm(Context context)
	{
		String nextAlarm = "";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			if (am != null && am.getNextAlarmClock() != null)
			{
				String amPmMarker = "";
				SimpleDateFormat sdf;
				boolean is24hFormat = DateFormat.is24HourFormat(context);
				long alarmTriggerTime = am.getNextAlarmClock().getTriggerTime();

				if (is24hFormat)
				{
					sdf = new SimpleDateFormat(context.getResources().getString(R.string.alarm_clock_24h_format));
				}
				else
				{
					sdf = new SimpleDateFormat(context.getResources().getString(R.string.alarm_clock_12h_format));
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(alarmTriggerTime);

					if (cal.get(Calendar.HOUR_OF_DAY) < 12)
					{
						amPmMarker = " " + context.getResources().getString(R.string.time_am_default);
					}
					else
					{
						amPmMarker = " " + context.getResources().getString(R.string.time_pm_default);
					}
				}
				nextAlarm = sdf.format(am.getNextAlarmClock().getTriggerTime()) + amPmMarker;
			}
		}
		else
		{
			nextAlarm = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
		}
		return nextAlarm;
	}

	private void setupBatteryLayout()
	{
		int batteryLevel = FairphoneClockData.getBatteryLevel(getContext());
		int batteryStatus = FairphoneClockData.getBatteryStatus(getContext());
		long chargingTime = FairphoneClockData.getBatteryTimeUntilCharged(getContext());
		long remainingTime = FairphoneClockData.getBatteryTimeUntilDischarged(getContext());
		updateBatteryStatusAndLevel(batteryLevel, batteryStatus, remainingTime, chargingTime);
	}

	private void setupPeaceOfMind()
	{
		long pom_current = FairphoneClockData.getPeaceOfMindCurrent(getContext());
		long pom_record = FairphoneClockData.getPeaceOfMindRecord(getContext());
		mPomCurrentText.setText(Long.toString(pom_current));
		mPomRecordText.setText(Long.toString(pom_record));
	}

	private void updateBatteryLevel(int level, boolean isCharging)
	{
		if (level <= 5)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_00 : R.drawable.battery_00);
		}
		else if (level <= 10)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_10 : R.drawable.battery_10);
		}
		else if (level <= 20)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_20 : R.drawable.battery_20);
		}
		else if (level <= 30)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_30 : R.drawable.battery_30);
		}
		else if (level <= 40)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_40 : R.drawable.battery_40);
		}
		else if (level <= 50)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_50 : R.drawable.battery_50);
		}
		else if (level <= 60)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_60 : R.drawable.battery_60);
		}
		else if (level <= 70)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_70 : R.drawable.battery_70);
		}
		else if (level <= 80)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_80 : R.drawable.battery_80);
		}
		else if (level <= 90)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_90 : R.drawable.battery_90);
		}
		else if (level <= 100)
		{
			mBatteryLevelImage.setImageResource(isCharging ? R.drawable.battery_charging_100 : R.drawable.battery_100);
		}
	}

	private void updateBatteryStatusAndLevel(int level, int status, long remainingTime, long chargingTime)
	{
		Resources resources = getResources();
		switch (status)
		{
			case BatteryManager.BATTERY_STATUS_CHARGING:
				updateBatteryLevel(level, true);
				mChargedText.setVisibility(View.GONE);
				mUnplugChargerText.setVisibility(View.GONE);
				mLastLongerButton.setVisibility(View.INVISIBLE);

				getRemainingTime(chargingTime, true);
				break;
			case BatteryManager.BATTERY_STATUS_FULL:
				mBatteryDescriptionText.setText(resources.getString(R.string.battery_is_fully));
				mBatteryLevelImage.setImageResource(R.drawable.battery_charging_100);
				mLastLongerButton.setVisibility(View.INVISIBLE);
				mBatteryTimeGroup.setVisibility(View.GONE);
				mChargedText.setVisibility(View.VISIBLE);
				mUnplugChargerText.setVisibility(View.VISIBLE);
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			case BatteryManager.BATTERY_STATUS_UNKNOWN:
			default:

				updateBatteryLevel(level, false);
				mChargedText.setVisibility(View.GONE);
				mUnplugChargerText.setVisibility(View.GONE);
				mLastLongerButton.setVisibility(View.VISIBLE);

				getRemainingTime(remainingTime, false);
				break;
		}
	}

	private void getRemainingTime(long remainingTime, boolean isCharging)
	{
		Calendar currentTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		endTime.add(Calendar.MILLISECOND, (int) remainingTime);

		if (endTime.get(Calendar.DAY_OF_MONTH) <= currentTime.get(Calendar.DAY_OF_MONTH) + 1)
		{
			mDayIndicator.setVisibility((endTime.get(Calendar.DAY_OF_MONTH) != currentTime.get(Calendar.DAY_OF_MONTH)) ? VISIBLE : INVISIBLE);
			mBatteryDescriptionText.setText(getResources().getString(isCharging ? R.string.battery_will_be_charged_at : R.string.battery_charge_will_last_until));
			mBatteryTimeGroup.setVisibility(View.VISIBLE);
			mBatteryDaysLeft.setVisibility(View.GONE);
			if (DateFormat.is24HourFormat(getContext()))
			{
				mHoursText.setText(String.format("%02d", endTime.get(Calendar.HOUR_OF_DAY)));
				mBatteryAmPmIndicator.setText("");
			}
			else
			{
				mHoursText.setText(String.format("%d", endTime.get(Calendar.HOUR)));
				mBatteryAmPmIndicator.setText(getResources().getString(endTime.get(Calendar.AM_PM) == Calendar.AM ? R.string.time_am_default : R.string.time_pm_default));
			}
			mMinutesText.setText(String.format("%02d", endTime.get(Calendar.MINUTE)));
		}
		else
		{
			mBatteryDescriptionText.setText(getResources().getString(isCharging ? R.string.battery_will_be_charged_in : R.string.battery_charge_will_last));
			long diff = endTime.getTimeInMillis() - currentTime.getTimeInMillis();
			long days = Math.abs(TimeUnit.MILLISECONDS.toDays(diff));
			mBatteryDaysLeft.setText(String.format("%d %s", days, getResources().getString(days == 1 ? R.string.day : R.string.days)));
			mBatteryTimeGroup.setVisibility(View.GONE);
			mBatteryDaysLeft.setVisibility(View.VISIBLE);
		}
	}

	//
//	public static String getBatteryStatusAsString(int status) {
//		String desc = "Unknown: ";
//		switch (status) {
//			case BatteryManager.BATTERY_STATUS_CHARGING:
//				desc = "BATTERY_STATUS_CHARGING";
//				break;
//			case BatteryManager.BATTERY_STATUS_FULL:
//				desc = "BATTERY_STATUS_FULL";
//				break;
//			case BatteryManager.BATTERY_STATUS_DISCHARGING:
//				desc = "BATTERY_STATUS_DISCHARGING";
//				break;
//			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
//				desc = "BATTERY_STATUS_NOT_CHARGING";
//				break;
//			case BatteryManager.BATTERY_STATUS_UNKNOWN:
//				desc = "BATTERY_STATUS_UNKNOWN";
//				break;
//			default:
//				desc += status;
//				break;
//		}
//		return desc;
//	}
//


	private void setYourFairphoneSince()
	{
		Resources resources = getResources();
		Period p = getPeriod();

		if (p.diffYears != 0)
		{
			mElapsedYearsText.setText(String.format("%02d", p.diffYears));
			mYearsText.setText(p.diffYears == 1 ? resources.getString(R.string.year) : resources.getString(R.string.years));
			mElapsedMonthsText.setText(String.format("%02d", p.diffMonths));
			mMonthsText.setText(p.diffMonths == 1 ? resources.getString(R.string.month) : resources.getString(R.string.months));
			mElapsedDaysText.setText(String.format("%02d", p.diffMonthDays));
			mDaysText.setText(p.diffMonthDays == 1 ? resources.getString(R.string.day) : resources.getString(R.string.days));
		}
		else if (p.diffMonths != 0)
		{
			mElapsedYearsText.setText(String.format("%02d", p.diffMonths));
			mYearsText.setText(p.diffMonths == 1 ? resources.getString(R.string.month) : resources.getString(R.string.months));
			mElapsedMonthsText.setText(String.format("%02d", p.diffWeeks));
			mMonthsText.setText(p.diffWeeks == 1 ? resources.getString(R.string.week) : resources.getString(R.string.weeks));
			mElapsedDaysText.setText(String.format("%02d", p.diffMonthWeekDays));
			mDaysText.setText(p.diffMonthWeekDays == 1 ? resources.getString(R.string.day) : resources.getString(R.string.days));
		}
		else
		{
			mElapsedYearsText.setText(String.format("%02d", p.diffWeeks));
			mYearsText.setText(p.diffWeeks == 1 ? resources.getString(R.string.week) : resources.getString(R.string.weeks));
			mElapsedMonthsText.setText(String.format("%02d", p.diffMonthWeekDays));
			mMonthsText.setText(p.diffMonthWeekDays == 1 ? resources.getString(R.string.day) : resources.getString(R.string.days));
			mElapsedDaysText.setText(String.format("%02d", p.diffHours));
			mDaysText.setText(p.diffHours == 1 ? resources.getString(R.string.hour) : resources.getString(R.string.hours));
		}
	}

	private Period getPeriod() {
		Period p = new Period();
		long startTime = FairphoneClockData.getFairphoneSince(getContext());
		if (startTime == 0L)
		{	
			Calendar cal = Calendar.getInstance();
        		cal.set(2015, Calendar.OCTOBER, 26);
			startTime = cal.getTimeInMillis();
			FairphoneClockData.setFairphoneSince(getContext(),startTime);
		}
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis(startTime);
		Calendar now = Calendar.getInstance();

		p.diffHours = now.get(Calendar.HOUR_OF_DAY)-start.get(Calendar.HOUR_OF_DAY);

		p.diffMonthDays = now.get(Calendar.DAY_OF_MONTH)-start.get(Calendar.DAY_OF_MONTH);
		if (p.diffHours < 0) {
			p.diffMonthDays -= 1;
			p.diffHours = 24 + p.diffHours;
		}

		p.diffMonths = now.get(Calendar.MONTH)-start.get(Calendar.MONTH);
		if (p.diffMonthDays < 0){
			p.diffMonths -= 1;
			Calendar c = Calendar.getInstance();
			c.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 0);
			p.diffMonthDays = c.get(Calendar.DAY_OF_MONTH) + p.diffMonthDays;
		}

		p.diffYears = now.get(Calendar.YEAR)-start.get(Calendar.YEAR);
		if (p.diffMonths < 0) {
			p.diffYears -= 1;
			p.diffMonths = 12 + p.diffMonths;
		}

		p.diffWeeks = p.diffMonthDays / 7;
		p.diffMonthWeekDays = p.diffMonthDays % 7;

		return p;
	}

	private static class Period {
		public int diffHours = 0;
		public int diffMonthDays = 0;
		public int diffMonthWeekDays = 0;
		public int diffWeeks = 0;
		public int diffMonths = 0;
		public int diffYears = 0;
// --Commented out by Inspection START (26/08/15 20:58):
//		public String print() {
//			return
//				"diffYears "+diffYears+", "+
//				"diffMonths "+diffMonths+", "+
//				"diffWeeks "+diffWeeks+", "+
//				"diffMonthWeekDays "+diffMonthWeekDays+", "+
//				"diffMonthDays "+diffMonthDays+", "+
//				"diffHours "+diffHours;
//		}
// --Commented out by Inspection STOP (26/08/15 20:58)
	}

	public static void dismissKeyguardOnNextActivity() {
		try {
			WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
		} catch (Exception e) {
			Log.e(TAG, "Error dismissing keyguard", e);
		}
	}
}
