package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppException;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.ActivityViewMembersAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView.OnItemClickListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import bean.ActivityIntroEntity;
import bean.ActivityViewEntity;
import bean.CardIntroEntity;
import bean.Entity;
import bean.PhoneIntroEntity;
import bean.PhoneViewEntity;
import bean.Result;
import bean.SMSPersonList;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;

public class AcivityViewMembers extends AppActivity{
	private TextView titleBarView;
	private List<List<CardIntroEntity>> cards;
	private PinnedHeaderListView mPinedListView;
	private ActivityViewMembersAdapter mCardAdapter;
	private TextView contentView;
	private ProgressDialog loadingPd;
	
	private Button addMyMobileButton;
	private Button editMyMobileButton;
	private LinearLayout adminLayout;
	private ActivityViewEntity activityview;;
	private Button rightBarButton;
	private SMSPersonList smsPersons;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_members);
		initUI();
		initData();
	}
	
	private void initUI() {
		rightBarButton = (Button) findViewById(R.id.rightBarButton);
		titleBarView = (TextView) findViewById(R.id.titleBarView);
		LayoutInflater inflater = LayoutInflater.from(this);
		View header = inflater.inflate(R.layout.view_members_header, null);
		contentView = (TextView) header.findViewById(R.id.headerContentView);
		contentView.setGravity(Gravity.LEFT);
		LayoutParams p = (LayoutParams) contentView.getLayoutParams();
		p.width = ImageUtils.getDisplayWidth(this) - ImageUtils.dip2px(this, 8);
		p.leftMargin = ImageUtils.dip2px(this, 8);
		p.rightMargin = ImageUtils.dip2px(this, 8);
		contentView.setLayoutParams(p);
		View footer = inflater.inflate(R.layout.activityview_members_footer, null);
		addMyMobileButton = (Button)footer.findViewById(R.id.addMyMobile);
		editMyMobileButton = (Button)footer.findViewById(R.id.editMyMobile);
		adminLayout = (LinearLayout) footer.findViewById(R.id.adminView);
		mPinedListView = (PinnedHeaderListView) findViewById(R.id.listView);
		mPinedListView.setDividerHeight(0);
		mPinedListView.addHeaderView(header);
		mPinedListView.addFooterView(footer, null, false);
		cards = new ArrayList<List<CardIntroEntity>>();
		mCardAdapter = new ActivityViewMembersAdapter(this, cards, appContext);
		mPinedListView.setAdapter(mCardAdapter);
	}
	
	private void initData() {
		smsPersons = new SMSPersonList();
		ActivityIntroEntity entity = (ActivityIntroEntity) getIntent().getSerializableExtra(CommonValue.IndexIntentKeyValue.PhoneView);
		titleBarView.setText(entity.title);
		contentView.setText(String.format("活动时间:%s\n活动地点:%s", entity.begin_at, entity.address));
		getActivityViewFromCache(entity);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			SMSDialog();
			break;
		case R.id.addMyMobile:
			String url = String.format("%s/activity/add/code/%s", CommonValue.BASE_URL, activityview.code);
			showCreate(url, CommonValue.PhonebookViewUrlRequest.editPhoneview);
			break;
		case R.id.shareWechat:
			showShare(false, Wechat.NAME);
			break;
		case R.id.shareTimeline:
			showShare(false, WechatMoments.NAME);
			break;
		case R.id.editMyMobile:
			try{
				String url1 = String.format("%s/card/setting/id/%s?return=/activity/view/code/%s", CommonValue.BASE_URL, activityview.added, activityview.code);
				showCreate(url1, CommonValue.PhonebookViewUrlRequest.editPhoneview);
			} catch (Exception e) {
				Logger.i(e);
			}
			break;
		case R.id.editActivityView:
			try{
				String url2 = String.format("%s/activity/create/code/%s", CommonValue.BASE_URL, activityview.code);
				showCreate(url2, CommonValue.PhonebookViewUrlRequest.editPhoneview);
			} catch (Exception e) {
				Logger.i(e);
			}
			break;
		case R.id.deleteActivityView:
			try{
				String url4 = String.format("%s/activity/remove/code/%s", CommonValue.BASE_URL, activityview.code);
				showCreate(url4, CommonValue.PhonebookViewUrlRequest.deletePhoneview);
			} catch (Exception e) {
				Logger.i(e);
			}
			break;
		}
	}
	
	private void getActivityViewFromCache(ActivityIntroEntity model) {
		String key = String.format("%s-%s-%s", CommonValue.CacheKey.ActivityView, model.code, appContext.getLoginUid());
		ActivityViewEntity entity = (ActivityViewEntity) appContext.readObject(key);
		if(entity == null){
			getActivityView(model.code);
			return;
		}
		if (entity.members.size()>0) {
			handleRight(entity);
		}
		getActivityView(model.code);
	}
	
	private void getActivityView(String code) {
		if (!appContext.isNetworkConnected()) {
			UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
			return;
		}
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.getActivityView(appContext, code, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				ActivityViewEntity entity = (ActivityViewEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					activityview = entity;
					if (entity.members.size()>0) {
						cards.clear();
						cards.add(entity.members);
					}
					handleRight(entity);
					mCardAdapter.notifyDataSetChanged();
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
	
	private void handleRight(ActivityViewEntity entity) {
		try {
			if (entity.openid.equals(appContext.getLoginUid()) && entity.members.size()>0) { // 发起人
				rightBarButton.setVisibility(View.VISIBLE);
				smsPersons.members.clear();
				smsPersons.members.addAll(entity.members);
			}
		} catch(Exception e) {
			Logger.i(e);
		}
		addMyMobileButton.setVisibility(View.GONE);
		editMyMobileButton.setVisibility(View.GONE);
		adminLayout.setVisibility(View.GONE);
		mCardAdapter.activity = entity;
		if (entity.added.equals(CommonValue.PhonebookLimitRight.Add_No)) {
			addMyMobileButton.setVisibility(View.VISIBLE);
		} else {
			editMyMobileButton.setVisibility(View.VISIBLE);
		}
		cards.clear();
		if (entity.members.size() > 0) {
			cards.add(entity.members);
			mCardAdapter.notifyDataSetChanged();
		}
		int isAdmin = Integer.valueOf(entity.is_admin);
		switch (isAdmin) {
		case CommonValue.PhonebookViewIsAdmin.AdminYes:
			adminLayout.setVisibility(View.VISIBLE);
			break;
		}
		titleBarView.setText(entity.title);
		contentView.setText(String.format("发起人:%s\n活动时间:%s\n活动地点:%s", entity.creator, entity.begin_at, entity.address));
		ViewTreeObserver observer = contentView.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver obs = contentView.getViewTreeObserver();
				obs.removeGlobalOnLayoutListener(this);
				if(contentView.getLineCount() > 6) {//判断行数大于多少时改变
					int lineEndIndex = contentView.getLayout().getLineEnd(5); //设置第六行打省略号
					String text = contentView.getText().subSequence(0, lineEndIndex-3) +"...";
					contentView.setText(text);
				}
			}
		});
	}
	
	private void showShare(boolean silent, String platform) {
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle("群友通讯录");
			oks.setText(String.format("群友聚会，帮您更方便的发起聚会、签到报名，自动通知，统计人数。%s", activityview.link));
			oks.setImagePath("file:///android_asset/ic_launcher.png");
			oks.setUrl(activityview.link);
			oks.setSilent(silent);
			if (platform != null) {
				oks.setPlatform(platform);
			}
			oks.show(this);
		} catch (Exception e) {
			((AppException)e).makeToast(getApplicationContext());
		}
	}
	
	private void showCreate(String url, int RequestCode) {
		Intent intent = new Intent(this,QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
        startActivityForResult(intent, RequestCode);
	}
	
	protected void SMSDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("允许群友通讯录发送短信?");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showSMS();
			}
		});

	   builder.setNegativeButton("取消", new OnClickListener() {
		   @Override
		   public void onClick(DialogInterface dialog, int which) {
			   dialog.dismiss();
		   }
	   });
	   builder.create().show();
	}
	
	private void showSMS() {
		Intent intent = new Intent(this,PhonebookSMS.class);
		intent.putExtra(CommonValue.PhonebookViewIntentKeyValue.SMS, smsPersons);
        startActivityForResult(intent, CommonValue.PhonebookViewIntentKeyValue.SMSRequest);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.PhonebookViewUrlRequest.editPhoneview:
			getActivityView(activityview.code);
			setResult(RESULT_OK);
			break;
		case CommonValue.PhonebookViewUrlRequest.deletePhoneview:
			setResult(RESULT_OK);
			AppManager.getAppManager().finishActivity(AcivityViewMembers.this);
			break;
		}
	}
}
