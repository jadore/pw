package ui;

import java.io.File;
import java.util.ArrayList;
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

import android.app.AlertDialog;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import tools.UIHelper;
import tools.UpdateManager;
import ui.adapter.IndexCardAdapter;
import ui.adapter.IndexPagerAdapter;
import ui.adapter.IphoneTreeViewAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;

public class Index extends AppActivity {
//	private ImageView avatarImageView;
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	private TextView messageView;
	private Button phoneButton;
	private Button activityButton;
	private Button cardButton;
	
	private boolean isFirst = true;
	private boolean isCFirst = true;
	private static final int PAGE1 = 0;// 页面1
	private static final int PAGE2 = 1;// 页面2
	private static final int PAGE3 = 2;// 页面3
	private ViewPager mPager;
	private List<View> mListViews;// Tab页面
	
	private ExpandableListView iphoneTreeView;
	private IphoneTreeViewAdapter mPhoneAdapter;
	private List<List<PhoneIntroEntity>> phones;
	
	private WebView webView;
	private Button loadAgainButton;
	
	private List<List<CardIntroEntity>> cards;
	private PinnedHeaderListView mPinedListView0;
	private IndexCardAdapter mCardAdapter;
	
	private ListView mListView3;
	private ProgressDialog loadingPd;
	
	private int firstVisibleItemPosition;
	private float mLastY = -1;
	
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
	protected void onResume() {
		super.onResume();
		TextView title = (TextView) findViewById(R.id.barTitleTV);
		if (!appContext.isNetworkConnected()) {
			title.setText("群友通讯录(未连接)");
		}
		else {
			title.setText("群友通讯录");
		}
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
		ShareSDK.initSDK(this);
		initUI();
		Handler jumpHandler = new Handler();
        jumpHandler.postDelayed(new Runnable() {
			public void run() {
				getCache();
				if (!appContext.isNetworkConnected()) {
		    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
		    		return;
		    	}
				UpdateManager.getUpdateManager().checkAppUpdate(Index.this, false);
				checkLogin();
			}
		}, 500);
	}
	
	private void blindBaidu() {
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, 
				Utils.getMetaValue(this, "api_key"));
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
		
		messageView = (TextView) findViewById(R.id.messageView);
		cardButton = (Button) findViewById(R.id.cardButton);
		activityButton = (Button) findViewById(R.id.activityButton);
		phoneButton = (Button) findViewById(R.id.phoneButton);
		phoneButton.setSelected(true);
		
		mPager = (ViewPager) findViewById(R.id.viewPager);
		mListViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		
		View lay1 = inflater.inflate(R.layout.index_tab0, null);
		View lay2 = inflater.inflate(R.layout.tab2, null);
		View lay0 = inflater.inflate(R.layout.tab0, null);
//		View lay3 = inflater.inflate(R.layout.tab3, null);
		
		mListViews.add(lay1);
		mListViews.add(lay2);
		mListViews.add(lay0);
//		mListViews.add(lay3);
		mPager.setAdapter(new IndexPagerAdapter(mListViews));
		mPager.setCurrentItem(PAGE1);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
		
		View footer = inflater.inflate(R.layout.index_footer, null);
		
		View header = inflater.inflate(R.layout.index_tab0_header, null);
		iphoneTreeView = (ExpandableListView) lay1.findViewById(R.id.iphone_tree_view);
		iphoneTreeView.setGroupIndicator(null);
		iphoneTreeView.addHeaderView(header);
		iphoneTreeView.addFooterView(footer);
		phones = new ArrayList<List<PhoneIntroEntity>>(4);
		
		List<PhoneIntroEntity> phone0 = new ArrayList<PhoneIntroEntity>();
		List<PhoneIntroEntity> phone1 = new ArrayList<PhoneIntroEntity>();
		List<PhoneIntroEntity> phone2 = new ArrayList<PhoneIntroEntity>();
		List<PhoneIntroEntity> phone3 = new ArrayList<PhoneIntroEntity>();
		List<PhoneIntroEntity> phone4 = new ArrayList<PhoneIntroEntity>();
		List<PhoneIntroEntity> phone5 = new ArrayList<PhoneIntroEntity>();
		phones.add(phone0);
		phones.add(phone1);
		phones.add(phone2);
		phones.add(phone3);
		phones.add(phone4);
		phones.add(phone5);
		mPhoneAdapter = new IphoneTreeViewAdapter(iphoneTreeView, this, phones);
		iphoneTreeView.setAdapter(mPhoneAdapter);
		iphoneTreeView.setSelection(0);
		iphoneTreeView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int position,
					long arg3) {
				if (iphoneTreeView.isGroupExpanded(position)) {
					iphoneTreeView.collapseGroup(position);
				}
				else {
					iphoneTreeView.expandGroup(position); 
				}
				if (position == 0 || position == 1) {
					if (phones.get(0).size() == 0 && phones.get(1).size() == 0) {
						getFamilyList();
					}
				}
				else if (position == 2 || position == 3) {
					if (phones.get(2).size() == 0 && phones.get(3).size() == 0) {
						getPhoneList();
					}
				}
				else if (position == 4 || position == 5) {
					if (phones.get(4).size() == 0 && phones.get(5).size() == 0) {
						getActivityList();
					}
				}
				return true;
			}
		});
		webView = (WebView) lay2.findViewById(R.id.webview);
		loadAgainButton = (Button) lay2.findViewById(R.id.loadAgain);
		
		mPinedListView0 = (PinnedHeaderListView) lay0.findViewById(R.id.tab0_listView);
		mPinedListView0.setDividerHeight(0);
		View footer1 = inflater.inflate(R.layout.index_footer, null);
		mPinedListView0.addFooterView(footer1);
		cards = new ArrayList<List<CardIntroEntity>>();
		mCardAdapter = new IndexCardAdapter(this, cards);
		mPinedListView0.setAdapter(mCardAdapter);
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
	
	public void showCardViewWeb(CardIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
		startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	private void showMessage() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看消息："+String.format("%s/message/index", CommonValue.BASE_URL),   // Event label
	                   null)            // Event value
	      .build()
		);
		messageView.setVisibility(View.INVISIBLE);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/message/index", CommonValue.BASE_URL));
		startActivity(intent);
	}
	
	public void showMyBarcode() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片二维码："+String.format("%s/card/mybarcode", CommonValue.BASE_URL),   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/card/mybarcode", CommonValue.BASE_URL));
		startActivity(intent);
	}
	
	public void showScan() {
		Intent intent = new Intent(this, CaptureActivity.class);
		startActivity(intent);
	}
	
	public void showFeedback() {
		Intent intent = new Intent(this, Feedback.class);
		startActivity(intent);
	}
	
	public void showUpdate() {
		UpdateManager.getUpdateManager().checkAppUpdate(this, true);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			showMessage();
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
		case R.id.cardButton:
			mPager.setCurrentItem(PAGE3);
			break;
		case R.id.navmobile:
			showMobileView();
			break;
		case R.id.friendmobile:
			showFriendCardView();
			break;
		case R.id.loadAgain:
			loadAgain();
			break;
		}
	}
	
	private void getCache() {
//		getCacheUser();
		getFamilyListFromCache();
		getPhoneListFromCache();
		getActivityListFromCache();
		getCardListFromCache();
	}
	
	private void getFamilyListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.FamilyList, appContext.getLoginUid());
		FamilyListEntity entity = (FamilyListEntity) appContext.readObject(key);
		if(entity != null){
			handlerFamilySection(entity);
		}
	}
	
	private void handlerFamilySection(FamilyListEntity entity) {
		if (entity.family.size()>0) {
			phones.set(4, entity.family);
			if (entity.family.size() <= 3) {
				iphoneTreeView.expandGroup(4);
			}
		}
		if (entity.clan.size()>0) {
			phones.set(5, entity.clan);
			if (entity.clan.size() <= 3) {
				iphoneTreeView.expandGroup(5);
			}
		}
		mPhoneAdapter.notifyDataSetChanged();
	}
	
	private void getPhoneListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.PhoneList, appContext.getLoginUid());
		PhoneListEntity entity = (PhoneListEntity) appContext.readObject(key);
		if(entity != null){
			handlerPhoneSection(entity);
		}
		
	}
	
	private void handlerPhoneSection(PhoneListEntity entity) {
		if (entity.owned.size()>0) {
			phones.set(0, entity.owned);
			if (entity.owned.size() <= 3) {
				iphoneTreeView.expandGroup(0);
			}
		}
		if (entity.joined.size()>0) {
			phones.set(1, entity.joined);
			if (entity.joined.size() <= 3) {
				iphoneTreeView.expandGroup(1);
			}
		}
		mPhoneAdapter.notifyDataSetChanged();
	}
	
	private void getActivityListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.ActivityList, appContext.getLoginUid());
		ActivityListEntity entity = (ActivityListEntity) appContext.readObject(key);
		if(entity != null){
			handlerActivitySection(entity);
		}
		
	}
	
	private void handlerActivitySection(ActivityListEntity entity) {
		if (entity.owned.size()>0) {
			phones.set(2, entity.owned);
			if (entity.owned.size() <= 3) {
				iphoneTreeView.expandGroup(2);
			}
		}
		if (entity.joined.size()>0) {
			phones.set(3, entity.joined);
			if (entity.joined.size() <= 3) {
				iphoneTreeView.expandGroup(3);
			}
		}
		mPhoneAdapter.notifyDataSetChanged();
	}
	
	private void getCardListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.CardList, appContext.getLoginUid());
		CardListEntity entity = (CardListEntity) appContext.readObject(key);
		if(entity == null){
			addCardOp();
			mCardAdapter.notifyDataSetChanged();
			return;
		}
		cards.clear();
		if (entity.owned.size()>0) {
			cards.add(entity.owned);
		}
		addCardOp();
		mCardAdapter.notifyDataSetChanged();
	}
	
	private void checkLogin() {
//		loadingPd = UIHelper.showProgress(this, null, null, true);
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
    	
		AppClient.autoLogin(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UserEntity user = (UserEntity)data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					appContext.saveLoginInfo(user);
					showReg(user);
					getFamilyList();
					getPhoneList();
					getActivityList();
					getUnReadMessage();
					if (!Utils.hasBind(getApplicationContext())) {
						blindBaidu();
					}
					WebView webview = (WebView) findViewById(R.id.webview);
					webview.loadUrl(CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign())  ;
					webview.setWebViewClient(new WebViewClient() {
						public boolean shouldOverrideUrlLoading(WebView view, String url) {
							view.loadUrl(url);
							return true;
						};
					});
					break;
				case CommonValue.USER_NOT_IN_ERROR:
					forceLogout();
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), user.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			@Override
			public void onFailure(String message) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				Logger.i(e);
			}
		});
	}
	
	private void showReg(UserEntity user) {
		String reg = "手机用户.*";
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(user.nickname);
		if (m.matches()) {
			Intent intent = new Intent(this, Register.class);
			intent.putExtra("mobile", user.username);
			intent.putExtra("jump", false);
	        startActivity(intent);
		}
	}
	
	private void showLogin() {
		appContext.setUserLogout();
		Intent intent = new Intent(this,LoginCode1.class);
        startActivity(intent);
        finish();
        
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
	
	private void getFamilyList() {
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getFamilyList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				FamilyListEntity entity = (FamilyListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handlerFamilySection(entity);
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
					mPhoneAdapter.notifyDataSetChanged();
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
	
	private void getActivityList() {
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getActivityList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				ActivityListEntity entity = (ActivityListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handlerActivitySection(entity);
					mPhoneAdapter.notifyDataSetChanged();
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
	
	private void getCardList() {
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getCardList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				CardListEntity entity = (CardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					cards.clear();
					if (entity.owned.size()>0) {
						cards.add(entity.owned);
					}
					else {
						
					}
					addCardOp();
					mCardAdapter.notifyDataSetChanged();
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
				e.printStackTrace();
				Logger.i(e);
			}
		});
	}
	
	private void getRecommendList() {
		AppClient.getRecommendList(appContext, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				RecommendListEntity entity = (RecommendListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					showLogin();
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
	
	private void getUnReadMessage() {
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getUnReadMessage(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				MessageUnReadEntity entity = (MessageUnReadEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					if (entity.news.equals("0")) {
						messageView.setVisibility(View.INVISIBLE);
					}
					else {
						try {
							int news = Integer.valueOf(entity.news);
							String n = news>99?"99+":entity.news;
							messageView.setText(n);
							messageView.setVisibility(View.VISIBLE);
						} catch (Exception e) {
							
						}
					}
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
				e.printStackTrace();
				Logger.i(e);
			}
		});
	}
	
	// ViewPager页面切换监听
	public class MyOnPageChangeListener implements OnPageChangeListener {
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case PAGE1:// 切换到页卡1
				phoneButton.setSelected(true);
				activityButton.setSelected(false);
				cardButton.setSelected(false);
				if (phones.get(0).size() == 0 && phones.get(1).size() == 0) {
					getPhoneList();
				}
				if (phones.get(2).size() == 0 && phones.get(3).size() == 0) {
					getActivityList();
				}
				break;
			case PAGE2:// 切换到页卡2
				if (isFirst) {
					Handler jumpHandler = new Handler();
			        jumpHandler.postDelayed(new Runnable() {
						public void run() {
							initWebData();
						}
					}, 200);
					isFirst = false;
				}
				phoneButton.setSelected(false);
				activityButton.setSelected(true);
				cardButton.setSelected(false);
				break;
			case PAGE3:// 切换到页卡3
				if (isCFirst) {
					getCardList();
					Logger.i("ddd");
					isCFirst = false;
				}
				phoneButton.setSelected(false);
				activityButton.setSelected(false);
				cardButton.setSelected(true);
				break;
			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	}
	
	public void oks(String title, String text, String link, String filePath) {
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle(title);
			if (!StringUtils.isEmpty(filePath)) {
				oks.setImagePath(filePath);
			}
			else {
				String cachePath = cn.sharesdk.framework.utils.R.getCachePath(this, null);
				oks.setImagePath(cachePath + "logo.png");
			}
			oks.setText("#群友通讯录#" + text + "\n" + link);
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
			String text = (!StringUtils.isEmpty(phoneIntro.content)?phoneIntro.content:String.format("您好，我在征集%s通讯录，点击下面的链接进入填写，填写后可申请查看群友的通讯录等，谢谢。", phoneIntro.title));
			oks(phoneIntro.title, text, phoneIntro.link, filePath);
		}
		else {
			String text = (!StringUtils.isEmpty(phoneIntro.content)?phoneIntro.content:String.format("您好，我发起了%s活动，点击参加。", phoneIntro.title));
			oks(phoneIntro.title, text, phoneIntro.link, filePath);
		}
	}
	
	public void showSharePre(final boolean silent, final String platform, final PhoneIntroEntity phoneIntro) {
		if (StringUtils.isEmpty(phoneIntro.logo)) {
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
	
	public void cardShare(boolean silent, String platform, CardIntroEntity card, String filePath) {
		try {
			String text = (!StringUtils.isEmpty(card.intro)?card.intro:String.format("您好，我叫%s，这是我的名片，请多多指教。",card.realname));
			oks(card.realname, text, card.link, filePath);
		} catch (Exception e) {
			Logger.i(e);
		}
	}
	
	public void cardSharePre(final boolean silent, final String platform, final CardIntroEntity card) {
		if (StringUtils.isEmpty(appContext.getLoginInfo().headimgurl)) {
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
				loadingPd = UIHelper.showProgress(Index.this, null, null, true);
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
	
	private void addCardOp() {
		List<CardIntroEntity> ops = new ArrayList<CardIntroEntity>();
		CardIntroEntity op1 = new CardIntroEntity();
		op1.realname = "我微友通讯录二维码";
		op1.department = CommonValue.subTitle.subtitle4;
		op1.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op1.position = "";
		ops.add(op1);
		CardIntroEntity op2 = new CardIntroEntity();
		op2.realname = "扫一扫";
		op2.department = CommonValue.subTitle.subtitle5;
		op2.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op2.position = "";
		ops.add(op2);
		cards.add(ops);
		
		List<CardIntroEntity> ops2 = new ArrayList<CardIntroEntity>();
		CardIntroEntity op21 = new CardIntroEntity();
		op21.realname = "客服反馈";
		op21.department = CommonValue.subTitle.subtitle6;
		op21.position = "";
		op21.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
		ops2.add(op21);
		
		CardIntroEntity op22 = new CardIntroEntity();
		op22.realname = "检查版本";
		op22.department = "当前版本:"+getCurrentVersionName();
		op22.position = "";
		op22.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
		ops2.add(op22);
		
		CardIntroEntity op23 = new CardIntroEntity();
		op23.realname = "注销";
		op23.department = "退出当前账号重新登录";
		op23.position = "";
		op23.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
		ops2.add(op23);
		
		cards.add(ops2);
		
	}
	
	/**
	 * 获取当前客户端版本信息
	 */
	private String  getCurrentVersionName(){
		String versionName = null;
        try { 
        	PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
        	versionName = info.versionName;
        } catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
        return versionName;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.CreateViewUrlAndRequest.ContactCreat:
			getPhoneList();
			mPager.setCurrentItem(PAGE1);
			break;
		case CommonValue.CreateViewUrlAndRequest.ActivityCreateCreat:
			getActivityList();
			int result1 = data.getIntExtra("resultcode", 0);
			if (result1 == CommonValue.CreateViewJSType.goPhonebookView) {
				PhoneIntroEntity entity = new PhoneIntroEntity();
				entity.code = data.getStringExtra("resultdata");
				entity.content = " ";
				showActivityViewWeb(entity);
			}
			mPager.setCurrentItem(PAGE1);
			break;
		case CommonValue.CreateViewUrlAndRequest.CardCreat:
			getCardList();
			mPager.setCurrentItem(PAGE3);
			break;
		case CommonValue.PhonebookViewUrlRequest.editPhoneview:
			getPhoneList();
			break;
		case CommonValue.ActivityViewUrlRequest.editActivity:
			getActivityList();
			break;
		case CommonValue.CardViewUrlRequest.editCard:
			getCardList();
			break;
		}
	}
	
	private void showFinder(String url) {
		Logger.i(url);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
		startActivity(intent);
	}
	
	private void initWebData() {
		String url = CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign() ;
		WebSettings webseting = webView.getSettings();  
		webseting.setJavaScriptEnabled(true);
		webseting.setLightTouchEnabled(true);
	    webseting.setDomStorageEnabled(true);             
	    webseting.setAppCacheMaxSize(1024*1024*8);//设置缓冲大小，我设的是8M  
	    String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();      
        webseting.setAppCachePath(appCacheDir);  
        webseting.setAllowFileAccess(true);  
        webseting.setAppCacheEnabled(true); 
//        webView.addJavascriptInterface(mJS, "pbwc");
        
        if (appContext.isNetworkConnected()) {
        	webseting.setCacheMode(WebSettings.LOAD_DEFAULT); 
		}
        else {
        	webseting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); 
        }
		
		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				showFinder(url);
				return true;
			}
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Logger.i(errorCode+"");
				switch (errorCode) {
				case -2:
					webView.setVisibility(View.INVISIBLE);
					break;
				}
				loadAgainButton.setVisibility(View.VISIBLE);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});
		webView.setWebChromeClient(new WebChromeClient() {
		    public void onProgressChanged(WebView view, int progress) {
		        setTitle("页面加载中，请稍候..." + progress + "%");
		        setProgress(progress * 100);
		        if (progress == 100) {
//		        	UIHelper.dismissProgress(loadingPd);
		        	indicatorImageView.clearAnimation();
		        	indicatorImageView.setVisibility(View.INVISIBLE);
		        }
		    }
		    
		    @Override
		    public void onReachedMaxAppCacheSize(long spaceNeeded,
		    		long quota, QuotaUpdater quotaUpdater) {
		    	quotaUpdater.updateQuota(spaceNeeded * 2);  
		    }
		});
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		webView.loadUrl(url);
		if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
	}
	
	private void loadAgain() {
		loadAgainButton.setVisibility(View.INVISIBLE);
		webView.setVisibility(View.VISIBLE);
		String url = CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign() ;
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		webView.loadUrl(url);
		if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
	}
	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("确定退出吗?")
		.setNeutralButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AppManager.getAppManager().finishAllActivity();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).show();
	}
	
	public void logout() {
		new AlertDialog.Builder(this).setTitle("确定注销本账号吗?")
		.setNeutralButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AppClient.Logout(appContext);
				CookieStore cookieStore = new PersistentCookieStore(Index.this);  
				cookieStore.clear();
				AppManager.getAppManager().finishAllActivity();
				appContext.setUserLogout();
				Intent intent = new Intent(Index.this, LoginCode1.class);
				startActivity(intent);
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).show();
	}

}
