package ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.QYWebView.pbwc;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.vikaa.mycontact.R;

import config.CommonValue;
import config.QYRestClient;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
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

public class Find extends AppActivity {
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	private WebView webView;
	private Button loadAgainButton;
	
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
		webView.destroy();
		super.onDestroy();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.loadAgain:
			loadAgain();
			break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find);
		initUI();
		initWebData();
	}
	
	private void initUI() {
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
		webView = (WebView) findViewById(R.id.webview);
		loadAgainButton = (Button) findViewById(R.id.loadAgain);
	}
	
	private void initWebData() {
		String url = CommonValue.BASE_URL + "/home/app/more" + "?_sign=" + appContext.getLoginSign() ;
		WebSettings webseting = webView.getSettings();  
		webseting.setJavaScriptEnabled(true);
		webseting.setLightTouchEnabled(true);
	    webseting.setDomStorageEnabled(true);             
	    webseting.setAppCacheMaxSize(1024*1024*8);//设置缓冲大小，我设的是8M  
	    String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();      
        webseting.setAppCachePath(appCacheDir);  
        webseting.setAllowFileAccess(true);  
        webseting.setAppCacheEnabled(true); 
//        webView.addJavascriptInterface(mJS, "pbwc");
        
        if (appContext.isNetworkConnected()) {
        	webseting.setCacheMode(WebSettings.LOAD_DEFAULT); 
		}
        else {
        	webseting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); 
        }

		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				showFinder(url);
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
		        if (progress == 100) {
//		        	UIHelper.dismissProgress(loadingPd);
		        	indicatorImageView.clearAnimation();
		        	indicatorImageView.setVisibility(View.INVISIBLE);
		        }
		    }

		    @Override
		    public void onReachedMaxAppCacheSize(long spaceNeeded,
		    		long quota, QuotaUpdater quotaUpdater) {
		    	quotaUpdater.updateQuota(spaceNeeded * 2);  
		    }
		});
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		webView.loadUrl(url);
		if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
	}

	private void loadAgain() {
		loadAgainButton.setVisibility(View.INVISIBLE);
		webView.setVisibility(View.VISIBLE);
		String url = CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign() ;
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		webView.loadUrl(url);
		if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
	}
	
	private void showFinder(String url) {
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
		startActivity(intent);
	}
}
