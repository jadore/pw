package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.picasso.Picasso;
import com.vikaa.mycontact.R;

import config.CommonValue;
import contact.DateBean;
import contact.EmailBean;
import contact.MobileSynBean;
import contact.PhoneBean;
import tools.AppManager;
import tools.CircleTransform;
import tools.Logger;
import tools.StringUtils;
import ui.adapter.CardViewAdapter;
import bean.CardIntroEntity;
import bean.KeyValue;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MobileVIew extends AppActivity{
	private MobileSynBean person;
	private CardIntroEntity card;
	private ImageView avatarImageView;
	private TextView titleView;
	private TextView nameView;
	private List<KeyValue> summarys = new ArrayList<KeyValue>();
	private ListView mListView;
	private CardViewAdapter mCardViewAdapter;
	
//	private Button callMobileButton;
//	private Button saveMobileButton;
//	private Button editMyMobileButton;
//	private Button exchangeButton;
//	private TextView exchangeView;
	
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.shareFriendButton:
//			cardSharePre(false, null, card);
			break;
		case R.id.lookupContactButton:
			showMobileView(card);
			break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card_view);
		initUI();
		initData();
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
//		callMobileButton = (Button) footer.findViewById(R.id.callContactButton);
//		saveMobileButton = (Button) footer.findViewById(R.id.saveContactButton);
//		editMyMobileButton = (Button) footer.findViewById(R.id.editMyMobile);
//		exchangeButton = (Button) footer.findViewById(R.id.exchangeMobile); 
//		exchangeView = (TextView) footer.findViewById(R.id.exchangeView);
		mListView = (ListView) findViewById(R.id.listView);
		mListView.addHeaderView(header, null, false);
		mListView.addFooterView(footer, null, false);
		mListView.setDividerHeight(0);
		mCardViewAdapter = new CardViewAdapter(this, summarys);
		mListView.setAdapter(mCardViewAdapter);
	}
	
	private void initData() {
		person = new MobileSynBean();
		card = (CardIntroEntity) getIntent().getSerializableExtra(CommonValue.CardViewIntentKeyValue.CardView);
		Picasso.with(context)
        .load(card.avatar)
        .placeholder(R.drawable.avatar_placeholder)
        .error(R.drawable.avatar_placeholder)
        .resize(100, 100)
        .centerCrop()
        .transform(new CircleTransform())
        .into(avatarImageView);
		nameView.setText(card.realname);
		titleView.setText("");
		summarys.clear();
		if (StringUtils.notEmpty(card.phone)) {
			KeyValue value = new KeyValue();
			value.key = "手机";
			value.value = card.phone ;
			summarys.add(value);
		}
		mCardViewAdapter.notifyDataSetChanged();
//		MyAsyncQueryHandler asyncQuery = new MyAsyncQueryHandler(getContentResolver());
//	    Uri	uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
//	    asyncQuery.startQuery(0, null, uri, null, "contact_id=?", new String[]{card.code}, null);
		handleCursor();
	}
	
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//			handleCursor(cursor);
		}
	}
	
	private void handleCursor() {
		final Handler handler1 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (!MobileVIew.this.isFinishing()) {
					indicatorImageView.setVisibility(View.INVISIBLE);
			    	indicatorImageView.clearAnimation();
			    	handleMobile();
				}
			}
		};
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Cursor cursor = getContentResolver().query(Data.CONTENT_URI,null, "contact_id=?", new String[]{card.code}, null);
				while (cursor.moveToNext()) {
					String mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
					if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
					    PhoneBean phoneBean = new PhoneBean();
				     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
				     	phoneBean.label = "手机";
				     	person.phone.add(phoneBean);
				   }
				   if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
					   EmailBean emailBean = new EmailBean();
					   emailBean.email = cursor.getString(cursor.getColumnIndex(Email.DATA));
					   emailBean.label = "邮箱";
					   person.email.add(emailBean);
				   }
				   if (Event.CONTENT_ITEM_TYPE.equals(mimetype)) {
					   int eventType = cursor.getInt(cursor.getColumnIndex(Event.TYPE));
					   switch (eventType) {
					   case Event.TYPE_CUSTOM:
						   DateBean date = new DateBean();
						   date.date = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
						   date.label = cursor.getString(cursor.getColumnIndex(Event.LABEL));
						   person.dates.add(date);
						   break;
					   case Event.TYPE_ANNIVERSARY:
						   DateBean date1 = new DateBean();
						   date1.date = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
						   date1.label = cursor.getString(cursor.getColumnIndex(Event.LABEL));
						   person.dates.add(date1);
						   break;
					   case Event.TYPE_OTHER:
						   DateBean date2 = new DateBean();
						   date2.date = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
						   date2.label = "其他";
						   person.dates.add(date2);
						   break;
					   case Event.TYPE_BIRTHDAY:
						   String birthday = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
						   person.birthday = birthday==null?"":birthday;
						   break;
					   }
				   }
				   if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) {
					   String organization = cursor.getString(cursor.getColumnIndex(Organization.COMPANY));
					   String jobtitle = cursor.getString(cursor.getColumnIndex(Organization.TITLE));
					   String department = cursor.getString(cursor.getColumnIndex(Organization.DEPARTMENT));
					   person.organization = organization==null?"":organization;
					   person.jobtitle = jobtitle==null?"":jobtitle;
					   person.department = department==null?"":department;
				   }
				}
				handler1.sendEmptyMessage(1);
			}
		});
	}
	
	private void handleMobile() {
		String organization = person.organization ==null?"":person.organization;
		String jobtitle = person.jobtitle ==null?"":person.jobtitle;
		String department = person.department ==null?"":person.department ;
		titleView.setText(organization+""+department+""+jobtitle);
		summarys.clear();
		for (PhoneBean phoneBean : person.phone) {
			KeyValue value = new KeyValue();
			value.key = "手机";
			value.value = phoneBean.phone ;
			summarys.add(value);
		}
		for ( EmailBean emailBean : person.email) {
			KeyValue value = new KeyValue();
			value.key = "邮箱";
			value.value = emailBean.email ;
			summarys.add(value);
		}
		for (  DateBean date : person.dates) {
			KeyValue value = new KeyValue();
			value.key = date.label;
			value.value = date.date ;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(person.birthday)) {
			KeyValue value = new KeyValue();
			value.key = "生日";
			value.value = person.birthday;
			summarys.add(value);
		}
		mCardViewAdapter.notifyDataSetChanged();
	}
	
	public void showMobileView(CardIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看手机名片",   // Event label
	                   null)            // Event value
	      .build()
		);
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		Uri personUri = ContentUris.withAppendedId(uri, Integer.valueOf(entity.code));
		Intent intent2 = new Intent();
		intent2.setAction(Intent.ACTION_VIEW);
		intent2.setData(personUri);
		context.startActivity(intent2);
	}
	
}
