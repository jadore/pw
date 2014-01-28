package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sms.MessageBoxList;
import tools.AppException;
import tools.AppManager;
import tools.BaseIntentUtil;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.CardViewAdapter;

import bean.CardIntroEntity;
import bean.ContactBean;
import bean.Entity;
import bean.KeyValue;
import bean.Result;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;

import android.R.string;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CardView extends AppActivity implements OnItemClickListener  {
	private AsyncQueryHandler asyncQuery;
	private CardIntroEntity card;
	private TextView titleBarView;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card_view);
		initUI();
		initData();
	}
	
	private void initUI() {
		titleBarView = (TextView) findViewById(R.id.titleBarView);
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
		asyncQuery = new MyAsyncQueryHandler(this.getContentResolver());
		card = (CardIntroEntity) getIntent().getSerializableExtra(CommonValue.CardViewIntentKeyValue.CardView);
		setData(card);
//		if (card.willRefresh) {
			getCard(card.code);
//		}
	}
	
	private void setData(CardIntroEntity entity) {
		card = entity;
		if (!StringUtils.isEmpty(entity.openid)) {
			if (entity.openid.equals(appContext.getLoginUid())) {
				saveMobileButton.setVisibility(View.GONE);
				editMyMobileButton.setVisibility(View.VISIBLE);
			}
			else {
				saveMobileButton.setVisibility(View.VISIBLE);
				editMyMobileButton.setVisibility(View.GONE);
				Logger.i(entity.isfriend);
				if (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)) {
					callMobileButton.setVisibility(View.VISIBLE);
				}
				else {
					callMobileButton.setVisibility(View.GONE);
				}
			}
		}
		
		if (!StringUtils.isEmpty(entity.isfriend)) {
			if (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_No)) {
				exchangeButton.setVisibility(View.VISIBLE);
			}
			else if (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_Wait)) {
				exchangeButton.setVisibility(View.GONE);
				exchangeView.setVisibility(View.VISIBLE);
			}
		}
		titleBarView.setText(entity.realname);
		imageLoader.displayImage(entity.headimgurl, avatarImageView, CommonValue.DisplayOptions.avatar_options);
		nameView.setText(entity.realname);
		titleView.setText(entity.department +" " +entity.position);
		summarys.clear();
		if (!StringUtils.isEmpty(entity.wechat)) {
			KeyValue value = new KeyValue();
			value.key = "微信号";
			value.value = entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)?entity.wechat : "*******(交换名片可见)";
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.email)) {
			KeyValue value = new KeyValue();
			value.key = "邮箱";
			value.value = entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)?entity.email : "*******(交换名片可见)";
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.phone)) {
			KeyValue value = new KeyValue();
			value.key = "手机";
			value.value = entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)?entity.phone : "*******(交换名片可见)";
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.birthday)) {
			KeyValue value = new KeyValue();
			value.key = "生日";
			value.value = entity.birthday;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.address)) {
			KeyValue value = new KeyValue();
			value.key = "地址";
			value.value = entity.address;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.intro)) {
			KeyValue value = new KeyValue();
			value.key = "个人介绍";
			value.value = entity.intro;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.supply)) {
			KeyValue value = new KeyValue();
			value.key = "供需关系";
			value.value = entity.supply;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.needs)) {
			KeyValue value = new KeyValue();
			value.key = "需求关系";
			value.value = entity.needs;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.hometown)) {
			KeyValue value = new KeyValue();
			value.key = "籍贯";
			value.value = entity.hometown;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.interest)) {
			KeyValue value = new KeyValue();
			value.key = "兴趣";
			value.value = entity.interest;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.school)) {
			KeyValue value = new KeyValue();
			value.key = "学校";
			value.value = entity.school;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.homepage)) {
			KeyValue value = new KeyValue();
			value.key = "个人主页";
			value.value = entity.homepage;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.company_site)) {
			KeyValue value = new KeyValue();
			value.key = "公司网站";
			value.value = entity.company_site;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.qq)) {
			KeyValue value = new KeyValue();
			value.key = "QQ";
			value.value = entity.qq;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.weibo)) {
			KeyValue value = new KeyValue();
			value.key = "新浪微博";
			value.value = entity.weibo;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.tencent)) {
			KeyValue value = new KeyValue();
			value.key = "腾讯微博";
			value.value = entity.tencent;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.renren)) {
			KeyValue value = new KeyValue();
			value.key = "人人";
			value.value = entity.renren;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.zhihu)) {
			KeyValue value = new KeyValue();
			value.key = "知乎";
			value.value = entity.zhihu;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.qzone)) {
			KeyValue value = new KeyValue();
			value.key = "QQ空间";
			value.value = entity.qzone;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.facebook)) {
			KeyValue value = new KeyValue();
			value.key = "FACEBOOK";
			value.value = entity.facebook;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.twitter)) {
			KeyValue value = new KeyValue();
			value.key = "Twitter";
			value.value = entity.twitter;
			summarys.add(value);
		}
		if (!StringUtils.isEmpty(entity.intentionen)) {
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
			showShare(false, Wechat.NAME);
			break;
		case R.id.shareTimelineButton:
			showShare(false, WechatMoments.NAME);
			break;
		case R.id.saveContactButton:
			addContact();
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
		}
	}
	
	private void getCard(String code) {
		if (!appContext.isNetworkConnected()) {
			UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
			return;
		}
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.getCard(appContext, code, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
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
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle("群友通讯录");
			oks.setText(String.format("您好，我叫%s，这是我的名片，请多多指教,%s", card.realname, card.link));
			oks.setImagePath("file:///android_asset/ic_launcher.png");
			oks.setUrl(card.link);
			oks.setSilent(silent);
			if (platform != null) {
				oks.setPlatform(platform);
			}
			oks.show(this);
		} catch (Exception e) {
			((AppException)e).makeToast(getApplicationContext());
		}
	}
	
	public void addContact(){
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				"sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
		}; 
		loadingPd = UIHelper.showProgress(this, null, null, true);
		asyncQuery.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
    }
	
	
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			UIHelper.dismissProgress(loadingPd);
			try {
				if (isPhoneExit(cursor)) {
					UIHelper.ToastMessage(getApplicationContext(), "名片已存在", Toast.LENGTH_SHORT);
				}
				else {//insert
					insert();
					UIHelper.ToastMessage(getApplicationContext(), "名片保存成功", Toast.LENGTH_SHORT);
				}
			} catch (Exception e) {
				((AppException)e).makeToast(getApplicationContext());
			}
			
		}
	}
	
	private boolean isPhoneExit(Cursor cursor) {
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String number = cursor.getString(2);
				number = number.replace("-", "");
				number = number.replace("+86", "");
				if (number.indexOf(card.phone) != -1) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void insert() {
		ContentValues values = new ContentValues();
        //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
        Uri rawContactUri = this.getContentResolver().insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        values.put(StructuredName.GIVEN_NAME, card.realname);
        this.getContentResolver().insert(
                android.provider.ContactsContract.Data.CONTENT_URI, values);
        
        values.clear();
        values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        values.put(Phone.NUMBER, card.phone);
        values.put(Phone.TYPE, Phone.TYPE_MOBILE);
        this.getContentResolver().insert(
                android.provider.ContactsContract.Data.CONTENT_URI, values);

        if (!StringUtils.isEmpty(card.email)) {
            values.clear();
            values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
            values.put(Email.DATA, card.email);
            values.put(Email.TYPE, Email.TYPE_WORK);
            this.getContentResolver().insert(
                    android.provider.ContactsContract.Data.CONTENT_URI, values);
		}
        
        if (!StringUtils.isEmpty(card.department)) {
            values.clear();
            values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
            values.put(Organization.COMPANY, card.department);
            values.put(Organization.TITLE, card.position);  
            values.put(Organization.TYPE, Organization.TYPE_WORK);  
            this.getContentResolver().insert(
                    android.provider.ContactsContract.Data.CONTENT_URI, values);
		}
	}

	
	private void showCreate(String url, int RequestCode) {
		Intent intent = new Intent(this,QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
        startActivityForResult(intent, RequestCode);
	}
	
	private void exchangeCard(final CardIntroEntity model) {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.followCard(appContext, model.openid, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
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
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
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
}
