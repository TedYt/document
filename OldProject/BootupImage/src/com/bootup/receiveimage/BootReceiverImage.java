package com.bootup.receiveimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class BootReceiverImage extends BroadcastReceiver {
	private static final int FILE_EXISTS = 1;
	private static final int FILE_CREATE_SUCCESS = 2;
	private static final int FILE_CREATE_FAIL = 3;
	private static final String CACHE_FILE_NAME = "NativeImageCount";
	
	private static final String CACHE_MUSIC_FILE_COUNT = "NativeMusicCount";//yutao 2015.01.16
	private static final String CACHE_VIDEO_FILE_COUNT = "NativeVideoCount";//yutao 2015.01.16

	private static final String IMAGE_DIR_PATH_WITHOUT_SDCARD = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/Pictures";
	
	private static final String MUSIC_DIR_PATH_IN_PHONE = Environment 
			.getExternalStorageDirectory().getAbsolutePath() + "/Music";//yutao 2015.01.16
	private static final String VIDEO_DIR_PATH_IN_PHONE = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/Videos";//yutao 2015.01.16
	
	private static final String IMAGE_DIR_PATH_WITH_SDCARD = "/storage/sdcard1/Pictures";
	private static final String BOOTUP_SOURCE_DIR_PATH = "/system/media/bootupResource";

	public static final String TAG = "BootReceiverImage";
	public static final String PICTURE_PATH_TAG = "picture_path";

	private Context mContext;
	
	final List<String> mPathList = new ArrayList<String>();

	private boolean createImageDir(String dirPath) {
		File imageDir = new File(dirPath);
		if (imageDir.exists()) {
			return true;
		} else {
			return imageDir.mkdirs();
		}
	}

	private String getImageDirPath() {
		if (createImageDir(IMAGE_DIR_PATH_WITH_SDCARD)) {
			return IMAGE_DIR_PATH_WITH_SDCARD;
		} else if (createImageDir(IMAGE_DIR_PATH_WITHOUT_SDCARD)) {
			return IMAGE_DIR_PATH_WITHOUT_SDCARD;
		}

		return null;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;

		Log.i(TAG, "onReceive, BootReceiverImage, action:" + intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			// Uri path = intent.getData();
			// Log.i(TAG, "OnReceive, mounted:"+path.getPath());

			// Timer timer = new Timer();
			// timer.schedule(new TimerTask() {
			// public void run() {
			// copyImagesToPhoneStorage();
			// }
			// }, 10000);
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			copyImagesToPhoneStorage();
			copyMusicToPhoneStorage();
			copyVideoToPhoneStorage();
			
			scanFiles();
			
		}
	}

	/**
	 * yutao
	 * 2015.01.16
	 */
	private void scanFiles() {
		try {
			Intent service = new Intent(mContext, CopyImageService.class);
			service.putStringArrayListExtra(PICTURE_PATH_TAG,
					(ArrayList<String>) mPathList);
			mContext.startService(service);
		} catch (Exception e) {
			Log.e(TAG, "Can't start service." + e);
		}
	}

	private String convertFileName(File file) {
		String fileName = file.getName();

		if (fileName.equals("sugar.mp4")) {
			return "SUGAR macaron.mp4";
		} else if (fileName.equals("sugar2.mp4")) {
			return "SUGAR aesthetics techniques.mp4";
		} else {
			return fileName;
		}
	}

	/**
	 * yutao copy video file
	 * 2015.01.16
	 */
	private void copyVideoToPhoneStorage() {
		int flag = FILE_CREATE_FAIL;
		String videodirpath;
		
		//保证恢复出厂设置的时候，重新加载，重启的时候不重新加载
		if (getNativeVideoCount() >= 0){
			return;
		}
		
		Log.i(TAG, "Entry into copyVideoToPhoneStorage");
		File sourceFileDir = new File(BOOTUP_SOURCE_DIR_PATH);
		if (!sourceFileDir.exists() || !sourceFileDir.isDirectory()) {
			return;
		}
		
		File[] sourceFiles = sourceFileDir.listFiles();
		int totalFile = sourceFiles.length;
		
		videodirpath = getVideodirPath();

		if (videodirpath == null){
			Log.w(TAG, "can't make video dir!!");
			return;
		}
		
		mPathList.add(videodirpath);//yutao
		
		int fileCount = 0;
		String fileName = null;
		for (int index = 0; index < totalFile; index++) {
			String name = convertFileName(sourceFiles[index]);
			if (!name.contains(".MP4")){
				continue;
			}
			
			fileName = videodirpath + "/" + name;
			flag = copyFileFromResource(sourceFiles[index], fileName);
			mPathList.add(fileName);
			if (flag == FILE_EXISTS || flag == FILE_CREATE_SUCCESS) {
				fileCount++;
			}
		}

		Log.i(TAG, "copy video files, count:" + fileCount);
		saveNativeVideoCount(fileCount);
	}

	/**
	 * yutao
	 * 2015.01.16
	 */
	private void saveNativeVideoCount(int count) {
		try {
			FileOutputStream lFile = mContext.openFileOutput(CACHE_VIDEO_FILE_COUNT,
					Context.MODE_PRIVATE);
			lFile.write(count);
			lFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * yutao
	 * 2015.01.16
	 */
	private int getNativeVideoCount() {
		int count = 0;
		try {
			FileInputStream lFile = mContext.openFileInput(CACHE_VIDEO_FILE_COUNT);
			count = lFile.read();
			Log.d(TAG,"native Video Count  = " + count);
			lFile.close();
		} catch (FileNotFoundException e) {
			count = -1; //文件不存在，说明是刷机后第一次执行，或者恢复出厂设置
		} catch (IOException e) {
			count = -1;
		}
		return count;
	}

	/**
	 * yutao copy music files
	 * 2015.01.16 
	 */
	private void copyMusicToPhoneStorage() {
		int flag = FILE_CREATE_FAIL;
		String musicdirpath;
		
		//保证恢复出厂设置的时候，重新加载，重启的时候不重新加载
		if(getNativeMusicCount() >=0 ){
			return;
		}
		
		Log.i(TAG, "Entry into copyImagesToPhoneStorage");
		File sourceFileDir = new File(BOOTUP_SOURCE_DIR_PATH);
		if (!sourceFileDir.exists() || !sourceFileDir.isDirectory()) {
			return;
		}
		
		File[] sourceFiles = sourceFileDir.listFiles();
		int totalFile = sourceFiles.length;
		
		musicdirpath = getMusicdirPath();

		if (musicdirpath == null){
			Log.w(TAG, "can't make music dir!!");
			return;
		}
		
		mPathList.add(musicdirpath);//yutao
		
		int fileCount = 0;
		String fileName = null;
		for (int index = 0; index < totalFile; index++) {
			String name = convertFileName(sourceFiles[index]);
			if (!name.contains(".mp3")){
				continue;
			}
			
			fileName = musicdirpath + "/" + name;// sourceFiles[index].getName();
			flag = copyFileFromResource(sourceFiles[index], fileName);
			mPathList.add(fileName);
			if (flag == FILE_EXISTS || flag == FILE_CREATE_SUCCESS) {
				fileCount++;
			}
		}

		Log.i(TAG, "copy Musicfiles, count:" + fileCount);
		saveNativeMusicCount(fileCount);
	}
	
	/**
	 * yutao
	 * 2015.01.16
	 */
	private int getNativeMusicCount() {
		int count = 0;
		try {
			FileInputStream lFile = mContext.openFileInput(CACHE_MUSIC_FILE_COUNT);
			count = lFile.read();
			Log.d(TAG,"native ImagesCount  = " + count);
			lFile.close();
		} catch (FileNotFoundException e) {
			count = -1;//文件不存在，说明是刷机后第一次执行，或者恢复出厂设置
		} catch (IOException e) {
			count = -1;
		}
		return count;
	}

	/**
	 * yutao
	 * 2015.01.16
	 */
	private void saveNativeMusicCount(int count) {
		try {
			FileOutputStream lFile = mContext.openFileOutput(CACHE_MUSIC_FILE_COUNT,
					Context.MODE_PRIVATE);
			lFile.write(count);
			lFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * yutao reprogram this function 
	 * 2015.01.16
	 */
	public void copyImagesToPhoneStorage() {
		int flag = FILE_CREATE_FAIL;
		String imgdirpath;//yutao
		
		if (getNativeImagesCount() >= 0){
			return;
		}
		
		Log.i(TAG, "Entry into copyImagesToPhoneStorage");
		File sourceFileDir = new File(BOOTUP_SOURCE_DIR_PATH);
		if (!sourceFileDir.exists() || !sourceFileDir.isDirectory()) {
			return;
		}
		File[] sourceFiles = sourceFileDir.listFiles();
		int totalFile = sourceFiles.length;

		imgdirpath = getImageDirPath();
		
		Log.i(TAG, "getNativeImagesCount() = " + getNativeImagesCount()
				+ ", exist = " + !IsEmptyFileExist(imgdirpath, sourceFiles));
		
		mPathList.add(imgdirpath);
		
		int fileCount = 0;
		String fileName = null;
		for (int index = 0; index < totalFile; index++) {
			String name = convertFileName(sourceFiles[index]);
			if (!name.contains(".jpg") && !name.contains(".bmp") ){
				continue;
			}			
			fileName = imgdirpath + "/" + convertFileName(sourceFiles[index]);// sourceFiles[index].getName();
			flag = copyFileFromResource(sourceFiles[index], fileName);
			mPathList.add(fileName);
			if (flag == FILE_EXISTS || flag == FILE_CREATE_SUCCESS) {
				fileCount++;
			}
		}

		Log.i(TAG, "copyImagesToPhoneStorage, count:" + fileCount);
		saveNativeImagesCount(fileCount);
	}

	/**
	 * yutao
	 * 2015.01.16
	 */
	private String getVideodirPath() {
		File dir = new File(VIDEO_DIR_PATH_IN_PHONE);
		boolean result = true;
		if (!dir.exists()) {
			result =  dir.mkdirs();
		}
		
		if (result){
			return VIDEO_DIR_PATH_IN_PHONE;
		}else {
			return null;
		}
	}

	/**
	 * yutao
	 * 2015.01.16
	 */
	private String getMusicdirPath() {
		File dir = new File(MUSIC_DIR_PATH_IN_PHONE);
		boolean result = true;
		if (!dir.exists()) {
			result = dir.mkdirs();
		}
		
		if (result){
			return MUSIC_DIR_PATH_IN_PHONE;
		}else {
			return null;
		}
	}

	private boolean IsEmptyFileExist(String path, File[] sourceFiles) {
		for (int index = 0; index < sourceFiles.length; index++) {
			File dstFile = new File(path + convertFileName(sourceFiles[index]));
			if (dstFile.exists()) {
				if (dstFile.length() == 0) {
					Log.e(TAG, "EmptyFileExist,file" + dstFile);
					return true;
				}
			}
		}

		return false;
	}

	private int copyFileFromResource(File srcFile, String dstPath) {
		int retValue = FILE_CREATE_FAIL;
		InputStream inStream = null;
		OutputStream outStream = null;
		File dstFile = new File(dstPath);

		if (dstFile.exists() && dstFile.length() > 0)// zhupeng
		{
			return FILE_EXISTS;
		}

		try {
			int onceReadCount = 0;
			byte buffer[] = new byte[1024 * 2048];

			Log.i(TAG, "copyFileFromResource, before inStream:");
			inStream = new FileInputStream(srcFile); // mContext.getResources().openRawResource(id);
			outStream = new FileOutputStream(dstPath);

			Log.i(TAG, "copyFileFromResource, inStream:" + inStream);
			while ((onceReadCount = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, onceReadCount);
			}

			inStream.close();
			outStream.close();
			retValue = FILE_CREATE_SUCCESS;

		} catch (IOException e) {
			Log.i(TAG, "copyFileFromResource, e:" + e);
			retValue = FILE_CREATE_FAIL;

			try {
				if (inStream != null) {
					inStream.close();
				}
				if (outStream != null) {
					outStream.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return retValue;
	}

	public final int getNativeImagesCount() {
		int count = 0;
		try {
			FileInputStream lFile = mContext.openFileInput(CACHE_FILE_NAME);
			count = lFile.read();
			Log.d(TAG,"native ImagesCount  = " + count);
			lFile.close();
		} catch (FileNotFoundException e) {
			count = -1; //文件不存在，说明是刷机后第一次执行，或者恢复出厂设置  yutao 2015.01.16
		} catch (IOException e) {
			count = -1;
		}
		return count;
	}

	public final void saveNativeImagesCount(int count) {
		try {
			FileOutputStream lFile = mContext.openFileOutput(CACHE_FILE_NAME,
					Context.MODE_PRIVATE);
			lFile.write(count);
			lFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
