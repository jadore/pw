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

public class CardListEntity extends Entity {
	
	public List<CardIntroEntity> owned = new ArrayList<CardIntroEntity>();
	
	public static CardListEntity parse(String res) throws IOException, AppException {
		CardListEntity data = new CardListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONArray ownedArr = js.getJSONArray("info");
				for (int i=0;i<ownedArr.length();i++) {
					CardIntroEntity phone = CardIntroEntity.parse(ownedArr.getJSONObject(i), CommonValue.CardSectionType.OwnedSectionType);
					data.owned.add(phone);
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
