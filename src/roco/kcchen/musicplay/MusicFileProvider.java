package roco.kcchen.musicplay;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Environment;

public class MusicFileProvider {
	public MusicFileProvider(){
		
	}
	
	public List<String> getMusicFile(){
		List<String> mfiles =new ArrayList<String>();
		File rootFile = Environment.getExternalStorageDirectory();//¶ÁÈ¡SD¿¨Â·¾¶
		
		@SuppressWarnings("unused")
		FileFilter mFileFilter = new FileFilter(){
			@Override
			public boolean accept(File f) {
				// TODO Auto-generated method stub
				if(f.isDirectory()){
					return false;
				}
				String fileName = f.getName();
				int index = fileName.lastIndexOf(".");
				if(index == -1){
					return false;
				}else if(index == (fileName.length()-1)){
					return false;
				}else {
		            return ("mp3").equals(fileName.substring(index+1));
		        }
			}
		};
//		File[] files = rootFile.listFiles(mFileFilter);
		
		
		FilenameFilter mFilenameFilter = new FilenameFilter(){

			@Override
			public boolean accept(File dir, String file_name) {
				// TODO Auto-generated method stub
				return isMp3(file_name);
			}
			
			public boolean isMp3(String file) {
				if (file.toLowerCase().endsWith(".mp3")){
					return true;
				}else{
					return false;
				}
			} 
		};
		File[] files = rootFile.listFiles(mFilenameFilter);
		for (int i = 0; i < files.length; i++){
			String file=files[i].getPath();
			mfiles.add(file);
		} 
		
		
		return mfiles;
	}
}
