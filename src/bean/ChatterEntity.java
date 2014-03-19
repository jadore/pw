/**
 * QYdonal
 */
package bean;

import java.io.IOException;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;

/**
 * QY
 *
 * @author donal
 *
 */
public class ChatterEntity extends Entity {
	
	public String openid;
	public String assist_openid;
	public String avatar;
	public String sex;
	public String state;
	public String weibo;
	public String realname;
	public String font_family;
	public String pinyin;
	public String phone;
	public String email;
	public String department;
	public String position;
	public String birthday;
	public String address;
	public String hits;
	public String certified;
	public String privacy;
	public String supply;
	public String needs;
	public String intro;
	public String wechat;
	public String nickname;
	public String link;
	public String code;
	public String isfriend;
	public String chathash;
	public String phone_display;
	
	
	public static ChatterEntity parse(String res) throws AppException {
		ChatterEntity data = new ChatterEntity();
		try {
			JSONObject js = new JSONObject(res);
			if (js.getInt("status") == 1) {
				JSONObject info = js.getJSONObject("info");
		        data.avatar 		 = info.getString("avatar");
		        data.realname        = info.getString("realname");
		        data.nickname        = info.getString("nickname");
			}
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
}
