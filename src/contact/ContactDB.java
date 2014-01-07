package contact;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDB extends SQLiteOpenHelper {

	public static final String DbName = "contact.db";
	public static final int VersionCreate = 1;
	public static final String TbContacts = "contact";
	public static final String TbMobiles = "mobile";
	
	public ContactDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table if not exists " + TbContacts + "(" +
				"contact_id varchar," +
				"photo_id varchar," +
				"phone_id varchar," +
				"avatar_url varchar," +
				"pinyin varchar)");
		db.execSQL("create table if not exists " + TbMobiles + "(" +
				"contact_id varchar," +
				"mobile varchar)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
