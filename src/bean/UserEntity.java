package bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;

public class UserEntity extends Entity{
	public String openid;
	public String nickname;
	public String sex;
	public String headimgurl;
	public String hash;
	public String _sign;
	public String username;
	
	public String credit;
	public String news;
	public String card;
	public String deg1;
	public String deg2;
	
	public static UserEntity parse(String res) throws IOException, AppException {
		Logger.i(res);
		UserEntity data = new UserEntity();
		try {
			JSONObject js = new JSONObject(res);
			if (js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				data.openid = info.getString("openid");
				data.sex = info.getString("sex");
				data.headimgurl = info.getString("headimgurl");
				data.nickname = info.getString("nickname");
				data.hash = info.getString("hash");
				if (!info.isNull("_sign")) {
					data._sign = info.getString("_sign");
				}
				if (!info.isNull("username")) {
					data.username = info.getString("username");
				}
				if (!info.isNull("credit")) {
					data.credit = info.getString("credit");
				}
				if (!info.isNull("message")) {
					data.news = info.getJSONObject("message").getString("news");
					data.card = info.getJSONObject("message").getString("card");
				}
				if (!info.isNull("friend")) {
					data.deg1 = info.getJSONObject("friend").getString("deg1");
					data.deg2 = info.getJSONObject("friend").getString("deg2");
				}
			}
			else {
				if (!js.isNull("error_code")) {
					data.error_code = js.getInt("error_code");
				}
				data.message = js.getString("info");
			}
			
		} catch (JSONException e) {
			Logger.i(res);
			throw AppException.json(e);
		}
		return data;
	}
}
