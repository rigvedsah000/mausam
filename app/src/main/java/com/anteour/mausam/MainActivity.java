package com.anteour.mausam;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Callback<JSONObject> {
    private final static String AHMADABAD = "Ahmadabad";
    private final static String MUMBAI = "Navi Mumbai";
    private final static String QUERY = "QUERY";
    private final static int MIN_TEMP = 0;
    private final static int MAX_TEMP = 1;
    private int flag = 0;
    public static DayTempDao dao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Pass Application Context instead of Activity to prevent Memory Leak since Instance of DM is static.
        // Other activities can use this instance of dao.
        dao = DatabaseManager.getInstance(getApplicationContext()).getCityTempDao();
        initButtons();
        callWeatherAPI();
    }

    @Override
    public void onClick(View view) {
        Intent i = new Intent(MainActivity.this, SecondActivity.class);
        i.putExtra(QUERY, (Integer) view.getTag());
        startActivity(i);
    }

    @SuppressLint("SimpleDateFormat")
    void initGraph() {
        LineChart l = findViewById(R.id.line_chart);
        ProgressBar p = findViewById(R.id.progress_bar);

        //This will store date of 5 days from today inclusive
        final List<String> mTimeStamp = new ArrayList<>();

        List<DayTemp> mListA = dao.findByCity(AHMADABAD);
        List<DayTemp> mListM = dao.findByCity(MUMBAI);

        //Since mTimeStamp array contents is common to both. We need ony one instance
        List<Entry> mEntriesA = loadFromDatabase(mListA, null);
        List<Entry> mEntriesM = loadFromDatabase(mListM, mTimeStamp);

        LineDataSet mDataSetA = new LineDataSet(mEntriesA, AHMADABAD);
        mDataSetA.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
        mDataSetA.setValueTextSize(12);
        mDataSetA.setLineWidth(2);
        mDataSetA.setCircleColor(ContextCompat.getColor(getApplicationContext(),R.color.colorBlack));
        LineDataSet mDataSetM = new LineDataSet(mEntriesM, MUMBAI);
        mDataSetM.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlue));
        mDataSetM.setValueTextSize(12);
        mDataSetM.setLineWidth(2);
        mDataSetM.setCircleColor(ContextCompat.getColor(getApplicationContext(),R.color.colorBlack));
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

        //Legend is circle appearing at top right with color and label
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

    void initButtons() {
        Button minTemp = findViewById(R.id.button_min_temp);
        Button maxTemp = findViewById(R.id.button_max_temp);
        minTemp.setTag(MIN_TEMP);
        maxTemp.setTag(MAX_TEMP);
        //When no data in database we cannot go to Second Activity
        minTemp.setEnabled(false);
        maxTemp.setEnabled(false);
        minTemp.setOnClickListener(this);
        maxTemp.setOnClickListener(this);
    }

    void callWeatherAPI() {
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.button_retry).setVisibility(View.GONE);
        Retrofit.Builder b = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit r = b.build();
        TemperatureService t = r.create(TemperatureService.class);
        //Metric refers to Celcius Unit
        Call<JSONObject> mCallAhmadabad = t.getTemperatureData(1279233, "d7e22bd4c31a43d6ab31579590973f1c", "metric");
        Call<JSONObject> mCallMumbai = t.getTemperatureData(6619347, "d7e22bd4c31a43d6ab31579590973f1c", "metric");
        mCallAhmadabad.enqueue(this);
        mCallMumbai.enqueue(this);
    }

    @Override
    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
        JSONObject to = response.body();
        if (to != null) {
            storeInDatabase(to);
            flag++;
            if (flag == 2) {
                initGraph();
                //Database is filled, we can now load Min and Max Temp
                findViewById(R.id.button_min_temp).setEnabled(true);
                findViewById(R.id.button_max_temp).setEnabled(true);
            }
        }
    }

    @Override
    public void onFailure(Call<JSONObject> call, Throwable t) {
        Toast.makeText(this, "Data could not be loaded!", Toast.LENGTH_SHORT).show();
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
        Button retry = findViewById(R.id.button_retry);
        retry.setVisibility(View.VISIBLE);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callWeatherAPI();
            }
        });
    }

    public void storeInDatabase(JSONObject to) {
        List<DayTemp> l = new ArrayList<>();
        //Assign Min Temp = 100 so that it can be compared
        float temp_min = 100.0f;
        //Assign Max Temp = 0 so that it can be compared
        float temp_max = 0.0f;
        String mOldDate = null;
        boolean mOldDateInitialized = false;
        List<JSONObject.ListObject> list = to.getList();
        DecimalFormat f = new DecimalFormat("#.00");

        /*There are instances of ListObject with time gap of 3 hrs. where date is same (i.e. day is same and calculations are at diff time).
         We find minimum and maximum temp by comparing among temp of all the instances of that day, which will be the Min and Max of that day.
         Now we calculate average from temp_min and temp_max and store in DayTemp Object.

         Working:
         We store the old date in mOldDate and if instances have same date we compare to find Min & Max.
         If now instances with new date comes, we calculate the average, store it in DayTemp Object and store DayTemp Object in array.
         Then we reset the temp_min,temp_max to start comparing instances of current new Date.
         Repeating this process till all days are checked!
         Finally deleting previous Database contents and storing current ones.
         */
        for (int i = 0; i < list.size(); i++) {
            JSONObject.ListObject lo = list.get(i);
            if (!mOldDateInitialized) {
                mOldDate = lo.getDt_txt();
                mOldDateInitialized = true;
            } else {
                try {

                    @SuppressLint("SimpleDateFormat") Date oldD = new SimpleDateFormat("yyyy-MM-dd").parse(mOldDate);
                    @SuppressLint("SimpleDateFormat") Date newD = new SimpleDateFormat("yyyy-MM-dd").parse(lo.getDt_txt());
                    //Comparing day
                    if (newD.after(oldD)) {
                        float temp_avg = Float.parseFloat(f.format((temp_max + temp_min) / 2));
                        l.add(new DayTemp(temp_avg, temp_max, temp_min, mOldDate, to.getCity().getName()));
                        mOldDate = lo.getDt_txt();
                        temp_min = 100.0f;
                        temp_max = 0.0f;
                    }
                } catch (ParseException ignored) {
                }
            }
            //Comparing Max and Min temp for a day
            float min = lo.getMain().getTemp_min();
            temp_min = (min < temp_min) ? min : temp_min;
            float max = lo.getMain().getTemp_max();
            temp_max = (max > temp_max) ? max : temp_max;
        }
        if (list.size() > 0) {
            float temp_avg = Float.parseFloat(f.format((temp_max + temp_min) / 2));
            l.add(new DayTemp(temp_avg, temp_max, temp_min, mOldDate, to.getCity().getName()));
        }
        dao.deleteAllforCity(to.getCity().getName());
        DayTemp[] ll = new DayTemp[l.size()];
        dao.insertAllForCity(l.toArray(ll));
    }

    public List<Entry> loadFromDatabase(List<DayTemp> mList, List<String> mTimeStamp) {
        List<Entry> list = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            DayTemp d = mList.get(i);
            Entry entry = new Entry(i, d.getTemp());
            list.add(entry);
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

    interface TemperatureService {
        @GET("data/2.5/forecast")
        Call<JSONObject> getTemperatureData(@Query("id") int cityID, @Query("appid") String appId, @Query("units") String metric);
    }

}
