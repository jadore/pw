package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.MyCardAdapter;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.Result;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MyCard extends AppActivity{
	private List<CardIntroEntity> cards = new ArrayList<CardIntroEntity>();
	private MyCardAdapter xAdapter;
	private ListView xListView;
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  
	}

	@Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mycard);
		initUI();
		getCardListFromCache();
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
	
	public void showCardViewWeb(CardIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
//		Intent intent = new Intent(this, QYWebView.class);
//		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
//		startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
		Intent intent = new Intent(context, CardView.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	private void initUI() {
		xListView = (ListView) findViewById(R.id.xlistview);
		xAdapter = new MyCardAdapter(this, cards, imageLoader);
		xListView.setAdapter(xAdapter);
	}
	
	private void getCardListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.CardList, appContext.getLoginUid());
		CardListEntity entity = (CardListEntity) appContext.readObject(key);
		if(entity != null){
			cards.clear();
			if (entity.owned.size()>0) {
				cards.addAll(entity.owned);
			}
			xAdapter.notifyDataSetChanged();
		}
		getCardList();
	}
	
	private void getCardList() {
		if (!appContext.isNetworkConnected() && cards.isEmpty()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
		AppClient.getCardList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				CardListEntity entity = (CardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					cards.clear();
					if (entity.owned.size()>0) {
						cards.addAll(entity.owned);
					}
					xAdapter.notifyDataSetChanged();
					break;
				case CommonValue.USER_NOT_IN_ERROR:
					forceLogout();
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				Logger.i(e);
			}
		});
	}
}
