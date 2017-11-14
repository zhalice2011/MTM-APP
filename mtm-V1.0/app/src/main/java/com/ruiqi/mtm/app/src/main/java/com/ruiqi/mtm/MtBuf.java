package com.ruiqi.mtm;

import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.util.Log;

import com.contec.jar.contec08a.DeviceCommand;
import com.contec.jar.contec08a.DeviceData;
import com.contec.jar.contec08a.DevicePackManager;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

/**
 * Created by HongYuLiu on 2017/10/11.
 */

public class MtBuf {
    private static final String TAG = "contec08a";
    public static Vector<Integer> m_buf = null;
    public static final int e_pack_pressure_back = 0x46;
    DevicePackManager mPackManager = new DevicePackManager();
    private DeviceData mDeviceData;
    public static final int e_pack_hand_back = 0x48;
    public static final int e_pack_oxygen_back = 0x47;
    private int mType = 0;
    private Handler myHandler;

    public Handler getMyHandler() {
        return myHandler;
    }

    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }

    public MtBuf(){
        m_buf = new Vector<Integer>();
    }

    public synchronized int Count() {
        return m_buf.size();
    }


    public synchronized void write(byte[] buf, int count,
                                   OutputStream pOutputStream) throws Exception {

        int state = mPackManager.arrangeMessage(buf, count, mType);
        int x = DevicePackManager.Flag_User;
        switch (state) {
            case e_pack_hand_back: // 0x48
                switch (mType) {
                    case 9:
                        mType = 5;
                        pOutputStream.write(DeviceCommand.DELETE_BP());
                        break;
                    case 0:
                        mType = 3;
                        pOutputStream.write(DeviceCommand.correct_time_notice); // 2.1请求校正时间命令
                        break;
//			case 1:
//				mType = 2;
//				pOutputStream.write(DeviceCommand.REQUEST_OXYGEN());
//				break;
//			case 7:
//				mType = 8;
//				pOutputStream.write(DeviceCommand.REQUEST_OXYGEN());
//				break;
//			case 2:
//				mType = 5;
//				pOutputStream.write(DeviceCommand.DELETE_OXYGEN());
//				break;
//			case 8:
//				mType = 5;
//				pOutputStream.write(DeviceCommand.DELETE_OXYGEN());
//				break;
                    case 3:
                        mType = 1;

                        if (x == 0x11) {
                            mType = 7;// 三个用户
                        } else {
                            mType = 1;// 单用户
                        }

                        //pOutputStream.write(DeviceCommand.REQUEST_AMBULATORY_BLOOD_PRESSURE());

                        pOutputStream.write(DeviceCommand.REQUEST_BLOOD_PRESSURE()); // 3.1 请求血压数据 关机掉数据
                        //pOutputStream.write(DeviceCommand.REQUEST_NORMAL_BLOOD_PRESSURE()); // 3.1 请求血压数据
                        break;
                    case 1:
                        mType = 1;

                        if (x == 0x11) {
                            mType = 7;// 三个用户
                        } else {
                            mType = 1;// 单用户
                        }
                        pOutputStream.write(DeviceCommand.REQUEST_BLOOD_PRESSURE()); // 3.1 请求血压数据 关机掉数据
                        break;
                }
                break;
            case 0x30:// 确认校正时间正确
                pOutputStream.write(DeviceCommand.Correct_Time()); // 2.2 设置校正时间
                break;
            case 0x40:// 校正时间正确
                pOutputStream.write(DeviceCommand.REQUEST_HANDSHAKE()); //
                break;
            case e_pack_pressure_back: // 0x46 数据接收完毕
                try {
                Thread.sleep(300);
            } catch (InterruptedException e) {////防止最后一条命令血压设备接收不到
                e.printStackTrace();
            }
                ArrayList<byte[]> _dataList = mPackManager.mDeviceData.mData_blood;  // 3.2 接收数据
                /*
                其中byte[ ]为一次血压测量结果
                byte[0]：高压的高8 位
                byte[1]：高压的低8 位
                byte[2]：低压
                byte[3]：脉率
                byte[4]：平均压
                byte[5]：年的低两位。例如：2013，则为13
                byte[6]：月
                byte[7]：日
                byte[8]：时
                byte[9]：分
                byte[10]:秒
                */
                int _size = _dataList.size();
                if(_size==0){
                    Message msg = myHandler.obtainMessage(BluetoothService.STATE_NO_DATA);
                    myHandler.sendMessage(msg);
                    break;
                }
                //DeviceData _mData = new DeviceData(null);
                for (int i = 0; i < _size; i++) {
                    byte[] _byte = _dataList.get(i);
                    byte[] _data = _byte;
                    int hiPre = ((_data[0] << 8 )|( _data[1] & 0xff)) & 0xffff;// 高压
                    int lowPre = _data[2] & 0xff;// 低压
                    int bpm = _data[3] & 0xff;// 脉率
                    int year = 2000 + (_data[5] & 0xff);
                    int month = _data[6] & 0xff;
                    int day = _data[7] & 0xff;
                    int hour = _data[8] & 0xff;
                    int min = _data[9] & 0xff;
                    int sec = _data[10] & 0xff;

                    // 时间
                    String date = String.valueOf(year) + "-" + getTimeFormat(month) + "-" + getTimeFormat(day)
                            + " "
                            + getTimeFormat(hour) + ":"+ getTimeFormat(min) + ":"+ getTimeFormat(sec);
                    // 发送血压数据
                    Message msg = myHandler.obtainMessage(BluetoothService.STATE_SEND);
                    BloodData data = new BloodData();
                    data.setTime(date);
                    data.setSys(hiPre);//高压
                    data.setDia(lowPre);
                    data.setBpm(bpm);
                    data.setDatatype(Constants.blooddeivce);
                    msg.obj = data;
                    myHandler.sendMessage(msg);
                }

                // 数据读取完成
                Message msg = myHandler.obtainMessage(BluetoothService.STATE_READ_COMPLETE);
                myHandler.sendMessage(msg);
                pOutputStream.write(DeviceCommand.REPLAY_CONTEC08A()); // 3.3 接收完血压数据
                break;
        }

    }

    private String getTimeFormat(int val) {
        if (val > 9) {
            return String.valueOf(val);
        } else {
            return "0" + String.valueOf(val);
        }
    }



}