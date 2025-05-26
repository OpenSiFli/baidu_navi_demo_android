package com.baidu.mapclient.liteapp.routeresult;

import java.util.ArrayList;
import java.util.List;

import android.view.View;

import com.baidu.mapclient.liteapp.R;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;

/**
 * Author: v_duanpeifeng
 * Time: 2020-05-19
 * Description:
 */
public class LongDistanceController implements View.OnClickListener {

    public View rootView;

    public LongDistanceController(View rootView) {
        this.rootView = rootView;
        setView(R.id.city);
        setView(R.id.route);
        setView(R.id.service);
        setView(R.id.checkpoint);
        setView(R.id.weather);
    }

    public void setView(int id) {
        View view = rootView.findViewById(id);
        view.setOnClickListener(this);
        views.add(view);
    };

    List<View> views = new ArrayList<>();

    public void setSelect(View clickView) {
        for (int i = 0; i < views.size(); i++) {
            if (views.get(i).equals(clickView)) {
                views.get(i).setSelected(!views.get(i).isSelected());
            } else {
                views.get(i).setSelected(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        setSelect(v);
        switch (v.getId()) {
            case R.id.city:
                BaiduNaviManagerFactory.getRouteResultManager().handleCityClick(v.isSelected());
                break;
            case R.id.route:
                BaiduNaviManagerFactory.getRouteResultManager().handleRouteClick(v.isSelected());
                break;
            case R.id.service:
                BaiduNaviManagerFactory.getRouteResultManager().handleServiceClick(v.isSelected());
                break;
            case R.id.checkpoint:
                BaiduNaviManagerFactory.getRouteResultManager()
                        .handleCheckpointClick(v.isSelected());
                break;
            case R.id.weather:
                BaiduNaviManagerFactory.getRouteResultManager().handleWeatherClick(v.isSelected());
                break;
            default:
                break;
        }
    }
}
