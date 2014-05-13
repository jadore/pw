package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceActivity.Header;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import baidupush.Utils;
import bean.CardIntroEntity;
import bean.CodeEntity;
import bean.Entity;
import bean.FriendCardListEntity;
import bean.OpenidListEntity;
import bean.Result;
import bean.UserEntity;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.utils.L;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.CommonValue.LianXiRenType;
import config.QYRestClient;
import db.manager.WeFriendManager;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import tools.UpdateManager;
import ui.adapter.FriendCardAdapter;
import widget.MyLetterListView;
import widget.MyLetterListView.OnTouchingLetterChangedListener;
import widget.XListView.IXListViewListener;
import widget.XListView;

public class WeFriendCard extends AppActivity implements OnItemClickListener, IXListViewListener {
	private TextView messageView;
	
	private List<CardIntroEntity> mobiles = new ArrayList<CardIntroEntity>();
	
	private int lvDataState;
	private int currentPage;
	private ProgressDialog loadingPd;
	
	private List<CardIntroEntity> bilaterals = new ArrayList<CardIntroEntity>();
	
	private TextView nobilateralView;
	
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	private View searchHeaderView;
	private EditText editText;
	private Button searchDeleteButton;
	
	private String keyword;
	
	private XListView xlistView;
	private List<CardIntroEntity> contactors = new ArrayList<CardIntroEntity>();
	private FriendCardAdapter mBilateralAdapter;
	
	private MyAsyncQueryHandler asyncQuery;
	private Uri uri ;
	private List<String> contactids;
	
	private static final int count = 200;
	  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		letterListView.setVisibility(View.VISIBLE);
	}
	  
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wefriendcard);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		initUI();
		currentPage = 1;
		keyword = "";
//		Handler jumpHandler = new Handler();
//        jumpHandler.postDelayed(new Runnable() {
//			public void run() {
////				getFriendCardFromCache();
				editText.setHint("你共有"+appContext.getDeg2()+"位二度好友");
				getAllFriend();
//			}
//		}, 100);
	}
	
	private void initUI() {
		overlay = (TextView) findViewById(R.id.fast_position);
		letterListView = (MyLetterListView) findViewById(R.id.ContactLetterListView);
		messageView = (TextView) findViewById(R.id.messageView);
		searchHeaderView = getLayoutInflater().inflate(R.layout.search_headview, null);
		searchHeaderView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				letterListView.setVisibility(View.INVISIBLE);
				Intent intent = new Intent(WeFriendCard.this, WeFriendCardSearch.class);
	            startActivityForResult(intent, 12);
			}
		});
		editText = (EditText) searchHeaderView.findViewById(R.id.searchEditView);
		editText.setFocusable(false);
		searchDeleteButton = (Button) searchHeaderView.findViewById(R.id.searchDeleteButton);
		
		nobilateralView = (TextView) findViewById(R.id.noting_view);
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
		letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
		alphaIndexer = new HashMap<String, Integer>();
		
		xlistView = (XListView)findViewById(R.id.xlistview);
		xlistView.setPullRefreshEnable(false);
		xlistView.setPullLoadEnable(false);
		xlistView.setXListViewListener(this, 0);
        xlistView.setDividerHeight(0);
        xlistView.addHeaderView(searchHeaderView, null, false);
		mBilateralAdapter = new FriendCardAdapter(this, contactors);
		xlistView.setAdapter(mBilateralAdapter);
		xlistView.setOnItemClickListener(this);
	}
	
	private void getFriendCardFromCache() {
		asyncQuery.startQuery(0, null, uri, projection, null, null, "sort_key COLLATE LOCALIZED asc"); 
	}
	
	private void checkLogin() {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.autoLogin(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				UserEntity user = (UserEntity)data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					appContext.saveLoginInfo(user);
					showReg(user);
					getAllFriend();
//					if (!Utils.hasBind(getApplicationContext())) {
//						blindBaidu();
//					}
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
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
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
	
	public void showMessage() {
		Intent intent = new Intent(this, MessageView.class);
		startActivity(intent);
	}
	
//	private void blindBaidu() {
//		PushManager.startWork(getApplicationContext(),
//				PushConstants.LOGIN_TYPE_API_KEY, 
//				Utils.getMetaValue(this, "api_key"));
//	}
	
	private void getAllFriend() {
		final Handler handler2 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					currentPage = 1;
					getFriendCard(currentPage, "", count+"", UIHelper.LISTVIEW_ACTION_INIT);
					asyncQuery.startQuery(0, null, uri, projection, null, null, "sort_key COLLATE LOCALIZED asc"); 
				}
				else {
					if (appContext.isNetworkConnected()) {
						asyncQuery.startQuery(0, null, uri, projection, null, null, "sort_key COLLATE LOCALIZED asc");
						getWeFriendsFromDB(count+"");
						currentPage = 1;
						getFriendCard(currentPage, "", count+"", UIHelper.LISTVIEW_ACTION_INIT);
			    	}
					else {
						asyncQuery.startQuery(0, null, uri, projection, null, null, "sort_key COLLATE LOCALIZED asc");
						getWeFriendsFromDB(null);
					}
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				int temp = WeFriendManager.getInstance(WeFriendCard.this).getWeFriendCount();
				if (temp == 0) {
					handler2.sendEmptyMessage(1);
				}
				else {
			        handler2.sendEmptyMessage(2);
				}
			}
		});
	}
	
	private void getFriendCard(int page, String kw, String count, final int action) {
//		if (loadingPd == null) {
//			loadingPd = UIHelper.showProgress(WeFriendCard.this, null, null);
//		}
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getChatFriendCard(this, appContext, page+"", kw, count, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
				
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				FriendCardListEntity entity = (FriendCardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handleFriends(entity, action);
					break;
				default:
//					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
				xlistView.stopLoadMore();
			}
			@Override
			public void onError(Exception e) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				xlistView.stopLoadMore();
			}
		});
	}
	
	private void handleFriends(FriendCardListEntity entity, int action) {
		saveListInDB(entity);
		switch (action) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
			contactors.removeAll(bilaterals);
			bilaterals.clear();
			bilaterals.addAll(entity.u);
			break;

		default:
			contactors.removeAll(bilaterals);
			bilaterals.addAll(entity.u);
			break;
		}
		if(entity.ne >= 1){					
			lvDataState = UIHelper.LISTVIEW_DATA_MORE;
			xlistView.setPullLoadEnable(true);
		}
		else if (entity.ne == -1) {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
			xlistView.setPullLoadEnable(false);
		}
		xlistView.stopLoadMore();
		contactors.addAll(bilaterals);
		try {
			Collections.sort(contactors);
		}
		catch (Exception e) {
			Crashlytics.logException(e);
		}
		mBilateralAdapter.notifyDataSetChanged();
		indicatorImageView.clearAnimation();
		indicatorImageView.setVisibility(View.INVISIBLE);
		sortPY();
		letterListView.setVisibility(View.VISIBLE);
	}
	
	private void saveListInDB(final FriendCardListEntity entity) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
//				if(entity.ne >= 1){					
//					++currentPage;
//					getFriendCard(currentPage, "", count+"", UIHelper.LISTVIEW_ACTION_INIT);
//				}
//				else if (entity.ne == -1) {
//					UIHelper.dismissProgress(loadingPd);
//					getWeFriendsFromDB();
//				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				WeFriendManager.getInstance(WeFriendCard.this).saveWeFriends(entity.u);
				handler.sendEmptyMessage(1);
			}
		});
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			showMessage();
			break;
		case R.id.searchEditView:
			letterListView.setVisibility(View.INVISIBLE);
			Intent intent = new Intent(WeFriendCard.this, WeFriendCardSearch.class);
            startActivityForResult(intent, 12);
			break;
		case R.id.searchDeleteButton:
			editText.setText("");
			editText.setCursorVisible(false);
			searchDeleteButton.setVisibility(View.INVISIBLE);
			break;
		case R.id.rightBarButton:
			
			break;
		}
	}
	
	private String[] projection = {Data.MIMETYPE, Phone.NUMBER, "display_name", "contact_id", "sort_key", "photo_thumb_uri"};
	private final static int MIMETYPE_INDEX = 0;
	private final static int NUMBER_INDEX = 1;
	private final static int NAME_INDEX = 2;
	private final static int ID_INDEX = 3;
	private final static int SORT_INDEX = 4;
	private final static int PHOTO_INDEX = 5;
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			handleCursor(cursor);
		}
	}
	
	private void handleCursor(final Cursor cursor) {
		final Handler handler1 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				contactors.addAll(mobiles);
				try {
					Collections.sort(contactors);
				} 
				catch(Exception e) {
					Crashlytics.logException(e);
				}
				mBilateralAdapter.notifyDataSetChanged();
				sortPY();
				letterListView.setVisibility(View.VISIBLE);
			}
		};
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					contactids = new ArrayList<String>();
					for (int i = 0; i < cursor.getCount(); i++) {
						cursor.moveToPosition(i);
						String mimetype = cursor.getString(MIMETYPE_INDEX);
						if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
							CardIntroEntity ce = new CardIntroEntity();
							ce.realname = cursor.getString(NAME_INDEX);
							ce.phone = cursor.getString(NUMBER_INDEX);
							ce.code = ""+cursor.getInt(ID_INDEX);
							ce.pinyin = cursor.getString(SORT_INDEX);
							ce.cardSectionType = LianXiRenType.mobile;
							ce.avatar = cursor.getString(PHOTO_INDEX);
							ce.department = "来自手机通讯录";
							ce.position = "";
							ce.py = StringUtils.getAlpha(ce.pinyin);
							if (!contactids.contains(ce.code)) {
								mobiles.add(ce);
								contactids.add(ce.code);
							}
						}
					}
				}
				handler1.sendEmptyMessage(1);
			}
		});
	}
	
	private void sortPY() {
		alphaIndexer .clear();
		for (int i = 0; i < contactors.size(); i++) {
			String currentStr = contactors.get(i).py;
			String previewStr = (i - 1) >= 0 ? contactors.get(i - 1).py : " ";
			if (!previewStr.equals(currentStr)) {
				if (currentStr.equals("~")) {
					alphaIndexer.put("#", i);
				}
				else {
					alphaIndexer.put(currentStr, i);
				}
			}
		}
	}
	
	private void getWeFriendsFromDB(final String limit) {
		final Handler handler1 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				contactors.addAll(bilaterals);
				try {
					Collections.sort(contactors);
				}
				catch (Exception e) {
					Crashlytics.logException(e);
				}
				mBilateralAdapter.notifyDataSetChanged();
				sortPY();
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				letterListView.setVisibility(View.VISIBLE);
			}
		};
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				contactors.removeAll(bilaterals);
				bilaterals.clear();
				bilaterals.addAll(WeFriendManager.getInstance(WeFriendCard.this).getWeFriends(limit));
				handler1.sendEmptyMessage(1);
			}
		});
	}
	
	protected void WarningDialog() {
		String message = "请在手机的[设置]->[应用]->[群友通讯录]->[权限管理]，允许群友通讯录访问你的联系人记录并重新运行程序";
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(message);
		builder.setTitle("通讯录提示");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	   builder.create().show();
	}
	
	private void getAllOpenidFromServer() {
		AppClient.getAllOpenid(this, appContext, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				OpenidListEntity entity = (OpenidListEntity)data;
				handleOpenid(entity);
			}
			
			@Override
			public void onFailure(String message) {
				
			}
			
			@Override
			public void onError(Exception e) {
				
			}
		});
	}
	
	private void handleOpenid(final OpenidListEntity entity) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				default:
					List<String> needUpdateOpenids = (List<String>) msg.obj;
					getAllWeFriendbyOpenids(needUpdateOpenids);
					break;
				}
				
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				List<String> temp = WeFriendManager.getInstance(WeFriendCard.this).getAllOpenidOfWeFriends();
				for (String openid : temp) {
					if (!entity.openids.contains(openid)) {
						WeFriendManager.getInstance(WeFriendCard.this).deleteWeFriendBy(openid);
					}
				}
				temp = WeFriendManager.getInstance(WeFriendCard.this).getAllOpenidOfWeFriends();
				List<String> needUpdateOpenids = new ArrayList<String>();
				for (String openid : entity.openids) {
					if (!temp.contains(openid)) {
						needUpdateOpenids.add(openid);
					}
				}
				if (needUpdateOpenids.size() > 0) {
					Message msg = new Message();
					msg.obj = needUpdateOpenids;
					handler.sendMessage(msg);
				}
			}
		});
		
	}
	
	private void getAllWeFriendbyOpenids(List<String> needUpdateOpenids) {
		Logger.i(needUpdateOpenids.size()+"");
		Gson gson = new Gson();
		AppClient.getAllWeFriendByOpenid(this, appContext, gson.toJson(needUpdateOpenids), new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				FriendCardListEntity entity = (FriendCardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					Logger.i("all2");
					saveListInDB(entity);
//					bilaterals.clear();
//					bilaterals.addAll(WeFriendManager.getInstance(WeFriendCard.this).getWeFriends());
//					mBilateralAdapter.notifyDataSetChanged();
					break;
				default:
					Logger.i(entity.getMessage());
					break;
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
	
	
	private TextView overlay;
//	private String[] sections;
	MyLetterListView letterListView = null;
	private HashMap<String, Integer> alphaIndexer;
	
	private class LetterListViewListener implements OnTouchingLetterChangedListener {
		@Override
		public void onTouchingLetterChanged(final String s) {
			if (alphaIndexer.get(s) != null) {
				int position = alphaIndexer.get(s);
				int xposition = (position + 1);
				xlistView.setSelection(xposition);
				overlay.setText(s);
				overlay.setVisibility(View.VISIBLE);
			}
		}
		@Override
		public void onTouchingUp() {
			overlay.setVisibility(View.GONE);
		}
	}	
	
//	private void initOverlay() {
//		LayoutInflater inflater = LayoutInflater.from(this);
//		overlay = (TextView) inflater.inflate(R.layout.overlay, null);
//		overlay.setVisibility(View.INVISIBLE);
//		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
//				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
//				WindowManager.LayoutParams.TYPE_APPLICATION,
//				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//				PixelFormat.TRANSLUCENT);
//		WindowManager windowManager = (WindowManager) this
//				.getSystemService(Context.WINDOW_SERVICE);
//		windowManager.addView(overlay, lp);
//	}

	@Override
	public void onItemClick(AdapterView<?> parent, View convertView, int position, long arg3) {
		CardIntroEntity model = (CardIntroEntity) parent.getAdapter().getItem(position);
		if (model.cardSectionType.equals(LianXiRenType.mobile)) {
			showMobileView(model);
		}
		else { 
			showCardView(model);
		}
	}
	
	private void showCardView(CardIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(context, CardView.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		((WeFriendCard)context).startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	private void showMobileView(CardIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看手机名片",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(context, MobileVIew.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		startActivity(intent);
	}

	@Override
	public void onRefresh(int id) {
		
	}

	@Override
	public void onLoadMore(int id) {
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			currentPage++;
			getFriendCard(currentPage, "", count+"", UIHelper.LISTVIEW_ACTION_SCROLL);
		}
	}
}
