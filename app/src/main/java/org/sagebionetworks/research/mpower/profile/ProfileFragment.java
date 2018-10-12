package org.sagebionetworks.research.mpower.profile;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.researchstack.backbone.ResourceManager;
import org.sagebionetworks.research.mpower.R;
import org.researchstack.backbone.ResourcePathManager;


import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

import org.sagebionetworks.research.mpower.researchstack.framework.MpResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profile;

    private Unbinder unbinder;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileFragment.class);


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
        android.content.Intent intent = org.researchstack.backbone.ui.ViewWebDocumentActivity.newIntentForPath(this.getActivity(),
                getString(org.researchstack.backbone.R.string.rsb_settings_privacy_policy), path);
        startActivity(intent);


    }

}
