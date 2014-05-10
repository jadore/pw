package db.manager;

import java.util.List;

import tools.Logger;
import tools.StringUtils;
import bean.CardIntroEntity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import config.MyApplication;
import config.CommonValue.LianXiRenType;
import db.DBManager;
import db.SQLiteTemplate;
import db.SQLiteTemplate.RowMapper;

/**
 * @author donal
 *
 */
/**
 * @author donal
 *
 */
public class WeFriendManager {
	private static WeFriendManager messageManager = null;
	private static DBManager manager = null;

	private WeFriendManager(Context context) {
		manager = DBManager.getInstance(context, MyApplication.getInstance().getLoginUid());
	}

	public static WeFriendManager getInstance(Context context) {
		if (messageManager == null) {
			messageManager = new WeFriendManager(context);
		}

		return messageManager;
	}
	
	public static void destroy() {
		messageManager = null;
		manager = null;
	}

	/**
	 * 
	 * 保存CardIntroEntity.
	 * 
	 * @param model
	 */
	public long saveWeFriend(CardIntroEntity model) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("openid",  model.openid);
		contentValues.put("assist_openid", "");
		contentValues.put("avatar", model.avatar);
		contentValues.put("sex", model.sex);
		contentValues.put("state", "");
		contentValues.put("weibo", model.weibo);
		contentValues.put("realname", model.realname);
		contentValues.put("font_family", "");
		contentValues.put("pinyin", model.pinyin);
		contentValues.put("phone",  model.phone);
		contentValues.put("email", model.email);
		contentValues.put("department", model.department);
		contentValues.put("position", model.position);
		contentValues.put("birthday", model.birthday);
		contentValues.put("address", model.address);
		contentValues.put("hits", model.hits);
		contentValues.put("certified", model.certified);
		contentValues.put("privacy", model.privacy);
		contentValues.put("supply",  model.supply);
		contentValues.put("needs", model.needs);
		contentValues.put("intro", model.intro);
		contentValues.put("wechat", model.wechat);
		contentValues.put("headimgurl", model.headimgurl);
		contentValues.put("nickname", model.nickname);
		contentValues.put("link", model.link);
		contentValues.put("link2", model.link);
		contentValues.put("code", model.code);
		contentValues.put("isfriend", model.isfriend);
		contentValues.put("phoneDisplay", model.phone_display);
		contentValues.put("py", model.py);
		return st.insert("wcb_phonebook", contentValues);
	}
	
	public int getWeFriendCount() {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.getCount("select realname from wcb_phonebook", null);
	}
	
	/**
	 * 获取所有CardIntroEntity
	 * @return List<CardIntroEntity>
	 */
	public List<CardIntroEntity> getWeFriends() {
		String sql;
		sql = "select code, openid, realname, department, position, avatar, pinyin, py from wcb_phonebook";
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<CardIntroEntity> list = st.queryForList(
				new RowMapper<CardIntroEntity>() {

					@Override
					public CardIntroEntity mapRow(Cursor cursor, int index) {
						CardIntroEntity data = new CardIntroEntity();
						data.code = cursor.getString(0);
						data.openid = cursor.getString(1);
						data.realname = cursor.getString(2);
						data.department = cursor.getString(3);
						data.position = cursor.getString(4);;
						data.avatar = cursor.getString(5);
						data.cardSectionType = LianXiRenType.yidu;
						data.pinyin = cursor.getString(6);
						data.py = cursor.getString(7);
						return data;
					}
				}, 
				sql,
				null);
		return list;
	}
	
	public CardIntroEntity getCardByOpenid(String openid) {
		String sql = "select * from wcb_phonebook where openid=?";
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.queryForObject(new RowMapper<CardIntroEntity>() {
			@Override
			public CardIntroEntity mapRow(Cursor cursor, int index) {
				CardIntroEntity data = new CardIntroEntity();
				data.code = cursor.getString(cursor.getColumnIndex("code"));
				data.openid = cursor.getString(cursor.getColumnIndex("openid"));
				data.realname = cursor.getString(cursor.getColumnIndex("realname"));
				data.phone = cursor.getString(cursor.getColumnIndex("phone"));
				data.privacy = cursor.getString(cursor.getColumnIndex("privacy"));
				data.department = cursor.getString(cursor.getColumnIndex("department"));
				data.position = cursor.getString(cursor.getColumnIndex("position"));;
				data.birthday = cursor.getString(cursor.getColumnIndex("birthday"));
				data.address = cursor.getString(cursor.getColumnIndex("address"));
				data.certified = cursor.getString(cursor.getColumnIndex("certified"));
				data.supply= cursor.getString(cursor.getColumnIndex("supply"));
				data.intro = cursor.getString(cursor.getColumnIndex("intro"));
				data.wechat= cursor.getString(cursor.getColumnIndex("wechat"));
				data.link = cursor.getString(cursor.getColumnIndex("link"));
				data.avatar = cursor.getString(cursor.getColumnIndex("avatar"));
				data.phone_display = cursor.getString(cursor.getColumnIndex("phoneDisplay"));
				data.cardSectionType = LianXiRenType.yidu;
				data.pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
				data.isfriend = cursor.getString(cursor.getColumnIndex("isfriend"));
				data.py = cursor.getString(cursor.getColumnIndex("py"));
				data. hometown =  cursor.getString(cursor.getColumnIndex("hometown"));
				data. interest =  cursor.getString(cursor.getColumnIndex("interest"));
				data. school=  cursor.getString(cursor.getColumnIndex("school"));
				data. homepage=  cursor.getString(cursor.getColumnIndex("homepage"));
				data. company_site=  cursor.getString(cursor.getColumnIndex("company_site"));
				data. qq=  cursor.getString(cursor.getColumnIndex("qq"));
				data. intentionen=  cursor.getString(cursor.getColumnIndex("intentionen"));
				data. tencent=  cursor.getString(cursor.getColumnIndex("tencent"));
				data. renren=  cursor.getString(cursor.getColumnIndex("renren"));
				data. zhihu=  cursor.getString(cursor.getColumnIndex("zhihu"));
				data. qzone=  cursor.getString(cursor.getColumnIndex("qzone"));
				data. facebook=  cursor.getString(cursor.getColumnIndex("facebook"));
				data. diandian=  cursor.getString(cursor.getColumnIndex("diandian"));
				data. twitter=  cursor.getString(cursor.getColumnIndex("twitter"));
				data.province= cursor.getString(cursor.getColumnIndex("twitter"));
				data.city= cursor.getString(cursor.getColumnIndex("city"));
				data.country= cursor.getString(cursor.getColumnIndex("city"));
				return data;
			}
		}, 
		sql, 
		new String[]{openid});
	}
	
	public List<CardIntroEntity> searchWeFriendsByKeyword(String keyword) {
		String sql;
		String py = keyword;
		py = py.replace("", "%");
		sql = "select code, openid, realname, department, position, avatar, pinyin, py from wcb_phonebook where realname like '%" + keyword +"%'"
				+" or pinyin like '%" + keyword +"%'"
				+" or department like '%" + keyword +"%'"
				+" or position like '%" + keyword +"%'"
				+" or supply like '%" + keyword +"%'"
				+" or intro like '%" + keyword +"%'"
				+" or wechat like '%" + keyword +"%'"
				+" or address like '%" + keyword +"%'"
				+" or pinyin like '" + py + "'"
				+ ";";
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<CardIntroEntity> list = st.queryForList(
				new RowMapper<CardIntroEntity>() {

					@Override
					public CardIntroEntity mapRow(Cursor cursor, int index) {
						CardIntroEntity data = new CardIntroEntity();
						data.code = cursor.getString(cursor.getColumnIndex("code"));
						data.openid = cursor.getString(cursor.getColumnIndex("openid"));
						data.realname = cursor.getString(cursor.getColumnIndex("realname"));
//						data.phone = cursor.getString(cursor.getColumnIndex("phone"));
//						data.privacy = cursor.getString(cursor.getColumnIndex("privacy"));
						data.department = cursor.getString(cursor.getColumnIndex("department"));
						data.position = cursor.getString(cursor.getColumnIndex("position"));;
//						data.birthday = cursor.getString(cursor.getColumnIndex("birthday"));
//						data.address = cursor.getString(cursor.getColumnIndex("address"));
//						data.certified = cursor.getString(cursor.getColumnIndex("certified"));
//						data.supply= cursor.getString(cursor.getColumnIndex("supply"));
//						data.intro = cursor.getString(cursor.getColumnIndex("intro"));
//						data.wechat= cursor.getString(cursor.getColumnIndex("wechat"));
//						data.link = cursor.getString(cursor.getColumnIndex("link"));
						data.avatar = cursor.getString(cursor.getColumnIndex("avatar"));
//						data.phone_display = cursor.getString(cursor.getColumnIndex("phoneDisplay"));
						data.cardSectionType = LianXiRenType.yidu;
						data.pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
//						data.isfriend = cursor.getString(cursor.getColumnIndex("isfriend"));
						data.py = cursor.getString(cursor.getColumnIndex("py"));
						return data;
					}
				}, 
				sql,
				null);
		return list;
	}
	
	/**
	 * 获取所有的openid
	 * @return
	 */
	public List<String> getAllOpenidOfWeFriends() {
		//读本地
		String sql;
		sql = "select openid from wcb_phonebook;";
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<String> list = st.queryForList(
				new RowMapper<String>() {

					@Override
					public String mapRow(Cursor cursor, int index) {
						return cursor.getString(cursor.getColumnIndex("openid"));
					}
				}, 
				sql,
				null);
		return list;
	}
	
	/**
	 * 删除openid的信息
	 * @param openid
	 */
	public void deleteWeFriendBy(String openid) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		st.deleteByCondition("wcb_phonebook", "openid=?", new String[]{openid});
	}
	
	public void updateWeFriend(CardIntroEntity model) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("openid",  model.openid);
		contentValues.put("assist_openid", "");
		contentValues.put("avatar", model.avatar);
		contentValues.put("sex", model.sex);
		contentValues.put("state", "");
		contentValues.put("weibo", model.weibo);
		contentValues.put("realname", model.realname);
		contentValues.put("font_family", "");
		contentValues.put("pinyin", model.pinyin);
		contentValues.put("phone",  model.phone);
		contentValues.put("email", model.email);
		contentValues.put("department", model.department);
		contentValues.put("position", model.position);
		contentValues.put("birthday", model.birthday);
		contentValues.put("address", model.address);
		contentValues.put("hits", model.hits);
		contentValues.put("certified", model.certified);
		contentValues.put("privacy", model.privacy);
		contentValues.put("supply",  model.supply);
		contentValues.put("needs", model.needs);
		contentValues.put("intro", model.intro);
		contentValues.put("wechat", model.wechat);
		contentValues.put("headimgurl", model.headimgurl);
		contentValues.put("nickname", model.nickname);
		contentValues.put("link", model.link);
		contentValues.put("link2", model.link);
		contentValues.put("code", model.code);
		contentValues.put("isfriend", model.isfriend);
		contentValues.put("phoneDisplay", model.phone_display);
		contentValues.put("py", model.py);
		contentValues.put("hometown",  model.hometown);
		contentValues.put("interest", model.interest);
		contentValues.put("school", model.school);
		contentValues.put("homepage", model.homepage);
		contentValues.put("company_site", model.company_site);
		contentValues.put("qq", model.qq);
		contentValues.put("intentionen", model.intentionen);
		contentValues.put("tencent", model.tencent);
		contentValues.put("renren", model.renren);
		contentValues.put("zhihu", model.zhihu);
		contentValues.put("qzone", model.qzone);
		contentValues.put("facebook", model.facebook);
		contentValues.put("diandian", model.diandian);
		contentValues.put("twitter", model.twitter);
		contentValues.put("province", model.province);
		contentValues.put("city", model.city);
		contentValues.put("country", model.country);
		st.update("wcb_phonebook", contentValues, "openid=?", new String[]{model.openid});
	}
	
	public boolean isOpenidExist(String openid) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		int count = st.getCount("select realname from wcb_phonebook where openid=?", new String[]{openid});
		return count>0?true:false;
	}
}
