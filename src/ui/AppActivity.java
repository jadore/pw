package ui;

import org.apache.http.client.CookieStore;

import com.loopj.android.http.PersistentCookieStore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import config.AppClient;
import config.MyApplication;
import tools.AppContext;
import tools.AppManager;
import tools.BaseActivity;
import tools.Logger;
import tools.UIHelper;

public class AppActivity extends BaseActivity {
	protected MyApplication appContext;
	protected Context context = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext =  (MyApplication)getApplication();
		context = this;
	}
	
	public void forceLogout() {
		UIHelper.ToastMessage(this, "用户未登录,1秒后重新进入登录界面", Toast.LENGTH_SHORT);
		Handler jumpHandler = new Handler();
        jumpHandler.postDelayed(new Runnable() {
			public void run() {
				AppClient.Logout(appContext);
				CookieStore cookieStore = new PersistentCookieStore(AppActivity.this);  
				cookieStore.clear();
				AppManager.getAppManager().finishAllActivity();
				appContext.setUserLogout();
				Intent intent = new Intent(AppActivity.this, LoginCode1.class);
				startActivity(intent);
			}
		}, 1000);
	}
	
	public void closeInput() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null && this.getCurrentFocus() != null) {
			inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
