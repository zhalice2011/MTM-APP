package com.ruiqi.mtm;
import android.media.JetPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cn.com.contec.jar.cmssxt.DeviceCommand;
import cn.com.contec.jar.cmssxt.DevicePackManager;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by angel on 2017/11/6.
 */
public class BgBuf {
    private static final String TAG = "cmssxt";
    public static Vector<Integer> m_buf = null;
    private DevicePackManager m_DevicePackManager = new DevicePackManager();
    private Handler myHandler;
    private Message msg;
    public Handler getMyHandler() {
        return myHandler;
    }

    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }
    BgBuf() {
        mPacks = new byte[1024];
        mCount = 0;
        m_buf = new Vector<Integer>();
    }

    public synchronized int Count() {
        return m_buf.size();
    }

    int mSettimeCount = 0;
    private byte[] mPacks;
    private int mCount;

    public synchronized void write(byte[] buf, int count,
                                   final OutputStream pOutputStream) throws Exception {
        int _receiveNum = m_DevicePackManager.arrangeMessage(buf, count);
        Log.i(TAG, "jar包返回的信息：" + _receiveNum);
        switch (_receiveNum) {
            case 8:// 该设备是旧设备 发送不带秒的对时命令
               // new Thread() {
                  //  public void run() {
                        try {
                            Thread.sleep(300);
                            pOutputStream.write(DeviceCommand.command_VerifyTime());// 发送请求数据的命令
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                   // };
                //}.start();
                break;
            case 9:// 该设备是新设备 发送带秒的对时命令
                //new Thread() {
                 //   public void run() {
                        try {
                            Thread.sleep(300);
                            pOutputStream.write(DeviceCommand
                                    .command_VerifyTimeSS());// 发送请求数据的命令
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                  //  };
               // }.start();
                break;
            case 1:// 成功接收数据
                if(m_DevicePackManager.m_DeviceDatas.size()==0){
                    Message msg = myHandler.obtainMessage(BluetoothService.STATE_NO_DATA);
                    myHandler.sendMessage(msg);
                    break;
                }
                for (int i = 0; i < m_DevicePackManager.m_DeviceDatas.size(); i++) {
                   Log.v(TAG,"数据"+m_DevicePackManager.m_DeviceDatas.toString());
                    String[] sugardata=m_DevicePackManager.m_DeviceDatas.get(i).toString().split("[\n]");;
                    String date=sugardata[0].replace("日期:","").trim();
                    String value=sugardata[1].replace("血糖值:","").trim();
                    //Log.v(TAG,"数据"+date+","+value);
                    //提交数据
                    Message msg = myHandler.obtainMessage(BluetoothService.STATE_SEND);
                    BloodData data = new BloodData();
                    data.setTime(date);//时间
                    data.setBloodSugar(value);//血糖值
                    data.setDatatype(Constants.glucometer);
                    msg.obj = data;
                    Log.v(TAG,"数据"+msg.toString());
                    myHandler.sendMessage(msg);
                }
                m_DevicePackManager.m_DeviceDatas.clear();// 清空数据集合 避免重复保存
                // 数据读取完成
                msg = myHandler.obtainMessage(BluetoothService.STATE_READ_COMPLETE);
                myHandler.sendMessage(msg);
               // new Thread() {
               //     public void run() {

                        try {
                            Thread.sleep(300);
,                            Log.i(TAG, "成功接收数据 发送删除命令");
                            pOutputStream.write(DeviceCommand.command_delData());// 发送删除数据的命令
                            //BluetoothService.run = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                  //  };
              //  }.start();

                break;
            case 2:// 接收数据失败
                Log.i(TAG, "接收数据失败");
                break;
            case 3:// 对时成功;
                Log.i(TAG, "对时间成功");
                //new Thread() {
               //     public void run() {
                        try {
                            Thread.sleep(300);
                            pOutputStream.write(DeviceCommand.command_requestData());// 发送请求数据的命令
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                 //   };
               // }.start();

                break;
            case 4:// 对时失败;
                Log.i(TAG, "对时间失败");
                break;
            case 5: // 删除成功
                Log.i(TAG, "删除成功");
                pOutputStream.close();
                break;
            case 6:// 删除失败；
                Log.i(TAG, "删除失败");
                pOutputStream.close();
                break;
            case 7:// 设备无数据；
                Log.i(TAG, "设备无数据");
                msg = myHandler.obtainMessage(BluetoothService.STATE_NO_DATA);
                myHandler.sendMessage(msg);
                break;
            case 0://其他数据
                Log.v(TAG,"获取数据是出错了！");
        }
    }

    /**
     * 接收到的数据存数到文件中
     *
     * @param pContent
     */
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
