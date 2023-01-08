package com.example.currencyconverter.exchangeRate;




import android.app.NotificationChannel;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

import com.example.currencyconverter.R;
import com.example.currencyconverter.activities.MainActivity;




public class ExchangeRateUpdateRunnable implements Runnable{
    private Context context;
    private Boolean currenciesUpdated;
    private final String CHANNEL_ID = "1";
    private SharedPreferences preferences;

    public ExchangeRateUpdateRunnable(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("applicationPreferences",Context.MODE_PRIVATE);
    }


    @Override
    public void run() {

        while (true){
            this.currenciesUpdated = this.preferences.getBoolean("currencyUpdated",false);
            if(currenciesUpdated){
                sendNotification();
                SharedPreferences.Editor editor = this.preferences.edit();
                editor.putBoolean("currencyUpdated", false);
                editor.commit();
            }
        }

    }

    private void sendNotification() {


        NotificationManager mNotificationManager =
                (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1",
                    "android",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("WorkManger");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.context, "1")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle("Updated Currencies")
                .setContentText("Everything fresh...")
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(this.context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    }
