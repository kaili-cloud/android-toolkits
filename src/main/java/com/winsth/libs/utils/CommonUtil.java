package com.winsth.libs.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextPaint;
import android.widget.TextView;

public class CommonUtil {
    private CommonUtil() {
    }

    /**
     * 获取类名称
     *
     * @param e 异常类对象(eg:new Exception())
     * @return 返回类名称
     */
    public static String getCName(Exception e) {
        return e.getStackTrace()[0].getClassName();
    }

    /**
     * 获取类中方法的名称
     *
     * @param e 异常类对象(eg:new Exception())
     * @return 返回类中方法的名称
     */
    public static String getMName(Exception e) {
        return e.getStackTrace()[0].getMethodName();
    }

    /**
     * 获取IMEI
     *
     * @param context 上下文环境
     * @return 返回IMEI
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }

            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = mTelephony.getImei();
                } else {
                    deviceId = mTelephony.getDeviceId();
                }
            } else {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }

        return deviceId;
    }

    /**
     * 拨打电话
     *
     * @param activity 当前Activity
     * @param phoneNO  电话号码
     */
    public static void dial(Activity activity, String phoneNO) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNO));
        activity.startActivity(intent);
    }

    /**
     * 发送短信
     *
     * @param activity 当前Activity
     * @param to       短信接收者
     * @param content  短信内容
     */

    public static void sendSMS(Activity activity, String to, String content) {
        Uri smsToUri = Uri.parse("smsto:" + to);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", content);
        activity.startActivity(intent);
    }

    /**
     * 打开网址通过浏览器
     *
     * @param context 上下文
     * @param url     网址
     */
    public static void openURLByBrowser(Context context, String url) {
        if (!url.trim().equalsIgnoreCase("") && url != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
    }

    /**
     * 设置TextView的字体加粗
     *
     * @param textView TextView组件
     * @param text     要显示的文字
     */
    public static void setBoldTextForTextView(TextView textView, String text) {
        TextPaint textPaint = textView.getPaint();
        textPaint.setFakeBoldText(true);
        textView.setText(text);
    }
}
