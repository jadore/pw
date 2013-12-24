package bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;

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
}
