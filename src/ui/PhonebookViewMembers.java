package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppException;
import tools.AppManager;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.PhonebookViewMembersAdapter;
import ui.adapter.ActivityViewMembersAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView.OnItemClickListener;
import bean.CardIntroEntity;
import bean.CardListEntity;
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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PhonebookViewMembers extends AppActivity{
	private List<List<CardIntroEntity>> cards;
	private PinnedHeaderListView mPinedListView;
	private PhonebookViewMembersAdapter mCardAdapter;
	
	private ProgressDialog loadingPd;
	private TextView nothingView;
	private Button addMyMobileButton;
	private Button editMyMobileButton;
	private LinearLayout adminLayout;
	private PhoneViewEntity phonebook;
	private Button rightBarButton;
	private SMSPersonList smsPersons;
	private TextView titleView;
	private TextView contentView;
	private TextView creatorView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_members);
		initUI();
		initData();
	}
	
	private void initUI() {
		rightBarButton = (Button) findViewById(R.id.rightBarButton);
		LayoutInflater inflater = LayoutInflater.from(this);
		View header = inflater.inflate(R.layout.view_members_header, null);
		titleView = (TextView) header.findViewById(R.id.titleView);
		contentView = (TextView) header.findViewById(R.id.headerContentView);
		creatorView = (TextView) header.findViewById(R.id.creatorView);
		View footer = inflater.inflate(R.layout.phoneview_members_footer, null);
		nothingView = (TextView) footer.findViewById(R.id.nothingView);
		addMyMobileButton = (Button)footer.findViewById(R.id.addMyMobile);
		editMyMobileButton = (Button)footer.findViewById(R.id.editMyMobile);
		adminLayout = (LinearLayout) footer.findViewById(R.id.adminView);
		mPinedListView = (PinnedHeaderListView) findViewById(R.id.listView);
		mPinedListView.setDividerHeight(0);
		mPinedListView.addHeaderView(header, null, false);
		mPinedListView.addFooterView(footer, null, false);
		cards = new ArrayList<List<CardIntroEntity>>();
		phonebook = new PhoneViewEntity();
		mCardAdapter = new PhonebookViewMembersAdapter(this, cards, appContext);
		mPinedListView.setAdapter(mCardAdapter);
	}
	
	private void initData() {
		smsPersons = new SMSPersonList();
		PhoneIntroEntity entity = (PhoneIntroEntity) getIntent().getSerializableExtra(CommonValue.IndexIntentKeyValue.PhoneView);
		Logger.i(entity.code);
		titleView.setText(entity.title);
		contentView.setText(entity.content);
		getPhoneViewFromCache(entity);
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
			String url = String.format("%s/add/%s", CommonValue.BASE_URL, phonebook.code);
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
				String url1 = String.format("%s/card/setting/id/%s?return=/book/%s", CommonValue.BASE_URL, phonebook.added, phonebook.code);
				showCreate(url1, CommonValue.PhonebookViewUrlRequest.editPhoneview);
			} catch (Exception e) {
				Logger.i(e);
			}
			break;
		case R.id.editPhoneView:
			try{
				String url2 = String.format("%s/index/create/code/%s", CommonValue.BASE_URL, phonebook.code);
				showCreate(url2, CommonValue.PhonebookViewUrlRequest.editPhoneview);
			} catch (Exception e) {
				Logger.i(e);
			}
			break;
		case R.id.createBarcode:
			try{
				String url3 = String.format("%s/index/qrview/code/%s", CommonValue.BASE_URL, phonebook.code);
				showCreate(url3, CommonValue.PhonebookViewUrlRequest.editPhoneview);
			} catch (Exception e) {
				Logger.i(e);
			}
			break;
		case R.id.deletePhoneView:
			try{
				String url4 = String.format("%s/index/remove/code/%s", CommonValue.BASE_URL, phonebook.code);
				showCreate(url4, CommonValue.PhonebookViewUrlRequest.deletePhoneview);
			} catch (Exception e) {
				Logger.i(e);
			}
			break;
		}
	}
	
	private void getPhoneViewFromCache(PhoneIntroEntity model) {
		String key = String.format("%s-%s-%s", CommonValue.CacheKey.PhoneView, model.code, appContext.getLoginUid());
		phonebook = (PhoneViewEntity) appContext.readObject(key);
		if(phonebook == null){
			getPhoneView(model.code);
			return;
		}
		if (phonebook.members.size()>0) {
			handleRight(phonebook);
		}
		getPhoneView(model.code);
	}
	
	private void getPhoneView(String code) {
		if (!appContext.isNetworkConnected()) {
			UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
			return;
		}
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.getPhoneView(appContext, code, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				PhoneViewEntity entity = (PhoneViewEntity)data;
				phonebook = entity;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handleRight(entity);
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
	
	private void showShare(boolean silent, String platform) {
		Logger.i(phonebook.link);
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle("群友通讯录");
			oks.setText(String.format("您好，我在征集%s群通讯录，点击下面的链接进入填写，填写后可申请查看群友的通讯录等，谢谢。%s", phonebook.title, phonebook.link));
			oks.setImagePath("file:///android_asset/ic_launcher.png");
			oks.setUrl(phonebook.link);
			oks.setSilent(silent);
			if (platform != null) {
				oks.setPlatform(platform);
			}
			oks.show(this);
		} catch (Exception e) {
			Logger.i(e);
		}
	}
	
	private void handleRight(PhoneViewEntity entity) {
		try {
			if (entity.wechat_id.equals(appContext.getLoginUid()) && entity.members.size()>0) { // 发起人
				rightBarButton.setVisibility(View.VISIBLE);
				smsPersons.members.clear();
				smsPersons.members.addAll(entity.members);
			}
		} catch(Exception e) {
			Logger.i(e);
		}
		numbersInSection(entity);
		nothingView.setVisibility(View.GONE);
		addMyMobileButton.setVisibility(View.GONE);
		editMyMobileButton.setVisibility(View.GONE);
		adminLayout.setVisibility(View.GONE);
		if (cards.size() == 0) {
			nothingView.setVisibility(View.VISIBLE);
		}
		if (entity.added.equals(CommonValue.PhonebookLimitRight.Add_No)) {
			addMyMobileButton.setVisibility(View.VISIBLE);
		} else {
			editMyMobileButton.setVisibility(View.VISIBLE);
		}
		int isReadable = Integer.valueOf(entity.readable);
		switch (isReadable) {
		case CommonValue.PhonebookViewIsReadable.Applying:
		case CommonValue.PhonebookViewIsReadable.Refuse:
			nothingView.setText(String.format("等待发起人(%s)审核", entity.creator));
			nothingView.setVisibility(View.VISIBLE);
			break;
		}
		int isAdmin = Integer.valueOf(entity.is_admin);
		switch (isAdmin) {
		case CommonValue.PhonebookViewIsAdmin.AdminYes:
			adminLayout.setVisibility(View.VISIBLE);
			break;
		}
		titleView.setText(entity.title);
		creatorView.setText("通讯录由"+entity.creator+"发起");
		contentView.setText(entity.content);
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
	
	private void numbersInSection(PhoneViewEntity phonebook) {
		if (phonebook.members.size() == 0) {
			return;
		}
		cards.clear();
		if (phonebook.privacy.equals(CommonValue.PhonebookLimitRight.PBprivacy_Yes)) {// 通讯录私密
			isAdmin(phonebook);
		}
		else {
			isAdded(phonebook);//名片是否录入
		}
		mCardAdapter.phonebook = phonebook;
		mCardAdapter.notifyDataSetChanged();
	}
	
	private void isAdmin(PhoneViewEntity phonebook) {
		if (phonebook.is_admin.equals(CommonValue.PhonebookLimitRight.Admin_Yes)) {// 管理员
			cards.add(phonebook.members);
		} else { //非管理员
			isAdded(phonebook);
		}
	}
	
	private void isAdded(PhoneViewEntity phonebook) {
		if (!phonebook.added.equals(CommonValue.PhonebookLimitRight.Add_No)) {// 已录入
				List<CardIntroEntity> temp = new ArrayList<CardIntroEntity>();
				for (CardIntroEntity cardIntroEntity : phonebook.members) {
					if (!cardIntroEntity.role.equals(CommonValue.PhonebookLimitRight.RoleNone)) {
						temp.add(cardIntroEntity);
					}
				}
				cards.add(temp);
		}
		else {// 未录入 显示前3个
			if (phonebook.members.size() > 3) {
				List<CardIntroEntity> temp = new ArrayList<CardIntroEntity>();
				temp.add(phonebook.members.get(0));
				temp.add(phonebook.members.get(1));
				temp.add(phonebook.members.get(2));
				cards.add(temp);
			}else {
				cards.add(phonebook.members);
			}
		}
	}
	
	private void showCreate(String url, int RequestCode) {
		Intent intent = new Intent(this,CreateView.class);
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
			getPhoneView(phonebook.code);
			setResult(RESULT_OK);
			break;
		case CommonValue.PhonebookViewUrlRequest.deletePhoneview:
			setResult(RESULT_OK);
			AppManager.getAppManager().finishActivity(PhonebookViewMembers.this);
			break;
		}
	}
}

