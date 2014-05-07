package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.LiveFolders;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import baidupush.Utils;
import bean.CardIntroEntity;
import bean.Entity;
import bean.FriendCardListEntity;
import bean.MessageUnReadEntity;
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
import config.QYRestClient;
import db.manager.WeFriendManager;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import tools.UpdateManager;
import ui.adapter.FriendCardAdapter;

public class WeFriendCard extends AppActivity implements OnScrollListener, OnEditorActionListener{
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
	
	private ExpandableListView xlistView;
	private List<List<CardIntroEntity>> contactors = new ArrayList<List<CardIntroEntity>>();
	private FriendCardAdapter mBilateralAdapter;
	
	MyAsyncQueryHandler asyncQuery;
	Uri uri ;
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	  @Override
	public void onStop() {
	    super.onStop();
	    QYRestClient.getIntance().cancelRequests(this, true);
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	    
	}
	  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
		contactors.add(mobiles);
		contactors.add(bilaterals);
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
		messageView = (TextView) findViewById(R.id.messageView);
		searchHeaderView = getLayoutInflater().inflate(R.layout.search_headview, null);
		searchHeaderView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(WeFriendCard.this, WeFriendCardSearch.class);
	            startActivityForResult(intent, 12);
			}
		});
		editText = (EditText) searchHeaderView.findViewById(R.id.searchEditView);
//		editText.setOnEditorActionListener(this);
//		editText.addTextChangedListener(TWPN);
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
		xlistView = (ExpandableListView)findViewById(R.id.xlistview);
        xlistView.setDividerHeight(0);
        xlistView.setGroupIndicator(null);
        xlistView.addHeaderView(searchHeaderView, null, false);
        xlistView.setOnScrollListener(this);
		mBilateralAdapter = new FriendCardAdapter(this, contactors);
		xlistView.setAdapter(mBilateralAdapter);
		xlistView.expandGroup(0);
		xlistView.expandGroup(1);
		xlistView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
					long arg3) {
				return true;
			}
		});
	}
	
	private void getFriendCardFromCache() {
		asyncQuery.startQuery(0, null, uri, null, null, null, "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
		bilaterals.addAll(WeFriendManager.getInstance(this).getWeFriends());
		mBilateralAdapter.notifyDataSetChanged();
	}
	
	private void checkLogin() {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
    	
		AppClient.autoLogin(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UserEntity user = (UserEntity)data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					appContext.saveLoginInfo(user);
					editText.setHint("搜索"+appContext.getDeg2()+"位二度好友");
					showReg(user);
					getAllFriend();
//					getUnReadMessage();
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
				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
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
	
	public void showMessage() {
		Intent intent = new Intent(this, MessageView.class);
		startActivity(intent);
	}
	
	private void getUnReadMessage() {
		AppClient.getUnReadMessage(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				MessageUnReadEntity entity = (MessageUnReadEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
//					Tabbar.setMessagePao(entity);
					if(entity != null){
						messageView.setVisibility(View.VISIBLE);
						int pao = Integer.valueOf(entity.news) + Integer.valueOf(entity.card);
						String num = pao>99?"99+":pao+"";
						messageView.setText(num);
						if (pao == 0) {
							messageView.setVisibility(View.INVISIBLE);
						}
					}
				case CommonValue.USER_NOT_IN_ERROR:
					break;
				default:
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
	
	private void blindBaidu() {
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, 
				Utils.getMetaValue(this, "api_key"));
	}
	
	private void getAllFriend() {
		List<CardIntroEntity> temp = WeFriendManager.getInstance(this).getWeFriends();
		if (temp.size() == 0) {
			currentPage = 1;
			getFriendCard(currentPage, "", 1000+"", UIHelper.LISTVIEW_ACTION_INIT);
		}
		else {
			bilaterals.clear();
			bilaterals.addAll(temp);
			mBilateralAdapter.notifyDataSetChanged();
			getAllOpenidFromServer();
		}
	}
	
	private void getFriendCard(int page, String kw, String count, final int action) {
		if (!appContext.isNetworkConnected()) {
			UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
			return;
		}
    	indicatorImageView.startAnimation(indicatorAnimation);
    	indicatorImageView.setVisibility(View.VISIBLE);
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
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
			}
		});
	}
	
	private void handleFriends(FriendCardListEntity entity, int action) {
		nobilateralView.setVisibility(View.GONE);
//		switch (action) {
//		case UIHelper.LISTVIEW_ACTION_INIT:
//		case UIHelper.LISTVIEW_ACTION_REFRESH:
//			bilaterals.clear();
//			bilaterals.addAll(entity.u);
//			break;
//		case UIHelper.LISTVIEW_ACTION_SCROLL:
//			bilaterals.addAll(entity.u);
//			break;
//		}
		Logger.i(entity.u.size()+"");
		for (CardIntroEntity card : entity.u) {
			WeFriendManager.getInstance(this).saveWeFriend(card);
		}
		if(entity.ne >= 1){					
//			lvDataState = UIHelper.LISTVIEW_DATA_MORE;
//			mBilateralAdapter.notifyDataSetChanged();
			++currentPage;
			getFriendCard(currentPage, "", 1000+"", UIHelper.LISTVIEW_ACTION_INIT);
		}
		else if (entity.ne == -1) {
//			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
			bilaterals.clear();
			bilaterals.addAll(WeFriendManager.getInstance(this).getWeFriends());
			mBilateralAdapter.notifyDataSetChanged();
		}
//		if(bilaterals.isEmpty()){
//			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
//			nobilateralView.setVisibility(View.VISIBLE);
//			if (StringUtils.notEmpty(keyword)) {
//				nobilateralView.setText(R.string.friend_search_no);
//			}
//			else {
//				nobilateralView.setText(R.string.friend_no);
//			}
//		}
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			showMessage();
			break;
		case R.id.searchEditView:
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
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);  
		editText.setCursorVisible(false);
	}

	public boolean onEditorAction(TextView v, int actionID, KeyEvent event) {
		
		switch(actionID){  
        case EditorInfo.IME_ACTION_SEARCH:  
        	
        	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
    		editText.setCursorVisible(false);
            currentPage = 1;
            keyword = v.getText().toString();
            if (StringUtils.empty(keyword)) {
				return false;
			}
            Intent intent = new Intent(WeFriendCard.this, WeFriendCardSearch.class);
            startActivity(intent);
//            loadingPd = UIHelper.showProgress(this, null, null, true);
//
//            String sql = "display_name like "+"'%" + keyword + "%' or " + Phone.NUMBER + " like " +"'%" + keyword + "%'";
//    		asyncQuery.startQuery(0, null, uri, null, sql, null, "sort_key COLLATE LOCALIZED asc");
//    		
//    		searchFriendCard(1, keyword, "", UIHelper.LISTVIEW_ACTION_INIT);
            break;  
        }  
		return true;
	}
	
	TextWatcher TWPN = new TextWatcher() {
        private CharSequence temp;
        private int editStart ;
        private int editEnd ;
        public void beforeTextChanged(CharSequence s, int arg1, int arg2,
                int arg3) {
            temp = s;
        }
       
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        	if (s.length() > 0) {
            	searchDeleteButton.setVisibility(View.VISIBLE);
        		String sql = "display_name like "+"'%" + s.toString() + "%' or " + Phone.NUMBER + " like " +"'%" + s.toString() + "%'";
        		asyncQuery.startQuery(0, null, uri, null, sql, null, "sort_key COLLATE LOCALIZED asc");
        		
        		bilaterals.clear();
				bilaterals.addAll(WeFriendManager.getInstance(WeFriendCard.this).searchWeFriendsByKeyword(s.toString()));
				mBilateralAdapter.notifyDataSetChanged();
        	}
            else {
            	searchDeleteButton.setVisibility(View.INVISIBLE);
        		asyncQuery.startQuery(0, null, uri, null, null, null, "sort_key COLLATE LOCALIZED asc");
        		bilaterals.clear();
				bilaterals.addAll(WeFriendManager.getInstance(WeFriendCard.this).getWeFriends());
				mBilateralAdapter.notifyDataSetChanged();
            }
        }
       
		public void afterTextChanged(Editable s) {
            
		}
    };
    
    
    
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
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
					if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
				     	String phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
						CardIntroEntity ce = new CardIntroEntity();
						ce.realname = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						ce.phone = phone;
						ce.code = ""+cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
						ce.cardSectionType = "mobile";
						mobiles.add(ce);
					}
				}
			}
			else {
//				WarningDialog();
			}
			mBilateralAdapter.notifyDataSetChanged();
		}
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
					if (!WeFriendCard.this.isFinishing()) {
						List<String> temp = WeFriendManager.getInstance(WeFriendCard.this).getAllOpenidOfWeFriends();
						for (String openid : temp) {
							if (!entity.openids.contains(openid)) {
								WeFriendManager.getInstance(WeFriendCard.this).deleteWeFriendBy(openid);
							}
						}
						temp = WeFriendManager.getInstance(WeFriendCard.this).getAllOpenidOfWeFriends();
						for (String openid : entity.openids) {
							if (!temp.contains(openid)) {
								CardIntroEntity model = new CardIntroEntity();
								model.openid = openid;
								WeFriendManager.getInstance(WeFriendCard.this).saveWeFriend(model);
							}
						}
						getAllWeFriendbyOpenids();
					}
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
	
	private void getAllWeFriendbyOpenids() {
		List<String> temp = WeFriendManager.getInstance(WeFriendCard.this).getAllOpenidOfWeFriends();
		Gson gson = new Gson();
		AppClient.getAllWeFriendByOpenid(this, appContext, gson.toJson(temp), new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				FriendCardListEntity entity = (FriendCardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					for (CardIntroEntity model : entity.u) {
						WeFriendManager.getInstance(WeFriendCard.this).updateWeFriend(model);
					}
					bilaterals.clear();
					bilaterals.addAll(WeFriendManager.getInstance(WeFriendCard.this).getWeFriends());
					mBilateralAdapter.notifyDataSetChanged();
					break;
				default:
				}
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
	
	private void searchFriendCard(int page, String kw, String count, final int action) {
		if (!appContext.isNetworkConnected()) {
			UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
			return;
		}
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.searchFriendCard(this, appContext, page+"", kw, count, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				FriendCardListEntity entity = (FriendCardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handleSearchFriends(entity, action);
					break;
				default:
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
	
	private void handleSearchFriends(FriendCardListEntity entity, int action) {
//		nobilateralView.setVisibility(View.GONE);
		switch (action) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
			bilaterals.clear();
			bilaterals.addAll(entity.u);
			break;
		case UIHelper.LISTVIEW_ACTION_SCROLL:
			bilaterals.addAll(entity.u);
			break;
		}
		if(entity.ne >= 1){					
			lvDataState = UIHelper.LISTVIEW_DATA_MORE;
		}
		else if (entity.ne == -1) {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
		}
		if(bilaterals.isEmpty()){
			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
//			nobilateralView.setVisibility(View.VISIBLE);
//			if (StringUtils.notEmpty(keyword)) {
//				nobilateralView.setText(R.string.friend_search_no);
//			}
//			else {
//				nobilateralView.setText(R.string.friend_no);
//			}
		}
		mBilateralAdapter.notifyDataSetChanged();
	}
}
