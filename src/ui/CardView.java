package ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import service.AddMobileService;
import sms.MessageBoxList;
import tools.AppException;
import tools.AppManager;
import tools.BaseIntentUtil;
import tools.CircleTransform;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.CardViewAdapter;
import bean.CardIntroEntity;
import bean.Entity;
import bean.KeyValue;
import bean.Result;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.FileCallback;
import config.AppClient.ClientCallback;
import config.CommonValue;
import db.manager.WeFriendManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CardView extends AppActivity implements OnItemClickListener  {
	private CardIntroEntity card;
	private ImageView avatarImageView;
	private TextView titleView;
	private TextView nameView;
	private List<KeyValue> summarys = new ArrayList<KeyValue>();
	private ListView mListView;
	private CardViewAdapter mCardViewAdapter;
	private ProgressDialog loadingPd;
	
	private Button callMobileButton;
	private Button saveMobileButton;
	private Button editMyMobileButton;
	private Button exchangeButton;
	private TextView exchangeView;
	
	private MobileReceiver mobileReceiver;
	
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	

	@Override
	protected void onDestroy() {
//		EasyTracker.getInstance(this).activityStop(this);
		unregisterGetReceiver();
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		EasyTracker.getInstance(this).activityStart(this);  
		setContentView(R.layout.card_view);
		registerGetReceiver();
		initUI();
//		Handler jumpHandler = new Handler();
//        jumpHandler.postDelayed(new Runnable() {
//			public void run() {
				initData();
//			}
//		}, 100);
		
	}
	
	private void initUI() {
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
		LayoutInflater inflater = LayoutInflater.from(this);
		View header = inflater.inflate(R.layout.card_view_header, null);
		avatarImageView = (ImageView) header.findViewById(R.id.avatarImageView);
		nameView = (TextView) header.findViewById(R.id.name);
		titleView = (TextView) header.findViewById(R.id.title);
		View footer = inflater.inflate(R.layout.card_view_footer, null);
		callMobileButton = (Button) footer.findViewById(R.id.callContactButton);
		saveMobileButton = (Button) footer.findViewById(R.id.saveContactButton);
		editMyMobileButton = (Button) footer.findViewById(R.id.editMyMobile);
		exchangeButton = (Button) footer.findViewById(R.id.exchangeMobile); 
		exchangeView = (TextView) footer.findViewById(R.id.exchangeView);
		mListView = (ListView) findViewById(R.id.listView);
		mListView.addHeaderView(header, null, false);
		mListView.addFooterView(footer, null, false);
		mListView.setDividerHeight(0);
		mCardViewAdapter = new CardViewAdapter(this, summarys);
		mListView.setAdapter(mCardViewAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	private void initData() {
		card = (CardIntroEntity) getIntent().getSerializableExtra(CommonValue.CardViewIntentKeyValue.CardView);
		CardIntroEntity data = WeFriendManager.getInstance(this).getCardByOpenid(card.openid);
		if ( data != null ) {
			setData(data);
		}
		else {
			setData(card);
		}
		getCard(card.code);
	}
	
	private void setData(CardIntroEntity entity) {
		if (this.isFinishing()) {
			return;
		}
		card = entity;
		dbUpdate(card);
		if (StringUtils.notEmpty(entity.openid)) {
			if (entity.openid.equals(appContext.getLoginUid())) {
				saveMobileButton.setVisibility(View.GONE);
				editMyMobileButton.setVisibility(View.VISIBLE);
			}
			else {
				editMyMobileButton.setVisibility(View.GONE);
				if (StringUtils.notEmpty(entity.isfriend)) {
					if (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_No)) {
						exchangeButton.setVisibility(View.VISIBLE);
						callMobileButton.setVisibility(View.GONE);
						saveMobileButton.setVisibility(View.GONE);
					}
					else if (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_Wait)) {
						exchangeButton.setVisibility(View.GONE);
						exchangeView.setVisibility(View.VISIBLE);
						callMobileButton.setVisibility(View.GONE);
						saveMobileButton.setVisibility(View.GONE);
					}
					else {
						callMobileButton.setVisibility(View.VISIBLE);
						saveMobileButton.setVisibility(View.VISIBLE);
					}
				}
			}
		}
		Picasso.with(context)
        .load(entity.avatar)
        .placeholder(R.drawable.avatar_placeholder)
        .error(R.drawable.avatar_placeholder)
        .resize(100, 100)
        .centerCrop()
        .transform(new CircleTransform())
        .into(avatarImageView);
		nameView.setText(entity.realname);
		titleView.setText(entity.department +" " +entity.position);
		summarys.clear();
		if (StringUtils.notEmpty(entity.wechat)) {
			KeyValue value = new KeyValue();
			value.key = "微信号";
			value.value = entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)?entity.wechat : "*******(交换名片可见)";
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.email)) {
			KeyValue value = new KeyValue();
			value.key = "邮箱";
			value.value = entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)?entity.email : "*******(交换名片可见)";
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.phone)) {
			KeyValue value = new KeyValue();
			value.key = "手机";
			value.value = entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)?entity.phone : "*******(交换名片可见)";
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.birthday)) {
			KeyValue value = new KeyValue();
			value.key = "生日";
			value.value = entity.birthday;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.address)) {
			KeyValue value = new KeyValue();
			value.key = "地址";
			value.value = entity.address;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.intro)) {
			KeyValue value = new KeyValue();
			value.key = "个人介绍";
			value.value = entity.intro;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.supply)) {
			KeyValue value = new KeyValue();
			value.key = "供需关系";
			value.value = entity.supply;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.needs)) {
			KeyValue value = new KeyValue();
			value.key = "需求关系";
			value.value = entity.needs;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.hometown)) {
			KeyValue value = new KeyValue();
			value.key = "籍贯";
			value.value = entity.hometown;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.interest)) {
			KeyValue value = new KeyValue();
			value.key = "兴趣";
			value.value = entity.interest;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.school)) {
			KeyValue value = new KeyValue();
			value.key = "学校";
			value.value = entity.school;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.homepage)) {
			KeyValue value = new KeyValue();
			value.key = "个人主页";
			value.value = entity.homepage;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.company_site)) {
			KeyValue value = new KeyValue();
			value.key = "公司网站";
			value.value = entity.company_site;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.qq)) {
			KeyValue value = new KeyValue();
			value.key = "QQ";
			value.value = entity.qq;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.weibo)) {
			KeyValue value = new KeyValue();
			value.key = "新浪微博";
			value.value = entity.weibo;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.tencent)) {
			KeyValue value = new KeyValue();
			value.key = "腾讯微博";
			value.value = entity.tencent;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.renren)) {
			KeyValue value = new KeyValue();
			value.key = "人人";
			value.value = entity.renren;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.zhihu)) {
			KeyValue value = new KeyValue();
			value.key = "知乎";
			value.value = entity.zhihu;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.qzone)) {
			KeyValue value = new KeyValue();
			value.key = "QQ空间";
			value.value = entity.qzone;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.facebook)) {
			KeyValue value = new KeyValue();
			value.key = "FACEBOOK";
			value.value = entity.facebook;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.twitter)) {
			KeyValue value = new KeyValue();
			value.key = "Twitter";
			value.value = entity.twitter;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.intentionen)) {
			KeyValue value = new KeyValue();
			value.key = "希望接受的名片";
			value.value = entity.intentionen;
			summarys.add(value);
		}
		mCardViewAdapter.notifyDataSetChanged();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.shareFriendButton:
			cardSharePre(false, null, card);
			break;
//		case R.id.shareTimelineButton:
//			showShare(false, WechatMoments.NAME);
//			break;
		case R.id.saveContactButton:
			addContact(card);
			break;
		case R.id.editMyMobile:
			String url1 = String.format("%s/card/setting/id/%s", CommonValue.BASE_URL, card.code);
			showCreate(url1, CommonValue.CardViewUrlRequest.editCard);
			break;
		case R.id.exchangeMobile:
			exchangeCard(card);
			break;
		case R.id.callContactButton:
			callMobile(card.phone);
			break;
		case R.id.lookupContactButton:
			String url2 = String.format("%s/card/%s", CommonValue.BASE_URL, card.code);
			showCreate(url2, CommonValue.CardViewUrlRequest.editCard);
			break;
		}
	}
	
	private void getCard(String code) {
		if (!appContext.isNetworkConnected()) {
			return;
		}
//		loadingPd = UIHelper.showProgress(this, null, null, true);
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getCard(appContext, code, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				CardIntroEntity entity = (CardIntroEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					setData(entity);
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void dbUpdate(final CardIntroEntity entity) {
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				WeFriendManager.getInstance(CardView.this).updateWeFriend(entity);
			}
		});
	}

	public void addContact(CardIntroEntity entity){
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AddMobileService.actionStartPAY(this, entity, true);
    }
	
	class MobileReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			UIHelper.dismissProgress(loadingPd);
			int type = intent.getIntExtra(CommonValue.ContactOperationResult.ContactOperationResultType, CommonValue.ContactOperationResult.SAVE_FAILURE);
			String message = "";
			switch (type) {
			case CommonValue.ContactOperationResult.EXIST:
				message = "名片已保存了";
				WarningDialog(message);
				break;
			case CommonValue.ContactOperationResult.SAVE_FAILURE:
				message = "保存名片失败";
				WarningDialog(message);
				break;
			case CommonValue.ContactOperationResult.SAVE_SUCCESS:
				message = "保存名片成功";
				WarningDialog(message);
				break;
			case CommonValue.ContactOperationResult.NOT_AUTHORITY:
				message = "请在手机的[设置]->[应用]->[群友通讯录]->[权限管理]，允许群友通讯录访问你的联系人记录并重新运行程序";
				WarningDialog(message);
				break;
			}
		}
	}
	
	private void registerGetReceiver() {
		mobileReceiver =  new  MobileReceiver();
        IntentFilter postFilter = new IntentFilter();
        postFilter.addAction(CommonValue.ContactOperationResult.ContactBCAction);
        registerReceiver(mobileReceiver, postFilter);
	}
	
	private void unregisterGetReceiver() {
		unregisterReceiver(mobileReceiver);
	}
	
	private void showCreate(String url, int RequestCode) {
		Intent intent = new Intent(this,QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
        startActivityForResult(intent, RequestCode);
	}
	
	private void exchangeCard(final CardIntroEntity model) {
//		loadingPd = UIHelper.showProgress(this, null, null, true);
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.followCard(appContext, model.openid, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				switch (data.getError_code()) {
				case Result.RESULT_OK:
					model.isfriend = CommonValue.PhonebookLimitRight.Friend_Wait;
					exchangeButton.setVisibility(View.GONE);
					exchangeView.setVisibility(View.VISIBLE);
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), data.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			@Override
			public void onFailure(String message) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		position = position - 1;
		if (position>=0 && position <summarys.size()) {
			KeyValue model = summarys.get(position);
			if (!card.openid.equals(appContext.getLoginUid()) && card.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)) {
				showContactDialog(model);
			}
		}
	}
	
	private String[] lianxiren1 = new String[] { "拨打电话", "发送短信"};
	
	private void showContactDialog(final KeyValue model){
		if(!model.key.equals("手机")) {
			return;
		}
		new AlertDialog.Builder(this).setTitle("").setItems(lianxiren1,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0://打电话
					callMobile(model.value);
					break;
				case 1://发短息
					sendSMS(model.value);
					break;
				}
			}
		}).show();
	}
	
	private void callMobile(String moblie) {
		Uri uri = null;
		uri = Uri.parse("tel:" + moblie);
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		startActivity(it);
	}
	
	private void sendSMS(String moblie) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("phoneNumber", moblie);
		BaseIntentUtil.intentSysDefault(CardView.this, MessageBoxList.class, map);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.CardViewUrlRequest.editCard:
			getCard(card.code);
			setResult(RESULT_OK);
			break;
		}
	}
	
	protected void WarningDialog(String message) {
		try {
			if (CardView.this.isFinishing()) {
				return;
			}
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(message);
			builder.setPositiveButton("确定", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		   builder.create().show();
		}
		catch (Exception e) {
			Crashlytics.logException(e);
		}
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
				loadingPd = UIHelper.showProgress(this, null, null, true);
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
