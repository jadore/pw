package bean;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;
import tools.StringUtils;

public class ActivityViewEntity  extends Entity{
	public String openid;
	public String code;//": "804e638cdf7d",
	public String title;//": "华广校友圈通讯录",
	public String content;//": "华广创业，高管，工作的朋友加入进来吧。",
	public String begin_at;
	public String end_at;
	public String city_code;
	public String address;
	public String privacy;//": "0",
	public String dateline;//": "1384959134",
	public String hits;//": "272",
	public String type;//": "14",
    public List<CardIntroEntity> members = new ArrayList<CardIntroEntity>();
	public String added = "0";//": "2",
	public String is_admin = "0";//": 0,
	public String readable = "0";//": 1
	public String link;
	public String creator;
	
	public static ActivityViewEntity parse(String res) throws IOException, AppException {
		ActivityViewEntity data = new ActivityViewEntity();
		try {
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				data.openid = info.getString("openid");
				data.code = info.getString("code");
				data.title = info.getString("title");
				data.content = info.getString("content");
				data.begin_at = StringUtils.phpLongtoDate(info.getString("begin_at"), new SimpleDateFormat("yyyy-MM-dd"));;
				data.end_at = StringUtils.phpLongtoDate(info.getString("end_at"), new SimpleDateFormat("yyyy-MM-dd"));;
				data.city_code = info.getString("city_code");
				data.address = info.getString("address");
				data.added = info.getString("added");
				data.is_admin = info.getString("is_admin");
				data.readable = info.getString("readable");
				data.link = info.getString("link");
				JSONObject creatorObj = new JSONObject(info.getString("creator"));
				data.creator = creatorObj.getString("nickname");
				JSONArray ownedArr = info.getJSONArray("member");
				for (int i=0;i<ownedArr.length();i++) {
					CardIntroEntity phone = CardIntroEntity.parseFromActivityMember(ownedArr.getJSONObject(i),"活动参加成员");
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
