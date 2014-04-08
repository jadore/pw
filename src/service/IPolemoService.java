/**
 * QYdonal
 */
package service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import im.bean.IMMessage;
import im.bean.IMMessage.JSBubbleMessageStatus;
import im.ui.Chating;

import org.json.JSONException;
import org.json.JSONObject;

import pomelo.DataCallBack;
import pomelo.DataEvent;
import pomelo.DataListener;
import pomelo.PomeloClient;

import tools.AppException;
import tools.AppManager;
import tools.DecodeUtil;
import tools.Logger;
import tools.StringUtils;
import ui.Index;
import com.vikaa.mycontact.R;

import config.CommonValue;
import config.MyApplication;
import db.manager.MessageManager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * QY
 *
 * @author donal
 *
 */
public class IPolemoService extends Service {
	public static final String TAG = "IPO";
	public static final String PREF_STARTED = "IPO_STATEED";
	public static final String PREF_CONNECTED = "IPO_CONNECTED";
	
	public static final String	ACTION_START = TAG + ".START";
	public static final String	ACTION_STOP = TAG + ".STOP";
	public static final String	ACTION_RECONNECT = TAG + ".RECONNECT";
	public static final String  ACTION_SCHEDULE = TAG + ".SCHEDULE";
	
	private String test_host = "192.168.1.147";
	private int test_port = 3014;
	private PomeloClient client;
	
	private SharedPreferences mPrefs;
	
	private boolean mStarted;
	private boolean mConnected;
	
	private long mStartTime;
	public static final String PREF_RETRY = "retryInterval";
	private static final long  INITIAL_RETRY_INTERVAL = 1000 * 60;
	private static final long  MAXIMUM_RETRY_INTERVAL = 1000 * 60 * 30;
	
	private NotificationManager notificationManager;
	
	private Timer mTimer; 
	private ReConnectTimer lockTask;
	
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mPrefs = getSharedPreferences(TAG, MODE_PRIVATE);
		mStartTime = System.currentTimeMillis();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.RECONNECT_ACTION);
		registerReceiver(receiver, filter);
		registerReceiver(mConnectivityChanged, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.getAction().equals(ACTION_STOP) == true) {
				stop();
				stopSelf();
			} 
			else if (intent != null && intent.getAction().equals(ACTION_START) == true ) {
				start();
			} 
			else if (intent.getAction().equals(ACTION_RECONNECT) == true) {
				if (MyApplication.getInstance().isNetworkConnected()) {
					try {
//						reconnectIfNecessary();
						if (client != null) {
							client.disconnect();
						}
						client = null;
						reconnectTask();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else if (intent.getAction().equals(ACTION_SCHEDULE) == true) {
				scheduleReconnect(mStartTime);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		unregisterReceiver(mConnectivityChanged);
		if (client!=null) {
			client.disconnect();
		}
		super.onDestroy();
	}
	
	
	private void setStarted(boolean started) {
		mPrefs.edit().putBoolean(PREF_STARTED, started).commit();		
		mStarted = started;
	}
	
	private boolean wasStarted() {
		return mPrefs.getBoolean(PREF_STARTED, false);
	}
	
	private boolean wasConnected() {
		return mPrefs.getBoolean(PREF_CONNECTED, false);
	}
	
	private synchronized void start() {
		Logger.i("start");
		connect();
			
	}
	
	private synchronized void stop() {
		setStarted(false);
		if (client != null) {
			client.disconnect();
			client = null;
		}
	}
	
	public void scheduleReconnect(long startTime) {
		if (wasConnected()) {
			return;
		}
		long interval = mPrefs.getLong(PREF_RETRY, INITIAL_RETRY_INTERVAL);

		long now = System.currentTimeMillis();
		long elapsed = now - startTime;

		if (elapsed < interval) {
			interval = Math.min(interval * 4, MAXIMUM_RETRY_INTERVAL);
		} else {
			interval = INITIAL_RETRY_INTERVAL;
		}
		
		Logger.i("Rescheduling connection in " + interval + "ms.");

		mPrefs.edit().putLong(PREF_RETRY, interval).commit();

		Intent i = new Intent();
		i.setClass(this, IPolemoService.class);
		i.setAction(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, now + interval, pi);
	}
	
	private synchronized void connect() {
		String openid = MyApplication.getInstance().getLoginUid();
		if (StringUtils.empty(openid) ) {
			return;
		}
		else {
			queryEntry();
		}
	}
	
	private synchronized void queryEntry() {
		client = new PomeloClient(test_host, test_port);
		client.init();
		JSONObject msg = new JSONObject();
		try {
			final String hash = (MyApplication.getInstance().getLoginUid());
			msg.put("openid", hash);
			client.request("gate.gateHandler.queryEntry", msg, new DataCallBack() {
				@Override
				public void responseData(JSONObject msg) {
					Logger.i(msg.toString());
					setStarted(true);
					client.disconnect();
					client = null;
					try {
						String ip = msg.getString("host");
						enter(ip, msg.getInt("port"), hash);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} 
		Logger.i("queryentry");
	}
	
	private void enter(String host, int port, String openid) {
		JSONObject msg = new JSONObject();
		try {
			msg.put("openid", openid);
			msg.put("rid", "wechatim");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		client = new PomeloClient(host, port);
		client.init();
		client.request("connector.entryHandler.entry", msg, new DataCallBack() {
			@Override
			public void responseData(JSONObject msg) {
				Logger.i(msg.toString());
				Message msgForHandler = new Message();
				if (msg.has("error")) {
					try {
						if (msg.getInt("code") == 500) {//duplicate log in
							msgForHandler.what = 2;
						}
						else {
							msgForHandler.what = 0;
							client.disconnect();
							client = null;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
				}
				else {
					msgForHandler.what = 1;
					MyApplication.getInstance().setPolemoClient(client);
				}
				myHandler.sendMessage(msgForHandler);
			}
		});
	}
	
	Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				 
				break;

			case 1:
				endReconnectTask();
				startChatListener();
				//send broadcast to 
				sendMessages();
				break;
				
			case 2:
				endReconnectTask();
				sendMessages();
				break;
			}
			
			
		};
	};
	
	private void startChatListener() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				client.on("onAddUser", new DataListener() {
					@Override
					public void receiveData(DataEvent event) {
						JSONObject msg = event.getMessage();
						try {
							Logger.i(msg.toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				client.on("onLeaveUser", new DataListener() {
					@Override
					public void receiveData(DataEvent event) {
						JSONObject msg = event.getMessage();
						try {
							Logger.i(msg.toString());
							if (msg.isNull("body")) {
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				client.on("onChat", new DataListener() {
					@Override
					public void receiveData(DataEvent event) {
						JSONObject msg = event.getMessage();
						Message m = new Message();
						try {
							if (msg.isNull("body")) {
								return;
							}
							JSONObject msgBody = msg.getJSONObject("body");
							String msgContent = msgBody.getString("msg");
							String sender = msgBody.getString("sender");
							String roomId = msgBody.getString("room_id");
							String postAt = msgBody.getString("post_at");
							String chatId = msgBody.getString("chat_id");
							IMMessage immsg = new IMMessage();
							immsg.msgTime = (postAt);
							immsg.content = (msgContent);
							immsg.openId = (sender);
							immsg.msgType = IMMessage.JSBubbleMessageType.JSBubbleMessageTypeIncoming;
							immsg.msgStatus = IMMessage.JSBubbleMessageStatus.JSBubbleMessageStatusReceiving;
							immsg.mediaType = IMMessage.JSBubbleMediaType.JSBubbleMediaTypeText;
							immsg.roomId = roomId;
							immsg.postAt = postAt;
							immsg.chatId = chatId;
							m.what = 1;
							m.obj = immsg;
							ChatHandler.sendMessage(m);
							
						} catch (Exception e) {
							Logger.i(e);
						}
					}

				});
			}
		}).start();
	}
	
	Handler ChatHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				IMMessage immsg = (IMMessage) msg.obj;
				Logger.i(immsg.content);
				long rows = MessageManager.getInstance(IPolemoService.this).saveIMMessage(immsg);
				if (rows != -1) {
					Intent intent = new Intent(CommonValue.NEW_MESSAGE_ACTION);
					intent.putExtra(IMMessage.IMMESSAGE_KEY, immsg);
					sendBroadcast(intent);
					setNotiType(R.drawable.ic_launcher,
							"新消息",
							immsg.content, Chating.class, immsg.roomId);
				}
				break;
			}
		};
	};
	
	private void setNotiType(int iconId, String contentTitle,
			String contentText, Class activity, String roomId) {
		Intent notifyIntent = new Intent(this, activity);
		notifyIntent.putExtra("roomId", roomId);
		PendingIntent appIntent = PendingIntent.getActivity(this, 0,
				notifyIntent, 0);
		Notification myNoti = new Notification();
		myNoti.flags = Notification.FLAG_AUTO_CANCEL;
		myNoti.defaults |= Notification.DEFAULT_SOUND;
		myNoti.defaults |= Notification.DEFAULT_VIBRATE;
		myNoti.icon = iconId;
		myNoti.tickerText = contentTitle;
		myNoti.setLatestEventInfo(this, contentTitle, contentText, appIntent);
		notificationManager.notify(0, myNoti);
	}
	

	private BroadcastReceiver mConnectivityChanged = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean hasConnectivity = MyApplication.getInstance().isNetworkConnected();
			if (hasConnectivity) {
				reconnectIfNecessary();
			} 
//			else if (client != null) {
//				client.disconnect();
//				client = null;
//				cancelReconnect();
//			}
		}
	};
	
	private synchronized void reconnectIfNecessary() {		
		if (!wasConnected()) {
			if (client != null) {
				client.disconnect();
				client = null;
			}
			connect();
		}
	}
	
//	private void cancelReconnect() {
//		Intent i = new Intent();
//		i.setClass(this, IPolemoService.class);
//		i.setAction(ACTION_RECONNECT);
//		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
//		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
//		alarmMgr.cancel(pi);
//	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void reconnectTask(){
		if (mTimer == null) {  
            mTimer = new Timer();  
        }
		if (lockTask == null) {  
			lockTask = new ReConnectTimer();  
		}
		if(mTimer != null && lockTask != null ) {
			mTimer.schedule(lockTask, 0L, 60*1000L);  
		}
	}
	
	public void endReconnectTask() {
		if (mTimer != null) {  
            mTimer.cancel();  
            mTimer = null;  
        }  
        if (lockTask != null) {  
        	lockTask.cancel();  
        	lockTask = null;  
        }  
	}
	
	class ReConnectTimer extends TimerTask {
		@Override
		public void run() {
			connect();
		}
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CommonValue.RECONNECT_ACTION.equals(action)) {
				try {
					if (client != null) {
						client.disconnect();
					}
					client = null;
					Intent intent1 = new Intent(context, IPolemoService.class);
					intent1.setAction(IPolemoService.ACTION_RECONNECT);
					startService(intent1);
				}
				catch (Exception e){
					Logger.i(e);
				}
			}
		}
	};
	
	private  synchronized void sendMessages() {
		if (client == null) {
			return;
		}
		List<IMMessage> messages = MessageManager.getInstance(this).getSendingMessages();
		if (messages.size() == 0) {
			return;
		}
		for (IMMessage imMessage : messages) {
			JSONObject msg = new JSONObject();
			try {
				msg.put("content", imMessage.content);
				msg.put("roomId", imMessage.roomId);
				msg.put("msgId", imMessage.msgTime);
				client.request("chat.chatHandler.send", msg, new DataCallBack() {
					@Override
					public void responseData(JSONObject msg) {
						Message mes = new Message();
						mes.obj = msg;
						msgHandler.sendMessage(mes);
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
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
				int row = MessageManager.getInstance(IPolemoService.this).updateSendingMessageWhere(roomId, openId, msgId, chatId, postAt);
				if (row > 0) {
					Intent intent = new Intent(CommonValue.UPDATE_MESSAGE_ACTION);
					intent.putExtra("chat_id", chatId);
					intent.putExtra("msg_id", msgId);
					intent.putExtra("room_id", roomId);
					intent.putExtra("sender", postAt);
					intent.putExtra("post_at", postAt);
					sendBroadcast(intent);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		};
	};
}
