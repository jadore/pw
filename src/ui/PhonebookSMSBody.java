package ui;

import java.util.ArrayList;

import service.SendSmsService;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

import bean.CardIntroEntity;

import com.google.analytics.tracking.android.EasyTracker;
import com.vikaa.mycontact.R;

import config.CommonValue;

import android.R.array;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
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

public class PhonebookSMSBody extends AppActivity{
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
	
	private ArrayList<String> smsMember = new ArrayList<String>();
	private SmsManager sManage; 
	private SMSSendBR smsBR;
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
	private int allNum = 0;
	private int successNum = 0;
	private int failureNum = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phonebook_sms_body);
		mContext = this;
		//软键盘管理类
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		sManage = SmsManager.getDefault();  
		initUI();
		initData();
//		registerSMSReceiver();
	}
	
	@Override
	protected void onDestroy() {
//		unregisterSMSReceiver();
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
    
    private void initData() {
    	smsMember = getIntent().getStringArrayListExtra(CommonValue.PhonebookViewIntentKeyValue.SMSPersons);
    	
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
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			sendSMS();
			break;
		}
	}
	
	private void registerSMSReceiver() {
		smsBR =  new  SMSSendBR();
        IntentFilter postFilter = new IntentFilter();
        postFilter.addAction(SENT_SMS_ACTION);
        registerReceiver(smsBR, postFilter);
	}
	
	private void unregisterSMSReceiver() {
		unregisterReceiver(smsBR);
	}
	
	class SMSSendBR extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:  
				++allNum;
				successNum++;
				showResult();
	            break;  
	        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:  
	        	Logger.i("lll");
	        	break;  
	        case SmsManager.RESULT_ERROR_RADIO_OFF:  
	        	Logger.i("lll");
	        	break;  
	        case SmsManager.RESULT_ERROR_NULL_PDU:  
	        	Logger.i("lll");
	        	break; 
	        case SmsManager.RESULT_ERROR_NO_SERVICE:
	        	++allNum;
	        	failureNum++;
	        	showResult();
	        	break;
			}
		}
		
		private void showResult() {
			mMessage.setVisibility(View.VISIBLE);
			String msg = String.format("正在发送%d，共%d条", allNum, smsMember.size());
			msgView.setText(msg);
			if (allNum == smsMember.size()) {
				mMessage.setVisibility(View.GONE);
				show1OptionsDialog(oprators);
			}
		}
	}
	
	private void saveSMS(String tel, String msg) {
		ContentValues values = new ContentValues();  
        //发送时间  
		values.put("date", System.currentTimeMillis());   
        //阅读状态              
		values.put("read", 0);             
        //1为收 2为发             
		values.put("type", 2);           
        //送达号码              
		values.put("address",tel);             
        //送达内容            
		values.put("body", msg);             
        //插入短信库    
		getContentResolver().insert(Uri.parse("content://sms/sent"), values);  
	}
	
	
	private void sendSMS() {
		
		try {
			imm.hideSoftInputFromWindow(mContent.getWindowToken(), 0);
//			allNum = 0;
//			failureNum = 0;
//			successNum = 0;
			String content = mContent.getText().toString();
			Logger.i(content);
			if (StringUtils.empty(content)) {
				UIHelper.ToastMessage(getApplicationContext(), "请输入内容", Toast.LENGTH_SHORT);
				return;
			}
			SendSmsService.actionStartPAY(this, smsMember, content);
//			loadingPd = UIHelper.showProgress(this, null, null, true);
//			for (int i = 0; i < smsMember.size(); i++) {  
//	            String number = smsMember.get(i);  
//	            Logger.i(number);
//	            Intent sentIntent = new Intent(SENT_SMS_ACTION); 
////	            saveSMS(number, content);
//	        	PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent,  0); 
//	            sManage.sendTextMessage(number, null, content, sentPI, null);  
//	        }  
//			
//			UIHelper.dismissProgress(loadingPd);
//			UIHelper.ToastMessage(getApplicationContext(), "发送完成", Toast.LENGTH_SHORT);
			setResult(RESULT_OK);
			AppManager.getAppManager().finishActivity(PhonebookSMSBody.this);
		} catch (Exception e ) {
			Logger.i(e);
		}
	}
	
	String[] oprators = new String[] { "前往查看信息","不去了,回到通讯录" };
	private void show1OptionsDialog(final String[] arg){
		new AlertDialog.Builder(this).setTitle("提示,短信已全部发送").setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.setData(Uri.parse("content://mms-sms/"));
					startActivity(intent);
					AppManager.getAppManager().finishActivity(PhonebookSMSBody.this);
					break;
				case 1:
					AppManager.getAppManager().finishActivity(PhonebookSMSBody.this);
					break;
				}
			}
		}).show();
	}
}
