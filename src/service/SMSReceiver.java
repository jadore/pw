package service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crashlytics.android.Crashlytics;

import tools.AppManager;
import tools.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

	public static final String TAG = "ImiChatSMSReceiver";

    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
			SmsMessage[] messages = getMessagesFromIntent(intent);
	        for (SmsMessage message : messages) {
	        	try {
	        		String responseString = message.getDisplayMessageBody();
		            String reg = "【(\\d{6})】\\w+";
		            Pattern pattern = Pattern.compile(reg);
	      			Matcher matcher = pattern.matcher(responseString);
	      			if(matcher.find()){
	      				String code = matcher.group(1);
	      				updateCode(code);
	      			}
	        	 }catch (Exception e){
	        		 Crashlytics.logException(e);
	        	 }
	        }
		}
	}

	public final SmsMessage[] getMessagesFromIntent(Intent intent)

    {

        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");

        byte[][] pduObjs = new byte[messages.length][];

 

        for (int i = 0; i < messages.length; i++)

        {

            pduObjs[i] = (byte[]) messages[i];

        }

        byte[][] pdus = new byte[pduObjs.length][];

        int pduCount = pdus.length;

        SmsMessage[] msgs = new SmsMessage[pduCount];

        for (int i = 0; i < pduCount; i++)

        {

            pdus[i] = pduObjs[i];

            msgs[i] = SmsMessage.createFromPdu(pdus[i]);

        }

        return msgs;

    }
	
	private void updateCode(String code) {
		Intent intent = new Intent();
		intent.putExtra("code", code);
		intent.setAction("get");
		try {
			AppManager.getAppManager().currentActivity().sendBroadcast(intent);
		} catch (Exception e) {
			Logger.i(e);
		}
	}
	
}
