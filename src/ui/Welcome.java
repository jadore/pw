package ui;

import java.io.File;
import java.io.FileOutputStream;
import bean.CardIntroEntity;

import com.crashlytics.android.Crashlytics;

import service.AddMobileService;
import tools.AppManager;
import com.vikaa.mycontact.R;

import config.CommonValue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
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
		CardIntroEntity card = new CardIntroEntity();
		card.realname = "群友通讯录客服";
		card.phone = "18811168650";
		AddMobileService.actionStartPAY(this, card, false);
		initImagePath();
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
			Intent intent = new Intent(this, Tabbar.class);
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
	
	private void initImagePath() {
		try {
			String cachePath = cn.sharesdk.framework.utils.R.getCachePath(this, null);
			String TEST_IMAGE = cachePath + "logo.png";
			File file = new File(TEST_IMAGE);
			if (!file.exists()) {
				file.createNewFile();
				Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
				FileOutputStream fos = new FileOutputStream(file);
				pic.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
	} 
}
