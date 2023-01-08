package com.example.currencyconverter.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.currencyconverter.adaptors.CurrencyListSpinnerAdapter;
import com.example.currencyconverter.exchangeRate.ExchangeRateDatabase;
import com.example.currencyconverter.exchangeRate.ExchangeRateUpdateRunnable;
import com.example.currencyconverter.exchangeRate.ExchangeRateUpdateWorker;
import com.example.currencyconverter.R;
import com.google.gson.Gson;
import org.parceler.Parcels;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {


    private Spinner fromSpinner;
    private Spinner toSpinner;
    private Button calculateButton;
    private EditText inputValue;
    private ExchangeRateDatabase currencyDatabase;
    private TextView result;
    private CurrencyListSpinnerAdapter spinnerListAdapter;
    private Animation animAlpha;

    private SharedPreferences preferences;
    private final String APP_PREFERENCES = "applicationPreferences";

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.preferences = getSharedPreferences(APP_PREFERENCES,MODE_PRIVATE);



        this.fromSpinner = (Spinner) findViewById(R.id.from_currencies);
        this.toSpinner = (Spinner) findViewById(R.id.to_currencies);
        this.calculateButton = (Button) findViewById(R.id.calculate_btn);
        this.inputValue = (EditText) findViewById(R.id.input_data);
        this.result = (TextView) findViewById(R.id.result);
        this.currencyDatabase = new ExchangeRateDatabase();
        this.spinnerListAdapter = new CurrencyListSpinnerAdapter(MainActivity.this ,this.currencyDatabase);
        this.animAlpha  = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
        this.toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(this.toolbar);
        this.fromSpinner.setAdapter(spinnerListAdapter);
        this.toSpinner.setAdapter(spinnerListAdapter);


        this.calculateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                calculateButtonClicked(v);
                v.startAnimation(animAlpha);
            }
        });

        this.schedulePeriodicUpdating();

        Thread thread = new Thread(new ExchangeRateUpdateRunnable(this));
        thread.start();

    }




    public void calculateButtonClicked(View v){
        if (TextUtils.isEmpty(inputValue.getText().toString()) || inputValue.getText().toString().equals(".")) {
            result.setText("Can not be empty");
        } else {
            String userData = inputValue.getText().toString();

            String itemFromSpinner = fromSpinner.getSelectedItem().toString();
            String itemToSpinner = toSpinner.getSelectedItem().toString();


            double calculated = currencyDatabase.convert(Double.parseDouble(userData), itemFromSpinner, itemToSpinner);
            String resultFormatted = String.format("%.2f", calculated);
            result.setText(resultFormatted);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();


        double inputData = this.preferences.getFloat("UserData",0.00f);
        this.inputValue.setText(Double.toString(inputData));

        int fromSpinner = this.preferences.getInt("FromSpinner",0);
        this.fromSpinner.setSelection(fromSpinner);

        int toSpinner = this.preferences.getInt("ToSpinner",0);
        this.toSpinner.setSelection(toSpinner);

        if(this.preferences.getString("EUR",null) == null){
            for (int i = 0; i<this.currencyDatabase.getCurrencies().length;i++){
                String currencyStr = this.currencyDatabase.getCurrencies()[i];
                double defaultCurrency = this.currencyDatabase.getExchangeRate(currencyStr);
                this.currencyDatabase.setExchangeRate(currencyStr,defaultCurrency);
            }
        }
        else {

        for (int i = 0; i<this.currencyDatabase.getCurrencies().length;i++){
            String currencyStr = this.currencyDatabase.getCurrencies()[i];
            double newCurrency = Double.parseDouble(this.preferences.getString(currencyStr,null));
            this.currencyDatabase.setExchangeRate(currencyStr,newCurrency);
        }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();

        float inputData =  Float.parseFloat(inputValue.getText().toString());
        editor.putFloat("UserData", inputData);

        int itemFromSpinner = fromSpinner.getSelectedItemPosition();
        editor.putInt("FromSpinner",itemFromSpinner);

        int itemToSpinner = toSpinner.getSelectedItemPosition();
        editor.putInt("ToSpinner",itemToSpinner);

        for (int i = 0; i<this.currencyDatabase.getCurrencies().length;i++){
            String currencyStr = this.currencyDatabase.getCurrencies()[i];
            editor.putString(currencyStr, this.currencyDatabase.getExchangeRate(currencyStr)+"");
        }

        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_currency_list:
                startCurrencyListActivity();
                return true;
            case R.id.menu_refresh_rates:
                updateCurrencies();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void schedulePeriodicUpdating() {
        WorkManager workManager = WorkManager.getInstance(this);

        Gson gsonParser = new Gson();

        String currencyDBString = gsonParser.toJson(this.currencyDatabase);

        Data.Builder data = new Data.Builder();
        data.putString("currencyDatabase", currencyDBString);


        PeriodicWorkRequest periodicUpdateRequest =
                new PeriodicWorkRequest.Builder(ExchangeRateUpdateWorker.class, 24,TimeUnit.HOURS)
                        .setInputData(data.build())
                        .addTag("periodicUpdateTag")
                        .build();

        workManager.enqueueUniquePeriodicWork("periodicUpdate",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicUpdateRequest);
    }

    private void updateCurrencies(){
        WorkManager workManager = WorkManager.getInstance(this);
        Data.Builder data = new Data.Builder();
        Gson gsonParser = new Gson();

        String currencyDBString = gsonParser.toJson(this.currencyDatabase);

        data.putString("currencyDatabase", currencyDBString);

        WorkRequest workRequest = new OneTimeWorkRequest.Builder(ExchangeRateUpdateWorker.class).setInputData(data.build()).build();
        workManager.enqueue(workRequest);

    }
    public void startCurrencyListActivity(){
        Intent intent =  new Intent(MainActivity.this, CurrencyListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("ExchangeRateDatabase", Parcels.wrap(this.currencyDatabase));
        intent.putExtras(bundle);
        startActivity(intent);
    }
}