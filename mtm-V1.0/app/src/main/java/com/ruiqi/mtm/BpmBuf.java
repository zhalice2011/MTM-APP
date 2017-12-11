package com.ruiqi.mtm;

/**
 * Created by zhalice on 2017/11/8.
 */
//MtBuf

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.contec.jar.pm10.DeviceCommand;
import com.contec.jar.pm10.DevicePackManager;
import com.contec.jar.pm10.PrintBytes;
import com.contec.jar.pm10.QueryParamInfo;
import com.contec.jar.pm10.QueryParamInfo.ReturnType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

public class BpmBuf {
    private static final String TAG = BpmBuf.class.getSimpleName();
    public static Vector<Integer> m_buf = null;
    private boolean mReceiveDataFailed = true;

    private boolean mFlage = false;

    public static final byte e_back_settime = (byte) 0xF2;  //-14 校时成功
    public static final byte e_back_deletedata = (byte) 0xC0; //-64 删除数据成功
    public static final byte e_back_caseinfo = (byte) 0xE0;   //-32 请求病例个数成功
    public static final byte e_back_single_caseinfo = (byte) 0xE1; //-31 获取单个病例信息成功
    public static final byte e_back_single_data = (byte) 0xD0;  //-48
    public static final byte e_back_stop_transfer = (byte) 0xF6;
    public static final byte e_back_dateresponse = (byte) 0xA0;// 获取病例数据命令及应答包
    public static final byte e_back_queryOrsetParam = (byte) 0xF3;
    public static final byte e_back_queryOrsetParam2 = (byte) 0xFF;
    public static final byte e_back_dali = (byte) 0xFF; //波形数据成功
    private BluetoothService bluetoothService;


    BpmBuf() {
        m_buf = new Vector<Integer>();
    }

    public synchronized int Count() {
        return m_buf.size();
    }

    DevicePackManager mPackManager = new DevicePackManager();
    int mSettimeCount = 0;
    int mCount = 0;
    int _receCount = 1;
    int _receCountdali = 1;
    int _dataCount = 0;
    int _caseLen = 0;
    int _recedataCount = 0;
    int mCaseCount = 1;
    String name22[]={"未见异常","漏搏","偶发室早","偶发室早","室早二联律","成对室早",
                    "室早三连发","室早四连发","室早RonT","心动过缓","心动过速","心律不齐",
                    "ST抬高","ST压低"};
    String Results="";
    private String time;     //时间
    private int bpm2;        //心率
    private String results;  // 心电计---分析结果
    private String datatype; //数据来源(datatype)
    private Handler myHandler;
    private Message msg;
    public Handler getMyHandler() {
        return myHandler;
    }
    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }

    public synchronized void write(byte[] buf, int count,
                                   OutputStream pOutputStream) throws Exception {
        PrintBytes.printData(buf, count);

        byte[] pack = mPackManager.arrangeMessage(buf, count);
        if (pack != null) {
            switch (pack[0]) {
                case e_back_settime:// 校时成功
                    Log.e(TAG, "-14:校时成功"+e_back_settime);
                    pOutputStream.write(DeviceCommand.GET_DATA_INFO(1, 0));
                    Log.e(TAG, "发送命令->请求病例个数: GET_DATA_INFO(1, 0)");
                    break;
                case e_back_caseinfo:// 返回病例信息
                    Log.e(TAG, "-32:请求病例个数成功"+e_back_single_caseinfo);
                    mCount = mPackManager.mCount;
                    if (mCount > 0) {
                        Log.e("病例信息个数", "---------------------->>::" + mCount);
                        Log.e("病例索引", "---------------------->>::" + _receCount);
                        //pOutputStream.write(DeviceCommand.GET_DATA_INFO(2, _receCount));
                        pOutputStream.write(DeviceCommand.GET_DATA_INFO(2, _receCount));
                        Log.e(TAG, "发送命令->请求第"+_receCount+"个病例信息: GET_DATA_INFO(2,"+_receCount);

                    } else {
                        Log.e("没有新的测量数据", "---------------------->>mCount<0::" + mCount);
                        Log.e("病例索引", "---------------------->>::" + _receCount);
                        Message msg = myHandler.obtainMessage(BluetoothService.STATE_NO_DATA);
                        myHandler.sendMessage(msg);
                    }
                    break;
                case e_back_single_caseinfo:// 返回单个病例信息
                    Log.e(TAG, "-31:获取单个病例信息成功"+e_back_single_caseinfo);
                    _receCount++;
                    PrintBytes.printData(pack);
                    int effect = pack[16] & 0xff;
                    int effect2 = pack[17] & 0xff;
                    if(effect2!=0){
                        Results=name22[effect]+"\n"+name22[effect2];
                    }else{
                        Results=name22[effect];
                    }
                    _caseLen = (pack[10] << 21) | (pack[11] << 14)
                            | (pack[12] << 7) | pack[13];
                    _dataCount = _caseLen / 25;
                    _recedataCount = 0;
                    //--------------发送心电计数据
                    String date = mPackManager.mDeviceData.mYear + "-" + mPackManager.mDeviceData.mMonth + "-" + mPackManager.mDeviceData.mDay
                            + " "
                            + mPackManager.mDeviceData.mHour + ":" + mPackManager.mDeviceData.mMin + ":" + mPackManager.mDeviceData.mSec;
                    time=date;
                    bpm2=mPackManager.mDeviceData.Plus;
                    results=Results;
                    datatype=Constants.ecgdeivce;
                    pOutputStream.write(DeviceCommand.GET_DATA(_receCount - 1));  //获取波形图的数据
                    Log.e("发送命令：", "->获取第"+(_receCount-1)+"个波形图数据:  GET_DATA(_receCount - 1)");
                    break;
                case (byte) 0xff:
                    Log.e(TAG, "-1:波形图数据接收成功"+0xff);
                    byte[] _CaseData = mPackManager.mDeviceData.CaseData;
                    String str = Base64.encodeToString(_CaseData,Base64.DEFAULT); //编码
                    //byte[] str2=Base64.decode(str,Base64.DEFAULT);  //解码
                    ArrayList<String> list = new ArrayList();
                    ArrayList<String>   list2 = new ArrayList();
                    for (int i = 0; i < _CaseData.length; i+=2) {
                        //这是宏宇老师的算法
                        int x = _CaseData[i] & 0xFF;
                        int y =(x << 8) | (_CaseData[i+1] & 0xFF);
                        //这是苑一峰的算法
                        int z = _CaseData[i]<<8 | _CaseData[i+1];
                        list.add(Integer.toString(x));
                        list2.add(Integer.toString(z));
                    }
                    // 转化成字符串
                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    for (String s : list)
                    {
                        sb.append(s);
                        sb.append(",");
                    }
                    sb.append("]");
                    Message msg = myHandler.obtainMessage(BluetoothService.STATE_SEND);
                    BloodData data = new BloodData();
                    data.setTime(time);
                    data.setBpm2(bpm2);
                    data.setResults(Results);
                    data.setDatatype(Constants.ecgdeivce);
                    data.setECG(str);
                    data.setECGdata(sb.toString());
                    msg.obj = data;
                    myHandler.sendMessage(msg);
                    Log.e("","保存数据===saveAsString=======_CaseData"+_CaseData);
                    saveAsString(_CaseData);
                    if ((_receCount - 1) == mCount) {  //表示是最后一条数据了
                        _receCount=1;
                        pOutputStream.write(DeviceCommand.GET_DATA_RE(0));
                        Log.e(TAG, "发送命令：GET_DATA_RE(0)");
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        pOutputStream.write(DeviceCommand.DELETE_DATA(0, 0));
                        Log.e(TAG, "发送命令->删除数据: DELETE_DATA(0, 0)");
                        pOutputStream.write(DeviceCommand.CONFIRM); // 3.3
                        //释放连接
                        Message msg2 = myHandler.obtainMessage(BluetoothService.STATE_BROKEN);
                        myHandler.sendMessage(msg2);

                    } else {
                        pOutputStream.write(DeviceCommand.GET_DATA_INFO(2, _receCount));
                        Log.e(TAG, "发送命令->再次请求第"+_receCount+"个病例信息: GET_DATA_INFO(2,_receCount"+_receCount);
                    }
                    break;
                case e_back_single_data:
                    //Log.e("","传入的数据"+e_back_single_data+"--------这是哪--------");
                    _recedataCount++;
                    _dataCount--;

                    if (_dataCount == 0) {
                        if ((_receCount - 1) == mCount) {
                            _receCount=1;
                            pOutputStream.write(DeviceCommand.GET_DATA_RE(0));
                            Log.e("发送命令：", "---------------------->>::DeviceCommand.GET_DATA_RE(0)");

                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            pOutputStream.write(DeviceCommand.DELETE_DATA(0, 0));
                        } else {
                            pOutputStream.write(DeviceCommand.GET_DATA_INFO(2,
                                    _receCount));
                            Log.e("88888888888888888888多条",
                                    "---------------->>>>>_receCount");
                        }
                    } else {
                        // SendCommand.send(DeviceCommand.CONFIRM);
                    }
                    break;
                case e_back_deletedata:// 删除成功
                    Log.e("=========删除数据成功=====", "---------------删除成功共-----------------");
                    //
                    break;
                case e_back_queryOrsetParam:// 查询或设置参数
                    Log.e("查询或设置参数", "---------------删除成功共-----------------");

                    QueryParamInfo mQueryParamInfo = mPackManager
                            .getmQueryOrSetParam();
                    ReturnType returnType = mQueryParamInfo
                            .getQureyReturnOrSetReturn();
                    switch (returnType) {
                        case RETURN_QUERY:
                            Log.e(TAG, "----查询参数信息成功----");
                            //getInfo(mQueryParamInfo);
                            break;
                        case RETURN_SET:
                            // 设置完之后的数据看一下返回结果是否返回成功或者做其他的命令操作
                            Log.e(TAG, "设置命令发送成功-");
                            //getInfo(mQueryParamInfo);
                            pOutputStream.write(DeviceCommand.SET_TIME());
                            break;
                        default:
                            break;
                    }

                    break;
//                case e_back_queryOrsetParam2:
//                    Log.e(TAG, "获取波形图数据成功");
//                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 获取设备参数信息
     *
     * @param mQueryOrSetParam
     */

    String PATH_BASE = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/contec";

    /**
     * 接收到的数据存数到文件中
     *
     * @param pContent
     */
    public void saveAsString(byte[] pContent) {
        Log.e("保存数据到手机", "---------------->存储路径=>>>>" + PATH_BASE);
        File _file = new File(PATH_BASE, "PM10_CASE_DAtA.txt");
        if (!_file.exists()) {
            try {
                _file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Log.e("================", "=====================");
        Log.e("================", "====================="+pContent);
        Log.e("================", "=====================");
        try {
            FileOutputStream os = new FileOutputStream(_file);
            os.write(pContent, 0, pContent.length);
            os.flush();
            os.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public synchronized int read(int[] buf) {
        int len = 0;
        if (buf.length <= m_buf.size()) {
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (int) (m_buf.get(i));
            }
            len = buf.length;
            for (int j = 0; j < len; j++) {
                m_buf.remove(0);
            }

        } else if (buf.length > m_buf.size()) {
            for (int i = 0; i < m_buf.size(); i++) {
                buf[i] = m_buf.get(i);
            }
            len = m_buf.size();
            for (int j = 0; j < len; j++) {
                m_buf.remove(0);
            }

        }
        return len;
    }
}