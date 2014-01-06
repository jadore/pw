package ui;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;
import tools.AppManager;
import tools.Logger;
import tools.UIHelper;

import com.loopj.android.http.PersistentCookieStore;
import com.vikaa.mycontact.R;

import config.CommonValue;
import config.QYRestClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebStorage.QuotaUpdater;

public class IndexWeb extends AppActivity {
	private WebView webView;
	private ProgressDialog loadingPd;
	
	 String lastUrl = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index_web);
		initUI();
		initData();
	}
	
	private void initUI() {
		webView = (WebView) findViewById(R.id.webview);
	}
	
	private void initData() {
		pbwc mJS = new pbwc();  
		String url = String.format("%s/index/my", CommonValue.BASE_URL);
		Logger.i(url);
		lastUrl = url;
		
		WebSettings webseting = webView.getSettings();  
		webseting.setJavaScriptEnabled(true);
//		webView.addJavascriptInterface(mJS, "pbwc");
		webseting.setLightTouchEnabled(true);
//	    webseting.setDomStorageEnabled(true);             
//	    webseting.setAppCacheMaxSize(1024*1024*8);//设置缓冲大小，我设的是8M  
//	    String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();      
//        webseting.setAppCachePath(appCacheDir);  
//        webseting.setAllowFileAccess(true);  
//        webseting.setAppCacheEnabled(true); 
//        if (appContext.isNetworkConnected()) {
//        	webseting.setCacheMode(WebSettings.LOAD_DEFAULT); 
//		}
//        else {
//        	webseting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); 
//        }
       
		
		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				
				Logger.i(url);
				Map<String, String> extraHeaders = new HashMap<String, String>();
				extraHeaders.put("Referer", lastUrl);
				Logger.i(lastUrl);
				view.loadUrl(url, extraHeaders);
				lastUrl = url;
				return true;
			}
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}
		});
		webView.setWebChromeClient(new WebChromeClient() {
		    public void onProgressChanged(WebView view, int progress) {
		        setTitle("页面加载中，请稍候..." + progress + "%");
		        setProgress(progress * 100);
		        if (progress == 100) {
		        	UIHelper.dismissProgress(loadingPd);
		        }
		    }
		    
//		    @Override
//		    public void onReachedMaxAppCacheSize(long spaceNeeded,
//		    		long quota, QuotaUpdater quotaUpdater) {
//		    	quotaUpdater.updateQuota(spaceNeeded * 2);  
//		    }
		});
		CookieStore cookieStore = new PersistentCookieStore(this);  
		QYRestClient.getIntance().setCookieStore(cookieStore);
		String cookieString2 = "";
		String cookieString3 = "";
		cookieString2 = String.format("hash=%s;", appContext.getLoginHash());
		cookieString3 = String.format("isapp=%s;", "1");
		Logger.i(cookieString2);
		Logger.i(cookieString3);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		cookieManager.setCookie(url, cookieString2);
		cookieManager.setCookie(url, cookieString3);
//		loadingPd = UIHelper.showProgress(this, null, null, true);
		webView.loadUrl(url);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
			webView.goBack();
			return true;
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
				AppManager.getAppManager().finishActivity(IndexWeb.this);
				break;
			case CommonValue.CreateViewJSType.goPhonebookList:
				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookList);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(IndexWeb.this);
				break;
			case CommonValue.CreateViewJSType.goActivityView:
				code = (String) msg.obj;
				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookView);
				intent.putExtra("resultdata", code);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(IndexWeb.this);
				break;
			case CommonValue.CreateViewJSType.goActivityList:
				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookList);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(IndexWeb.this);
				break;
			case CommonValue.CreateViewJSType.goCardView:
//				code = (String) msg.obj;
//				intent.putExtra("resultcode", CommonValue.CreateViewJSType.goPhonebookView);
//				intent.putExtra("resultdata", code);
				setResult(RESULT_OK, intent);
				AppManager.getAppManager().finishActivity(IndexWeb.this);
				break;
			}
		};
	};
}
