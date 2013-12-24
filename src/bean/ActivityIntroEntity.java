package bean;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;

import tools.AppException;
import tools.StringUtils;

public class ActivityIntroEntity extends Entity {
	public String activity_id;//": "206",
	public String openid;//": "oYTgBuITFr_rlDTx7VLiPvrt-D_s",
	public String code;//": "989134",
	public String title;//": "546546",
	public String content;//": "5465465465",
	public String begin_at;//": "1388505600",
	public String end_at;//": "0",
	public String city_code;//": null,
	public String address;//": "546",
	public String latlng;//": "",
    public String privacy;//": "0",
    public String dateline;//": "1386178664",
    public String hits;//": "5",
    public String type;//": "0",
    public String count;//": "1"
    public String member;
    
    public String activitySectionType;
    public boolean willRefresh;
    
    public static ActivityIntroEntity parse(JSONObject info, String sectionType) throws IOException, AppException {
    	ActivityIntroEntity data = new ActivityIntroEntity();
		try {
			data.code = info.getString("code");
			data.title = info.getString("title");
			data.content = info.getString("content");
			data.privacy = info.getString("privacy");
			data.dateline = info.getString("dateline");
			data.hits = info.getString("hits");
			data.type = info.getString("type");
			data.member = info.getString("member");
			data.begin_at = StringUtils.phpLongtoDate(info.getString("begin_at"), new SimpleDateFormat("yyyy-MM-dd"));
			data.end_at= StringUtils.phpLongtoDate(info.getString("end_at"), new SimpleDateFormat("yyyy-MM-dd"));
			data.city_code = info.getString("city_code");
			data.address= info.getString("address");
			data.activitySectionType = sectionType;
			data.willRefresh = false;
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
}
