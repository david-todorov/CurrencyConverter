package com.example.currencyconverter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.currencyconverter.adaptors.CurrencyListAdapter;
import com.example.currencyconverter.exchangeRate.ExchangeRateDatabase;
import com.example.currencyconverter.R;

import org.parceler.Parcels;

public class CurrencyListActivity extends AppCompatActivity {

    private CurrencyListAdapter adapter;
    private ExchangeRateDatabase currencies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        this.currencies = Parcels.unwrap(getIntent().getParcelableExtra("ExchangeRateDatabase"));

        this.adapter = new CurrencyListAdapter(CurrencyListActivity.this, currencies);

        ListView listView = findViewById(R.id.currency_list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String currency = (String) adapter.getItem(i);
                String capital = currencies.getCapital(currency);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q="+capital));
                startActivity(intent);
            }
        });
    }
}