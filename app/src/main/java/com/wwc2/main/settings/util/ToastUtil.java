package com.wwc2.main.settings.util;

/**
 * Created by swd1 on 17-7-26.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

public class ToastUtil {
    private static Toast mToast;
    private static final String TAG = "ToastUtil";


    //短时间吐司
    public static void show(Context context, int resourceID) {
        show(context, resourceID, Toast.LENGTH_SHORT);
    }

    //短时间吐司
    public static void show(Context context, String text) {
        show(context, text, Toast.LENGTH_SHORT);
    }

    //自定义时长吐司
    public static void show(Context context, Integer resourceID, int duration) {
        String text = context.getApplicationContext().getString(resourceID);// 用于显示的文字
        show(context, text, duration);
    }

    //自定义时长吐司
    public static void show(@NonNull final Context context, @NonNull final String text, final int duration) {

        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), text, duration);
        } else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }

        mToast.show();
    }

    /**
     * 隐藏toast
     */
    public static void hideToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}