package config;

import java.util.List;
import java.util.Properties;

import org.apache.http.client.CookieStore;

import pomelo.PomeloClient;

import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.utils.L;

import service.QYEnterService;
import service.T9Service;
import tools.AppContext;
import tools.AppException;
import tools.ImageCacheUtil;
import tools.Logger;
import tools.StringUtils;

import bean.ContactBean;
import bean.UserEntity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.view.WindowManager;

public class MyApplication extends AppContext {
	private static MyApplication mApplication;
	
	private NotificationManager mNotificationManager;
	
	private boolean login = false;	//登录状态
	private String loginUid = "0";	//登录用户的id
	
	private List<ContactBean> contactBeanList;
	
	private PomeloClient pomeloClient;
		
	public List<ContactBean> getContactBeanList() {
		return contactBeanList;
	}
	public void setContactBeanList(List<ContactBean> contactBeanList) {
		this.contactBeanList = contactBeanList;
	}
	
	public synchronized static MyApplication getInstance() {
		return mApplication;
	}
	
	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}
	
	private WindowManager.LayoutParams wmParams=new WindowManager.LayoutParams();
	public WindowManager.LayoutParams getMywmParams(){
		return wmParams;
	}
	
	public void setPolemoClient(PomeloClient pomeloClient) {
		this.pomeloClient = pomeloClient;
	}
	
	public PomeloClient getPolemoClient() {
		return this.pomeloClient;
	}
	
	public void onCreate() {
		mApplication = this;
		Logger.getLogger().setTag("MyContact");
		Intent startService = new Intent(MyApplication.this, T9Service.class);
		startService(startService);
		ImageCacheUtil.init(this);
		Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		L.enableLogging();
		Logger.setDebug(true);
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		CookieStore cookieStore = new PersistentCookieStore(this);  
		QYRestClient.getIntance().setCookieStore(cookieStore);
		
//        Intent service = new Intent(this, QYEnterService.class);
//		startService(service);
	}
	
	
	@Override
	public void onTerminate() {
		Logger.i("ter");
		super.onTerminate();
	}
	/**
	 * 用户是否登录
	 * @return
	 */
	public boolean isLogin() {
		try {
			String loginStr = getProperty("user.login");
			if (StringUtils.empty(loginStr)) {
				login = false;
			}
			else {
				login = (loginStr.equals("1")) ? true : false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return login;
	}

	/**
	 * 保存登录信息
	 * @param username
	 * @param pwd
	 */
	@SuppressWarnings("serial")
	public void saveLoginInfo(final UserEntity user) {
		this.loginUid = user.openid;
		this.login = true;
		Logger.i(user.headimgurl);
		setProperties(new Properties(){
			{
				setProperty("user.login","1");
				setProperty("user.uid", user.openid);
				setProperty("user.name", user.nickname);
				setProperty("user.face", user.headimgurl);
				setProperty("user.hashcode", user.hash);
				setProperty("user.sign", user._sign);
			}
		});		
	}

	/**
	 * 获取登录用户id
	 * @return
	 */
	public String getLoginUid() {
		return (getProperty("user.uid"));
	}
	
	public String getLoginHashCode() {
		return (getProperty("user.hashcode"));
	}
	
	public String getLoginSign() {
		return (getProperty("user.sign"));
	}

	/**
	 * 获取登录信息
	 * @return
	 */
	public UserEntity getLoginInfo() {		
		UserEntity lu = new UserEntity();		
		lu.openid = (getProperty("user.uid"));
		lu.nickname = (getProperty("user.name"));
		lu.headimgurl = (getProperty("user.face"));
		return lu;
	}
	
	public String getNickname() {		
		return (getProperty("user.name"));
	}
	
	public String getUserAvatar() {		
		return (getProperty("user.face"));
	}
	
	/**
	 * 退出登录
	 */
	public void setUserLogout() {
		this.login = false;
		setProperties(new Properties(){
			{
				setProperty("user.login","0");
			}
		});	
	}
	
	public boolean isNeedCheckLogin() {
		try {
			String loginStr = getProperty("user.needchecklogin");
			if (StringUtils.empty(loginStr)) {
				return false;
			}
			else {
				return (loginStr.equals("1")) ? true : false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setNeedCheckLogin() {
		setProperties(new Properties(){
			{
				setProperty("user.needchecklogin","1");
			}
		});
	}
	
	public void saveNotiWhen(final String when) {
		setProperties(new Properties(){
			{
				setProperty("noti.when",when);
			}
		});
	}
	
	public String getNotiWhen() {
		try {
			String loginStr = getProperty("noti.when");
			if (StringUtils.empty(loginStr)) {
				return "0";
			}
			else {
				return loginStr;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "0";
	}
	
}
