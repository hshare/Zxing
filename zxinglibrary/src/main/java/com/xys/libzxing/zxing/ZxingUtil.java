package com.xys.libzxing.zxing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.gyzx.zxing.R;
import com.xys.libzxing.zxing.activity.CaptureActivity;

/**
 * 功能/模块 ：扫码工具类
 *
 * @author huzeliang
 * @version 1.0 2017-6-1 12:51:36
 * @see ***
 * @since ***
 * 需要如下权限，自己处理6.0权限问题
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.CAMERA" />
 * <uses-permission android:name="android.permission.VIBRATE" />
 */
public class ZxingUtil {
    /**
     * 启动扫码界面
     * @param context 上下文
     */
    public static void gotoCaptureActivity(Context context) {
        Intent intent = new Intent(context, CaptureActivity.class);
        context.startActivity(intent);
        setGotoAnim(context);
    }

    /**
     * 启动扫码功能，并返回结果
     * @param context 上下文
     * @param requestCode 请求码
     */
    public static void gotoCaptureActivityForResult(Activity context, int requestCode) {

        Intent intent = new Intent(context, CaptureActivity.class);
        intent.putExtra("Mode", 1);
        context.startActivityForResult(intent, requestCode);
        setGotoAnim(context);
    }

    /**
     * 动画
     * @param context 上下文
     */
    private static void setGotoAnim(Context context) {
        ((Activity) context).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * 参考代码
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 1111:
                if (data != null && data.getExtras() != null) {
                    String url = data.getExtras().getString("url", "");
                    if (!TextUtils.isEmpty(url)) {
                        SnackBarUtils.makeLong(myToolbar, "扫描成功，正在从服务器获取您的个人信息...").success();
                        loadData(url);
                    } else {
                        SnackBarUtils.makeLong(myToolbar, "扫描失败...").danger();
                        handlerData(null);
                    }
                }
                break;
            default:
                break;
        }
    }*/
}
