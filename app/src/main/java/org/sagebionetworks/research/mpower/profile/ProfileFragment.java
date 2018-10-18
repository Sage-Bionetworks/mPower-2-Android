package org.sagebionetworks.research.mpower.profile;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.researchstack.backbone.ResourceManager;
import org.sagebionetworks.research.mpower.R;
import org.researchstack.backbone.ResourcePathManager;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

import org.sagebionetworks.research.mpower.researchstack.framework.MpResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.researchstack.backbone.utils.LogExt;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profile;

    private Unbinder unbinder;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileFragment.class);

    @BindView(R.id.footer_text) TextView footerTextView;


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        profile = ViewModelProviders.of(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel @liujoshua 2018/08/06
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.licenses_button)
    public void onLicensesClicked() {
        LOGGER.debug("Clicked on the licenses button");
        String path =  MpResourceManager.getInstance().getLicense().getAbsolutePath();
        Intent intent = org.researchstack.backbone.ui.ViewWebDocumentActivity.newIntentForPath(this.getActivity(),
                "", path);
        startActivity(intent);


        String current_version = getVersionString();
        footerTextView.setText(current_version);

    }

    public String getVersionString()
    {
        int versionCode;
        String versionName;
        PackageManager manager = getActivity().getPackageManager();

        try
        {
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            versionCode = info.versionCode;
            versionName = info.versionName;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            LogExt.e(getClass(), "Could not find package version info");
            versionCode = 0;
            versionName = getString(R.string.rsb_settings_version_unknown);
        }
        return getString(R.string.rsb_settings_version, versionName, versionCode);
    }

}
