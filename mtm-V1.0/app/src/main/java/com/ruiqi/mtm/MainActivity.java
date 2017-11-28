package com.ruiqi.mtm;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.ViewGroup;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.lang.String;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.view.KeyEvent;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class MainActivity extends AppCompatActivity implements
       ICallBack {
    private BluetoothAdapter bluetoothAdapter;
    private Toast tipToast;
    private Context context;
    private ListView lsvDevices;
    private ListView lsvDevicesData;
    private Button btnGetData;
    private BluetoothService bluetoothService;
    private CallBack call;
    private Handler mHandler;
    private MtBuf m_mtbuf = new MtBuf();
    private BgBuf m_bgbuf=new BgBuf();
    private BpmBuf m_bpmbuf=new BpmBuf();
    private SpoBuf m_spobuf=new SpoBuf();
    private WTBuf m_wtbuf=new WTBuf();
    private static  boolean isExit=false;

    private Bundle params;
    private Dialog mDialog;
    private ArrayList<HashMap<String,Object>> dataList;
    private List<Map<String,Object>> devDataList;
    //private SimpleAdapter sim_adapter;
    private mySimpleAdapter sim_adapter;
    private SimpleAdapter dev_adapter;
    public static  String devicename;
    public static  String devicetype;
    public static  int deviceimg;

    private static final int REQUEST_EXTERNAL_STORAGE = 1; //权限申请 达理
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
             "android.permission.WRITE_EXTERNAL_STORAGE" };
    //退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void exit(){
        if(isExit) {
            finish();
            onDestroy();
            System.exit(0);
        } else{
            isExit=true;
            Toast.makeText(getApplicationContext(),this.getString(R.string.exitmsg),Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            myHandle.sendEmptyMessageDelayed(0,2000);
        }
    }
    //菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public void logOut(MenuItem item){
        SharedPreferences readdata=getSharedPreferences("login",0) ;
        SharedPreferences.Editor editor = readdata.edit();
        editor.clear();
        editor.commit();
        Intent logoutIntent = new Intent(MainActivity.this, loginActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(logoutIntent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lsvDevices = (ListView)findViewById(R.id.lsvDevices);  //设备列表
        lsvDevicesData = (ListView)findViewById(R.id.lsvDevicesData);  //数据列表
        dataList=new ArrayList<HashMap<String, Object>>();
        params=this.getIntent().getExtras();
        init();
        verifyStoragePermissions(this); //申请权限 达理

    /*
        BloodData data = new BloodData();
        data.setTime("2017-12-16 17:17:17");;
        data.setResults("室早RonT室\n早三联律");
        data.setBpm(86);
        //data.setSys(102);
        //data.setDia(89);
        data.setDatatype("PM");
        sendData(data,true);
      data.setDatatype("NIBP03");
        sendData(data,true);

        BloodData data = new BloodData();
        data.setTime("2017-12-16 17:17:17");;
        data.setBloodSugar("18.8");
        data.setDatatype("BG01");
        sendData(data,true);
        *//*
        BloodData data = new BloodData();
        data.setTime("2017-12-16 17:17:17");;
        data.setSys(125);
        data.setDia(86);
        data.setBpm(90);
        data.setDatatype("NIBP03");
        sendData(data,true);
*/
    }

    /**
     * 初始化
     */
    private void init() {
        context = this;
        // 初始化蓝牙
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()){
            Log.v("设备","设备未开启蓝牙");
            bluetoothAdapter.enable();
        }
        // 设备列表
        lsvDevices.setOnItemClickListener(deviceClickListener);
        // 设备数据列表
        devDataList=new ArrayList<Map<String, Object>>();  //定义设备列表适配器
        // 加载已经连接蓝牙设备
        Set<BluetoothDevice> dev = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : dev) {
            Boolean legaldevice=false;
            Map<String,Object> dalimap=new HashMap<String,  Object>();  //定义一个字符串对象
            Log.v("device",device.getName().toString());
            //测试部分别忘了改回去
            if(device.getName().contains(Constants.blooddeivce)){//NIBP03   ✔
                deviceimg=R.drawable.contect08a;
                devicetype=Constants.blooddeivce;
                devicename=Constants.NIBP03;
                legaldevice=true;
            }else if(device.getName().contains(Constants.ecgdeivce)){//PM ✔
                deviceimg=R.drawable.pm;
                devicetype=Constants.ecgdeivce;
                devicename=Constants.PM;
                legaldevice=true;
            }else if(device.getName().contains(Constants.pulsedeice)){ //spodata
                devicename=Constants.SpO;
                devicetype=Constants.pulsedeice;
                deviceimg=R.drawable.jerry;
                legaldevice=true;
            }else if(device.getName().contains(Constants.glucometer)){ //BG01   ✔
                devicename=Constants.bg0;
                devicetype=Constants.glucometer;
                deviceimg=R.drawable.glucometer;
                legaldevice=true;
            }else if(device.getName().contains(Constants.weightscale)){ //WT   ✔
                devicename=Constants.WT;
                devicetype=Constants.weightscale;
                deviceimg=R.drawable.weightscale;
                legaldevice=true;
            }

            if(legaldevice) {
                dalimap.put("pic", deviceimg);
                dalimap.put("text", devicename);
                dalimap.put("devicetype", devicetype);
                dalimap.put("adress", device.getAddress());
                dalimap.put("refresh_pic", R.drawable.sync);
                dalimap.put("refresh_msg", "同步");
                devDataList.add(dalimap);
            }
        }
        //new一个新的 SimpleAdapter适配器
        dev_adapter = new SimpleAdapter(this,devDataList,R.layout.device_list,
                new String[]{"pic","text","devicetype","adress","refresh_pic","refresh_msg"},
                new int[]{R.id.item_pic,R.id.item_name,R.id.devicetype,R.id.item_address,R.id.connect_pic,R.id.connect_msg});
        //渲染设备视图列表
        lsvDevices.setAdapter(dev_adapter);
        m_mtbuf.setMyHandler(myHandle);
        m_bgbuf.setMyHandler(myHandle);
        m_bpmbuf.setMyHandler(myHandle);
        m_spobuf.setMyHandler(myHandle);
        m_wtbuf.setMyHandler(myHandle);
        call = new CallBack(m_mtbuf, this,m_bgbuf,m_bpmbuf,m_spobuf,m_wtbuf);
        bluetoothService = new BluetoothService(context, call, myHandle);
        mHandler = new Handler();
        //数据列表的点击事件
        //lsvDevicesData.setOnItemClickListener(dataClickListener);
        // 注册Receiver来获取蓝牙设备相关的结果
        /*
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(searchDevices, intentFilter);
        */
    }

    //点击设备列表
    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {   //点击列表项，连接设备
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            LinearLayout parent = (LinearLayout) v;
            TextView t = (TextView) parent.findViewById(R.id.item_address);
            TextView devtype = (TextView) parent.findViewById(R.id.devicetype);
            String info = (String) t.getText();
            String devicetypename=(String) devtype.getText();
            mDialog = DialogThridUtils.showWaitDialog(MainActivity.this, "蓝牙连接中...", false, true);
            // 连接蓝牙设备
            bluetoothService.start();
            bluetoothService.setDevicetype(devicetypename);
            bluetoothService.connectedServer();
            //lsvDevicesData.setAdapter(sim_adapter);
            dataList.clear();
            lsvDevicesData.setAdapter(null);
            bluetoothService.connect(bluetoothService.getDevByMac(info));
        }
    };

    //点击数据列表重新上传
   /*
    private AdapterView.OnItemClickListener dataClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            HashMap<String,Object> blooddata=(HashMap<String, Object>) sim_adapter.getItem(arg2);
            Log.v("device",blooddata.get("datatype").toString());
            switch (blooddata.get("datatype").toString()){
                case Constants.ecgdeivce :{
                    Log.v("device","心电计");
                    int xl=Integer.parseInt(blooddata.get("bpm").toString());
                    String results=blooddata.get("results").toString();
                    String datatype=blooddata.get("datatype").toString();
                    String time = blooddata.get("time").toString();
                    BloodData data = new BloodData();
                    data.setTime(time);
                    data.setBpm(xl);
                    data.setResults(results);
                    data.setDatatype(datatype);
                    sendData(data,false);
                    break;
                }
                case Constants.blooddeivce :{
                    Log.v("device","血压计");
                    int gy=Integer.parseInt(blooddata.get("sys").toString());
                    int dy=Integer.parseInt(blooddata.get("dia").toString());
                    int xl=Integer.parseInt(blooddata.get("bpm").toString());
                    String datatype=blooddata.get("datatype").toString();
                    String time = blooddata.get("time").toString();
                    BloodData data = new BloodData();
                    data.setTime(time);
                    data.setSys(gy);
                    data.setDia(dy);
                    data.setBpm(xl);
                    data.setDatatype(datatype);
                    sendData(data,false);
                    break;
                }
                case Constants.glucometer :{
                    Log.v("device","血糖仪"+blooddata.toString());
                    String bloodsugar=blooddata.get("bloodsugar").toString();
                    String datatype=blooddata.get("datatype").toString();
                    String time = blooddata.get("time").toString();
                    BloodData data = new BloodData();
                    data.setTime(time);
                    data.setBloodSugar(bloodsugar);
                    data.setDatatype(datatype);
                    sendData(data,false);
                    break;
                }
            }

    };
}*/
    // 搜索的设备信息
    private BroadcastReceiver searchDevices = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            Object[] names = bundle.keySet().toArray();

            // 取得设备的MAC地址
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    String name = device.getName();
                }
            }
        }
    };

    // 搜索蓝牙设备，在BroadcastReceiver显示结果
    public void searchOnClick(View view) {
        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            Toast.makeText(context, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void call() {

    }
    //
    private final Handler myHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    DialogThridUtils.closeDialog(mDialog);
                    super.handleMessage(msg);
                    isExit=false;
                    break;
                case BluetoothService.STATE_CONNECTED:
                    DialogThridUtils.closeDialog(mDialog);
                    //Toast.makeText(context, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                    showTip("蓝牙连接成功!");
                    break;
                case BluetoothService.STATE_SEND:
                    // 上传数据
                    //bluetoothService.connectCancel();//达理  心电计是先发送数据然后获取波形图这里断开蓝牙连接就不能获取蓝牙数据了
                    BloodData data = (BloodData) msg.obj;
                    if (data != null) {
                        Log.v("sendata",data.toString());
                        sendData(data,true);
                    }
                    break;
                case BluetoothService.STATE_READ_COMPLETE:
                    bluetoothService.connectCancel();
                    DialogThridUtils.closeDialog(mDialog);
                    //Toast.makeText(context, "数据读取完成", Toast.LENGTH_SHORT).show();
                    showTip("数据读取完成!");
                    break;
                case BluetoothService.STATE_NO_DATA:
                    DialogThridUtils.closeDialog(mDialog);
                    //Toast.makeText(context, "无新的测量数据,请测量后点击上传!", Toast.LENGTH_SHORT).show();
                    showTip("设备无新数据,请测量后点击上传!");
                    break;
                case BluetoothService.STATE_NO_DEVICE:
                    DialogThridUtils.closeDialog(mDialog);
                    //Toast.makeText(context, "连接设备失败请稍后重试...", Toast.LENGTH_SHORT).show();
                    showTip("连接设备失败请稍后重试...");
                    break;
                case BluetoothService.STATE_BROKEN:
                    bluetoothService.connectCancel();
                    //showTip("数据接收结束!");//心电计波形图数据数据接收完成,释放连接成功
                    break;
                case BluetoothService.STATE_CLEAR:
                    dataList.clear();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        // 关闭连接
        bluetoothService.connectCancel();
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    /**
     * 上传数据
     * @param //sid  门店编码
     * @param //time 采集时间，时间格式：2017-03-01 17:17:21
     * @param //sys 收缩压（高压）
     * @param //dia 舒张压（低压）
     *
     */
    private void sendData( BloodData data ,boolean put) {
        String serverURL = getResources().getString(R.string.server_url)+"/api/bloodpressure"; // 上传服务器
        String appid=params.getString("appid");
        String appkey=params.getString("appkey");
        String storeid=params.getString("storeid");
        JSONObject jsonObject = new JSONObject();
        switch (data.getDatatype()){
            case Constants.blooddeivce :// 血压计:NIBP03
                //生成json数据
                try {
                    jsonObject.put("sid", storeid);
                    jsonObject.put("time", data.getTime());
                    jsonObject.put("sys", data.getSys());
                    jsonObject.put("dia", data.getDia());
                    jsonObject.put("heartrate", data.getBpm());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //渲染页面
                if(put){ //点击上传还是渲染数据
                    //String data = "高压:   " + dia + "\n低压:   " + sys+"\n心率:   "+bpm+"\n时间:   "+time; // 界面显示使用
                    //sim_adapter = new SimpleAdapter(this, dataList, R.layout.nibdata,
                    sim_adapter = new mySimpleAdapter(this, dataList, R.layout.nibdata,
                            new String[]{"pic", "sys", "dia", "bpm", "time","datatype"},
                            new int[]{R.id.pic, R.id.sys, R.id.dia, R.id.bpm, R.id.times,R.id.datatype});
                    lsvDevicesData.setAdapter(sim_adapter);
                    HashMap<String, Object> blood = new HashMap<String, Object>();
                    blood.put("pic", R.drawable.blood);//图片
                    blood.put("sys", data.getSys());
                    blood.put("dia", data.getDia());
                    blood.put("bpm", data.getBpm());
                    blood.put("time",data.getTime());
                    blood.put("datatype",data.getDatatype());
                    dataList.add(blood);
                }
                break;
            case Constants.ecgdeivce :// 智能心电计:PM
                //生成json数据
                try {
                    jsonObject.put("sid", storeid);
                    jsonObject.put("time", data.getTime());
                    jsonObject.put("bpm_rusult", data.getResults());
                    jsonObject.put("heartrate", data.getBpm());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //渲染页面
                if(put){ //点击上传还是渲染数据
                    sim_adapter =new mySimpleAdapter(this,dataList,R.layout.pmdata,
                            new String[]{ "results", "bpm", "time","datatye"},
                            new int[]{ R.id.results, R.id.bpm, R.id.times,R.id.datatype});
                    lsvDevicesData.setAdapter(sim_adapter);
                    HashMap<String, Object> pm = new HashMap<String, Object>();
                    pm.put("results",data.getResults());
                    pm.put("bpm",data.getBpm());
                    pm.put("time",data.getTime());
                    pm.put("datatype",data.getDatatype());
                    dataList.add(pm);
                }
                break;
            case Constants.glucometer ://血糖仪:BG01
                //生成json数据
                try {
                    jsonObject.put("sid", storeid);
                    jsonObject.put("time", data.getTime());
                    jsonObject.put("bloodSugar", data.getBloodSugar());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //渲染页面
                if(put){ //点击上传还是渲染数据
                    sim_adapter = new mySimpleAdapter(this, dataList, R.layout.bloodsugardata,
                            new String[]{"bloodsugar","time","datatype"},
                           new int[]{R.id.bloodsugar,R.id.times,R.id.datatype});
                    lsvDevicesData.setAdapter(sim_adapter);
                    HashMap<String, Object> blsugar = new HashMap<String, Object>();
                    blsugar.put("bloodsugar",data.getBloodSugar());
                    blsugar.put("time",data.getTime());
                    blsugar.put("datatype",data.getDatatype());
                    dataList.add(blsugar);
                }
                break;
            case Constants.pulsedeice :// 血氧
                //生成json数据
                try {
                    jsonObject.put("sid", storeid);
                    jsonObject.put("time", data.getTime());
                    jsonObject.put("spo2", data.getSpo());
                    jsonObject.put("pr", data.getPr());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //渲染页面
                if(put){ //点击上传还是渲染数据
                    sim_adapter = new mySimpleAdapter(this, dataList, R.layout.spodata,
                            new String[]{"spo","pr","time","datatype"},
                            new int[]{R.id.spo,R.id.pr,R.id.times,R.id.datatype});
                    lsvDevicesData.setAdapter(sim_adapter);
                    HashMap<String, Object> spo = new HashMap<String, Object>();
                    spo.put("spo",data.getSpo());
                    spo.put("pr",data.getPr());
                    spo.put("time",data.getTime());
                    spo.put("datatype",data.getDatatype());
                    dataList.add(spo);
                }
                break;
            case Constants.weightscale :// 体重计
                //生成json数据
                try {
                    jsonObject.put("sid", storeid);
                    jsonObject.put("time", data.getTime());
                    jsonObject.put("weight", data.getWt());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //渲染页面
                if(put){ //点击上传还是渲染数据
                    sim_adapter = new mySimpleAdapter(this, dataList, R.layout.wtdata,
                            new String[]{"weight","time","datatype"},
                            new int[]{R.id.weight,R.id.times,R.id.datatype});
                    lsvDevicesData.setAdapter(sim_adapter);
                    HashMap<String, Object> wt = new HashMap<String, Object>();
                    wt.put("weight",data.getWt());
                    wt.put("time",data.getTime());
                    wt.put("datatype",data.getDatatype());
                    dataList.add(wt);
                }
                break;
        }
        String json = jsonObject.toString();
        // 向服务器上传数据json格式
        Request request = new Request.Builder()
                .url(serverURL)
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, json))
                .header("appid", appid)
                .header("appkey", appkey)
                .build();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //mInfoText.setText("HEM-7081-IT已连接，开始接收数据");
                            //dismissProgressDialog();
                            if (response != null) {
                                try {
                                    String resp = response.body().string();
                                    JSONObject jo = new JSONObject(resp);
                                    int code = jo.getJSONObject("error").getInt("code");
                                    if (code == 0) {
                                       // poastToast("上传成功");
                                        showTip("上传成功");
                                    } else {
                                       // poastToast("上传失败");
                                        showTip("上传失败");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.v("upload","upero0:"+e.toString());
                                }
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //提示框1
    public void poastToast(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
    //提示框2--big
    private void showTip(String str) {
       // runOnUiThread(new Runnable() {
         //   @Override
          //  public void run() {
                if (tipToast == null) {
                    tipToast = Toast.makeText(getApplicationContext(), "",
                            Toast.LENGTH_LONG);
                    LinearLayout layout = (LinearLayout) tipToast.getView();
                    TextView tv = (TextView) layout.getChildAt(0);
                    tv.setTextSize(16);
                }
                //mToast.cancel();
                tipToast.setGravity(Gravity.CENTER, 0, 0);
                tipToast.setText(str);
                tipToast.show();
           // }
        //});
    }
    //权限申请 达理
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //自定义适配器
    public class mySimpleAdapter extends SimpleAdapter {
        //重写listview适配器
        private int[] colors1 = new int[] { 0xFFFFFFFF, 0xFFedeeef};
        private int[] Colors2 = new int[] { 0xFFcd211e, 0xFF3e9e0f};
        private ArrayList<HashMap<String, Object>> listitem;

        public mySimpleAdapter(Context context, ArrayList<HashMap<String, Object>> data, int resource, String[] from, int[] to){
            super(context, data, resource, from, to);
            // TODO Auto-generated constructor stub
            this.listitem = data;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            ImageView upload=(ImageView) view.findViewById(R.id.upload);
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reupload(listitem.get(position));
                }
            });

            //下面设置值变色效果。
            TextView nowdatatype=(TextView)view.findViewById(R.id.datatype);
            if(nowdatatype.getText().toString().equals(Constants.blooddeivce)){
                TextView payorsave = (TextView)view.findViewById(R.id.sys);
                TextView diadisp = (TextView)view.findViewById(R.id.dia);
                int sys = Integer.parseInt(listitem.get(position).get("sys").toString());
                int dia = Integer.parseInt(listitem.get(position).get("dia").toString());
                Log.v("dev",payorsave.getText()+","+String.valueOf(sys));
                //获取“data”中需要自定义样式的数组
                if(sys>140 || sys<90){
                    payorsave.setTextColor(Colors2[0]);
                }else{
                    payorsave.setTextColor(Colors2[1]);
                }
                if(dia>90 || dia<60){
                    diadisp.setTextColor(Colors2[0]);
                }else{
                    diadisp.setTextColor(Colors2[1]);
                }
            }
            return view;
        }
    }
    public void  reupload(HashMap<String,Object> updata){
        Log.e("test","点击事件："+updata.get("datatype").toString());
        switch (updata.get("datatype").toString()) {
            case Constants.ecgdeivce: {
                Log.v("device", "心电计"+updata.get("results").toString());
                int xl = Integer.parseInt(updata.get("bpm").toString());
                String results = updata.get("results").toString();
                String datatype = updata.get("datatype").toString();
                String time = updata.get("time").toString();
                BloodData data = new BloodData();
                data.setTime(time);
                data.setBpm(xl);
                data.setResults(results);
                data.setDatatype(datatype);
                sendData(data, false);
                break;
            }
            case Constants.blooddeivce: {
                Log.v("device", "血压计");
                int gy = Integer.parseInt(updata.get("sys").toString());
                int dy = Integer.parseInt(updata.get("dia").toString());
                int xl = Integer.parseInt(updata.get("bpm").toString());
                String datatype = updata.get("datatype").toString();
                String time = updata.get("time").toString();
                BloodData data = new BloodData();
                data.setTime(time);
                data.setSys(gy);
                data.setDia(dy);
                data.setBpm(xl);
                data.setDatatype(datatype);
                sendData(data, false);
                break;
            }
            case Constants.glucometer: {
                Log.v("device", "血糖仪" + updata.toString());
                String bloodsugar = updata.get("bloodsugar").toString();
                String datatype = updata.get("datatype").toString();
                String time = updata.get("time").toString();
                BloodData data = new BloodData();
                data.setTime(time);
                data.setBloodSugar(bloodsugar);
                data.setDatatype(datatype);
                sendData(data, false);
                break;
            }
        }
    }
}


