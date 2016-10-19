package com.sugar.note.picture;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.sugar.note.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 7/4/14.
 */
public class GridViewAdapter extends BaseAdapter implements AbsListView.OnScrollListener
        ,ImageDownloader.OnImageShowListener{

    private Context mContext;

    private GridView mGrid;

    private String[] mProjection = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
    };

    private final int PICTURE_ID = 0;
    private final int PICTURE_PATH = 1;
    private final int PICTURE_NAME = 2;
    private final int PICTURE_DATE_ADDED = 3;
    private final int PICTURE_DATE_MODIFIED = 4;
    private final int PICTURE_WIDTH = 5;
    private final int PICTURE_HEIGHT = 6;
    private final int PICTURE_MIME = 7;
    private final int PICTURE_FOLDER = 8;

    private final int RECENT_PICTURE_COUNT = 100;

    private final String IMAGE_MIME = "image";

    private int mFirstVisibleItem;
    private int mVisibleItemCount;

    private boolean isFirstEnter = true;
    private ImageDownloader mImageDownloader;

    private boolean isFromNoteViewActivity = true;

    private HashMap<Integer, Boolean> mMap ;
    private ArrayList<PictureAttr> mPictureAttrs;

    public GridViewAdapter(Context context, GridView grid, String path) {
        mContext = context;
        mGrid = grid;
        mGrid.setOnScrollListener(this);
        mImageDownloader = new ImageDownloader(context);
        mImageDownloader.setDownloadListener(this);
        initPictureCursor(path);
    }

    private void initPictureCursor(String path) {

        Uri uri;
        String volumeName = "external";
        uri = MediaStore.Images.Media.getContentUri(volumeName);

        String where = null;
        if (path != null){
            isFromNoteViewActivity = false;
            where = mProjection[PICTURE_FOLDER] + " like " + "'" + path + "'" +
                    " and " +
                    mProjection[PICTURE_MIME] + " like " + "'" + IMAGE_MIME + "%'";
        }

        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(uri, mProjection,
                where,
                null,
                mProjection[PICTURE_DATE_ADDED] + " desc ");

        Log.d("tui", "initPictureCursor, path = " + path + ", count = " + cursor.getCount());

        try{
            if (mMap != null) {
                mMap.clear();
            } else {
                mMap = new HashMap<Integer, Boolean>(cursor.getCount());
            }

            if (mPictureAttrs != null) {
                mPictureAttrs.clear();
            } else {
                mPictureAttrs = new ArrayList<PictureAttr>();
            }

            cursor.moveToFirst();
            for (int i = 0; !cursor.isAfterLast(); i ++){
                mMap.put(i, false);

                PictureAttr pa = new PictureAttr();
                pa.id = cursor.getInt(PICTURE_ID);
                pa.path = cursor.getString(PICTURE_PATH);
                mPictureAttrs.add(pa);

                cursor.moveToNext();
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 只能由onResume 方法调用
     */
    public void updateCursor(String path) {

        initPictureCursor(path);
    }

    @Override
    public int getCount() {
        if (isFromNoteViewActivity){ //从NoteView进入时，显示最近的100张图片
            int count = mPictureAttrs.size();
            return count >= RECENT_PICTURE_COUNT ? RECENT_PICTURE_COUNT : count;
        }else {
            return mPictureAttrs.size();
        }
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public String getItemPath(int index){
        return mPictureAttrs.get(index).path;
    }

    public boolean isItemSelected(int index){
        return mMap.get(index);
    }

    public void setItemSelected(int index, boolean isSelected){
        mMap.put(index, isSelected);
    }

    public void clearAll(){
        mMap.clear();
        mPictureAttrs.clear();
    }


    @Override
    public View getView(int i, View containView, ViewGroup viewGroup) {
        if (containView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            containView = inflater.inflate(R.layout.note_select_picture_item_layout, null);
        }else {
            Log.d("tui", "getView, containView != null, i = " + i);
        }

        ImageView imageView = (ImageView) containView.findViewById(R.id.picture_icon);
        int color = mContext.getResources().getColor(R.color.image_pre_background);
        imageView.setImageResource(color);

        PictureAttr pa = mPictureAttrs.get(i);
        imageView.setTag(pa.path);

        if (mMap.get(i)){
            containView.setBackgroundColor(getItemSelectedColor());
        }else{
            containView.setBackgroundColor(0x00ffffff);
        }

        return containView;
    }

    private int getItemSelectedColor(){
        Resources rs = mContext.getResources();
        return rs.getColor(R.color.grid_item_selected_color);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {

        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
            showImage(mFirstVisibleItem, mVisibleItemCount);
        }else{
            cancelTask();
        }
    }

    /**
     * 滚动时会调用此方法
     * 第一次进入界面时，会调用此方法，完成界面数据的初始化
     */
    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;

        if(isFirstEnter && visibleItemCount > 0){
            showImage(mFirstVisibleItem, mVisibleItemCount);
            isFirstEnter = false;
        }
    }

    public void resetFirstEnter() {
        isFirstEnter = true;
    }

    /**
     *
     * @param firstVisibleItem
     * @param visibleItemCount
     */
    private void showImage(int firstVisibleItem,int visibleItemCount)
    {
        for(int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++){
            PictureAttr pa = mPictureAttrs.get(i);
            Bitmap bitmap = mImageDownloader.downLoadImage(pa.path, this, 3, true, pa.id);
            if (bitmap != null){
                onImageShow(pa.path, bitmap);
            }
        }
    }

    /**
     *取消任务
     */
    public void cancelTask(){
        mImageDownloader.cancelTask();
    }

    public void onDestroy(){
        mImageDownloader.OnDestroy();
        clearAll();
        cancelTask();
    }
    /**
     *
     * @param path
     * @param bitmap
     */
    @Override
    public void onImageShow(String path, Bitmap bitmap) {
        ImageView imageView = (ImageView)mGrid.findViewWithTag(path);

        if(imageView != null && bitmap!=null){
            imageView.setImageBitmap(bitmap);//下载后直接设置到view对象上
        }
    }

    class PictureAttr{
        String path;
        int id;
    }
}
