package bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;

public class RecommendIntroEntity extends Entity{

	public String recommend_id;
	public String title;
	public String openid;//": "oYTgBuITFr_rlDTx7VLiPvrt-D_s",
	public String manager;//": "管理人员",
	public String requirements;//": "成员要求",
	public String capacity;//": "2",
	public String introduction;//": "群介绍",
	public String vision;//": "群工作与愿景群工作与愿景",
	public String rule;//": "禁止说明禁止说明禁止说明",
	public String audit_level;//": "审核时间层次审核时间层次审核时间层次审核时间层次审核时间层次审核时间层次审核时间层次",
	public String question;//": "群主审核问题群主审核问题测试测试阿萨德哈怂i等IUM",
	public String state;//": "0",
	public String created_at;//": "0",
	public String updata_at;//": "0",
	public String link;//": "",
	public String sort;//": "0",
	public String tags;//": "公关 互联网 金融",
	public String hits;//": "5",
	public String cover;//": "哈哈",
	public String creator;//": "哈哈哈",
	public String member;//": "1"
	
	public static RecommendIntroEntity parse(JSONObject info) throws IOException, AppException {
		RecommendIntroEntity data = new RecommendIntroEntity();
		try {
			data.title = info.getString("title");
			data.introduction = info.getString("introduction");
			data.tags = info.getString("tags");
			data.cover = info.getString("cover");
			data.creator = info.getString("creator");
		} catch (JSONException e) {
			throw AppException.json(e);
		}
		return data;
	}
    	
}
