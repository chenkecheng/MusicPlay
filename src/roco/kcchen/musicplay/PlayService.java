package roco.kcchen.musicplay;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore.Audio;
import android.util.Log;

public class PlayService extends Service implements MusicPlayable,OnCompletionListener, OnPreparedListener,
OnErrorListener{
	
	private static final String TAG = "PlayService";
	
	private Context mContext;
	private MediaPlayer mMediaPlayer =null;
	public static PlayList mPlayList;
	
//	MusicInfo mIMusicInfo;
	
	// 指定播放状态
    enum State {
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    };

//    State mState = State.Stopped;
    State mState = State.Playing;
	
    //暂停的原因
    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    };
    
    //用作与Activity的绑定，用于调用服务里面的方法，传输数据
    public class LocalBinder extends Binder implements IPlayService{
    	
    	PlayService getService(){
			return PlayService.this;
			}

		@Override
		public MusicInfo getCurrentMusic() {
			// TODO Auto-generated method stub
			return mPlayList.getCurrent();
		}

		@Override
		public int getCurrentPlayPosition() {
			// TODO Auto-generated method stub
			return getPosition();
		}
    }
    
	private IBinder mIBinder = new LocalBinder();
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mIBinder;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mPlayList = new PlayList(this);
//		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
//			
//			@Override
//			public void onCompletion(MediaPlayer arg0) {
//				// TODO Auto-generated method stub
//				relaxResources(true);
//			}
//		});
	}



	/* (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		mState = State.Stopped;
		relaxResources(true);	//释放资源
		
		super.onDestroy();
	}



	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
//		return super.onStartCommand(intent, flags, startId);
		return START_NOT_STICKY;
	}

	public void processPlayPauseRequest(){
		//测试说明取得当前的音频文件
//		MusicInfo file = mPlayList.getCurrent();
//		Log.d(TAG, file.getmFilePath());
		
		switch(mState){
		case Stopped:stop();break;
		case Paused:paused();break;
		case Playing:playing();break;
		case Preparing:preparing();break;
		
		}
	}
	
	
	public void processPlayNow(){
//		mMediaPlayer = MediaPlayer.create(mContext, Uri.fromFile(new File(mIMusicInfo.getmFilePath())));
//		mPlayList.saveCurrent(mIMusicInfo.getId());
//		if(mMediaPlayer != null){
//			if(mMediaPlayer.isPlaying()){
//				paused();
//			}else{
//				playing();
//			}
//		}
		relaxResources(false);
		MusicInfo file = mPlayList.getCurrent();
		if(file == null){
			return;
		}
		createMediaPlayerIfNeeded();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);	//设置媒体播放器播放的流的类型为音乐流
		try {
//			mMediaPlayer.setDataSource(file.getmFilePath());
			mMediaPlayer = MediaPlayer.create(mContext, Uri.fromFile(new File(file.getmFilePath())));
//			mState = State.Preparing;
			
			mMediaPlayer.prepareAsync();	// Until the media player is prepared, we *cannot* call start() on it!
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("PlayService", "IOException playing current song: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void playing() {
		// TODO Auto-generated method stub
		processPlayNow();
		if(mMediaPlayer != null){
			mMediaPlayer.start();
		}
		mState = State.Paused;	//???
	}

	@Override
	public void paused() {
		// TODO Auto-generated method stub
		mState = State.Playing;
		if(mMediaPlayer.isPlaying()){
			mMediaPlayer.pause();
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		preparNextSong();
	}

	@Override
	public void preparing() {
		// TODO Auto-generated method stub
		mState = State.Stopped;
		mMediaPlayer.pause();
	}

	@Override
	public void setPosition(int position) {
		// TODO Auto-generated method stub
		if(mMediaPlayer != null){
			mMediaPlayer.seekTo(position);
		}
	}

	@Override
	public void preparNextSong() {
		// TODO Auto-generated method stub
		mState = State.Stopped;
		relaxResources(false);
		
		MusicInfo file = mPlayList.nextFile();
		if(file == null){
			return;
		}
		createMediaPlayerIfNeeded();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);	//设置媒体播放器播放的流的类型为音乐流
		try {
			mMediaPlayer.setDataSource(file.getmFilePath());
			mState = State.Preparing;
			
			mMediaPlayer.prepareAsync();	// Until the media player is prepared, we *cannot* call start() on it!
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("PlayService", "IOException playing next song: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		if(mMediaPlayer != null){
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public void relaxResources(boolean releaseMediaPlayer) {
		// TODO Auto-generated method stub
		if(releaseMediaPlayer && mMediaPlayer != null){
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	/**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
        	mMediaPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
        	mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
        	mMediaPlayer.setOnPreparedListener(this);
        	mMediaPlayer.setOnCompletionListener(this);
        	mMediaPlayer.setOnErrorListener(this);
        }
        else
        	mMediaPlayer.reset();
    }

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}
}
