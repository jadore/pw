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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import bean.Entity;
import bean.FriendCardListEntity;
import bean.OpenidListEntity;
import bean.Result;
import bean.UserEntity;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.CommonValue.LianXiRenType;
import config.QYRestClient;
import db.manager.WeFriendManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import tools.UpdateManager;
import ui.adapter.FriendCardAdapter;
import widget.MyLetterListView;
import widget.MyLetterListView.OnTouchingLetterChangedListener;

public class WeFriendCard extends AppActivity implements OnItemClickListener {
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
	private InputMethodManager imm;
	private EditText editText;
	private Button searchDeleteButton;
	
	private String keyword;
	
	private ListView xlistView;
	private List<CardIntroEntity> contactors = new ArrayList<CardIntroEntity>();
	private FriendCardAdapter mBilateralAdapter;
	
	private MyAsyncQueryHandler asyncQuery;
	private Uri uri ;
	private List<String> contactids;
	
	private List<String> needUpdateOpenids = new ArrayList<String>();
	private static final int count = 200;
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	  @Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		letterListView.setVisibility(View.VISIBLE);
	}
	  
	@Override
	protected void onDestroy() {
		QYRestClient.getIntance().cancelRequests(this, true);
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
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		getFriendCardFromCache();
		Handler jumpHandler = new Handler();
        jumpHandler.postDelayed(new Runnable() {
			public void run() {
				if (!appContext.isNetworkConnected()) {
		    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
		    		return;
		    	}
				UpdateManager.getUpdateManager().checkAppUpdate(WeFriendCard.this, false);
				checkLogin();
			}
		}, 500);
	}
	
	private void initUI() {
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
		touchhandler = new Handler();
		overlayThread = new OverlayThread();
		initOverlay();
		
		xlistView = (ListView)findViewById(R.id.xlistview);
        xlistView.setDividerHeight(0);
        xlistView.addHeaderView(searchHeaderView, null, false);
		mBilateralAdapter = new FriendCardAdapter(this, contactors, imageLoader);
		xlistView.setAdapter(mBilateralAdapter);
		xlistView.setOnItemClickListener(this);
	}
	
	private void getFriendCardFromCache() {
		asyncQuery.startQuery(0, null, uri, null, null, null, "sort_key COLLATE LOCALIZED asc"); 
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
					editText.setHint("搜索"+appContext.getDeg2()+"位二度好友");
					showReg(user);
					getAllFriend();
					if (!Utils.hasBind(getApplicationContext())) {
						blindBaidu();
					}
//					WebView webview = (WebView) findViewById(R.id.webview);
//					webview.loadUrl(CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign())  ;
//					webview.setWebViewClient(new WebViewClient() {
//						public boolean shouldOverrideUrlLoading(WebView view, String url) {
//							view.loadUrl(url);
//							return true;
//						};
//					});
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
	
	private void blindBaidu() {
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, 
				Utils.getMetaValue(this, "api_key"));
	}
	
	private void getAllFriend() {
		final Handler handler2 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					loadingPd = UIHelper.showProgress(WeFriendCard.this, null, null, true);
					currentPage = 1;
					getFriendCard(currentPage, "", count+"", UIHelper.LISTVIEW_ACTION_INIT);
				}
				else {
					if (!appContext.isNetworkConnected()) {
			    		return;
			    	}
					getAllOpenidFromServer();
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
			        handler2.sendEmptyMessageDelayed(2, 60*1000);
				}
			}
		});
	}
	
	private void getFriendCard(int page, String kw, String count, final int action) {
		if (!appContext.isNetworkConnected()) {
			UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
			return;
		}
		AppClient.getChatFriendCard(this, appContext, page+"", kw, count, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
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
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
			}
		});
	}
	
	private void handleFriends(FriendCardListEntity entity, int action) {
		Logger.i(entity.u.size()+"");
		saveListInDB(entity);
		if(entity.ne >= 1){					
			++currentPage;
			getFriendCard(currentPage, "", count+"", UIHelper.LISTVIEW_ACTION_INIT);
		}
		else if (entity.ne == -1) {
			UIHelper.dismissProgress(loadingPd);
			contactors.removeAll(bilaterals);
			mBilateralAdapter.notifyDataSetChanged();
			getWeFriendsFromDB();
		}
	}
	
	private void saveListInDB(final FriendCardListEntity entity) {
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (CardIntroEntity card : entity.u) {
					WeFriendManager.getInstance(WeFriendCard.this).saveWeFriend(card);
				}
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
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
			searchDeleteButton.setVisibility(View.INVISIBLE);
			break;
		case R.id.rightBarButton:
			
			break;
		}
	}
	
    /**
	 * 数据库异步查询类AsyncQueryHandler
	 * 
	 */
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		/**
		 * 查询结束的回调函数
		 */
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			mobiles.clear();
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				contactids = new ArrayList<String>();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
					if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
				     	String phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
						CardIntroEntity ce = new CardIntroEntity();
						ce.realname = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						ce.phone = phone;
						ce.code = ""+cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
						ce.cardSectionType = LianXiRenType.mobile;
						ce.department = "来自手机通讯录";
						ce.position = "";
						ce.avatar = "";
						ce.pinyin = cursor.getString(cursor.getColumnIndex("sort_key"));
						ce.py = StringUtils.getAlpha(ce.pinyin);
						if (!contactids.contains(ce.code)) {
							mobiles.add(ce);
							contactids.add(ce.code);
						}
					}
				}
			}
			else {
//				WarningDialog();
			}
			contactors.addAll(mobiles);
			Collections.sort(contactors);
			mBilateralAdapter.notifyDataSetChanged();
			getWeFriendsFromDB();
		}
	}
	
	private void getWeFriendsFromDB() {
		final Handler handler1 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mBilateralAdapter.notifyDataSetChanged();
				UIHelper.dismissProgress(loadingPd);
			}
		};
		loadingPd = UIHelper.showProgress(this, null, null, true);
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				bilaterals.clear();
				bilaterals.addAll(WeFriendManager.getInstance(WeFriendCard.this).getWeFriends());
				contactors.addAll(bilaterals);
				Collections.sort(contactors);
				alphaIndexer .clear();
				sections = new String[contactors.size()];
				for (int i = 0; i < contactors.size(); i++) {
					String currentStr = contactors.get(i).pinyin.substring(0, 1).toLowerCase();
					String previewStr = (i - 1) >= 0 ? contactors.get(i - 1).pinyin.substring(0, 1).toLowerCase() : " ";
					if (!previewStr.equals(currentStr)) {
						String name = contactors.get(i).pinyin.substring(0, 1).toUpperCase();
						alphaIndexer.put(name, i);
						sections[i] = name;
					}
				}
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
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handleOpenid(entity);
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
	
	private void handleOpenid(final OpenidListEntity entity) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				getAllWeFriendbyOpenids();
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
				needUpdateOpenids.clear();
				for (String openid : entity.openids) {
					if (!temp.contains(openid)) {
						needUpdateOpenids.add(openid);
					}
				}
				if (needUpdateOpenids.size() > 0) {
					handler.sendEmptyMessage(1);
				}
			}
		});
		
	}
	
	private void getAllWeFriendbyOpenids() {
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
	
	
	private Handler touchhandler;
	private OverlayThread overlayThread;
	private TextView overlay;
	private String[] sections;
	MyLetterListView letterListView = null;
	private HashMap<String, Integer> alphaIndexer;
	
	private class OverlayThread implements Runnable {

		@Override
		public void run() {
			overlay.setVisibility(View.GONE);
		}
	}
	
	private class LetterListViewListener implements OnTouchingLetterChangedListener {
		@Override
		public void onTouchingLetterChanged(final String s) {
			if (alphaIndexer.get(s) != null) {
				int position = alphaIndexer.get(s);
				int xposition = (position + 1);
				xlistView.setSelection(xposition);
				overlay.setText(sections[position]);
				overlay.setVisibility(View.VISIBLE);
				touchhandler.removeCallbacks(overlayThread);
				touchhandler.postDelayed(overlayThread, 1000);
			}
		}
	}	
	
	private void initOverlay() {
		LayoutInflater inflater = LayoutInflater.from(this);
		overlay = (TextView) inflater.inflate(R.layout.overlay, null);
		overlay.setVisibility(View.INVISIBLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		WindowManager windowManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(overlay, lp);
	}

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
		Intent intent = new Intent(context, CardView.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		((WeFriendCard)context).startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	private void showMobileView(CardIntroEntity entity) {
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		Uri personUri = ContentUris.withAppendedId(uri, Integer.valueOf(entity.code));
		Intent intent2 = new Intent();
		intent2.setAction(Intent.ACTION_VIEW);
		intent2.setData(personUri);
		context.startActivity(intent2);
	}
}
