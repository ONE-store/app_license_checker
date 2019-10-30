package com.onestore.licensing.sample;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.onestore.app.licensing.AppLicenseChecker;
import com.onestore.app.licensing.LicenseCheckerListener;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private AppLicenseChecker appLicenseChecker;
    private static final String BASE64_PUBLIC_KEY = "INSERT YOUR PUBLIC_KEY";
    private static final String PID = "INSERT YOUR PID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button flexible = (Button)findViewById(R.id.check_licensing_flexible);
        Button strict = (Button)findViewById(R.id.check_licensing_strict);

        flexible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // License Verify Flexible
                appLicenseChecker = new AppLicenseChecker(MainActivity.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
                appLicenseChecker.queryLicense();
            }
        });
        strict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // License Verify Strict
                appLicenseChecker = new AppLicenseChecker(MainActivity.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
                appLicenseChecker.strictQueryLicense();
            }
        });
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
            if (isFinishing()) {
                return;
            }

            deniedDialog();
        }

        @Override
        public void error(int errorCode, String error) {
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
    protected void onDestroy() {
        if (null != appLicenseChecker)
            appLicenseChecker.destroy();

        super.onDestroy();
    }

    private void handleError(int errorCode) {
        if (2000 == errorCode && 2100 > errorCode ) {
            unknownErrorDialog();
        } else if (2100 == errorCode) {
            goSettingForNetwork();
        } else if (2101 == errorCode) {
            retryLoginDialog();
        } else if (2102 == errorCode) {
        } else if (2103 == errorCode) {
            retryInstall();
        } else if (2104 == errorCode) {
            retryALC();
        } else if (1 == errorCode) {
            retryLoginDialog();
        }  else if (2 == errorCode) {
            goSettingForNetwork();
        } else if (3 == errorCode) {
            // download library link : https://github.com/ONE-store/app_license_checker
        } else if (5 == errorCode) {
            unknownErrorDialog();
        } else {
            unknownErrorDialog();
        }
    }

    private void retryALC() {
        if(null == appLicenseChecker)
            appLicenseChecker = new AppLicenseChecker(MainActivity.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
        appLicenseChecker.queryLicense();
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
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                getString(R.string.app_market_detail_url)+PID));
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
}
