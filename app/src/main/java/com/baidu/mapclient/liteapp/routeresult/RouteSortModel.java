package com.baidu.mapclient.liteapp.routeresult;

import com.baidu.mapclient.liteapp.R;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;

/**
 * Created by duanpeifeng.
 */
public class RouteSortModel {
    public String mItemName = null;
    public int mPreferValue = 0;

    public RouteSortModel(String itemName, int preferValue) {
        mItemName = itemName;
        mPreferValue = preferValue;
    }

    public int getPreferIconId(boolean isSelected) {
        int drawableId;
        switch (mPreferValue) {
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_TIME_FIRST:
                drawableId = isSelected ? R.drawable.nsdk_drawable_route_sort_time_first_selected :
                        R.drawable.nsdk_drawable_route_sort_time_first_normal;
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DISTANCE_FIRST:
                drawableId = isSelected ? R.drawable.nsdk_drawable_route_sort_distance_first_selected :
                        R.drawable.nsdk_drawable_route_sort_distance_first_normal;
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_AVOID_TRAFFIC_JAM:
                drawableId = isSelected ? R.drawable.nsdk_drawable_route_sort_avoid_traffic_jam_selected :
                        R.drawable.nsdk_drawable_route_sort_avoid_traffic_jam_normal;
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_NOHIGHWAY:
                drawableId = isSelected ? R.drawable.nsdk_drawable_route_sort_nohighway_selected :
                        R.drawable.nsdk_drawable_route_sort_nohighway_normal;
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_ROAD_FIRST:
                drawableId = isSelected ? R.drawable.nsdk_drawable_route_sort_road_first_selected :
                        R.drawable.nsdk_drawable_route_sort_road_first_normal;
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_NOTOLL:
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_ECONOMIC_ROUTE:
                drawableId = isSelected ? R.drawable.nsdk_drawable_route_sort_notoll_selected :
                        R.drawable.nsdk_drawable_route_sort_notoll_normal;
                break;
            default:
                drawableId = isSelected ? R.drawable.nsdk_drawable_route_sort_default_selected :
                        R.drawable.nsdk_drawable_route_sort_default_normal;
                break;
        }
        return drawableId;
    }
}
