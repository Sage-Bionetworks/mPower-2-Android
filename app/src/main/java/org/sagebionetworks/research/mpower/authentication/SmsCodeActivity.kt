package org.sagebionetworks.research.mpower.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.common.base.Strings
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_external_id_sign_in.progressBar
import kotlinx.android.synthetic.main.activity_phone_sign_up.submit_button
import kotlinx.android.synthetic.main.activity_sms_code.container
import kotlinx.android.synthetic.main.activity_sms_code.edit_button
import kotlinx.android.synthetic.main.activity_sms_code.phone_number
import kotlinx.android.synthetic.main.activity_sms_code.resend_link_button
import kotlinx.android.synthetic.main.activity_sms_code.sms_code_text_input
import org.sagebionetworks.bridge.android.manager.AuthenticationManager
import org.sagebionetworks.bridge.researchstack.ApiUtils
import org.sagebionetworks.bridge.rest.exceptions.ConsentRequiredException
import org.sagebionetworks.bridge.rest.exceptions.InvalidEntityException
import org.sagebionetworks.bridge.rest.model.Phone
import org.sagebionetworks.bridge.rest.model.SignUp
import org.sagebionetworks.research.mpower.EntryActivity
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.R.id
import org.sagebionetworks.research.mpower.R.layout
import org.sagebionetworks.research.mpower.WebConsentFragment
import org.sagebionetworks.researchstack.backbone.DataResponse
import org.slf4j.LoggerFactory
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

open class SmsCodeActivity : AppCompatActivity() {

    companion object {
        private val TAG = SmsCodeActivity::class.java.simpleName
        private val PHONE_EXTRA_KEY = "PHONE_EXTRA_KEY"
        fun create(context: Context, phoneNumber: String): Intent {
            val intent = Intent(context, SmsCodeActivity::class.java)
            intent.putExtra(PHONE_EXTRA_KEY, phoneNumber)
            return intent
        }
    }

    var smsCodeViewModel: SmsCodeViewModel? = null

    @JvmField
    @Inject
    var factory: SmsCodeViewModel.Factory? = null
    
    var consentFragment: WebConsentFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_sms_code)

        val smsCodeFactory = factory?.create() ?: run {
            Log.e(TAG, "Failed to create Phone sign up view model factory")
            return
        }

        val viewModel =
                ViewModelProvider(this, smsCodeFactory)
                        .get(SmsCodeViewModel::class.java)

        intent.getStringExtra(PHONE_EXTRA_KEY)?.let {
            viewModel.phoneNumber = it
        }

        viewModel.getIsSignedUpLiveData().observe(this, Observer { isSignedUp: Boolean ->
            if (isSignedUp) {
                returnToEntryActivity()
            }
        })

        viewModel.getIsSignedUpLiveData().observe(this, Observer { isSignedUp: Boolean ->
            if (isSignedUp) {
                progressBar?.visibility = View.INVISIBLE
            }
        })
        viewModel.isLoadingLiveData.observe(this, Observer { isLoading: Boolean? ->
            progressBar?.visibility = if (isLoading == true) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        })
        viewModel.errorMessageLiveData.observe(this, Observer { errorMessage: String? ->
            onErrorMessage(errorMessage)
        })

        submit_button.isEnabled = false
        submit_button.alpha = 0.33f
        viewModel.isSmsCodeValidLiveData.observe(this, Observer { isValid: Boolean? ->
            submit_button.isEnabled = isValid == true
            submit_button.alpha = if (isValid == true) {
                1.0f
            } else {
                0.33f
            }
        })

        submit_button.setOnClickListener {
            viewModel.smsCodeSignIn(this)
        }

        resend_link_button.setOnClickListener {
            viewModel.resendLink(this)
        }

        viewModel.resendLinkEnabledLiveData.observe(this, Observer { isEnabled: Boolean? ->
            resend_link_button.isEnabled = isEnabled ?: false
            resend_link_button.alpha = if (isEnabled == true) {
                1.0f
            } else {
                0.33f
            }
        })

        edit_button.setOnClickListener {
            this.finish() // go back
            overridePendingTransition(R.anim.exit, R.anim.enter)
        }

        phone_number.text = viewModel.phoneNumber ?: ""

        sms_code_text_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(smsCode: Editable?) {
                smsCodeViewModel?.smsCode = smsCode?.toString() ?: ""
            }
        })

        viewModel.isConsentRequired.observe(this, Observer { isConsentRequired: Boolean? ->
            if (isConsentRequired == true) {
                container.visibility = View.VISIBLE
                var fragment: Fragment? = consentFragment
                if (fragment == null) {
                    consentFragment = WebConsentFragment.newInstance()
                    fragment = consentFragment
                }
                fragment?.let {
                    val manager = supportFragmentManager
                    val transaction = manager.beginTransaction()
                    transaction.add(id.container, it, "WebConsentFragment")
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            } else {
                container.visibility = View.GONE
            }
        })

        smsCodeViewModel = viewModel
    }

    fun onErrorMessage(errorMessage: String?) {
        if (Strings.isNullOrEmpty(errorMessage)) {
            return
        }
        AlertDialog.Builder(this)
                .setTitle(R.string.consent_error_title)
                .setNeutralButton(R.string.rsb_ok, null)
                .create().show()
    }

    fun returnToEntryActivity() {
        val intent = Intent(this, EntryActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}

public class SmsCodeViewModel @MainThread constructor(
        val authenticationManager: AuthenticationManager) : ViewModel() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SmsCodeViewModel::class.java)
        private val smsCodeLength = 6
    }

    public class Factory @Inject constructor(val authenticationManager: AuthenticationManager) {
        public fun create(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SmsCodeViewModel::class.java)) {
                        return SmsCodeViewModel(authenticationManager) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

    private val compositeSubscription = CompositeSubscription()
    private val errorMessageMutableLiveData: MutableLiveData<String?> = MutableLiveData()
    private val isSmsCodeValid: MutableLiveData<Boolean> = MutableLiveData()
    private val isLoadingMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val isSignedInLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val isConsentRequiredExceptionData: MutableLiveData<Boolean> = MutableLiveData()
    private val resendLinkEnabledData: MutableLiveData<Boolean> = MutableLiveData()

    val errorMessageLiveData: LiveData<String?>
        get() = errorMessageMutableLiveData
    val isSmsCodeValidLiveData: LiveData<Boolean>
        get() = this.isSmsCodeValid
    val isLoadingLiveData: LiveData<Boolean>
        get() = isLoadingMutableLiveData
    val isConsentRequired: LiveData<Boolean>
        get() = isConsentRequiredExceptionData
    val resendLinkEnabledLiveData: LiveData<Boolean>
        get() = resendLinkEnabledData

    var smsCode = ""
        set(value) {
            field = value
            this.isSmsCodeValid.postValue(value.isNotEmpty())
        }

    fun getIsSignedUpLiveData(): LiveData<Boolean> {
        return isSignedInLiveData
    }

    var phoneNumber: String? = null

    /**
     * Use the smsCode value to sign in
     */
    fun smsCodeSignIn(context: Context) {

        LOGGER.debug("smsCodeSignIn $smsCode")
        if (Strings.isNullOrEmpty(smsCode) || smsCode.length < smsCodeLength) {
            LOGGER.warn("Cannot sign in with null or empty SMS code")
            isSignedInLiveData.postValue(false)
            errorMessageMutableLiveData.postValue("Cannot sign in with null or empty SMS code")
            return
        }

        val number = phoneNumber ?: run {
            LOGGER.warn("Cannot sign in with null or empty past phone number")
            isSignedInLiveData.postValue(false)
            errorMessageMutableLiveData.postValue("Cannot sign in with null or empty past phone number")
            return
        }

        val regionCode = PhoneSignUpViewModel.phoneRegion(context)
        val hyphenlessToken = smsCode.replace("-", "")
        val token = hyphenlessToken.substring(0 until 3) + "-" +
                hyphenlessToken.substring(3 until 6)

        compositeSubscription.add(
                authenticationManager.signInViaPhoneLink(regionCode, number, token)
                        .doOnSubscribe {
                            isLoadingMutableLiveData.postValue(true)
                        }
                        .doAfterTerminate {
                            isLoadingMutableLiveData.postValue(false)
                        }
                        .subscribe({
                            isSignedInLiveData.postValue(true)
                        }) { error: Throwable ->
                            isSignedInLiveData.postValue(false)
                            if (error is ConsentRequiredException) {
                                isConsentRequiredExceptionData.postValue(true)
                            } else {
                                errorMessageMutableLiveData.postValue(error.message)
                            }
                        })
    }

    fun resendLink(context: Context) {
        LOGGER.debug("resendLink")

        val phoneErrorMsg = context.getString(R.string.mp_phone_error)

        val number = phoneNumber ?: run {
            LOGGER.warn("Cannot sign in with null or empty past phone number")
            isSignedInLiveData.postValue(false)
            errorMessageMutableLiveData.postValue("Cannot sign in with null or empty past phone number")
            return
        }

        val regionCode = PhoneSignUpViewModel.phoneRegion(context)
        val phone = Phone().regionCode(regionCode).number(number)
        val signUp = SignUp().phone(phone)

        resendLinkEnabledData.postValue(false)
        Handler().postDelayed({
            resendLinkEnabledData.postValue(true)
        }, 15000) // 15 second limiter on this network event

        compositeSubscription.add(
                authenticationManager.signUp(signUp)
                        .andThen(ApiUtils.SUCCESS_DATA_RESPONSE)
                        .doOnSubscribe {
                            isLoadingMutableLiveData.postValue(true)
                        }
                        .doAfterTerminate {
                            isLoadingMutableLiveData.postValue(false)
                        }
                        .subscribe({ response: DataResponse? ->
                            LOGGER.debug("Resend link success")
                        }) { error: Throwable ->
                            // 400 is the response for an invalid phone number
                            if (error is InvalidEntityException) {
                                errorMessageMutableLiveData.postValue(phoneErrorMsg)
                                return@subscribe
                            }
                            isSignedInLiveData.postValue(false)
                            errorMessageMutableLiveData.postValue(error.message)
                        })
    }

    override fun onCleared() {
        compositeSubscription.unsubscribe()
    }

    fun onErrorMessageConsumed() {
        errorMessageMutableLiveData.postValue(null)
    }
}