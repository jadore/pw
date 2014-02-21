package bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import config.CommonValue;

import tools.AppException;
import tools.Logger;

public class PhoneListEntity extends Entity{
	public List<PhoneIntroEntity> owned = new ArrayList<PhoneIntroEntity>();
	public List<PhoneIntroEntity> joined = new ArrayList<PhoneIntroEntity>();
	
	public static PhoneListEntity parse(String res) throws IOException, AppException {
		PhoneListEntity data = new PhoneListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				JSONArray ownedArr = info.getJSONArray("owned");
				for (int i=0;i<ownedArr.length();i++) {
					PhoneIntroEntity phone = PhoneIntroEntity.parsePhonebookAndActivity(ownedArr.getJSONObject(i), CommonValue.PhoneSectionType.OwnedSectionType);
					data.owned.add(phone);
				}
				JSONArray joinedArr = info.getJSONArray("joined");
				for (int i=0;i<joinedArr.length();i++) {
					PhoneIntroEntity phone = PhoneIntroEntity.parsePhonebookAndActivity(joinedArr.getJSONObject(i), CommonValue.PhoneSectionType.JoinedSectionType);
					data.joined.add(phone);
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
