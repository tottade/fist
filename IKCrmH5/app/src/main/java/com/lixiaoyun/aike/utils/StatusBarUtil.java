package com.lixiaoyun.aike.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.lixiaoyun.aike.R;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TargetApi(21)
public class StatusBarUtil {

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param colorId 颜色
     */
    private static void setStatusBarColor(Activity activity, int colorId) {
        Window window = activity.getWindow();
        window.setStatusBarColor(colorId);
    }

    /**
     * 设置状态栏透明
     */
    private static void setTranslucentStatus(Activity activity) {
        Window window = activity.getWindow();
        //清除透明状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //设置状态栏颜色必须添加
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置透明
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 设置沉浸式状态栏
     *
     * @param fontIconDark 状态栏字体和图标颜色是否为深色
     */
    @TargetApi(21)
    public static void setImmersiveStatusBar(Activity activity, View view, boolean fontIconDark, boolean statusBarDark, int color) {
        setTranslucentStatus(activity);
        if (fontIconDark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setStatusBarFontIconDark(activity, "common");
            } else if (Rom.isMiui()) {
                setStatusBarFontIconDark(activity, "miui");
            } else if (Rom.isFlyme()) {
                setStatusBarFontIconDark(activity, "flyme");
            } else {
                //其他情况下我们将状态栏设置为灰色，就不会看不见字体
                setStatusBarColor(activity, Color.LTGRAY);
            }
        }

        if (statusBarDark) {
            setStatusBarColor(activity, activity.getResources().getColor(R.color.colorPrimary));
        }

        setTopPadding(activity, view, color);
    }

    private static void setTopPadding(Activity activity, View view, int color) {
        int statusBarHeight;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        } else {
            statusBarHeight = 0;
        }
        if (color != 0) {
            view.setBackgroundColor(color);
        }
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + statusBarHeight, view.getPaddingRight(), view.getPaddingBottom());
    }

    /**
     * 设置文字颜色
     */
    private static void setStatusBarFontIconDark(Activity activity, String type) {
        switch (type) {
            case "miui":
                setMiuiUI(activity, true);
                break;
            case "common":
                setCommonUI(activity);
                break;
            case "flyme":
                setFlymeUI(activity, true);
                break;
        }
    }

    //设置6.0的字体
    private static void setCommonUI(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    //设置Flyme的字体
    public static void setFlymeUI(Activity activity, boolean dark) {
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置MIUI字体
    private static void setMiuiUI(Activity activity, boolean dark) {
        try {
            Window window = activity.getWindow();
            Class clazz = activity.getWindow().getClass();
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}