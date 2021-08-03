package com.winsth.libs.bases;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.winsth.libs.R;
import com.winsth.libs.utils.CommonUtil;
import com.winsth.libs.utils.ConfigUtil;
import com.winsth.libs.utils.DialogUtil;
import com.winsth.libs.utils.HttpUtil;
import com.winsth.libs.utils.LogUtil;
import com.winsth.libs.utils.StringUtil;
import com.winsth.libs.utils.singleton.AppBaseUtil;
import com.winsth.libs.utils.singleton.OKHttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aaron.zhao on 2016/4/5.
 */
public class AppUpdate {
    private Context mContext;

    private String updateMsg = "";
    private String updateTitle = "";
    private String downloadTitle = "";

    private String mChkVerURL = "";
    private String mFileName = "";
    private String mSavePath = "";
    private String mDownloadURL = "";
    private String mPackageName = "";

    private ProgressBar mProgressBar;
    private int progress;
    private Thread checkThread;
    private Thread downloadThread;
    private boolean interceptFlag = false;
    private boolean isNOUpdateDisplay = false;

    private static final int DOWNLOAD_UPDATE = 1;
    private static final int DOWNLOAD_OVER = 2;
    private static final int NEW_UPDATE = 3;
    private static final int NO_UPDATE = 4;

    private AlertDialog mProgressDialog = null;

    public AppUpdate(Context context, String chkVerURL, String savePath, String downloadURL) {
        this.mContext = context;
        this.mChkVerURL = chkVerURL;
        this.mSavePath = savePath;
        this.mDownloadURL = downloadURL;

        updateTitle = mContext.getResources().getString(R.string.update_title);
        updateMsg = mContext.getResources().getString(R.string.update_msg);
        downloadTitle = mContext.getResources().getString(R.string.download_title);
    }

    public AppUpdate(Context context, String chkVerURL, String savePath, String downloadURL, String packageName) {
        this.mContext = context;
        this.mChkVerURL = chkVerURL;
        this.mSavePath = savePath;
        this.mDownloadURL = downloadURL;
        this.mPackageName = packageName;

        updateTitle = mContext.getResources().getString(R.string.update_title);
        updateMsg = mContext.getResources().getString(R.string.update_msg);
        downloadTitle = mContext.getResources().getString(R.string.download_title);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_UPDATE:
                    mProgressBar.setProgress(progress);
                    break;
                case DOWNLOAD_OVER:
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    installAPK();
                    break;
                case NEW_UPDATE:
                    mFileName = (String) msg.obj;
                    String strDownload = mContext.getResources().getString(R.string.button_download);
                    String strIgnore = mContext.getResources().getString(R.string.button_ignore);
                    DialogUtil.showAlertDialog(mContext, updateTitle, updateMsg, strDownload, strIgnore, downloadListener, cancelUpdateListener);
//                    DialogUtil.showAlertDialog(mContext, updateTitle, updateMsg, strDownload, "", downloadListener, null);
                    break;
                case NO_UPDATE:
                    if (isNOUpdateDisplay) {
                        DialogUtil.showToast(mContext, mContext.getResources().getString(R.string.update_no));
                    }
                    break;
            }
            return false;
        }
    });

    private DialogInterface.OnClickListener downloadListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            // 显示下载框
            showDownloadDialog();
        }
    };

    private DialogInterface.OnClickListener cancelUpdateListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    public void checkUpdateInfo(boolean isNOUpdateDisplay) {
        this.isNOUpdateDisplay = isNOUpdateDisplay;
        checkThread = new Thread(mCheckRunnable);
        checkThread.start();
    }

    private DialogInterface.OnClickListener cancelDownloadListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            interceptFlag = true;
        }
    };

    private void showDownloadDialog() {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.dialog_update_progress_bar, null);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress);
        String strCancel = mContext.getResources().getString(R.string.button_cancel);
        mProgressDialog = DialogUtil.showAlertDialog(mContext, downloadTitle, v, strCancel, cancelDownloadListener);

        downloadAPK();
    }

    private Runnable mCheckRunnable = new Runnable() {
        public void run() {
            String result = OKHttpUtil.getInstance().requestStringByGet(mChkVerURL, null, ConfigUtil.TextCode.UTF_8);
            if (!StringUtil.isNullOrEmpty(result)) {
                JSONObject jsonObject = JSON.parseObject(result);
                if (jsonObject == null || jsonObject.isEmpty()) {
                    mHandler.sendMessage(Message.obtain(mHandler, NO_UPDATE, result));
                } else {
                    String returnCode = jsonObject.getString("returnCode");
                    String returnMsg = jsonObject.getString("returnMsg");
                    JSONObject subJSONObject = jsonObject.getJSONObject("returnData");
                    if (returnCode.equals("200")) {
                        String fileName = subJSONObject.getString("fileName");
                        mHandler.sendMessage(Message.obtain(mHandler, NEW_UPDATE, fileName));
                    } else {
                        mHandler.sendMessage(Message.obtain(mHandler, NO_UPDATE, result));
                    }
                }
            } else {
                mHandler.sendMessage(Message.obtain(mHandler, NO_UPDATE, result));
            }
        }
    };

    private Runnable mDownloadRunnable = new Runnable() {
        public void run() {
            FileOutputStream fileOutputStream = null;
            InputStream inputStram = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(String.format(mDownloadURL, mFileName));
                con = (HttpURLConnection) url.openConnection();
                con.connect();

                int length = con.getContentLength();
                inputStram = con.getInputStream();

                File file = new File(mSavePath);
                if (!file.exists()) {
                    file.mkdir();
                }

                File ApkFile = new File(mSavePath, mFileName);
                fileOutputStream = new FileOutputStream(ApkFile);
                int count = 0;

                byte[] buffer = new byte[1024];

                do {
                    int numread = inputStram.read(buffer);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    mHandler.sendEmptyMessage(DOWNLOAD_UPDATE);
                    if (numread <= 0) {
                        mHandler.sendEmptyMessage(DOWNLOAD_OVER);
                        break;
                    }
                    fileOutputStream.write(buffer, 0, numread);
                } while (!interceptFlag);
            } catch (MalformedURLException e) {
                LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new Exception()), ":MalformedURLException" + e.getMessage(),
                        true);
            } catch (IOException e) {
                LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new Exception()), ":IOException" + e.getMessage(), true);
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (inputStram != null) {
                        inputStram.close();
                    }
                    if (con != null) {
                        con.disconnect();
                    }
                } catch (IOException e) {
                    LogUtil.exportLog(CommonUtil.getCName(new Exception()), CommonUtil.getMName(new Exception()), ":IOException" + e.getMessage(), true);
                }

            }
        }
    };

    private void downloadAPK() {
        downloadThread = new Thread(mDownloadRunnable);
        downloadThread.start();
    }

    private void installAPK() {
        File apkFile = new File(mSavePath, mFileName);
        if (!apkFile.exists()) {
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(FileProvider.getUriForFile(mContext, mPackageName + ".fileprovider", apkFile), "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);

        /*Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(AppBaseUtil.getInstance().getUriOfFile(mContext, apkFile), "application/vnd.android.package-archive");
        if (mContext.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            mContext.startActivity(intent);
        }*/
    }
}
