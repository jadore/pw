package bean;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import tools.AppException;
import tools.StringUtils;

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
	
	public String logo;
	public String link;
	
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
			data.link = info.getString("link");
			data.willRefresh = false;
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
	
	
	public String begin_at;
	public String end_at;
	public String count;
	public String creator;
	public static PhoneIntroEntity parsePhonebookAndActivity(JSONObject info, String sectionType) throws IOException, AppException {
		PhoneIntroEntity data = new PhoneIntroEntity();
		try {
			if (!info.isNull("code")) {
				data.code = info.getString("code");
			}
			if (!info.isNull("family_id")) {
				data.code = info.getString("family_id");
			}
			data.title = info.getString("title");
			data.content = info.getString("content");
			
			if (!info.isNull("privacy")) {
				data.privacy = info.getString("privacy");
			}
			if (!info.isNull("dateline")) {
				data.dateline = info.getString("dateline");
			}
			data.hits = info.getString("hits");
			data.type = info.getString("type");
			if (!info.isNull("question")) {
				data.question = info.getString("question");
			}
			if (!info.isNull("member")) {
				data.member = info.getString("member");
			}
			if (!info.isNull("begin_at")) {
				data.begin_at = StringUtils.phpLongtoDate(info.getString("begin_at"), new SimpleDateFormat("yyyy-MM-dd"));
			}
			if (!info.isNull("end_at")) {
				data.end_at = StringUtils.phpLongtoDate(info.getString("end_at"), new SimpleDateFormat("yyyy-MM-dd"));
			}
			if (!info.isNull("count")) {
				data.count = info.getString("count");
			}
			if (!info.isNull("logo")) {
				data.logo = info.getString("logo");
			}
			if (!info.isNull("creator")) {
				JSONObject creatorObj = new JSONObject(info.getString("creator"));
				data. creator = creatorObj.getString("nickname");
			}
			data.phoneSectionType = sectionType;
			data.link = info.getString("link");
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
    	
}
