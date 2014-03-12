/**
 * QYdonal
 */
package service;

import im.bean.IMMessage;
import im.bean.Notice;
import org.json.JSONException;
import org.json.JSONObject;

import tools.Logger;
import tools.StringUtils;
import ui.Index;
import com.netease.pomelo.DataCallBack;
import com.netease.pomelo.DataEvent;
import com.netease.pomelo.DataListener;
import com.netease.pomelo.PomeloClient;
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
public class IPolemoService extends Service{
	private static final String TAG = "IPO";
	private static final String PREF_STARTED = "IPO_STATEED";
	
	public static final String	ACTION_START = TAG + ".START";
	public static final String	ACTION_STOP = TAG + ".STOP";
	public static final String	ACTION_RECONNECT = TAG + ".RECONNECT";
	
	private String test_host = "192.168.1.147";
	private int test_port = 3014;
	private PomeloClient client;
	
	private SharedPreferences mPrefs;
	private boolean mStarted;
	
	private long mStartTime;
	public static final String PREF_RETRY = "retryInterval";
	private static final long INITIAL_RETRY_INTERVAL = 1000 * 15;
	private static final long MAXIMUM_RETRY_INTERVAL = 1000 * 60 * 30;
	
	private NotificationManager notificationManager;
	
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mPrefs = getSharedPreferences(TAG, MODE_PRIVATE);
		mStartTime = System.currentTimeMillis();
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.i("start");
		if (intent.getAction().equals(ACTION_STOP) == true) {
			stop();
			stopSelf();
		} else if (intent.getAction().equals(ACTION_START) == true || intent == null) {
			start();
		} else if (intent.getAction().equals(ACTION_RECONNECT) == true) {
			if (MyApplication.getInstance().isNetworkConnected()) {
				try {
					reconnectIfNecessary();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		flags = START_STICKY;
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
		if (StringUtils.empty(openid)) {
			return;
		}
		else {
//			setStarted(true);
			queryEntry();
		}
	}
	
	public void scheduleReconnect(long startTime) {
		long interval = mPrefs.getLong(PREF_RETRY, INITIAL_RETRY_INTERVAL);

		long now = System.currentTimeMillis();
		long elapsed = now - startTime;

//		if (elapsed < interval) {
//			interval = Math.min(interval * 4, MAXIMUM_RETRY_INTERVAL);
//		} else {
			interval = INITIAL_RETRY_INTERVAL;
//		}
		
		Logger.i("Rescheduling connection in " + interval + "ms.");

		mPrefs.edit().putLong(PREF_RETRY, interval).commit();

		Intent i = new Intent();
		i.setClass(this, IPolemoService.class);
		i.setAction(ACTION_RECONNECT);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, now + interval, pi);
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
					String target = msgBody.getString("target");
					String msgContent = msgBody.getString("msg");
					String from = msgBody.getString("from");
					Logger.i(msg.toString());
					
					IMMessage immsg = new IMMessage();
					// String time = (String)
					// message.getProperty(IMMessage.KEY_TIME);
					String time = (System.currentTimeMillis()/1000)+"";//DateUtil.date2Str(Calendar.getInstance(), Constant.MS_FORMART);
					immsg.setTime(time);
					immsg.setContent(msgContent);
					immsg.setType(IMMessage.SUCCESS);
					immsg.setFromSubJid(from);
					NoticeManager noticeManager = NoticeManager.getInstance(IPolemoService.this);
					Notice notice = new Notice();
					notice.setTitle("会话信息");
					notice.setNoticeType(Notice.CHAT_MSG);
					notice.setContent(msgContent);
					notice.setFrom(from);
					notice.setStatus(Notice.UNREAD);
					notice.setNoticeTime(time);

					IMMessage newMessage = new IMMessage();
					newMessage.setMsgType(0);
					newMessage.setFromSubJid(from);
					newMessage.setContent(msgContent);
					newMessage.setTime(time);
					MessageManager.getInstance(IPolemoService.this).saveIMMessage(newMessage);
					long noticeId = -1;

					noticeId = noticeManager.saveNotice(notice);
					if (noticeId != -1) {
						Intent intent = new Intent(CommonValue.NEW_MESSAGE_ACTION);
						intent.putExtra(IMMessage.IMMESSAGE_KEY, immsg);
						intent.putExtra("notice", notice);
						sendBroadcast(intent);
						setNotiType(R.drawable.ic_launcher,
								"新消息",
								notice.getContent(), Index.class, from);

					}
					
				} catch (Exception e) {
					e.printStackTrace();
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
		if (mStarted == true && client == null) {
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
