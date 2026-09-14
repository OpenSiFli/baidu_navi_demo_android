package com.baidu.mapclient.liteapp.devicescan;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.devicescan.adapter.DeviceAdapter;
import com.baidu.mapclient.liteapp.devicescan.model.BluetoothLeDevice;
import com.baidu.mapclient.liteapp.devicescan.model.BluetoothLeDeviceStore;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DeviceScanActivity extends Activity {
    private static final String SIFLI_TAG = "SIFLI-BLE";
    public final static String EXTRA_BLE_DEVICE = "EXTRA_BLE_DEVICE";
    public static long scanStartTime;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeDeviceStore bluetoothLeDeviceStore = new BluetoothLeDeviceStore();
    private ListView lv;
    private DeviceAdapter adapter;
    BluetoothLeScanner mBLEScanner;
    private boolean filterEnable = false;
    private boolean isEngineeringMode = false;
    private boolean isAutoTest = false;
    private long filterTime = 0;
    private String autoTestAddress;
    private int autoTestType;
    private int autoTestCommand;
    private int autoTestPath;
    private final String TAG = "sifli-scan";
    private boolean mScanning = false;
    private Context mContext;
    private boolean isBluetoothOn;
    private boolean isSort;

    private final String[] PERMISSIONS_BLUETOOTH = {
            //Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
    };

    public DeviceScanActivity() {
    }

    public void verifyBluetoothPermissions(Activity activity) {
        // Check if we have scan permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.BLUETOOTH_SCAN);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            int request = 1;
            ActivityCompat.requestPermissions(activity, PERMISSIONS_BLUETOOTH,
                    request);
        }
    }

    private List<String> requestList = new ArrayList<String>();

    public void checkPer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestList.add(Manifest.permission.BLUETOOTH_SCAN);
            requestList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (requestList.size() != 0) {
            ActivityCompat.requestPermissions(DeviceScanActivity.this, requestList.toArray(new String[0]), 1);
        }
    }

    ScanCallback mScanCallback = new ScanCallback() {
        //当一个蓝牙ble广播被发现时回调
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);


            //扫描类型有开始扫描时传入的ScanSettings相关
            //对扫描到的设备进行操作。如：获取设备信息。
            Log.i(TAG, "is auto: " + isAutoTest + ", addr: " + autoTestAddress);
            // Log.i(TAG, "scan find: " + result.getDevice().getAddress());
            BluetoothLeDevice bluetoothLeDevice = new BluetoothLeDevice(result.getDevice(),
                    result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());
            bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);

            if (isAutoTest && autoTestAddress != null) {
                if (autoTestAddress.equals(result.getDevice().getAddress())) {
                    Toast.makeText(DeviceScanActivity.this, "found auto dev", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            ActivityCompat.requestPermissions(DeviceScanActivity.this, PERMISSIONS_BLUETOOTH, 1);
                            return;
                        }
                    }
                    mBLEScanner.stopScan(mScanCallback);
                    isAutoTest = false;
//                    startAutoTest(bluetoothLeDevice);
                }
            }


            //Log.i(SIFLI_TAG, "found, " + bluetoothDevice.getAddress() + "rssi: " + bluetoothLeDevice.getRssi());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter != null && bluetoothLeDeviceStore != null) {
                        if (isSort) {

                        } else {
                            if (filterEnable) {
                                if (System.currentTimeMillis() - filterTime > 3000) {
                                    adapter.clear();
                                    adapter.addAll(filterScanDevice());
                                    filterTime = System.currentTimeMillis();
                                }
                            } else {
                                adapter.clear();
                                adapter.addAll(bluetoothLeDeviceStore.getDeviceList());
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }

        //批量返回扫描结果
        //@param results 以前扫描到的扫描结果列表。
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

        }

        //当扫描不能开启时回调
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG, "scan error: " + errorCode);
            //扫描太频繁会返回ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED，表示app无法注册，无法开始扫描。

        }
    };

    private void enableBle() {
        if (!bluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(DeviceScanActivity.this, PERMISSIONS_BLUETOOTH, 1);
                    return;
                }
            }
            //bluetoothAdapter.enable();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private void init() {


        Button button = findViewById(R.id.scan_sort_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isSort = true;
                        Log.d(TAG, "sort rssi");
                        adapter.clear();
                        adapter.addAll(bluetoothLeDeviceStore.getDeviceList(1));
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        Button scanBtn = findViewById(R.id.scan_scan_btn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartScanBtnTouch();
            }
        });

        Button stopScanBtn = findViewById(R.id.scan_stop_btn);
        stopScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStopBtnTouch();
            }
        });

    }

    private List<BluetoothLeDevice> filterScanDevice() {

//        TextView textView = findViewById(R.id.editTextFRSSI);
        int mFilterRssiValue = -60;
//        mFilterRssiValue = Integer.parseInt(textView.getText().toString());

        List<BluetoothLeDevice> bluetoothLeDevices = new ArrayList<>();
        List<BluetoothLeDevice> showList = new ArrayList<>();
        bluetoothLeDevices = bluetoothLeDeviceStore.getDeviceList();
        for (int i = 0; i < bluetoothLeDevices.size(); i++) {
            if (bluetoothLeDevices.get(i).getRssi() > mFilterRssiValue) {
                showList.add(bluetoothLeDevices.get(i));
            }
        }
        return showList;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //enableBle();

        verifyBluetoothPermissions(DeviceScanActivity.this);
        setContentView(R.layout.activity_device_scan);
        //lv = (ListView) findViewById(android.R.id.scan_device_list);
        ActivityCompat.requestPermissions(DeviceScanActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        ActivityCompat.requestPermissions(DeviceScanActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        lv = findViewById(R.id.scan_device_list);

        mContext = DeviceScanActivity.this;
        mContext.registerReceiver(mReceiver, makeFilter());

        //checkPer();

        adapter = new DeviceAdapter(DeviceScanActivity.this, R.layout.scan_device);
        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            mBLEScanner = bluetoothAdapter.getBluetoothLeScanner();
            isBluetoothOn = true;
        } else {
            isBluetoothOn = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(DeviceScanActivity.this, PERMISSIONS_BLUETOOTH, 1);
                    return;
                }
            }
            bluetoothAdapter.enable();
        }
        //mBLEScanner = bleadapter.getBluetoothLeScanner();
        init();
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new ScanRecordClickListener());

    }

    private IntentFilter makeFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            isBluetoothOn = true;
                            mBLEScanner = bluetoothAdapter.getBluetoothLeScanner();
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            isBluetoothOn = false;
                            break;
                    }
            }
        }
    };

    class ScanRecordClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            BluetoothLeDevice device = (BluetoothLeDevice) adapter.getItem(position);
            if (device == null) {
                return;
            }
            finishWithMac(device.getAddress());
        }

    }

    private View permissionRationale;
    private final static int REQUEST_PERMISSION_REQ_CODE = 34;

//    @Override
//    public boolean onCreateOptionsMenu(final Menu menu) {
//        getMenuInflater().inflate(R.menu.scan, menu);
//        menu.findItem(R.id.menu_stop).setVisible(true);
//        menu.findItem(R.id.menu_scan).setVisible(true);
//        //menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress);
//
//        return true;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    public boolean onOptionsItemSelected(final MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_scan://开始扫描
//                enableBle();
//                Log.i(SIFLI_TAG, "begin to scan");
//                scanStartTime = System.currentTimeMillis();
//                filterEnable = false;
//
//                if (ContextCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // When user pressed Deny and still wants to use this functionality, show the rationale
//                    if (permissionRationale == null) {
//                        Log.e(TAG, "permissionRationale null");
//                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
//                        return false;
//                    }
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(DeviceScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) && permissionRationale.getVisibility() == View.GONE) {
//                        permissionRationale.setVisibility(View.VISIBLE);
//                        return false;
//                    }
//
//                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
//                    return false;
//                }
//                startScan();
//                break;
//            case R.id.menu_stop://停止扫描
//
//                mBLEScanner.stopScan(mScanCallback);
//                mScanning = false;
//                break;
//        }
//        return true;
//    }

    private void onStopBtnTouch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mBLEScanner.stopScan(mScanCallback);
        mScanning = false;
    }

    private void onStartScanBtnTouch(){
        enableBle();
        Log.i(SIFLI_TAG, "begin to scan");
        scanStartTime = System.currentTimeMillis();
        filterEnable = false;

        if (ContextCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (permissionRationale == null) {
                Log.e(TAG, "permissionRationale null");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
                return ;
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(DeviceScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) && permissionRationale.getVisibility() == View.GONE) {
                permissionRationale.setVisibility(View.VISIBLE);
                return ;
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            return ;
        }
        startScan();
    }

    private void startScan() {
        if (mBLEScanner == null || mScanCallback == null) {
            return;
        }
        if (isBluetoothOn) {

            ScanSettings settings = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                settings = new ScanSettings.Builder()
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            } else {
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            }

            //checkPer();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(DeviceScanActivity.this, PERMISSIONS_BLUETOOTH, 1);
                    return;
                }
            }
            mBLEScanner.startScan(null, settings, mScanCallback);
            mScanning = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_FINE_LOCATION permission. Now we may proceed with scanning.
                    startScan();
                } else {
                    permissionRationale.setVisibility(View.VISIBLE);
                    //Toast.makeText(getActivity(), R.string.no_required_permission, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        bluetoothLeDeviceStore.clear();
        scanStartTime = System.currentTimeMillis();
        filterEnable = false;
        if (isAutoTest) {
            startScan();
        }
        startScan();
//
//        ToggleButton toggleButton = findViewById(R.id.toggleButton_filter);
//        toggleButton.setChecked(false);
//        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        invalidateOptionsMenu();
        bluetoothLeDeviceStore.clear();
        if (mScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(DeviceScanActivity.this, PERMISSIONS_BLUETOOTH, 1);
                    return;
                }
            }
            mBLEScanner.stopScan(mScanCallback);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            // 已经注销或未注册，忽略
        }

        super.onDestroy();
        if (mScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(DeviceScanActivity.this, PERMISSIONS_BLUETOOTH, 1);
                    return;
                }
            }
            mBLEScanner.stopScan(mScanCallback);
        }
    }

    private void finishWithMac(String mac){
        Intent intent = getIntent();
        intent.putExtra(EXTRA_BLE_DEVICE, mac);
        setResult(RESULT_OK,intent);
        finish();
    }

}