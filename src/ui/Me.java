package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import tools.UIHelper;
import tools.UpdateManager;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;

public class Me extends AppActivity{
	private ExpandableListView iphoneTreeView;
	private List<List<CardIntroEntity>> cards;
	private MeCardAdapter mCardAdapter;
	
	private ProgressDialog loadingPd;
	
	private ImageView avatarView;
	private TextView nameTV;
	private TextView creditTV;
	private DisplayImageOptions avatar_options = new DisplayImageOptions.Builder()
	.bitmapConfig(Bitmap.Config.RGB_565)
	.cacheInMemory(true)
	.cacheOnDisc(true)
	.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
	.displayer(new RoundedBitmapDisplayer(10))
	.build();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me);
		initUI();
		addCardOp();
		mCardAdapter.notifyDataSetChanged();
		expandView();
		this.imageLoader.displayImage(appContext.getUserAvatar(), avatarView, avatar_options);
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
		View header = inflater.inflate(R.layout.more_headerview, null);
		avatarView = (ImageView) header.findViewById(R.id.avatar);
		nameTV = (TextView) header.findViewById(R.id.title);
		creditTV = (TextView) header.findViewById(R.id.jifen);
		nameTV.setText(appContext.getNickname());
		creditTV.setText("我的积分: "+appContext.getCredits());
		iphoneTreeView.addHeaderView(header);
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
			
			getCardList();
			return;
		}
		cards.clear();
		if (entity.owned.size()>0) {
			cards.add(entity.owned);
		}
		mCardAdapter.notifyDataSetChanged();
		expandView();
		getCardList();
	}
	
	private void addCardOp() {
		List<CardIntroEntity> ops = new ArrayList<CardIntroEntity>();
		CardIntroEntity op0 = new CardIntroEntity();
		op0.realname = "我的名片";
		op0.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op0.department = R.drawable.icon_set_card+"";
		op0.position = "";
		ops.add(op0);
		
		CardIntroEntity op2 = new CardIntroEntity();
		op2.realname = "扫一扫";
		op2.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op2.position = "";
		op2.department = R.drawable.icon_set_ocr+"";
		ops.add(op2);
		
		CardIntroEntity op1 = new CardIntroEntity();
		op1.realname = "我的二维码";
		op1.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op1.position = "";
		op1.department = R.drawable.icon_set_barcode+"";
		ops.add(op1);
		cards.add(ops);
		
		List<CardIntroEntity> ops2 = new ArrayList<CardIntroEntity>();
		CardIntroEntity op20 = new CardIntroEntity();
		op20.realname = "设置";
		op20.position = "";
		op20.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
		op20.department = R.drawable.icon_set_setting+"";
		ops2.add(op20);
		CardIntroEntity op21 = new CardIntroEntity();
		op21.realname = "客服反馈";
		op21.position = "";
		op21.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
		op21.department = R.drawable.icon_set_feedback+"";
		ops2.add(op21);
		CardIntroEntity op22 = new CardIntroEntity();
		op22.realname = "版本升级("+getCurrentVersionName()+")";
		op22.position = "";
		op22.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
		op22.department = R.drawable.icon_set_update+"";
		ops2.add(op22);
		cards.add(ops2);
	}
	
	/**
	 * 获取当前客户端版本信息
	 */
	private String  getCurrentVersionName(){
		String versionName = null;
        try { 
        	PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
        	versionName = info.versionName;
        } catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
        return versionName;
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
//					forceLogout();
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
	
	public void showMyCard() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看我的名片",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, MyCard.class);
		startActivity(intent);
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
	
	public void showSetting() {
		Intent intent = new Intent(Me.this, Setting.class);
		startActivity(intent);
	}
	
	public void showFeedback() {
		Intent intent = new Intent(this, Feedback.class);
		startActivity(intent);
	}
	
	public void showUpdate() {
		UpdateManager.getUpdateManager().checkAppUpdate(this, true);
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
