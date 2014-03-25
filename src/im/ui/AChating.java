/**
 * wechatdonal
 */
package im.ui;


import im.bean.IMMessage;
import im.bean.IMMessage.JSBubbleMessageStatus;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pomelo.DataCallBack;
import pomelo.PomeloClient;


import service.IPolemoService;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.AppActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import config.CommonValue;
import config.MyApplication;
import db.manager.MessageManager;

/**
 * wechat
 *
 * @author donal
 *
 */
public abstract class AChating extends AppActivity{
	protected List<IMMessage> message_pool = new ArrayList<IMMessage>();
	protected String roomId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.NEW_MESSAGE_ACTION);
		filter.addAction(CommonValue.UPDATE_MESSAGE_ACTION);
		registerReceiver(receiver, filter);
		roomId = getIntent().getStringExtra("roomId");
		if (roomId == null)
			return;
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CommonValue.NEW_MESSAGE_ACTION.equals(action)) {
				IMMessage message = (IMMessage) intent.getSerializableExtra(IMMessage.IMMESSAGE_KEY);
				//adjust roomId
				Logger.i(message.roomId);
				if (!roomId.equals(message.roomId)) {
					return;
				}
				message_pool.add(message);
				receiveNewMessage(message);
				refreshMessage(message_pool);
			}
			else if (CommonValue.UPDATE_MESSAGE_ACTION.equals(action)) {
				String chatId = intent.getExtras().getString("chat_id");
				String msgId = intent.getExtras().getString("msg_id");
				String room = intent.getExtras().getString("room_id");
				String postAt = intent.getExtras().getString("post_at");
				if (!roomId.equals(room)) {
					return;
				}
				updateThisRoom(msgId, chatId, postAt);
			}
		}

	};
	
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
		MessageManager.getInstance(context).saveIMMessage(newMessage);
		message_pool.add(newMessage);
		refreshMessage(message_pool);
		PomeloClient client = MyApplication.getInstance().getPolemoClient();
		if (client == null) {
			scheduleReconnect();
			return;
		}
		if (!client.isConnected()) {
			scheduleReconnect();
			return;
		}	
		JSONObject msg = new JSONObject();
		try {
			msg.put("content", messageContent);
			msg.put("roomId", roomId);
			msg.put("msgId", time);
			client.request("chat.chatHandler.send", msg, new DataCallBack() {
				@Override
				public void responseData(JSONObject msg) {
					Logger.i(msg.toString());
					Message mes = new Message();
					mes.obj = msg;
					msgHandler.sendMessage(mes);
				}
			});
		} catch (JSONException e) {
			Logger.i(e);
		}
	}
	
	protected synchronized void sendMessage(IMMessage immsg) throws Exception {
		PomeloClient client = MyApplication.getInstance().getPolemoClient();
		if (client == null) {
			scheduleReconnect();
			return;
		}
		if (!client.isConnected()) {
			scheduleReconnect();
			return;
		}
		JSONObject msg = new JSONObject();
		try {
			msg.put("content", immsg.content);
			msg.put("roomId", roomId);
			msg.put("msgId", immsg.msgTime);
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
	
	private void scheduleReconnect() {
		if (!this.isFinishing()) {
//			Intent intent = new Intent(AppManager.getAppManager().currentActivity(), IPolemoService.class);
//			intent.setAction(IPolemoService.ACTION_START);
//			AppManager.getAppManager().currentActivity().startService(intent);
			Intent intent = new Intent(CommonValue.RECONNECT_ACTION);
			sendBroadcast(intent);
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
						MessageManager.getInstance(context).updateSendingMessageWhere(roomId, openId, msgId, immsg);
					}
				}
				refreshMessage(message_pool);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		};
	};
	
	private void updateThisRoom(String msgId, String chatId, String postAt) {
		for (IMMessage immsg : message_pool) {
			if (immsg.msgStatus == JSBubbleMessageStatus.JSBubbleMessageStatusDelivering && immsg.msgTime.equals(msgId)) {
				immsg.msgStatus = JSBubbleMessageStatus.JSBubbleMessageStatusReaded;
				immsg.msgTime = postAt;
				immsg.postAt = postAt;
				immsg.chatId = chatId;
			}
		}
		refreshMessage(message_pool);
	}
	
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
