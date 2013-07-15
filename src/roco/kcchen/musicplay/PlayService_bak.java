package roco.kcchen.musicplay;

import java.io.File;
import java.io.IOException;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.util.Log;

public class PlayService_bak extends Service implements OnCompletionListener, OnPreparedListener,
OnErrorListener{
	
	private final static String TAG = "PlayService";
	
	private String music_file;
	private Context context;
	private MediaPlayer mMediaPlayer = null;
	public PlayList mPlayList;
	// our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;
	
	NotificationManager mNotificationManager;
	Notification mNotification = null;
	 final int NOTIFICATION_ID = 1;
	
	// 指定播放状态
    enum State {
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    };

//    State mState = State.Retrieving;
    State mState = State.Stopped;
	
    //暂停的原因
    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    };
	
    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;
	
    
//	public PlayService(Context context,String file){
//		this.context = context;
//		this.music_file = file;
//		File t = new File(file);
//		if(t.exists()){
//			Log.d(TAG, "文件存在");
//		}else{
//			Log.d(TAG, "文件不存在！！！！！！！！");
//		}
//	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mPlayList = new PlayList(this);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	PlayService_bak getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayService_bak.this;
        }
    }
    
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
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
	
    
    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // String action = intent.getAction();

        return START_NOT_STICKY; // Means we started the service, but don't want it to
                                 // restart in case it's killed.
    }
    
    
	/**
	 * 处理播放与暂停请求
	 * */
	public void processPlayPauseRequest() {
    	switch (mState) {
    		case Stopped:
	            // If we're stopped, just go ahead to the next song and start playing
    			tryToGetAudioFocus();
	            playNextSong();
	            break;
	            
    		case Paused:
	            // If we're paused, just continue playback and restore the 'foreground service' state.
    			tryToGetAudioFocus();
    			mState = State.Playing;
	            setUpAsForeground(null);
	            configAndStartMediaPlayer();
	            break;
	            
    		case Playing:
    			mMediaPlayer.start();
    			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
    				
    				@Override
    				public void onCompletion(MediaPlayer media) {
    					// TODO Auto-generated method stub
    					media.release();
    				}
    			});
    			
    		case Preparing:
    			// Pause media player and cancel the 'foreground service' state.
                mState = State.Paused;
                mMediaPlayer.pause();
                relaxResources(false); // while paused, we always retain the MediaPlayer
                giveUpAudioFocus();
                break;
    	}
    }
	
	
	
	
	
	public void processPlayNowRequest() {
    	tryToGetAudioFocus();
    	playNextSong();
    }
    
    public void setPosition(int position) {
    	if (mMediaPlayer != null) {
    		mMediaPlayer.seekTo(position);
    	}
    }
	
	
	 void tryToGetAudioFocus() {
	        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
	                        && mAudioFocusHelper.requestFocus())
	            mAudioFocus = AudioFocus.Focused;
	    }
	 
	 /**
	     * Starts playing the next song. If manualUrl is null, the next song will be randomly selected
	     * from our Media Retriever (that is, it will be a random song in the user's device). If
	     * manualUrl is non-null, then it specifies the URL or path to the song that will be played
	     * next.
	     */
	    void playNextSong(/*String manualUrl*/) {
	        mState = State.Stopped;
	        relaxResources(false); // release everything except MediaPlayer

	        MusicInfo file = mPlayList.nextFile();
	        if (file == null) {
	        	return;
	        }

	        try {
	            // set the source of the media player a a content URI
	            createMediaPlayerIfNeeded();
	            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//	            mMediaPlayer.setDataSource(getApplicationContext(), file.getUri());
	            mMediaPlayer.setDataSource(file.getmFilePath());

	            mState = State.Preparing;
	            // MUST call this!
	            setUpAsForeground(file.getmFileTitle() + " (loading)");

	            // starts preparing the media player in the background. When it's done, it will call
	            // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
	            // the listener to 'this').
	            //
	            // Until the media player is prepared, we *cannot* call start() on it!
	            mMediaPlayer.prepareAsync();
	        }
	        catch (IOException ex) {
	            Log.e("PlayService", "IOException playing next song: " + ex.getMessage());
	            ex.printStackTrace();
	        }
	    }
	 
	    /**
	     * Configures service as a foreground service. A foreground service is a service that's doing
	     * something the user is actively aware of (such as playing music), and must appear to the
	     * user as a notification. That's why we create the notification here.
	     */
	    void setUpAsForeground(String text) {
	        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
	                new Intent(getApplicationContext(), MainActivity.class),
	                PendingIntent.FLAG_UPDATE_CURRENT);
	        mNotification = new Notification();
	        mNotification.tickerText = text == null ? text : "";
//	        mNotification.icon = R.drawable.ic_stat_playing;
	        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
	        mNotification.setLatestEventInfo(getApplicationContext(), "RandomMusicPlayer",
	                text, pi);
	        startForeground(NOTIFICATION_ID, mNotification);
	    }
	    
	    
	    
	    
	 // The volume we set the media player to when we lose audio focus, but are allowed to reduce
	 // the volume instead of stopping playback.
	 public final float DUCK_VOLUME = 0.1f;
	 void configAndStartMediaPlayer() {
	        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
	            // If we don't have audio focus and can't duck, we have to pause, even if mState
	            // is State.Playing. But we stay in the Playing state so that we know we have to resume
	            // playback once we get the focus back.
	            if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
	            return;
	        }
	        else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
	        	mMediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
	        else
	        	mMediaPlayer.setVolume(1.0f, 1.0f); // we can be loud

	        if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
	    }
	
	 
	 
	 //---------------------------------------------------------------------------------------------------
	public void playAction(){
		
		mMediaPlayer = MediaPlayer.create(this.context, Uri.fromFile(new File(this.music_file)));
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer media) {
				// TODO Auto-generated method stub
				media.release();
			}
		});
		if(mMediaPlayer != null){
			mMediaPlayer.stop();
		}
		try {
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			Log.d(TAG, "正在播放音频文件");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG, "音频播放出现异常");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG, "音频播放出现IO异常");
		}
	}
	public void pauseAction(){
		mMediaPlayer.pause();
	}
	public void stopAction(){
		mMediaPlayer.stop();
	}
	
	//-------------------------------------------------------------------------------------------------------
	/**
	 * 得到当前音乐的播放位置
	 * */
	public int getPosition() {
    	if (mMediaPlayer != null) {
    		return mMediaPlayer.getCurrentPosition();
    	}
    	
    	return 0;
    }
	
	/**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// 服务正被杀死, 确认我们已释放资源
		mState = State.Stopped;
		relaxResources(true);
		giveUpAudioFocus();
		super.onDestroy();
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
