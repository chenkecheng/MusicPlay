package roco.kcchen.musicplay;

public interface MusicPlayable {
	/**
	 * ����
	 * */
	public void playing();	
	/**
	 * ��ͣ
	 * */
	public void paused();
	/**
	 * ֹͣ
	 * */
	public void stop();
	/**
	 * ׼��
	 * */
	public void preparing();
	/**
	 * ���ò��ŵ�λ��
	 * */
	public void setPosition(int position);
	/**
	 * ׼����һ�׸�
	 * */
	public void preparNextSong();
	/**
	 * �õ���ǰ���ֵĲ���λ��
	 * */
	public int getPosition();
	/**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer ȷ���Ƿ�Ӧ���ͷ�ý����Դ
     */
	public void relaxResources(boolean releaseMediaPlayer);
}
