package db.manager;

import im.bean.IMMessage;
import im.bean.IMMessage.JSBubbleMessageStatus;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import bean.CardIntroEntity;
import tools.Logger;
import tools.StringUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import config.MyApplication;
import config.CommonValue.CardSectionType;
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
		return st.insert("wcb_phonebook", contentValues);
	}
	
	
	/**
	 * 获取所有CardIntroEntity
	 * @return List<CardIntroEntity>
	 */
	public List<CardIntroEntity> getWeFriends() {
		String sql;
		sql = "select * from wcb_phonebook";
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<CardIntroEntity> list = st.queryForList(
				new RowMapper<CardIntroEntity>() {

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
						data.cardSectionType = "";
						data.pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
						data.isfriend = cursor.getString(cursor.getColumnIndex("isfriend"));
						return data;
					}
				}, 
				sql,
				null);
		return list;
	}
	
	public List<CardIntroEntity> searchWeFriendsByKeyword(String keyword) {
		String sql;
		String py = keyword;
		py = py.replace("", "%");
		sql = "select * from wcb_phonebook where realname like '%" + keyword +"%'"
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
						data.cardSectionType = "一度好友";
						data.pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
						data.isfriend = cursor.getString(cursor.getColumnIndex("isfriend"));
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
		st.update("wcb_phonebook", contentValues, "openid=?", new String[]{model.openid});
	}
	
	public boolean isOpenidExist(String openid) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.isExistsByField("wcb_phonebook", "openid", openid);
	}
}
