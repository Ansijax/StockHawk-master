package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by massimo on 21/05/17.
 */

public class WidgetRemoteViewService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                Log.d("widget","create");
            }

            @Override
            public void onDataSetChanged() {
                if(data!=null){
                    data.close();
                }


                final long idToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, Contract.Quote.COLUMN_SYMBOL);

                Binder.restoreCallingIdentity(idToken);
            }

            @Override
            public void onDestroy() {

                if(data!=null)
                    data.close();
                data=null;
            }

            @Override
            public int getCount() {
                if(data==null)
                    return 0 ;
                else
                    return data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views =new RemoteViews(getPackageName(), R.layout.widget_list_item);
                String symbol = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
                Float value = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PRICE));
                Float percentageChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));
                //Float rawAbsoluteChange = data.getFloat(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));
                final DecimalFormat percentageFormat;
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                final DecimalFormat dollarFormat;
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

                if (percentageChange > 0) {
                    views.setInt(R.id.widget_diff_tv,"setBackgroundResource",R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.widget_diff_tv,"setBackgroundResource",R.drawable.percent_change_pill_red);
                }
                String percentage = percentageFormat.format(percentageChange / 100);

                views.setTextViewText(R.id.widget_symbol_tv,symbol);
                String a11y_symbol= getString(R.string.a11y_stock_name,symbol);
                views.setContentDescription(R.id.widget_symbol_tv,a11y_symbol);

                views.setTextViewText(R.id.widget_value_tv, dollarFormat.format(value));
                String a11y_value= getString(R.string.a11y_stock_value,dollarFormat.format(value));
                views.setContentDescription(R.id.widget_symbol_tv,a11y_value);

                views.setTextViewText(R.id.widget_diff_tv,percentage);
                views.setTextViewText(R.id.widget_diff_tv, percentage);
                String a11y_change= getString(R.string.a11y_stock_diff,percentage);
                views.setContentDescription(R.id.widget_symbol_tv,a11y_change);

                Intent fillIntent = new Intent();
                fillIntent.putExtra(getString(R.string.symbol_detail_stock),symbol);
                views.setOnClickFillInIntent(R.id.widget_row,fillIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(data.getColumnIndex(Contract.Quote._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };

    }
}
