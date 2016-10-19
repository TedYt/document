package com.zale.ydan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;

public class CopyPictureAsync extends AsyncTask<String, Integer, Boolean> {

	static final String TAG = "YDan";///CopyPicture"
	protected static final int BUFFER_SIZE = 2048 * 1024;
	
	//private String mDstFolder;
	
	private File mDstFolder;
	
	ProgressDialog mProgressBar;
	
	private Handler mHandler;
	
	public CopyPictureAsync(Context c, Handler handler) {
		super();
		String external = Environment.getExternalStorageDirectory().getAbsolutePath();
		log("externalStorageDir = " + external);
		String dstFolder = external + "/YDan";
		mDstFolder = new File(dstFolder);
		if (!mDstFolder.exists()){
			mDstFolder.mkdirs();
		}
		
		if (!mDstFolder.exists()){
			log("***DstFolder doesn't exists***");
		}
		
		mProgressBar = new ProgressDialog(c);
		
		mHandler = handler;
		
	}

	@Override
	protected Boolean doInBackground(String... datas) {
		// TODO Auto-generated method stub
		for (int i = 0; i < datas.length; i++){
			String s = datas[i];
			log("data = " + s);
			File file = new File(s);
			if (file.exists()){
				String fileName = file.getName();
				File dstFile = new File(mDstFolder.getAbsoluteFile() + "/" + fileName);
				copyFile(file, dstFile);
			}
			publishProgress(i);
		}
		
		return null;
	}
	
	private void copyFile(File srcFile, File dstFile){
		byte[] buffer = new byte[BUFFER_SIZE];
		
		if (srcFile == null){
			log("***srcFile == null***");
			return;
		}
		if (dstFile == null){
			log("***dstFile == null***");
		}
		
		FileInputStream in = null;
        FileOutputStream out = null;
        
		try {
			if (!dstFile.createNewFile()){
				log("***!dstFile.createNewFile()***");
				return;
			}
			if (!srcFile.exists()){
				log("***!srcFile.exists()***");
				return;
			}
			
			in = new FileInputStream(srcFile);
			out = new FileOutputStream(dstFile);
			
			int len = 0;
			while((len = in.read(buffer)) > 0){
				out.write(buffer, 0, len);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log("***IOException e 1 : ***" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}

				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				log("***IOException e 2***");
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPreExecute() {
		mProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressBar.show();
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		mProgressBar.dismiss();
		Message msg = mHandler.obtainMessage();
		msg.what = 0;
		msg.obj = "YDan";
		msg.sendToTarget();
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		mProgressBar.setProgressNumberFormat(null);
		mProgressBar.incrementProgressBy(values[0]);
	}
	
	private void log(String msg){
		Log.d(TAG, "CopyPicture -- " + msg);
	}
}

