package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import ui.adapter.IndexPagerAdapter;
import com.vikaa.mycontact.R;

import config.CommonValue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

public class GuidePage extends AppActivity{
	private ViewPager mPager;
	private List<View> mListViews;// Tab页面
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide);
		initUI();
	}
	
	private void initUI() {
		mPager = (ViewPager) findViewById(R.id.viewPager);
		mListViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View lay0 = inflater.inflate(R.layout.guide01, null);
		View lay1 = inflater.inflate(R.layout.guide02, null);
		View lay2 = inflater.inflate(R.layout.guide03, null);
		View lay3 = inflater.inflate(R.layout.guide04, null);
		View lay4 = inflater.inflate(R.layout.guide05, null);
		mListViews.add(lay0);
		mListViews.add(lay1);
		mListViews.add(lay2);
		mListViews.add(lay3);
		mListViews.add(lay4);
		mPager.setAdapter(new IndexPagerAdapter(mListViews));
		mPager.setCurrentItem(0);
		
	}
	
	public void ButtonClick(View v) {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(CommonValue.PackageName, 0);
			int currentVersion = info.versionCode;
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			prefs.edit().putInt(CommonValue.KEY_GUIDE_SHOWN, currentVersion).commit();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(this,LoginCode1.class);
		startActivity(intent);
		AppManager.getAppManager().finishActivity(this);
	}
}
