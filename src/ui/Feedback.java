package ui;

import service.SendSmsService;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

import bean.Entity;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Feedback extends AppActivity{
	private ProgressDialog loadingPd;
	private FrameLayout mForm;
	private TextView msgView;
	private EditText mContent;
	private LinearLayout mClearwords;
	private TextView mNumberwords;
	
	private InputMethodManager imm;
	
	public static LinearLayout mMessage;
	public static Context mContext;
	
	private static final int MAX_TEXT_LENGTH = 160;//最大输入字数
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		mContext = this;
		//软键盘管理类
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		initUI();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {    	
    	mForm = (FrameLayout)findViewById(R.id.tweet_pub_form);
    	msgView = (TextView) findViewById(R.id.msg_view);
    	mMessage = (LinearLayout)findViewById(R.id.tweet_pub_message);
    	mContent = (EditText)findViewById(R.id.tweet_pub_content);
    	mClearwords = (LinearLayout)findViewById(R.id.tweet_pub_clearwords);
    	mNumberwords = (TextView)findViewById(R.id.tweet_pub_numberwords);
    	
    	mClearwords.setOnClickListener(clearwordsClickListener);
    	
    	//编辑器添加文本监听
    	mContent.addTextChangedListener(new TextWatcher() {		
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//显示剩余可输入的字数
				mNumberwords.setText((s.length()) + "");
			}		
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}		
			public void afterTextChanged(Editable s) {}
		});
    	//编辑器点击事件
    	mContent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//显示软键盘
				showIMM();
			}
		});
    	//设置最大输入字数
    	InputFilter[] filters = new InputFilter[1];  
    	filters[0] = new InputFilter.LengthFilter(MAX_TEXT_LENGTH);
    	mContent.setFilters(filters);
    }
    
    private void showIMM() {
    	imm.showSoftInput(mContent, 0);
    }
	
    private View.OnClickListener clearwordsClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			String content = mContent.getText().toString();
			if(StringUtils.notEmpty(content)){
				UIHelper.showClearWordsDialog(v.getContext(), mContent, mNumberwords);
			}
		}
	};
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			closeInput();
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			sendFeedback();
			break;
		}
	}
	
	private void sendFeedback() {
		
		try {
			imm.hideSoftInputFromWindow(mContent.getWindowToken(), 0);
			String content = mContent.getText().toString();
			Logger.i(content);
			if (StringUtils.empty(content)) {
				UIHelper.ToastMessage(getApplicationContext(), "请输入内容", Toast.LENGTH_SHORT);
				return;
			}
			loadingPd = UIHelper.showProgress(this, null, null, true);
			AppClient.sendFeedback(appContext, content, new ClientCallback() {
				@Override
				public void onSuccess(Entity data) {
					UIHelper.dismissProgress(loadingPd);
					show1OptionsDialog(oprators);
				}
				
				@Override
				public void onFailure(String message) {
					UIHelper.dismissProgress(loadingPd);
					UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
				}
				
				@Override
				public void onError(Exception e) {
					UIHelper.dismissProgress(loadingPd);
					Logger.i(e);
					UIHelper.ToastMessage(getApplicationContext(), "网络不给力", Toast.LENGTH_SHORT);
				}
			});
		} catch (Exception e ) {
			Logger.i(e);
		}
	}
	
	String[] oprators = new String[] { "返回"};
	private void show1OptionsDialog(final String[] arg){
		new AlertDialog.Builder(this).setTitle("反馈发送成功,谢谢您的支持").setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					AppManager.getAppManager().finishActivity(Feedback.this);
					break;
				}
			}
		}).show();
	}
}
