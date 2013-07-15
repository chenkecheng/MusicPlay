package roco.kcchen.musicplay;

/**
 * @author kcchen
 * */
public class MusicInfo {
	
	private int id;

	/**
     * fileName
     */
    private String mFileName = "";

    /**
     * song name
     */
    private String mFileTitle = "";

    /**
     * play total time
     */
    private int mDuration = 0;

    /**
     * singer
     */
    private String mSinger = "";

    /**
     * album name
     */
    private String mAlbum = "";

    /**
     * mYear
     */
    private String mYear = "";

    /**
     * mFileType
     */
    private String mFileType = "";

    /**
     * mFileSize
     */
    private String mFileSize = "";

    /**
     * mFilePath
     */
    private String mFilePath = "";
    
    /**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the FileName
	 */
	public String getmFileName() {
		return mFileName;
	}

	/**
	 * @param mFileName the mFileName to set
	 */
	public void setmFileName(String mFileName) {
		this.mFileName = mFileName;
	}

	/**
	 * @return the song name
	 */
	public String getmFileTitle() {
		return mFileTitle;
	}

	/**
	 * @param mFileTitle the song name to set
	 */
	public void setmFileTitle(String mFileTitle) {
		this.mFileTitle = mFileTitle;
	}

	/**
	 * @return the mDuration
	 */
	public int getmDuration() {
		return mDuration;
	}

	/**
	 * @param mDuration the mDuration to set
	 */
	public void setmDuration(int mDuration) {
		this.mDuration = mDuration;
	}

	/**
	 * @return the mSinger
	 */
	public String getmSinger() {
		return mSinger;
	}

	/**
	 * @param mSinger the mSinger to set
	 */
	public void setmSinger(String mSinger) {
		this.mSinger = mSinger;
	}

	/**
	 * @return the mAlbum
	 */
	public String getmAlbum() {
		return mAlbum;
	}

	/**
	 * @param mAlbum the mAlbum to set
	 */
	public void setmAlbum(String mAlbum) {
		this.mAlbum = mAlbum;
	}

	/**
	 * @return the mYear
	 */
	public String getmYear() {
		return mYear;
	}

	/**
	 * @param mYear the mYear to set
	 */
	public void setmYear(String mYear) {
		this.mYear = mYear;
	}

	/**
	 * @return the mFileType
	 */
	public String getmFileType() {
		return mFileType;
	}

	/**
	 * @param mFileType the mFileType to set
	 */
	public void setmFileType(String mFileType) {
		this.mFileType = mFileType;
	}

	/**
	 * @return the mFileSize
	 */
	public String getmFileSize() {
		return mFileSize;
	}

	/**
	 * @param mFileSize the mFileSize to set
	 */
	public void setmFileSize(String mFileSize) {
		this.mFileSize = mFileSize;
	}

	/**
	 * @return the mFilePath
	 */
	public String getmFilePath() {
		return mFilePath;
	}

	/**
	 * @param mFilePath the mFilePath to set
	 */
	public void setmFilePath(String mFilePath) {
		this.mFilePath = mFilePath;
	}
	
	public static String formatDuration(int d) {
        String min = Integer.toString((d/1000)/60);
        String sec = Integer.toString((d/1000)%60);
        if (sec.length() == 1) sec = "0"+sec;
        return min+":"+sec;
    }

}
