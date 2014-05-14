package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppException;
import tools.AppManager;
import tools.UIHelper;
import ui.adapter.MessageCenterAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import bean.ActivityViewEntity;
import bean.CardIntroEntity;
import bean.Entity;
import bean.FriendCardListEntity;
import bean.MessageEntity;
import bean.MessageListEntity;
import bean.MessageUnReadEntity;
import bean.Result;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MessageView extends AppActivity {
	private PinnedHeaderListView mPinedListView0;
	private MessageCenterAdapter mMessageViewAdapter;
	private List<List<MessageEntity>> messsages;
	  
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_view);
		initUI();
		addMessageOp();
		
	}
	
	private void initUI() {
		mPinedListView0 = (PinnedHeaderListView) findViewById(R.id.listView);
		mPinedListView0.setDividerHeight(0);
		messsages = new ArrayList<List<MessageEntity>>();
		mMessageViewAdapter = new MessageCenterAdapter(this, messsages);
		mPinedListView0.setAdapter(mMessageViewAdapter);
	}
	
	private void addMessageOp() {
		List<MessageEntity> ops = new ArrayList<MessageEntity>();
		MessageEntity op1 = new MessageEntity();
		op1.message = "";
		ops.add(op1);
		messsages.add(ops);
		
		List<MessageEntity> ops1 = new ArrayList<MessageEntity>();
		MessageEntity op11 = new MessageEntity();
		op11.message = "";
		ops1.add(op11);
		messsages.add(ops1);
		
		
		List<MessageEntity> ops2 = new ArrayList<MessageEntity>();
		MessageEntity op21 = new MessageEntity();
		op21.message = "";
		ops2.add(op21);
		messsages.add(ops2);
		
		mMessageViewAdapter.notifyDataSetChanged();
	}
	

	private void getNewsNumber() {
		AppClient.getUnReadMessage(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				MessageUnReadEntity entity = (MessageUnReadEntity) data;
				List<MessageEntity> ops1 = new ArrayList<MessageEntity>();
				MessageEntity op11 = new MessageEntity();
				op11.message = entity.card;
				ops1.add(op11);
				messsages.set(1, ops1);
				
				List<MessageEntity> ops2 = new ArrayList<MessageEntity>();
				MessageEntity op21 = new MessageEntity();
				op21.message = entity.news;
				ops2.add(op21);
				messsages.set(2, ops2);
				mMessageViewAdapter.notifyDataSetChanged();
				
				Tabbar.setMessagePao(entity);
			}
			
			@Override
			public void onFailure(String message) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void showConversation() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看对话",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, Conversation.class);
		startActivity(intent);
	}
	
	public void showCard() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片交换请求："+String.format("%s/card/follower", CommonValue.BASE_URL),   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/card/follower", CommonValue.BASE_URL));
		startActivity(intent);
		AppClient.setMessageRead(appContext);
	}
	
	public void showNotification() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看通知："+String.format("%s/message/index", CommonValue.BASE_URL),   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/message/index", CommonValue.BASE_URL));
		startActivity(intent);
		AppClient.setMessageRead(appContext);
	}
	
}
