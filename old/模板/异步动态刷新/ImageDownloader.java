package com.sugar.note.picture;

import android.content.ContentResolver;
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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 7/21/14.
 */
public class ImageDownloader {

    private Context mContext;

    private int MSG_DOWNLOAD = 1;

    private ExecutorService mThreadPool = null;

    private OnImageShowListener mListener;

    /**
     *
     * @param mContext
     */
    public ImageDownloader(Context mContext) {
        this.mContext = mContext;       
    }

    /**
     *
     * @param path
     * @param listener
     * @return
     */
    public Bitmap downLoadImage(final String path, final OnImageShowListener listener,
                                int threadNum, final boolean isNeedCompress){

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
                Bitmap bitmap = getBitmap(path, isNeedCompress);
                Message msg = Message.obtain(handler, MSG_DOWNLOAD, bitmap);
                msg.sendToTarget();
            }
        });

        return null;
    }

    private Bitmap findThumbnail(int imageID) {



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
            mThreadPool.shutdown();
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
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = 4;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    public void setDownloadListener(OnImageShowListener listener){
        mListener = listener;
    }

    public interface OnImageShowListener
    {
        void onImageShow(String path,Bitmap bitmap);
    }

}
