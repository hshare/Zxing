package com.xys.libzxing.zxing.view;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.gyzx.zxing.R;


/**
* 功能/模块 ：通用弹框界面
* @author 胡泽亮
* @date 2016/09/12
* @version 1.0 
* 类描述 ：通用弹框界面
* 修订历史：
* 日期  作者  参考  描述
* see 相关类连接
*/
public class NormalAlertDialog {

	private AlertDialog myDialog;
	private Context context;
	private Button btn_cancel;
	private Button bt_sure11;
	private TextView tv_edit_title_one;
	private TextView tv_edit_title_two;

	private boolean isDismiss = true;


	public void setDismiss(boolean is){
		isDismiss = is;
	}

	public NormalAlertDialog(Context context) {
		this.context = context;
		initDialog();
	}

	public void setCancelable(boolean cancle) {
		myDialog.setCancelable(cancle);
	}

	private View getDialog() {
		View view = LayoutInflater.from(context).inflate(R.layout.bdlib_normal_alert_dialog3, null, false);
		btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		bt_sure11 = (Button) view.findViewById(R.id.bt_sure11);
		tv_edit_title_one = (TextView) view.findViewById(R.id.tv_edit_title_one);
		tv_edit_title_two = (TextView) view.findViewById(R.id.tv_edit_title_two);
		return view;
	}

	@SuppressLint("NewApi")
	private void initDialog() {
		myDialog = new AlertDialog.Builder(context, R.style.bdlib_dialog).create();
		Window window = myDialog.getWindow();
		window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
		window.setWindowAnimations(R.style.dialog_mystyle); // 添加动画
		myDialog.setView(getDialog(), 0, 0, 0, 0);
	}

	public interface DialogClick {
		void firstClick();

		void secondClick();
	}
	
	public void dismiss(){
		if (myDialog != null && myDialog.isShowing()) {
			myDialog.dismiss();
			myDialog = null ;
		}
	}

	public void showMyDialog(String title_one, String title_two, String leftStr, String rightStr, final DialogClick dialogClick) {
		if (title_one != null) {
			tv_edit_title_one.setText(title_one);
		}
		if (title_two != null) {
			tv_edit_title_two.setText(title_two);
		} else {
			tv_edit_title_two.setVisibility(View.GONE);
			tv_edit_title_one.setPadding(dip2px(context, 20), dip2px(context, 25), dip2px(context, 20), dip2px(context, 25));
		}
		if (leftStr != null) {
			btn_cancel.setText(leftStr);
		} else {
			btn_cancel.setVisibility(View.GONE);
		}
		if (rightStr != null) {
			bt_sure11.setText(rightStr);
		} else {
			bt_sure11.setVisibility(View.GONE);
		}

		btn_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isDismiss){
					myDialog.dismiss();
				}
				dialogClick.firstClick();
			}
		});
		bt_sure11.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isDismiss){
					myDialog.dismiss();
				}
				dialogClick.secondClick();
			}
		});
		myDialog.show();
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
