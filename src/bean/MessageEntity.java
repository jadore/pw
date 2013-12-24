package bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils.StringSplitter;

import tools.AppException;
import tools.StringUtils;

public class MessageEntity extends Entity {
	public String message_id;
	public String hash;
	public String type;
	public String openid;
	public String from_openid;
	public String message;
	public String created_at;
	public String is_read;
	public String params;
	
	public static MessageEntity parse(JSONObject info) throws IOException, AppException {
		MessageEntity data = new MessageEntity();
		try {
			data.hash = info.getString("hash");
			data.type = info.getString("type");
			data.message = info.getString("message");
			data.created_at = StringUtils.friendly_time(StringUtils.phpLongtoDate(info.getString("created_at")));
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
    	
}
