package com.baidu.mapclient.liteapp.devicescan.adapter;






import static com.baidu.mapclient.liteapp.devicescan.model.BluetoothLeDevice.encodeHexStr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.devicescan.model.BluetoothLeDevice;


public class DeviceAdapter extends ArrayAdapter<BluetoothLeDevice> {
    private static final String SIFLI_TAG = "SIFLI-BLE";
    int resourceId;
    public DeviceAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BluetoothLeDevice bluetoothLeDevice = getItem(position);//获取当前项的Fruit实例
        /* View view = LayoutInflater.from
                (getContext()).inflate(resourceId, parent, false);

        */
        //对上一个语句进行优化
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);//false参数表示只让我们在父布局中声明的layout属性生效
        } else {
            view = convertView;
        }


        TextView deviceName = view.findViewById(R.id.device_name);
        TextView deviceAddr = view.findViewById(R.id.device_addr);
        TextView deviceRssi = view.findViewById(R.id.device_rssi);
        TextView deviceAdRecord = view.findViewById(R.id.device_record);
        //Log.i(SIFLI_TAG, "before  " + bluetoothLeDevice.getRssi());

        deviceName.setText(bluetoothLeDevice.getDevice().getName());
        deviceAddr.setText(bluetoothLeDevice.getDevice().getAddress());
        deviceRssi.setText(bluetoothLeDevice.getRssi() + "dB");
        deviceAdRecord.setText(encodeHexStr(bluetoothLeDevice.getScanRecord()));

        return view;
    }

}
