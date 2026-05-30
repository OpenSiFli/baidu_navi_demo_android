package com.baidu.mapclient.liteapp.util;

import android.app.Activity;
import android.content.Context;
import com.kaopiz.kprogresshud.KProgressHUD;
import java.lang.ref.WeakReference;

public class ProgressHUDHelper {
    private static KProgressHUD hud;
    private static WeakReference<Activity> activityRef; // 使用弱引用持有 Activity

    private ProgressHUDHelper() {
    }

    /**
     * 显示加载提示（必须传入当前 Activity）
     * @param activity 当前显示的 Activity
     * @param info 提示文字
     */
    public static void show(Activity activity, String info) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return; // Activity 无效，不显示
        }

        dismiss();
        activityRef = new WeakReference<>(activity);

        hud = KProgressHUD.create(activity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(info)
                .setCancellable(false)
                .setDimAmount(0.5f)
                .show();
    }

    /**
     * 更新提示文字
     */
    public static void updateMessage(String info) {
        if (hud != null && hud.isShowing()) {
            hud.setLabel(info);
        }
    }

    /**
     * 关闭弹窗
     */
    public static void dismiss() {
        if (hud != null && hud.isShowing()) {
            // 检查 Activity 是否还存在
            Activity activity = activityRef != null ? activityRef.get() : null;
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                hud.dismiss();
            }
            hud = null;
        }
    }

    /**
     * 判断是否正在显示
     */
    public static boolean isShowing() {
        return hud != null && hud.isShowing();
    }
}