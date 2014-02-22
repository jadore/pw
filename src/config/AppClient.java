package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.Header;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.os.Environment;
import baidupush.Utils;
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
import bean.Update;
import bean.UserEntity;
import bean.WebContent;
import tools.AppException;
import tools.AppManager;
import tools.DecodeUtil;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;

public class AppClient {
	
    public interface ClientCallback{
        abstract void onSuccess(Entity data);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
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
					CodeEntity data = CodeEntity.parse(DecodeUtil.decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("请再次点击获取验证码");
			}
		});
	}
    
	public static void vertifiedCode(final MyApplication appContext, String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("user/wechatlogin", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					UserEntity data = UserEntity.parse(DecodeUtil.decode(new String(content)));
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
	
	public static void vertifiedCode(final MyApplication appContext, String code, String mobile, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		params.add("phone", mobile);
		QYRestClient.post("user/login", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					UserEntity data = UserEntity.parse(DecodeUtil.decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					Logger.i(e);
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
//		RequestParams params = new RequestParams();
//		params.add("key", "18967680777");
//		QYRestClient.post("user/adminlogin", params, new AsyncHttpResponseHandler() {
		QYRestClient.post("user/autologin", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					Logger.i(DecodeUtil.decode(new String(content)));
					UserEntity data = UserEntity.parse(DecodeUtil.decode(new String(content)));
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
	
	public static void Logout(final MyApplication appContext) {
		QYRestClient.post("user/logout", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try {
					Logger.i(DecodeUtil.decode(new String(content)));
				} catch (AppException e) {
					Logger.i(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
			}
		});
	}
	
	public static void setUser(final Context context, String baiduUserId, String baiduChannelId) {
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
				Utils.setBind(context, true);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
			}
		});
	}
	
	public static void getPhoneList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("phonebook/lists"+"?_sign="+appContext.getLoginHash(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					PhoneListEntity data = PhoneListEntity.parse(DecodeUtil.decode(new String(content)));
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
					PhoneViewEntity data = PhoneViewEntity.parse(DecodeUtil.decode(new String(content)));
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
					CodeEntity data = CodeEntity.parse(DecodeUtil.decode(new String(content)));
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
					CodeEntity data = CodeEntity.parse(DecodeUtil.decode(new String(content)));
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
		QYRestClient.post("activity/lists"+"?_sign="+appContext.getLoginHash(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					ActivityListEntity data = ActivityListEntity.parse(DecodeUtil.decode(new String(content)));
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
					ActivityViewEntity data = ActivityViewEntity.parse(DecodeUtil.decode(new String(content)));
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
		QYRestClient.post("card/lists"+"?_sign="+appContext.getLoginHash(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CardListEntity data = CardListEntity.parse(DecodeUtil.decode(new String(content)));
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
					CardIntroEntity data = CardIntroEntity.parse(DecodeUtil.decode(new String(content)));
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
//					Logger.i(new String(content));
//					Logger.i(DecodeUtil.decode(new String(content)));
					FriendCardListEntity data = FriendCardListEntity.parse(DecodeUtil.decode(new String(content)));
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
	
	public static void getChatFriendCard(Context context, final MyApplication appContext, final String page, String keyword, String count, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		if (!StringUtils.isEmpty(page)) {
			params.add("page", page);
		}
		if (!StringUtils.isEmpty(keyword)) {
			params.add("keyword", keyword);
		}
		if (!StringUtils.isEmpty(count)) {
			params.add("count", count);
		}
		Logger.i("s");
		QYRestClient.post(context, "card/friendlist"+"?_sign="+appContext.getLoginHash(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				Logger.i("g");
				try{
					FriendCardListEntity data = FriendCardListEntity.parseF(DecodeUtil.decode(new String(content)));
					if (!StringUtils.isEmpty(page) && page.equals("1")) {
						saveCache(appContext, CommonValue.CacheKey.FriendCardList1, data);
					}
					Logger.i("e");
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
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
					CodeEntity data = CodeEntity.parse(DecodeUtil.decode(new String(content)));
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
					MessageListEntity data = MessageListEntity.parse(DecodeUtil.decode(new String(content)));
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
					MessageUnReadEntity data = MessageUnReadEntity.parse(DecodeUtil.decode(new String(content)));
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
					RecommendListEntity data = RecommendListEntity.parse(DecodeUtil.decode(new String(content)));
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
	
	public static void syncContact(final MyApplication appContext, String data, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		param.add("data", data);
		QYRestClient.post("contact/sync"+"?_sign="+appContext.getLoginHash(), param, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					Logger.i(DecodeUtil.decode(new String(content)));
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
	
	public static void sendFeedback(final MyApplication appContext, String data, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		data += " | client_browser : android |";
		try {
			data += " | client_version :" + AppManager.getAppManager().currentActivity().getPackageManager().getPackageInfo(AppManager.getAppManager().currentActivity().getPackageName(), 0).versionCode+" |";
		} catch (Exception e) {
			e.printStackTrace();
		}
		data += " |client_push : android |";
		param.add("message", data);
		QYRestClient.post("feedback/send"+"?_sign="+appContext.getLoginHash(), param, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					Logger.i(DecodeUtil.decode(new String(content)));
					callback.onSuccess(new Entity() {
						private static final long serialVersionUID = 1L;
					});
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
	
	public static void update(final MyApplication appContext, String data, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		QYRestClient.post("update/check", param, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					Update data = Update.parse(DecodeUtil.decode(new String(content)));
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
	
	public interface WebCallback{
        abstract void onSuccess(int type, Entity data, String key);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
    }
	
	public static void loadURL(Context context, final MyApplication appContext, final String url, final WebCallback callback) {
//		RequestParams params = new RequestParams();
//		params.add("_sign", appContext.getLoginHash());
		QYRestClient.getWeb(context, url+"?_sign="+appContext.getLoginHash(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					String data = new String(content);
					String md5 = MD5Util.getMD5String(content);
					String key = String.format("%s-%s", MD5Util.getMD5String(url), appContext.getLoginUid());
					WebContent dc = (WebContent) appContext.readObject(key);
					WebContent con = new WebContent();
					con.text = data;
					con.md5 = md5;
					saveCache(appContext, MD5Util.getMD5String(url), con);
					if(dc == null){
						callback.onSuccess(0, con, MD5Util.getMD5String(url));
					}
					else {
//						Logger.i(dc.md5);
//						Logger.i(con.md5);
//						Logger.i(dc.text);
//						Logger.i(con.text);
						if (dc.md5.equals(con.md5)) {
							callback.onSuccess(2, con, MD5Util.getMD5String(url));
						}
						else {
							callback.onSuccess(1, con, MD5Util.getMD5String(url));
						}
					}
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure(url);
			}
		});
	}
	
	private void writeHtml2File(String fileName, String content) {
		String savePath;
		String htmlFilePath = "";
		String storageState = Environment.getExternalStorageState();
		if(storageState.equals(Environment.MEDIA_MOUNTED)){
			savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qy/";
			File file = new File(savePath);
			if(!file.exists()){
				file.mkdirs();
			}
			htmlFilePath = savePath + fileName;
			File ApkFile = new File(htmlFilePath);
			try {
				FileOutputStream outStream = new FileOutputStream(ApkFile);
				outStream.write(content.getBytes());
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public interface FileCallback{
        abstract void onSuccess(String filePath);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
    }
	public static void downFile(Context context, final MyApplication appContext, final String url, final String format, final FileCallback callback) {
		QYRestClient.downImage(appContext, url, null, new BinaryHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
				Logger.i(statusCode+"");
				String storageState = Environment.getExternalStorageState();	
				String savePath = null;
				if(storageState.equals(Environment.MEDIA_MOUNTED)){
					savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qy/";
					File dir = new File(savePath);
					if(!dir.exists()){
						dir.mkdirs();
					}
				}
				String md5FilePath = savePath + MD5Util.getMD5String(url) + format;
//				File ApkFile = new File(md5FilePath);
//				if(ApkFile.exists()){
//					ApkFile.delete();
//					return;
//				}
				File tmpFile = new File(md5FilePath);
				try {
					FileOutputStream fos = new FileOutputStream(tmpFile);
					fos.write(binaryData);
					fos.close();
					callback.onSuccess(md5FilePath);
				} catch (FileNotFoundException e) {
					callback.onError(e);
					e.printStackTrace();
				} catch (IOException e) {
					callback.onError(e);
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] binaryData,
					Throwable error) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
}
