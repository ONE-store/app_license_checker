package com.onestore.licensing.sample.kotlin

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.onestore.extern.licensing.AppLicenseChecker
import com.onestore.extern.licensing.LicenseCheckerListener
import com.onestore.licensing.sample.BuildConfig
import com.onestore.licensing.sample.MainActivity
import com.onestore.licensing.sample.R
import kotlinx.android.synthetic.main.activity_main.*

class KotlinMainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private var appLicenseChecker: AppLicenseChecker? = null
    private val BASE64_PUBLIC_KEY = BuildConfig.PUBLIC_KEY
    private val PID = "INSERT YOUR PID"

    private var isFlexiblePolicy = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        check_licensing_flexible.setOnClickListener {
            appLicenseChecker =
                AppLicenseChecker.get(
                    this@KotlinMainActivity,
                    BASE64_PUBLIC_KEY,
                    AppLicenseListener()
                )
            appLicenseChecker?.queryLicense()
            isFlexiblePolicy = true
        }

        check_licensing_strict.setOnClickListener {
            appLicenseChecker =
                AppLicenseChecker.get(
                    this@KotlinMainActivity,
                    BASE64_PUBLIC_KEY,
                    AppLicenseListener()
                )
            appLicenseChecker?.strictQueryLicense()
            isFlexiblePolicy = false
        }
    }

    private inner class AppLicenseListener : LicenseCheckerListener {
        override fun denied() {
            if(isFinishing) {
                return
            }
            deniedDialog()
        }

        override fun granted(license: String?, signature: String?) {
            if(isFinishing) {
                return
            }

            Toast.makeText(this@KotlinMainActivity, "granted!", Toast.LENGTH_LONG).show()
        }

        override fun error(errorCode: Int, errorMessage: String?) {
            if(isFinishing) {
                return
            }

            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this@KotlinMainActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
            handError(errorCode)
        }
    }


    override fun onDestroy() {
        appLicenseChecker?.destroy()
        super.onDestroy()
    }


    private fun handError(errorCode : Int) {
        Log.d(TAG, "error code : $errorCode")
        when(errorCode) {
            in AppLicenseChecker.ResponseCode.ERROR_SERVICE_UNAVAILABLE..AppLicenseChecker.ResponseCode.ERROR_SERVICE_TIMEOUT -> unknownErrorDialog()
            AppLicenseChecker.ResponseCode.ERROR_SERVICE_TIMEOUT -> goSettingForNetwork()
            AppLicenseChecker.ResponseCode.ERROR_USER_LOGIN_CANCELED -> retryLoginDialog()
            AppLicenseChecker.ResponseCode.ERROR_INSTALL_USER_CANCELED -> retryInstall()
            AppLicenseChecker.ResponseCode.ERROR_NOT_FOREGROUND -> retryALC()
            AppLicenseChecker.ResponseCode.RESULT_USER_CANCELED -> retryLoginDialog()
            AppLicenseChecker.ResponseCode.RESULT_SERVICE_UNAVAILABLE -> goSettingForNetwork()
            AppLicenseChecker.ResponseCode.RESULT_ALC_UNAVAILABLE -> { /*download library link : https://github.com/ONE-store/app_license_checker*/ }
            AppLicenseChecker.ResponseCode.RESULT_DEVELOPER_ERROR -> unknownErrorDialog()
            else -> unknownErrorDialog()
        }
    }

    private fun retryALC() {
        if (null == appLicenseChecker) {
            appLicenseChecker =
                AppLicenseChecker.get(
                    this@KotlinMainActivity,
                    BASE64_PUBLIC_KEY,
                    AppLicenseListener()
                )
        }

        when (isFlexiblePolicy) {
            true -> appLicenseChecker?.queryLicense()
            false -> appLicenseChecker?.strictQueryLicense()
        }
    }

    private fun showDialog(message : String,
                           positiveText : String,
                           negativeText : String,
                           positiveClickListener : DialogInterface.OnClickListener,
                           negativeClickListener : DialogInterface.OnClickListener) {

        AlertDialog.Builder(this@KotlinMainActivity).apply {
            setMessage(message)
            setPositiveButton(positiveText, positiveClickListener)
            setNegativeButton(negativeText, negativeClickListener)
            setCancelable(false)
        }.show()
    }

    private fun deniedDialog() {
        showDialog(getString(R.string.does_not_exist),
                "ok",
                "finish",
            { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_market_detail_url)+PID))
                startActivity(intent)
            },
            { _, _ ->
                finish()
            })

    }

    private fun goSettingForNetwork() {
        showDialog(getString(R.string.move_network_setting_screen),
                "setting",
                "finish",
            { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                startActivityForResult(intent, 0)
            },
            { _, _ ->
                finish()
            })
    }

    private fun retryLoginDialog() {
        showDialog(getString(R.string.required_onestore_login),
                "retry",
                "finish",
            { _, _ ->
                retryALC()
            },
            { _, _ ->
                finish()
            })
    }

    private fun retryInstall() {
        showDialog(getString(R.string.required_onestore_service_install),
                "ok",
                "finish",
            { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.download_onestore_service_url)))
                startActivity(intent)
            },
            { _, _ ->
                finish()
            })
    }

    private fun unknownErrorDialog() {
        showDialog(getString(R.string.unknown_error),
                "retry",
                "finish",
            { _, _ ->
                retryALC()
            },
            { _, _ ->
                finish()
            })
    }
}
