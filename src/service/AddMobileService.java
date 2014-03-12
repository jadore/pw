package service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import baidupush.Utils;
import bean.CardIntroEntity;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.crashlytics.android.Crashlytics;

import tools.AppException;
import tools.Logger;
import tools.StringUtils;
import config.CommonValue;
import config.MyApplication;
import contact.AddressBean;
import contact.DateBean;
import contact.EmailBean;
import contact.IMBean;
import contact.MobileSynBean;
import contact.MobileSynListBean;
import contact.PhoneBean;
import contact.UrlBean;
import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;

public class AddMobileService extends IntentService{
	
	public static String			MOBILESYN_CLIENT = "add.mobile.service";
	private static final String ACTION_START_PAY = MOBILESYN_CLIENT + ".START.PAY";
	private static final String	ACTION_STOP = MOBILESYN_CLIENT + ".STOP";
	private MyApplication appContext ;
	
	public AddMobileService() {
		super(MOBILESYN_CLIENT);
	}

	public static void actionStartPAY(Context ctx, CardIntroEntity entity, boolean sendBC) {
		try{
			Intent i = new Intent(ctx, AddMobileService.class);
			i.putExtra("card", entity);
			i.setAction(ACTION_START_PAY);
			i.putExtra("sendBC", sendBC);
			ctx.startService(i);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void actionStop(Context ctx) {
		try {
			Intent i = new Intent(ctx, AddMobileService.class);
			i.setAction(ACTION_STOP);
			ctx.stopService(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(ACTION_START_PAY) == true) {
			appContext = MyApplication.getInstance();
			CardIntroEntity card = (CardIntroEntity) intent.getSerializableExtra("card");
			boolean sendBC = intent.getBooleanExtra("sendBC", false);
			try {
				getContactInfo(card, sendBC);
				try {
					if (appContext.isLogin() && !Utils.hasBind(this)) {
						PushManager.startWork(getApplicationContext(),
								PushConstants.LOGIN_TYPE_API_KEY, 
								Utils.getMetaValue(this, "api_key"));
					}
				} catch (Exception e) {
					Logger.i(e);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//	private List<Contacts> list;
//	private Context context;
	
	private MobileSynBean person;
	public void getContactInfo(CardIntroEntity card, boolean sendBC) throws JSONException, AppException {
		// 获得通讯录信息 ，URI是ContactsContract.Contacts.CONTENT_URI
		MobileSynListBean allPerson = new MobileSynListBean();
		List<MobileSynBean> persons = new ArrayList<MobileSynBean>();
		String mimetype = "";
		int oldrid = -1;
		int contactId = -1;
		Cursor cursor = getContentResolver().query(Data.CONTENT_URI,null, null, null, Data.RAW_CONTACT_ID);
		if (cursor.getColumnCount() == 1) {
			if (sendBC) {
				sendBroadcast(CommonValue.ContactOperationResult.NOT_AUTHORITY);
			}
			return;
		}
		while (cursor.moveToNext()) {
			contactId = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
			if (oldrid != contactId) {
				person = new MobileSynBean();
			   	persons.add(person);
			   	allPerson.data.add(person);
			    oldrid = contactId;
			}
			// 取得mimetype类型
			mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
			// 获得通讯录中每个联系人的ID
			// 获得通讯录中联系人的名字
			if (StructuredName.CONTENT_ITEM_TYPE.equals(mimetype)) {
			    String prefix = cursor.getString(cursor.getColumnIndex(StructuredName.PREFIX));
			    String firstName = cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME));
			    String middleName = cursor.getString(cursor.getColumnIndex(StructuredName.MIDDLE_NAME));
			    String lastname = cursor.getString(cursor.getColumnIndex(StructuredName.GIVEN_NAME));
			    String suffix = cursor.getString(cursor.getColumnIndex(StructuredName.SUFFIX));
			    
			    person.prefix = prefix==null?"":prefix;
			    person.suffix = suffix==null?"":suffix;
			    person.firstname = firstName==null?"":firstName;
			    person.middlename = middleName==null?"":middleName;
			    person.lastname = lastname==null?"":lastname;
			}
			// 获取电话信息
			if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
			    // 取出电话类型
			    int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
			    PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
			    switch (phoneType) {
				case Phone.TYPE_CUSTOM:
					phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
					break;
				case Phone.TYPE_HOME:
					phoneBean.label =  "住宅";
					break;
				case Phone.TYPE_MOBILE:
					phoneBean.label =  "移动";
					break;
				case Phone.TYPE_WORK:
					phoneBean.label =  "工作";
					break;
				case Phone.TYPE_FAX_WORK:
					phoneBean.label =  "工作传真";
					break;
				case Phone.TYPE_FAX_HOME:
					phoneBean.label =  "住宅传值";
					break;
				case Phone.TYPE_PAGER:
					phoneBean.label =  "传呼";
					break;
				case Phone.TYPE_OTHER:
					phoneBean.label =  "其他";
					break;
				case Phone.TYPE_CALLBACK:
					phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));;
					break;
				case Phone.TYPE_CAR:
					phoneBean.label =  "车载";
					break;
				case Phone.	TYPE_COMPANY_MAIN:
					phoneBean.label =  "工作";
					break;
				case Phone.TYPE_ISDN:
					phoneBean.label =  "ISDN";
					break;
				case Phone.TYPE_MAIN:
					phoneBean.label =  "主要";
					break;
				case Phone.TYPE_OTHER_FAX:
					phoneBean.label =  "其他传真";
					break;
				case Phone.TYPE_RADIO:
					phoneBean.label =  "无线";
					break;
				case Phone.TYPE_TELEX:
					phoneBean.label =  "电报";
					break;
				case Phone.TYPE_TTY_TDD:
					phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));;
					break;
				case Phone.TYPE_WORK_MOBILE:
					phoneBean.label =  "工作移动";
					break;
				case Phone.TYPE_WORK_PAGER:
					phoneBean.label =  "工作传呼";
					break;
				case Phone.TYPE_ASSISTANT:
					phoneBean.label =  "助手";
					break;
				case Phone.TYPE_MMS:
					phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));;
					break;
				}
			    person.phone.add(phoneBean);
		   }
		   // 查找email地址
		   if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
			   // 取出邮件类型
			   int emailType = cursor.getInt(cursor.getColumnIndex(Email.TYPE));
			   EmailBean emailBean = new EmailBean();
			   emailBean.email = cursor.getString(cursor.getColumnIndex(Email.DATA));
			   switch (emailType) {
			   case Email.TYPE_CUSTOM:
				   emailBean.label =  cursor.getString(cursor.getColumnIndex(Email.LABEL));
				   break;
			   case Email.TYPE_HOME:
				   emailBean.label =  "住宅";
				   break;
			   case Email.TYPE_WORK:
				   emailBean.label =  "工作";
				   break;
			   case Email.TYPE_OTHER:
				   emailBean.label =  "其他";
				   break;
			   case Email.TYPE_MOBILE:
				   emailBean.label =  "住宅";
				   break;
			   }
			   person.email.add(emailBean);
		   }
		   // 查找event地址
		   if (Event.CONTENT_ITEM_TYPE.equals(mimetype)) {
			   // 取出时间类型
			   int eventType = cursor.getInt(cursor.getColumnIndex(Event.TYPE));
			   switch (eventType) {
			   case Event.TYPE_CUSTOM:
				   DateBean date = new DateBean();
				   date.date = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
				   date.label = cursor.getString(cursor.getColumnIndex(Event.LABEL));
				   person.dates.add(date);
				   break;
			   case Event.TYPE_ANNIVERSARY:
				   DateBean date1 = new DateBean();
				   date1.date = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
				   date1.label = cursor.getString(cursor.getColumnIndex(Event.LABEL));
				   person.dates.add(date1);
				   break;
			   case Event.TYPE_OTHER:
				   DateBean date2 = new DateBean();
				   date2.date = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
				   date2.label = "其他";
				   person.dates.add(date2);
				   break;
			   case Event.TYPE_BIRTHDAY:
				   String birthday = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
				   person.birthday = birthday==null?"":birthday;
				   break;
			   }
		   }
		   // 即时消息
		   if (Im.CONTENT_ITEM_TYPE.equals(mimetype)) {
			   // 取出即时消息类型
			   int protocal = cursor.getInt(cursor.getColumnIndex(Im.PROTOCOL));
			   IMBean im = new IMBean();
			   im.im = cursor.getString(cursor.getColumnIndex(Im.DATA));
			   im.username = cursor.getString(cursor.getColumnIndex(Im.DATA));
			   switch (protocal) {
			   case Im.TYPE_CUSTOM:
				   im.label = cursor.getString(cursor.getColumnIndex(Im.LABEL));
				   break;
			   case Im.PROTOCOL_MSN:
				   im.label = "MSN";
				   break;
			   case Im.PROTOCOL_YAHOO:
				   im.label = "YAHOO";
				   break;
			   case Im.PROTOCOL_SKYPE:
				   im.label = "SKYPE";
				   break;
			   case Im.PROTOCOL_QQ:
				   im.label = "QQ";
				   break;
			   case Im.PROTOCOL_GOOGLE_TALK:
				   im.label = "GOOGLE TALK";
				   break;
			   case Im.PROTOCOL_ICQ:
				   im.label = "ICQ";
				   break;
			   case Im.PROTOCOL_JABBER:
				   im.label = "JABBER";
				   break;
			   case Im.PROTOCOL_NETMEETING:
				   im.label = "NETMEETING";
				   break;
			   }
			   person.im.add(im);
		   }
		   // 获取备注信息
		   if (Note.CONTENT_ITEM_TYPE.equals(mimetype)) {
			   String remark = cursor.getString(cursor.getColumnIndex(Note.NOTE));
			   person.note = remark==null?"":remark;
		   }
		   // 获取组织信息
		   if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) {
			   String organization = cursor.getString(cursor.getColumnIndex(Organization.COMPANY));
			   String jobtitle = cursor.getString(cursor.getColumnIndex(Organization.TITLE));
			   String department = cursor.getString(cursor.getColumnIndex(Organization.DEPARTMENT));
			   person.organization = organization==null?"":organization;
			   person.jobtitle = jobtitle==null?"":jobtitle;
			   person.department = department==null?"":department;
		   }
		   // 获取网站信息
		   if (Website.CONTENT_ITEM_TYPE.equals(mimetype)) {
			   // 取出组织类型
			   int webType = cursor.getInt(cursor.getColumnIndex(Website.TYPE));
			   UrlBean url = new UrlBean();
			     url.url = cursor.getString(cursor.getColumnIndex(Website.URL));
			   switch (webType) {
				case Website.TYPE_CUSTOM:
					url.label = cursor.getString(cursor.getColumnIndex(Website.LABEL));
					break;
				case Website.TYPE_HOMEPAGE:
					url.label = "个人主页";
					break;
				case Website.TYPE_BLOG:
					url.label = "博客";
					break;
				case Website.TYPE_PROFILE:
					url.label = cursor.getString(cursor.getColumnIndex(Website.LABEL));
					break;
				case Website.TYPE_HOME:
					url.label = "主页";
					break;
				case Website.TYPE_WORK:
					url.label = "工作";
					break;
				case Website.TYPE_FTP:
					url.label = "FTP";
					break;
				case Website.TYPE_OTHER:
					url.label = "其他";
					break;
			   }
			   person.url.add(url);
		   }
		   // 查找通讯地址
		   if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)) {
			   // 取出邮件类型
			   int postalType = cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE));
			   String street = cursor.getString(cursor.getColumnIndex(StructuredPostal.STREET));
			   String ciry = cursor.getString(cursor.getColumnIndex(StructuredPostal.CITY));
			   String box = cursor.getString(cursor.getColumnIndex(StructuredPostal.POBOX));
			   String area = cursor.getString(cursor.getColumnIndex(StructuredPostal.NEIGHBORHOOD));
			   String state = cursor.getString(cursor.getColumnIndex(StructuredPostal.REGION));
			   String zip = cursor.getString(cursor.getColumnIndex(StructuredPostal.POSTCODE));
			   String country = cursor.getString(cursor.getColumnIndex(StructuredPostal.COUNTRY));
			   AddressBean address = new AddressBean();
			   address.address = street+""+ciry+""+box+""+area+""+state+""+zip+""+country;
			   switch (postalType) {
			   case StructuredPostal.TYPE_CUSTOM:
				   address.label = cursor.getString(cursor.getColumnIndex(StructuredPostal.LABEL));;
			   case StructuredPostal.TYPE_HOME:
				   address.label = "住宅";
				   break;
			   case StructuredPostal.TYPE_WORK:
				   address.label = "工作";
				   break;
			   case StructuredPostal.TYPE_OTHER:
				   address.label = "其他";
				   break;
			   }
			   person.address.add(address);
		   }   
	   }
	  cursor.close();
	  updateMobiles(true, allPerson);
	  try {
		  boolean exit = false;
		  for (MobileSynBean mobileSynBean : persons) {
			  for (PhoneBean phone : mobileSynBean.phone) {
				  String mobile = phone.phone;
				  mobile = mobile.replace(" ", "");
				  mobile = mobile.replace("+86", "");
				  mobile = mobile.replace("-", "");
				  if (mobile.indexOf(card.phone) != -1) {
					  exit = true;
					  if (sendBC) {
						  Logger.i("aaa");
							sendBroadcast(CommonValue.ContactOperationResult.EXIST);
						}
					  break;
				  }
			  }
		  }
		  if (!exit) {
			  insert(card, sendBC);
		  }
	  } catch (Exception e) {
		  if (sendBC) {
				sendBroadcast(CommonValue.ContactOperationResult.SAVE_FAILURE);
		  }
		  Crashlytics.logException(e);
	  }
	}
	
	private void insert(CardIntroEntity card, boolean sendBC) {
		try {
			ContentValues values = new ContentValues();
	        //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
	        Uri rawContactUri = this.getContentResolver().insert(RawContacts.CONTENT_URI, values);
	        if (StringUtils.empty(rawContactUri)) {
	        	if (sendBC) {
					sendBroadcast(CommonValue.ContactOperationResult.SAVE_FAILURE);
				}
	        	return;
			}
	        long rawContactId = ContentUris.parseId(rawContactUri);
	        
	        values.clear();
	        values.put(Data.RAW_CONTACT_ID, rawContactId);
	        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
	        values.put(StructuredName.GIVEN_NAME, card.realname);
	        this.getContentResolver().insert(
	                android.provider.ContactsContract.Data.CONTENT_URI, values);
	        
	        values.clear();
	        values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
	        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
	        values.put(Phone.NUMBER, card.phone);
	        values.put(Phone.TYPE, Phone.TYPE_MOBILE);
	        this.getContentResolver().insert(
	                android.provider.ContactsContract.Data.CONTENT_URI, values);

	        if (StringUtils.notEmpty(card.email)) {
	            values.clear();
	            values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
	            values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
	            values.put(Email.DATA, card.email);
	            values.put(Email.TYPE, Email.TYPE_WORK);
	            this.getContentResolver().insert(
	                    android.provider.ContactsContract.Data.CONTENT_URI, values);
			}
	        
	            values.clear();
	            values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
	            values.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
	            if (StringUtils.notEmpty( card.department)) {
	            	values.put(Organization.COMPANY, card.department);
				}
	            else {
	            	values.put(Organization.COMPANY, card.position);
	            }
	            values.put(Organization.TITLE, card.position);  
	            values.put(Organization.TYPE, Organization.TYPE_WORK);  
	            this.getContentResolver().insert(
	                    android.provider.ContactsContract.Data.CONTENT_URI, values);

	            values.clear();
	            values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
	            values.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
	            values.put(Contacts.ContactMethods.KIND, Contacts.KIND_POSTAL);
	            values.put(Contacts.ContactMethods.TYPE, Contacts.ContactMethods.TYPE_WORK);
	            values.put(Contacts.ContactMethods.DATA, card.address);
	            this.getContentResolver().insert(
	                    android.provider.ContactsContract.Data.CONTENT_URI, values);
	            if (sendBC) {
					sendBroadcast(CommonValue.ContactOperationResult.SAVE_SUCCESS);
	            }
		} catch (Exception e) {
			if (sendBC) {
				sendBroadcast(CommonValue.ContactOperationResult.SAVE_FAILURE);
			}
			Crashlytics.logException(e);
		}
	}
	
	private void updateMobiles(boolean authority, MobileSynListBean model) {
		try {
			appContext.saveObject(model, "mobile");
		} catch(Exception e) {
			Crashlytics.logException(e);
		}
	}
	
	private void sendBroadcast(int type) {
		Intent intent = new Intent();
		intent.putExtra(CommonValue.ContactOperationResult.ContactOperationResultType, type);
		intent.setAction(CommonValue.ContactOperationResult.ContactBCAction);
		sendBroadcast(intent);
	}
}
