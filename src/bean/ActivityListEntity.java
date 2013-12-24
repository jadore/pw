package bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;
import config.CommonValue;

public class ActivityListEntity extends Entity {
	public List<ActivityIntroEntity> owned = new ArrayList<ActivityIntroEntity>();
	public List<ActivityIntroEntity> joined = new ArrayList<ActivityIntroEntity>();
	
	public static ActivityListEntity parse(String res) throws IOException, AppException {
		ActivityListEntity data = new ActivityListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				JSONArray ownedArr = info.getJSONArray("owned");
				for (int i=0;i<ownedArr.length();i++) {
					ActivityIntroEntity phone = ActivityIntroEntity.parse(ownedArr.getJSONObject(i), CommonValue.ActivitySectionType.OwnedSectionType);
					data.owned.add(phone);
				}
				JSONArray joinedArr = info.getJSONArray("joined");
				for (int i=0;i<joinedArr.length();i++) {
					ActivityIntroEntity phone = ActivityIntroEntity.parse(joinedArr.getJSONObject(i), CommonValue.ActivitySectionType.JoinedSectionType);
					data.joined.add(phone);
				}
			}
			else {
				data.error_code = 11;
				data.message = js.getString("info");
			}
		} catch (JSONException e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
}
