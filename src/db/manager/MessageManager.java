package db.manager;


import im.bean.HistoryChatBean;
import im.bean.IMMessage;
import im.bean.Notice;

import java.util.List;

import config.MyApplication;

import tools.Logger;
import tools.StringUtils;


import db.DBManager;
import db.SQLiteTemplate;
import db.SQLiteTemplate.RowMapper;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

/**
 * 
 * 消息管理
 * 
 */
public class MessageManager {
	private static MessageManager messageManager = null;
	private static DBManager manager = null;

	private MessageManager(Context context) {
		manager = DBManager.getInstance(context, MyApplication.getInstance().getLoginUid());
	}

	public static MessageManager getInstance(Context context) {

		if (messageManager == null) {
			messageManager = new MessageManager(context);
		}

		return messageManager;
	}

	/**
	 * 
	 * 保存消息.
	 * 
	 * @param msg
	 */
	public long saveIMMessage(IMMessage msg) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("msg_content",  StringUtils.doEmpty(msg.content));
		contentValues.put("openid", StringUtils.doEmpty(msg.openId));
		contentValues.put("msg_type", msg.msgType);
		contentValues.put("msg_time", msg.msgTime);
		contentValues.put("room_id", msg.roomId);
		contentValues.put("msg_status", msg.msgStatus);
		contentValues.put("media_type", msg.mediaType);
		return st.insert("im_msg", contentValues);
	}

	/**
	 * 
	 * 更新状态.
	 * 
	 * @param status
	 */
	public void updateStatus(String id, Integer status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("msg_status", status);
		st.updateById("im_msg", id, contentValues);
	}

	/**
	 * 
	 * 查找与某人的聊天记录聊天记录
	 * 
	 * @param pageNum
	 *            第几页
	 * @param pageSize
	 *            要查的记录条数
	 * @return
	 */
	public List<IMMessage> getMessageListByFrom(String roomId, int pageNum,
			int pageSize) {
		if (StringUtils.empty(roomId)) {
			return null;
		}
		int fromIndex = (pageNum - 1) * pageSize;
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		List<IMMessage> list = st.queryForList(
				new RowMapper<IMMessage>() {
					@Override
					public IMMessage mapRow(Cursor cursor, int index) {
						IMMessage msg = new IMMessage();
						msg.content = (cursor.getString(cursor.getColumnIndex("msg_content")));
						msg.openId = (cursor.getString(cursor.getColumnIndex("openid")));
						msg.msgType = (cursor.getInt(cursor.getColumnIndex("msg_type")));
						msg.msgTime = (cursor.getString(cursor.getColumnIndex("msg_time")));
						msg.mediaType = (cursor.getInt(cursor.getColumnIndex("media_type")));
						msg.msgStatus = (cursor.getInt(cursor.getColumnIndex("msg_status")));
						return msg;
					}
				},
				"select * from im_msg where room_id=? order by msg_time desc limit ? , ? ",
				new String[] { "" + roomId, "" + fromIndex, "" + pageSize });
		return list;

	}

	/**
	 * 
	 * 查找roomId的聊天记录总数
	 * 
	 * @return
	 */
	public int getChatCountWithSb(String roomId) {
		if (StringUtils.empty(roomId)) {
			return 0;
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st
				.getCount(
						"select * from im_msg where room_id=?",
						new String[] { "" + roomId });

	}

	/**
	 * 
	 * @param fromUser
	 */
	public int delChatHisWithSb(String roomId) {
		if (StringUtils.empty(roomId)) {
			return 0;
		}
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.deleteByCondition("im_msg", "room_id=?",
				new String[] { "" + roomId });
	}

//	/**
//	 * 
//	 * 获取最近聊天人聊天最后一条消息和未读消息总数
//	 * 
//	 * @return
//	 */
//	public List<HistoryChatBean> getRecentContactsWithLastMsg() {
//		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
//		List<HistoryChatBean> list = st
//				.queryForList(
//						new RowMapper<HistoryChatBean>() {
//
//							@Override
//							public HistoryChatBean mapRow(Cursor cursor, int index) {
//								HistoryChatBean notice = new HistoryChatBean();
//								notice.setId(cursor.getString(cursor
//										.getColumnIndex("_id")));
//								notice.setContent(cursor.getString(cursor
//										.getColumnIndex("content")));
//								notice.setFrom(cursor.getString(cursor
//										.getColumnIndex("msg_from")));
//								notice.setNoticeTime(cursor.getString(cursor
//										.getColumnIndex("msg_time")));
//								return notice;
//							}
//						},
//						"select m.[_id],m.[msg_content],m.[msg_time],m.msg_incoming from im_msg  m join (select msg_incoming, max(msg_time) as time from im_msg group by msg_incoming) as tem  on  tem.time=m.msg_time and tem.msg_incoming=m.msg_incoming order by msg_time desc",
//						null);
//		for (HistoryChatBean b : list) {
//			int count = st
//					.getCount(
//							"select _id from im_notice where status=? and type=? and notice_from=?",
//							new String[] { "" + Notice.UNREAD,
//									"" + Notice.CHAT_MSG, b.getFrom() });
//			b.setNoticeSum(count);
//		}
//		return list;
//	}

}
