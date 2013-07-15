package roco.kcchen.musicplay;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicDatabaseHelper extends SQLiteOpenHelper{
	private static final String DATABASENAME = "musicplay.db";//���ݿ�����
	private static final int DATABASEVERSION = 1;//���ݿ�汾
	private static final String TABLENAME = "music";//���ݱ���
	
	//���캯��
	public MusicDatabaseHelper(Context context) {
		super(context, DATABASENAME, null, DATABASEVERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) { //�������ݱ�
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE "+TABLENAME+"("+
		"id             INTEGER       PRIMARY KEY,"+
		"music_name     VARCHAR(50)   NOT NULL,"+	//����
		"path           TEXT                  ,"+	//�ļ�·��
		"artist         VARCHAR(50)           ,"+	//������
		"paly_count     INTEGER       NOT NULL,"+	//���Ŵ���
		"add_time       DATE          NOT NULL)";	//���ʱ��
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql = "DROP TABLE IF EXISTS "+TABLENAME;
		db.execSQL(sql);
		this.onCreate(db);	//������
	}

}
