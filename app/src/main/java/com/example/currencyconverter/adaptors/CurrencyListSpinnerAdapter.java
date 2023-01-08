package com.example.currencyconverter.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.currencyconverter.exchangeRate.ExchangeRateDatabase;
import com.example.currencyconverter.R;

public class CurrencyListSpinnerAdapter extends BaseAdapter {


    private Context context;
    private ExchangeRateDatabase currencies;
    LayoutInflater inflater;

    public CurrencyListSpinnerAdapter(Context context, ExchangeRateDatabase currencies){
        this.context = context;
        this.currencies = currencies;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return this.currencies.getCurrencies().length;
    }

    @Override
    public Object getItem(int i) {

        return this.currencies.getCurrencies()[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {


        if(view == null){
            view = this.inflater.inflate(R.layout.list_currency_view_item_spinner, null);

        }

        TextView names = view.findViewById(R.id.text_view_in_view_item);
        names.setText(this.currencies.getCurrencies()[i]);

        String flagName = "flag_" + this.currencies.getCurrencies()[i].toLowerCase();
        int imageId = this.context.getResources().getIdentifier(flagName,"drawable",this.context.getPackageName());
        ImageView flag = view.findViewById(R.id.image_view_in_view_item);
        flag.setImageResource(imageId);

        return view;
    }
}
