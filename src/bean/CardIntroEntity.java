package bean;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import config.CommonValue;

import tools.AppException;
import tools.Logger;
import tools.StringUtils;

public class CardIntroEntity extends Entity {
	public String openid;//": "oYTgBuITFr_rlDTx7VLiPvrt-D_s",
	public String weibo;//": "伟章Ho",
	public String realname;//": "何伟章1",
	public String phone;//": "15914376176",
	public String email;//": "",
	public String department;//": "维卡互动",
	public String position;//": "产品经理",
	public String birthday;//": "523206000",
	public String address;//": "广东广州番禺",
	public String create_at;//": "1385694759",
	public String ip;//": "192.168.1.10",
	public String hits;//": "180",
	public String certified;//": "0",
	public String privacy;//": "0",
	public String supply;//": "无",
	public String intro;//": "笨人蠢蛋",
	public String wechat;//": "winrdcn",
	public String link;
	public String headimgurl;
	public String pinyin;
	
	public String avatar;
	
	public String cardSectionType;
	public boolean willRefresh;
	public String code;
	
	public String role;//": "0",
	public String wechat_id;//": "",
	public String nickname;//": "詹景文Max",
	public String sex;//": "1",
	public String province;//": "Guangdong",
	public String city;;//": "Guangzhou",
	public String country;//": "CN",
	public String privilege;//": null,
	public String login_at;//": "1385511684",
	public String language;//": "zh_CN",
	public String isfriend;//": "0",
	public String readable;
	public String qun_readable;
	
	public boolean isChosen;
	
	//card 单张增加字段
	public String needs;
	public String hometown;
	public String interest;
	public String school;
	public String homepage;
	public String company_site;
	public String qq;
	public String intentionen;
	public String tencent;
	public String renren;
	public String zhihu;
	public String qzone;
	public String facebook;
	public String diandian;
	public String twitter;
	
	public String phone_display;
	
	//card list
	public static CardIntroEntity parse(JSONObject info, String sectionType) throws IOException, AppException {
		CardIntroEntity data = new CardIntroEntity();
		try {
			data.code = info.getString("code");
			data.openid = info.getString("openid");
			data.realname = info.getString("realname");
			data.phone = info.getString("phone");
//			data.email = info.getString("email");
			data.privacy = info.getString("privacy");
			data.department = info.getString("department");
//			data.hits = info.getString("hits");
			data.position = info.getString("position");
			data.birthday = StringUtils.phpLongtoDate(info.getString("birthday"), new SimpleDateFormat("yyyy-MM-dd"));
			data.address = info.getString("address");
//			data.create_at= info.getString("create_at");
			data.certified = info.getString("certified");
			data.address= info.getString("address");
			data.privacy = info.getString("privacy");
			data.supply= info.getString("supply");
			data.intro = info.getString("intro");
			data.wechat= info.getString("wechat");
			data.link = info.getString("link");
			if (!info.isNull("headimgurl")) {
				data.headimgurl = info.getString("headimgurl");
			}
			if (!info.isNull("avatar")) {
				data.avatar = info.getString("avatar");
			}
			if (!info.isNull("phone_display")) {
				data.phone_display = info.getString("phone_display");
			}
			data.cardSectionType = sectionType;
			data.willRefresh = false;
			data.pinyin = info.getString("pinyin");
			data.isfriend = info.getString("isfriend");
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
	
	//activity member
	public static CardIntroEntity parseFromActivityMember(JSONObject info, String sectionType) throws IOException, AppException {
		CardIntroEntity data = new CardIntroEntity();
		try {
			data.code = info.getString("code");
			data.openid = info.getString("openid");
			data.realname = info.getString("realname");
			data.phone = info.getString("phone");
//			data.email = info.getString("email");
			data.privacy = info.getString("privacy");
			data.department = info.getString("department");
//			data.hits = info.getString("hits");
			data.position = info.getString("position");
			data.birthday = StringUtils.phpLongtoDate(info.getString("birthday"), new SimpleDateFormat("yyyy-MM-dd"));
			data.address = info.getString("address");
//			data.create_at= info.getString("create_at");
			data.certified = info.getString("certified");
			data.address= info.getString("address");
			data.privacy = info.getString("privacy");
			data.supply= info.getString("supply");
			data.intro = info.getString("intro");
			data.wechat= info.getString("wechat");
			data.link = info.getString("link");
			if (!info.isNull("avatar")) {
				data.avatar = info.getString("avatar");
			}
			data.cardSectionType = sectionType;
			data.willRefresh = false;
			data.nickname = info.getString("nickname");
			data.pinyin = info.getString("pinyin");
			data.isfriend = info.getString("isfriend");
		} catch (JSONException e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
	
	//card view
	public static CardIntroEntity parse(String res) throws IOException, AppException {
		CardIntroEntity data = new CardIntroEntity();
		try {
			Logger.i(res);
			JSONObject js = new JSONObject(res);
			if(js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				data.openid = info.getString("openid");
				data.realname = info.getString("realname");
				data.phone = info.getString("phone");
				data.privacy = info.getString("privacy");
				data.department = info.getString("department");
				data.position = info.getString("position");
				data.birthday = StringUtils.phpLongtoDate(info.getString("birthday"), new SimpleDateFormat("yyyy-MM-dd"));
				data.address = info.getString("address");
				data.certified = info.getString("certified");
				data.address= info.getString("address");
				data.privacy = info.getString("privacy");
				data.supply= info.getString("supply");
				data.intro = info.getString("intro");
				data.wechat= info.getString("wechat");
				data.link = info.getString("link");
				if (!info.isNull("headimgurl")) {
					data.headimgurl = info.getString("headimgurl");
				}
				if (!info.isNull("avatar")) {
					data.avatar = info.getString("avatar");
				}
				data. needs = info.getString("needs");
				data. hometown = info.getString("hometown");
				data. interest = info.getString("interest");
				data. school= info.getString("school");
				data. homepage= info.getString("homepage");
				data. company_site= info.getString("company_site");
				data. qq= info.getString("qq");
				data. intentionen= info.getString("intentionen");
				data. tencent= info.getString("tencent");
				data. renren= info.getString("renren");
				data. zhihu= info.getString("zhihu");
				data. qzone= info.getString("qzone");
				data. facebook= info.getString("twitter");
				data. diandian= info.getString("twitter");
				data. twitter= info.getString("twitter");
				data.isfriend = info.getString("isfriend");
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
	
	
	/**
	 * parse from phonebook view member
	 * @param info
	 * @param sectionType
	 * @return
	 * @throws IOException
	 * @throws AppException
	 */
	public static CardIntroEntity parseFromViewMembers(JSONObject info, String sectionType) throws IOException, AppException {
		CardIntroEntity data = new CardIntroEntity();
		try {
			data.code = info.getString("code");
			data.openid = info.getString("openid");
			data.role = info.getString("role");
			data.weibo = info.getString("weibo");
			data.realname = info.getString("realname");
			data.phone = info.getString("phone");
			data.email = info.getString("email");
			data.department = info.getString("department");
			data.position = info.getString("position");
			data.birthday = StringUtils.phpLongtoDate(info.getString("birthday"), new SimpleDateFormat("yyyy-MM-dd"));
			data.address = info.getString("address");
//			data.create_at= info.getString("create_at");
			data.hits = info.getString("hits");
			data.certified = info.getString("certified");
			data.address= info.getString("address");
			data.privacy = info.getString("privacy");
			data.supply= info.getString("supply");
			data.intro = info.getString("intro");
			data.nickname = info.getString("nickname");
//			data.sex = info.getString("sex");
//			data.province = info.getString("province");
//			data.city = info.getString("city");
//			data.country = info.getString("country");
//			data.wechat_id = info.getString("wechat_id");
			data.wechat= info.getString("wechat");
			data.link = info.getString("link");
			if (!info.isNull("avatar")) {
				data.avatar = info.getString("avatar");
			}
			data.isfriend = info.getString("isfriend");
			data.readable = info.getString("readable");
			data.qun_readable = info.getString("qun_readable");
			data.cardSectionType = sectionType;
			data.willRefresh = false;
			data.pinyin = info.getString("pinyin");
			
		} catch (JSONException e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
	
	public static CardIntroEntity parseFC(JSONObject info, String sectionType) throws IOException, AppException {
		CardIntroEntity data = new CardIntroEntity();
		try {
			data.code = info.getString("co");
			data.openid = info.getString("o");
			data.realname = info.getString("r");
			data.phone = info.getString("ph");
//			data.email = info.getString("email");
			data.privacy = info.getString("pr");
			data.department = info.getString("d");
//			data.hits = info.getString("hits");
			data.position = info.getString("po");
			data.birthday = StringUtils.phpLongtoDate(info.getString("b"), new SimpleDateFormat("yyyy-MM-dd"));
			data.address = info.getString("ad");
//			data.create_at= info.getString("create_at");
			data.certified = info.getString("c");
			data.supply= info.getString("su");
			data.intro = info.getString("i");
			data.wechat= info.getString("we");
			data.link = info.getString("l");
			if (!info.isNull("av")) {
				data.avatar = info.getString("av");
			}
			if (!info.isNull("pho")) {
				data.phone_display = info.getString("pho");
			}
			data.cardSectionType = sectionType;
			data.willRefresh = false;
			data.pinyin = info.getString("p");
			data.isfriend = info.getString("is");
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
}
