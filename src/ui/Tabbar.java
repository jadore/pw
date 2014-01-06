package ui;


import baidupush.Utils;
import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.MyApplication;

import tools.AppContext;
import tools.AppException;
import tools.AppManager;
import tools.UIHelper;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;


public class Tabbar extends TabActivity implements OnCheckedChangeListener{
	private MyApplication appContext;
	private RadioGroup mainTab;
	public static TabHost mTabHost;
	
	//内容Intent
	private Intent homeIntent;
	private Intent nearmeIntent;
	private Intent meIntent;
	private Intent moreIntent;
	
	private final static String TAB_TAG_HOME = "tab_tag_home";
	private final static String TAB_TAG_NEARME = "tab_tag_nearme";
	private final static String TAB_TAG_ME = "tab_tag_me";
	private final static String TAB_TAG_MORE = "tab_tag_more";
	
	private ProgressDialog loadingPd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbar);
        AppManager.getAppManager().addActivity(this);
        mainTab=(RadioGroup)findViewById(R.id.main_tab);
        mainTab.setOnCheckedChangeListener(this);
        prepareIntent();
        setupIntent();
        RadioButton homebutton = (RadioButton)findViewById(R.id.radio_button1);
        homebutton.setChecked(true);
        appContext = (MyApplication) getApplication();
        if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
        checkLogin();
	}
	
	private void prepareIntent() {
		homeIntent = new Intent(this, MessageView.class);
		nearmeIntent = new Intent(this, QunZi.class);
		meIntent = new Intent(this, MessageView.class);
		moreIntent = new Intent(this, MessageView.class);
	}
	
	private void setupIntent() {
		mTabHost = getTabHost();
		TabHost localTabHost = mTabHost;
		localTabHost.addTab(buildTabSpec(TAB_TAG_HOME, R.string.main_home, R.drawable.btn_phone, homeIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_NEARME, R.string.main_my_card, R.drawable.btn_phone, nearmeIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_ME, R.string.main_message, R.drawable.btn_phone, meIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_MORE, R.string.main_more, R.drawable.btn_phone, moreIntent));
	}
	
	/**
	 * 构建TabHost的Tab页
	 * @param tag 标记
	 * @param resLabel 标签
	 * @param resIcon 图标
	 * @param content 该tab展示的内容
	 * @return 一个tab
	 */
	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,final Intent content) {
		return this.mTabHost.newTabSpec(tag).setIndicator(getString(resLabel),
				getResources().getDrawable(resIcon)).setContent(content);
	} 
	
	
	@Override
	public void onCheckedChanged(RadioGroup arg0, int checkedId) {
		switch(checkedId){
		case R.id.radio_button1:
			this.mTabHost.setCurrentTabByTag(TAB_TAG_HOME);
			break;
		case R.id.radio_button2:
			this.mTabHost.setCurrentTabByTag(TAB_TAG_NEARME);
			break;
		case R.id.radio_button3:
			this.mTabHost.setCurrentTabByTag(TAB_TAG_ME);
			break;
		case R.id.radio_button4:
			this.mTabHost.setCurrentTabByTag(TAB_TAG_MORE);
			break;
		}
	}
	
	public void tabClick(View v) {
		
	}
	
	private void checkLogin() {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.autoLogin(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				UserEntity user = (UserEntity)data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					appContext.saveLoginInfo(user);
//					getUnReadMessage();
//					if (!Utils.hasBind(getApplicationContext())) {
//						blindBaidu();
//					}
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), user.getMessage(), Toast.LENGTH_SHORT);
					showLogin();
					break;
				}
			}
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void showLogin() {
		appContext.setUserLogout();
		Intent intent = new Intent(this,LoginCode1.class);
        startActivity(intent);
        AppManager.getAppManager().finishActivity(this);
	}
}
