/*
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp.routeresult;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapclient.liteapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 偏好 item adapter
 *
 * @author duanpeifeng
 */
public class PreferItemsAdapter extends RecyclerView.Adapter<PreferItemsAdapter.ViewHolder> {

    public static final String TAG = "BNPreferItemsAdapter";
    public static int numColumns = 3;

    private ClickPreferListener mClickPreferListener;
    private Context context;
    private List<RouteSortModel> routeSortList;
    // 当前偏好
    private int mCurrentPreferValue = 1;

    public PreferItemsAdapter(Context context, ArrayList<RouteSortModel> sortModels) {
        this.context = context;
        this.routeSortList = sortModels;
    }

    public void updatePrefer(int currentPrefer) {
        this.mCurrentPreferValue = currentPrefer;
    }

    public void setClickPreferListener(ClickPreferListener listener) {
        mClickPreferListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prefer, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == null) {
                    return;
                }
                if (routeSortList == null) {
                    return;
                }

                int pos = holder.getAdapterPosition();
                if (pos < 0) {
                    return;
                }

                RouteSortModel model = routeSortList.get(pos);
                if (model == null) {
                    return;
                }
                mCurrentPreferValue = model.mPreferValue;
                if (mClickPreferListener != null) {
                    mClickPreferListener.onClickPrefer(model.mPreferValue);
                }
            }
        });

        if ((position + 1) % numColumns == 0) {
            holder.mVerticalDivider.setVisibility(View.INVISIBLE);
        } else {
            holder.mVerticalDivider.setVisibility(View.VISIBLE);
        }

        if (position >= numColumns) {
            holder.mHorizontalDivider.setVisibility(View.INVISIBLE);
        } else {
            holder.mHorizontalDivider.setVisibility(View.VISIBLE);
        }

        holder.mVerticalDivider.setBackgroundColor(getColor(R.color.nsdk_cl_bg_d_mm));
        holder.mHorizontalDivider.setBackgroundColor(getColor(R.color.nsdk_cl_bg_d_mm));
        holder.itemView.setBackgroundDrawable(getDrawable(R.drawable.bnav_bt_pressed_bg));

        if (routeSortList == null) {
            return;
        }

        if (position >= 0 && position < routeSortList.size()) {
            RouteSortModel model = routeSortList.get(position);

            if (model == null) {
                return;
            }

            holder.mItemName.setText(model.mItemName);

            Drawable topDrawable = null;
            if ((model.mPreferValue & mCurrentPreferValue) != 0) {
                // 当前选中偏好
                holder.mItemName.setTextColor(getColor(R.color.nsdk_route_sort_setting_default));
                int drawableId = model.getPreferIconId(true);
                topDrawable = getDrawable(drawableId);
                holder.mItemName
                        .setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, null);
            } else {
                holder.mItemName.setTextColor(getColor(R.color.nsdk_route_sort_item_text));
                int drawableId = model.getPreferIconId(false);
                topDrawable = getDrawable(drawableId);
                holder.mItemName
                        .setCompoundDrawablesWithIntrinsicBounds(null, topDrawable, null, null);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (routeSortList == null) {
            return 0;
        }
        return routeSortList.size();
    }

    private Drawable getDrawable(int resId) {
        return context.getResources().getDrawable(resId);
    }

    private int getColor(int resId) {
        return context.getResources().getColor(resId);
    }

    public void onDestroy() {
        mClickPreferListener = null;
        context = null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View mVerticalDivider;
        View mHorizontalDivider;
        TextView mItemName;

        public ViewHolder(View itemView) {
            super(itemView);
            mItemName = itemView.findViewById(R.id.nsdk_route_sort_item_tv);
            mVerticalDivider = itemView.findViewById(R.id.nsdk_route_sort_item_divider_vertical);
            mHorizontalDivider = itemView.findViewById(R.id.nsdk_route_sort_item_divider_bottom);
        }
    }

    public interface ClickPreferListener {
        void onClickPrefer(int clickPrefer);
    }
}