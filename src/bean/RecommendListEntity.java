package bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import config.CommonValue;
import tools.AppException;
import tools.Logger;

public class RecommendListEntity extends Entity {
	public List<RecommendIntroEntity> recomments = new ArrayList<RecommendIntroEntity>();
	
	public static RecommendListEntity parse(String res) throws IOException, AppException {
		RecommendListEntity data = new RecommendListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONArray ownedArr = js.getJSONArray("info");
				for (int i=0;i<ownedArr.length();i++) {
					RecommendIntroEntity phone = RecommendIntroEntity.parse(ownedArr.getJSONObject(i));
					data.recomments.add(phone);
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
	
	public List<PhoneIntroEntity> squares = new ArrayList<PhoneIntroEntity>();
	public int next;
	public static RecommendListEntity parseSquare(String res) throws IOException, AppException {
		RecommendListEntity data = new RecommendListEntity();
		try {
			JSONObject js = new JSONObject(res);
//			if(js.getInt("status") == 1) {
//				data.error_code = Result.RESULT_OK;
//				JSONObject info = js.getJSONObject("info");
				JSONArray ownedArr = js.getJSONArray("data");
				for (int i=0;i<ownedArr.length();i++) {
					PhoneIntroEntity phone = PhoneIntroEntity.parsePhonebookAndActivity(ownedArr.getJSONObject(i), CommonValue.PhoneSectionType.OwnedSectionType);
					data.squares.add(phone);
				}
				data.next = js.getInt("next");
//			}
//			else {
//				if (!js.isNull("error_code")) {
//					data.error_code = js.getInt("error_code");
//				}
//				data.message = js.getString("info");
//			}
		} catch (JSONException e) {
			Logger.i(e);
			Logger.i(res);
			throw AppException.json(e);
		}
		return data;
	}
}
