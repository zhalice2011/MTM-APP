package com.ruiqi.mtm;

/**
 * Created by zhalice on 2017/11/24.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import cn.com.contec.jar.wt100.DeviceCommand;
import cn.com.contec.jar.wt100.DevicePackManager;
import cn.com.contec.jar.wt100.WTDeviceDataJar;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class WTBuf {
    private static final String TAG = "lz";
    public static Vector<Integer> m_buf = null;
    private boolean mReceiveDataFailed = true;
    //达理
    private Handler myHandler;
    private Message msg;
    public Handler getMyHandler() {
        return myHandler;
    }
    WTBuf() {
        m_buf = new Vector<Integer>();
    }

    public synchronized int Count() {
        return m_buf.size();
    }

    DevicePackManager mPackManager = new DevicePackManager();
    int mSettimeCount = 0;

    public synchronized void write(byte[] buf, int count,
                                   OutputStream pOutputStream) throws Exception {
        HashMap<String, String>  _resultmap = mPackManager.arrangeMessage(buf, count);
        int _back = Integer.valueOf(_resultmap.get("result"));
        switch (_back) {
            case 2: //接收数据成功
                mReceiveDataFailed = false;
                Log.d(TAG, "---------接受数据成功 开始发送删除数据命令-----------");
                pOutputStream.write(DeviceCommand.command_delData());
                Log.d(TAG, "---------接受到的数据：-----------");
                ArrayList<WTDeviceDataJar> mDatas = mPackManager.m_DeviceDatas;
                int _size = mDatas.size();
                for (int i = 0; i < _size; i++) {
                    WTDeviceDataJar mData = mDatas.get(i);
                    String _content = mData.getUserMeasureResult();
                    String time = mData.m_saveDate;   //时间
                    String wt = mData.m_data;         //体重
                    int mResult_H = mData.mResult_H;  //体重高八位
                    int mResult_L = mData.mResult_L;  //体重低八位
                    int num = mData.mNum;             //序号
                    Log.e(TAG,"体重数据"+_content);
                    Log.e(TAG,"时间"+time);
                    Log.e(TAG,"体重"+wt);
                    Log.e(TAG,"体重高八位"+mResult_H);
                    Log.e(TAG,"体重低八位"+mResult_L);
                    Log.e(TAG,"序号"+num);
                    //提交数据
                    Message msg = myHandler.obtainMessage(BluetoothService.STATE_SEND);
                    BloodData data = new BloodData();

                    data.setTime(time); //时间Integer.
                    data.setWt(wt);   //体重
                    data.setDatatype(Constants.weightscale);
                    msg.obj = data;
                    myHandler.sendMessage(msg);
                }
                break;
            case 3://设置日期时间成功
                Log.d(TAG, "---------设置日期时间成功-----------获取版本号_resultmap");
                String _version= _resultmap.get("version");
                //请求数据
                pOutputStream.write(DeviceCommand.command_requestData(_version));
                break;
            case 4:// 设置日期时间失败
                Log.i(TAG, "-------------设置日期时间失败" + mSettimeCount + "次-------------");
                //请求数据
                break;
            case 6://无数据
                Log.d(TAG, "---------无数据-----------");
                Message msg = myHandler.obtainMessage(BluetoothService.STATE_NO_DATA);
                myHandler.sendMessage(msg);
                //请求数据
                break;
            case 7:// 或接受数据失败或设备的实时状态
                if(mReceiveDataFailed){ // 接受数据失败
                    Log.d(TAG, "---------接受数据失败-----------");
                    //请求数据
                }else { //设备的实时状态
                    Log.d(TAG, "---------设备的实时状态-----------");
                }
                break;
            case 5:// 删除数据成功
                Log.d(TAG, "---------删除数据成功-----------");
                Message msg2 = myHandler.obtainMessage(BluetoothService.STATE_BROKEN);
                myHandler.sendMessage(msg2);
                break;
        }
    }


    String PATH_BASE = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/contec";
    /**
     * 接收到的数据存数到文件中
     * @param pContent
     */
    public void saveAsString(String pContent){
        File _file=new File(PATH_BASE);
        if(!_file.exists()){
            _file.mkdirs();
        }
        try {
            OutputStreamWriter os=new OutputStreamWriter(new FileOutputStream(_file+"/WT_Data.txt"));
            os.write(pContent);
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

    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }


}
