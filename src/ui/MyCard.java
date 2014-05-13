package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.MyCardAdapter;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.Result;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MyCard extends AppActivity implements OnRefreshListener{
	private int lvDataState;
	private List<CardIntroEntity> cards = new ArrayList<CardIntroEntity>();
	private MyCardAdapter xAdapter;
	private ListView xListView;
	private SwipeRefreshLayout swipeLayout;
	
	  
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
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.xrefresh);
		xListView = (ListView) findViewById(R.id.xlistview);
		xAdapter = new MyCardAdapter(this, cards, imageLoader);
		xListView.setAdapter(xAdapter);
		swipeLayout.setOnRefreshListener(this);
	    swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
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
    		swipeLayout.setRefreshing(false);
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
				lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				swipeLayout.setRefreshing(false);
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
				lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				swipeLayout.setRefreshing(false);
			}
			@Override
			public void onError(Exception e) {
				Crashlytics.logException(e);
				lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				swipeLayout.setRefreshing(false);
			}
		});
	}
	
	private ProgressDialog loadingPd;
	public void oks(String title, String text, String link, String filePath) {
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle(title);
			if (StringUtils.notEmpty(filePath)) {
				oks.setImagePath(filePath);
			}
			else {
				String cachePath = cn.sharesdk.framework.utils.R.getCachePath(this, null);
				oks.setImagePath(cachePath + "logo.png");
			}
			oks.setText(text + "\n" + link);
			oks.setUrl(link);
			oks.setSiteUrl(link);
			oks.setSite(link);
			oks.setTitleUrl(link);
			oks.setLatitude(23.056081f);
			oks.setLongitude(113.385708f);
			oks.setSilent(false);
			oks.show(this);
		} catch (Exception e) {
			Logger.i(e);
		}
	}
	
	public void cardShare(boolean silent, String platform, CardIntroEntity card, String filePath) {
		try {
			String text = (StringUtils.notEmpty(card.intro)?card.intro:String.format("您好，我叫%s，这是我的名片，请多多指教。",card.realname));
			oks(card.realname, text, card.link, filePath);
		} catch (Exception e) {
			Logger.i(e);
		}
	}

	public void cardSharePre(final boolean silent, final String platform, final CardIntroEntity card) {
		if (StringUtils.empty(appContext.getLoginInfo().headimgurl)) {
			cardShare(silent, platform, card, "");
			return;
		}
		String storageState = Environment.getExternalStorageState();	
		if(storageState.equals(Environment.MEDIA_MOUNTED)){
			String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qy/" + MD5Util.getMD5String(appContext.getLoginInfo().headimgurl) + ".png";
			File file = new File(savePath);
			if (file.exists()) {
				cardShare(silent, platform, card, savePath);
			}
			else {
				loadingPd = UIHelper.showProgress(this, null, null, true);
				AppClient.downFile(this, appContext, appContext.getLoginInfo().headimgurl, ".png", new FileCallback() {
					@Override
					public void onSuccess(String filePath) {
						UIHelper.dismissProgress(loadingPd);
						cardShare(silent, platform, card, filePath);
					}

					@Override
					public void onFailure(String message) {
						UIHelper.dismissProgress(loadingPd);
						cardShare(silent, platform, card, "");
					}

					@Override
					public void onError(Exception e) {
						UIHelper.dismissProgress(loadingPd);
						cardShare(silent, platform, card, "");
					}
				});
			}
		}
	}

	@Override
	public void onRefresh() {
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			getCardList();
		}
		else {
			swipeLayout.setRefreshing(false);
		}
	}
}
