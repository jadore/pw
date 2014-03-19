/**
 * QYdonal
 */
package bean;

import im.bean.IMMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;
import config.CommonValue;
import config.MyApplication;

/**
 * QY
 *
 * @author donal
 *
 */
public class ChatHistoryListEntity extends Entity {
	public List<IMMessage> messages = new ArrayList<IMMessage>();
	
	public static ChatHistoryListEntity parse(String res) throws IOException, AppException {
		ChatHistoryListEntity data = new ChatHistoryListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONArray ownedArr = js.getJSONArray("info");
				for (int i=0;i<ownedArr.length();i++) {
					JSONObject message = ownedArr.getJSONObject(i);
					IMMessage imm = new IMMessage();
					imm.chatId = message.getString("chat_id");
					imm.roomId = message.getString("hash");
					imm.openId = message.getString("openid");
					imm.content = message.getString("message");
					imm.postAt = message.getString("post_at");
					imm.msgTime = imm.postAt;
					imm.msgStatus = IMMessage.JSBubbleMessageStatus.JSBubbleMessageStatusReaded;
					imm.msgType = imm.openId.equals(MyApplication.getInstance().getLoginUid())?
										IMMessage.JSBubbleMessageType.JSBubbleMessageTypeOutgoing:
										IMMessage.JSBubbleMessageType.JSBubbleMessageTypeIncoming;
					imm.mediaType = IMMessage.JSBubbleMediaType.JSBubbleMediaTypeText;
					data.messages.add(imm);
				}
			}
			else {
				if (!js.isNull("error_code")) {
					data.error_code = js.getInt("error_code");
				}
				data.message = js.getString("info");
			}
		} catch (JSONException e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
}
