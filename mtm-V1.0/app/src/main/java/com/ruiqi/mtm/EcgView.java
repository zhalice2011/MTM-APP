package com.ruiqi.mtm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
    private String dataSource;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_view);
        /*获取Intent中的Bundle对象*/
        Bundle bundle = this.getIntent().getExtras();
        String name = bundle.getString("Name");
        boolean ismale = bundle.getBoolean("Ismale");

        ecgView = (WH_ECGView)findViewById(R.id.ecg_data_ecgView);
        allData_view = (ECG_allData_View)findViewById(R.id.allData_ecgView);
        dataSource = "[2071, 2086, 2099, 2109, 2117, 2124, 2130, 2134, 2136, 2138, 2139, 2141, 2143, 2145, 2147, 2148, 2148, 2148, 2147, 2145, 2141, 2134, 2125, 2116, 2107, 2098, 2088, 2078, 2067, 2058, 2049, 1997, 1991, 1991, 1997, 2058, 2168, 2221, 2221, 2144, 2003, 1937, 1937, 1954, 1970, 1972, 1972, 1972, 1984, 2005, 2019, 2020, 2020, 2019, 2019, 2021, 2025, 2028, 2032, 2036, 2041, 2048, 2057, 2066, 2074, 2081, 2089, 2097, 2105, 2113, 2122, 2132, 2143, 2154, 2164, 2174, 2184, 2190, 2190, 2181, 2166, 2146, 2125, 2103, 2082, 2062, 2046, 2036, 2030, 2027, 2025, 2024, 2022, 2019, 2017, 2014, 2012, 2012, 2014, 2016, 2019, 2021, 2026, 2031, 2037, 2041, 2044, 2045, 2047, 2048, 2048, 2047, 2046, 2044, 2043, 2041, 2040, 2039, 2038, 2037, 2037, 2037, 2038, 2039, 2041, 2042, 2043, 2044, 2046, 2047, 2048, 2048, 2047, 2047, 2047, 2047, 2047, 2048, 2050, 2052, 2055, 2058, 2060, 2062, 2062, 2059, 2056, 2052, 2049, 2046, 2044, 2041, 2042, 2044, 2046, 2047, 2034, 2023, 2023, 2045, 2128, 2231, 2273, 2273, 2206, 2092, 2045, 2045, 2046, 2051, 2051, 2051]";
        data_source = StringToAscii.test(name);
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
//        // 为close按钮绑定事件监听器
//        close.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View source)
//            {
//                // 获取启动当前Activity的上一个Intent
//                Intent intent = new Intent(EcgView.this,
//                        MainActivity.class);
//                // 启动intent对应的Activity
//                startActivity(intent);
//                // 结束当前Activity
//                finish();
//            }
//        });
    }
}