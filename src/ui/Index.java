package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.CookieStore;

import baidupush.Utils;
import bean.ActivityListEntity;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.FamilyListEntity;
import bean.MessageUnReadEntity;
import bean.PhoneIntroEntity;
import bean.PhoneListEntity;
import bean.RecommendListEntity;
import bean.Result;
import bean.UserEntity;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.zxing.client.android.CaptureActivity;
import com.loopj.android.http.PersistentCookieStore;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import config.CommonValue;
import db.manager.WeFriendManager;
import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebStorage.QuotaUpdater;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import service.IPolemoService;
import tools.AppManager;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import tools.UIHelper;
import tools.UpdateManager;
import ui.adapter.IndexPagerAdapter;
import ui.adapter.IndexPhoneAdapter;
import ui.adapter.IndexSquareAdapter;
import widget.XListView;
import widget.XListView.IXListViewListener;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;

public class Index extends AppActivity implements IXListViewListener{
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	private Button phoneButton;
	private Button activityButton;
	
	
	private static final int PAGE1 = 0;// 页面1
	private static final int PAGE2 = 1;// 页面2
	private ViewPager mPager;
	private List<View> mListViews;// Tab页面
	
	private XListView xListView;
	private List<PhoneIntroEntity> allPhonebook = new ArrayList<PhoneIntroEntity>();
	private List<PhoneIntroEntity> phones = new ArrayList<PhoneIntroEntity>();
	private List<PhoneIntroEntity> activitys = new ArrayList<PhoneIntroEntity>();
	private IndexPhoneAdapter phoneAdapter;
	
	private ProgressDialog loadingPd;
	
	private XListView xListViewForSqure;
	private List<PhoneIntroEntity> phonesForSqure = new ArrayList<PhoneIntroEntity>();
	private IndexSquareAdapter phoneAdapterForSqure;
	private int lvDataState;
	private int currentPage;
	private boolean isSquare;
	
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
	protected void onDestroy() {
		super.onDestroy();
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
		ShareSDK.initSDK(this);
		initUI();
		getCache();
		isSquare = false;
	}
	
	private void initUI() {
		indicatorImageView = (ImageView) findViewById(R.id.xindicator);
		indicatorAnimation = AnimationUtils.loadAnimation(this, R.anim.refresh_button_rotation);
		indicatorAnimation.setDuration(500);
		indicatorAnimation.setInterpolator(new Interpolator() {
		    private final int frameCount = 10;
		    @Override
		    public float getInterpolation(float input) {
		        return (float)Math.floor(input*frameCount)/frameCount;
		    }
		});
		
		activityButton = (Button) findViewById(R.id.activityButton);
		phoneButton = (Button) findViewById(R.id.phoneButton);
		phoneButton.setSelected(true);
		
		mPager = (ViewPager) findViewById(R.id.viewPager);
		mListViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		
		View lay0 = inflater.inflate(R.layout.tab0, null);
		View lay1 = inflater.inflate(R.layout.tab1, null);
		
		mListViews.add(lay0);
		mListViews.add(lay1);
		
		mPager.setAdapter(new IndexPagerAdapter(mListViews));
		mPager.setCurrentItem(PAGE1);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
		
		View footer = inflater.inflate(R.layout.index_footer, null);
		
		xListView = (XListView) lay0.findViewById(R.id.tab0_listView);
		xListView.addFooterView(footer);
		xListView.setPullLoadEnable(false);
		xListView.setPullRefreshEnable(false);
		xListView.setDividerHeight(0);
		phoneAdapter = new IndexPhoneAdapter(this, allPhonebook);
		xListView.setAdapter(phoneAdapter);
		
		xListViewForSqure = (XListView) lay1.findViewById(R.id.tab1_listView);
		xListViewForSqure.setPullLoadEnable(false);
		xListViewForSqure.setPullRefreshEnable(false);
		xListViewForSqure.setDividerHeight(0);
		phoneAdapterForSqure = new IndexSquareAdapter(this, phonesForSqure);
		xListViewForSqure.setAdapter(phoneAdapterForSqure);
	}
	
	public void showMobileView() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看手机通讯录",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, HomeContactActivity.class);
		startActivity(intent);
	}
	
	public void showFriendCardView() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看微友通讯录",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, WeFriendCard.class);
		startActivity(intent);
	}
	
	public void showActivityViewWeb(PhoneIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看聚会："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
	    startActivityForResult(intent, CommonValue.ActivityViewUrlRequest.editActivity);
	}
	
	public void showPhoneViewWeb(PhoneIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看群友通讯录："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}
	
	public void showPhoneTimeline(PhoneIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看群友通讯录动态："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, PhoneTimeline.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.PhoneView, entity);
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			break;
		case R.id.rightBarButton:
			showContactDialog();
			break;
		case R.id.avatarImageView:
			break;
		case R.id.phoneButton:
			mPager.setCurrentItem(PAGE1);
			break;
		case R.id.activityButton:
			mPager.setCurrentItem(PAGE2);
			break;
		case R.id.navmobile:
			showMobileView();
			break;
		case R.id.friendmobile:
			showFriendCardView();
			break;
		case R.id.loadAgain:
			break;
		}
	}
	
	private void getCache() {
		getPhoneListFromCache();
		getPhoneList();
		getSquareListFromCache();
	}
	
	private void getPhoneListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.PhoneList, appContext.getLoginUid());
		PhoneListEntity entity = (PhoneListEntity) appContext.readObject(key);
		if(entity != null){
			handlerPhoneSection(entity);
		}
		getActivityListFromCache();
	}
	
	private void getPhoneList() {
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getPhoneList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				PhoneListEntity entity = (PhoneListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handlerPhoneSection(entity);
					getActivityList();
					break;
				case CommonValue.USER_NOT_IN_ERROR:
					forceLogout();
					break;
				default:
//					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				Logger.i(e);
			}
		});
	}
	
	private void handlerPhoneSection(PhoneListEntity entity) {
		allPhonebook.removeAll(phones);
		phones.clear();
		phones.addAll(entity.owned);
		phones.addAll(entity.joined);
		allPhonebook.addAll(phones);
		Collections.sort(allPhonebook);
		phoneAdapter.notifyDataSetChanged();
	}
	
	private void getSquareListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.SquareList, appContext.getLoginUid());
		RecommendListEntity entity = (RecommendListEntity) appContext.readObject(key);
		if(entity != null){
			handlerSquare(entity, UIHelper.LISTVIEW_ACTION_INIT);
		}
	}
	
	private void getSquareList(final int action) {
//		loadingPd = UIHelper.showProgress(Index.this, null, null, true);
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getPhoneSquareList(appContext, currentPage+"", "", new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
//				UIHelper.dismissProgress(loadingPd);
				RecommendListEntity entity = (RecommendListEntity)data;
//				switch (entity.getError_code()) {
//				case Result.RESULT_OK:
					handlerSquare(entity, action);
//					break;
//				case CommonValue.USER_NOT_IN_ERROR:
//					forceLogout();
//					break;
//				default:
//					break;
//				}
			}
			
			@Override
			public void onFailure(String message) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void onError(Exception e) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
			}
		});
	}
	
	private void handlerSquare(RecommendListEntity entity, int action) {
		switch (action) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
			phonesForSqure.clear();
			phonesForSqure.addAll(entity.squares);
			break;
		case UIHelper.LISTVIEW_ACTION_SCROLL:
			phonesForSqure.addAll(entity.squares);
			break;
		}
		if(entity.next >= 1){					
			lvDataState = UIHelper.LISTVIEW_DATA_MORE;
		}
		else if (entity.next == -1) {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
		}
		phoneAdapterForSqure.notifyDataSetChanged();
	}
	
	private void getActivityListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.ActivityList, appContext.getLoginUid());
		ActivityListEntity entity = (ActivityListEntity) appContext.readObject(key);
		if(entity != null){
			handlerActivitySection(entity);
		}
	}
	
	private void getActivityList() {
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getActivityList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				ActivityListEntity entity = (ActivityListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handlerActivitySection(entity);
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
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				Logger.i(e);
			}
		});
	}
	
	private void handlerActivitySection(ActivityListEntity entity) {
		allPhonebook.removeAll(activitys);
		activitys.clear();
		activitys.addAll(entity.owned);
		activitys.addAll(entity.joined);
		allPhonebook.addAll(activitys);
		Collections.sort(allPhonebook);
		phoneAdapter.notifyDataSetChanged();
	}
	
	private String[] lianxiren1 = new String[] { "创建通讯录", "创建活动", "创建我的名片"};
	
	private void showContactDialog(){
		final EasyTracker easyTracker = EasyTracker.getInstance(Index.this);
		new AlertDialog.Builder(this).setTitle("").setItems(lianxiren1,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					easyTracker.send(MapBuilder
				      .createEvent("ui_action",     // Event category (required)
				                   "button_press",  // Event action (required)
				                   "创建通讯录",   // Event label
				                   null)            // Event value
				      .build()
					);
					showCreate(CommonValue.CreateViewUrlAndRequest.ContactCreateUrl, CommonValue.CreateViewUrlAndRequest.ContactCreat);
					break;
				case 1:
					easyTracker.send(MapBuilder
				      .createEvent("ui_action",     // Event category (required)
				                   "button_press",  // Event action (required)
				                   "创建聚会",   // Event label
				                   null)            // Event value
				      .build()
					);
					showCreate(CommonValue.CreateViewUrlAndRequest.ActivityCreateUrl, CommonValue.CreateViewUrlAndRequest.ActivityCreateCreat);
					break;
				case 2:
					easyTracker.send(MapBuilder
						      .createEvent("ui_action",     // Event category (required)
						                   "button_press",  // Event action (required)
						                   "创建名片",   // Event label
						                   null)            // Event value
						      .build()
							);
					showCreate(CommonValue.CreateViewUrlAndRequest.CardCreateUrl, CommonValue.CreateViewUrlAndRequest.CardCreat);
					break;
				}
			}
		}).show();
	}
	
	private void showCreate(String url, int RequestCode) {
		Intent intent = new Intent(this,QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
        startActivityForResult(intent, RequestCode);
	}
	
	// ViewPager页面切换监听
	public class MyOnPageChangeListener implements OnPageChangeListener {
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case PAGE1:// 切换到页卡1
				phoneButton.setSelected(true);
				activityButton.setSelected(false);
				break;
			case PAGE2:// 切换到页卡2
				phoneButton.setSelected(false);
				activityButton.setSelected(true);
				if (!isSquare) {
					isSquare = true;
					getSquareList(UIHelper.LISTVIEW_ACTION_INIT);
				}
				break;
			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
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
	
	public void showShare(boolean silent, String platform, PhoneIntroEntity phoneIntro, String filePath) {
		if (phoneIntro.phoneSectionType.equals(CommonValue.PhoneSectionType.OwnedSectionType) 
			|| phoneIntro.phoneSectionType.equals(CommonValue.PhoneSectionType.JoinedSectionType)
			|| phoneIntro.phoneSectionType.equals(CommonValue.FamilySectionType.FamilySectionType)
			|| phoneIntro.phoneSectionType.equals(CommonValue.FamilySectionType.ClanSectionType)) {
			String text = (StringUtils.notEmpty(phoneIntro.content)?phoneIntro.content:String.format("您好，我在征集%s通讯录，点击下面的链接进入填写，填写后可申请查看群友的通讯录等，谢谢。", phoneIntro.title));
			oks(phoneIntro.title, text, phoneIntro.link, filePath);
		}
		else {
			String text = (StringUtils.notEmpty(phoneIntro.content)?phoneIntro.content:String.format("您好，我发起了%s活动，点击参加。", phoneIntro.title));
			oks(phoneIntro.title, text, phoneIntro.link, filePath);
		}
	}
	
	public void showSharePre(final boolean silent, final String platform, final PhoneIntroEntity phoneIntro) {
		if (StringUtils.empty(phoneIntro.logo)) {
			showShare(silent, platform, phoneIntro, "");
			return;
		}
		String storageState = Environment.getExternalStorageState();	
		if(storageState.equals(Environment.MEDIA_MOUNTED)){
			String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qy/" + MD5Util.getMD5String(phoneIntro.logo) + ".png";
			File file = new File(savePath);
			if (file.exists()) {
				showShare(silent, platform, phoneIntro, savePath);
			}
			else {
				loadingPd = UIHelper.showProgress(Index.this, null, null, true);
				AppClient.downFile(this, appContext, phoneIntro.logo, ".png", new FileCallback() {
					@Override
					public void onSuccess(String filePath) {
						UIHelper.dismissProgress(loadingPd);
						showShare(silent, platform, phoneIntro, filePath);
					}
					
					@Override
					public void onFailure(String message) {
						UIHelper.dismissProgress(loadingPd);
						showShare(silent, platform, phoneIntro, "");
					}
					
					@Override
					public void onError(Exception e) {
						UIHelper.dismissProgress(loadingPd);
						showShare(silent, platform, phoneIntro, "");
					}
				});
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
	}

	@Override
	public void onRefresh(int id) {
		currentPage = 1;
		getSquareList(UIHelper.LISTVIEW_ACTION_REFRESH);
	}

	@Override
	public void onLoadMore(int id) {
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			currentPage++;
			getSquareList(UIHelper.LISTVIEW_ACTION_SCROLL);
		}
	}
	
}
