package config;

import tools.AppManager;
import tools.ImageUtils;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.baseapp.R;

public class CommonValue {
	public static final String PackageName = "com.vikaa.mycontact";
	
	public static final String BASE_API = "http://pb.wc.m0.hk/api/";
	public static final String BASE_URL = "http://pb.wc.m0.hk";
	
	public static final String KEY_GUIDE_SHOWN = "preferences_guide_shown";
	
	public interface subTitle {
		String subtitle1 = "查看手机通讯录";
		String subtitle2 = "查看交换名片的朋友";
	}
	
	public interface CacheKey {
		String PhoneList = "PhoneList";
		String PhoneView = "PhoneView";
		String ActivityList = "ActivityList";
		String ActivityView = "ActivityView";
		String CardList = "CardList";
		String FriendCardList = "FriendCardList";
		String MessageList = "MessageList";
	}
	
	public interface LoginRequest {
		int LoginMobile = 1;
		int LoginWechat = 2;
	}
	
	// options
	public interface DisplayOptions {
		public DisplayImageOptions default_options = new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565)
				.showImageOnLoading(R.drawable.ic_launcher)
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher)
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) 
				.displayer(new BitmapDisplayer() {
					@Override
					public void display(Bitmap bitmap, ImageAware imageAware,
							LoadedFrom loadedFrom) {
						imageAware.setImageBitmap(bitmap);
					}
				})
				.build();
		
		public DisplayImageOptions avatar_options = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.ic_launcher)
		.showImageForEmptyUri(R.drawable.ic_launcher)
		.showImageOnFail(R.drawable.ic_launcher)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
		.displayer(new RoundedBitmapDisplayer(ImageUtils.dip2px(AppManager.getAppManager().currentActivity(), 30)))
		.build();
	}
	
	public interface PhoneSectionType {
		String MobileSectionType = "手机通讯录";
		String OwnedSectionType = "我发起的通讯录";
		String JoinedSectionType = "我参与的通讯录";
	}
	
	public interface IndexIntentKeyValue {
		String PhoneView = "phoneview";
		String ActivityView = "activityview";
		String CreateView = "createview";
	}
	
	public interface ActivitySectionType {
		String OwnedSectionType = "我发起的聚会";
		String JoinedSectionType = "我参与的聚会";
	}
	
	public interface CardSectionType {
		String OwnedSectionType = "我的名片";
	}
	
	public interface CardViewIntentKeyValue {
		String CardView = "cardview";
	}
	
	public interface CreateViewUrlAndRequest {
		String ContactCreateUrl = String.format("%s/index/create", BASE_URL);
		int ContactCreat = 1;
		String ActivityCreateUrl = String.format("%s/activity/create", BASE_URL);
		int ActivityCreateCreat = 2;
		String CardCreateUrl = String.format("%s/card/setting/id/0", BASE_URL);
		int CardCreat = 3;
	}
	
	public interface CreateViewJSType {
		int	goPhonebookView = 1;
		int	goPhonebookList = 2;
		int	goActivityView = 3;
		int	goActivityList = 4;
		int goCardView = 5;
	}
	
	public interface PhonebookViewIntentKeyValue {
		String SMS = "sms";
		int SMSRequest = 6;
		
		String SMSPersons = "sms_person";
		int SMSPersonRequest = 7;
	}
	
	public interface PhonebookViewUrlRequest {
		int editPhoneview = 4;
		int deletePhoneview = 5;
	}
	
	public interface PhonebookViewIsAdd {
		int No = 0;
	}
	
	public interface PhonebookViewIsReadable {
		int UnApply = -1;
		int Applying = 0;
		int Pass = 1;
		int Refuse = 2;
	}
	
	public interface PhonebookViewIsAdmin {
		int AdminNo = 0;
		int AdminYes = 1;
	}
	public interface PhonebookLimitRight {
		String PBprivacy_Yes = "1";
		
		String Admin_Yes = "1";
		
		String Add_No = "0";
		
		String PBreadable_Yes = "1"; 
		
		String Memreadable_Yes = "1"; 
		
		String Friend_No = "-1";
		String Friend_Wait = "0";
		String Frined_Yes =  "1";
		
		String RoleAdmin = "1";
		String RoleCreator = "2";
		String RolePublic = "0";
		String RoleNone = "9";
		
		String QunReadable_Yes = "1";
		String QunReadable_No = "2";
	}
}
