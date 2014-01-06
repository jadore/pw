package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppException;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.QuanZiAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import bean.Entity;
import bean.PhoneIntroEntity;
import bean.PhoneListEntity;
import bean.Result;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class QunZi extends AppActivity {
	
	private ProgressDialog loadingPd;
	private List<List<PhoneIntroEntity>> phones;
	private PinnedHeaderListView mPinedListView;
	private QuanZiAdapter mPhoneAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qunzi);
		initUI();
		getPhoneList();
	}
	
	private void initUI() {
		View header = (View) getLayoutInflater().inflate(R.layout.quzi_header, null);
		mPinedListView = (PinnedHeaderListView) findViewById(R.id.listView);
		mPinedListView.setDividerHeight(0);
		mPinedListView.addHeaderView(header, null, false);
		phones = new ArrayList<List<PhoneIntroEntity>>();
		mPhoneAdapter = new QuanZiAdapter(this, phones);
		mPinedListView.setAdapter(mPhoneAdapter);
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
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.createPhonebook:
			showCreate(CommonValue.CreateViewUrlAndRequest.ContactCreateUrl, CommonValue.CreateViewUrlAndRequest.ContactCreat);
			break;

		case R.id.openQuanZi:
			
			break;
		}
	}
	
	public void showPhoneViewWeb(PhoneIntroEntity entity, int RequestCode) {
		Intent intent = new Intent(this, PhonebookViewWeb.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/book/%s", CommonValue.BASE_URL, entity.code));
	    startActivityForResult(intent, RequestCode);
	}
	
	public void showCreate(String url, int RequestCode) {
//		Intent intent = new Intent(this,CreateView.class);
//		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
//        startActivityForResult(intent, RequestCode);
	}
	
	public void showPhoneView(PhoneIntroEntity entity) {
	Intent intent = new Intent(this, PhonebookViewMembers.class);
	intent.putExtra(CommonValue.IndexIntentKeyValue.PhoneView, entity);
	startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
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
}
