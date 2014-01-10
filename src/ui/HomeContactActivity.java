package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bean.ContactBean;
import bean.Entity;

import com.google.gson.Gson;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import contact.MobileSynListBean;

import service.MobileSynService;
import sms.MessageBoxList;
import tools.AppException;
import tools.AppManager;
import tools.BaseIntentUtil;
import tools.DecodeUtil;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import ui.adapter.ContactHomeAdapter;
import widget.QuickAlphabeticBar;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class HomeContactActivity extends AppActivity {

	private ContactHomeAdapter adapter;
	private ListView personList;
	private List<ContactBean> list;
	private AsyncQueryHandler asyncQuery;
	private QuickAlphabeticBar alpha;

	private Map<Integer, ContactBean> contactIdMap = null;
	
	private GetMobileReceiver getMobileReceiver;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_contact_page);

		personList = (ListView)this.findViewById(R.id.acbuwa_list);
		alpha = (QuickAlphabeticBar)this.findViewById(R.id.fast_scroller);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		init();
		setAdapter();
		registerGetReceiver();
		MobileSynService.actionStartPAY(this);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
			break;

		default:
			break;
		}
	}

	private void init(){
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				"sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
		}; // 查询的列
		asyncQuery.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			AppManager.getAppManager().finishActivity(this);
			overridePendingTransition(R.anim.exit_in_from_left, R.anim.exit_out_to_right);
		}
		return super.onKeyDown(keyCode, event);
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
				
				contactIdMap = new HashMap<Integer, ContactBean>();
				
				
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);

					if (contactIdMap.containsKey(contactId)) {
						
					}else{
						
						ContactBean cb = new ContactBean();
						cb.setDisplayName(name);
//					if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
//						cb.setPhoneNum(number.substring(3));
//					} else {
						cb.setPhoneNum(number);
//					}
						cb.setSortKey(sortKey);
						cb.setContactId(contactId);
						cb.setPhotoId(photoId);
						cb.setLookUpKey(lookUpKey);
						list.add(cb);
						
						contactIdMap.put(contactId, cb);
						
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
				}
			}
		}

	}


	private void setAdapter() {
		list = new ArrayList<ContactBean>();
		adapter = new ContactHomeAdapter(this, list, alpha);
		personList.setAdapter(adapter);
		alpha.init(HomeContactActivity.this);
		alpha.setListView(personList);
		alpha.setHight(ImageUtils.getDisplayHeighth(getApplicationContext()) - ImageUtils.dip2px(getApplicationContext(), 100));
		personList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactBean cb = (ContactBean) adapter.getItem(position);
				showContactDialog(lianxiren1, cb, position);
			}
		});
	}


	private String[] lianxiren1 = new String[] { "拨打电话", "发送短信", "查看详细","删除" };

	//群组联系人弹出页
	private void showContactDialog(final String[] arg ,final ContactBean cb, final int position){
		new AlertDialog.Builder(this).setTitle(cb.getDisplayName()).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){

				Uri uri = null;

				switch(which){

				case 0://打电话
					String toPhone = cb.getPhoneNum();
					uri = Uri.parse("tel:" + toPhone);
					Intent it = new Intent(Intent.ACTION_CALL, uri);
					startActivity(it);
					break;

				case 1://发短息

					String threadId = getSMSThreadId(cb.getPhoneNum());
					
					Map<String, String> map = new HashMap<String, String>();
					map.put("phoneNumber", cb.getPhoneNum());
					map.put("threadId", threadId);
					BaseIntentUtil.intentSysDefault(HomeContactActivity.this, MessageBoxList.class, map);
					break;

				case 2:// 查看详细       修改联系人资料

					uri = ContactsContract.Contacts.CONTENT_URI;
					Uri personUri = ContentUris.withAppendedId(uri, cb.getContactId());
					Intent intent2 = new Intent();
					intent2.setAction(Intent.ACTION_VIEW);
					intent2.setData(personUri);
					startActivity(intent2);
					break;

				case 3:// 删除
					showDelete(cb.getContactId(), position);
					break;
				}
			}
		}).show();
	}

	// 删除联系人方法
	private void showDelete(final int contactsID, final int position) {
		new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle("是否删除此联系人")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//源码删除
				Uri deleteUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactsID);
				Uri lookupUri = ContactsContract.Contacts.getLookupUri(HomeContactActivity.this.getContentResolver(), deleteUri);
				if(lookupUri != Uri.EMPTY){
					HomeContactActivity.this.getContentResolver().delete(deleteUri, null, null);
				}
				adapter.remove(position);
				adapter.notifyDataSetChanged();
				Toast.makeText(HomeContactActivity.this, "该联系人已经被删除.", Toast.LENGTH_SHORT).show();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).show();
	}

	public static String[] SMS_COLUMNS = new String[]{  
		"thread_id"
	};
	
	private String getSMSThreadId(String adddress){
		Cursor cursor = null;  
		ContentResolver contentResolver = getContentResolver();  
		cursor = contentResolver.query(Uri.parse("content://sms"), SMS_COLUMNS, " address like '%" + adddress + "%' ", null, null);  
		String threadId = "";
		if (cursor == null || cursor.getCount() > 0){
			cursor.moveToFirst();
			threadId = cursor.getString(0);
			cursor.close();
			return threadId;
		}else{
			cursor.close();
			return threadId;
		}
	}
	
	class GetMobileReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			MobileSynListBean mobiles = (MobileSynListBean) intent.getSerializableExtra("mobile");
			Logger.i(mobiles.data.size()+"");
			Gson gson = new Gson();
			String json = gson.toJson(mobiles.data);
			try {
				String encodeJson = DecodeUtil.encodeContact(json);
				syncMobile(encodeJson);
			} catch (AppException e) {
				Logger.i(e);
			}
		}
	}
	
	private void registerGetReceiver() {
		getMobileReceiver =  new  GetMobileReceiver();
        IntentFilter postFilter = new IntentFilter();
        postFilter.addAction("update");
        registerReceiver(getMobileReceiver, postFilter);
	}
	
	private void unregisterGetReceiver() {
		unregisterReceiver(getMobileReceiver);
	}
	
	@Override
	protected void onDestroy() {
		unregisterGetReceiver();
		super.onDestroy();
	}
	
	private void syncMobile(String encodeJson) {
		AppClient.syncContact(appContext, encodeJson, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				
			}
			
			@Override
			public void onFailure(String message) {
				
			}
			
			@Override
			public void onError(Exception e) {
				Logger.i(e.toString());
			}
	  });
	}

}
