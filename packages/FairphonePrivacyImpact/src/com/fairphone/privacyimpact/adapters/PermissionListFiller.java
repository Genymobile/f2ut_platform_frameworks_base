package com.fairphone.privacyimpact.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fairphone.privacyimpact.R;

import java.util.List;

/**
 * Created by fp2builder on 27-04-2015.
 */
public class PermissionListFiller {

    private static final String TAG = PermissionListFiller.class.getSimpleName();

    public static void fillLayout(Context context, LinearLayout listContainer, List<String> permissionsList) {
        Resources resources = context.getResources();
        PackageManager packageManager = context.getPackageManager();
        if (listContainer != null) {
            listContainer.removeAllViews();
            for (String permission : permissionsList) {

                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.activity_grant_access_list_item, listContainer, false);

                LinearLayout permissionGroup = (LinearLayout) v.findViewById(R.id.permission_group);
                permissionGroup.setOnClickListener(new View.OnClickListener() {
                                                      private Context lContext;
                                                      public View.OnClickListener setup(Context context){
                                                          lContext = context;
                                                          return this;
                                                      }
                                                       @Override
                                                       public void onClick(View v) {
                                                           ImageView hideRevealButton = (ImageView) v.findViewById(R.id.reveal_description_arrow);
                                                           TextView name = (TextView) v.findViewById(R.id.name);
                                                           TextView detailDescription = (TextView) v.findViewById(R.id.details_text);
                                                           boolean isOpen = detailDescription.getVisibility() == View.VISIBLE;
                                                           Resources resource = lContext.getResources();
                                                           if (isOpen) {
                                                               if (hideRevealButton != null) {
                                                                   hideRevealButton.setImageDrawable(resource.getDrawable(R.drawable.icon_reveal_arrow_down));
                                                               }
                                                               name.setSingleLine(true);
                                                               detailDescription.setVisibility(View.GONE);
                                                           } else {
                                                               if (hideRevealButton != null) {
                                                                   hideRevealButton.setImageDrawable(resource.getDrawable(R.drawable.icon_reveal_arrow_up));
                                                               }
                                                               name.setSingleLine(false);
                                                               detailDescription.setVisibility(View.VISIBLE);
                                                           }
                                                       }
                                                   }.setup(context)
                );

                ImageView icon = (ImageView) v.findViewById(R.id.icon);
                TextView name = (TextView) v.findViewById(R.id.name);
                TextView details = (TextView) v.findViewById(R.id.details_text);
                ImageView revealDescriptionArrow = (ImageView) v.findViewById(R.id.reveal_description_arrow);

                //reset state
                if (revealDescriptionArrow != null) {
                    revealDescriptionArrow.setImageDrawable(resources.getDrawable(R.drawable.icon_reveal_arrow_down));
                }
                details.setVisibility(View.GONE);
                boolean addView = false;

                if (!TextUtils.isEmpty(permission)) {

                    try {
                        PermissionInfo info = packageManager.getPermissionInfo(permission, PackageManager.GET_META_DATA);

                        String group = info.group;

                        if (!TextUtils.isEmpty(group)) {
                            PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(group, 0);
                            icon.setImageDrawable(permissionGroupInfo.loadIcon(packageManager));
                        } else {
                            icon.setImageResource(R.drawable.icon_generic_permission);
                        }
                        String cap = capitalizeSentence(info.loadLabel(packageManager));
                        name.setText(cap);
                        name.setSingleLine(true);

                        CharSequence detailedDescription = info.loadDescription(packageManager);
                        if (!TextUtils.isEmpty(detailedDescription)) {
                            details.setText(detailedDescription);
                        } else {
                            details.setText(R.string.no_description_available);
                        }

                        addView = true;
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w(TAG, "Permission name not found: " + e.getLocalizedMessage());

                    }
                    if (addView) {
                        listContainer.addView(v);
                    }
                } else {
                    Log.w(TAG, "Permission is null.");
                }
            }
        }
    }

    private static String capitalizeSentence(CharSequence sequence) {
        StringBuilder sb = new StringBuilder(sequence);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }
}
