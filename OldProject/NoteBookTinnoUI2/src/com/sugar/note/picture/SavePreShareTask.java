package com.sugar.note.picture;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.sugar.note.NoteImageView;
import com.sugar.note.PublicUtils;
import com.sugar.note.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.sugar.note.NoteView;

/**
 * Created by user on 9/23/14.
 */
public class SavePreShareTask extends AsyncTask<Object, Integer, String> {

    private ProgressDialog mProgress;
    private Activity mContext;

    public SavePreShareTask(Activity context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mProgress = new ProgressDialog(mContext);
        mProgress.setMessage(mContext.getResources().getString(R.string.picture_loading));
        mProgress.setProgressStyle(R.style.ProgressDialogStyle);
        mProgress.show();
    }

    @Override
    protected String doInBackground(Object... objects) {

        Bitmap bitmap = (Bitmap)objects[0];
        String filePath = (String)objects[1];
        Boolean isCreateBitmap = (Boolean)objects[2];

        if (bitmap == null){
            if (isCreateBitmap) {
                if (mContext instanceof NoteView) {
                    bitmap = PublicUtils.convertViewToBitmapEx(((NoteView)mContext).getEditArea());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        File f = new File(filePath);
        if (f.exists()) {
            return filePath;
        }

        //如果文件不存在，则将分享图片临时保存在指定的目录中，关机时会删掉
        File dir = new File(PublicUtils.FOLD_SHARE_PIC);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String path = PublicUtils.FOLD_SHARE_PIC + "/" +getFileNameFromPath(filePath);

        try {
            FileOutputStream fos = new FileOutputStream(path);
            if (null != fos) {
                //bitmap.compress(Bitmap.CompressFormat.PNG, 10, fos);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                fos.flush();
                fos.close();
                return filePath;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String path) {
        if (path == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);

        File f = new File(path);
        if (f.exists() && f.isFile()) {
            intent.setType("image/*");
            Uri u = Uri.fromFile(f);
            Log.d("tui", "u = " + u);
            intent.putExtra(Intent.EXTRA_STREAM, u);
        }

        //intent.putExtra(Intent.EXTRA_SUBJECT, "Share***********");
        //intent.putExtra(Intent.EXTRA_TEXT, "TEXT-----------------");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(Intent.createChooser(intent, mContext.getTitle()));

        if (mProgress != null) {
            mProgress.dismiss();
        }
    }

    private String getFileNameFromPath(String path) {
        String[] p = path.split("/");
        String name = p[p.length - 1];
        Log.d("tui", "mame = " + name);

        //String[] p1 = name.split("\\.");

        return name;
    }
}
