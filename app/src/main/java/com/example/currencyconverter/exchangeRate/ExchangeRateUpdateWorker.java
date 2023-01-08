package com.example.currencyconverter.exchangeRate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.currencyconverter.R;
import com.example.currencyconverter.activities.MainActivity;
import com.google.gson.Gson;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.net.URL;
import java.net.URLConnection;

public class ExchangeRateUpdateWorker extends Worker {
    private SharedPreferences preferences;
    public ExchangeRateUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.preferences = context.getSharedPreferences("applicationPreferences", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public Result doWork() {
        this.updateCurrencies();
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean("currencyUpdated", true);
        editor.commit();
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
}
