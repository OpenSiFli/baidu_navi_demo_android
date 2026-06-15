package com.baidu.mapclient.liteapp.util;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_UNKNOWN;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.baidu.mapclient.liteapp.util.bt.BTScaner;
import com.baidu.mapclient.liteapp.util.bt.BTScanerCallback;
import com.sifli.siflicore.log.SFLog;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2023/9/4
 * description
 */
public class BTBondManager implements BTScanerCallback {
    private BluetoothAdapter mBluetoothAdapter;
    private final static String TAG = "BTBondManager";
    private Context mContext;
    private BTScaner btScaner;

    public BTBondManager(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.btScaner = new BTScaner(context);
        this.btScaner.setCallback(this);
    }

    public void init() {
        mBluetoothAdapter.isEnabled();
    }

    public void createBondByMac(String mac) {
        Log.i(TAG, "createBondByMac =" + mac);
//        BluetoothDevice dev = mBluetoothAdapter.getRemoteDevice(mac);
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "createBondByMac require permission BLUETOOTH_CONNECT");
//            return;
//        }
//        Log.i(TAG, "device type =" + dev.getType());
//        this.createBond(dev);
//        Log.i(TAG, "create bond suc=" + suc + ",mac=" + mac);
        toast("BT Discovery...");
        this.btScaner.startDiscovery(mac);
    }

    public BluetoothDevice createBluetoothDevice(String mac) {
        Log.i(TAG, "createBluetoothDevice =" + mac);
        BluetoothDevice dev = mBluetoothAdapter.getRemoteDevice(mac);
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "createBondByMac require permission BLUETOOTH_CONNECT");
//            return null;
//        }
//        Log.i(TAG, "device type =" + dev.getType());
        return dev;
    }

    public int getDeviceType(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
           return DEVICE_TYPE_UNKNOWN;
        }
        return device.getType();
    }

    public void removeBond(String mac){
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            LDLog.e(TAG,"removeBond require permission BLUETOOTH_CONNECT");
//            return;
//        }
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            SFLog.i(TAG,"remove bond success.mac=" + mac);
        } catch (Exception e) {
            SFLog.e(TAG, e.getMessage());
        }
    }

    public void createBond(BluetoothDevice device){
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            SFDemoLog.e(TAG,"createBond require permission BLUETOOTH_CONNECT");
//            return;
//        }
        int deviceType = device.getType();
        SFLog.i(TAG, "device type =" + deviceType);
        if(deviceType == DEVICE_TYPE_UNKNOWN || deviceType == DEVICE_TYPE_LE){
            toast("设备未识别为支持BR/EDR，请重试");
            return;
        }

        if(device.getBondState() == BOND_BONDING){
            SFLog.i(TAG, "device is BOND_BONDING,return");
            return;
        }else if(device.getBondState() == BOND_BONDED){
            SFLog.i(TAG, "device is BOND_BONDED,return");
            return;
        }

        try {
            Method[] methods = device.getClass().getMethods();
            for (Method m:methods) {
//                Log.i(TAG,"method=" + m.getName());
                if(m.getName().equals("createBond") && m.getParameterCount() == 1){
                    m.setAccessible(true);
                   Object o = m.invoke(device,1);
                    SFLog.i(TAG,"createBond success.result =" + o);

                    break;
                }
            }

//                device.createBond();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeBond(BluetoothDevice device){
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            SFLog.e(TAG, e.getMessage());
        }
    }

    public boolean isBond(String macAddress) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


//        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            SFException exception = new SFException(SFAutoTestResponseCode.PERMISSION_REQUIRED,"Manifest.permission.BLUETOOTH_CONNECT required");
//            throw exception;
//        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(macAddress)) {
                // 设备已配对
                return  true;
            }
        }
        return false;
    }

    private void toast(String msg){
        Toast.makeText(this.mContext,msg,Toast.LENGTH_SHORT).show();
    }

    //region BT Scanner
    @Override
    public void onTargetDeviceFound(BluetoothDevice device) {
        SFLog.i(TAG,"onTargetDeviceFound %s",device.getAddress());
        this.btScaner.cancelDiscovery();
        toast("找到设备,开始配对...");
        this.createBond(device);
    }

    @Override
    public void onTimeout() {
        SFLog.e(TAG,"bt discovery time out");
        toast("BT Discovery time out");
    }
    //endregion
}
