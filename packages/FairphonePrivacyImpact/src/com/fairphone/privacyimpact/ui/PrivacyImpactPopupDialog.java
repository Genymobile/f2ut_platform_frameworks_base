package com.fairphone.privacyimpact.ui;

import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.fairphone.privacyimpact.R;
import com.fairphone.privacyimpact.adapters.PermissionListFiller;
import com.fairphone.privacyimpact.appdata.PrivacyImpactCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrivacyImpactPopupDialog extends DialogFragment implements OnEditorActionListener {

    private PrivacyImpactCalculator.PRIVACY_LEVEL mPrivacyLevel;
    private List<String> mPermissionList;

    public PrivacyImpactPopupDialog() {
        // Empty constructor required for DialogFragment
        super();
    }

    public void setPrivacyLevel(PrivacyImpactCalculator.PRIVACY_LEVEL level) {
        mPrivacyLevel = level;
    }

    public void setPackageInfoAndPermissionList(PackageInfo packageInfo, List<String> permissionList) {
        PackageInfo mPackageInfo = packageInfo;
        mPermissionList = new ArrayList<>();
        mPermissionList.addAll(permissionList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.privacy_impact_popup, container);

        Resources resources=getActivity().getResources();
        TextView privacyText = (TextView) view.findViewById(R.id.privacy_impact_text);
        privacyText.setTextColor(resources.getColor(PrivacyImpactCalculator.getPrivacyColor(mPrivacyLevel)));
        TextView privacyLevelText = (TextView) view.findViewById(R.id.privacy_impact_level_text);
        privacyLevelText.setText(PrivacyImpactCalculator.getPrivacyName(getActivity(), mPrivacyLevel));
        privacyLevelText.setTextColor(resources.getColor(PrivacyImpactCalculator.getPrivacyColor(mPrivacyLevel)));

        TextView link = (TextView) view.findViewById(R.id.privacy_impact_info_link);
        link.setText(setupLink(resources));
        link.setMovementMethod(LinkMovementMethod.getInstance());

        View noPermissionsGroup = view.findViewById(R.id.no_permissions_group);
        View permissionsGroup = view.findViewById(R.id.permissions_group);
        if (mPermissionList.isEmpty()) {
            permissionsGroup.setVisibility(View.GONE);
            noPermissionsGroup.setVisibility(View.VISIBLE);
        } else {
            permissionsGroup.setVisibility(View.VISIBLE);
            noPermissionsGroup.setVisibility(View.GONE);

            TextView levelDescription = (TextView) view.findViewById(R.id.privacy_level_description);
            levelDescription.setText(PrivacyImpactCalculator.getPrivacyDescription(getActivity(), mPrivacyLevel));

            LinearLayout permissionsListView = (LinearLayout) view.findViewById(R.id.permissions_list_view);

            PermissionListFiller.fillLayout(getActivity(), permissionsListView, mPermissionList);
        }


        TextView mOkButton = (TextView) view.findViewById(R.id.confirmation_yes_button);

        mOkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PrivacyImpactPopupDialog.this.dismiss();
            }
        });

        return view;
    }

    private Spanned setupLink(Resources resources) {
        StringBuilder link = new StringBuilder();
        link.append("<a href=\"");
        link.append(resources.getString(R.string.privacy_impact_info_link));
        link.append("\">");
        link.append(resources.getString(R.string.privacy_impact_info_link_text));
        link.append("</a>");
        return Html.fromHtml(link.toString());
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            this.dismiss();
            return true;
        }
        return false;
    }

}
