package roco.kcchen.musicplay;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PlayList {
	
	private static final String FILENAME = "mldn";
	private static final String CURRENT_PLAY = "currentPlay";
	SharedPreferences share;
	SharedPreferences.Editor mEditer;
	
	public static ArrayList<MusicInfo> mMusicList; 
	private static MusicInfo current;	//当前播放歌曲
	private int currentPosition = 0;	//当前歌曲的位置
	
	public PlayList(Context context){
		MusicInfoUtils mMusicInfoUtils = new MusicInfoUtils(context);
		mMusicList = mMusicInfoUtils.getMusicList();//获得设备中所有的音乐文件
		
		share = context.getSharedPreferences(FILENAME, Activity.MODE_PRIVATE);
		mEditer = share.edit();
	}
	
	public synchronized void saveCurrent(int id){
		mEditer.putInt(CURRENT_PLAY, id);
		mEditer.commit();
	}
	public MusicInfo getCurrent() {
		current = null;
		int current_id = share.getInt(CURRENT_PLAY, 0);
		if(!mMusicList.isEmpty()){
			if(current_id == 0){
				current = mMusicList.get(0);
			}else{
				for(int i=0; i<mMusicList.size(); i++){
					if(current_id==mMusicList.get(i).getId()){
						currentPosition = i;
						current = mMusicList.get(i);
					}
				}
			}
		}else{
//			current = mMusicList.get(0);
		}
		
		return current;
	}
	
	public synchronized MusicInfo nextFile() {
		current = null;
		
		if (!mMusicList.isEmpty()) {
//			current = files.remove(0);
			int index = currentPosition+1;
			if(index > mMusicList.size()){
				current = mMusicList.get(0);
			}else{
				current = mMusicList.get(index);
			}
			
		}
		
//		if (frontEnd != null) {
//			frontEnd.changeFile(current);
//		}
		saveCurrent(current.getId());
		
		return current;
	}
}
