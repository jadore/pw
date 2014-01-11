package ui;

import com.crashlytics.android.Crashlytics;

import service.AddMobileService;
import tools.AppManager;

import com.vikaa.mycontact.R;

import config.CommonValue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class Welcome extends AppActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		final View view = View.inflate(this, R.layout.welcome, null);
		setContentView(view);
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		AddMobileService.actionStartPAY(this);
		aa.setAnimationListener(new AnimationListener()
		{
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}
			
		});
	}
	
	private void redirectTo(){     
		if(!appContext.isLogin()){
			if(!showWhatsNewOnFirstLaunch()){
				Intent intent = new Intent(this,LoginCode1.class);
				startActivity(intent);
				AppManager.getAppManager().finishActivity(this);
			}
		}
		else {
			Intent intent = new Intent(this, Index.class);
	        startActivity(intent);
	        AppManager.getAppManager().finishActivity(this);
		}
    }
	
	private boolean showWhatsNewOnFirstLaunch() {
	    try {
		      PackageInfo info = getPackageManager().getPackageInfo(CommonValue.PackageName, 0);
		      int currentVersion = info.versionCode;
		      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		      int lastVersion = prefs.getInt(CommonValue.KEY_GUIDE_SHOWN, 0);
		      if (currentVersion > lastVersion) {
			        
			        Intent intent = new Intent(this, GuidePage.class);
			        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			        startActivity(intent);
			        AppManager.getAppManager().finishActivity(this);
			        return true;
		      	}
	    	} catch (PackageManager.NameNotFoundException e) {
	    		e.printStackTrace();
	    	}
	    return false;
	}
}
