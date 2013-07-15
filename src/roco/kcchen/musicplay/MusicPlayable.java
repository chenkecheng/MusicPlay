package roco.kcchen.musicplay;

public interface MusicPlayable {
	/**
	 * 播放
	 * */
	public void playing();	
	/**
	 * 暂停
	 * */
	public void paused();
	/**
	 * 停止
	 * */
	public void stop();
	/**
	 * 准备
	 * */
	public void preparing();
	/**
	 * 设置播放的位置
	 * */
	public void setPosition(int position);
	/**
	 * 准备下一首歌
	 * */
	public void preparNextSong();
	/**
	 * 得到当前音乐的播放位置
	 * */
	public int getPosition();
	/**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer 确认是否应该释放媒体资源
     */
	public void relaxResources(boolean releaseMediaPlayer);
}
