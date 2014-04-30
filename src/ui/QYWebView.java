package ui;

import im.ui.Chating;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import service.AddMobileService;
import tools.AppContext;
import tools.AppManager;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import tools.UIHelper;
import bean.CardIntroEntity;
import bean.Entity;
import bean.Result;
import bean.UserEntity;
import bean.WebContent;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import config.AppClient.WebCallback;
import config.CommonValue;
import config.QYRestClient;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebStorage.QuotaUpdater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class QYWebView extends AppActivity  {
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	private WebView webView;
	private Button loadAgainButton;
	private ProgressDialog loadingPd;
	private Button rightBarButton;
	private Button closeBarButton;
	private String keyCode;
	private int keyType;
	
	private List<String> urls = new ArrayList<String>();
	
	private ValueCallback<Uri> mUploadMessage;
	private final static int FILECHOOSER_RESULTCODE = 1;
	private final static int CAMERA_RESULTCODE = 2;
	
	private Uri outputFileUri;
	
	String QYurl ;
	WebSettings webseting;
	private TextView newtv;
	
	private MobileReceiver mobileReceiver;
	private IWXAPI api;
	
	private SharedPreferences shared;
	private SharedPreferences.Editor editor;
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  
	    
	}
	
	@Override
	public void onStop() {
	    super.onStop();
	    QYRestClient.getIntance().cancelRequests(this, true);
	    EasyTracker.getInstance(this).activityStop(this);  
	}
	
	@Override
	protected void onDestroy() {
		QYRestClient.getIntance().cancelRequests(this, true);
		webView.destroy();
		unregisterGetReceiver();
		super.onDestroy();
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shared = getSharedPreferences("userInfo", 0); 
		editor = shared.edit();
		setContentView(R.layout.create_view);
		registerGetReceiver();
		api = WXAPIFactory.createWXAPI(this, "wx8b5b960fc0311f3e", false);
		initUI();
		initData();
	}
	
	private void initUI() {
		newtv = (TextView) findViewById(R.id.new_data_toast_message);
		
		indicatorImageView = (ImageView) findViewById(R.id.xindicator);
		indicatorAnimation = AnimationUtils.loadAnimation(context, R.anim.refresh_button_rotation);
		indicatorAnimation.setDuration(500);
		indicatorAnimation.setInterpolator(new Interpolator() {
		    private final int frameCount = 10;
		    @Override
		    public float getInterpolation(float input) {
		        return (float)Math.floor(input*frameCount)/frameCount;
		    }
		});
		rightBarButton = (Button) findViewById(R.id.rightBarButton);
		closeBarButton = (Button) findViewById(R.id.closeBarButton);
		webView = (WebView) findViewById(R.id.webview);
		loadAgainButton = (Button) findViewById(R.id.loadAgain);
	}
	
	private void loadAgain() {
		loadAgainButton.setVisibility(View.INVISIBLE);
		webView.setVisibility(View.VISIBLE);
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
    	loadURLScheme(QYurl);
	}
	
	private void initData() {
		pbwc mJS = new pbwc();  
		QYurl = getIntent().getStringExtra(CommonValue.IndexIntentKeyValue.CreateView);
		webseting = webView.getSettings();  
		webseting.setJavaScriptEnabled(true);
		webseting.setLightTouchEnabled(true);
		webseting.setDomStorageEnabled(true);  
		webseting.setDatabaseEnabled(true);     
        String dbPath =this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();  
        webseting.setDatabasePath(dbPath);         
	    webseting.setAppCacheMaxSize(1024*1024*8); 
	    String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();      
        webseting.setAppCachePath(appCacheDir);  
        webseting.setAllowFileAccess(true);  
        webseting.setAppCacheEnabled(true); 
        webView.addJavascriptInterface(mJS, "pbwc");
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
					String reg = "weixin://contacts/profile/(\\S+)";
					Pattern pattern = Pattern.compile(reg);
					Matcher matcher = pattern.matcher(url);
					if (matcher.find()) {
						try{
							int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                            if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", matcher.group(1));
                                clipboard.setPrimaryClip(clip);
                            } else {
                                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                clipboard.setText(matcher.group(1));
                            }
							WarningDialogAndOpenWechat("微信号已保存到剪切板,需要打开微信吗？");
						}catch (Exception e) {
							Crashlytics.logException(e);
						}
					}
				}
				else {
					indicatorImageView.setVisibility(View.VISIBLE);
			    	indicatorImageView.startAnimation(indicatorAnimation);
			    	if (StringUtils.notEmpty(url) && !QYWebView.this.isFinishing()) {
						loadSecondURLScheme(url);
					}
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
					try {
						view.setVisibility(View.INVISIBLE);
					}catch (Exception e) {
						Crashlytics.logException(e);
					}
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), "网速不给力,请重新加载", Toast.LENGTH_SHORT);
					break;
				}
				loadAgainButton.setVisibility(View.VISIBLE);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
			
		});
		webView.setWebChromeClient(new WebChromeClient() {
		    public void onProgressChanged(WebView view, int progress) {
		    	if (progress >= 50) {
		    		UIHelper.dismissProgress(loadingPd);
		    	}
		        if (progress == 100) {
		        	indicatorImageView.setVisibility(View.INVISIBLE);
		        	indicatorImageView.clearAnimation();
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
		
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
    	urls.add(QYurl);
    	loadURLScheme(QYurl);
	}
	
	private void loadURLScheme(String url) {
		switch (appContext.getNetworkType()) {
		case AppContext.NETTYPE_WIFI:
			if (url.contains("card")) {
				webView.loadUrl(url);
			}
			else {
				String key = String.format("%s-%s", MD5Util.getMD5String(url), appContext.getLoginUid());
				WebContent dc = (WebContent) appContext.readObject(key);
				if(dc == null){
					if (!appContext.isNetworkConnected()) {
		            	webView.loadUrl(url);
		            	UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
					}
					else {
						loadURL(url, true, true);
					}
				}
				else {
					webView.loadDataWithBaseURL(CommonValue.BASE_URL, dc.text, "text/html", "utf-8", url);
					loadURL(url, false, true);
				}
			}
			break;
		case AppContext.NETTYPE_CMNET:
		case AppContext.NETTYPE_CMWAP:
			String key = String.format("%s-%s", MD5Util.getMD5String(url), appContext.getLoginUid());
			WebContent dc = (WebContent) appContext.readObject(key);
			if(dc == null){
				if (!appContext.isNetworkConnected()) {
	            	webView.loadUrl(url);
	            	UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
				}
				else {
					loadURL(url, true, true);
				}
			}
			else {
				webView.loadDataWithBaseURL(CommonValue.BASE_URL, dc.text, "text/html", "utf-8", url);
				loadURL(url, false, true);
			}
			break;
		}
	}
	
	private void loadSecondURLScheme(String url) {
		urls.add(url);
		newtv.setVisibility(View.INVISIBLE);
    	webView.loadUrl(url);
    	if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(context, "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    	}
	}
	
	private void loadURL(final String url, final boolean isLoad, final boolean isPlay) {
		if (this.isFinishing()) {
			return;
		}
		AppClient.loadURL(context, appContext, url, new WebCallback() {
			
			@Override
			public void onFailure(String message) {
				if (isLoad && StringUtils.notEmpty(message) && appContext.isNetworkConnected() && !QYWebView.this.isFinishing()) {
					UIHelper.ToastMessage(getApplicationContext(), "正在努力帮你加载内容，请稍等", Toast.LENGTH_SHORT);
					if (webView != null) {
						webseting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
						webView.loadUrl(message);
					}
					
				}
			}
			
			@Override
			public void onError(Exception e) {
				
			}

			@Override
			public void onSuccess(int type, Entity data, String key) {
				if (QYWebView.this.isFinishing()) {
					return;
				}
				WebContent wc = (WebContent) data;
				
				if (isLoad) {
					webView.loadDataWithBaseURL(CommonValue.BASE_URL, wc.text, "text/html", "utf-8", url);
				}
				switch (type) {
				case 1:
					if (isPlay && !url.contains("card")) {
						newtv.setVisibility(View.VISIBLE);
						newtv.setText("亲，页面有更新，请点击加载");
					}
					break;
				default:
					newtv.setVisibility(View.INVISIBLE);
					break;
				}
			}
		});
	}
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			if (urls.size() > 1) {
				urls.remove(urls.size()-1);
		        String url = urls.get(urls.size()-1);
		        loadingPd = UIHelper.showProgress(QYWebView.this, "", "", true);
		        webView.loadUrl(url);
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
		case R.id.new_data_toast_message:
			newtv.setVisibility(View.INVISIBLE);
			try {
				newtv.setVisibility(View.INVISIBLE);
				loadURLScheme(QYurl);
			} catch (Exception e) {
				Logger.i(e);
				Crashlytics.logException(e);
			}
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
	    public void webNotSign(String c) {
	    	Message msg = new Message();
	    	msg.what = CommonValue.CreateViewJSType.webNotSign;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void showChat(String c) {
	    	Message msg = new Message();
	    	msg.obj = c;
	    	msg.what = CommonValue.CreateViewJSType.showChat;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void phonebookAssistSelect(String c) {
	    	Message msg = new Message();
	    	msg.obj = c;
	    	msg.what = CommonValue.CreateViewJSType.phonebookAssistSelect;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void showJiaV(String c) {
	    	Message msg = new Message();
	    	msg.obj = c;
	    	msg.what = CommonValue.CreateViewJSType.showJiaV;
	    	mJSHandler.sendMessage(msg);
	    }
	    public void setAvatar(String c) {
	    	Message msg = new Message();
	    	msg.obj = c;
	    	msg.what = CommonValue.CreateViewJSType.showUploadAvatar;
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
				break;
			case CommonValue.CreateViewJSType.showActivitySmsButton:
				code = (String) msg.obj;
				keyCode = code;
				keyType = 2;
				rightBarButton.setVisibility(View.VISIBLE);
				break;
			case CommonValue.CreateViewJSType.webNotSign:
				reLogin();
				break;
			case CommonValue.CreateViewJSType.showChat:
				code = (String) msg.obj;
				enterChat(code);
				break;
			case CommonValue.CreateViewJSType.phonebookAssistSelect:
				code = (String) msg.obj;
				enterPhonebook(code);
				break;
			case CommonValue.CreateViewJSType.showJiaV:
				code = (String) msg.obj;
				Logger.i(code);
				enterJiaV(code);
				break;
			case CommonValue.CreateViewJSType.showUploadAvatar:
				code = (String) msg.obj;
				Logger.i(code);
				enterUploadAvatar(code);
				break;
			}
		};
	};
	
	private void enterUploadAvatar(String code) {
		if (this.isFinishing()) {
			return;
		}
		try {
			JSONObject js = new JSONObject(code);
			Intent intent = new Intent(context, UploadAvatar.class);
			intent.putExtra("code", js.getString("code"));
			intent.putExtra("token", js.getString("token"));
			intent.putExtra("avatar", js.getString("avatar"));
			intent.putExtra("sign", js.getString("sign"));
			startActivityForResult(intent, CommonValue.CreateViewJSType.showUploadAvatar);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void enterJiaV(String code) {
		if (this.isFinishing()) {
			return;
		}
		try {
			JSONObject js = new JSONObject(code);
			Intent intent = new Intent(context, JiaV.class);
			intent.putExtra("code", js.getString("code"));
			intent.putExtra("token", js.getString("token"));
			startActivity(intent);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void enterPhonebook(String code) {
		if (this.isFinishing()) {
			return;
		}
		Intent intent = new Intent(context, MobileSelect.class);
		intent.putExtra("code", code);
		startActivityForResult(intent, CommonValue.CreateViewJSType.phonebookAssistSelect);
	}
	
	private void enterChat(String roomId) {
		if (this.isFinishing()) {
			return;
		}
		Intent intent = new Intent(context, Chating.class);
		intent.putExtra("roomId", roomId);
		context.startActivity(intent);
	}
	
	private void reLogin() {
		if (this.isFinishing()) {
			return;
		}
		loadingPd = UIHelper.showProgress(QYWebView.this, null, "用户未登录，正在尝试重连", true);
		AppClient.autoLogin(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				UserEntity user = (UserEntity) data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					if (urls.size() > 1) {
						urls.remove(urls.size()-1);
				        String url = urls.get(urls.size()-1);
				        loadingPd = UIHelper.showProgress(QYWebView.this, null, "正在刷新页面", true);
				       	url = url.contains("?")? url+"&_sign="+appContext.getLoginSign() :  url+"?_sign="+appContext.getLoginSign();
				        webView.loadUrl(url);
					}
					break;
				default:
					forceLogout();
					UIHelper.ToastMessage(getApplicationContext(), user.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
			}
		});
	}
	
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
	
	private void showShare(final boolean silent, final String platform, final String desc, final String title, final String link, String TLImg, String MsgImg) {
		String storageState = Environment.getExternalStorageState();	
		if(storageState.equals(Environment.MEDIA_MOUNTED)){
			String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qy/" + MD5Util.getMD5String(TLImg) + ".png";
			File file = new File(savePath);
			if (file.exists()) {
				okshare(silent, platform, desc, title, link, savePath);
			}
			else {
				loadingPd = UIHelper.showProgress(QYWebView.this, null, null, true);
				AppClient.downFile(this, appContext, TLImg, ".png", new FileCallback() {
					@Override
					public void onSuccess(String filePath) {
						UIHelper.dismissProgress(loadingPd);
						okshare(silent, platform, desc, title, link, filePath);
					}
					
					@Override
					public void onFailure(String message) {
						UIHelper.dismissProgress(loadingPd);
						okshare(silent, platform, desc, title, link, "");
					}
					
					@Override
					public void onError(Exception e) {
						UIHelper.dismissProgress(loadingPd);
						okshare(silent, platform, desc, title, link, "");
					}
				});
			}
		}
	}
	
	private void okshare(boolean silent, String platform, String desc, String title, String link, String filePath) {
		try {
			final OnekeyShare oks = new OnekeyShare();
			oks.setNotification(R.drawable.ic_launcher, getResources().getString(R.string.app_name));
			oks.setTitle(title);
			oks.setText(desc + "\n" + link);
			oks.setUrl(link);
			if (StringUtils.notEmpty(filePath)) {
				oks.setImagePath(filePath);
			}
			else {
				String cachePath = cn.sharesdk.framework.utils.R.getCachePath(this, null);
				oks.setImagePath(cachePath + "logo.png");
			}
			if (StringUtils.notEmpty(link)) {
				oks.setUrl(link);
			}
			oks.setSilent(silent);
			if (platform != null) {
				oks.setPlatform(platform);
			}
			oks.setSiteUrl(link);
			oks.setSite(link);
			oks.setTitleUrl(link);
			oks.setLatitude(23.056081f);
			oks.setLongitude(113.385708f);
			oks.show(context);
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
	
	protected void WarningDialogAndOpenWechat(String message) {
		if (QYWebView.this.isFinishing()) {
			return;
		}
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("打开", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				api.openWXApp();
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
	
	protected void WarningDialog(String message) {
		try {
			if (QYWebView.this.isFinishing()) {
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
	
	public void addContact(CardIntroEntity entity){
		loadingPd = UIHelper.showProgress(QYWebView.this, null, null, true);
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
	
	protected void SMSDialog(final int type) {
		try {
			AlertDialog.Builder builder = new Builder(context);
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
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
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
		{  
			if (null == mUploadMessage) return;  
			outputFileUri = outputFileUri == null || resultCode != RESULT_OK ? null  
					: outputFileUri;  
			mUploadMessage.onReceiveValue(outputFileUri);  
			mUploadMessage = null;  
		} 
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.CreateViewJSType.phonebookAssistSelect:
			String url = intent.getStringExtra("url");
			webView.loadUrl(url);
			loadingPd = UIHelper.showProgress(QYWebView.this, "", "", true);
			break;
		case CommonValue.CreateViewJSType.showUploadAvatar:
			webView.loadUrl(urls.get(urls.size()-1));
			loadingPd = UIHelper.showProgress(QYWebView.this, "", "", true);
			break;
		}
	}

	
}
