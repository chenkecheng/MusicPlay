package roco.kcchen.musicplay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import roco.kcchen.musicplay.PlayService.LocalBinder;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	static ViewPager mViewPager;
	
	static PlayService mPlayService = null;
	LocalBinder mLocalBinder;
	
	static SharedPreferences share;
	static SharedPreferences.Editor mEditer;
	
	private static final String FILENAME = "mldn";
	private static final String CURRENT_PLAY = "currentPlay";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		SQLiteOpenHelper helper = new MusicDatabaseHelper(this);	//定义数据库的辅助类
//		helper.getWritableDatabase();	//以修改的方式打开数据库

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
	
		share = this.getSharedPreferences(FILENAME, Activity.MODE_PRIVATE);
		mEditer = share.edit();

	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Intent mIntent = new Intent(this,PlayService.class);
		bindService(mIntent,mConnection,Context.BIND_AUTO_CREATE);
		
		updateHandler.postDelayed(new Updater(), 100);	//Millis 毫秒  启动Updater()线程，更新播放位置与内容
	}
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(mConnection);//解除服务的绑定
	}


	private ServiceConnection mConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// TODO Auto-generated method stub
			mLocalBinder = (LocalBinder)service;
			mPlayService = mLocalBinder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// TODO Auto-generated method stub
			mPlayService = null;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			
			
//			Fragment fragment = new DummySectionFragment();
//			Bundle args = new Bundle();
//			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//			fragment.setArguments(args);
//			return fragment;
			
			
			Fragment fragment = null;
			switch (position) {  
            case 0:  
                fragment = new HomeFragment();  
                break;  
            case 1:
            	fragment = new MListFragment();
            	break;
            case 2:
            	fragment = new PlayFragment();
            	break;
            
//            default:  
//                fragment = new DummySectionFragment();  
//                break; 
            }  
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
//	public static class DummySectionFragment extends Fragment {
//		/**
//		 * The fragment argument representing the section number for this
//		 * fragment.
//		 */
//		public static final String ARG_SECTION_NUMBER = "section_number";
//
//		public DummySectionFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
//					container, false);
//			TextView dummyTextView = (TextView) rootView
//					.findViewById(R.id.section_label);
//			dummyTextView.setText(Integer.toString(getArguments().getInt(
//					ARG_SECTION_NUMBER)));
////			dummyTextView.setText(R.string.);
//			return rootView;
//		}
//	}
//	
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		
		public static int[] layout_res = {R.layout.home,R.layout.mlist,R.layout.play}; 

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			int currentTab = getArguments().getInt(ARG_SECTION_NUMBER)-1;
			View rootView = inflater.inflate(layout_res[currentTab],container, false);
			
			return rootView;
		}

	}
	
	public static class HomeFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		private Context context;
		private String homeListOption[] = {"当前播放","当前播放列表","历史播放","喜爱（列表）"};

		public HomeFragment() {
		}
	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.home,
					container, false);
			
			Button button = (Button)rootView.findViewById(R.id.button1);
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(rootView.getContext(), "测试", Toast.LENGTH_LONG).show();
					Dialog mDialog = new AlertDialog.Builder(rootView.getContext()).setTitle("自动搜索添加音乐")
							.setMessage("你确定要自动添加音乐文件?").setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int wButton) {
											// TODO Auto-generated method stub
											
										}
									}).setNeutralButton("手动添加",
											new DialogInterface.OnClickListener() {											
												@Override
												public void onClick(DialogInterface dialog, int wButton) {
													// TODO Auto-generated method stub
													
												}
											}).setNegativeButton("取消", 
													new DialogInterface.OnClickListener() {													
														@Override
														public void onClick(DialogInterface dialog, int wButton) {
															// TODO Auto-generated method stub
															
														}
													}).create();
					mDialog.show();
				}
			});
			
			ListView listView1 = (ListView)rootView.findViewById(R.id.listView_home);
			listView1.setAdapter(new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_expandable_list_item_1,homeListOption));
			return rootView;
		}
	}
	
	public static class MListFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
			public static final String ARG_SECTION_NUMBER = "section_number";

			public MListFragment() {
			}
			
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
				final View rootView = inflater.inflate(R.layout.mlist,
						container, false);
				//获得音乐列表
				MusicInfoUtils mMusicInfoUtils = new MusicInfoUtils(rootView.getContext());
				final ArrayList<MusicInfo> mMusicList = mMusicInfoUtils.getMusicList();//获得设备中所有的音乐文件
				

				List<HashMap<String, Object>> itemList = new ArrayList<HashMap<String, Object>>();
				for(MusicInfo mMusicInfo:mMusicList){
					HashMap<String,Object> item_map = new HashMap<String,Object>();
					item_map.put("musicName", mMusicInfo.getmFileTitle());
					item_map.put("artistName", mMusicInfo.getmSinger());
					item_map.put("duration", MusicInfo.formatDuration(mMusicInfo.getmDuration()));
					item_map.put("playButton","播放");
					itemList.add(item_map);
				}
				mBaseAdapter ListItemAdapter = new mBaseAdapter(rootView.getContext(),itemList,mMusicList,R.layout.mlist_list_view,
						new String[]{"musicName","artistName","duration","playButton"},
						new int[]{R.id.mlist_musicName,R.id.mlist_artist,R.id.mlist_timer,R.id.mlist_play});
				ListView musicListView = (ListView)rootView.findViewById(R.id.listView_music_list);
				musicListView.setAdapter(ListItemAdapter);
				musicListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						
//						//页面跳转
						mViewPager.setCurrentItem(2);

					}
				});
				
				return rootView;
			}
//			intent.setClass(getActivity()，跳转的Activity.class);
			public class mBaseAdapter extends BaseAdapter{
				private class ViewHolder{
		    		TextView musicName;		//音乐名称
		    		TextView artistName;	//艺术家
		    		TextView duration;		//时间长度
		    		Button playButton;		//播放按钮
		    		
		    	}
				
				private List<HashMap<String,Object>> mMusicInfoList;
				private ArrayList<MusicInfo> mmMusicList;
		        private LayoutInflater mInflater;
		        private Context mContext;
		        private String[] keyString;
		        private int[] valueViewID;
		        private ViewHolder holder;
				
		        public mBaseAdapter(Context context, List<HashMap<String,Object>> viewInfoList,ArrayList<MusicInfo> mMusicList ,int resource,
		                String[] from , int[] to){
		        	mContext = context;
		        	mMusicInfoList = viewInfoList;
		        	mmMusicList = mMusicList;
		        	mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
		        	keyString = new String[from.length];
		        	valueViewID = new int[to.length];
		        	
		        	//将数组拷贝到
		            System.arraycopy(from , 0, keyString, 0, from.length);
		            System.arraycopy(to, 0, valueViewID, 0, to.length);

		        }
		        
				@Override
				public int getCount() {
					// TODO Auto-generated method stub
					return mMusicInfoList.size();
				}

				@Override
				public Object getItem(int position) {
					// TODO Auto-generated method stub
					return mMusicInfoList.get(position);
//					return mmMusicList.get(position);
				}

				@Override
				public long getItemId(int position) {
					// TODO Auto-generated method stub
					return position;
				}
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					// TODO Auto-generated method stub
					if(convertView != null){
						holder = (ViewHolder)convertView.getTag();
					}else{
						convertView = mInflater.inflate(R.layout.mlist_list_view, null);
						holder = new ViewHolder();
						holder.musicName = (TextView)convertView.findViewById(R.id.mlist_musicName);
						holder.artistName = (TextView)convertView.findViewById(R.id.mlist_artist);
						holder.duration = (TextView)convertView.findViewById(R.id.mlist_timer);
						holder.playButton = (Button)convertView.findViewById(R.id.mlist_play);
						convertView.setTag(holder);
					}
					
					//想组件中添加资源与动作
					HashMap<String,Object> viewInfo = mMusicInfoList.get(position);
					final MusicInfo musicInfo = mmMusicList.get(position);
					if(viewInfo != null){
						String musicNameStr = (String)viewInfo.get(keyString[0]);
						String artistStr = (String)viewInfo.get(keyString[1]);
						String duration = (String)viewInfo.get(keyString[2]);
						
						holder.musicName.setText(musicNameStr);
						holder.artistName.setText(artistStr);
						holder.duration.setText(duration);
						holder.playButton.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub								
								
								//记录播放的歌曲
								mEditer.putInt(CURRENT_PLAY, musicInfo.getId());
								mEditer.commit();
								
								mPlayService.processPlayPauseRequest();
							}							
						});//添加播放按钮的动作
					}					
					return convertView;
				}
				
			}
			
//			
//			public class mButtonListener implements OnClickListener {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
////					String aa = binder.testService();
//////					Toast.makeText(getActivity(), aa, Toast.LENGTH_LONG);
////					Log.d(ARG_SECTION_NUMBER, aa);
//					mPlayService.processPlayNow(mBaseAdapter.this.getItem(position))
//				}
//				
//			}
			
	}
	
	public static class PlayFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
			public static final String ARG_SECTION_NUMBER = "section_number";

			public PlayFragment(){
			}
		
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
				final View rootView = inflater.inflate(R.layout.play,
						container, false);
				
				SeekBar playSeekBar = (SeekBar)rootView.findViewById(R.id.seekBar_play);
				playSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {	//进度条停止拖动时候触发事件
						// TODO Auto-generated method stub
						mPlayService.setPosition(seekBar.getProgress());
					}
				});
				
				ImageButton playButton = (ImageButton)rootView.findViewById(R.id.music_play);
				playButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
//						String musicPath = "file:///android_asset/Next_to_Me.mp3";
//						String musicPath = Environment.getExternalStorageState()+"/Summer Love.mp3";
//						Log.d("音频路径", musicPath);
//						String musicPath = "./mnt/sdcard/Summer Love.mp3";
////						String musicPath = "/assets/Next_to_Me.mp3";
//						PlayService mPlay = new PlayService(rootView.getContext(),musicPath);
//						mPlay.playAction();
						mPlayService.processPlayPauseRequest();
						
					}
				});
				
				
				return rootView;
			}
	}
	
	private Handler updateHandler = new Handler();	//实现非UI线程更新UI界面
	/**
	 * 用于监听播放内容
	 * */
	private class Updater implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
//			android.app.FragmentManager fragmentManager = getFragmentManager();
//			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			
			
			int pagePos = mViewPager.getCurrentItem();
//			String str = ""+pagePos;
//			Log.d("page", str);
			
			
			
			
			//获得当前播放的音乐
			final MusicInfo currentMusic = mLocalBinder.getCurrentMusic();	//会出现空指针的问题
			if(currentMusic != null){
//				int duration = currentMusic.getmDuration();
//				int position = mLocalBinder.getCurrentPlayPosition();
//				playSeekBar.setMax(duration);
//				playSeekBar.setProgress(position);
//				
//				homeMusicName.setText(currentMusic.getmFileTitle());
//				homeArtist.setText(currentMusic.getmSinger());
//				
//				switch(mPlayService.mState){
//				case Stopped:
//				case Playing:
//				case Paused:
//				case Preparing:
//				}
				
				switch(pagePos){
				case 0:homePageUpdate(currentMusic);break;	//Home页面
				case 1:mListPageUpdate();break; //mList页面
				case 2:playPageUpdate(currentMusic);break; //Play页面
				}
			}else{
				
			}

			
			updateHandler.postDelayed(this, 100);
		}
		public void homePageUpdate(MusicInfo currentMusic){
			TextView homeMusicName = (TextView)findViewById(R.id.textView_musicName);
			TextView homeArtist = (TextView)findViewById(R.id.textView_artist);
			ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar_home);
			
			int duration = currentMusic.getmDuration();
			int position = mLocalBinder.getCurrentPlayPosition();
			
			homeMusicName.setText(currentMusic.getmFileTitle());
			homeArtist.setText(currentMusic.getmSinger());
			progressBar.setMax(duration);
			progressBar.setProgress(position);
		}
		public void mListPageUpdate(){
//			Button playButton = (Button)findViewById(R.id.mlist_play);
//			
//			switch(mPlayService.mState){
//			case Stopped:
//			case Playing:playButton.setText("暂停");
//			case Paused:playButton.setText("播放");
//			case Preparing:
//			}
		}
		public void playPageUpdate(MusicInfo currentMusic){
			TextView playPageMusicName = (TextView)findViewById(R.id.textView_playMusicName);
			TextView playPageArtist = (TextView)findViewById(R.id.textView_playArtist);
			TextView playPageTime = (TextView)findViewById(R.id.textView_playTime);
			SeekBar playSeekBar = (SeekBar)findViewById(R.id.seekBar_play);
			
			int duration = currentMusic.getmDuration();
			int position = mLocalBinder.getCurrentPlayPosition();
			
			playPageMusicName.setText(currentMusic.getmFileTitle());
			playPageArtist.setText(currentMusic.getmSinger());
			playPageTime.setText("-"+currentMusic.formatDuration(currentMusic.getmDuration()-position));

			playSeekBar.setMax(duration);
			playSeekBar.setProgress(position);
			
		}
		
	}

}
