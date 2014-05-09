package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * SQLite数据库的帮助类
 * 
 * 该类属于扩展类,主要承担数据库初始化和版本升级使用,其他核心全由核心父类完成
 * 
 */
public class DataBaseHelper extends SDCardSQLiteOpenHelper {

	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE [im_msg] ([_id] INTEGER NOT NULL  PRIMARY KEY AUTOINCREMENT, [chat_id] INTEGER, [msg_content] NVARCHAR, [openid] NVARCHAR, [room_id] NVARCHAR, [post_at] TEXT, [msg_time] TEXT, [msg_type] INTEGER, [msg_status] INTEGER, [media_type] INTEGER);");
		db.execSQL("CREATE TABLE [wcb_phonebook] ("
				+ "[openid] varchar(50) NOT NULL primary key,"
				+ "[assist_openid] varchar(50), "
				+ "[avatar] varchar(300) , "
				+ "[sex] varchar(50) , "
				+ "[state] varchar(50),  "
				+ "[weibo] varchar(50) ,"
				+ "[realname] varchar(50) ,"
				+ "[font_family] varchar(50) ,"
				+ "[pinyin] varchar(50) ,"
				+ "[phone] varchar(50) ,"
				+ "[email] varchar(200) ,"
				+ "[department] varchar(50) ,"
				+ "[position] varchar(50) ,"
				+ "[birthday] varchar(50) ,"
				+ "[address] text ,"
				+ "[hits] varchar(50) ,"
				+ "[certified] varchar(50) ,"
				+ "[privacy] varchar(50) ,"
				+ "[supply] text ,"
				+ "[needs] text ,"
				+ "[intro] text ,"
				+ "[wechat] varchar(50) ,"
				+ "[headimgurl] varchar(300) ,"
				+ "[nickname] varchar(50) ,"
				+ "[link] varchar(50) ,"
				+ "[link2] varchar(50) ,"
				+ "[code] varchar(50) ,"
				+ "[isfriend] varchar(50) ,"
				+ "[phoneDisplay] varchar(50), "
				+ "[py] varchar(50), "
				+ "[hometown] varchar(50), "
				+ "[interest] text ,"
				+ "[school] varchar(50) ,"
				+ "[homepage] varchar(100) ,"
				+ "[company_site] varchar(100) ,"
				+ "[qq] varchar(50) ,"
				+ "[intentionen] varchar(100) ,"
				+ "[tencent] varchar(50) ,"
				+ "[renren] varchar(50) ,"
				+ "[zhihu] varchar(50), "
				+ "[qzone] varchar(50), "
				+ "[facebook] varchar(50), "
				+ "[diandian] varchar(50), "
				+ "[twitter] varchar(50), "
				+ "[province] varchar(50), "
				+ "[city] varchar(50), "
				+ "[country] varchar(50) "
				+ ");");
//		db.execSQL("create index [wcb_phonebook_index] on [wcb_phonebook] (openid);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
