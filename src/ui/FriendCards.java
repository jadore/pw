package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.AppException;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.Index.MyOnPageChangeListener;
import ui.adapter.FriendCardAdapter;
import ui.adapter.IndexPagerAdapter;
import widget.QuickAlphabeticBar;
import bean.CardIntroEntity;
import bean.ContactBean;
import bean.Entity;
import bean.FriendCardListEntity;
import bean.PhoneListEntity;
import bean.Result;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

public class FriendCards extends AppActivity {
	private static final int PAGE1 = 0;// 页面1
	private static final int PAGE2 = 1;// 页面2
	private static final int PAGE3 = 2;// 页面3
	private ViewPager mPager;
	private List<View> mListViews;// Tab页面
	
	private List<CardIntroEntity> bilaterals = new ArrayList<CardIntroEntity>();
	private ListView mBilateralListView;
	private FriendCardAdapter mBilateralAdapter;
	private TextView nobilateralView;
	private QuickAlphabeticBar alpha1;
	
	private List<CardIntroEntity> friends = new ArrayList<CardIntroEntity>();
	private ListView mFriendListView;
	private FriendCardAdapter mFriendAdapter;
	private TextView noFriendView;
	private QuickAlphabeticBar alpha2;
	
	private List<CardIntroEntity> followers = new ArrayList<CardIntroEntity>();
	private ListView mFollowerListView;
	private FriendCardAdapter mFollowerAdapter;
	private TextView noFollowerView;
	private QuickAlphabeticBar alpha3;
	
	private Button bilateralsButton;
	private Button friendsButton;
	private Button followersButton;
	
	private ProgressDialog loadingPd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_card);
		initUI();
		getFriendCardFromCache();
	}
	
	private void initUI() {
		bilateralsButton = (Button) findViewById(R.id.bilateralsButton);
		bilateralsButton.setSelected(true);
		friendsButton = (Button) findViewById(R.id.friendsButton);
		followersButton = (Button) findViewById(R.id.followersButton);
		int w = ImageUtils.getDisplayWidth(this);
		LinearLayout.LayoutParams p1 = (LayoutParams) bilateralsButton.getLayoutParams();
		p1.width = w/3;
		bilateralsButton.setLayoutParams(p1);
		LinearLayout.LayoutParams p2 = (LayoutParams) friendsButton.getLayoutParams();
		p2.width = w/3;
		friendsButton.setLayoutParams(p2);
		LinearLayout.LayoutParams p3 = (LayoutParams) followersButton.getLayoutParams();
		p3.width = w/3;
		followersButton.setLayoutParams(p3);
		mPager = (ViewPager) findViewById(R.id.viewPager);
		mListViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View lay0 = inflater.inflate(R.layout.friend_tab0, null);
		View lay1 = inflater.inflate(R.layout.friend_tab1, null);
		View lay2 = inflater.inflate(R.layout.friend_tab2, null);
		mListViews.add(lay0);
		mListViews.add(lay1);
		mListViews.add(lay2);
		mPager.setAdapter(new IndexPagerAdapter(mListViews));
		mPager.setCurrentItem(PAGE1);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
		
		
		mBilateralListView = (ListView) lay0.findViewById(R.id.tab0_listView);
		nobilateralView = (TextView) lay0.findViewById(R.id.noting_view);
		mBilateralListView.setDividerHeight(0);
		bilaterals = new ArrayList<CardIntroEntity>();
		mBilateralAdapter = new FriendCardAdapter(this, bilaterals);
		mBilateralListView.setAdapter(mBilateralAdapter);
		alpha1 = (QuickAlphabeticBar) lay0.findViewById(R.id.fast_scroller);
		alpha1.initFrom(lay0);
		alpha1.setListView(mBilateralListView);
		alpha1.setHight(ImageUtils.getDisplayHeighth(getApplicationContext()) - ImageUtils.dip2px(getApplicationContext(), 88));
		
		mFriendListView = (ListView) lay1.findViewById(R.id.tab1_listView);
		noFriendView = (TextView) lay1.findViewById(R.id.noting_view);
		mFriendListView.setDividerHeight(0);
		friends = new ArrayList<CardIntroEntity>();
		mFriendAdapter = new FriendCardAdapter(this, friends);
		mFriendListView.setAdapter(mFriendAdapter);
		alpha2 = (QuickAlphabeticBar) lay1.findViewById(R.id.fast_scroller);
		alpha2.initFrom(lay1);
		alpha2.setListView(mFriendListView);
		alpha2.setHight(ImageUtils.getDisplayHeighth(getApplicationContext()) - ImageUtils.dip2px(getApplicationContext(), 88));
		
		mFollowerListView = (ListView) lay2.findViewById(R.id.tab2_listView);
		noFollowerView = (TextView) lay2.findViewById(R.id.noting_view);
		mFollowerListView.setDividerHeight(0);
		followers = new ArrayList<CardIntroEntity>();
		mFollowerAdapter = new FriendCardAdapter(this, followers);
		mFollowerListView.setAdapter(mFollowerAdapter);
		alpha3 = (QuickAlphabeticBar) lay2.findViewById(R.id.fast_scroller);
		alpha3.initFrom(lay2);
		alpha3.setListView(mFollowerListView);
		alpha3.setHight(ImageUtils.getDisplayHeighth(getApplicationContext()) - ImageUtils.dip2px(getApplicationContext(), 88));
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.bilateralsButton:
			mPager.setCurrentItem(PAGE1);
			break;
		case R.id.friendsButton:
			mPager.setCurrentItem(PAGE2);
			break;
		case R.id.followersButton:
			mPager.setCurrentItem(PAGE3);
			break;
		}
	}
	
	private void getFriendCardFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.FriendCardList, appContext.getLoginUid());
		FriendCardListEntity entity = (FriendCardListEntity) appContext.readObject(key);
		if(entity == null){
			getFriendCard();
			return;
		}
		handleFriends(entity);
		getFriendCard();
	}
	
	private void getFriendCard() {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.getFriendCard(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				FriendCardListEntity entity = (FriendCardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handleFriends(entity);
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
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
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void handleFriends(FriendCardListEntity entity) {
		nobilateralView.setVisibility(View.GONE);
		noFriendView.setVisibility(View.GONE);
		noFollowerView.setVisibility(View.GONE);
		if (entity.bilateral.size() > 0) {
			bilaterals.clear();
			bilaterals.addAll(entity.bilateral);
			mBilateralAdapter.notifyDataSetChanged();
			setAlpha(alpha1, bilaterals);
		}
		if (entity.friend.size() > 0) {
			friends.clear();
			friends.addAll(entity.friend);
			mFriendAdapter.notifyDataSetChanged();
			setAlpha(alpha2, friends);
		}
		if (entity.follower.size() > 0) {
			followers.clear();
			followers.addAll(entity.follower);
			mFollowerAdapter.notifyDataSetChanged();
			setAlpha(alpha3, followers);
		}
		if (bilaterals.size() == 0) {
			nobilateralView.setVisibility(View.VISIBLE);
		}
		if (friends.size() == 0) {
			noFriendView.setVisibility(View.VISIBLE);
		}
		if (followers.size() == 0) {
			noFollowerView.setVisibility(View.VISIBLE);
		}
	}
	
	private void setAlpha(QuickAlphabeticBar alpha, List<CardIntroEntity> list) {
		HashMap<String, Integer> alphaIndexer = new HashMap<String, Integer>();
		String[] sections = new String[list.size()];
		for (int i =0; i <list.size(); i++) {
			String name = StringUtils.getAlpha(list.get(i).pinyin);
			if(!alphaIndexer.containsKey(name)){ 
				alphaIndexer.put(name, i);
			}
		}
		Set<String> sectionLetters = alphaIndexer.keySet();
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
		Collections.sort(sectionList);
		sections = new String[sectionList.size()];
		sectionList.toArray(sections);
		Logger.i(alphaIndexer.values().toString());
		alpha.setAlphaIndexer(alphaIndexer);
		alpha.setVisibility(View.VISIBLE);
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {

		public void onPageSelected(int arg0) {
//			Animation animation = null;
			switch (arg0) {
			case PAGE1:// 切换到页卡1
				bilateralsButton.setSelected(true);
				friendsButton.setSelected(false);
				followersButton.setSelected(false);
				break;
			case PAGE2:// 切换到页卡2
				bilateralsButton.setSelected(false);
				friendsButton.setSelected(true);
				followersButton.setSelected(false);
				break;
			case PAGE3:
				bilateralsButton.setSelected(false);
				friendsButton.setSelected(false);
				followersButton.setSelected(true);
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
	
}
