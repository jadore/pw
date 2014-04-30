package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.zxing.client.android.CaptureActivity;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.QYRestClient;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import tools.UpdateManager;
import ui.adapter.FriendCardAdapter;
import widget.XListView;
import widget.XListView.IXListViewListener;

public class WeFriendCard extends AppActivity implements IXListViewListener, OnScrollListener, OnEditorActionListener{
	private TextView messageView;
	
	private int lvDataState;
	private int currentPage;
	private ProgressDialog loadingPd;
	private XListView xlistView;
	private List<CardIntroEntity> bilaterals = new ArrayList<CardIntroEntity>();
	private FriendCardAdapter mBilateralAdapter;
	private TextView nobilateralView;
	
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	private View searchHeaderView;
	private InputMethodManager imm;
	private EditText editText;
	private Button searchDeleteButton;
	
	private String keyword;
	
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
		xlistView = (XListView)findViewById(R.id.xlistview);
		xlistView.setXListViewListener(this, 0);
        xlistView.setRefreshTime();
        xlistView.setPullLoadEnable(false);
        xlistView.setDividerHeight(0);
        xlistView.addHeaderView(searchHeaderView, null, false);
        xlistView.setOnScrollListener(this);
		bilaterals = new ArrayList<CardIntroEntity>();
		mBilateralAdapter = new FriendCardAdapter(this, bilaterals);
		xlistView.setAdapter(mBilateralAdapter);
		
	}
	
	private void getFriendCardFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.FriendCardList1, appContext.getLoginUid());
		FriendCardListEntity entity = (FriendCardListEntity) appContext.readObject(key);
		if(entity == null){
//			currentPage = 1;
//			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
//			xlistView.startLoadMore();
			return;
		}
		handleFriends(entity, UIHelper.LISTVIEW_ACTION_INIT);
//		currentPage = 1;
//		getFriendCard(currentPage, keyword, "", UIHelper.LISTVIEW_ACTION_REFRESH);
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
						int pao = Integer.valueOf(entity.news);
						String num = pao>99?"99+":pao+"";
						messageView.setText(num);
						if (pao == 0) {
							messageView.setVisibility(View.INVISIBLE);
						}
						WebView webview = (WebView) findViewById(R.id.webview);
						webview.loadUrl(CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign())  ;
						webview.setWebViewClient(new WebViewClient() {
							public boolean shouldOverrideUrlLoading(WebView view, String url) {
								view.loadUrl(url);
								return true;
							};
						});
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
		xlistView.stopLoadMore();
		xlistView.stopRefresh();
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
			xlistView.setPullLoadEnable(true);
			mBilateralAdapter.notifyDataSetChanged();
		}
		else if (entity.ne == -1) {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
			xlistView.setPullLoadEnable(false);
			mBilateralAdapter.notifyDataSetChanged();
		}
		if(bilaterals.isEmpty()){
			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
			xlistView.setPullLoadEnable(false);
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
	public void onRefresh(int id) {
		currentPage = 1;
		keyword = "";
		getFriendCard(currentPage, keyword, "", UIHelper.LISTVIEW_ACTION_REFRESH);
	}

	@Override
	public void onLoadMore(int id) {
		if (lvDataState == UIHelper.LISTVIEW_DATA_EMPTY) {
			getFriendCard(currentPage,"","", UIHelper.LISTVIEW_ACTION_INIT);
		}
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
			currentPage ++;
			getFriendCard(currentPage,"","", UIHelper.LISTVIEW_ACTION_SCROLL);
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
}
