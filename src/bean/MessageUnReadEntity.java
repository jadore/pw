package bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;

public class MessageUnReadEntity extends Entity{
	public String news;
	
	public static MessageUnReadEntity parse(String res) throws IOException, AppException {
		MessageUnReadEntity data = new MessageUnReadEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				data.news = info.getString("news");
			}
			else {
				if (!js.isNull("error_code")) {
					data.error_code = js.getInt("error_code");
				}
				data.message = js.getString("info");
			}
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
}
