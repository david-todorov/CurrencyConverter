package com.example.currencyconverter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.Gson;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.net.URL;
import java.net.URLConnection;

public class ExchangeRateUpdateWorker extends Worker {

    public ExchangeRateUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {
        this.updateCurrencies();
        this.sendNotification();
        return Result.success();
    }

    private void updateCurrencies(){
        Gson gsonParser = new Gson();

        String currencyDatabaseString = getInputData().getString("currencyDatabase");

        ExchangeRateDatabase currencyDatabase  = gsonParser.fromJson(currencyDatabaseString, ExchangeRateDatabase.class);
        try {
            URL u = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
            URLConnection connection = u.openConnection();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(connection.getInputStream(), connection.getContentEncoding());

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT){

                if(eventType == XmlPullParser.START_TAG){
                    if(parser.getAttributeCount()==2){
                        if("Cube".equals(parser.getName())){
                            String currency = parser.getAttributeValue(null, "currency");
                            double exchangeRate = Double.parseDouble(parser.getAttributeValue(null, "rate"));
                            currencyDatabase.setExchangeRate(currency, exchangeRate);
                        }

                    }
                }
                eventType = parser.next();
            }

        }
        catch (Exception e){

        }
    }

    void sendNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1",
                    "android",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("WorkManger");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "1")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle("Updated Currencies")
                .setContentText("Everything fresh...")
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

}
