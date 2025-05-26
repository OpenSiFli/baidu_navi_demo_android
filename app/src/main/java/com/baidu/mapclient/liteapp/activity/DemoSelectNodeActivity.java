package com.baidu.mapclient.liteapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapclient.liteapp.BNDemoFactory;
import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.R;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: v_duanpeifeng
 * Time: 2020-05-21
 * Description:
 */
public class DemoSelectNodeActivity extends Activity implements View.OnClickListener {

    private EditText carNumText;
    private EditText startNodeText;
    private EditText endNodeText;
    private EditText endUid;

    private SeekBar seekBarLongitude;
    private SeekBar seekBarLatitude;

    private String focusNodeString = null;
    private String newFocusNodeString = null;
    private EditText focusEditView;
    private EditText testEnvironment;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);

        initView();
        mapView = new MapView(this);
        // 将设置的起终点展示在地图上，可以通过进度条调整数值，方便精确定位坐标点
        ((ViewGroup) findViewById(R.id.map_container)).addView(mapView);
    }

    private void initView() {
        carNumText = findViewById(R.id.car_num);
        startNodeText = findViewById(R.id.start_node);
        endNodeText = findViewById(R.id.end_node);
        endUid = findViewById(R.id.end_uid);
        testEnvironment = findViewById(R.id.test_environment);
        if (BNDemoUtils.getString(this, "start_node") != null) {
            startNodeText.setText(BNDemoUtils.getString(this, "start_node"));
        }
        if (BNDemoUtils.getString(this, "end_node") != null) {
            endNodeText.setText(BNDemoUtils.getString(this, "end_node"));
        }
        if (BNDemoUtils.getString(this, "end_uid") != null) {
            endUid.setText(BNDemoUtils.getString(this, "end_uid"));
        }
        if (BNDemoUtils.getString(this, "TestEnvironmentUrl") != null) {
            testEnvironment.setText(BNDemoUtils.getString(this, "TestEnvironmentUrl"));
        }
        findViewById(R.id.confirm).setOnClickListener(this);

        startNodeText.setOnFocusChangeListener(
            new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    Log.e("nodeFocus", "Selection:" + startNodeText.getSelectionStart());
                    editFocus(b, startNodeText);
                }
            });
        endNodeText.setOnFocusChangeListener(
            new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    Log.e("nodeFocus", "Selection:" + endNodeText.getSelectionStart());
                    editFocus(b, endNodeText);
                }
            });

        // x轴
        seekBarLongitude = findViewById(R.id.node_change_longitude);
        // y轴
        seekBarLatitude = findViewById(R.id.node_change_latitude);
        findViewById(R.id.change_node_string)
            .setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusNodeString = newFocusNodeString;
                        focusEditView.setText(newFocusNodeString);
                        seekBarLongitude.setProgress(50);
                        seekBarLatitude.setProgress(50);
                    }
                });
        showNode();
    }

    private void editFocus(boolean focus, EditText focusEdit) {
        focusEditView = focusEdit;
        focusNodeString = focusEdit.getText().toString();
        if (!focus) {
            seekBarLongitude.setVisibility(View.GONE);
            seekBarLatitude.setVisibility(View.GONE);
        } else {
            seekBarLongitude.setVisibility(View.VISIBLE);
            seekBarLatitude.setVisibility(View.VISIBLE);
            seekBarLongitude.setProgress(50);
            seekBarLatitude.setProgress(50);
        }
        showNode(getNode(focusNodeString), true);
    }

    private SeekBar.OnSeekBarChangeListener listener =
        new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                showNode(
                    getNode(
                        focusNodeString,
                        (seekBarLongitude.getProgress() - 50) * 0.0001,
                        (seekBarLatitude.getProgress() - 50) * 0.0001),
                    false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

    private void showNode() {
        seekBarLongitude.setOnSeekBarChangeListener(listener);

        seekBarLatitude.setOnSeekBarChangeListener(listener);
    }

    public void showNode(BNRoutePlanNode node, boolean changeMap) {
        if (node == null) {
            return;
        }
        BaiduMap baiduMap = mapView.getMap();
        baiduMap.clear();
        LatLng point = new LatLng(node.getLatitude(), node.getLongitude());
        BitmapDescriptor pickBitmap =
            BitmapDescriptorFactory.fromResource(R.drawable.onsdk_drawable_reservation_start_point);
        OverlayOptions pickOption = new MarkerOptions().position(point).icon(pickBitmap);
        baiduMap.addOverlay(pickOption);
        if (changeMap) {
            List<LatLng> nodes = new ArrayList<>();
            nodes.add(point);
            LatLngBounds latLngBounds = new LatLngBounds.Builder().include(nodes).build();
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(latLngBounds, 10, 30, 10, 30));
        }
    }

    public BNRoutePlanNode getNode(String nodeString, double... addCount) {
        BNRoutePlanNode planNode = null;
        double addLongitude = 0;
        double addLatitude = 0;
        if (addCount.length > 1) {
            addLongitude = addCount[0];
            addLatitude = addCount[1];
        }
        if (!TextUtils.isEmpty(nodeString)) {
            String[] node = nodeString.split(",");
            planNode =
                new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]) + addLongitude)
                    .latitude(Double.parseDouble(node[1]) + addLatitude)
                    .build();
        } else {
            return null;
        }
        newFocusNodeString = planNode.getLongitude() + "," + planNode.getLatitude();
        return planNode;
    }

    @Override
    public void onClick(View v) {
        String carNum = carNumText.getText().toString();
        if (!TextUtils.isEmpty(carNum)) {
            BaiduNaviManagerFactory.getCommonSettingManager().setCarNum(carNum);
        }

        String startNode = startNodeText.getText().toString();
        if (!TextUtils.isEmpty(startNode)) {
            BNDemoFactory.getInstance().setStartNode(this, startNode);
        }

        String endNode = endNodeText.getText().toString();
        if (!TextUtils.isEmpty(endNode)) {
            BNDemoFactory.getInstance().setEndNode(this, endNode);
        }
        String uid = endUid.getText().toString();
        BNDemoFactory.getInstance().setEndUid(this, uid);
        String testEnvironmentUrl = testEnvironment.getText().toString();
        BNDemoFactory.getInstance().setTestEnvironmentUrl(this, testEnvironmentUrl);
    }
}
