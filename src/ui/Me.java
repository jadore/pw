package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.IndexCardAdapter;
import ui.adapter.MeCardAdapter;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.Result;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.zxing.client.android.CaptureActivity;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;

public class Me extends AppActivity{
	private ExpandableListView iphoneTreeView;
	private List<List<CardIntroEntity>> cards;
	private MeCardAdapter mCardAdapter;
	
	private ProgressDialog loadingPd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me);
		initUI();
		getCardListFromCache();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.rightBarButton:
			Intent intent = new Intent(Me.this, Setting.class);
			startActivity(intent);
			break;
		}
	}
	
	private void initUI() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View footer = inflater.inflate(R.layout.index_footer, null);
		iphoneTreeView = (ExpandableListView) findViewById(R.id.iphone_tree_view);
		iphoneTreeView.setGroupIndicator(null);
		iphoneTreeView.addFooterView(footer);
		cards = new ArrayList<List<CardIntroEntity>>();
		mCardAdapter = new MeCardAdapter(iphoneTreeView, this, cards);
		iphoneTreeView.setAdapter(mCardAdapter);
		iphoneTreeView.setSelection(0);
		iphoneTreeView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int position,
					long arg3) {
				return true;
			}
		});
	}
	
	private void expandView() {
		for (int i = 0; i < cards.size(); i++) {
			iphoneTreeView.expandGroup(i);
		}
	}
	
	private void getCardListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.CardList, appContext.getLoginUid());
		CardListEntity entity = (CardListEntity) appContext.readObject(key);
		if(entity == null){
			addCardOp();
			mCardAdapter.notifyDataSetChanged();
			expandView();
			getCardList();
			return;
		}
		cards.clear();
		if (entity.owned.size()>0) {
			cards.add(entity.owned);
		}
		addCardOp();
		mCardAdapter.notifyDataSetChanged();
		expandView();
		getCardList();
	}
	
	private void addCardOp() {
		List<CardIntroEntity> ops = new ArrayList<CardIntroEntity>();
		CardIntroEntity op1 = new CardIntroEntity();
		op1.realname = "我微友通讯录二维码";
		op1.department = CommonValue.subTitle.subtitle4;
		op1.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op1.position = "";
		ops.add(op1);
		CardIntroEntity op2 = new CardIntroEntity();
		op2.realname = "扫一扫";
		op2.department = CommonValue.subTitle.subtitle5;
		op2.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op2.position = "";
		ops.add(op2);
		cards.add(ops);
		
//		List<CardIntroEntity> ops2 = new ArrayList<CardIntroEntity>();
//		CardIntroEntity op21 = new CardIntroEntity();
//		op21.realname = "客服反馈";
//		op21.department = CommonValue.subTitle.subtitle6;
//		op21.position = "";
//		op21.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
//		ops2.add(op21);
//		cards.add(ops2);
//		
//		List<CardIntroEntity> ops3 = new ArrayList<CardIntroEntity>();
//		CardIntroEntity op31 = new CardIntroEntity();
//		op31.realname = "功能消息免打扰";
//		op31.department = "开启免打扰后，功能消息将收不到声音和震动提醒。";
//		op31.position = "";
//		op31.cardSectionType = CommonValue.CardSectionType .SettingsSectionType;
//		ops3.add(op31);
//		CardIntroEntity op32 = new CardIntroEntity();
//		op32.realname = "检查版本";
//		op32.department = "当前版本:"+getCurrentVersionName();
//		op32.position = "";
//		op32.cardSectionType = CommonValue.CardSectionType .SettingsSectionType;
//		ops3.add(op32);
//		
//		CardIntroEntity op33 = new CardIntroEntity();
//		op33.realname = "注销";
//		op33.department = "退出当前账号重新登录";
//		op33.position = "";
//		op33.cardSectionType = CommonValue.CardSectionType .SettingsSectionType;
//		ops3.add(op33);
//		
//		cards.add(ops3);
		
	}
	
	private void getCardList() {
//		indicatorImageView.setVisibility(View.VISIBLE);
//    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getCardList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
//				indicatorImageView.clearAnimation();
//				indicatorImageView.setVisibility(View.INVISIBLE);
				CardListEntity entity = (CardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					cards.clear();
					if (entity.owned.size()>0) {
						cards.add(entity.owned);
					}
					Logger.i(cards.size()+"");
					addCardOp();
					mCardAdapter.notifyDataSetChanged();
					expandView();
					break;
				case CommonValue.USER_NOT_IN_ERROR:
					forceLogout();
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
//				indicatorImageView.clearAnimation();
//				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
//				indicatorImageView.clearAnimation();
//				indicatorImageView.setVisibility(View.INVISIBLE);
				e.printStackTrace();
				Logger.i(e);
			}
		});
	}
	
	public void showCardViewWeb(CardIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
		startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	public void showMyBarcode() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片二维码："+String.format("%s/card/mybarcode", CommonValue.BASE_URL),   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/card/mybarcode", CommonValue.BASE_URL));
		startActivity(intent);
	}
	
	public void showScan() {
		Intent intent = new Intent(this, CaptureActivity.class);
		startActivity(intent);
	}
	
	public void oks(String title, String text, String link, String filePath) {
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle(title);
			if (StringUtils.notEmpty(filePath)) {
				oks.setImagePath(filePath);
			}
			else {
				String cachePath = cn.sharesdk.framework.utils.R.getCachePath(this, null);
				oks.setImagePath(cachePath + "logo.png");
			}
			oks.setText(text + "\n" + link);
			oks.setUrl(link);
			oks.setSiteUrl(link);
			oks.setSite(link);
			oks.setTitleUrl(link);
			oks.setLatitude(23.056081f);
			oks.setLongitude(113.385708f);
			oks.setSilent(false);
			oks.show(this);
		} catch (Exception e) {
			Logger.i(e);
		}
	}
	
	public void cardShare(boolean silent, String platform, CardIntroEntity card, String filePath) {
		try {
			String text = (StringUtils.notEmpty(card.intro)?card.intro:String.format("您好，我叫%s，这是我的名片，请多多指教。",card.realname));
			oks(card.realname, text, card.link, filePath);
		} catch (Exception e) {
			Logger.i(e);
		}
	}
	
	public void cardSharePre(final boolean silent, final String platform, final CardIntroEntity card) {
		if (StringUtils.empty(appContext.getLoginInfo().headimgurl)) {
			cardShare(silent, platform, card, "");
			return;
		}
		String storageState = Environment.getExternalStorageState();	
		if(storageState.equals(Environment.MEDIA_MOUNTED)){
			String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qy/" + MD5Util.getMD5String(appContext.getLoginInfo().headimgurl) + ".png";
			File file = new File(savePath);
			if (file.exists()) {
				cardShare(silent, platform, card, savePath);
			}
			else {
				loadingPd = UIHelper.showProgress(Me.this, null, null, true);
				AppClient.downFile(this, appContext, appContext.getLoginInfo().headimgurl, ".png", new FileCallback() {
					@Override
					public void onSuccess(String filePath) {
						UIHelper.dismissProgress(loadingPd);
						cardShare(silent, platform, card, filePath);
					}
					
					@Override
					public void onFailure(String message) {
						UIHelper.dismissProgress(loadingPd);
						cardShare(silent, platform, card, "");
					}
					
					@Override
					public void onError(Exception e) {
						UIHelper.dismissProgress(loadingPd);
						cardShare(silent, platform, card, "");
					}
				});
			}
		}
	}
}	
