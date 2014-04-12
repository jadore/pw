package service;

import java.util.ArrayList;

import com.vikaa.mycontact.R;

import config.CommonValue;
import config.MyApplication;

import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.QYWebView;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SendSmsService extends IntentService {
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
	public static String			UPLOAD_CLIENT = "pw.sms.service";
	private static final String ACTION_START_PAY = UPLOAD_CLIENT + ".START.PAY";
	private static final String	ACTION_STOP = UPLOAD_CLIENT + ".STOP";
	private static final String	ACTION_KEEPALIVE = UPLOAD_CLIENT + ".KEEP_ALIVE";
	
	private ArrayList<String> smsMember ;
	private String smsBody;
	private SmsManager sManage; 
	MyApplication application ;
	public SendSmsService() {
		super(UPLOAD_CLIENT);
	}
	
	
	public static void actionStartPAY(Context ctx, ArrayList<String> smsMember, String smsBody) {
		try{
			Intent i = new Intent(ctx, SendSmsService.class);
			i.setAction(ACTION_START_PAY);
			i.putStringArrayListExtra("smsMember", smsMember);
			i.putExtra("smsBody", smsBody);
			ctx.startService(i);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void actionStop(Context ctx) {
		try {
			Intent i = new Intent(ctx, SendSmsService.class);
			i.setAction(ACTION_STOP);
			ctx.stopService(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(ACTION_START_PAY) == true) {
				application = MyApplication.getInstance();
				smsMember = new ArrayList<String>();
				smsMember.addAll(intent.getStringArrayListExtra("smsMember"));
				smsBody = intent.getStringExtra("smsBody") +" （" + application.getNickname() + "）";
				Logger.i(smsBody);
				sManage = SmsManager.getDefault();  
				
				sendSMS();
		}
	}

	private void sendSMS() {
		try {
			showNotify("群友通讯录,正在发短息...", "正在发短息...");
			for (int i = 0; i < smsMember.size(); i++) {  
	            String number = smsMember.get(i);  
	            Intent sentIntent = new Intent(SENT_SMS_ACTION); 
	        	PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent,  0); 
	            sManage.sendTextMessage(number, null, smsBody, sentPI, null);  
	            ContentValues values = new ContentValues();  
	            //发送时间  
				values.put("date", System.currentTimeMillis());   
	            //阅读状态              
				values.put("read", 0);             
	            //1为收 2为发             
				values.put("type", 2);           
	            //送达号码   
				values.put("address", number);             
	            //送达内容            
				values.put("body", smsBody);             
	            //插入短信库    
				getContentResolver().insert(Uri.parse("content://sms/sent"), values); 
	        } 
			
			showNotify("群友通讯录,短息发送完成", "短息发送完成");
		} catch (Exception e ) {
			Logger.i(e);
		}
	}
	
	public static final int NOTIFY_ID = 0x001;
	private void showNotify(String title, String message) {
		// 更新通知栏
		

		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, title, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.contentView = null;

		Intent intent = new Intent();
//		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/message/index", CommonValue.BASE_URL));
		PendingIntent contentIntent = PendingIntent.getActivity(application, 0,
				intent, 0);
		notification.setLatestEventInfo(application, title, message, contentIntent);

		application.getNotificationManager().notify(NOTIFY_ID, notification);// 通知一下才会生效哦
	}
}
