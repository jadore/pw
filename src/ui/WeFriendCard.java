package ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import bean.CardIntroEntity;
import bean.Entity;
import bean.FriendCardListEntity;
import bean.Result;

import com.google.analytics.tracking.android.EasyTracker;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.QYRestClient;

import tools.AppManager;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.FriendCardAdapter;
import widget.XListView;
import widget.XListView.IXListViewListener;

public class WeFriendCard extends AppActivity implements IXListViewListener, OnScrollListener, OnEditorActionListener{
	
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
		keyword = "";
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
//		Handler jumpHandler = new Handler();
//        jumpHandler.postDelayed(new Runnable() {
//			public void run() {
		getFriendCardFromCache();
//			}
//		}, 100);
	}
	
	private void initUI() {
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
			currentPage = 1;
			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
			xlistView.startLoadMore();
			return;
		}
		handleFriends(entity, UIHelper.LISTVIEW_ACTION_INIT);
		currentPage = 1;
		getFriendCard(currentPage, keyword, "", UIHelper.LISTVIEW_ACTION_REFRESH);
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
			AppManager.getAppManager().finishActivity(this);
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
