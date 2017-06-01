/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xys.libzxing.zxing.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.gyzx.zxing.R;
import com.xys.libzxing.zxing.camera.CameraManager;
import com.xys.libzxing.zxing.decode.DecodeThread;
import com.xys.libzxing.zxing.utils.BeepManager;
import com.xys.libzxing.zxing.utils.CaptureActivityHandler;
import com.xys.libzxing.zxing.utils.InactivityTimer;
import com.xys.libzxing.zxing.view.NormalAlertDialog;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;


/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private SurfaceView scanPreview = null;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;

    private Rect mCropRect = null;
    private boolean isHasSurface = false;

    private Button bt_cancle, bt_xiangce;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    private int Mode = 0;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Mode = getIntent().getExtras().getInt("Mode", 0);
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_zxing_capture);


        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        bt_xiangce = (Button) findViewById(R.id.bt_xiangce);
        bt_cancle = (Button) findViewById(R.id.bt_cancle);
        bt_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CaptureActivity.this.finish();
            }
        });
        bt_xiangce.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
//				if(android.os.Build.VERSION.SDK_INT < 19){
//					intent1.setAction(Intent.ACTION_GET_CONTENT);
//				}else{
//					intent1.setAction(Intent.ACTION_OPEN_DOCUMENT);
//				}
                intent1.setAction(Intent.ACTION_PICK);

                intent1.setType("image/*");

                Intent intent2 = Intent.createChooser(intent1, "选择二维码图片");
                startActivityForResult(intent2, 100);
            }
        });


        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath = null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 100:
                    String[] proj = new String[]{MediaStore.Images.Media.DATA};
                    Cursor cursor = CaptureActivity.this.getContentResolver().query(data.getData(), proj, null, null, null);
                    if (cursor == null) {
                        Toast.makeText(CaptureActivity.this, "解析出错...请重新选择...", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        System.out.println(columnIndex);
                        imgPath = cursor.getString(columnIndex);
                    }
                    cursor.close();

                    Result ret = parseQRcodeBitmap(imgPath);

                    if (ret != null) {
                        openBrowser(ret.toString());
                    } else {
                        Toast.makeText(CaptureActivity.this, "解析出错...请重新选择...", Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    break;
            }
        }

    }

    NormalAlertDialog normalAlertDialog = null;

    private void openBrowser(final String url) {
        if (Mode == 0) {
            if (url != null && url.startsWith("http")) {
                Uri uri = Uri.parse(url.toString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                CaptureActivity.this.finish();
            } else {
                if (normalAlertDialog == null) {
                    normalAlertDialog = new NormalAlertDialog(this);
                    normalAlertDialog.setCancelable(false);
                }
                normalAlertDialog.showMyDialog("扫描结果：", "" + url, "了解", "复制", new NormalAlertDialog.DialogClick() {
                    @Override
                    public void firstClick() {
                        finish();
                    }

                    @Override
                    public void secondClick() {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setText(url);
                        Toast.makeText(CaptureActivity.this, "复制成功...", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
//            Log.i("huzeliang","扫描结果" + url);
//            Toast.makeText(this, "扫描结果:" + url, Toast.LENGTH_LONG).show();
            }
        } else {
            Intent intent = new Intent();
            intent.putExtra("url", url);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private com.google.zxing.Result parseQRcodeBitmap(String bitmapPath) {
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        /**
         options.outHeight = 400;
         options.outWidth = 400;
         options.inJustDecodeBounds = false;
         bitmap = BitmapFactory.decodeFile(bitmapPath, options);
         */
        options.inSampleSize = options.outHeight / 400;
        if (options.inSampleSize <= 0) {
            options.inSampleSize = 1;
        }
        /**
         *
         * options.inPreferredConfig = Bitmap.Config.ARGB_4444;    //
         * options.inPurgeable = true;
         * options.inInputShareable = true;
         */
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {
        }

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(scanPreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            scanPreview.getHolder().addCallback(this);
        }

        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param bundle    The extras
     */
    public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();

        openBrowser(rawResult.getText());
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }

            initCrop();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Camera error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 鍒濆鍖栨埅鍙栫殑鐭╁舰鍖哄煙
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        Log.i("huzeliang", cameraWidth + ":" + cameraHeight + ":" + cropLeft + ":" + cropTop + ":" + cropWidth + ":" + cropHeight);

        int x = (int) (cropLeft * (float) cameraWidth / (float) containerWidth / 1.1);
        int y = (int) (cropTop * (float) cameraHeight / (float) containerHeight / 1.1);

        int width = (int) (1.1 * cropWidth * (float) cameraWidth / (float) containerWidth);
        int height = (int) (1.1 * cropHeight * (float) cameraHeight / (float) containerHeight);

        Log.i("huzeliang", x + ":" + y + ":" + (width + x) + ":" + (height + y));
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}