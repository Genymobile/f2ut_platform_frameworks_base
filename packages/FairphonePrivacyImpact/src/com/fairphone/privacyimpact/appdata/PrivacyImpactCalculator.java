package com.fairphone.privacyimpact.appdata;

import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;

import com.fairphone.privacyimpact.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jpascoal on 29/06/2015.
 */
public class PrivacyImpactCalculator {

    public enum PRIVACY_LEVEL {
        NONE,
        LOW,
        MEDIUM,
        HIGH
    }

    private static List<Pair<Integer, List<String>>> permissionsCombinationList;

    public static void setupPrivacyCombinations() {
        if (permissionsCombinationList == null || permissionsCombinationList.isEmpty()) {
            permissionsCombinationList = new ArrayList<>();
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.WRITE_EXTERNAL_STORAGE"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.READ_EXTERNAL_STORAGE"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.READ_PHONE_STATE"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.ACCESS_WIFI_STATE"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.ACCESS_COARSE_LOCATION"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.ACCESS_FINE_LOCATION"})));
            permissionsCombinationList.add(new Pair<>(2, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.GET_ACCOUNTS"})));
            permissionsCombinationList.add(new Pair<>(3, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.CAMERA"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.GET_TASKS"})));
            permissionsCombinationList.add(new Pair<>(4, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.READ_CONTACTS"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.READ_HISTORY_BOOKMARKS"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.READ_CALL_LOG"})));
            permissionsCombinationList.add(new Pair<>(3, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.RECORD_AUDIO"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.READ_LOGS"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.USE_CREDENTIALS"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.SEND_SMS", "android.permission.READ_PHONE_STATE"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.INTERNET", "android.permission.RECEIVE_SMS"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.SEND_SMS", "android.permission.ACCESS_NETWORK_STATE"})));
            permissionsCombinationList.add(new Pair<>(1, Arrays.asList(new String[]{"android.permission.SEND_SMS", "android.permission.WRITE_EXTERNAL_STORAGE"})));
        }
    }

    public static Pair<PRIVACY_LEVEL, Double> calculateLevel(List<String> permissionList) {
        PRIVACY_LEVEL level = PRIVACY_LEVEL.NONE;
        double appScore = 0.0;
        if (permissionList != null && !permissionList.isEmpty()) {
            setupPrivacyCombinations();
            int privacyImpact = 0;
            int privacySum = 0;
            for (Pair<Integer, List<String>> permissionsCombination : permissionsCombinationList) {
                if (permissionList.containsAll(permissionsCombination.second)) {
                    privacyImpact += permissionsCombination.first;
                }
                privacySum += permissionsCombination.first;
            }

            appScore = Math.round(((double) privacyImpact / privacySum) * 100.0) / 100.0;
            if (appScore == 0.0) {
                level = PRIVACY_LEVEL.NONE;
            } else if (appScore < 0.45) {
                level = PRIVACY_LEVEL.LOW;
            } else if (appScore < 0.85) {
                level = PRIVACY_LEVEL.MEDIUM;
            } else {
                level = PRIVACY_LEVEL.HIGH;
            }
        }
        return new Pair<>(level, appScore);
    }

    public static int getPrivacyColor(PRIVACY_LEVEL level) {
        int colorResourceId;
        switch (level) {
            case LOW:
                colorResourceId = R.color.green;
                break;
            case MEDIUM:
                colorResourceId = R.color.orange;
                break;
            case HIGH:
                colorResourceId = R.color.orange_dark;
                break;
            case NONE:
            default:
                colorResourceId = R.color.blue;
                break;
        }
        return colorResourceId;
    }

    public static int getPrivacyBackground(PRIVACY_LEVEL level) {
        int colorResourceId;
        switch (level) {
            case LOW:
                colorResourceId = R.drawable.privacy_low;
                break;
            case MEDIUM:
                colorResourceId = R.drawable.privacy_medium;
                break;
            case HIGH:
                colorResourceId = R.drawable.privacy_high;
                break;
            case NONE:
            default:
                colorResourceId = R.drawable.privacy_none;
                break;
        }
        return colorResourceId;
    }

    public static String getPrivacyName(Context context, PRIVACY_LEVEL level) {
        Resources resources = context.getResources();
        String privacyName;
        switch (level) {
            case LOW:
                privacyName = resources.getString(R.string.privacy_level_low);
                break;
            case MEDIUM:
                privacyName = resources.getString(R.string.privacy_level_medium);
                break;
            case HIGH:
                privacyName = resources.getString(R.string.privacy_level_high);
                break;
            case NONE:
            default:
                privacyName = resources.getString(R.string.privacy_level_none);
                break;
        }
        return privacyName;
    }

    public static String getPrivacyDescription(Context context, PRIVACY_LEVEL level) {
        Resources resources = context.getResources();
        String privacyDescription;
        switch (level) {
            case LOW:
                privacyDescription = resources.getString(R.string.privacy_level_low_description);
                break;
            case MEDIUM:
                privacyDescription = resources.getString(R.string.privacy_level_medium_description);
                break;
            case HIGH:
                privacyDescription = resources.getString(R.string.privacy_level_high_description);
                break;
            case NONE:
            default:
                privacyDescription = resources.getString(R.string.privacy_level_none_description);
                break;
        }
        return privacyDescription;
    }

}
