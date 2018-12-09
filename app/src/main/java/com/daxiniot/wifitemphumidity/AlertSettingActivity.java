package com.daxiniot.wifitemphumidity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AlertSettingActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 取消按钮
     */
    private Button mCancelBtn;
    /**
     * 保存按钮
     */
    private Button mSaveBtn;
    /**
     * 温度上限
     */
    private EditText mTempHigh;
    /**
     * 温度下限
     */
    private EditText mTempLow;
    /**
     * 湿度上限
     */
    private EditText mHumidityHigh;
    /**
     * 湿度下限
     */
    private EditText mHumidityLow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_setting);
        //显示返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //获取从MainActivity传递过来的告警值，用于显示
        Intent intent = getIntent();
        String tempHigh = "";
        String tempLow = "";
        String humidityHigh = "";
        String humidityLow = "";
        if (intent != null) {
            tempHigh = intent.getStringExtra(Constants.TEMP_HIGH_SETTING);
            tempLow = intent.getStringExtra(Constants.TEMP_LOW_SETTING);
            humidityHigh = intent.getStringExtra(Constants.HUMIDITY_HIGH_SETTING);
            humidityLow = intent.getStringExtra(Constants.HUMIDITY_LOW_SETTING);
        }
        //控件初始化
        mCancelBtn = (Button) findViewById(R.id.btn_cancel);
        mSaveBtn = (Button) findViewById(R.id.btn_save);
        mTempHigh = (EditText) findViewById(R.id.et_temp_high);
        mTempLow = (EditText) findViewById(R.id.et_temp_low);
        mHumidityHigh = (EditText) findViewById(R.id.et_humidity_high);
        mHumidityLow = (EditText) findViewById(R.id.et_humidity_low);
        mTempHigh.setText(tempHigh);
        mTempLow.setText(tempLow);
        mHumidityHigh.setText(humidityHigh);
        mHumidityLow.setText(humidityLow);
        mCancelBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //取消按钮
            case R.id.btn_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            //保存按钮
            case R.id.btn_save:
                Intent settingIntent = new Intent();
                String tempHigh = mTempHigh.getText().toString();
                String tempLow = mTempLow.getText().toString();
                String humidityHigh = mHumidityHigh.getText().toString();
                String humidityLow = mHumidityLow.getText().toString();
                //数值设置合规判断
                if (TextUtils.isEmpty(tempHigh) || TextUtils.isEmpty(tempLow)
                        || TextUtils.isEmpty(humidityHigh) || TextUtils.isEmpty(humidityLow)) {
                    Toast.makeText(AlertSettingActivity.this, "告警数值不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    settingIntent.putExtra(Constants.TEMP_HIGH_SETTING, tempHigh);
                    settingIntent.putExtra(Constants.TEMP_LOW_SETTING, tempLow);
                    settingIntent.putExtra(Constants.HUMIDITY_HIGH_SETTING, humidityHigh);
                    settingIntent.putExtra(Constants.HUMIDITY_LOW_SETTING, humidityLow);
                    setResult(RESULT_OK, settingIntent);
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return true;
    }
}
