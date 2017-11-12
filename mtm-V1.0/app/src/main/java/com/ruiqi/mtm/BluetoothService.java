package com.ruiqi.mtm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
//import com.contec.jar.contec08a.DeviceCommand;
//import com.contec.jar.pm10.DeviceCommand;
/**
 * Created by HongYuLiu on 2017/10/11.
 */

public class BluetoothService {
    private static final String TAG = "BluetoothService";
    private static final boolean D = true;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private CallBack call;
    private String devicetype;
    private Handler myHandler;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_SEND = 4;  // now connected to a remote device
    public static final int STATE_READ_COMPLETE = 5;  // now connected to a remote device
    public static final int STATE_NO_DATA = 6;  // size=0 无新的测量数据
    public static final int STATE_NO_DEVICE = 7;  // 血压计处于关闭状态
    public static final int STATE_NO_HAND = 8;  // bt socket closed, read return: -1 握手失败 请重试

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, CallBack callBack, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        call = callBack;
        myHandler = handler;
    }
    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }
    public String  getDevicetype() {
        return devicetype;
    }
    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        Log.d(TAG, "start");

        connectCancel();
        // Start the thread to listen on a BluetoothServerSocket
        setState(STATE_LISTEN);
    }

    public void connectCancel() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        connectCancel();
        // Cancel the accept thread because we only want to connect to one device
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Send the name of the connected device back to the UI Activity
        setState(STATE_CONNECTED);
    }

    public synchronized BluetoothDevice getDevByMac(String mac)
    {
        return mAdapter.getRemoteDevice(mac);
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception

                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.v("设备类型",e.toString());
//                if(e instanceof  NumberFormatException) {//判断是不是数据转换异常
//                    System.out.println("NumberFormatException");//输出结果
//                }
                Message msg = myHandler.obtainMessage(BluetoothService.STATE_NO_DEVICE);
                myHandler.sendMessage(msg);
//                connectionFailed();
                // Close the socket
                try {
                    if (mmSocket!=null) {
                        mmSocket.close();
                    }
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            switch (devicetype){
                 case Constants.glucometer://血糖
                    try{
                        tmpIn = socket.getInputStream();
                        tmpOut = socket.getOutputStream();
                        Log.i(TAG, "send verify time command");
                        Log.v(TAG,"定位："+String.valueOf(tmpIn));
                        byte[] _cmd = cn.com.contec.jar.cmssxt.DeviceCommand.command_ReadID(); // 获取设备id
                        tmpOut.write(_cmd);
                        // 发送连接成功命令
                        Message msg = myHandler.obtainMessage(BluetoothService.STATE_CONNECTED);
                        myHandler.sendMessage(msg);
                    }catch (IOException e) {
                        Log.e(TAG, "血糖仪 temp sockets not created", e);
                    }
                    break;
                case Constants.blooddeivce://血压
                    try{
                        tmpIn = socket.getInputStream();
                        tmpOut = socket.getOutputStream();
                        Log.i(TAG, "send verify time command");
                        Log.v(TAG,devicetype);
                        byte[] _cmd = com.contec.jar.contec08a.DeviceCommand.REQUEST_HANDSHAKE(); // 1.1 获取握手命令代码
                        tmpOut.write(_cmd);
                        // 发送连接成功命令
                        Message msg = myHandler.obtainMessage(BluetoothService.STATE_CONNECTED);
                        myHandler.sendMessage(msg);
                    } catch (IOException e) {
                        Log.e(TAG, "血压计temp sockets not created", e);
                    }
                    break;
           }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            boolean run = true;
            // Keep listening to the InputStream while connected
            while (run) {
                Log.e(TAG, "正在接受数据 **************************" + run);
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    Log.e(TAG,devicetype );
                    switch (devicetype) {
                        case Constants.glucometer://血糖
                            call.m_bgbuf.write(buffer, bytes, mmOutStream); // 处理来自蓝牙数据
                            break;
                        case Constants.blooddeivce:
                            call.m_mtbuf.write(buffer, bytes, mmOutStream); // 处理来自蓝牙数据
                            break;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    // 发送连接失败的命令
                    if (mmInStream!=null) {
                        try {
                            mmInStream.close();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                    connectCancel(); // 释放连接
                    connectionLost();
                    run = false;
                    break;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "disconnected2", e);
                    if (mmInStream!=null) {
                        try {
                            mmInStream.close();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                    connectionLost();
                    run = false;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
