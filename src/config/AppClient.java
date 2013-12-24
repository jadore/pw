package config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Base64;
import bean.ActivityIntroEntity;
import bean.ActivityListEntity;
import bean.ActivityViewEntity;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.CodeEntity;
import bean.Entity;
import bean.FriendCardListEntity;
import bean.MessageListEntity;
import bean.MessageUnReadEntity;
import bean.PhoneListEntity;
import bean.PhoneViewEntity;
import bean.RecommendListEntity;
import bean.UserEntity;
import tools.AppContext;
import tools.AppException;
import tools.AppManager;
import tools.Logger;

public class AppClient {
	private static final int[] key = {16,4,3,19,20, 10, 1, 7, 15, 21, 2, 0, 17, 18, 5, 8, 6, 12, 14, 9, 13, 11};
	
    public interface ClientCallback{
        abstract void onSuccess(Entity data);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
    }
    
    protected static String decode(String origin) throws AppException {
		String target;
		try {
			origin = origin.replace("O", "#");
			origin = origin.replace("0", "O");
			origin = origin.replace("I", "0");
			origin = origin.replace("#", "I");
			byte[] b = Base64.decode(origin, Base64.DEFAULT);
			byte[] bnew = new byte[b.length];
			for (int i=0;i<b.length;i++) {
				bnew[(int)(Math.floor(i/key.length)*key.length)+key[i%key.length]] = b[i];
			}
			target = new String(bnew);
			Pattern regex = Pattern.compile("\\{(.*)\\}\\d*");
			Matcher matcher = regex.matcher(target);
			if (matcher.find()) {
				target = String.format("{%s}", matcher.group(1));
			}
			return target;
		} catch (Exception e) {
			Logger.i(origin);
			e.printStackTrace();
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.json(e);
		}
	}
    
    private static void saveCache(MyApplication appContext, String key, Entity entity) {
    	appContext.saveObject(entity, String.format("%s-%s", key, appContext.getLoginUid()));
    }
	
    public static void getVertifyCode(final MyApplication appContext, String mobile, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("phone", mobile);
		QYRestClient.post("user/send", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CodeEntity data = CodeEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
    
	public static void vertifiedCode(final MyApplication appContext, String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("user/login", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					UserEntity data = UserEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void autoLogin(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("user/autologin", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					UserEntity data = UserEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void setUser(String baiduUserId, String baiduChannelId) {
		RequestParams params = new RequestParams();
		params.add("client_browser", "android");
		try {
			params.add("client_version", AppManager.getAppManager().currentActivity().getPackageManager().getPackageInfo(AppManager.getAppManager().currentActivity().getPackageName(), 0).versionCode+"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.add("client_push", "android");
		params.add("push_user_id", baiduUserId);
		params.add("push_channel_id", baiduChannelId);
		params.add("push_device_type", "3");
		QYRestClient.post("user/set", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
			}
		});
	}
	
	public static void getPhoneList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("phonebook/lists", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					PhoneListEntity data = PhoneListEntity.parse(decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.PhoneList, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getPhoneView(final MyApplication appContext, final String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("phonebook/view", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					PhoneViewEntity data = PhoneViewEntity.parse(decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.PhoneView+"-"+code, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void setPhonebookRole(final MyApplication appContext,String code, String openid, String role,final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		params.add("openid", openid);
		params.add("role", role);
		QYRestClient.post("phonebook/setrole", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CodeEntity data = CodeEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void setPhonebookPassmember(final MyApplication appContext,String code, String openid, String state, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		params.add("openid", openid);
		params.add("state", state);
		QYRestClient.post("phonebook/passmember", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CodeEntity data = CodeEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getActivityList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("activity/lists", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					ActivityListEntity data = ActivityListEntity.parse(decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.ActivityList, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getActivityView(final MyApplication appContext, final String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("activity/view", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					ActivityViewEntity data = ActivityViewEntity.parse(decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.ActivityView+"-"+code, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getCardList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("card/lists", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CardListEntity data = CardListEntity.parse(decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.CardList, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getCard(final MyApplication appContext, String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("card/info", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CardIntroEntity data = CardIntroEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getFriendCard(final MyApplication appContext, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		QYRestClient.post("card/friend", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					FriendCardListEntity data = FriendCardListEntity.parse(decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.FriendCardList, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void followCard(final MyApplication appContext, String openid, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("openid", openid);
		QYRestClient.post("card/follow", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CodeEntity data = CodeEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getMessageList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("message/index", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					MessageListEntity data = MessageListEntity.parse(decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.MessageList, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getUnReadMessage(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("message/getnewsnumber", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					MessageUnReadEntity data = MessageUnReadEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void setMessageRead(MyApplication appContext) {
		QYRestClient.post("message/read", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {

			}
		});
	}
	
	public static void getRecommendList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("recommend/lists", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					RecommendListEntity data = RecommendListEntity.parse(decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
}
