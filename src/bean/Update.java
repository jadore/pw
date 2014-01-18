/**
 * 
 */
package bean;

import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;

/**
 * @author Donal Tong 
 * momoka
 * 2013-1-12
 */
public class Update extends Entity {
	public final static String UTF8 = "UTF-8";
	
	private int versionCode;
	private String versionName;
	private String appName;
	private String downloadUrl;
	private String updateLog;
	private int minVersion;
	private Result result;
	
	public int getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getUpdateLog() {
		return updateLog;
	}
	public void setUpdateLog(String updateLog) {
		this.updateLog = updateLog;
	}
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public int getMinVersion() {
		return minVersion;
	}
	public void setMinVersion(int minVersion) {
		this.minVersion = minVersion;
	}
	public Result getResult() {
		return result;
	}
	public void setResult(Result result) {
		this.result = result;
	}
	/**
	 * @param postRequest
	 * @return
	 */
	public static Update parse(String postRequest) throws AppException{
		Update update = new Update();
		Result result = new Result();
		try{
			JSONObject js = new JSONObject(postRequest);
			if(js.getInt("result") == 1){
				result.setError_code(Result.RESULT_OK);
				update.setVersionCode(js.getJSONObject("info").getInt("version_code"));
				update.setVersionName(js.getJSONObject("info").getString("version_name"));
				update.setDownloadUrl(js.getJSONObject("info").getString("app_url"));
				update.setUpdateLog(js.getJSONObject("info").getString("update_log"));
				update.setAppName(js.getJSONObject("info").getString("app_name"));
				update.setMinVersion(js.getJSONObject("info").getInt("min_version"));
				update.setResult(result);
			}else {
				result.setError_code(js.getInt("error_code"));
				result.setMessage(js.getString("message"));
				update.setResult(result);
			}
		}catch (JSONException e) {
			Logger.i(postRequest);
			throw AppException.json(e);
		}
		return update;
	}
	
	
	
}
