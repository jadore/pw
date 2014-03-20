/**
 * QYdonal
 */
package service;

import im.bean.IMMessage;
import im.bean.Notice;
import org.json.JSONException;
import org.json.JSONObject;

import pomelo.DataCallBack;
import pomelo.DataEvent;
import pomelo.DataListener;
import pomelo.PomeloClient;

import tools.Logger;
import tools.StringUtils;
import ui.Index;
import com.vikaa.mycontact.R;

import config.CommonValue;
import config.MyApplication;
import db.manager.MessageManager;
import db.manager.NoticeManager;

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
	
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mPrefs = getSharedPreferences(TAG, MODE_PRIVATE);
		mStartTime = System.currentTimeMillis();
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.i("start");
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
						reconnectIfNecessary();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else if (intent.getAction().equals(ACTION_SCHEDULE) == true) {
				scheduleReconnect(mStartTime);
			}
		}
//		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mConnectivityChanged);	
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
//		if (mStarted) {
//			return;
//		}
		Logger.i("start");
		connect();
		registerReceiver(mConnectivityChanged, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));	
	}
	
	private synchronized void stop() {
//		if (mStarted == false) {
//			return;
//		}
		setStarted(false);
		cancelReconnect();
		if (client != null) {
			client.disconnect();
			client = null;
		}
	}
	
	private synchronized void connect() {
		String openid = MyApplication.getInstance().getLoginUid();
		if (StringUtils.empty(openid) ) {
			return;
		}
		else {
//			setStarted(true);
			queryEntry();
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
	
	private synchronized void sccketListener() {
	}
	
	private synchronized void queryEntry() {
		client = new PomeloClient(test_host, test_port);
		client.init();
		JSONObject msg = new JSONObject();
		try {
			msg.put("openid", MyApplication.getInstance().getLoginUid());
			client.request("gate.gateHandler.queryEntry", msg, new DataCallBack() {
				@Override
				public void responseData(JSONObject msg) {
					Logger.i(msg.toString());
					setStarted(true);
					client.disconnect();
					try {
						String ip = msg.getString("host");
						enter(ip, msg.getInt("port"), MyApplication.getInstance().getLoginUid());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (JSONException e) {
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
				scheduleReconnect(mStartTime);
				break;

			case 1:
				startChatListener();
				break;
				
			case 2:
				break;
			}
			
			
		};
	};
	
	private void startChatListener() {
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
					JSONObject msgBody = msg.getJSONObject("body");
					String openId = msgBody.getString("user");
					if (openId.equals(MyApplication.getInstance().getLoginUid())) {
						//下线了，重连
						SharedPreferences sharedPre = MyApplication.getInstance().getSharedPreferences(
								IPolemoService.TAG, Context.MODE_PRIVATE);
						sharedPre.edit().putBoolean(IPolemoService.PREF_CONNECTED, false).commit();
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
					String time = (System.currentTimeMillis()/1000)+"";//DateUtil.date2Str(Calendar.getInstance(), Constant.MS_FORMART);
					immsg.msgTime = (time);
					immsg.content = (msgContent);
					immsg.openId = (sender);
					immsg.msgType = IMMessage.JSBubbleMessageType.JSBubbleMessageTypeIncoming;
					immsg.msgStatus = IMMessage.JSBubbleMessageStatus.JSBubbleMessageStatusReceiving;
					immsg.mediaType = IMMessage.JSBubbleMediaType.JSBubbleMediaTypeText;
					immsg.roomId = roomId;
					immsg.postAt = postAt;
					immsg.chatId = chatId;
//					NoticeManager noticeManager = NoticeManager.getInstance(IPolemoService.this);
//					Notice notice = new Notice();
//					notice.setTitle("会话信息");
//					notice.setNoticeType(Notice.CHAT_MSG);
//					notice.setContent(msgContent);
//					notice.setFrom(from);
//					notice.setStatus(Notice.UNREAD);
//					notice.setNoticeTime(time);

					
					MessageManager.getInstance(IPolemoService.this).saveIMMessage(immsg);
					long noticeId = -1;

//					noticeId = noticeManager.saveNotice(notice);
//					if (noticeId != -1) {
						Intent intent = new Intent(CommonValue.NEW_MESSAGE_ACTION);
						intent.putExtra(IMMessage.IMMESSAGE_KEY, immsg);
//						intent.putExtra("notice", notice);
						sendBroadcast(intent);
						setNotiType(R.drawable.ic_launcher,
								"新消息",
								immsg.content, Index.class, sender);

//					}
					
				} catch (Exception e) {
					Logger.i(e);
				}
			}

		});
	}
	
	private void setNotiType(int iconId, String contentTitle,
			String contentText, Class activity, String from) {
		Intent notifyIntent = new Intent(this, activity);
		notifyIntent.putExtra("to", from);
		PendingIntent appIntent = PendingIntent.getActivity(this, 0,
				notifyIntent, 0);
		Notification myNoti = new Notification();
		myNoti.flags = Notification.FLAG_AUTO_CANCEL;
		myNoti.icon = iconId;
		myNoti.tickerText = contentTitle;
		myNoti.setLatestEventInfo(this, contentTitle, contentText, appIntent);
		notificationManager.notify(0, myNoti);
	}
	

//	private AIDLPolemoService.Stub stub = new AIDLPolemoService.Stub() {
//		
//		@Override
//		public void send(String target, String message) throws RemoteException {
//			
//		}
//	};
	
	private BroadcastReceiver mConnectivityChanged = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean hasConnectivity = MyApplication.getInstance().isNetworkConnected();
			if (hasConnectivity) {
				reconnectIfNecessary();
			} 
			else if (client != null) {
				client.disconnect();
				cancelReconnect();
				client = null;
			}
		}
	};
	
	private synchronized void reconnectIfNecessary() {		
		if (!wasConnected()) {
			client = null;
			connect();
		}
	}
	
	private void cancelReconnect() {
		Intent i = new Intent();
		i.setClass(this, IPolemoService.class);
		i.setAction(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmMgr.cancel(pi);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
