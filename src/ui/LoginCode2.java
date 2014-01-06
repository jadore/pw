package ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.CookieStore;

import bean.CodeEntity;
import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.loopj.android.http.PersistentCookieStore;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.QYRestClient;
import config.AppClient.ClientCallback;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import tools.AppException;
import tools.AppManager;
import tools.BaseActivity;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

public class LoginCode2 extends AppActivity{
	private TextView contentView;
	private TextView tipView;
	private EditText codeET;
	private CountDown cd;
	int leftSeconds;
	private ProgressDialog loadingPd;
	private String mobile;
	private InputMethodManager imm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_code2);
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initUI();
		initData();
	}
	
	private void initUI() {
		Button leftBarButton = (Button) findViewById(R.id.leftBarButton);
		accretionArea(leftBarButton);
		Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
		accretionArea(rightBarButton);
		contentView = (TextView) findViewById(R.id.contentView);
		tipView = (TextView) findViewById(R.id.tipView);
		codeET = (EditText) findViewById(R.id.editTextCode);
	}
	
	private void initData() {
		String content = getIntent().getStringExtra("content");
		mobile = getIntent().getStringExtra("mobile");
		contentView.setText(content);
		cd = new CountDown(60*1000, 1000);
		cd.start();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			imm.hideSoftInputFromWindow(codeET.getWindowToken(), 0);
			vertifiedCode();
			break;
		case R.id.tipView:
			getVertifyCode(mobile);
			break;
		}
	}
	
	private void getVertifyCode(final String mobile) {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		cd.cancel();
		cd.start();
		AppClient.getVertifyCode(appContext, mobile, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				CodeEntity code = (CodeEntity)data;
				switch (code.getError_code()) {
				case Result.RESULT_OK:
					contentView.setText(code.info);
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), code.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void vertifiedCode() {
		Pattern regex = Pattern.compile("^([0-9]{6})$");
		Matcher matcher = regex.matcher(codeET.getText().toString());
		if (matcher.find()) {
			vertifiedCode(matcher.group(1));
		}
		else {
			UIHelper.ToastMessage(getApplicationContext(), "请输入6位验证码", Toast.LENGTH_SHORT);
		}
	}
	
	private void vertifiedCode(final String code) {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.vertifiedCode(appContext, code, mobile, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				UserEntity user = (UserEntity) data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					appContext.saveLoginInfo(user);
					enterIndex();
					break;
				default:
					UIHelper.ToastMessage(LoginCode2.this, user.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(LoginCode2.this, message, Toast.LENGTH_SHORT);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void enterIndex() {
		Intent intent = new Intent(this, Tabbar.class);
		startActivity(intent);
		setResult(RESULT_OK);
		AppManager.getAppManager().finishActivity(this);
	}
	
	class CountDown extends CountDownTimer {

		public CountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			tipView.setEnabled(true);
			tipView.setText(R.string.code_retake);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			leftSeconds = (int) (millisUntilFinished/1000);
			String strTime = String.format("<u>接收短信大约需要 <a href=\"http://pb.wcl.m0.hk/book/109\">%d</a> 秒</u>", leftSeconds);
			tipView.setText(Html.fromHtml(strTime));
			tipView.setMovementMethod(LinkMovementMethod.getInstance());
	        CharSequence text = tipView.getText();
	        if (text instanceof Spannable) {
	            int end = text.length();
	            Spannable sp = (Spannable) tipView.getText();
	            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
	            SpannableStringBuilder style = new SpannableStringBuilder(text);
	            style.clearSpans();
	            for (URLSpan url : urls) {
	            	NoLineClickSpan myURLSpan = new NoLineClickSpan(url.getURL());
	                style.setSpan(myURLSpan, sp.getSpanStart(url),
	                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
	            tipView.setText(style);
	        }
			tipView.setEnabled(false);
		}
	}
	
	private class NoLineClickSpan extends ClickableSpan { 
	    String text;

	    public NoLineClickSpan(String text) {
	        super();
	        this.text = text;
	    }

	    @Override
	    public void updateDrawState(TextPaint ds) {
	        ds.setColor(getResources().getColor(R.color.red));
	        ds.setTextSize(30);
	        ds.setUnderlineText(true);
	    }

		@Override
		public void onClick(View arg0) {
		}
	}
}
