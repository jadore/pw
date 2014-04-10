package ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import tools.AppException;
import tools.AppManager;
import tools.BaseActivity;
import tools.Logger;
import tools.UIHelper;

public class LoginWechat extends AppActivity{
	private EditText codeET;
	private ProgressDialog loadingPd;
	String b = "请发送【9】到我们<font color=\"#088ec1\">微信公众帐号</font><br>获取6位验证数字";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_wechat);
		this.initUI();
	}
	
	private void initUI() {
		Button leftBarButton = (Button) findViewById(R.id.leftBarButton);
		accretionArea(leftBarButton);
		Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
		accretionArea(rightBarButton);
		TextView textview = (TextView) findViewById(R.id.textview1);
		textview.setText(Html.fromHtml(b));
		codeET = (EditText) findViewById(R.id.editTextCode);
//		textview.setMovementMethod(LinkMovementMethod.getInstance());
//        CharSequence text = textview.getText();
//        if (text instanceof Spannable) {
//            int end = text.length();
//            Spannable sp = (Spannable) textview.getText();
//            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
//            SpannableStringBuilder style = new SpannableStringBuilder(text);
//            style.clearSpans();
//            for (URLSpan url : urls) {
//            	NoLineClickSpan myURLSpan = new NoLineClickSpan(url.getURL());
//                style.setSpan(myURLSpan, sp.getSpanStart(url),
//                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//            textview.setText(style);
//        }
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			vertifiedCode();
			break;
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
	        ds.setColor(getResources().getColor(R.color.nav_color));
	        ds.setUnderlineText(false);
	    }

		@Override
		public void onClick(View arg0) {
			Logger.i(text);
		}
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
		AppClient.vertifiedCode(appContext, code, new ClientCallback() {
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
					UIHelper.ToastMessage(LoginWechat.this, user.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(LoginWechat.this, message, Toast.LENGTH_SHORT);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				Logger.i(e);
			}
		});
	}
	
	private void enterIndex() {
		Intent intent = new Intent(this, Tabbar.class);
		startActivity(intent);
		setResult(RESULT_OK);
		AppManager.getAppManager().finishActivity(this);
	}
}	
