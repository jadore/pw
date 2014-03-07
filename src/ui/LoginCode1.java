package ui;

import bean.CodeEntity;
import bean.Entity;
import bean.Result;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import tools.AppException;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

public class LoginCode1 extends AppActivity{
	private EditText mobileET;
//	private CountDown cd;
	boolean canVertify ;
	int leftSeconds;
	private ProgressDialog loadingPd;
	private TextView barTitle;
	private TextView regTV;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.LoginRequest.LoginMobile:
			AppManager.getAppManager().finishActivity(this);
			break;
		case CommonValue.LoginRequest.LoginWechat:
		case CommonValue.LoginRequest.Register:
			AppManager.getAppManager().finishActivity(this);
			break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_code1);
		initUI();
		initData();
	}
	
	private void initUI() {
		barTitle = (TextView) findViewById(R.id.barTitle);
		regTV = (TextView) findViewById(R.id.textView2);
		regTV.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
		accretionArea(rightBarButton);
		mobileET = (EditText) findViewById(R.id.editTextPhone);
	}
	
	private void initData() {
		canVertify = true;
//		cd = new CountDown(60*1000, 1000);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.rightBarButton:
			this.getVertifyCode();
			break;

		case R.id.textView1:
			this.wechat();
			break;
		case R.id.textView2:
			register();
			break;
		}
	}
	
	private void getVertifyCode(final String mobile)  {
		loadingPd = UIHelper.showProgress(this, null, null, true);
//		cd.cancel();
//		cd.start();
		AppClient.getVertifyCode(appContext, mobile, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				CodeEntity code = (CodeEntity)data;
				switch (code.getError_code()) {
				case Result.RESULT_OK:
//					cd.cancel();
					canVertify = true;
					step2(code.info);
					break;
				default:
//					cd.cancel();
					canVertify = true;
					UIHelper.ToastMessage(getApplicationContext(), code.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
//				cd.cancel();
				canVertify = true;
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			
			@Override
			public void onError(Exception e) {
//				cd.cancel();
				canVertify = true;
				UIHelper.dismissProgress(loadingPd);
				Logger.i(e);
			}
		});
	}
	
	private void getVertifyCode() {
		if (StringUtils.isMobileNO(mobileET.getText().toString())) {
			if (canVertify) {
				getVertifyCode(mobileET.getText().toString());
			}
			else {
				UIHelper.ToastMessage(getApplicationContext(), String.format("还需要%d秒获取验证码", leftSeconds), Toast.LENGTH_SHORT);
			}
		}
		else {
			UIHelper.ToastMessage(getApplicationContext(), R.layout.toastmessage_text, "请输入正确的手机号码", Toast.LENGTH_SHORT);
		}
	}
	
	private void step2(String content) {
		Intent intent = new Intent(LoginCode1.this, LoginCode2.class);
		intent.putExtra("content", content);
		intent.putExtra("mobile", mobileET.getText().toString());
		startActivityForResult(intent, CommonValue.LoginRequest.LoginMobile);
	}
	
	private void wechat() {
		Intent intent = new Intent(LoginCode1.this, LoginWechat.class);
		startActivityForResult(intent, CommonValue.LoginRequest.LoginWechat);
	}
	
	private void register() {
		if (regTV.getText().toString().equals(getResources().getString(R.string.register))) {
			regTV.setText(getResources().getString(R.string.mobile_login));
			barTitle.setText("手机号码注册");
		}
		else {
			regTV.setText(getResources().getString(R.string.register));
			barTitle.setText("手机号码登录");
		}
	}
	
//	class CountDown extends CountDownTimer {
//
//		public CountDown(long millisInFuture, long countDownInterval) {
//			super(millisInFuture, countDownInterval);
//		}
//
//		@Override
//		public void onFinish() {
//			canVertify = true;
//		}
//
//		@Override
//		public void onTick(long millisUntilFinished) {
//			canVertify = false;
//			leftSeconds = (int) (millisUntilFinished/1000);
//		}
//	}
}
