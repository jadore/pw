package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
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
import bean.Result;
import bean.UserEntity;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.QYRestClient;
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
		editText = (EditText) searchHeaderView.findViewById(R.id.searchEditView);
		editText.setOnEditorActionListener(this);
		editText.addTextChangedListener(TWPN);
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
		MyAsyncQueryHandler asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		asyncQuery.startQuery(0, null, uri, null, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
		String key = String.format("%s-%s", CommonValue.CacheKey.FriendCardList1, appContext.getLoginUid());
		FriendCardListEntity entity = (FriendCardListEntity) appContext.readObject(key);
		if(entity == null){
			return;
		}
		handleFriends(entity, UIHelper.LISTVIEW_ACTION_INIT);
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
					showReg(user);
					getFriendCard(currentPage, keyword, "", UIHelper.LISTVIEW_ACTION_REFRESH);
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
//					handleFriends(entity, action);
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
			mBilateralAdapter.notifyDataSetChanged();
		}
		else if (entity.ne == -1) {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
			mBilateralAdapter.notifyDataSetChanged();
		}
		if(bilaterals.isEmpty()){
			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
			nobilateralView.setVisibility(View.VISIBLE);
			if (StringUtils.notEmpty(keyword)) {
				nobilateralView.setText(R.string.friend_search_no);
			}
			else {
				nobilateralView.setText(R.string.friend_no);
			}
		}
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			showMessage();
			break;
		case R.id.searchEditView:
			editText.setCursorVisible(true);
			break;
		case R.id.searchDeleteButton:
			editText.setText("");
			editText.setCursorVisible(false);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
			searchDeleteButton.setVisibility(View.INVISIBLE);
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
            loadingPd = UIHelper.showProgress(this, null, null, true);
			getFriendCard(currentPage, keyword, "", UIHelper.LISTVIEW_ACTION_INIT);
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
            	String key = String.format("%s-%s", CommonValue.CacheKey.FriendCardList1, appContext.getLoginUid());
        		FriendCardListEntity entity = (FriendCardListEntity) appContext.readObject(key);
        		if(entity != null){
        			if (entity.u.size() > 0) {
        				List<CardIntroEntity> tempList = new ArrayList<CardIntroEntity>();
                		for (CardIntroEntity friend : entity.u) {
    						if (friend.realname.contains(s.toString()) ) {
    							tempList.add(friend);
    						}
    					}
                		if (tempList.size() > 0) {
                			bilaterals.clear();
                			bilaterals.addAll(tempList);
    						lvDataState = UIHelper.LISTVIEW_DATA_FULL;
    						mBilateralAdapter.notifyDataSetChanged();
    					}
        			}
        		}
        	}
            else {
            	searchDeleteButton.setVisibility(View.INVISIBLE);
            	String key = String.format("%s-%s", CommonValue.CacheKey.FriendCardList1, appContext.getLoginUid());
        		FriendCardListEntity entity = (FriendCardListEntity) appContext.readObject(key);
        		if(entity != null){
        			if (entity.u.size() > 0) {
        				currentPage = 1;
        				handleFriends(entity, UIHelper.LISTVIEW_ACTION_INIT);
        			}
        		}
            }
        }
       
		public void afterTextChanged(Editable s) {
            
		}
    };
    
    
    
    /**
	 * 数据库异步查询类AsyncQueryHandler
	 * 
	 * @author administrator
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
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					
					String mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
					if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
				     	String phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
//				     	phone = phone.replace(" ", "");
//				     	phone = phone.replace("+86", "");
//				     	phone = phone.replace("-", "");
//			     		ContactBean cb = new ContactBean();
//			     		
//						cb.setDisplayName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
//						cb.setPhoneNum(phone);
//						cb.setSortKey(cursor.getString(cursor.getColumnIndex("sort_key")));
//						cb.setContactId(cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
//						cb.setPhotoId(cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID)));
//						cb.setLookUpKey(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)));
//						list.add(cb);
						CardIntroEntity ce = new CardIntroEntity();
						ce.realname = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						ce.phone = phone;
						ce.cardSectionType = "mobile";
						mobiles.add(ce);
					}
				}
				if (mobiles.size() > 0) {
//					contactors.set(0, mobiles);
					mBilateralAdapter.notifyDataSetChanged();
//					HashMap<String, Integer> alphaIndexer = new HashMap<String, Integer>();
//					String[] sections = new String[list.size()];
//
//					for (int i =0; i <list.size(); i++) {
//						String name = StringUtils.getAlpha(list.get(i).getSortKey());
//						if(!alphaIndexer.containsKey(name)){ 
//							alphaIndexer.put(name, i);
//						}
//					}
//					
//					Set<String> sectionLetters = alphaIndexer.keySet();
//					ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
//					Collections.sort(sectionList);
//					sections = new String[sectionList.size()];
//					sectionList.toArray(sections);
//
//					alpha.setAlphaIndexer(alphaIndexer);
//					alpha.setVisibility(View.VISIBLE);
//					UIHelper.dismissProgress(loadingPd);
				}
			}
//			else {
//				authority = false;
//				UIHelper.dismissProgress(loadingPd);
//				WarningDialog();
//				return;
//			}
		}
	}
}
