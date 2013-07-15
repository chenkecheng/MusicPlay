package roco.kcchen.musicplay;

public interface IPlayService {
	/**
	 * 得到当前播放的音乐信息
	 * */
	public MusicInfo getCurrentMusic();
	/**
	 * 获取当前播放的位置
	 * */
	public int getCurrentPlayPosition();
}
