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
import android.view.KeyEvent;
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
			closeInput();
			AppManager.getAppManager().finishActivity(this);
			overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
			break;

		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			AppManager.getAppManager().finishActivity(this);
			overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
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
		List<CardIntroEntity> ops3 = new ArrayList<CardIntroEntity>();
		CardIntroEntity op31 = new CardIntroEntity();
		op31.realname = "功能消息免打扰";
		op31.position = "";
		op31.cardSectionType = CommonValue.CardSectionType .SettingsSectionType;
		ops3.add(op31);
		cards.add(ops3);
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
