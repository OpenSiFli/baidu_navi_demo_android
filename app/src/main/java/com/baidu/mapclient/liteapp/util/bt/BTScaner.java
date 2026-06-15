package com.baidu.mapclient.liteapp.util.bt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.sifli.siflicore.log.SFLog;


/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2023/9/4
 * description
 */
public class BTScaner {
    private BluetoothAdapter mBluetoothAdapter;
    private final static String TAG = "BTScaner";
    private BTScanerCallback callback;

    private Context mContext;
    private String mTargetMac;
    private Handler mTimeHandler;
    private boolean targetFound;

    public BTScaner(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(receiver, filter);
        mTimeHandler = new Handler(Looper.getMainLooper());
    }

    public void destroy() {
        mContext.unregisterReceiver(receiver);
    }

    public void startDiscovery(String targetMac) {
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//
//            return;
//        }
        mTargetMac = targetMac;
        targetFound = false;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG,"startDiscovery require BLUETOOTH_SCAN");
            return;
        }
        mBluetoothAdapter.startDiscovery();
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTimeHandler.removeCallbacksAndMessages(null);
                mBluetoothAdapter.cancelDiscovery();
                if(!targetFound){
                    callback.onTimeout();
                }

            }
        },15 * 1000);

    }

    public void cancelDiscovery(){
        SFLog.i(TAG,"cancelDiscovery");
        if(mTimeHandler != null)mTimeHandler.removeCallbacksAndMessages(null);
        mBluetoothAdapter.cancelDiscovery();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    LDLog.e(TAG,"require BLUETOOTH_CONNECT permission here!");
//                    return;
//                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceHardwareAddress.equals(mTargetMac)){
                    targetFound = true;
                    callback.onTargetDeviceFound(device);
                }
            }
        }
    };

    public void setCallback(BTScanerCallback callback) {
        this.callback = callback;
    }
}
