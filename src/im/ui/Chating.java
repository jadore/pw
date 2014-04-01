/**
 * wechatdonal
 */
package im.ui;


import im.bean.IMMessage;
import im.bean.IMMessage.JSBubbleMessageStatus;

import java.util.Collections;
import java.util.List;

import bean.ChatterEntity;
import bean.Entity;
import com.vikaa.mycontact.R;

import tools.AppManager;
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Chating extends AChating implements IXListViewListener{
	private IMMessageAdapter adapter = null;
	private EditText messageInput = null;
	private Button messageSendBtn = null;
	private XListView listView;
	
	private int firstVisibleItem;
	
	private int lvDataState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chating);
		init();
	}
	
	private void init() {
		listView = (XListView) findViewById(R.id.chat_list);
		listView.setPullLoadEnable(false);
		listView.setPullRefreshEnable(false);
		listView.setXListViewListener(this, 0);
		listView.setCacheColorHint(0);
		adapter = new IMMessageAdapter(this, message_pool, imageLoader);
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
		messageInput.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (getMessages().size() < 1) {
					return false;
				}
				Handler jumpHandler = new Handler();
		        jumpHandler.postDelayed(new Runnable() {
					public void run() {
						listView.setSelection(getMessages().size()-1);
					}
				}, 500);
				return false;
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
						
					} catch (Exception e) {
						Logger.i(e);
					}
					messageInput.setText("");
				}
				listView.setSelection(getMessages().size()-1);
			}
		});
		lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
		listView.startRefresh();
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
		adapter.notifyDataSetChanged();
		if(messages.size() > 0){
			listView.setSelection(messages.size()-1);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	@Override
	public void onBackPressed() {
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
        mHandler.sendEmptyMessageDelayed(0, 1000);
	}

	@Override
	public void onLoadMore(int id) {
		
	}
	
	private void getHistory() {
		List<IMMessage> msgs= getMessages();
		String maxId;
		if (msgs.size() > 0) {
			maxId = msgs.get(0).chatId;
			if (maxId.equals("-1") || StringUtils.empty(maxId)) {
				lvDataState = UIHelper.LISTVIEW_DATA_FULL;
				listView.stopRefresh();
				listView.setPullRefreshEnable(false);
				return;
			}
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
											message_pool.addAll(0, data);
											adapter.notifyDataSetChanged();
											listView.setSelection(data.size());
										}
										listView.stopRefresh();
										listView.setPullRefreshEnable(false);
									}
								});
		}
		else {
			MessageManager.getInstance(context).getFirstMessageListByFrom(roomId, new MessageManagerCallback() {
				
				@Override
				public void getMessages(List<IMMessage> data) {
					if (data.size() >= 30) {
				        lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				    }
				    else {
				    	lvDataState = UIHelper.LISTVIEW_DATA_FULL;
				    }
					if (data.size() > 0) {
						message_pool.addAll(data);
						adapter.notifyDataSetChanged();
						listView.setSelection(data.size());
					}
					listView.stopRefresh();
					listView.setPullRefreshEnable(false);
				}
			});
		}
		
	}
}
