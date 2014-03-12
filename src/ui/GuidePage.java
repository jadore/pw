package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.ImageUtils;
import ui.adapter.IndexPagerAdapter;
import com.vikaa.mycontact.R;

import config.CommonValue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

public class GuidePage extends AppActivity implements OnPageChangeListener{
	private ViewPager mPager;
	private List<View> mListViews;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide);
		initUI();
	}
	
	private void initUI() {
		int w = ImageUtils.getDisplayWidth(this);
		int h = w*960/640;
		mPager = (ViewPager) findViewById(R.id.viewPager);
		mListViews = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View lay0 = inflater.inflate(R.layout.guide01, null);
		ImageView image1 = (ImageView) lay0.findViewById(R.id.image);
		LayoutParams p1 = (LayoutParams) image1.getLayoutParams();
		p1.width = w;
		p1.height = h;
		image1.setLayoutParams(p1);
		
		View lay1 = inflater.inflate(R.layout.guide02, null);
		ImageView image2 = (ImageView) lay1.findViewById(R.id.image);
		LayoutParams p2 = (LayoutParams) image2.getLayoutParams();
		p2.width = w;
		p2.height = h;
		image2.setLayoutParams(p2);
		
		View lay2 = inflater.inflate(R.layout.guide03, null);
		ImageView image3 = (ImageView) lay2.findViewById(R.id.image);
		LayoutParams p3 = (LayoutParams) image3.getLayoutParams();
		p3.width = w;
		p3.height = h;
		image3.setLayoutParams(p3);
		
		View lay3 = inflater.inflate(R.layout.guide04, null);
		ImageView image4 = (ImageView) lay3.findViewById(R.id.image);
		LayoutParams p4 = (LayoutParams) image4.getLayoutParams();
		p4.width = w;
		p4.height = h;
		image4.setLayoutParams(p4);
		
		View lay4 = inflater.inflate(R.layout.guide05, null);
		ImageView image5 = (ImageView) lay4.findViewById(R.id.image);
		LayoutParams p5 = (LayoutParams) image5.getLayoutParams();
		p5.width = w;
		p5.height = h;
		image5.setLayoutParams(p5);
		
		mListViews.add(lay0);
		mListViews.add(lay1);
		mListViews.add(lay2);
		mListViews.add(lay3);
		mListViews.add(lay4);
		mPager.setAdapter(new IndexPagerAdapter(mListViews));
		mPager.setOnPageChangeListener(this);
		mPager.setCurrentItem(0);
	}
	
	public void ButtonClick(View v) {
		jump();
	}
	
	public void jump() {
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


	@Override
	public void onPageScrollStateChanged(int state) {
	}

	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
	}
	
	@Override
	public void onPageSelected(int page) {
		if (page == 4) {
			Handler jumpHandler = new Handler();
	        jumpHandler.postDelayed(new Runnable() {
				public void run() {
					jump();
				}
			}, 1000);
		}
	}
}
