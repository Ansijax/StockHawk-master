package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetails extends AppCompatActivity {
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.symbol_tv)
    TextView tv_symbol;
    @BindView(R.id.chart)
    LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String symbol=intent.getStringExtra(getString(R.string.symbol_detail_stock));

        String title = getString(R.string.chart_title,symbol);
        tv_symbol.setText(title);
        String a11y_chart=getString(R.string.a11y_chart,symbol);
        lineChart.setContentDescription(a11y_chart);

        List<Entry> entries = new ArrayList<Entry>();

        String[] columns = new String[]{Contract.Quote.COLUMN_HISTORY};
        Cursor mCursor = getContentResolver().query(
                Contract.Quote.makeUriForStock(symbol),
                columns,
                null,
                null,
                null
        );

        final List<String> date = new ArrayList<String>();
        while (mCursor.moveToNext()) {
            Log.d("cursor", mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY)) + " ");
            String values = mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            String [] splittedValues=values.split("\n");

            for (int i =splittedValues.length-1; i >=0; i--){
                String[] stockAtTime = splittedValues[i].split(",");
                Log.d("values",stockAtTime[1]+ " "+  String.valueOf(splittedValues.length-(i+1)));
                entries.add( new Entry((float) splittedValues.length-(i+1),Float.parseFloat(stockAtTime[1])));
                date.add(getDate(Long.parseLong(stockAtTime[0])));
            }
        }
        mCursor.close();

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                    return date.get((int) value);

            }
        };

        LineDataSet dataSet = new LineDataSet(entries,"");

        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.material_red_700));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity((1f));
        xAxis.setValueFormatter(formatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(90f);
        xAxis.setDrawGridLines(false);


        //hide all description
        lineChart.setContentDescription("");
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        Description description= new Description();
        description.setText("");
        lineChart.setDescription(description);

        dataSet.setColor(getResources().getColor(R.color.colorAccent));

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setExtraBottomOffset(8);


    }


    String getDate(long milliseconds){
        String format =getString(R.string.string_format);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }
}
