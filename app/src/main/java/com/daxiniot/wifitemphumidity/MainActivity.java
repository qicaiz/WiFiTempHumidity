package com.daxiniot.wifitemphumidity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /**
     * 图表控件
     */
    private LineChart mLineChart;

    /**
     * 图表数据
     */
    private LineData mLineData;

    /**
     * 温度上限
     */
    private String mTempHigh = "32";
    /**
     * 温度下限
     */
    private String mTempLow = "18";
    /**
     * 湿度上限
     */
    private String mHumidityHigh = "92";
    /**
     * 湿度下限
     */
    private String mHumidityLow = "70";
    /**
     * 温度显示控件
     */
    private TextView mTemperatureTv;
    /**
     * 湿度显示控件
     */
    private TextView mHumidityTv;
    /**
     * 温度告警控件
     */
    private TextView mTemperatureAlert;
    /**
     * 湿度告警控件
     */
    private TextView mHumidityAlert;
    /**
     * 温度告警范围
     */
    private TextView mTemperatureRangeTv;
    /**
     * 湿度告警范围
     */
    private TextView mHumidityRangeTv;
    /**
     * 温度数据
     */
    private LineDataSet mTemperatureDataSet;
    /**
     * 湿度数据
     */
    private LineDataSet mHumidityDataSet;
    /**
     * 图表显示8个数据
     */
    private int xIndex = 8;
    /**
     * 接收温度数据的ServerSocket
     */
    private ServerSocket serverSocket;
    /**
     * Socket客户端
     */
    private Socket client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //实例化控件
        mTemperatureTv = findViewById(R.id.tv_temperature);
        mHumidityTv = findViewById(R.id.tv_humidity);
        mTemperatureAlert = findViewById(R.id.tv_temperature_alert);
        mHumidityAlert = findViewById(R.id.tv_humidity_alert);
        mTemperatureRangeTv = findViewById(R.id.tv_temperature_range);
        mHumidityRangeTv = findViewById(R.id.tv_humidity_range);
        mTemperatureRangeTv.setText("(" + mTempLow + " - " + mTempHigh + "℃)");
        mHumidityRangeTv.setText("(" + mHumidityLow + " - " + mHumidityHigh + "%)");
        mLineChart = findViewById(R.id.chart);
        //初始化图表属性
        initChart();
        //开启温度显示线程
        new ReceiveDataThread().start();
    }

    /**
     * 初始化图表属性
     */
    private void initChart() {
        //设置X轴属性
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        //设置Y轴属性
        YAxis yAxisLeft = mLineChart.getAxisLeft();
        yAxisLeft.setTextColor(Color.WHITE);
        YAxis yAxisRight = mLineChart.getAxisRight();
        yAxisRight.setTextColor(Color.WHITE);
        //初始化温度数据
        ArrayList<Entry> temperatureList = new ArrayList<>();
        temperatureList.add(new Entry(0, 0));
        //初始化湿度数据
        ArrayList<Entry> humidityList = new ArrayList<>();
        humidityList.add(new Entry(0, 0));
        //设置温度曲线属性
        mTemperatureDataSet = new LineDataSet(temperatureList, "温度");
        mTemperatureDataSet.setLineWidth(1.75f); // 线宽
        mTemperatureDataSet.setCircleRadius(2f);// 显示的圆形大小
        mTemperatureDataSet.setColor(Color.rgb(89, 194, 230));// 折线显示颜色
        mTemperatureDataSet.setCircleColor(Color.rgb(89, 194, 230));// 圆形折点的颜色
        mTemperatureDataSet.setHighLightColor(Color.GREEN); // 高亮的线的颜色
        mTemperatureDataSet.setHighlightEnabled(true);
        mTemperatureDataSet.setValueTextColor(Color.rgb(89, 194, 230)); //数值显示的颜色
        mTemperatureDataSet.setValueTextSize(8f);     //数值显示的大小
        //设置湿度曲线属性
        mHumidityDataSet = new LineDataSet(humidityList, "湿度");
        mHumidityDataSet.setLineWidth(1.75f);
        mHumidityDataSet.setCircleRadius(2f);
        mHumidityDataSet.setColor(Color.rgb(252, 76, 122));
        mHumidityDataSet.setCircleColor(Color.rgb(252, 76, 122));
        mHumidityDataSet.setHighLightColor(Color.GREEN);
        mHumidityDataSet.setHighlightEnabled(true);
        mHumidityDataSet.setValueTextColor(Color.rgb(252, 76, 122));
        mHumidityDataSet.setValueTextSize(8f);
        //构建一个类型为LineDataSet的ArrayList 用来存放所有 y的LineDataSet
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        //将数据加入dataSets
        dataSets.add(mTemperatureDataSet);
        dataSets.add(mHumidityDataSet);
        //构建一个LineData  将dataSets放入
        mLineData = new LineData(dataSets);
        mLineChart.setData(mLineData);
        mLineChart.invalidate();
    }

    /**
     * 温湿度数据接收线程
     */
    private class ReceiveDataThread extends Thread {
        private DataInputStream in;
        private byte[] receive;

        @Override
        public void run() {
            try {
                //在手机端建立一个ServerSocket，负责接收ESP8266发送的数据，端口为5000
                serverSocket = new ServerSocket(5000);
                client = serverSocket.accept();
                while (true) {
                    //循环读取数据
                    in = new DataInputStream(client.getInputStream());
                    receive = new byte[5];
                    in.read(receive);
                    String data = new String(receive);
                    //刷新UI
                    doUIRrefresh(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接收温湿度数据后刷新UI
     *
     * @param data 温湿度数据
     */
    private void doUIRrefresh(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //将字符转换成16进制的字符串,共5个字节 xx xx xx xx xx
                String hexData = str2HexStr(data);
                //湿度整数部分,第一个字节
                String humidityTemp = hexData.substring(0, 2);
                //温度整数部分，第三个字节
                String temperatureTemp = hexData.substring(4, 6);
                //将16进制转换成10进制
                int humidity = Character.digit(humidityTemp.charAt(0), 16) * 16
                        + Character.digit(humidityTemp.charAt(1), 16);
                int temperature = Character.digit(temperatureTemp.charAt(0), 16) * 16
                        + Character.digit(temperatureTemp.charAt(1), 16);
                mTemperatureTv.setText(temperature + "℃");
                mHumidityTv.setText(humidity + "%");
                //判断温度是否超出告警阈值
                doCheckTemperatureAlert(temperature);
                //判断湿度是否超出告警阈值
                doCheckHumidityAlert(humidity);
                //刷新图表
                int entryCount = mTemperatureDataSet.getEntryCount();
                if (entryCount < 8) {
                    ++entryCount;
                    int tempEntryCount = entryCount;
                    mTemperatureDataSet.addEntry(new Entry(tempEntryCount, temperature));
                    mHumidityDataSet.addEntry(new Entry(tempEntryCount, humidity));
                } else {
                    ++xIndex;
                    int tempIndex = xIndex;
                    mTemperatureDataSet.addEntry(new Entry(tempIndex, temperature));
                    mTemperatureDataSet.removeFirst();
                    mHumidityDataSet.addEntry(new Entry(tempIndex, humidity));
                    mHumidityDataSet.removeFirst();
                }
                mLineData.notifyDataChanged();
                mLineChart.notifyDataSetChanged();
                mLineChart.invalidate();
            }
        });
    }

    /**
     * 检查温度告警
     *
     * @param temperature
     */
    private void doCheckTemperatureAlert(int temperature) {
        //判断温度是否超出告警阈值
        if (temperature > Integer.valueOf(mTempHigh)) {
            mTemperatureAlert.setText("温度过高");
            mTemperatureAlert.setTextColor(Color.RED);
        } else if (temperature < Integer.valueOf(mTempLow)) {
            mTemperatureAlert.setText("温度偏低");
            mTemperatureAlert.setTextColor(Color.RED);
        } else {
            mTemperatureAlert.setText("温度正常");
            mTemperatureAlert.setTextColor(Color.GREEN);
        }
    }

    /**
     * 检查湿度告警
     *
     * @param humidity
     */
    private void doCheckHumidityAlert(int humidity) {
        if (humidity > Integer.valueOf(mHumidityHigh)) {
            mHumidityAlert.setText("湿度过高");
            mHumidityAlert.setTextColor(Color.RED);
        } else if (humidity < Integer.valueOf(mHumidityLow)) {
            mHumidityAlert.setText("湿度偏低");
            mHumidityAlert.setTextColor(Color.RED);
        } else {
            mHumidityAlert.setText("湿度正常");
            mHumidityAlert.setTextColor(Color.GREEN);
        }
    }

    /**
     * 获取从告警设置页面返回的值
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mTempHigh = data.getStringExtra(Constants.TEMP_HIGH_SETTING);
            mTempLow = data.getStringExtra(Constants.TEMP_LOW_SETTING);
            mHumidityHigh = data.getStringExtra(Constants.HUMIDITY_HIGH_SETTING);
            mHumidityLow = data.getStringExtra(Constants.HUMIDITY_LOW_SETTING);
            mTemperatureRangeTv.setText("(" + mTempLow + " - " + mTempHigh + "℃)");
            mHumidityRangeTv.setText("(" + mHumidityLow + " - " + mHumidityHigh + "%)");
        }
    }

    /**
     * 创建菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单点击事件回调方法
     *
     * @param item 被点击的菜单项
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.alert_settings) {
            Intent intent = new Intent(MainActivity.this, AlertSettingActivity.class);
            intent.putExtra(Constants.TEMP_HIGH_SETTING, mTempHigh);
            intent.putExtra(Constants.TEMP_LOW_SETTING, mTempLow);
            intent.putExtra(Constants.HUMIDITY_HIGH_SETTING, mHumidityHigh);
            intent.putExtra(Constants.HUMIDITY_LOW_SETTING, mHumidityLow);
            startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE);
        }
        return true;
    }

    /**
     * 将字符串转换成16进制字符串
     *
     * @param origin
     * @return
     */
    private static String str2HexStr(String origin) {
        byte[] bytes = origin.getBytes();
        return bytesToHexString(bytes);
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
    }


}