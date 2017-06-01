package com.hucare.hushare.zxing;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xys.libzxing.zxing.ZxingUtil;
import com.xys.libzxing.zxing.activity.CaptureActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onMyClick(View v) {
        ZxingUtil.gotoCaptureActivity(this);
    }

    public void onMyClick1(View v) {
        ZxingUtil.gotoCaptureActivityForResult(this, 1111);
    }

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
                        Toast.makeText(MainActivity.this, "扫码成功...", Toast.LENGTH_SHORT).show();
                        ((TextView) findViewById(R.id.tv)).setText(url);
                    } else {
                        Toast.makeText(MainActivity.this, "扫码失败...", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }
}
