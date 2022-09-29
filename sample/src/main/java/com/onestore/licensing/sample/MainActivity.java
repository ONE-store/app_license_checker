package com.onestore.licensing.sample;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.onestore.extern.licensing.AppLicenseChecker;
import com.onestore.extern.licensing.LicenseCheckerListener;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private AppLicenseChecker appLicenseChecker;
    private static final String BASE64_PUBLIC_KEY = BuildConfig.PUBLIC_KEY;
    private static final String PID = "INSERT YOUR PID";

    private boolean isFlexiblePolicy = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button flexible = (Button)findViewById(R.id.check_licensing_flexible);
        Button strict = (Button)findViewById(R.id.check_licensing_strict);
        Button myService = (Button)findViewById(R.id.my_service);

        flexible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // License Verify Flexible
                appLicenseChecker = AppLicenseChecker.get(MainActivity.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
                appLicenseChecker.queryLicense();
                isFlexiblePolicy = true;
            }
        });
        strict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // License Verify Strict
                appLicenseChecker = AppLicenseChecker.get(MainActivity.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
                appLicenseChecker.strictQueryLicense();
                isFlexiblePolicy = false;
            }
        });


        myService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyService.class);
                startService(intent);
            }
        });
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(null != intent) {
            int errorCode = intent.getIntExtra("errorCode",-1);
            if (0 < errorCode ) {
                handleError(errorCode);
            }
        }
    }

    private class AppLicenseListener implements LicenseCheckerListener {
        @Override
        public void granted(String license, String signature) {
            if (isFinishing()) {
                return;
            }

            Toast.makeText(MainActivity.this,"granted!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void denied() {
            Log.d(TAG, "denied");
            if (isFinishing()) {
                return;
            }

            deniedDialog();
        }

        @Override
        public void error(int errorCode, String error) {
            Log.d(TAG, "error: " + errorCode);
            if (isFinishing()) {
                return;
            }

            if(null != error && !error.isEmpty()) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            }

            handleError(errorCode);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (null != appLicenseChecker)
            appLicenseChecker.destroy();

        if(true == isMyServiceRunning(MyService.class)) {
            Intent intent = new Intent(MainActivity.this, MyService.class);
            stopService(intent);
        }

        super.onDestroy();
    }

    private void handleError(int errorCode) {
        Log.d(TAG, "error code : " + errorCode);
        if (AppLicenseChecker.ResponseCode.ERROR_SERVICE_UNAVAILABLE <= errorCode && AppLicenseChecker.ResponseCode.ERROR_SERVICE_TIMEOUT > errorCode ) {
            unknownErrorDialog();
        } else if (AppLicenseChecker.ResponseCode.ERROR_SERVICE_TIMEOUT == errorCode) {
            goSettingForNetwork();
        } else if (AppLicenseChecker.ResponseCode.ERROR_USER_LOGIN_CANCELED == errorCode) {
            retryLoginDialog();
        } else if (AppLicenseChecker.ResponseCode.ERROR_INSTALL_USER_CANCELED == errorCode) {
            retryInstall();
        } else if (AppLicenseChecker.ResponseCode.ERROR_NOT_FOREGROUND == errorCode) {
            retryALC();
        } else if (AppLicenseChecker.ResponseCode.RESULT_USER_CANCELED == errorCode) {
            retryLoginDialog();
        }  else if (AppLicenseChecker.ResponseCode.RESULT_SERVICE_UNAVAILABLE == errorCode) {
            goSettingForNetwork();
        } else if (AppLicenseChecker.ResponseCode.RESULT_ALC_UNAVAILABLE == errorCode) {
            // download library link : https://github.com/ONE-store/app_license_checker
        } else if (AppLicenseChecker.ResponseCode.RESULT_DEVELOPER_ERROR == errorCode) {
            unknownErrorDialog();
        } else {
            unknownErrorDialog();
        }
    }

    private void retryALC() {
        if(null == appLicenseChecker) {
            appLicenseChecker = AppLicenseChecker.get(MainActivity.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
        }

        if ((true == isFlexiblePolicy)) {
            appLicenseChecker.queryLicense();
        } else {
            appLicenseChecker.strictQueryLicense();
        }
    }


    private void showDialog(String message,
                            String positiveText,
                            String negativeText,
                            DialogInterface.OnClickListener positiveClickListener,
                            DialogInterface.OnClickListener negativeClickListener) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(positiveText, positiveClickListener);
        alertDialog.setNegativeButton(negativeText, negativeClickListener);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }


    private void deniedDialog() {
        showDialog(getString(R.string.does_not_exist),
                "ok",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Log.d(TAG, getString(R.string.app_market_detail_url) + PID);
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                getString(R.string.app_market_detail_url) + PID));
                        startActivity(marketIntent);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

    }

    private void goSettingForNetwork() {
        showDialog(getString(R.string.move_network_setting_screen),
                "setting",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        startActivityForResult(intent, 0);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }


    private void retryLoginDialog() {
        showDialog(getString(R.string.required_onestore_login),
                "retry",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        retryALC();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    private void retryInstall() {
        showDialog(getString(R.string.required_onestore_service_install),
                "ok",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.download_onestore_service_url)));
                        startActivity(intent);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    private void unknownErrorDialog() {
        showDialog(getString(R.string.unknown_error),
                "retry",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        retryALC();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
