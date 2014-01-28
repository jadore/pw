package ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.client.CookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

import bean.CardIntroEntity;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.PersistentCookieStore;
import com.vikaa.mycontact.R;

import config.CommonValue;
import config.QYRestClient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebStorage.QuotaUpdater;
import android.widget.Button;
import android.widget.Toast;

public class QYWebView extends AppActivity {
	private WebView webView;
	private Button loadAgainButton;
	private ProgressDialog loadingPd;
	private Button rightBarButton;
	private Button closeBarButton;
	private MyAsyncQueryHandler asyncQuery;
	private String keyCode;
	private int keyType;
	
	private ValueCallback<Uri> mUploadMessage;
	private final static int FILECHOOSER_RESULTCODE = 1;
	private final static int CAMERA_RESULTCODE = 2;
	
	private Uri outputFileUri;
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}
	
	@Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_view);
		initUI();
		initData();
	}
	
	private void initUI() {
		rightBarButton = (Button) findViewById(R.id.rightBarButton);
		closeBarButton = (Button) findViewById(R.id.closeBarButton);
		webView = (WebView) findViewById(R.id.webview);
		loadAgainButton = (Button) findViewById(R.id.loadAgain);
	}
	
	private void loadAgain() {
		loadAgainButton.setVisibility(View.INVISIBLE);
		webView.setVisibility(View.VISIBLE);
		String url = CommonValue.BASE_URL + "/home/app";
		loadingPd = UIHelper.showProgress(this, null, null, true);
		webView.loadUrl(url);
		if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
	}
	
	private void initData() {
		asyncQuery = new MyAsyncQueryHandler(this.getContentResolver());
		pbwc mJS = new pbwc();  
		String url = getIntent().getStringExtra(CommonValue.IndexIntentKeyValue.CreateView);
		WebSettings webseting = webView.getSettings();  
		webseting.setJavaScriptEnabled(true);
		webseting.setLightTouchEnabled(true);
	    webseting.setDomStorageEnabled(true);             
	    webseting.setAppCacheMaxSize(1024*1024*8);//设置缓冲大小，我设的是8M  
	    String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();      
        webseting.setAppCachePath(appCacheDir);  
        webseting.setAllowFileAccess(true);  
        webseting.setAppCacheEnabled(true); 
        webView.addJavascriptInterface(mJS, "pbwc");
        
        if (appContext.isNetworkConnected()) {
        	webseting.setCacheMode(WebSettings.LOAD_DEFAULT); 
		}
        else {
        	webseting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); 
        }
		
		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				rightBarButton.setVisibility(View.GONE);
				if (!appContext.isNetworkConnected()) {
		    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
		    		return true;
		    	}
				else if (url.startsWith("tel:")) { 
					Logger.i(url);
					Intent intent;
					try {
						intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					} catch (Exception e) {
						intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
					}
	                startActivity(intent); 
	            }
				else if (url.startsWith("mailto:")) {
					try {
						Logger.i(url);
		                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url)); 
		                startActivity(intent); 
					}catch (Exception e) {
						
					}
				}
				else if (url.startsWith("sms:")) {
					try {
						Logger.i(url);
		                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url)); 
		                startActivity(intent); 
					}catch (Exception e) {
						
					}
				}
				else if (url.startsWith("weixin:")) {
					UIHelper.ToastMessage(QYWebView.this, "请运行微信查找微信号【bibi100】欢迎咨询", Toast.LENGTH_SHORT);
				}
				else {
					CookieManager cookieManager = CookieManager.getInstance();
					cookieManager.setAcceptCookie(true);
					cookieManager.removeSessionCookie();
					CookieStore cookieStore = new PersistentCookieStore(QYWebView.this);  
					for (org.apache.http.cookie.Cookie cookie : cookieStore.getCookies()) {
						String cookieString = cookie.getName() +"="+cookie.getValue()+"; domain="+cookie.getDomain(); 
						Logger.i(cookieString);
					    cookieManager.setCookie(url, cookieString); 
					    CookieSyncManager.getInstance().sync(); 
					}
					QYWebView.this.loadingPd = UIHelper.showProgress(QYWebView.this, null, null, true);
					view.loadUrl(url);
				}
				return true;
			}
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Logger.i(errorCode+"");
				switch (errorCode) {
				case -2:
					webView.setVisibility(View.INVISIBLE);
					break;
				}
				loadAgainButton.setVisibility(View.VISIBLE);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});
		webView.setWebChromeClient(new WebChromeClient() {
		    public void onProgressChanged(WebView view, int progress) {
		        setTitle("页面加载中，请稍候..." + progress + "%");
		        setProgress(progress * 100);
		        
		        if (progress >= 50) {
		        	UIHelper.dismissProgress(loadingPd);
		        }
		    }
		    
		    @Override
		    public void onReachedMaxAppCacheSize(long spaceNeeded,
		    		long quota, QuotaUpdater quotaUpdater) {
		    	quotaUpdater.updateQuota(spaceNeeded * 2);  
		    }
		    
		    public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType ) {  
		    	mUploadMessage = uploadMsg;  
		    	QYWebView.this.openImageIntent();
		    }

	    	// For Android < 3.0
	    	public void openFileChooser( ValueCallback<Uri> uploadMsg ) {
	    		openFileChooser( uploadMsg, "" );
	    	}

	    	// For Android > 4.1
	    	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
	    		openFileChooser( uploadMsg, "" );
	    	}
		});
//		CookieStore cookieStore = new PersistentCookieStore(this);  
//		QYRestClient.getIntance().setCookieStore(cookieStore);
//		String cookieString2 = "";
//		String cookieString3 = "";
//		cookieString2 = String.format("hash=%s;", appContext.getLoginHash());
//		cookieString3 = String.format("isapp=%s;", "1");
//		Logger.i(cookieString2);
//		Logger.i(cookieString3);
//		CookieManager cookieManager = CookieManager.getInstance();
//		cookieManager.removeAllCookie();
//		cookieManager.setCookie(url, cookieString2);
//		cookieManager.setCookie(url, cookieString3);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();
		CookieStore cookieStore = new PersistentCookieStore(this);  
		for (org.apache.http.cookie.Cookie cookie : cookieStore.getCookies()) {
			String cookieString = cookie.getName() +"="+cookie.getValue()+"; domain="+cookie.getDomain(); 
			Logger.i(cookieString);
		    cookieManager.setCookie(url, cookieString); 
		    CookieSyncManager.getInstance().sync(); 
		}
		loadingPd = UIHelper.showProgress(this, null, null, true);
		webView.loadUrl(url);
		if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			if (webView.canGoBack()) {
				webView.goBack();
			}
			else {
				AppManager.getAppManager().finishActivity(this);
				overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
			}
			break;
		case R.id.rightBarButton:
			SMSDialog(keyType);
			break;
		case R.id.loadAgain:
			loadAgain();
			break;
		case R.id.closeBarButton:
			AppManager.getAppManager().finishActivity(this);
			overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (webView.canGoBack()) {
				webView.goBack();// 返回前一个页面
				return true;
			}
			else {
				AppManager.getAppManager().finishActivity(this);
				overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
			}
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public class pbwc {
	    public void goPhonebookView(String code) {
	    	Logger.i(code+"");
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.goPhonebookView;
	    	msg.obj = code;
	    	mJSHandler.sendMessage(msg);
	    }
	    
	    public void goPhonebookList(String c){
	    	Logger.i("aaa");
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.goPhonebookList;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void goActivityView(String code) {
	    	Logger.i(code+"");
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.goActivityView;
	    	msg.obj = code;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void goActivityList(String c){
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.goActivityList;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void goCardList(String code) {
	    	Logger.i(code+"");
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.goCardView;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void share(String code) {
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.share;
	    	msg.obj = code;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void savePhoneBook(String code) {
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.savePhoneBook;
	    	msg.obj = code;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void phonebookShowSmsBtn(String code) {
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.showPhonebookSmsButton;
	    	msg.obj = code;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void activityShowSmsBtn(String code) {
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.showActivitySmsButton;
	    	msg.obj = code;
	    	mJSHandler.sendMessage(msg);
	    }
    }
	
	Handler mJSHandler = new Handler(){
		public void handleMessage(Message msg) {
			Intent intent = new Intent();
			String code ;
			switch (msg.what) {
			case CommonValue.CreateViewJSType.goPhonebookView:
				code = (String) msg.obj;
				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookView);
				intent.putExtra("resultdata", code);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(QYWebView.this);
				break;
			case CommonValue.CreateViewJSType.goPhonebookList:
				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookList);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(QYWebView.this);
				break;
			case CommonValue.CreateViewJSType.goActivityView:
				code = (String) msg.obj;
				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookView);
				intent.putExtra("resultdata", code);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(QYWebView.this);
				break;
			case CommonValue.CreateViewJSType.goActivityList:
				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookList);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(QYWebView.this);
				break;
			case CommonValue.CreateViewJSType.goCardView:
//				code = (String) msg.obj;
//				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookView);
//				intent.putExtra("resultdata", code);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(QYWebView.this);
				break;
			case CommonValue.CreateViewJSType.share:
				code = (String) msg.obj;
				Logger.i(code);
				parseShare(code);
				break;
			case CommonValue.CreateViewJSType.savePhoneBook:
				code = (String) msg.obj;
				Logger.i(code);
				parsePhonebook(code);
				break;
			case CommonValue.CreateViewJSType.showPhonebookSmsButton:
				code = (String) msg.obj;
				keyCode = code;
				keyType = 1;
				rightBarButton.setVisibility(View.VISIBLE);
				Logger.i(code);
				break;
			case CommonValue.CreateViewJSType.showActivitySmsButton:
				code = (String) msg.obj;
				keyCode = code;
				keyType = 2;
				rightBarButton.setVisibility(View.VISIBLE);
				Logger.i(code);
				break;
			}
		};
	};
	
	private void parseShare(String res) {
		String MsgImg = "";
		String TLImg = "";
		String link = "";
		String title = "";
		String desc = "";
		try {
			JSONObject js = new JSONObject(res);
			if (!js.isNull("MsgImg")) {
				MsgImg = js.getString("MsgImg");
			}
			if (!js.isNull("TLImg")) {
				TLImg = js.getString("TLImg");
			}
			if (!js.isNull("link")) {
				link = js.getString("link");
			}
			if (!js.isNull("title")) {
				title = js.getString("title");
			}
			if (!js.isNull("desc")) {
				desc = js.getString("desc");
			}
			showShare(false, null, desc, title, link, TLImg, MsgImg);	
		} catch (JSONException e) {
			Logger.i(e);
		}
	}
	
	private void showShare(boolean silent, String platform, String desc, String title, String link, String TLImg, String MsgImg) {
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle("群友通讯录");
			oks.setText(String.format("%s。%s", desc, link));
			oks.setImageUrl(MsgImg);
			oks.setUrl(link);
			oks.setSilent(silent);
			if (platform != null) {
				oks.setPlatform(platform);
			}
			oks.show(this);
		} catch (Exception e) {
			Logger.i(e);
		}
	}
	
	private void parsePhonebook(String res) {
		try {
			JSONObject js = new JSONObject(res);
			CardIntroEntity entity = new CardIntroEntity();
			entity. headimgurl = js.getString("avatar");
			entity. realname = js.getString("realname");
			entity. phone = js.getString("phone");
			entity. email = js.getString("email");
			entity. department = js.getString("department");
			entity. position = js.getString("position");
			entity. address = js.getString("address");
			addContact(entity);
		} catch (JSONException e) {
			Logger.i(e);
		}
	}
	
	public void addContact(CardIntroEntity entity){
		asyncQuery.setCard(entity);
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
	
	class MyAsyncQueryHandler extends AsyncQueryHandler {
		
		private CardIntroEntity card;
		
		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			UIHelper.dismissProgress(loadingPd);
			try {
				if (isPhoneExit(cursor, card)) {
					UIHelper.ToastMessage(getApplicationContext(), "名片已存在", Toast.LENGTH_SHORT);
				}
				else {//insert
					insert(card);
					UIHelper.ToastMessage(getApplicationContext(), "名片保存成功", Toast.LENGTH_SHORT);
				}
			} catch (Exception e) {
				Logger.i(e);
			}
			
		}

		public CardIntroEntity getCard() {
			return card;
		}

		public void setCard(CardIntroEntity card) {
			this.card = card;
		}
	}
	
	private boolean isPhoneExit(Cursor cursor, CardIntroEntity card) {
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
	
	private void insert(CardIntroEntity card) {
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
        
            values.clear();
            values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
            if (!StringUtils.isEmpty( card.department)) {
            	values.put(Organization.COMPANY, card.department);
			}
            else {
            	values.put(Organization.COMPANY, card.position);
            }
            values.put(Organization.TITLE, card.position);  
            values.put(Organization.TYPE, Organization.TYPE_WORK);  
            this.getContentResolver().insert(
                    android.provider.ContactsContract.Data.CONTENT_URI, values);

            values.clear();
            values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
            values.put(Contacts.ContactMethods.KIND, Contacts.KIND_POSTAL);
            values.put(Contacts.ContactMethods.TYPE, Contacts.ContactMethods.TYPE_WORK);
            values.put(Contacts.ContactMethods.DATA, card.address);
            this.getContentResolver().insert(
                    android.provider.ContactsContract.Data.CONTENT_URI, values);
	}
	
	protected void SMSDialog(final int type) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("允许群友通讯录发送短信?\n建议一次发送不超过50条短信");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showSMS(type);
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
	
	private void showSMS(int type) {
		Intent intent = new Intent(this,PhonebookSMS.class);
		intent.putExtra(CommonValue.PhonebookViewIntentKeyValue.SMS, keyCode);
		intent.putExtra("type", type);
        startActivityForResult(intent, CommonValue.PhonebookViewIntentKeyValue.SMSRequest);
	}
	
	private void openImageIntent() {

		final File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); //file storage
		    root.mkdirs();
		    int nCnt = 1;
		    if ( root.listFiles() != null )
		        nCnt = root.listFiles().length;
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		    final String fname =  String.format("/dest-%s-%d.jpg", sdf.format(Calendar.getInstance().getTime()), nCnt);

		    final File sdImageMainDirectory = new File(root.getAbsolutePath() + fname);
		    outputFileUri = Uri.fromFile(sdImageMainDirectory);
		//selection Photo/Gallery dialog
		    AlertDialog.Builder alert = new AlertDialog.Builder(this);

		    alert.setTitle("请选择");

		    final CharSequence[] items = {"拍照", "本地相册"};
		    alert.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {

		            dialog.dismiss();
		            if( whichButton == 0)
		            {
		                Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		                chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		                startActivityForResult(chooserIntent, CAMERA_RESULTCODE);
		            }
		            if( whichButton == 1)
		            {
		                Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
		                chooserIntent.addCategory(Intent.CATEGORY_OPENABLE); 
		                chooserIntent.setType("image/*");
		                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
		            }
		      }
		    });
		    alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
		        @Override
		        public void onCancel(DialogInterface dialog) {

		        //here we have to handle BACK button/cancel 
		            if ( mUploadMessage!= null ){
		                mUploadMessage.onReceiveValue(null);
		            }
		            mUploadMessage = null;
		            dialog.dismiss();
		        }
		    });
		    alert.create().show();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{  
		if(requestCode==FILECHOOSER_RESULTCODE)  
		{  Logger.i("ee");
			if (null == mUploadMessage) return;  
			Uri result = intent == null || resultCode != RESULT_OK ? null  
					: intent.getData();  
			mUploadMessage.onReceiveValue(result);  
			mUploadMessage = null;  

		}  
		if(requestCode==CAMERA_RESULTCODE)  
		{  Logger.i("ee");
			if (null == mUploadMessage) return;  
			outputFileUri = outputFileUri == null || resultCode != RESULT_OK ? null  
					: outputFileUri;  
			mUploadMessage.onReceiveValue(outputFileUri);  
			mUploadMessage = null;  

		} 
	} 
}
