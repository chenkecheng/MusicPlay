package roco.kcchen.musicplay;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

/**
 * @author kcchen
 * */
public class MusicInfoUtils {
	private static final String TAG = "MusicInfoUtils";
	
	private ArrayList<MusicInfo> mMusicList = new ArrayList<MusicInfo>();
	
	private Context mContext = null;
	
	 /**
     * MusicInfoUtils()
     * @param context
     */
    public MusicInfoUtils(Context context) {
        mContext = context;
    }
    /**
     * @deprecated
     * */
    public String[] getMusicInfo(String aFileAbsoulatePath) {
        String[] fileMessage = new String[3];
        File file = new File(aFileAbsoulatePath);
        String fileName = file.getName();
        String filePath = "/mnt" + file.getPath();

        if (file.exists()) {
            if (mContext != null) {
                findMusicFromSD();
                int count = mMusicList.size();
                for (int i = 0; i < count; i++) {
                    if (mMusicList.get(i).getmFilePath().equals(filePath)
                            && mMusicList.get(i).getmFileName()
                                    .equals(fileName)) {
                        fileMessage[0] = mMusicList.get(i).getmFileTitle();
                        fileMessage[1] = mMusicList.get(i).getmAlbum();
                        fileMessage[2] = mMusicList.get(i).getmSinger();
                        break;
                    }
                }
            }
        }

        return fileMessage;
    }
    
    public ArrayList<MusicInfo> getMusicList() {
   
    	findMusicFromSD();
    	findMusicFromInternal();
        return mMusicList;
    }
    
    private void findMusicFromInternal() {

        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA },
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);
        if (cursor.moveToFirst()) {
            getMusicList(cursor);
        }
        return;
    }
    
    private void findMusicFromSD() {

        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA },
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);
        if (cursor.moveToFirst()) {
            getMusicList(cursor);
        }
        return;
    }
    
    private void getMusicList(Cursor cursor) {
        MusicInfo mMusicInfo = null;
        do {
        	mMusicInfo = new MusicInfo();
        	mMusicInfo.setId(cursor.getInt(0));
        	mMusicInfo.setmFileName(cursor.getString(1));// file Name
        	mMusicInfo.setmFileTitle(cursor.getString(2));// song name
        	mMusicInfo.setmDuration(cursor.getInt(3));// play time
        	mMusicInfo.setmSinger(cursor.getString(4));// artist
        	mMusicInfo.setmAlbum(cursor.getString(5));// album
            if (cursor.getString(6) != null) {
            	mMusicInfo.setmYear(cursor.getString(6));
            } else {
            	mMusicInfo.setmYear("undefine");
            }
            if ("audio/mpeg".equals(cursor.getString(7).trim())) {// file type
            	mMusicInfo.setmFileType("mp3");
            } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
            	mMusicInfo.setmFileType("wma");
            }
            if (cursor.getString(8) != null) {// fileSize
                float temp = cursor.getInt(8) / 1024f / 1024f;
                String sizeStr = (temp + "").substring(0, 4);
                mMusicInfo.setmFileSize(sizeStr + "M");
            } else {
            	mMusicInfo.setmFileSize("undefine");
            }

            if (cursor.getString(9) != null) {//file path
            	mMusicInfo.setmFilePath(cursor.getString(9));
            }

            mMusicList.add(mMusicInfo);
        } while (cursor.moveToNext());

        cursor.close();

        return;
    }
}
