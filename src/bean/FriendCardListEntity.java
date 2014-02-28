package bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;

public class FriendCardListEntity extends Entity {
	public List<CardIntroEntity> bilateral = new ArrayList<CardIntroEntity>();
	public List<CardIntroEntity> friend = new ArrayList<CardIntroEntity>();
	public List<CardIntroEntity> follower = new ArrayList<CardIntroEntity>();
	
	public int status;
	
	public static FriendCardListEntity parse(String res) throws IOException, AppException {
//		Logger.i(res);
		FriendCardListEntity data = new FriendCardListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				JSONArray bilateralArray = info.getJSONArray("bilateral");
				for (int i=0;i<bilateralArray.length();i++) {
					CardIntroEntity phone = CardIntroEntity.parse(bilateralArray.getJSONObject(i), "");
					data.bilateral.add(phone);
				}
				JSONArray friendArray = info.getJSONArray("friend");
				for (int i=0;i<friendArray.length();i++) {
					CardIntroEntity phone = CardIntroEntity.parse(friendArray.getJSONObject(i), "");
					data.friend.add(phone);
				}
				JSONArray followerArray = info.getJSONArray("follower");
				for (int i=0;i<followerArray.length();i++) {
					CardIntroEntity phone = CardIntroEntity.parse(followerArray.getJSONObject(i), "");
					data.follower.add(phone);
				}
			}
			else {
				data.error_code = 11;
				data.message = js.getString("info");
			}
		} catch (JSONException e) {
			Logger.i(e);
//			Logger.i(res);
			throw AppException.json(e);
		}
		return data;
	}
	
	public List<CardIntroEntity> u = new ArrayList<CardIntroEntity>();
	public int ne;
	public static FriendCardListEntity parseF(String res) throws IOException, AppException {
//		Logger.i(res);
		FriendCardListEntity data = new FriendCardListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				JSONArray bilateralArray = info.getJSONArray("u");
				for (int i=0;i<bilateralArray.length();i++) {
					CardIntroEntity phone = CardIntroEntity.parseFC(bilateralArray.getJSONObject(i), "");
					data.u.add(phone);
				}
				data.ne = -1;
				if (!info.isNull("ne")) {
					data.ne = info.getInt("ne");
				}
				
				Logger.i(data.ne+"");
			}
			else {
				if (!js.isNull("error_code")) {
					data.error_code = js.getInt("error_code");
				}
				data.message = js.getString("info");
			}
		} catch (JSONException e) {
			Logger.i(e);
//			Logger.i(res);
			throw AppException.json(e);
		}
		return data;
	}
	
}
