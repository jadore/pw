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

public class PhoneViewEntity extends Entity{
	public String qun_id;//": "110",
	public String wechat_id;//": "oYTgBuJw_Om72qDAV4pX1-EW1Ajk",
	public String code;//": "804e638cdf7d",
	public String title;//": "华广校友圈通讯录",
	public String content;//": "华广创业，高管，工作的朋友加入进来吧。",
	public String privacy;//": "0",
	public String dateline;//": "1384959134",
	public String hits;//": "272",
	public String type;//": "14",
	public String question;//": "",
    public List<CardIntroEntity> members = new ArrayList<CardIntroEntity>();
	public String added;//": "2",
	public String is_admin;//": 0,
	public String readable;//": 1
	public String creator;
	public String link;
	
	
	public static PhoneViewEntity parse(String res) throws IOException, AppException {
		PhoneViewEntity data = new PhoneViewEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				data.wechat_id = info.getString("wechat_id");
				data.code = info.getString("code");
				data.title = info.getString("title");
				data.content = info.getString("content");
				data.privacy = info.getString("privacy");
				JSONObject creatorObj = new JSONObject(info.getString("creator"));
				data.creator = creatorObj.getString("nickname");
				data.link = info.getString("link");
				data.added = info.getString("added");
				data.is_admin = info.getString("is_admin");
				data.readable = info.getString("readable");
				JSONArray ownedArr = info.getJSONArray("members");
				for (int i=0;i<ownedArr.length();i++) {
					CardIntroEntity phone = CardIntroEntity.parseFromViewMembers(ownedArr.getJSONObject(i),"通讯录成员");
					data.members.add(phone);
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
