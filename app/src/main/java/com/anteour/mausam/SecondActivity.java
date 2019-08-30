package com.anteour.mausam;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SecondActivity extends AppCompatActivity {
    private final static String AHMADABAD = "Ahmadabad";
    private final static String MUMBAI = "Navi Mumbai";
    private final static String QUERY = "QUERY";
    private final static int MIN_TEMP = 0;
    private final static int MAX_TEMP = 1;
    private static int mStartCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        MainActivity.dao = DatabaseManager.getInstance(getApplicationContext()).getCityTempDao();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mStartCode = getIntent().getIntExtra(QUERY, MAX_TEMP);
        initGraph();
    }

    void initGraph() {
        LineChart l = findViewById(R.id.line_chart);
        ProgressBar p = findViewById(R.id.progress_bar);

        final List<String> mTimeStamp = new ArrayList<>();
        List<DayTemp> mListA = MainActivity.dao.findByCity(AHMADABAD);
        List<DayTemp> mListM = MainActivity.dao.findByCity(MUMBAI);
        List<Entry> mEntriesA = loadFromDatabase(mListA, null);
        List<Entry> mEntriesM = loadFromDatabase(mListM, mTimeStamp);

        LineDataSet mDataSetA = new LineDataSet(mEntriesA, AHMADABAD);
        mDataSetA.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
        mDataSetA.setValueTextSize(12);
        mDataSetA.setLineWidth(2);
        mDataSetA.setCircleColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));

        LineDataSet mDataSetM = new LineDataSet(mEntriesM, MUMBAI);
        mDataSetM.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlue));
        mDataSetM.setValueTextSize(12);
        mDataSetM.setLineWidth(2);
        mDataSetM.setCircleColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
        LineData data = new LineData(mDataSetA, mDataSetM);

        l.setData(data);
        l.setPinchZoom(true);
        l.setDoubleTapToZoomEnabled(false);

        p.setVisibility(View.GONE);
        l.setVisibility(View.VISIBLE);

        XAxis x = l.getXAxis();
        //Getting Labels for x Axis
        x.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mTimeStamp.get((int) value);
            }
        });
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelRotationAngle(-30);
        x.setTextSize(12);

        //Getting Labels for y Axis
        l.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return new DecimalFormat("#.00").format(value) + " °C";
            }
        });

        Legend legend = l.getLegend();
        legend.setFormSize(10f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
        legend.setTextSize(12f);
        legend.setXEntrySpace(5f);
        legend.setYEntrySpace(5f);

        l.setDescription(null);
        l.getAxisLeft().setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        l.getAxisLeft().setTextSize(12);
        l.getAxisRight().setEnabled(false);
        l.animateX(1000, Easing.Linear);
    }

    public List<Entry> loadFromDatabase(List<DayTemp> mList, List<String> mTimeStamp) {
        List<Entry> list = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            DayTemp d = mList.get(i);
            if (mStartCode == MAX_TEMP) {
                Entry entry = new Entry(i, d.getTemp_max());
                list.add(entry);
            } else if (mStartCode == MIN_TEMP) {
                Entry entry = new Entry(i, d.getTemp_min());
                list.add(entry);
            }
            if (mTimeStamp != null)
                try {
                    @SuppressLint("SimpleDateFormat") String s = new SimpleDateFormat("dd MMM").format(
                            new SimpleDateFormat("yyyy-MM-dd").parse(d.getTimeStamp()));
                    mTimeStamp.add(s);
                } catch (ParseException ignored) {
                }
        }
        return list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
