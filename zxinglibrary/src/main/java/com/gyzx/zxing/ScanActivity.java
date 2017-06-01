package com.gyzx.zxing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.xys.libzxing.zxing.activity.CaptureActivity;


public class ScanActivity extends Activity {

	private TextView resultTextView;
	private EditText qrStrEditText;
	private ImageView qrImgImageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zxing_scan);


		resultTextView = (TextView) this.findViewById(R.id.tv_scan_result);
		qrStrEditText = (EditText) this.findViewById(R.id.et_qr_string);
		qrImgImageView = (ImageView) this.findViewById(R.id.iv_qr_image);
		Button scanBarCodeButton = (Button) this.findViewById(R.id.btn_scan_barcode);
		scanBarCodeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// ��ɨ�����ɨ����������ά��
				Intent openCameraIntent = new Intent(ScanActivity.this, CaptureActivity.class);
				startActivityForResult(openCameraIntent, 0);
			}
		});

		Button generateQRCodeButton = (Button) this.findViewById(R.id.btn_add_qrcode);
		generateQRCodeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				String contentString = qrStrEditText.getText().toString();
//				if (!contentString.equals("")) {
//					// �����ַ������ɶ�ά��ͼƬ����ʾ�ڽ����ϣ��ڶ�������ΪͼƬ�Ĵ�С��350*350��
//					Bitmap qrCodeBitmap = EncodingUtils.createQRCode(contentString, 350, 350,
//							mCheckBox.isChecked() ? BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher) : null);
//					qrImgImageView.setImageBitmap(qrCodeBitmap);
//				} else {
//					Toast.makeText(ScanActivity.this, "Text can not be empty", Toast.LENGTH_SHORT).show();
//				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			resultTextView.setText(scanResult);
		}
	}
}