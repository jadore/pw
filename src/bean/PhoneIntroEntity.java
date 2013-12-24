package bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import tools.AppException;

public class PhoneIntroEntity extends Entity {
	
	public String qun_id;//": "4",
	public String wechat_id;//": "oYTgBuITFr_rlDTx7VLiPvrt-D_s",
	public String code;//": "54b92330a4a7",
	public String title;//": "维卡互动微信通讯录1",
	public String content;//": "小伙伴们快测试吧进群请发短信至15914376176",
	public String privacy;//": "1",
	public String dateline;//": "1384533616",
	public String hits;//": "198",
	public String type;//": "2",
	public String question;//": "",
	public String member;//": "0"
	
	public String phoneSectionType;
	public boolean willRefresh;
	
	public static PhoneIntroEntity parse(JSONObject info, String sectionType) throws IOException, AppException {
		PhoneIntroEntity data = new PhoneIntroEntity();
		try {
			data.code = info.getString("code");
			data.title = info.getString("title");
			data.content = info.getString("content");
			data.privacy = info.getString("privacy");
			data.dateline = info.getString("dateline");
			data.hits = info.getString("hits");
			data.type = info.getString("type");
			data.question = info.getString("question");
			data.member = info.getString("member");
			data.phoneSectionType = sectionType;
			data.willRefresh = false;
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
    	
}
