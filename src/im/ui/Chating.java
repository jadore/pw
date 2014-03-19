/**
 * wechatdonal
 */
package im.ui;


import im.bean.IMMessage;
import im.bean.Notice;

import java.util.Collections;
import java.util.List;

import bean.ChatterEntity;
import bean.Entity;
import bean.FamilyListEntity;

import com.vikaa.mycontact.R;

import tools.AppManager;
import tools.DateUtil;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import widget.XListView;
import widget.XListView.IXListViewListener;



import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;
import db.manager.MessageManager;
import db.manager.MessageManager.MessageManagerCallback;
import db.manager.NoticeManager;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Chating extends AChating implements IXListViewListener{
	private MessageListAdapter adapter = null;
	private EditText messageInput = null;
	private Button messageSendBtn = null;
	private XListView listView;
	private int recordCount;
//	private UserInfo user;// 聊天人
	private String to_name;
	private Notice notice;
	
	private int firstVisibleItem;
	private int objc;
	
	private int lvDataState;
	private int currentPage = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chating);
		init();
	}
	
	@Override
	protected void onDestroy() {
		//set read
		super.onDestroy();
	}
	
	private void init() {
		listView = (XListView) findViewById(R.id.chat_list);
		listView.setPullLoadEnable(false);
		listView.setPullRefreshEnable(false);
		listView.setXListViewListener(this, 0);
		listView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				closeInput();
				return false;
			}
		});
		listView.setCacheColorHint(0);
		adapter = new MessageListAdapter(Chating.this, getMessages(),
				listView);
		listView.setAdapter(adapter);
		
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_FLING:
					break;
				case SCROLL_STATE_IDLE:
					if (firstVisibleItem == 0 && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
						lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
						listView.startRefresh();
					}
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
					
					break;
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				Chating.this.firstVisibleItem = firstVisibleItem;
			}
		});

		messageInput = (EditText) findViewById(R.id.chat_content);
		messageInput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				listView.setSelection(getMessages().size()-1);
			}
		});
		messageSendBtn = (Button) findViewById(R.id.chat_sendbtn);
		messageSendBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = messageInput.getText().toString();
				if ("".equals(message)) {
					Toast.makeText(Chating.this, "不能为空",
							Toast.LENGTH_SHORT).show();
				} else {

					try {
						sendMessage(message);
						messageInput.setText("");
					} catch (Exception e) {
						messageInput.setText(message);
					}
				}
				listView.setSelection(getMessages().size()-1);
			}
		});
	}

	@Override
	protected void receiveNotice(Notice notice) {
		this.notice = notice;
	}
	
	@Override
	protected void receiveNewMessage(IMMessage message) {
		
	}

	@Override
	protected void refreshMessage(List<IMMessage> messages) {
		if (messages.size() >= 30) {
			lvDataState = UIHelper.LISTVIEW_DATA_MORE;
		}
		else {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
		}
		adapter.refreshList(messages);
		if(messages.size() > 0){
			listView.setSelection(messages.size()-1);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		recordCount = MessageManager.getInstance(context)
//				.getChatCountWithSb(roomId);
	}
	
	private class MessageListAdapter extends BaseAdapter {

		private List<IMMessage> items;
		private Context context;
		private ListView adapterList;
		private LayoutInflater inflater;

		public MessageListAdapter(Context context, List<IMMessage> items,
				ListView adapterList) {
			this.context = context;
			this.items = items;
			this.adapterList = adapterList;
		}

		public void refreshList(List<IMMessage> items) {
			this.items = items;
			this.notifyDataSetChanged();
			
		}

		@Override
		public int getCount() {
			return items == null ? 0 : items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			IMMessage message = items.get(position);
			if (message.msgType == IMMessage.JSBubbleMessageType.JSBubbleMessageTypeIncoming) {
				convertView = this.inflater.inflate(R.layout.chating_in, null);
			} else {
				convertView = this.inflater.inflate(R.layout.chating_out, null);
			}
			ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
			TextView useridView = (TextView) convertView.findViewById(R.id.row_userid);
			TextView dateView = (TextView) convertView.findViewById(R.id.row_date);
			TextView msgView = (TextView) convertView.findViewById(R.id.row_msg);
			
			useridView.setVisibility(View.GONE);
			String content = message.content;
			if (message.msgType == IMMessage.JSBubbleMessageType.JSBubbleMessageTypeIncoming) {
				getChatterFromCache(message.openId, avatar);
			} else {
				imageLoader.displayImage(appContext.getUserAvatar(), avatar, CommonValue.DisplayOptions.default_options);
			}
			msgView.setText(content);
//			String currentTime = message.msgTime;
//			String previewTime = (position - 1) >= 0 ? items.get(position-1).msgTime : "0";
//			try {
//				long time1 = Long.valueOf(currentTime);
//				long time2 = Long.valueOf(previewTime);
//				if ((time1-time2) >= 5 * 60 ) {
//					dateView.setVisibility(View.VISIBLE);
//					dateView.setText(DateUtil.wechat_time(message.msgTime));
//				}
//				else {
					dateView.setVisibility(View.GONE);
//				}
//			} catch (Exception e) {
//				Logger.i(e);
//			}
			return convertView;
		}

	}
	
	@Override
	public void onBackPressed() {
//		NoticeManager.getInstance(context).updateStatusByFrom(roomId, Notice.READ);
		super.onBackPressed();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		default:
			break;
		}
	}

	@Override
	public void onRefresh(int id) {
		Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                getHistory();
            }
        };
        mHandler.sendEmptyMessageDelayed(0, 500);
	}

	@Override
	public void onLoadMore(int id) {
		// TODO Auto-generated method stub
		
	}
	
	private void getHistory() {
		List<IMMessage> msgs= getMessages();
		if (msgs.size() > 0) {
			String maxId = msgs.get(0).chatId;
			MessageManager.getInstance(context).
				getMessageListByFrom(roomId, 
									maxId, 
									new MessageManagerCallback() {
					
										@Override
										public void getMessages(List<IMMessage> data) {
											if (data.size() == 30) {
										        lvDataState = UIHelper.LISTVIEW_DATA_MORE;
										    }
										    else {
										    	lvDataState = UIHelper.LISTVIEW_DATA_FULL;
										    }
											if (data.size() > 0) {
												Chating.this.getMessages().addAll(data);
												Collections.sort(Chating.this.getMessages());
												adapter.refreshList(Chating.this.getMessages());
												listView.setSelection(data.size());
											}
											listView.stopRefresh();
											listView.setPullRefreshEnable(false);
										}
									});
		}
	}
	
	private void getChatterFromCache(String openId, ImageView avatar) {
		String key = String.format("%s-%s", CommonValue.CacheKey.ChatterInfo+"-"+openId, appContext.getLoginUid());
		ChatterEntity entity = (ChatterEntity) appContext.readObject(key);
		if(entity != null){
			if (StringUtils.notEmpty(entity.avatar)) {
				imageLoader.displayImage(entity.avatar, avatar, CommonValue.DisplayOptions.default_options);
			}
		}
		else {
			getChatter(openId, avatar);
		}
	}
	
	private synchronized void getChatter(String openId, final ImageView avatar) {
		AppClient.getChaterBy(appContext, openId, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				ChatterEntity chatter = (ChatterEntity) data;
				if (StringUtils.notEmpty(chatter.avatar)) {
					imageLoader.displayImage(chatter.avatar, avatar, CommonValue.DisplayOptions.default_options);
				}
			}
			
			@Override
			public void onFailure(String message) {
				
			}
			
			@Override
			public void onError(Exception e) {
				
			}
		});
	}
}
