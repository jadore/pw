package ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.CookieStore;

import service.IPolemoService;
import tools.AppManager;
import tools.StringUtils;
import tools.UpdateManager;
import ui.adapter.MeCardAdapter;
import ui.adapter.SettingCardAdapter;
import bean.CardIntroEntity;

import com.loopj.android.http.PersistentCookieStore;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;

public class Setting extends AppActivity{
	private ExpandableListView iphoneTreeView;
	private List<List<CardIntroEntity>> cards;
	private SettingCardAdapter mCardAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		initUI();
		addCardOp();
		mCardAdapter.notifyDataSetChanged();
		expandView();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		}
	}
	
	private void initUI() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View footer = inflater.inflate(R.layout.index_footer, null);
		iphoneTreeView = (ExpandableListView) findViewById(R.id.iphone_tree_view);
		iphoneTreeView.setGroupIndicator(null);
		iphoneTreeView.addFooterView(footer);
		cards = new ArrayList<List<CardIntroEntity>>();
		mCardAdapter = new SettingCardAdapter(iphoneTreeView, this, cards);
		iphoneTreeView.setAdapter(mCardAdapter);
		iphoneTreeView.setSelection(0);
		iphoneTreeView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int position,
					long arg3) {
				return true;
			}
		});
	}
	
	private void expandView() {
		for (int i = 0; i < cards.size(); i++) {
			iphoneTreeView.expandGroup(i);
		}
	}
	
	
	private void addCardOp() {
//		List<CardIntroEntity> ops = new ArrayList<CardIntroEntity>();
//		CardIntroEntity op1 = new CardIntroEntity();
//		op1.realname = "我微友通讯录二维码";
//		op1.department = CommonValue.subTitle.subtitle4;
//		op1.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
//		op1.position = "";
//		ops.add(op1);
//		CardIntroEntity op2 = new CardIntroEntity();
//		op2.realname = "扫一扫";
//		op2.department = CommonValue.subTitle.subtitle5;
//		op2.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
//		op2.position = "";
//		ops.add(op2);
//		cards.add(ops);
		
		List<CardIntroEntity> ops2 = new ArrayList<CardIntroEntity>();
		CardIntroEntity op21 = new CardIntroEntity();
		op21.realname = "客服反馈";
		op21.department = CommonValue.subTitle.subtitle6;
		op21.position = "";
		op21.cardSectionType = CommonValue.CardSectionType .FeedbackSectionType;
		ops2.add(op21);
		cards.add(ops2);
		
		List<CardIntroEntity> ops3 = new ArrayList<CardIntroEntity>();
		CardIntroEntity op31 = new CardIntroEntity();
		op31.realname = "功能消息免打扰";
		op31.department = "开启免打扰后，功能消息将收不到声音和震动提醒。";
		op31.position = "";
		op31.cardSectionType = CommonValue.CardSectionType .SettingsSectionType;
		ops3.add(op31);
		CardIntroEntity op32 = new CardIntroEntity();
		op32.realname = "检查版本";
		op32.department = "当前版本:"+getCurrentVersionName();
		op32.position = "";
		op32.cardSectionType = CommonValue.CardSectionType .SettingsSectionType;
		ops3.add(op32);
		
		CardIntroEntity op33 = new CardIntroEntity();
		op33.realname = "注销";
		op33.department = "退出当前账号重新登录";
		op33.position = "";
		op33.cardSectionType = CommonValue.CardSectionType .SettingsSectionType;
		ops3.add(op33);
		
		cards.add(ops3);
		
	}
	
	/**
	 * 获取当前客户端版本信息
	 */
	private String  getCurrentVersionName(){
		String versionName = null;
        try { 
        	PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
        	versionName = info.versionName;
        } catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
        return versionName;
	}
	
	public void showFeedback() {
		Intent intent = new Intent(this, Feedback.class);
		startActivity(intent);
	}
	
	public void showUpdate() {
		UpdateManager.getUpdateManager().checkAppUpdate(this, true);
	}
	
	public void logout() {
		new AlertDialog.Builder(this).setTitle("确定注销本账号吗?")
		.setNeutralButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AppClient.Logout(appContext);
				CookieStore cookieStore = new PersistentCookieStore(Setting.this);  
				cookieStore.clear();
				AppManager.getAppManager().finishAllActivity();
				appContext.setUserLogout();
				if (appContext.getPolemoClient()!=null) {
					appContext.getPolemoClient().disconnect();
				}
				if (isServiceRunning()) {
					Intent intent1 = new Intent(Setting.this, IPolemoService.class);
					stopService(intent1);
				}
				Intent intent = new Intent(Setting.this, LoginCode1.class);
				startActivity(intent);
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).show();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null; 
		 switch(id) {  
         case 1:  
        	 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
             builder.setTitle("功能消息免打扰");  
             final ChoiceOnClickListener choiceListener =   
                 new ChoiceOnClickListener();  
             String interupt = appContext.getMessageInterupt();
             int i = 1;
             try {
            	i = StringUtils.empty(interupt)? 1 : Integer.valueOf(interupt);
             }
             catch (Exception e) {
            	i = 1;
             }
             
             builder.setSingleChoiceItems(R.array.message_settings, i, choiceListener);  
               
             DialogInterface.OnClickListener btnListener =   
                 new DialogInterface.OnClickListener() {  
                     @Override  
                     public void onClick(DialogInterface dialogInterface, int which) {  
                         int choiceWhich = choiceListener.getWhich();  
                         appContext.setMessageInterupt(choiceWhich+"");
                         AppClient.setUser(context, "", "", choiceWhich+"");
                     }  
                 };  
             builder.setPositiveButton("确定", btnListener);  
             dialog = builder.create();  
             break;  
		 }  
		 return dialog;
	}
	
	private class ChoiceOnClickListener implements DialogInterface.OnClickListener {  
		  
        private int which = 2;  
        @Override  
        public void onClick(DialogInterface dialogInterface, int which) {  
            this.which = which;  
        }  
          
        public int getWhich() {  
            return which;  
        }  
    }
}	
