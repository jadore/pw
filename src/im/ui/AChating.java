/**
 * wechatdonal
 */
package im.ui;


import im.bean.IMMessage;
import im.bean.Notice;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


import com.google.gson.Gson;
import com.netease.pomelo.DataCallBack;


import tools.Logger;
import tools.StringUtils;
import ui.AppActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import config.CommonValue;
import config.MyApplication;
import db.manager.MessageManager;
import db.manager.NoticeManager;

/**
 * wechat
 *
 * @author donal
 *
 */
public abstract class AChating extends AppActivity{
	private List<IMMessage> message_pool = null;
	protected String to;
	private static int pageSize = 10;
	private List<Notice> noticeList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		to = getIntent().getStringExtra("to");
		if (to == null)
			return;
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		message_pool = MessageManager.getInstance(context)
				.getMessageListByFrom(to, 1, pageSize);
		if (null != message_pool && message_pool.size() > 0)
			Collections.sort(message_pool);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.NEW_MESSAGE_ACTION);
		registerReceiver(receiver, filter);
		NoticeManager.getInstance(context).updateStatusByFrom(to, Notice.READ);
		super.onResume();

	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Notice notice = (Notice) intent.getSerializableExtra("notice");
			if (CommonValue.NEW_MESSAGE_ACTION.equals(action)) {
				IMMessage message = intent
						.getParcelableExtra(IMMessage.IMMESSAGE_KEY);
				message_pool.add(message);
				receiveNewMessage(message);
				refreshMessage(message_pool);
				receiveNotice(notice);
			}
		}

	};
	
	protected abstract void receiveNotice(Notice notice);
	
	protected abstract void receiveNewMessage(IMMessage message);

	protected abstract void refreshMessage(List<IMMessage> messages);
	
	protected List<IMMessage> getMessages() {
		return message_pool;
	}
	
	protected void sendMessage(final String messageContent) throws Exception {
		if (StringUtils.empty(messageContent)) {
			return;
		}
		JSONObject msg = new JSONObject();
		try {
			msg.put("content", messageContent);
			msg.put("target", to);
			msg.put("rid", "wechatim");
			msg.put("from", MyApplication.getInstance().getLoginUid());
			MyApplication.getInstance().getPolemoClient().request("chat.chatHandler.send", msg, new DataCallBack() {
				@Override
				public void responseData(JSONObject msg) {
					Message mes = new Message();
					if (!to.equals("*") && !to.equals(MyApplication.getInstance().getLoginUid())) {
						mes.obj = messageContent;
						msgHandler.sendMessage(mes);
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	Handler msgHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			String time = (System.currentTimeMillis()/1000)+"";
			IMMessage newMessage = new IMMessage();
			newMessage.setMsgType(1);
			newMessage.setFromSubJid(to);
			newMessage.setContent((String)msg.obj);
			newMessage.setTime(time);
			message_pool.add(newMessage);
			Logger.i(MessageManager.getInstance(context).saveIMMessage(newMessage)+"");
			refreshMessage(message_pool);
		};
	};
	
	protected Boolean addNewMessage() {
		List<IMMessage> newMsgList = MessageManager.getInstance(context)
				.getMessageListByFrom(to, message_pool.size(), pageSize);
		if (newMsgList != null && newMsgList.size() > 0) {
			message_pool.addAll(newMsgList);
			Collections.sort(message_pool);
			return true;
		}
		return false;
	}
	
	protected int addNewMessage(int currentPage) {
		List<IMMessage> newMsgList = MessageManager.getInstance(context)
				.getMessageListByFrom(to, currentPage, pageSize);
		if (newMsgList != null && newMsgList.size() > 0) {
			message_pool.addAll(newMsgList);
			Collections.sort(message_pool);
			return newMsgList.size();
		}
		return 0;
	}

	protected void resh() {
		refreshMessage(message_pool);
	}
	
}
