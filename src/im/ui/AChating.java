/**
 * wechatdonal
 */
package im.ui;


import im.bean.IMMessage;
import im.bean.IMMessage.JSBubbleMessageStatus;
import im.bean.Notice;

import java.util.ArrayList;
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
import db.manager.MessageManager.MessageManagerCallback;
import db.manager.NoticeManager;

/**
 * wechat
 *
 * @author donal
 *
 */
public abstract class AChating extends AppActivity{
	private List<IMMessage> message_pool = new ArrayList<IMMessage>();
	protected String roomId;
	private static int pageSize = 10;
	private List<Notice> noticeList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		roomId = getIntent().getStringExtra("roomId");
		if (roomId == null)
			return;
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
//		MessageManager.getInstance(context).getMessageListByFrom(roomId, "0", new MessageManagerCallback() {
//			@Override
//			public void getMessages(List<IMMessage> data) {
//				message_pool = data;
//				if (null != message_pool && message_pool.size() > 0) {
//					Collections.sort(message_pool);
//				}
//				refreshMessage(message_pool);
//			}
//		});
//		message_pool = MessageManager.getInstance(context).getMessageListByFrom(roomId, 1, pageSize);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.NEW_MESSAGE_ACTION);
		registerReceiver(receiver, filter);
//		NoticeManager.getInstance(context).updateStatusByFrom(roomId, Notice.READ);
		
		super.onResume();

	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Notice notice = (Notice) intent.getSerializableExtra("notice");
			if (CommonValue.NEW_MESSAGE_ACTION.equals(action)) {
				IMMessage message = (IMMessage) intent.getSerializableExtra(IMMessage.IMMESSAGE_KEY);
				//adjust roomId
				Logger.i(message.roomId);
				if (!roomId.equals(message.roomId)) {
					return;
				}
				Logger.i(message.roomId);
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
		String time = (System.currentTimeMillis()/1000)+"";
		IMMessage newMessage = new IMMessage();
		newMessage.msgType = IMMessage.JSBubbleMessageType.JSBubbleMessageTypeOutgoing;
		newMessage.roomId = roomId;
		newMessage.content = messageContent;
		newMessage.msgTime = time;
		newMessage.postAt = time;
		newMessage.openId = appContext.getLoginUid();
		newMessage.msgStatus = IMMessage.JSBubbleMessageStatus.JSBubbleMessageStatusDelivering;
		newMessage.mediaType = IMMessage.JSBubbleMediaType.JSBubbleMediaTypeText;
		newMessage.chatId = "-1";
		Logger.i(MessageManager.getInstance(context).saveIMMessage(newMessage)+"");
		message_pool.add(newMessage);
		refreshMessage(message_pool);
		
		JSONObject msg = new JSONObject();
		try {
			msg.put("content", messageContent);
			msg.put("roomId", roomId);
			msg.put("msgId", time);
			MyApplication.getInstance().getPolemoClient().request("chat.chatHandler.send", msg, new DataCallBack() {
				@Override
				public void responseData(JSONObject msg) {
					Logger.i(msg.toString());
					Message mes = new Message();
					mes.obj = msg;
					msgHandler.sendMessage(mes);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	Handler msgHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			JSONObject obj = (JSONObject) msg.obj; 
			try {
				String chatId = obj.getString("chat_id");
				String msgId = obj.getString("msg_id");
				String roomId = obj.getString("room_id");
				String openId = obj.getString("sender");
				String postAt = obj.getString("post_at");
				for (IMMessage immsg : message_pool) {
					if (immsg.msgStatus == JSBubbleMessageStatus.JSBubbleMessageStatusDelivering && immsg.msgTime.equals(msgId)) {
						immsg.msgStatus = JSBubbleMessageStatus.JSBubbleMessageStatusReaded;
						immsg.msgTime = postAt;
						immsg.postAt = postAt;
						immsg.chatId = chatId;
						//update db
						Logger.i(""+MessageManager.getInstance(context).updateSendingMessageWhere(roomId, openId, msgId, immsg));
						
					}
				}
				refreshMessage(message_pool);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//update the status
			
//			IMMessage newMessage = new IMMessage();
//			newMessage.msgType = IMMessage.JSBubbleMessageType.JSBubbleMessageTypeOutgoing;
//			newMessage.roomId = roomId;
//			newMessage.content = ((String)msg.obj);
//			newMessage.msgTime = time;
//			newMessage.openId = appContext.getLoginUid();
//			newMessage.msgStatus = IMMessage.JSBubbleMessageStatus.JSBubbleMessageStatusNormal;
//			newMessage.mediaType = IMMessage.JSBubbleMediaType.JSBubbleMediaTypeText;
//			message_pool.add(newMessage);
//			Logger.i(MessageManager.getInstance(context).saveIMMessage(newMessage)+"");
//			refreshMessage(message_pool);
		};
	};
	
//	protected Boolean addNewMessage() {
//		List<IMMessage> newMsgList = MessageManager.getInstance(context)
//				.getMessageListByFrom(roomId, message_pool.size(), pageSize);
//		if (newMsgList != null && newMsgList.size() > 0) {
//			message_pool.addAll(newMsgList);
//			Collections.sort(message_pool);
//			return true;
//		}
//		return false;
//	}
//	
//	protected int addNewMessage(int currentPage) {
//		List<IMMessage> newMsgList = MessageManager.getInstance(context)
//				.getMessageListByFrom(roomId, currentPage, pageSize);
//		if (newMsgList != null && newMsgList.size() > 0) {
//			message_pool.addAll(newMsgList);
//			Collections.sort(message_pool);
//			return newMsgList.size();
//		}
//		return 0;
//	}

	protected void resh() {
		refreshMessage(message_pool);
	}
	
}
