package com.sugar.note.picture;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.provider.MediaStore.Images;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sugar.note.R;
import com.sugar.note.PublicUtils;

/**
 * Created by user on 7/21/14.
 */
public class ImageDownloader {

    private Context mContext;

    private int MSG_DOWNLOAD = 1;
    private final int WIDTH = 180;
    private final int HEIGHT = 180;

    private final String THUMBNAIL_FOLDER = "/.thumbnails";
    private final String NAME_SUFFIX = "-thumbnails.";

    private ExecutorService mThreadPool = null;

    private OnImageShowListener mListener;

    private Cursor mCursor;

    private final int THUMBNAIL_ID = 0;
    private final int THUMBNAIL_DATA = 1;
    private final int THUMBNAIL_IMAGE_ID = 2;
    private final int THUMBNAIL_KIND = 3;
    private final int THUMBNAIL_WIDTH = 4;
    private final int THUMBNAIL_HEIGHT = 5;

    /**
     *
     * @param mContext
     */
    public ImageDownloader(Context mContext) {
        this.mContext = mContext;

        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Images.Thumbnails.EXTERNAL_CONTENT_URI;
        Log.d("tui", "EXTERNAL_CONTENT_URI = " + uri.toString());
        mCursor = cr.query(uri, null, null, null, null);
    }

    /**
     *
     * @param path
     * @param listener
     * @param threadNum
     * @param isNeedCompress
     * @return
     */
    public Bitmap downLoadImage(final String path, final OnImageShowListener listener,
                                int threadNum, final boolean isNeedCompress){

        return downLoadImage(path, listener, threadNum, isNeedCompress, -1);
    }

    public Bitmap downLoadImage(final String path, final OnImageShowListener listener,
                                int threadNum, final boolean isNeedCompress, final int imageID){

        Bitmap thumbnail = findThumbnail(imageID);
        if (thumbnail != null){
            return thumbnail;
        }

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                listener.onImageShow(path, (Bitmap) msg.obj);
            }
        };

        getThread(threadNum).execute(new Runnable() {
            @Override
            public void run() {
                Message msg;
                Bitmap bitmap = getBitmap(path, isNeedCompress);
                if (imageID == -1){
                    msg = Message.obtain(handler,MSG_DOWNLOAD, bitmap);
                }else{
                    Bitmap thumbnail = createThumbnail(path, bitmap, imageID);
                    msg = Message.obtain(handler,MSG_DOWNLOAD, thumbnail);
                }

                msg.sendToTarget();
            }
        });

        return null;
    }

    /**
     *
     * @param bitmap
     */
    private Bitmap createThumbnail(String path, Bitmap bitmap, int id) {

        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, WIDTH, HEIGHT);
        if (thumbnail == null) {//处理0字节的图片
            Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.error_picture);
            return b;
        }

        String thumbnailName = saveThumbnail(path, thumbnail);

        ContentValues values = new ContentValues();
        values.put(mCursor.getColumnName(THUMBNAIL_DATA), thumbnailName);
        values.put(mCursor.getColumnName(THUMBNAIL_IMAGE_ID), id);
        values.put(mCursor.getColumnName(THUMBNAIL_KIND), Images.Thumbnails.MINI_KIND);
        values.put(mCursor.getColumnName(THUMBNAIL_WIDTH), WIDTH);
        values.put(mCursor.getColumnName(THUMBNAIL_HEIGHT), HEIGHT);

        ContentResolver cr = mContext.getContentResolver();
        cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI,values);

        return thumbnail;
    }

    private String saveThumbnail(String path, Bitmap thumbnail) {

        String folder = getThumbnailFolder(path);
        File file = new File(folder);
        if(!file.exists()){
            file.mkdirs();
        }

        String thumbnailName = folder + "/" + getThumbnailName(path);
        //Log.d("tui", "saveThumbnail, thumbnailName = " + thumbnailName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(thumbnailName);
            Log.e("tui", "saveThumbnail, thumbnailName = " + thumbnailName);
            if (fos != null) {
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }

        return thumbnailName;
    }

    private String getThumbnailFolder(String path) {
        int index = path.lastIndexOf("/");
        String thumbnailPath = path.substring(0,index);

        return thumbnailPath + THUMBNAIL_FOLDER;
    }

    private String getThumbnailName(String path) {

        int index = path.lastIndexOf("/");
        String thumbnailName = path.substring(index + 1);
        thumbnailName = thumbnailName.replace(".", NAME_SUFFIX);

        return thumbnailName;
    }

    /**
     *
     * @param imageID
     * @return
     */
    private Bitmap findThumbnail(int imageID) {

        if (mCursor.getCount() == 0){
            return null;
        }

        mCursor.moveToFirst();
        do {
            int id = mCursor.getInt(THUMBNAIL_IMAGE_ID);
            if (id == imageID){
                String path = mCursor.getString(THUMBNAIL_DATA);
                //Log.d("tui", "findThumbnail, path = " + path);
                return getBitmap(path, false);
            }
            mCursor.moveToNext();
        }while(!mCursor.isAfterLast());

        return null;
    }

    /**
     *
     * @return
     */
    public ExecutorService getThread(int threadNum){
        if (mThreadPool == null){
            synchronized (ExecutorService.class){
                if (mThreadPool == null){
                    mThreadPool = Executors.newFixedThreadPool(threadNum);
                }
            }
        }

        return mThreadPool;
    }

    /**
     *
     */
    public synchronized void cancelTask(){
        if (mThreadPool != null){
            mThreadPool.shutdownNow();
            mThreadPool = null;
        }
    }

    /**
     *
     * @param path
     * @return
     */
    private Bitmap getBitmap(String path, boolean isNeedCompress) {

        if (!isNeedCompress){
            return BitmapFactory.decodeFile(path);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeFile(path, options);

        int reqWidth = mContext.getResources().getDimensionPixelSize(R.dimen.noteview_image_view_width);
        int reqHeight = mContext.getResources().getDimensionPixelSize(R.dimen.noteview_image_view_height);
        options.inSampleSize = PublicUtils.getInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    public void OnDestroy(){
        if (mCursor != null){
            mCursor.close();
        }
    }

    public void setDownloadListener(OnImageShowListener listener){
        mListener = listener;
    }

    public interface OnImageShowListener
    {
        void onImageShow(String path,Bitmap bitmap);
    }

}
