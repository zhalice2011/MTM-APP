package com.ruiqi.mtm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ruiqi.mtm.ecgview.ECG_allData_View;
import com.ruiqi.mtm.ecgview.StringToAscii;
import com.ruiqi.mtm.ecgview.WH_ECGView;

import java.util.ArrayList;

//import android.view.View;

/**
 * Created by zhalice on 2017/12/6.
 */

public class EcgView extends AppCompatActivity
{
    //心电图
    private WH_ECGView ecgView;
    private ECG_allData_View allData_view;
    private ArrayList<String> data_source;
    private TextView txt_result;
    private TextView txt_bpm;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_view);
        Bundle bundle = this.getIntent().getExtras();
        String ECGdata = bundle.getString("ECGdata");
        String bpm = bundle.getString("bpm");
        String results = bundle.getString("results");
        txt_bpm = (TextView)findViewById(R.id.ecgviewbpm);
        txt_result = (TextView)findViewById(R.id.ecgviewresult);
        txt_bpm.setText("心率:\n"+bpm);
        txt_result.setText("解读:\n"+results);
        ecgView = (WH_ECGView)findViewById(R.id.ecg_data_ecgView);
        allData_view = (ECG_allData_View)findViewById(R.id.allData_ecgView);
        data_source = StringToAscii.test(ECGdata);
        ecgView.setData(data_source);
        allData_view.setData(data_source);
//        // 获取应用程序中的previous按钮
//        Button previous = (Button) findViewById(R.id.previous);
//        // 获取应用程序中的close按钮
//        Button close = (Button) findViewById(R.id.close);
//        // 为previous按钮绑定事件监听器
//        previous.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View source)
//            {
//                // 获取启动当前Activity的上一个Intent
//                Intent intent = new Intent(EcgView.this,
//                        MainActivity.class);
//                // 启动intent对应的Activity
//                startActivity(intent);
//            }
//        });

    }
}