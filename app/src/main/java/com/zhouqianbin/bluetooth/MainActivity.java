package com.zhouqianbin.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.clj.fastble.BleManager;
import com.inuker.bluetooth.library.BluetoothClient;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private AppCompatButton mBtnOpenBlue;
    private AppCompatButton mBtnCloseBlue;
    private AppCompatButton mBtnStartScan;
    private AppCompatButton mBtnStopScan;
    private AppCompatButton mBtnSendContent;
    private AppCompatEditText mEtSendContent;

    private ReadWriteThread readWriteThread;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        doBusiness();
        setListen();
    }


    private void doBusiness() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
            this.finish();
        }

        mBlueToothRecyAdapter = new BlueToothRecyAdapter(R.layout.item_main_recy_list, mBluetoothDeviceList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRecyclerView.setAdapter(mBlueToothRecyAdapter);

        registerRecevice();

        if (mBluetoothAdapter.isEnabled()) {
            if (mAcceptThread == null) {
                //开始线程接收数据
                mAcceptThread = new AcceptThread();
                mAcceptThread.start();
            }
        }
    }




    /**
     * 注册蓝牙接收的广播
     */
    private void registerRecevice() {
        IntentFilter filter = new IntentFilter();
        //发现蓝牙设备广播
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //蓝牙设备连接状态改变
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //蓝牙设备状态改变
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //蓝牙设备断开
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //蓝牙设备断开状态
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //蓝牙适配器的连接状态更改到远程设备的配置文件的意图。
        // 当适配器没有连接到任何远程设备的任何配置文件，并且
        // 尝试连接到配置文件时，这个意图将被发送
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        //蓝牙扫描模式发生改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //蓝牙名称更改
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
    }

    private RecyclerView mRecyclerView;
    private BlueToothRecyAdapter mBlueToothRecyAdapter;
    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<>();

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //每扫描到一个设备，系统都会发送此广播。
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //获取蓝牙设备
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "发现设备 " + bluetoothDevice.toString());
                mBluetoothDeviceList.add(bluetoothDevice);
                mBlueToothRecyAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showToast("蓝牙扫描完成");
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_CONNECTION_STATE,
                        BluetoothAdapter.STATE_DISCONNECTED);
                switch (state) {
                    case BluetoothAdapter.STATE_CONNECTED:
                        showToast("蓝牙状态：连接");
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:
                        showToast("蓝牙状态：断开");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        showToast("蓝牙状态：连接中");
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        showToast("蓝牙状态：断中");
                        break;
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        showToast("蓝牙正在打开");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        showToast("蓝牙已经开启");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        showToast("蓝牙正在关闭");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        showToast("蓝牙已经关闭");
                        break;
                }
            }
        }
    };


    private void initView() {
        mEtSendContent = findViewById(R.id.main_et_send_content);
        mBtnOpenBlue = findViewById(R.id.main_btn_open_blut);
        mBtnCloseBlue = findViewById(R.id.main_btn_close_blut);
        mBtnStartScan = findViewById(R.id.main_btn_start_scan);
        mBtnStopScan = findViewById(R.id.main_btn_stop_scan);
        mBtnSendContent = findViewById(R.id.main_btn_send);
        mRecyclerView = findViewById(R.id.main_recy_list);
    }

    private void setListen() {

        mBtnOpenBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBlueTooth(false);
            }
        });

        mBtnCloseBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeBlueTooth();
            }
        });

        mBtnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanBlue();
            }
        });

        mBtnStopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanBlue();
            }
        });


        mBtnSendContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mEtSendContent.getText())){
                    return;
                }
                readWriteThread.write(mEtSendContent.getText().toString().trim().getBytes());
            }
        });

        mBlueToothRecyAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (mConnectThread == null) {   //开启连接蓝牙的线程
                    mConnectThread = new ConnectThread(mBluetoothDeviceList.get(position));
                    mConnectThread.start();
                }
            }
        });

    }




    private final int OPEN_BLUTTOOTH_REQUEST_CODE = 1000;

    /**
     * 打开蓝牙
     *
     * @param isHintUserOpen 是否提示用户自己打开
     */
    public void openBlueTooth(boolean isHintUserOpen) {
        //蓝牙是否打开
        if (mBluetoothAdapter.isEnabled()) {
            showToast("当前蓝牙已经打开");
            return;
        }
        if (isHintUserOpen) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, OPEN_BLUTTOOTH_REQUEST_CODE);
        } else {
            mBluetoothAdapter.enable();
        }
    }


    /**
     * 关闭蓝牙
     */
    public void closeBlueTooth() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }


    /**
     * 开始扫描
     */
    public void startScanBlue() {
        if (!mBluetoothAdapter.isEnabled()) {
            showToast("请先打开蓝牙");
            return;
        }
        //清空列表数据
        mBluetoothDeviceList.clear();
        mBlueToothRecyAdapter.notifyDataSetChanged();
        //是否正在扫描
        if (!mBluetoothAdapter.isDiscovering()) {
            //取消扫描
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.startDiscovery();
        }
    }


    /**
     * 取消扫描
     */
    public void stopScanBlue() {
        if (!mBluetoothAdapter.isEnabled()) {
            showToast("请先打开蓝牙");
            return;
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }


    private class AcceptThread extends Thread {
        //蓝牙服务端的Socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                //通过蓝牙适配器获取服务端的Socket
                tmp = mBluetoothAdapter
                        .listenUsingRfcommWithServiceRecord(
                                "eric",
                                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.d(TAG, "AcceptThread " + e.toString());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            while (isRun) {
                try {
                    BluetoothSocket scoket = mmServerSocket.accept();
                    manageConnectedSocket(scoket);//相关处理函数
                    mmServerSocket.close();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //取消正在进行的连接，并且关闭套接字
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private void manageConnectedSocket(BluetoothSocket socket) {
        if (socket == null) {
            return;
        }
        readWriteThread = new ReadWriteThread(socket);
        readWriteThread.start();
    }

    private class ReadWriteThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ReadWriteThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // 开始读取数据
                    if ((bytes = mmInStream.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        //读取到的结果
                        final String s = new String(buf_data);
                        //发送读取的数据
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    try {
                        //关闭输入流
                        mmInStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        /**
         * 具体连接你扫描到的蓝牙设备
         *
         * @param device
         */
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                //通过BluetoothDevice对象获取BluetoothSocket
                tmp = device.createRfcommSocketToServiceRecord(UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            //连接的时候取消扫描
            mBluetoothAdapter.cancelDiscovery();
            try {
                //开始连接
                mmSocket.connect();
                //如果程序往下执行则表明连接成功，可以提示用户
                showToast("连接成功");
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            manageConnectedSocket(mmSocket);
        }

        //将取消正在进行的连接，并关闭套接字
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "mSocket 关闭错误 " + e.getLocalizedMessage());
            }
        }
    }


    private boolean isRun = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothReceiver);
        isRun = false;
        //释放线程
        mConnectThread.cancel();
        readWriteThread.cancel();
        mAcceptThread.cancel();
    }



    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}