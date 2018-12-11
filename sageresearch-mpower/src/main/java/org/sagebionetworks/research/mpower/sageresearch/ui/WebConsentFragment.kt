/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
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

package org.sagebionetworks.research.mpower

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Bitmap
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.support.annotation.AnyThread
import android.support.annotation.RequiresApi
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.web_consent_fragment.consent_webview
import kotlinx.android.synthetic.main.web_consent_fragment.view.consent_webview
import org.json.JSONObject
import org.sagebionetworks.bridge.android.access.BridgeAccessState
import org.sagebionetworks.bridge.android.access.BridgeAccessViewModel
import org.sagebionetworks.bridge.android.access.Resource
import org.sagebionetworks.bridge.android.access.Resource.Status
import org.sagebionetworks.bridge.rest.model.SharingScope
import org.sagebionetworks.research.mpower.sageresearch.R
import org.slf4j.LoggerFactory
import javax.inject.Inject

class WebConsentFragment : DaggerFragment() {
    private val LOGGER = LoggerFactory.getLogger(WebConsentFragment::class.java)

    companion object {
        fun newInstance() = WebConsentFragment()
    }

    @Inject
    lateinit var bridgeAccessViewModelFactory: BridgeAccessViewModel.Factory

    private lateinit var bridgeAccessViewModel: BridgeAccessViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

        // same scope as BridgeAccessFragment to get same instance
        bridgeAccessViewModel = ViewModelProviders.of(requireActivity(), bridgeAccessViewModelFactory)
                .get(BridgeAccessViewModel::class.java)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val fragmentLayout = inflater.inflate(R.layout.web_consent_fragment, container, false)
        fragmentLayout.consent_webview.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onWebViewLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onWebViewLoadingFinished()
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                onWebViewReceiveError(failingUrl, errorCode, description)
            }

            @RequiresApi(VERSION_CODES.LOLLIPOP)
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
                onWebViewReceiveError(request?.url.toString(), errorResponse?.statusCode ?: 0,
                        errorResponse?.reasonPhrase)
            }
        }

        fragmentLayout.consent_webview.settings.javaScriptEnabled = true
        fragmentLayout.consent_webview.addJavascriptInterface(this, "AndroidJsBridge")

        val consentUrl = resources.getString(R.string.web_consent_url)
        LOGGER.debug("Using consent url {}", consentUrl)
        fragmentLayout.consent_webview.loadUrl(consentUrl)

        return fragmentLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bridgeAccessViewModel.bridgeAccessStatus.observe(this, Observer { onConsentUploadState(it) })
    }

    @JavascriptInterface
    @AnyThread
    @Suppress("unused")
    fun consentsToResearch(jsonString: String) {
        val jsonObject = JSONObject(jsonString)
        bridgeAccessViewModel.consentsToResearch(
                jsonObject.getString("name"),
                SharingScope.fromValue(jsonObject.getString("scope")))
    }

    fun onWebViewLoading() {
        consent_webview?.visibility = INVISIBLE
    }

    fun onWebViewLoadingFinished() {
        consent_webview?.visibility = VISIBLE
    }

    fun onWebViewReceiveError(url: String?, errorCode: Int, description: String?) {
        LOGGER.warn("WebView received error: {} with code: {} for request: {}", description, errorCode, url)
    }

    fun onConsentLoading() {
    }

    fun onConsentError() {
    }

    fun onConsentUploadState(state: Resource<BridgeAccessState>?) {
        // TODO: handle states, check BridgeAccessState value, determine how to split consent POST-to-Bridge
        // in-progress and error handling responsibility 
        state?.let {
            when {
                it.status == Status.LOADING -> {

                }
                it.status == Status.ERROR -> {

                }
                it.status == Status.SUCCESS -> {

                }
            }
        }
    }
}

