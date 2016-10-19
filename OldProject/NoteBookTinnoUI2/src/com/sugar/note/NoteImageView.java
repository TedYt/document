package com.sugar.note;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.sugar.note.picture.SavePreShareTask;

import java.lang.ref.WeakReference;


public class NoteImageView extends Activity implements OnClickListener {  // implements OnClickListener
    private Context mContext;
    private Uri mUri;
    private static int mWidth;
    private static int mHeight;
    private Bitmap mBitmap;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_imageview);
        mContext = this;

        ((Button)findViewById(R.id.picture_save)).setOnClickListener(this);
        ((Button)findViewById(R.id.picture_share)).setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;

        ImageView note_image = (ImageView) findViewById(R.id.note_image_view);
        mFilePath = getIntent().getStringExtra("note_img_path");
        Log.d("tui", "lmj5588 NoteImageView onCreate note_image_path = " + mFilePath);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeFile(mFilePath, options);
        options.inSampleSize = PublicUtils.calculateInSampleSize(options, mWidth, mHeight);
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(mFilePath, options);

        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(mBitmap);
        note_image.setImageBitmap(weak.get());
        note_image.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("notedb", "andy3344 NoteImageView onResume  ");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mNoteAlarmUpdateReceiver);
        Log.e("notedb", "andy3344 NoteImageView onDestroy  ");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.picture_save:
                SavePictureTask task = new SavePictureTask();
                task.execute();
               break;
            case R.id.picture_share:
                SavePreShareTask task1 = new SavePreShareTask(this);
                task1.execute(mBitmap, mFilePath,false);
                break;
            default:
                break;
        }
    }


    class SavePictureTask extends AsyncTask<Object, Integer, Integer> {

        private ProgressDialog mProgress;
        private final String SAVE_PATH =
                Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Pictures/Note";

        private final int RESULT_DONE = 0;
        private final int RESULT_FAIL = 1;
        private final int RESULT_EXIST = 2;

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(mContext);
            mProgress.setMessage(mContext.getResources().getString(R.string.picture_saving));
            mProgress.setProgressStyle(R.style.ProgressDialogStyle);
            mProgress.show();
        }

        @Override
        protected Integer doInBackground(Object... objects) {
            if (mBitmap == null){
                return RESULT_FAIL;
            }

            FileOutputStream fos = null;
            File file = new File(SAVE_PATH);

            if (!file.exists()) {
                file.mkdirs();
            }

            String path = SAVE_PATH + "/" + getFileNameFromPath(mFilePath);

            File f = new File(path);
            if (f.exists()) {
                return RESULT_EXIST;
            }

            try {
                fos = new FileOutputStream(path);
                if (null != fos) {
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    return RESULT_DONE;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return RESULT_FAIL;
            } catch (IOException e) {
                e.printStackTrace();
                return RESULT_FAIL;
            }

            return RESULT_FAIL;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mProgress != null) {
                mProgress.dismiss();
            }

            if (result == RESULT_DONE) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + SAVE_PATH + "/" + getFileNameFromPath(mFilePath))));
                Toast.makeText(mContext,
                        mContext.getString(R.string.picture_save_done),
                        Toast.LENGTH_SHORT).show();
            } else if (result == RESULT_EXIST) {
                Toast.makeText(mContext,
                        mContext.getString(R.string.picture_save_exist),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext,
                        mContext.getString(R.string.picture_save_failed),
                        Toast.LENGTH_SHORT).show();
            }
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
