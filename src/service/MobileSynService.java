package service;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import contact.AddressBean;
import contact.DateBean;
import contact.EmailBean;
import contact.IMBean;
import contact.MobileSynBean;
import contact.MobileSynListBean;
import contact.PhoneBean;
import contact.UrlBean;

import bean.ContactBean;

import tools.Logger;
import android.app.IntentService;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;

public class MobileSynService extends IntentService{
	private AsyncQueryHandler asyncQuery;
	public static String			MOBILESYN_CLIENT = "pw.mobile.service";
	private static final String ACTION_START_PAY = MOBILESYN_CLIENT + ".START.PAY";
	private static final String	ACTION_STOP = MOBILESYN_CLIENT + ".STOP";
	
	public MobileSynService() {
		super(MOBILESYN_CLIENT);
	}

	public static void actionStartPAY(Context ctx) {
		try{
			Intent i = new Intent(ctx, MobileSynService.class);
			i.setAction(ACTION_START_PAY);
			ctx.startService(i);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void actionStop(Context ctx) {
		try {
			Intent i = new Intent(ctx, MobileSynService.class);
			i.setAction(ACTION_STOP);
			ctx.stopService(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(ACTION_START_PAY) == true) {
			try {
				getContactInfo();
//				Logger.i(s);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//	private List<Contacts> list;
//	private Context context;
	
	private MobileSynBean person;
	public void getContactInfo() throws JSONException {
		  // 获得通讯录信息 ，URI是ContactsContract.Contacts.CONTENT_URI
		MobileSynListBean persons = new MobileSynListBean();
		  String mimetype = "";
		  int oldrid = -1;
		  int contactId = -1;
		  Cursor cursor = getContentResolver().query(Data.CONTENT_URI,null, null, null, Data.RAW_CONTACT_ID);
		  int numm=0;
		  while (cursor.moveToNext()) {
			  
		   contactId = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
		   if (oldrid != contactId) {
			   	person = new MobileSynBean();
			   	persons.data.add(person);
			    numm++;
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
		    // 手机
		    switch (phoneType) {
			case Phone.TYPE_MOBILE:
				
				break;

			default:
				break;
			}
		    if (phoneType == Phone.TYPE_MOBILE) {
		     
		     	PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "移动";
	        	person.phone.add(phoneBean);
		     
		    }
		    // 住宅电话
		    if (phoneType == Phone.TYPE_HOME) {

		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "住宅";
	        	person.phone.add(phoneBean);
		    }
		    // 单位电话
		    if (phoneType == Phone.TYPE_WORK) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "工作";
	        	person.phone.add(phoneBean);
		    }
		    // 单位传真
		    if (phoneType == Phone.TYPE_FAX_WORK) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "工作传真";
	        	person.phone.add(phoneBean);
		    }
		    // 住宅传真
		    if (phoneType == Phone.TYPE_FAX_HOME) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "住宅传真";
	        	person.phone.add(phoneBean);
		    }
		    // 寻呼机
		    if (phoneType == Phone.TYPE_PAGER) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "传呼";
	        	person.phone.add(phoneBean);
		    }
		    // 回拨号码
		    if (phoneType == Phone.TYPE_CALLBACK) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    // 公司总机
		    if (phoneType == Phone.TYPE_COMPANY_MAIN) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "公司";
	        	person.phone.add(phoneBean);
		    }
		    // 车载电话
		    if (phoneType == Phone.TYPE_CAR) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "车载";
	        	person.phone.add(phoneBean);
		    }
		    // ISDN
		    if (phoneType == Phone.TYPE_ISDN) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    // 总机
		    if (phoneType == Phone.TYPE_MAIN) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  "主要";
	        	person.phone.add(phoneBean);
		    }
		    // 无线装置
		    if (phoneType == Phone.TYPE_RADIO) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    // 电报
		    if (phoneType == Phone.TYPE_TELEX) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    // TTY_TDD
		    if (phoneType == Phone.TYPE_TTY_TDD) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    // 单位手机
		    if (phoneType == Phone.TYPE_WORK_MOBILE) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    // 单位寻呼机
		    if (phoneType == Phone.TYPE_WORK_PAGER) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    // 助理
		    if (phoneType == Phone.TYPE_ASSISTANT) {
		     
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    // 彩信
		    if (phoneType == Phone.TYPE_MMS) {
		     PhoneBean phoneBean = new PhoneBean();
		     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
	        	person.phone.add(phoneBean);
		    }
		    if (phoneType == Phone.TYPE_CUSTOM) {
			     PhoneBean phoneBean = new PhoneBean();
			     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
			     	phoneBean.label =  cursor.getString(cursor.getColumnIndex(Phone.LABEL));
		        	person.phone.add(phoneBean);
			    }
		    if (phoneType == Phone.TYPE_OTHER) {
			     PhoneBean phoneBean = new PhoneBean();
			     	phoneBean.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
			     	phoneBean.label =  "其他";
		        	person.phone.add(phoneBean);
			    }
		   }
		   // }
		   // 查找email地址
		   if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
		    // 取出邮件类型
		    int emailType = cursor.getInt(cursor.getColumnIndex(Email.TYPE));

		    // 住宅邮件地址
		    if (emailType == Email.TYPE_CUSTOM) {
		     
		     EmailBean emailBean = new EmailBean();
		     emailBean.email = cursor.getString(cursor.getColumnIndex(Email.DATA));
		     emailBean.label =  cursor.getString(cursor.getColumnIndex(Email.LABEL));
	        	person.email.add(emailBean);
		    }

		    // 住宅邮件地址
		    else if (emailType == Email.TYPE_HOME) {
		     
		     EmailBean emailBean = new EmailBean();
		     emailBean.email = cursor.getString(cursor.getColumnIndex(Email.DATA));
		     emailBean.label =  "住宅";
	        	person.email.add(emailBean);
		    }

		    // 单位邮件地址
		    else if (emailType == Email.TYPE_WORK) {
		     
		     EmailBean emailBean = new EmailBean();
		     emailBean.email = cursor.getString(cursor.getColumnIndex(Email.DATA));
		     emailBean.label =  "工作";
	        	person.email.add(emailBean);
		    }

		    // 手机邮件地址
		    else if (emailType == Email.TYPE_MOBILE) {
		     
		     EmailBean emailBean = new EmailBean();
		     emailBean.email = cursor.getString(cursor.getColumnIndex(Email.DATA));
		     emailBean.label =  cursor.getString(cursor.getColumnIndex(Email.LABEL));
	        	person.email.add(emailBean);
		    }
		    else {
		    	EmailBean emailBean = new EmailBean();
			     emailBean.email = cursor.getString(cursor.getColumnIndex(Email.DATA));
			     emailBean.label =  cursor.getString(cursor.getColumnIndex(Email.LABEL));
		        	person.email.add(emailBean);
		    }
		   }
		   // 查找event地址
		   if (Event.CONTENT_ITEM_TYPE.equals(mimetype)) {
		    // 取出时间类型
		    int eventType = cursor.getInt(cursor.getColumnIndex(Event.TYPE));
		    // 生日
		    if (eventType == Event.TYPE_BIRTHDAY) {
		     String birthday = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
		     
		     person.birthday = birthday==null?"":birthday;
		    }
		    // 周年纪念日
		    if (eventType == Event.TYPE_ANNIVERSARY) {
		     String anniversary = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
		     
		     DateBean date = new DateBean();
		     date.date = cursor.getString(cursor.getColumnIndex(Event.START_DATE));
		     date.label = cursor.getString(cursor.getColumnIndex(Event.LABEL));
		     person.dates.add(date);
		    }
		   }
		   // 即时消息
		   if (Im.CONTENT_ITEM_TYPE.equals(mimetype)) {
		    // 取出即时消息类型
		    int protocal = cursor.getInt(cursor.getColumnIndex(Im.PROTOCOL));
		    if (Im.TYPE_CUSTOM == protocal) {
		     
		     IMBean im = new IMBean();
		     im.im = cursor.getString(cursor.getColumnIndex(Im.DATA));
		     im.username = cursor.getString(cursor.getColumnIndex(Im.DATA));
		     im.label = cursor.getString(cursor.getColumnIndex(Im.LABEL));
		     person.im.add(im);
		    }

		    else if (Im.PROTOCOL_MSN == protocal) {
		     
		     IMBean im = new IMBean();
		     im.im = cursor.getString(cursor.getColumnIndex(Im.DATA));
		     im.username = cursor.getString(cursor.getColumnIndex(Im.DATA));
		     im.label = cursor.getString(cursor.getColumnIndex(Im.LABEL));
		     person.im.add(im);
		    }
		    if (Im.PROTOCOL_QQ == protocal) {
		     
		     IMBean im = new IMBean();
		     im.im = cursor.getString(cursor.getColumnIndex(Im.DATA));
		     im.username = cursor.getString(cursor.getColumnIndex(Im.DATA));
		     im.label = cursor.getString(cursor.getColumnIndex(Im.LABEL));
		     person.im.add(im);
		    }
		   }
		   // 获取备注信息
		   if (Note.CONTENT_ITEM_TYPE.equals(mimetype)) {
		    String remark = cursor.getString(cursor.getColumnIndex(Note.NOTE));
		    
		    person.note = remark==null?"":remark;
		   }
		   // 获取组织信息
		   if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) {
		    // 取出组织类型
		    int orgType = cursor.getInt(cursor.getColumnIndex(Organization.TYPE));
		    // 单位
		    if (orgType == Organization.TYPE_CUSTOM) {
//		     if (orgType == Organization.TYPE_WORK) {
		     String organization = cursor.getString(cursor.getColumnIndex(Organization.COMPANY));
		     String jobtitle = cursor.getString(cursor.getColumnIndex(Organization.TITLE));
		     String department = cursor.getString(cursor.getColumnIndex(Organization.DEPARTMENT));
		     
		     person.organization = organization==null?"":organization;
			    person.jobtitle = jobtitle==null?"":jobtitle;
			    person.department = department==null?"":department;
		    }
		   }
		   // 获取网站信息
		   if (Website.CONTENT_ITEM_TYPE.equals(mimetype)) {
		    // 取出组织类型
		    int webType = cursor.getInt(cursor.getColumnIndex(Website.TYPE));
		    // 主页
		    if (webType == Website.TYPE_CUSTOM) {
		     
		     UrlBean url = new UrlBean();
		     url.url = cursor.getString(cursor.getColumnIndex(Website.URL));
		     url.label = cursor.getString(cursor.getColumnIndex(Website.LABEL));
		     person.url.add(url);
		    }
		    // 主页
		    else if (webType == Website.TYPE_HOME) {
		     
		     UrlBean url = new UrlBean();
		     url.url = cursor.getString(cursor.getColumnIndex(Website.URL));
		     url.label = cursor.getString(cursor.getColumnIndex(Website.LABEL));
		     person.url.add(url);
		    }

		    // 个人主页
		    if (webType == Website.TYPE_HOMEPAGE) {
		     
		     UrlBean url = new UrlBean();
		     url.url = cursor.getString(cursor.getColumnIndex(Website.URL));
		     url.label = cursor.getString(cursor.getColumnIndex(Website.LABEL));
		     person.url.add(url);
		    }
		    // 工作主页
		    if (webType == Website.TYPE_WORK) {
		     String workPage = cursor.getString(cursor.getColumnIndex(Website.URL));
		     
		     UrlBean url = new UrlBean();
		     url.url = cursor.getString(cursor.getColumnIndex(Website.URL));
		     url.label = cursor.getString(cursor.getColumnIndex(Website.LABEL));
		     person.url.add(url);
		    }
		   }
		   // 查找通讯地址
		   if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)) {
		    // 取出邮件类型
		    int postalType = cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE));
		    // 单位通讯地址
		    if (postalType == StructuredPostal.TYPE_WORK) {
		     String street = cursor.getString(cursor.getColumnIndex(StructuredPostal.STREET));
		     String ciry = cursor.getString(cursor.getColumnIndex(StructuredPostal.CITY));
		     String box = cursor.getString(cursor.getColumnIndex(StructuredPostal.POBOX));
		     String area = cursor.getString(cursor.getColumnIndex(StructuredPostal.NEIGHBORHOOD));
		     String state = cursor.getString(cursor.getColumnIndex(StructuredPostal.REGION));
		     String zip = cursor.getString(cursor.getColumnIndex(StructuredPostal.POSTCODE));
		     String country = cursor.getString(cursor.getColumnIndex(StructuredPostal.COUNTRY));
		     
		     AddressBean address = new AddressBean();
		     address.address = street+""+ciry+""+box+""+area+""+state+""+zip+""+country;
		     address.label = "工作";
		     person.address.add(address);
		    }
		    // 住宅通讯地址
		    if (postalType == StructuredPostal.TYPE_HOME) {
		     String homeStreet = cursor.getString(cursor.getColumnIndex(StructuredPostal.STREET));
		     String homeCity = cursor.getString(cursor.getColumnIndex(StructuredPostal.CITY));
		     String homeBox = cursor.getString(cursor.getColumnIndex(StructuredPostal.POBOX));
		     String homeArea = cursor.getString(cursor.getColumnIndex(StructuredPostal.NEIGHBORHOOD));
		     String homeState = cursor.getString(cursor.getColumnIndex(StructuredPostal.REGION));
		     String homeZip = cursor.getString(cursor.getColumnIndex(StructuredPostal.POSTCODE));
		     String homeCountry = cursor.getString(cursor.getColumnIndex(StructuredPostal.COUNTRY));
		     
		     AddressBean address = new AddressBean();
		     address.address = homeStreet+""+homeCity+""+homeBox+""+homeArea+""+homeState+""+homeZip+""+homeCountry;
		     address.label = "住宅";
		     person.address.add(address);
		    }
		    // 其他通讯地址
		    if (postalType == StructuredPostal.TYPE_OTHER) {
		     String otherStreet = cursor.getString(cursor.getColumnIndex(StructuredPostal.STREET));
		     String otherCity = cursor.getString(cursor.getColumnIndex(StructuredPostal.CITY));
		     String otherBox = cursor.getString(cursor.getColumnIndex(StructuredPostal.POBOX));
		     String otherArea = cursor.getString(cursor.getColumnIndex(StructuredPostal.NEIGHBORHOOD));
		     String otherState = cursor.getString(cursor.getColumnIndex(StructuredPostal.REGION));
		     String otherZip = cursor.getString(cursor.getColumnIndex(StructuredPostal.POSTCODE));
		     String otherCountry = cursor.getString(cursor.getColumnIndex(StructuredPostal.COUNTRY));
		     
		     AddressBean address = new AddressBean();
		     address.address = otherStreet+""+otherCity+""+otherBox+""+otherArea+""+otherState+""+otherZip+""+otherCountry;
		     address.label = "其他";
		     person.address.add(address);
		    }
		   }
		  }
		  cursor.close();
//		   Logger.i( contactData.toString());
		  Gson gson = new Gson();
			String json = gson.toJson(persons);
			Logger.i(json);
		}
	
}
