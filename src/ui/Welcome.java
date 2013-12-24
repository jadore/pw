package ui;

import tools.AppManager;

import com.vikaa.mycontact.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class Welcome extends AppActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.welcome, null);
		setContentView(view);
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
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
			Intent intent = new Intent(this,LoginCode1.class);
	        startActivity(intent);
	        AppManager.getAppManager().finishActivity(this);
		}
		else {
			Intent intent = new Intent(this,Index.class);
	        startActivity(intent);
	        AppManager.getAppManager().finishActivity(this);
		}
    }
	
//	private boolean showWhatsNewOnFirstLaunch() {
//	    try {
//		      PackageInfo info = getPackageManager().getPackageInfo(ValueClass.PackageName, 0);
//		      int currentVersion = info.versionCode;
//		      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		      int lastVersion = prefs.getInt(KEY_HELP_VERSION_SHOWN, 0);
//		      if (currentVersion > lastVersion) {
//			        prefs.edit().putInt(KEY_HELP_VERSION_SHOWN, currentVersion).commit();
//			        Intent intent = new Intent(this, GuidingPage.class);
//			        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//			        startActivity(intent);
//			        finish();
//			        return true;
//		      	}
//	    	} catch (PackageManager.NameNotFoundException e) {
//	    		e.printStackTrace();
//	    	}
//	    return false;
//	}
}
