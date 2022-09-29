package com.onestore.licensing.sample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.onestore.extern.licensing.AppLicenseChecker;
import com.onestore.extern.licensing.LicenseCheckerListener;

import java.util.UUID;

public class MyService extends Service {

    private String TAG = MyService.class.getSimpleName();
    private AppLicenseChecker appLicenseChecker;
    private static final String BASE64_PUBLIC_KEY = BuildConfig.PUBLIC_KEY;
    private static final String PID = "INSERT YOUR PID";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        appLicenseChecker = AppLicenseChecker.get(MyService.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
        appLicenseChecker.queryLicense();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (null != appLicenseChecker)
            appLicenseChecker.destroy();

        super.onDestroy();
    }

    private void setPendingIntent(int errorCode) {
        int notiId = UUID.randomUUID().hashCode();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("errorCode", errorCode);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder ;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel = nm.getNotificationChannel("notification");

            if(null == channel)
                channel = new NotificationChannel("notification","notification", NotificationManager.IMPORTANCE_DEFAULT);

            nm.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this,channel.getId());
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        int resId = R.drawable.ic_stat_notify;
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resId = R.drawable.ic_stat_notify_l;
        }

        builder.setContentTitle("알림")
                .setContentText("알림을 터치하여 foreground 에서 실행해주세요."+"("+errorCode+")")
                .setSmallIcon(resId)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle("알림").bigText("알림을 터치하여 foreground 에서 실행해주세요."+"("+errorCode+")"))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), resId))
                .setContentIntent(contentIntent) .setAutoCancel(true) .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);


        nm.notify(notiId, builder.build());
    }

    private class AppLicenseListener implements LicenseCheckerListener {
        @Override
        public void granted(String license, String signature) {
            Log.d(TAG, "granted!");
        }

        @Override
        public void denied() {
            Log.d(TAG, "denied!");
        }

        @Override
        public void error(int errorCode, String error) {

            if(null != error && !error.isEmpty()) {
                Log.d(TAG, "errorCode = "+ errorCode + "error = " + error);
            }

            setPendingIntent(errorCode);
        }
    }
}
