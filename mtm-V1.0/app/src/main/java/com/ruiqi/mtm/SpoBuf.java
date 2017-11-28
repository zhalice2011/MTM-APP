package com.ruiqi.mtm;

/**
 * Created by zhalice on 2017/11/13.
 *
 *   Ⅰ.   手机对CMS50D+发送索要设备号的命令
     Ⅱ.   手机对CMS50D+发送对时命令
     Ⅲ.   手机对CMS50D+发送设置计步器信息
     Ⅳ.   手机对CMS50D+发送请求血氧脉率的数据命令
     Ⅴ     手机对CMS50D+发送请求计步器数据命令(以天为单位)
     ⅤI.   手机对CMS50D+发送请求计步器数据命令(以分为单位)

 */
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.contec.cms50dj_jar.DeviceCommand;
import com.contec.cms50dj_jar.DeviceData50DJ_Jar;
import com.contec.cms50dj_jar.DevicePackManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

public class SpoBuf {
    private static final String TAG = "SpoBuf";
    public static Vector<Integer> m_buf = null;
    private DevicePackManager m_DevicePackManager = new DevicePackManager();
    private DeviceData50DJ_Jar m_DeviceData50DJ_Jar = new DeviceData50DJ_Jar();
    //达理
    private Handler myHandler;
    private Message msg;
    public Handler getMyHandler() {
        return myHandler;
    }

    SpoBuf() {
        m_buf = new Vector<Integer>();
    }

    public synchronized int Count() {
        return m_buf.size();
    }

    int mSettimeCount = 0;

    public synchronized void write(byte[] buf, int count,
                                   final OutputStream pOutputStream) throws Exception {
        int _receiveNum = m_DevicePackManager.arrangeMessage(buf, count);
        switch (_receiveNum) {
            case 1:// 得到设备号 发送校时命令
                try {
                    pOutputStream.write(DeviceCommand.correctionDateTime());
                    Log.i(TAG, "获取设备号成功 发送校时命令--->DeviceCommand.correctionDateTime()");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:// 对时成功
                try {
                    pOutputStream.write(DeviceCommand.setPedometerInfo("175", "75", 0, 24, 10000, 1));

                    Log.i(TAG, "校时成功  发送设置计步器信息命令--->DeviceCommand.setPedometerInfo (170，65，0，24，5000,1))");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:// 对时失败 关闭socket
                Log.i(TAG, "校时失败");
                break;
            case 8:// 8:设置计步器 成功
                try {
                    pOutputStream.write(DeviceCommand.getDataFromDevice());
                    Log.i(TAG, "设置计步器 成功  发送 请求血氧数据命令--->DeviceCommand.getDataFromDevice()" +
                            "" +
                            "返回6，表示一包血氧、脉率数据接收完毕，此时应该发送血氧、脉率上传成功命令，返回5，表示全部血氧、脉率数据接收完毕，此时可以发送计步器以天为单位数据的命令，返回20，表示上一包数据接收完成的命令发送成功，此时应该发送请求下一包的命令，返回7 ，代表血氧、脉率数据接收失败。返回4，血氧、脉率无新数据。");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 4:// 无新数据;
                int mCount =DevicePackManager.mCount;
                Log.e("没有新的测量数据", "---------------------->>mCount=="+mCount);
                pOutputStream.write(DeviceCommand.dayPedometerDataCommand()); //发送请求计步器以天为单位的数据的命令
                Log.v(TAG,"发送以天为单位的计步器数据  dayPedometerDataCommand 返回 10时，以天为单位 计步器数据 一包上传完成，返回11时，以天为单位计步器数据上一包上传成功请求下一包数据，返回12时，以天为单位的计步器数据全部上传完成，返回17时，以天为单位 计步器无新数据，返回13，计步器以天为单位的数据上传失败 ");
                Message msg = myHandler.obtainMessage(BluetoothService.STATE_NO_DATA);
                myHandler.sendMessage(msg);
                break;
            case 5: // 整个数据接收完成
                Log.i(TAG, " 全部血氧数据接收完成  接下来处理数据");
                Message msg4 = myHandler.obtainMessage(BluetoothService.STATE_CLEAR);
                myHandler.sendMessage(msg4);
                //此时应该对血氧、脉率数据进行处理和发送请求计步器以天为单位的数据的命令
                //字节数组的长度是8，前6个字节表示血氧、脉率的测量时间： 年 （eg：2014->14）、月、日、时、分、秒，第7、8个字节分别表示血氧值和脉率值。
                DeviceData50DJ_Jar _djData = m_DevicePackManager.getDeviceData50dj();
                    for (int i = 0; i < _djData.getmSp02DataList().size(); i++) {
                        byte[] _data = _djData.getmSp02DataList().get(i);
                        int year = _data[0] & 0xff;// 年
                        int month = _data[1] & 0xff;//  月
                        int day = _data[2] & 0xff;//  日
                        int hour = _data[3] & 0xff;// 时
                        int min = _data[4] & 0xff;//  分
                        int sec = _data[5] & 0xff;//  秒
                        int sp0 = _data[6] & 0xff;//  血氧
                        int bpm = _data[7] & 0xff;//  脉率
                        String date = "20"+String.valueOf(year) + "-" + getTimeFormat(month) + "-" + getTimeFormat(day)
                                + " "
                                + getTimeFormat(hour) + ":"+ getTimeFormat(min) + ":"+ getTimeFormat(sec);
                        //提交数据
                        Message msg2 = myHandler.obtainMessage(BluetoothService.STATE_SEND);
                        BloodData data = new BloodData();
                        data.setTime(date); //时间
                        data.setSpo(sp0);   //血氧
                        data.setPr(bpm);   //脉率
                        data.setDatatype(Constants.pulsedeice);
                        msg2.obj = data;
                        Log.v(TAG,"秒钟:"+sec+"血氧:"+sp0+"/脉率"+bpm);
                        myHandler.sendMessage(msg2);

                    }
                pOutputStream.write(DeviceCommand.dayPedometerDataCommand()); //发送请求计步器以天为单位的数据的命令
                Log.v(TAG,"发送以天为单位的计步器数据  dayPedometerDataCommand 返回 10时，以天为单位 计步器数据 一包上传完成，返回11时，以天为单位计步器数据上一包上传成功请求下一包数据，返回12时，以天为单位的计步器数据全部上传完成，返回17时，以天为单位 计步器无新数据，返回13，计步器以天为单位的数据上传失败 ");
                //pOutputStream.write(DeviceCommand.minPedometerDataSuccessCommand())
                //Message msg2 = myHandler.obtainMessage(BluetoothService.STATE_BROKEN);
                //myHandler.sendMessage(msg2);
                break;
            case 6://一包数据接收完毕 ；发送数据上传成功的指令，（必须这样发）
                Log.i(TAG, "一包血氧、脉率数据接收完毕  发送数据上传成功的指令---> DeviceCommand.dataUploadSuccessCommand ()");
                pOutputStream.write(DeviceCommand.dataUploadSuccessCommand ());
                break;
            case 7:// 接收失败
                Log.i(TAG, "血氧数据接收失败  请求以天为单位的 计步器数据");
                pOutputStream.write(DeviceCommand.dayPedometerDataCommand());
                break;
            case 9:// 9: 设置计步器失败 由于血氧、脉率数据和计步器没有关系，所以这里也可以发送请求血氧、脉率数据的命令
                try {
                    pOutputStream.write(DeviceCommand.getDataFromDevice());
                    Log.i(TAG, "设置计步器 失败  发送 请求血氧数据命令--->DeviceCommand.getDataFromDevice()");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 10:// 以天为单位 计步器数据 一包上传完成
                try {
                    pOutputStream.write(DeviceCommand.dayPedometerDataSuccessCommand());
                    Log.i(TAG, "以天为单位计步器 数据 一包上传完成  发送上传完成命令");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 11:// 以天为单位计步器 数据上一包上传成功 请求下一包数据
					try {
                        pOutputStream.write(DeviceCommand.dayPedometerDataCommand());
						Log.i(TAG, "以天为单位计步器 数据上一包上传成功  请求下一包数据 DeviceCommand.dayPedometerDataCommand()");
					} catch (Exception e) {
						e.printStackTrace();
					}


                break;
            case 12:// 以天为单位计步器 数据 全部 上传成功 请求以分为单位的数据
                // TODO 此处处理计步器一天为单位的数据
//			    saveDaypedometerData();
                try {
                    pOutputStream.write(DeviceCommand.minPedometerDataCommand());
                    Log.i(TAG, "以天为单位计步器 数据  全部   上传成功  请求以分为单位的数据");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case 13:// 以天为单位计步器 数据上传失败 请求以分为单位的数据
                try {
                    pOutputStream.write(DeviceCommand.minPedometerDataCommand());
                    Log.i(TAG, " 以天为单位计步器 数据上传失败 请求以分为单位的数据");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 14:// 以分为单位 计步器数据 一包上传完成
                try {
                    Thread.sleep(500);
                    pOutputStream.write(DeviceCommand.minPedometerDataSuccessCommand());
                    Log.i(TAG, "以 分 为单位 计步器数据 一包上传完成 发送上传完成命令");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case 15:// 以分为单位 计步器数据 一包上传完成
					try {
						Thread.sleep(500);
                        pOutputStream.write(DeviceCommand.minPedometerDataCommand());
						Log.i(TAG, "以分为单位计步器 数据 一包上传完成  发送请求下一包的命令");
					} catch (Exception e) {
						e.printStackTrace();
					}

                break;
            case 16:
                // 以分为单位计步器 数据 全部 上传成功 关闭socket
                // TODO 存储以分为单位的数据
                Log.i(TAG, " 以分为单位计步器 数据 全部 上传成功 关闭socket ");
                break;
            case 17:// 以天为单位 计步器无新数据
                Log.i(TAG, " 返回17 以天为单位 计步器无新数据    请求以分为单位的数据");
                try {
                    pOutputStream.write(DeviceCommand.minPedometerDataCommand());
                    Message msg2 = myHandler.obtainMessage(BluetoothService.STATE_BROKEN);
                    myHandler.sendMessage(msg2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 18:// 以分为单位 计步器无新数据
                Log.i(TAG, " 以分为单位 计步器无新数据   ");
                break;
            case 19:// 以分为单位 计步器数据上传失败
                Log.i(TAG, " 以分为单位 计步器数据上传失败   ");
                break;
            case 20:// 表示上一包数据接收完成的命令发送成功，此时应该发送请求下一包的命令 请求下一包血氧数据
                Log.i(TAG, " 请求下一包血氧数据   DeviceCommand.getDataFromDevice())");
                pOutputStream.write(DeviceCommand.getDataFromDevice());
                break;
        }
    }

    /**
     * 接收到的数据存数到文件中
     *
     * @param //pContent
     */
//    public void saveAsString(String pContent) {
//        String PATH_BASE = Environment.getExternalStorageDirectory()
//                .getAbsolutePath() + "/contec";
//        File _file = new File(PATH_BASE);
//        if (!_file.exists()) {
//            _file.mkdirs();
//        }
//        try {
//            OutputStreamWriter os = new OutputStreamWriter(
//                    new FileOutputStream(_file + "/Cmssxt_Data.txt", true));
//            os.write(pContent);
//            os.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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

    private String getTimeFormat(int val) {
        if (val > 9) {
            return String.valueOf(val);
        } else {
            return "0" + String.valueOf(val);
        }
    }
}

