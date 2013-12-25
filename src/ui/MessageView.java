package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppException;
import tools.AppManager;
import tools.UIHelper;
import ui.adapter.MessageViewAdapter;
import bean.ActivityViewEntity;
import bean.Entity;
import bean.FriendCardListEntity;
import bean.MessageEntity;
import bean.MessageListEntity;
import bean.Result;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MessageView extends AppActivity {
	private List<MessageEntity> messages = new ArrayList<MessageEntity>();
	private ListView mListView;
	private MessageViewAdapter mMessageViewAdapter;
	private ProgressDialog loadingPd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_view);
		initUI();
		getMeesageFromCache();
	}
	
	private void initUI() {
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setDividerHeight(0);
		mMessageViewAdapter = new MessageViewAdapter(this, messages);
		mListView.setAdapter(mMessageViewAdapter);
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
	
	private void getMeesageFromCache () {
		String key = String.format("%s-%s", CommonValue.CacheKey.MessageList, appContext.getLoginUid());
		MessageListEntity entity = (MessageListEntity) appContext.readObject(key);
		if(entity == null){
			getMessage();
			return;
		}
		handleMessage(entity);
		getMessage();
	}
	
	private void getMessage() {
		if (!appContext.isNetworkConnected()) {
			UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
			return;
		}
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.getMessageList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				MessageListEntity entity = (MessageListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handleMessage(entity);
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				((AppException)e).makeToast(getApplicationContext());
			}
		});
		AppClient.setMessageRead(appContext);
	}
	
	private void handleMessage(MessageListEntity entity) {
		if (entity.messages.size()>0) {
			messages.clear();
			messages.addAll(entity.messages);
			mMessageViewAdapter.notifyDataSetChanged();
		}
	}
}
