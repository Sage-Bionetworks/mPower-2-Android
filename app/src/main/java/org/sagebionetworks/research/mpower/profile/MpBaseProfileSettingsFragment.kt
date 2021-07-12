/*
 * BSD 3-Clause License
 *
 * Copyright 2021  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.mpower.profile

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_profilesettings_list.settings_icon
import kotlinx.android.synthetic.main.fragment_profilesettings_list.spinner
import kotlinx.android.synthetic.main.fragment_profilesettings_list.view.back_icon
import kotlinx.android.synthetic.main.fragment_profilesettings_list.view.list
import kotlinx.android.synthetic.main.fragment_profilesettings_list.view.settings_icon
import kotlinx.android.synthetic.main.fragment_profilesettings_list.view.textView
import org.sagebionetworks.bridge.rest.model.SurveyReference
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper
import org.sagebionetworks.research.sageresearch.profile.EditProfileItemDialogFragment
import org.sagebionetworks.research.sageresearch.profile.EditProfileItemDialogListener
import org.sagebionetworks.research.sageresearch.profile.OnListInteractionListener
import org.sagebionetworks.research.sageresearch.profile.ProfileSettingsRecyclerViewAdapter
import org.sagebionetworks.research.sageresearch.profile.ProfileSettingsRecyclerViewAdapter.Companion.VIEW_TYPE_SECTION
import org.sagebionetworks.research.sageresearch_app_sdk.R

abstract class MpBaseProfileSettingsFragment : OnListInteractionListener, EditProfileItemDialogListener, androidx.fragment.app.Fragment()  {

    private var profileKey = "ProfileDataSource" //Initialized to the default key
    private var isMainView = true;
    var adapter: ProfileSettingsRecyclerViewAdapter? = null

    protected lateinit var profileViewModel: MpBaseProfileViewModel

    override abstract fun launchSurvey(surveyReference: SurveyReference)
    abstract fun newInstance(profileKey: String, isMainView: Boolean): MpBaseProfileSettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            profileKey = it.getString(ARG_PROFILE_KEY) ?: ""
            isMainView = it.getBoolean(ARG_IS_MAIN_VIEW, true)
        }
    }

    abstract fun loadProfileViewModel(): MpBaseProfileViewModel

    fun showLoading(show: Boolean) {
        Handler(Looper.getMainLooper()).post {spinner?.visibility = if (show) View.VISIBLE else View.GONE }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        profileViewModel = loadProfileViewModel()

        val view = inflater.inflate(R.layout.fragment_profilesettings_list, container, false)

        if (!isMainView) {
            view.back_icon.visibility = View.VISIBLE
            view.textView.visibility = View.INVISIBLE
            view.settings_icon.visibility = View.GONE
            view.back_icon.setOnClickListener {
                activity?.onBackPressed()
            }
        }

        val topListener = SystemWindowHelper.getOnApplyWindowInsetsListener(SystemWindowHelper.Direction.TOP)
        ViewCompat.setOnApplyWindowInsetsListener(view.textView, topListener)


        return view
    }

    override fun launchEditProfileItemDialog(value: String, profileItemKey: String) {
        val dialogFragment = EditProfileItemDialogFragment.newInstance(value, profileItemKey, this)
        dialogFragment.show(requireFragmentManager(), "EditDialog")
    }

    override  fun saveEditDialogValue(value: String, profileItemKey: String) {
        profileViewModel.saveStudyParticipantValue(value, profileItemKey)
        adapter?.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings_icon.setOnClickListener {
            val settingsFragment = newInstance("SettingsDataSource", false)
            addChildFragmentOnTop(settingsFragment, "settingsFragment")
        }

        showLoading(true)
        // Set the adapter
        if (view.list is androidx.recyclerview.widget.RecyclerView) {
            with(view.list) {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

                val divider = object : androidx.recyclerview.widget.DividerItemDecoration(this.getContext(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL) {

                    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
                        val pos = getChildAdapterPosition(view)
                        if (parent.adapter?.getItemViewType(pos) == VIEW_TYPE_SECTION) {
                            if (pos == 0) {
                                outRect.set(0, 0, 0, 0)
                            } else {
                                outRect.set(0, 50, 0, 0)
                            }
                        } else {
                            super.getItemOffsets(outRect, view, parent, state)
                        }
                    }

                }
                val drawable = requireContext().resources.getDrawable(R.drawable.form_step_divider)
                divider.setDrawable(drawable)
                this.addItemDecoration(divider)

            }
            if (adapter == null) {
                profileViewModel.profileData(profileKey).observe(viewLifecycleOwner, Observer { loader ->
                    if (adapter == null) {
                        adapter = ProfileSettingsRecyclerViewAdapter(loader, this)
                        view.list.adapter = adapter
                    } else {
                        adapter?.updateDataLoader(loader)
                    }
                    showLoading(false)
                })
            } else {
                view.list.adapter = adapter
                showLoading(false)
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    /**
     * Adds a child fragment on top of this fragment and adds this fragment to the back stack with the provided tag.
     * @param childFragment The fragment to add on top of this fragment.
     * @param tag The tag for this fragment on the back stack.
     */
    fun addChildFragmentOnTop(childFragment: androidx.fragment.app.Fragment, tag: String?) {
        requireFragmentManager()
                .beginTransaction()
                .detach(this)
                .add((this.requireView().parent as ViewGroup).id, childFragment)
                .addToBackStack(null)
                .commit()
    }

    companion object {
        const val ARG_PROFILE_KEY = "profile_key"
        const val ARG_IS_MAIN_VIEW = "is_main_view"
    }
}
