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
import com.onestore.app.licensing.Enumeration;
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

    private void deniedDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("구매내역이 존재하지 않습니다.\n원스토어에서 구매하시겠습니까?");
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://m.onestore.co.kr/mobilepoc/apps/appsDetail.omp?prodId="+PID));
                startActivity(marketIntent);
            }
        });

        alertDialog.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 취소
            }
        });

        alertDialog.setCancelable(false);
        alertDialog.show();
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

            if (2000 == errorCode && 2100 > errorCode ) {
                // TODO: 원스토어에 문의해주세요.
            } else if (2100 == errorCode) {
                // TODO: 네트워크 상태를 체크해주세요.
            } else if (2101 == errorCode) {
                // TODO: 원스토어 로그인을 취소하였습니다.
            } else if (2102 == errorCode) {
                // TODO: 원스토어 서비스 설치중입니다.
            } else if (2103 == errorCode) {
                // TODO: 원스토어를 설치해주세요.
            } else if (2104 == errorCode) {
                // TODO: 백그라운드 서비스에서는 진행할 수 없습니다.
            } else if (2 == errorCode) {
                // TODO: 네트워크 상태를 체크해주세요.
            } else if (3 == errorCode) {
                // TODO: 라이브러리를 최신버전으로 업데이트 해주세요.
            } else if (5 == errorCode) {
                // TODO: 원스토어에 문의해주세요.
            } else {
                // TODO: 원스토어에 문의해주세요.
            }
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

        super.onDestroy();
    }
}
