package org.sagebionetworks.research.mpower.profile;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sagebionetworks.bridge.android.BridgeConfig;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.researchstack.framework.MpResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profile;

    private Unbinder unbinder;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileFragment.class);

    @BindView(R.id.footer_text)
    TextView footerTextView;

    @Inject
    BridgeConfig bridgeConfig;

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

        String appVersionString =
                getString(R.string.app_version_label,
                        getString(R.string.app_name),
                        bridgeConfig.getAppVersionName(),
                        bridgeConfig.getAppVersion());

        footerTextView.setText(appVersionString);
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
        String path = MpResourceManager.getInstance().getLicense().getAbsolutePath();
        Intent intent = org.sagebionetworks.researchstack.backbone.ui.ViewWebDocumentActivity.newIntentForPath(this.getActivity(),
                "", path);
        startActivity(intent);
    }
}
