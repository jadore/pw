package ui;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.CookieStore;

import baidupush.Utils;
import bean.ActivityIntroEntity;
import bean.ActivityListEntity;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.MessageUnReadEntity;
import bean.PhoneIntroEntity;
import bean.PhoneListEntity;
import bean.RecommendListEntity;
import bean.Result;
import bean.UserEntity;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.zxing.client.android.CaptureActivity;
import com.loopj.android.http.PersistentCookieStore;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.QYRestClient;
import config.AppClient.ClientCallback;
import config.CommonValue;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import tools.AppException;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.IndexActivityAdapter;
import ui.adapter.IndexCardAdapter;
import ui.adapter.IndexPagerAdapter;
import ui.adapter.IndexPhoneAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView.OnItemClickListener;

public class Index extends AppActivity {
	private ImageView avatarImageView;
	private TextView messageView;
	private Button phoneButton;
	private Button activityButton;
	private Button cardButton;
	
	private static final int PAGE1 = 0;// 页面1
	private static final int PAGE2 = 1;// 页面2
	private static final int PAGE3 = 2;// 页面3
//	private static final int PAGE4 = 3;// 页面3
	private ViewPager mPager;
	private List<View> mListViews;// Tab页面
	
//	private int currentIndex = PAGE1; // 默认选中第2个，可以动态的改变此参数值
//	private int offset = 0;// 动画图片偏移量
//	private int bmpW;// 动画图片宽度
	
	private List<List<PhoneIntroEntity>> phones;
	private PinnedHeaderListView mPinedListView1;
	private IndexPhoneAdapter mPhoneAdapter;
	
	private List<List<ActivityIntroEntity>> activities;
	private PinnedHeaderListView mPinedListView2;
	private IndexActivityAdapter mActivityAdapter;
	
	private List<List<CardIntroEntity>> cards;
	private PinnedHeaderListView mPinedListView0;
	private IndexCardAdapter mCardAdapter;
	
	private ListView mListView3;
	private ProgressDialog loadingPd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
		ShareSDK.initSDK(this);
		initUI();
		getCache();
		if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
        checkLogin();
	}
	
	private void blindBaidu() {
		Resources resource = this.getResources();
		String pkgName = this.getPackageName();
		if (!Utils.hasBind(getApplicationContext())) {
			PushManager.startWork(getApplicationContext(),
					PushConstants.LOGIN_TYPE_API_KEY, 
					Utils.getMetaValue(this, "api_key"));
			PushManager.enableLbs(getApplicationContext());
		}
		
        CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
        		getApplicationContext(),
        		resource.getIdentifier("notification_custom_builder", "layout", pkgName), 
        		resource.getIdentifier("notification_icon", "id", pkgName), 
        		resource.getIdentifier("notification_title", "id", pkgName), 
        		resource.getIdentifier("notification_text", "id", pkgName));
        cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
        cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
        cBuilder.setLayoutDrawable(resource.getIdentifier("simple_notification_icon", "drawable", pkgName));
		PushManager.setNotificationBuilder(this, 1, cBuilder);
		
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, 
				Utils.getMetaValue(this, "api_key"));
	}
	
	private void initUI() {
		messageView = (TextView) findViewById(R.id.messageView);
		cardButton = (Button) findViewById(R.id.cardButton);
		activityButton = (Button) findViewById(R.id.activityButton);
		phoneButton = (Button) findViewById(R.id.phoneButton);
		phoneButton.setSelected(true);
		
		avatarImageView = (ImageView ) findViewById(R.id.avatarImageView);
		mPager = (ViewPager) findViewById(R.id.viewPager);
		mListViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View lay0 = inflater.inflate(R.layout.tab0, null);
		View lay1 = inflater.inflate(R.layout.tab1, null);
		View lay2 = inflater.inflate(R.layout.tab2, null);
//		View lay3 = inflater.inflate(R.layout.tab3, null);
		
		mListViews.add(lay1);
		mListViews.add(lay2);
		mListViews.add(lay0);
//		mListViews.add(lay3);
		mPager.setAdapter(new IndexPagerAdapter(mListViews));
		mPager.setCurrentItem(PAGE1);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
		
		mPinedListView1 = (PinnedHeaderListView) lay1.findViewById(R.id.tab1_listView);
		mPinedListView1.setDividerHeight(0);
		phones = new ArrayList<List<PhoneIntroEntity>>();
		List<PhoneIntroEntity> mobilesInPhone = new ArrayList<PhoneIntroEntity>();
		PhoneIntroEntity mobile = new PhoneIntroEntity();
		mobile.title = "手机通讯录";
		mobile.content = CommonValue.subTitle.subtitle1;
		mobile.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
		mobilesInPhone.add(mobile);
		PhoneIntroEntity mobile0 = new PhoneIntroEntity();
		mobile0.title = "家庭族谱通讯录";
		mobile0.content = CommonValue.subTitle.subtitle2;
		mobile0.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
		mobilesInPhone.add(mobile0);
		PhoneIntroEntity mobile1 = new PhoneIntroEntity();
		mobile1.title = "个人微友通讯录";
		mobile1.content = CommonValue.subTitle.subtitle2;
		mobile1.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
		mobilesInPhone.add(mobile1);
		phones.add(mobilesInPhone);
		mPhoneAdapter = new IndexPhoneAdapter(this, phones);
		mPinedListView1.setAdapter(mPhoneAdapter);
		mPinedListView2 = (PinnedHeaderListView) lay2.findViewById(R.id.tab2_listView);
		mPinedListView2.setDividerHeight(0);
		activities = new ArrayList<List<ActivityIntroEntity>>();
		mActivityAdapter = new IndexActivityAdapter(this, activities);
		mPinedListView2.setAdapter(mActivityAdapter);
		mPinedListView2.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onSectionClick(AdapterView<?> adapterView, View view,
					int section, long id) {
				
			}
			
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int section,
					int position, long id) {
				ActivityIntroEntity entity = activities.get(section).get(position);
				showActivityViewWeb(entity, 46);
			}
		});
		
		mPinedListView0 = (PinnedHeaderListView) lay0.findViewById(R.id.tab0_listView);
		mPinedListView0.setDividerHeight(0);
		cards = new ArrayList<List<CardIntroEntity>>();
		mCardAdapter = new IndexCardAdapter(this, cards);
		mPinedListView0.setAdapter(mCardAdapter);
		
//		mListView3 = (ListView) lay3.findViewById(R.id.tab3_listView);
//		mListView3.setDividerHeight(0);
	}
	
	public void showMobileView() {
		Intent intent = new Intent(this, HomeContactActivity.class);
		startActivity(intent);
	}
	
	public void showFriendCardView() {
		Intent intent = new Intent(this, FriendCards.class);
		startActivity(intent);
	}
	
	public void showPhoneView(PhoneIntroEntity entity) {
		Intent intent = new Intent(this, PhonebookViewMembers.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.PhoneView, entity);
		startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}
	
	public void showPhoneViewWeb(PhoneIntroEntity entity) {
		Intent intent = new Intent(this, PhonebookViewWeb.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/book/%s", CommonValue.BASE_URL, entity.code));
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}
	
	
//	private void showActivityView(ActivityIntroEntity entity) {
//		Intent intent = new Intent(this, AcivityViewMembers.class);
//		intent.putExtra(CommonValue.IndexIntentKeyValue.PhoneView, entity);
//		startActivityForResult(intent, CommonValue.ActivityViewUrlRequest.editActivity);
//	}
	
	private void showActivityViewWeb(ActivityIntroEntity entity, int RequestCode) {
		Intent intent = new Intent(this, ActivityViewWeb.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/event/%s", CommonValue.BASE_URL, entity.code));
	    startActivityForResult(intent, RequestCode);
	}
	
	public void showCardViewWeb(CardIntroEntity entity) {
		Intent intent = new Intent(this, CardViewWeb.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/card/%s", CommonValue.BASE_URL, entity.code));
		startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	private void showMessage() {
		messageView.setVisibility(View.INVISIBLE);
		Intent intent = new Intent(this, MessageView.class);
		startActivity(intent);
	}
	
	public void showScan() {
		Intent intent = new Intent(this, CaptureActivity.class);
		startActivity(intent);
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
		case R.id.phoneLayout:
		case R.id.phoneButton:
			mPager.setCurrentItem(PAGE1);
			break;
		case R.id.activityLayout:
		case R.id.activityButton:
			mPager.setCurrentItem(PAGE2);
			break;
		case R.id.cardLayout:
		case R.id.cardButton:
			mPager.setCurrentItem(PAGE3);
			break;
		}
	}
	
	private void getCache() {
		getCacheUser();
		getPhoneListFromCache();
		getActivityListFromCache();
		getCardListFromCache();
	}
	
	private void getCacheUser() {
		UserEntity user = appContext.getLoginInfo();
		imageLoader.displayImage(user.headimgurl, avatarImageView, CommonValue.DisplayOptions.avatar_options);
	}
	
	private void getPhoneListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.PhoneList, appContext.getLoginUid());
		PhoneListEntity entity = (PhoneListEntity) appContext.readObject(key);
		if(entity == null){
			return;
		}
		phones.clear();
		List<PhoneIntroEntity> mobilesInPhone = new ArrayList<PhoneIntroEntity>();
		PhoneIntroEntity mobile = new PhoneIntroEntity();
		mobile.title = "手机通讯录";
		mobile.content = CommonValue.subTitle.subtitle1;
		mobile.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
		mobilesInPhone.add(mobile);
		PhoneIntroEntity mobile1 = new PhoneIntroEntity();
		PhoneIntroEntity mobile0 = new PhoneIntroEntity();
		mobile0.title = "家庭族谱通讯录";
		mobile0.content = CommonValue.subTitle.subtitle2;
		mobile0.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
		mobilesInPhone.add(mobile0);
		mobile1.title = "个人微友通讯录";
		mobile1.content = CommonValue.subTitle.subtitle2;
		mobile1.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
		mobilesInPhone.add(mobile1);
		phones.add(mobilesInPhone);
		if (entity.owned.size()>0) {
			phones.add(entity.owned);
		}
		if (entity.joined.size()>0) {
			phones.add(entity.joined);
		}
		mPhoneAdapter.notifyDataSetChanged();
	}
	
	private void getActivityListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.ActivityList, appContext.getLoginUid());
		ActivityListEntity entity = (ActivityListEntity) appContext.readObject(key);
		if(entity == null){
			return;
		}
		activities.clear();
		if (entity.owned.size()>0) {
			activities.add(entity.owned);
		}
		if (entity.joined.size()>0) {
			activities.add(entity.joined);
		}
		mActivityAdapter.notifyDataSetChanged();
	}
	
	private void getCardListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.CardList, appContext.getLoginUid());
		CardListEntity entity = (CardListEntity) appContext.readObject(key);
		if(entity == null){
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
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.autoLogin(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				UserEntity user = (UserEntity)data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					appContext.saveLoginInfo(user);
					imageLoader.displayImage(user.headimgurl, avatarImageView, CommonValue.DisplayOptions.avatar_options);
					getPhoneList();
					getActivityList();
					getCardList();
					getUnReadMessage();
					if (!Utils.hasBind(getApplicationContext())) {
						blindBaidu();
					}
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), user.getMessage(), Toast.LENGTH_SHORT);
					showLogin();
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
	
	private void showLogin() {
		appContext.setUserLogout();
		Intent intent = new Intent(this,LoginCode1.class);
        startActivity(intent);
        finish();
        
	}
	
	private String[] lianxiren1 = new String[] { "创建通讯录", "创建活动", "创建我的名片"};
	
	private void showContactDialog(){
		new AlertDialog.Builder(this).setTitle("").setItems(lianxiren1,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					showCreate(CommonValue.CreateViewUrlAndRequest.ContactCreateUrl, CommonValue.CreateViewUrlAndRequest.ContactCreat);
					break;
				case 1:
					showCreate(CommonValue.CreateViewUrlAndRequest.ActivityCreateUrl, CommonValue.CreateViewUrlAndRequest.ActivityCreateCreat);
					break;
				case 2:
					showCreate(CommonValue.CreateViewUrlAndRequest.CardCreateUrl, CommonValue.CreateViewUrlAndRequest.CardCreat);
					break;
				}
			}
		}).show();
	}
	
	private void showCreate(String url, int RequestCode) {
		Intent intent = new Intent(this,CreateView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
        startActivityForResult(intent, RequestCode);
	}
	
	private void getPhoneList() {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.getPhoneList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				PhoneListEntity entity = (PhoneListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					if (phones.size() == 3) {
						if (entity.owned.size()>0) {
							for (PhoneIntroEntity entity1 : phones.get(1)) {
								for (PhoneIntroEntity entity2 : entity.owned) {
									if (entity2.code.equals(entity1.code)) {
										entity2.willRefresh = !entity2.member.equals(entity1.member);
									}
								}
							}
						}
						if (entity.joined.size()>0) {
							for (PhoneIntroEntity entity1 : phones.get(2)) {
								for (PhoneIntroEntity entity2 : entity.owned) {
									if (entity2.code.equals(entity1.code)) {
										entity2.willRefresh = !entity2.member.equals(entity1.member);
									}
								}
							}
						}
					}
					else if (phones.size() == 2) {
						if (phones.get(1).get(0).phoneSectionType.equals(CommonValue.PhoneSectionType.OwnedSectionType)) {
							if (entity.owned.size()>0) {
								for (PhoneIntroEntity entity1 : phones.get(0)) {
									for (PhoneIntroEntity entity2 : entity.owned) {
										if (entity2.code.equals(entity1.code)) {
											entity2.willRefresh = !entity2.member.equals(entity1.member);
										}
									}
								}
							}
						}
						else if (phones.get(1).get(0).phoneSectionType.equals(CommonValue.PhoneSectionType.JoinedSectionType)) {
							if (entity.joined.size()>0) {
								for (PhoneIntroEntity entity1 : phones.get(0)) {
									for (PhoneIntroEntity entity2 : entity.owned) {
										if (entity2.code.equals(entity1.code)) {
											entity2.willRefresh = !entity2.member.equals(entity1.member);
										}
									}
								}
							}
						}
					}
					phones.clear();
					List<PhoneIntroEntity> mobilesInPhone = new ArrayList<PhoneIntroEntity>();
					PhoneIntroEntity mobile = new PhoneIntroEntity();
					mobile.title = "手机通讯录";
					mobile.content = CommonValue.subTitle.subtitle1;
					mobile.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
					mobilesInPhone.add(mobile);
					PhoneIntroEntity mobile0 = new PhoneIntroEntity();
					mobile0.title = "家庭族谱通讯录";
					mobile0.content = CommonValue.subTitle.subtitle2;
					mobile0.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
					mobilesInPhone.add(mobile0);
					PhoneIntroEntity mobile1 = new PhoneIntroEntity();
					mobile1.title = "个人微友通讯录";
					mobile1.content = CommonValue.subTitle.subtitle2;
					mobile1.phoneSectionType = CommonValue.PhoneSectionType .MobileSectionType;
					mobilesInPhone.add(mobile1);
					phones.add(mobilesInPhone);
					if (entity.owned.size() > 0) {
						phones.add(entity.owned);
					}
					if (entity.joined.size() > 0) {
						phones.add(entity.joined);
					}
					mPhoneAdapter.notifyDataSetChanged();
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					showLogin();
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
	
	private void getActivityList() {
		AppClient.getActivityList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				ActivityListEntity entity = (ActivityListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					if (activities.size() == 2) {
						if (entity.owned.size()>0) {
							for (ActivityIntroEntity entity1 : activities.get(0)) {
								for (ActivityIntroEntity entity2 : entity.owned) {
									if (entity2.code.equals(entity1.code)) {
										entity2.willRefresh = !entity2.member.equals(entity1.member);
									}
								}
							}
						}
						if (entity.joined.size()>0) {
							for (ActivityIntroEntity entity1 : activities.get(0)) {
								for (ActivityIntroEntity entity2 : entity.owned) {
									if (entity2.code.equals(entity1.code)) {
										entity2.willRefresh = !entity2.member.equals(entity1.member);
									}
								}
							}
						}
					}
					else if (activities.size() == 1) {
						if (activities.get(0).get(0).activitySectionType.equals(CommonValue.ActivitySectionType.OwnedSectionType)) {
							if (entity.owned.size()>0) {
								for (ActivityIntroEntity entity1 : activities.get(0)) {
									for (ActivityIntroEntity entity2 : entity.owned) {
										if (entity2.code.equals(entity1.code)) {
											entity2.willRefresh = !entity2.member.equals(entity1.member);
										}
									}
								}
							}
						}
						else {
							if (entity.joined.size()>0) {
								for (ActivityIntroEntity entity1 : activities.get(0)) {
									for (ActivityIntroEntity entity2 : entity.owned) {
										if (entity2.code.equals(entity1.code)) {
											entity2.willRefresh = !entity2.member.equals(entity1.member);
										}
									}
								}
							}
						}
					}
					activities.clear();
					if (entity.owned.size()>0) {
						activities.add(entity.owned);
					}
					if (entity.joined.size()>0) {
						activities.add(entity.joined);
					}
					mActivityAdapter.notifyDataSetChanged();
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
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void getCardList() {
		AppClient.getCardList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				CardListEntity entity = (CardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					cards.clear();
					if (entity.owned.size()>0) {
						cards.add(entity.owned);
					}
					addCardOp();
					mCardAdapter.notifyDataSetChanged();
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
				((AppException)e).makeToast(getApplicationContext());
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
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void getUnReadMessage() {
		AppClient.getUnReadMessage(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				MessageUnReadEntity entity = (MessageUnReadEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					if (entity.news.equals("0")) {
						messageView.setVisibility(View.INVISIBLE);
					}
					else {
						messageView.setText(entity.news);
						messageView.setVisibility(View.VISIBLE);
					}
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
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	// ViewPager页面切换监听
	public class MyOnPageChangeListener implements OnPageChangeListener {

//		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

		public void onPageSelected(int arg0) {
//			Animation animation = null;
			switch (arg0) {
			case PAGE1:// 切换到页卡1
//				if (currentIndex == PAGE1) {// 如果之前显示的是页卡2
//					animation = new TranslateAnimation(0, -one, 0, 0);
//				} else if (currentIndex == PAGE2) {// 如果之前显示的是页卡3
//					animation = new TranslateAnimation(one, -one, 0, 0);
//				}
				phoneButton.setSelected(true);
				activityButton.setSelected(false);
				cardButton.setSelected(false);
				break;
			case PAGE2:// 切换到页卡2
//				if (currentIndex == PAGE1) {// 如果之前显示的是页卡1
//					animation = new TranslateAnimation(-one, 0, 0, 0);
//				} else if (currentIndex == PAGE3) {// 如果之前显示的是页卡3
//					animation = new TranslateAnimation(one, 0, 0, 0);
//				}
				phoneButton.setSelected(false);
				activityButton.setSelected(true);
				cardButton.setSelected(false);
				break;
			case PAGE3:// 切换到页卡3
//				if (currentIndex == PAGE1) {// 如果之前显示的是页卡1
//					animation = new TranslateAnimation(-one, one, 0, 0);
//				} else if (currentIndex == PAGE2) {// 如果之前显示的是页卡2
//					animation = new TranslateAnimation(0, one, 0, 0);
//				}
				phoneButton.setSelected(false);
				activityButton.setSelected(false);
				cardButton.setSelected(true);
				break;
//			case PAGE1:
//				phoneButton.setSelected(false);
//				activityButton.setSelected(false);
//				cardButton.setSelected(false);
//				break;
			}
//			currentIndex = arg0;// 动画结束后，改变当前图片位置
//				animation.setFillAfter(true);// True:图片停在动画结束位置
//				animation.setDuration(300);
//				cursor.startAnimation(animation);
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	}
	
	private String[] ot = new String[] { "推荐给好友", "分享到朋友圈"};
	
	public void showShareDialog(final PhoneIntroEntity phoneIntro){
		new AlertDialog.Builder(this).setTitle("").setItems(ot,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					showShare(false, Wechat.NAME, phoneIntro);
					break;
				case 1:
					showShare(false, WechatMoments.NAME, phoneIntro);
					break;
				}
			}
		}).show();
	}
	
	private void showShare(boolean silent, String platform, PhoneIntroEntity phoneIntro) {
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle("群友通讯录");
			oks.setText(String.format("您好，我在征集%s群通讯录，点击下面的链接进入填写，填写后可申请查看群友的通讯录等，谢谢。", phoneIntro.title));
			oks.setImagePath("file:///android_asset/ic_launcher.png");
			oks.setUrl(CommonValue.BASE_URL+"/"+phoneIntro.code);
			oks.setSilent(silent);
			if (platform != null) {
				oks.setPlatform(platform);
			}
			oks.show(this);
		} catch (Exception e) {
			Logger.i(e);
		}
	}
	
	private void addCardOp() {
		List<CardIntroEntity> ops = new ArrayList<CardIntroEntity>();
		CardIntroEntity op1 = new CardIntroEntity();
		op1.realname = "我微友通讯录二维码";
		op1.department = CommonValue.subTitle.subtitle1;
		op1.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		ops.add(op1);
		CardIntroEntity op2 = new CardIntroEntity();
		op2.realname = "扫一扫";
		op2.department = CommonValue.subTitle.subtitle1;
		op2.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		ops.add(op2);
		cards.add(ops);
		
		List<CardIntroEntity> ops1 = new ArrayList<CardIntroEntity>();
		CardIntroEntity op11 = new CardIntroEntity();
		op11.realname = "加V认证";
		op11.department = CommonValue.subTitle.subtitle1;
		op11.cardSectionType = CommonValue.CardSectionType .VSectionType;
		ops1.add(op11);
		cards.add(ops1);
		
		List<CardIntroEntity> ops2 = new ArrayList<CardIntroEntity>();
		CardIntroEntity op21 = new CardIntroEntity();
		op21.realname = "客服反馈";
		op21.department = CommonValue.subTitle.subtitle1;
		op21.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
		ops2.add(op21);
		cards.add(ops2);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.CreateViewUrlAndRequest.ContactCreat:
			getPhoneList();
			int result = data.getIntExtra("resultcode", 0);
			if (result == CommonValue.CreateViewJSType.goPhonebookView) {
				PhoneIntroEntity entity = new PhoneIntroEntity();
				entity.code = data.getStringExtra("resultdata");
				entity.content = " ";
				showPhoneView(entity);
			}
			mPager.setCurrentItem(PAGE1);
			break;
		case CommonValue.CreateViewUrlAndRequest.ActivityCreateCreat:
			getActivityList();
			int result1 = data.getIntExtra("resultcode", 0);
			if (result1 == CommonValue.CreateViewJSType.goPhonebookView) {
				ActivityIntroEntity entity = new ActivityIntroEntity();
				entity.code = data.getStringExtra("resultdata");
				entity.content = " ";
				showActivityViewWeb(entity, 46);
			}
			mPager.setCurrentItem(PAGE2);
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
	
}
