package roco.kcchen.musicplay;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicDatabaseHelper extends SQLiteOpenHelper{
	private static final String DATABASENAME = "musicplay.db";//数据库名称
	private static final int DATABASEVERSION = 1;//数据库版本
	private static final String TABLENAME = "music";//数据表名
	
	//构造函数
	public MusicDatabaseHelper(Context context) {
		super(context, DATABASENAME, null, DATABASEVERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) { //创建数据表
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE "+TABLENAME+"("+
		"id             INTEGER       PRIMARY KEY,"+
		"music_name     VARCHAR(50)   NOT NULL,"+	//歌名
		"path           TEXT                  ,"+	//文件路径
		"artist         VARCHAR(50)           ,"+	//艺术家
		"paly_count     INTEGER       NOT NULL,"+	//播放次数
		"add_time       DATE          NOT NULL)";	//添加时间
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql = "DROP TABLE IF EXISTS "+TABLENAME;
		db.execSQL(sql);
		this.onCreate(db);	//创建表
	}

}
