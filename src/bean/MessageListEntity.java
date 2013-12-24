package bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import config.CommonValue;

public class MessageListEntity extends Entity {
	
	public List<MessageEntity> messages = new ArrayList<MessageEntity>();
	
	public static MessageListEntity parse(String res) throws IOException, AppException {
		MessageListEntity data = new MessageListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONArray ownedArr = js.getJSONArray("info");
				for (int i=0;i<ownedArr.length();i++) {
					MessageEntity phone = MessageEntity.parse(ownedArr.getJSONObject(i));
					data.messages.add(phone);
				}
			}
			else {
				data.error_code = 11;
				data.message = js.getString("info");
			}
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}

}
