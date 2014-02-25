/**
 * QYdonal
 */
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

/**
 * QY
 *
 * @author donal
 *
 */
public class FamilyListEntity extends Entity {
	public List<PhoneIntroEntity> family = new ArrayList<PhoneIntroEntity>();
	public List<PhoneIntroEntity> clan = new ArrayList<PhoneIntroEntity>();
	
	public static FamilyListEntity parse(String res) throws IOException, AppException {
		FamilyListEntity data = new FamilyListEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				JSONArray ownedArr = info.getJSONArray("family");
				for (int i=0;i<ownedArr.length();i++) {
					PhoneIntroEntity phone = PhoneIntroEntity.parsePhonebookAndActivity(ownedArr.getJSONObject(i), CommonValue.FamilySectionType.FamilySectionType);
					data.family.add(phone);
				}
				JSONArray joinedArr = info.getJSONArray("clan");
				for (int i=0;i<joinedArr.length();i++) {
					PhoneIntroEntity phone = PhoneIntroEntity.parsePhonebookAndActivity(joinedArr.getJSONObject(i), CommonValue.FamilySectionType.ClanSectionType);
					data.clan.add(phone);
				}
			}
			else {
				if (!js.isNull("error_code")) {
					data.error_code = js.getInt("error_code");
				}
				data.message = js.getString("info");
			}
		} catch (JSONException e) {
			Logger.i(res);
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
}
