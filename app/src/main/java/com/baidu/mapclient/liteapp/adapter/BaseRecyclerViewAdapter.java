package com.baidu.mapclient.liteapp.adapter;

/**
 * BaseRecyclerViewAdapter是用来干什么的
 *
 * @author wangyao
 * @email 382708580@qq.com
 * @create 2025/4/8
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * BaseRecyclerViewAdapter
 * 静态内部类 BaseViewHolder
 * SparseArray 缓存view
 * 范型获取自定义类型view
 */
public class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<BaseRecyclerViewAdapter.BaseViewHolder> {
    public interface HolderCallback {
        void callback(BaseViewHolder holder, int position, Context context);
    }

    private List<T> watches;
    private final int layoutResId;
    private final Context context;
    private final HolderCallback callback;

    public BaseRecyclerViewAdapter(List<T> watches, Context context, @LayoutRes int layoutResId, HolderCallback callback) {
        this.watches = watches;
        this.context = context;
        this.layoutResId = layoutResId;
        this.callback = callback;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setWatches(List<T> watches, int start) {
        this.watches = watches;
        if (start == 0) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BaseViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        if (callback != null) callback.callback(holder, position, context);
    }

    @Override
    public int getItemCount() {
        return watches.size();
    }

    /**
     * 静态内部类 BaseViewHolder
     */
    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        private final SparseArray<View> mViews;

        @SuppressWarnings("unchecked")
        public <T extends View> T getView(@IdRes int id) {
            View view = mViews.get(id);
            if (view == null) {
                view = itemView.findViewById(id);
                mViews.put(view.getId(), view);
            }
            return (T) view;
        }

        public BaseViewHolder(View itemView) {
            super(itemView);
            this.mViews = new SparseArray<>();
        }
    }
}
