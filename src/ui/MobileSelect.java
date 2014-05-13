package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bean.ContactBean;
import bean.Entity;

import com.google.analytics.tracking.android.EasyTracker;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.ContactHomeAdapter;
import widget.QuickAlphabeticBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MobileSelect extends AppActivity {

	private ContactHomeAdapter adapter;
	private ListView personList;
	private List<ContactBean> list;
	private AsyncQueryHandler asyncQuery;
	private QuickAlphabeticBar alpha;
	private boolean authority;
	private ProgressDialog loadingPd;
	private String code;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_contact_page);
		code = getIntent().getStringExtra("code");
		loadingPd = UIHelper.showProgress(MobileSelect.this, null, null, true);
		Handler jumpHandler = new Handler();
        jumpHandler.postDelayed(new Runnable() {
			public void run() {
				personList = (ListView) MobileSelect.this.findViewById(R.id.acbuwa_list);
				alpha = (QuickAlphabeticBar) MobileSelect.this.findViewById(R.id.fast_scroller);
				asyncQuery = new MyAsyncQueryHandler(getContentResolver());
				init();
				setAdapter();
			}
		}, 100);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;

		default:
			break;
		}
	}

	private void init(){
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		asyncQuery.startQuery(0, null, uri, null, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
	}

	/**
	 * 数据库异步查询类AsyncQueryHandler
	 * 
	 * @author administrator
	 * 
	 */
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		/**
		 * 查询结束的回调函数
		 */
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					
					String mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
					if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
				     	String phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
				     	phone = phone.replace(" ", "");
				     	phone = phone.replace("+86", "");
				     	phone = phone.replace("-", "");
			     		ContactBean cb = new ContactBean();
						cb.setDisplayName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
						cb.setPhoneNum(phone);
						cb.setSortKey(cursor.getString(cursor.getColumnIndex("sort_key")));
						cb.setContactId(cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
						cb.setPhotoId(cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID)));
						cb.setLookUpKey(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)));
						list.add(cb);
					}
				}
				if (list.size() > 0) {
					adapter.notifyDataSetChanged();
					HashMap<String, Integer> alphaIndexer = new HashMap<String, Integer>();
					String[] sections = new String[list.size()];

					for (int i =0; i <list.size(); i++) {
						String name = StringUtils.getAlpha(list.get(i).getSortKey());
						if(!alphaIndexer.containsKey(name)){ 
							alphaIndexer.put(name, i);
						}
					}
					
					Set<String> sectionLetters = alphaIndexer.keySet();
					ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
					Collections.sort(sectionList);
					sections = new String[sectionList.size()];
					sectionList.toArray(sections);

					alpha.setAlphaIndexer(alphaIndexer);
					alpha.setVisibility(View.VISIBLE);
					UIHelper.dismissProgress(loadingPd);
				}
			}
			else {
				authority = false;
				UIHelper.dismissProgress(loadingPd);
				WarningDialog();
				return;
			}
		}
	}

	private void setAdapter() {
		
		list = new ArrayList<ContactBean>();
		adapter = new ContactHomeAdapter(this, list, alpha);
		personList.setAdapter(adapter);
		alpha.init(MobileSelect.this);
		alpha.setListView(personList);
		alpha.setHight(ImageUtils.getDisplayHeighth(getApplicationContext()) - ImageUtils.dip2px(getApplicationContext(), 100));
		personList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactBean cb = (ContactBean) adapter.getItem(position);
				showContactDialog( cb);
			}
		});
	}


	protected void showContactDialog(final ContactBean cb) {
		String message = "协助录入该联系人吗？";
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(message);
		builder.setTitle("通讯录提示");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				phonebookAssist(cb.getDisplayName(), cb.getPhoneNum());
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	   builder.create().show();
	}
	
	private void phonebookAssist(String realname, String phone) {
		if (phone.length() == 12) {
			phone = phone.substring(1, 12);
		}
		loadingPd = UIHelper.showProgress(MobileSelect.this, null, null, true);
		AppClient.phonebookAssist(appContext, realname, StringUtils.doEmpty(phone), code, new FileCallback() {
			@Override
			public void onSuccess(String url) {
				UIHelper.dismissProgress(loadingPd);
				Intent intent = new Intent();
				intent.putExtra("url", url);
				setResult(RESULT_OK, intent);
				UIHelper.ToastMessage(context, "录入成功", Toast.LENGTH_SHORT);
				finish();
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				if (StringUtils.notEmpty(message)) {
					WarningDialog(message);
				}
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	protected void WarningDialog() {
		String message = "请在手机的[设置]->[应用]->[群友通讯录]->[权限管理]，允许群友通讯录访问你的联系人记录并重新运行程序";
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(message);
		builder.setTitle("通讯录提示");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				AppManager.getAppManager().finishActivity(MobileSelect.this);
			}
		});
	   builder.create().show();
	}
	
	protected void WarningDialog(String message) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(message);
		builder.setTitle("通讯录提示");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	   builder.create().show();
	}

}
