package com.sugar.note.picture;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.provider.MediaStore.Images;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sugar.note.AbsDateView;
import com.sugar.note.R;

import java.util.ArrayList;

/**
 * Created by user on 7/24/14.
 */
public class PictureFolderAdapter extends BaseAdapter implements ImageDownloader.OnImageShowListener,
        AbsListView.OnScrollListener {

    private Context mContext;
    private ListView mList;

    private ImageDownloader mImageDown;

    private int mFirstVisibleItem;
    private int mVisibleItemCount;

    private boolean isFirstEnter = true;

    private ArrayList<FolderAttrs> mFolders ;

    private String[] mProjection = {
            Images.ImageColumns._ID,
            Images.ImageColumns.DATA,
            Images.ImageColumns.DISPLAY_NAME,
            Images.ImageColumns.DATE_ADDED,
            Images.ImageColumns.DATE_MODIFIED,
            Images.ImageColumns.BUCKET_DISPLAY_NAME,
    };
    private final int PICTURE_ID = 0;
    private final int PICTURE_PATH = 1;
    private final int PICTURE_NAME = 2;
    private final int PICTURE_DATE_ADDED = 3;
    private final int PICTURE_DATE_MODIFIED = 4;
    private final int PICTURE_FOLDER_NAME = 5;


    //distinct 关键字是为了去掉重复记录
    private String[] mFolderPro = {
            "distinct " + Images.ImageColumns.BUCKET_DISPLAY_NAME,
    };
    private final int FOLDERPRO_COL_FOLDERNAME = 0;

    public PictureFolderAdapter(Context context, ListView listView) {
        mContext = context;
        mList = listView;
        mList.setOnScrollListener(this);
        mImageDown = new ImageDownloader(context);

        mFolders = new ArrayList<FolderAttrs>();

        init();
    }

    private void init() {
        Uri uri;
        String volumeName = "external";
        uri = MediaStore.Images.Media.getContentUri(volumeName);

        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(uri, mFolderPro, null, null, null);

        try {
            mFolders.clear();
            while (cursor.moveToNext()) {
                FolderAttrs fa = new FolderAttrs();
                fa.name = cursor.getString(FOLDERPRO_COL_FOLDERNAME);
                //Log.d("tui", "folder name = " + cursor.getString(0));

                Cursor c = cr.query(uri, mProjection,
                        mProjection[PICTURE_FOLDER_NAME] + " like " + "'" + fa.name + "'",
                        null,
                        mProjection[PICTURE_DATE_ADDED] + " desc");
                try {
                    fa.count = c.getCount();
                    c.moveToFirst();
                    String data = c.getString(PICTURE_PATH);
                    fa.firstPicture = data;
                    fa.path = getFolderPath(data);
                    //Log.d("tui", "count = " + fa.count + ", path = " + fa.path + ", first = " + fa.firstPicture);

                    mFolders.add(fa);
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void updateFolderAttrs(){
        init();
    }

    public void onDestroy(){
        cancelTask();
        mFolders.clear();
        mImageDown.OnDestroy();
    }

    private void cancelTask() {
        mImageDown.cancelTask();
    }

    private String getFolderPath(String data) {

        int index = data.lastIndexOf("/");
        String path = data.substring(0,index + 1);//加1是为了保留“/”，以便查询比较
        //Log.d("tui", "getFolderPath, folder path = " + path);

        return path;
    }

    @Override
    public int getCount() {
        return mFolders.size();
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
        return mFolders.get(index).path;
    }

    public String getItemFolderName(int index){
        return mFolders.get(index).name;
    }

    @Override
    public View getView(int i, View containView, ViewGroup viewGroup) {

        ViewHolder holder = null;
        if (containView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            containView = inflater.inflate(R.layout.picture_folder_list_item, null);
            holder.mImagePre = (ImageView)containView.findViewById(R.id.folder_preview);
            holder.mText = (TextView)containView.findViewById(R.id.folder_name);
            containView.setTag(holder);
        }else {
            holder = (ViewHolder)containView.getTag();
        }

        FolderAttrs fa = mFolders.get(i);
        holder.mText.setText(fa.name + "  ( " + fa.count +" ) ");
        holder.mImagePre.setTag(fa.firstPicture);
        if (fa.bitmap == null){
            holder.mImagePre.setImageResource(R.color.image_pre_background);
        }else {
            holder.mImagePre.setImageBitmap(fa.bitmap);
        }

        return containView;
    }

    private Bitmap getFolderPreView(String picturePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 100);

        return bitmap;
    }

    @Override
    public void onImageShow(String firstPicturePath, Bitmap bitmap) {
        ImageView imageView = (ImageView)mList.findViewWithTag(firstPicturePath);
        Log.d("tui", "onImageShow path = " + firstPicturePath);

        int index = getFolderAttrIndex(firstPicturePath);
        if (index != -1){
            //throw new ArrayIndexOutOfBoundsException("length = " + mFolders.size() +"index = -1");
            FolderAttrs fa = mFolders.get(index);
            fa.bitmap = bitmap;

            if (fa.bitmap == null) {
                Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.folder_error_picture);
                fa.bitmap = b;
            }

            if(imageView != null ){
                imageView.setImageBitmap(fa.bitmap);
            }
        }
    }

    private int getFolderAttrIndex(String firstPicturePath) {
        for (int i =0 ; i < mFolders.size(); i ++){
            FolderAttrs fa = mFolders.get(i);
            if (fa.firstPicture.equals(firstPicturePath)){
                return i;
            }
        }

        return -1;
    }


    private void showImage(int firstVisibleItem, int visibleItemCount) {
        for(int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++){
            FolderAttrs fa = mFolders.get(i);
            String imagePath = fa.firstPicture;
            Bitmap bitmap = mImageDown.downLoadImage(imagePath, this, 1, true);
            if (bitmap != null){
                onImageShow(imagePath, bitmap);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
            showImage(mFirstVisibleItem, mVisibleItemCount);
        }else{
            cancelTask();
        }
    }

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

    class ViewHolder {
        ImageView mImagePre;
        TextView mText;
    }

    class FolderAttrs {
        String path; // like " /storage/sdcard0/Pictures/ "
        String name; // like " Pictures "
        String firstPicture; //like " /storage/sdcard0/Pictures/abc.png "
        Bitmap bitmap;
        int count;
    }
}
