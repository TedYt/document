package com.bootup.receiveimage;

import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class CopyImageService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		try {
			if (intent != null) {
				List<String> scanPaths = intent
						.getStringArrayListExtra(BootReceiverImage.PICTURE_PATH_TAG);
				String[] paths = new String[scanPaths.size()];
				scanPaths.toArray(paths);
				MediaScannerConnection.scanFile(getApplicationContext(), paths,
						null, null);
				// sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				// Uri.parse("file://"+
				// Environment.getExternalStorageDirectory())));
			}
		} catch (Exception e) {
			Log.e(BootReceiverImage.TAG, "Can't start MediaScannerConnection."
					+ e);
		}

	}
}
