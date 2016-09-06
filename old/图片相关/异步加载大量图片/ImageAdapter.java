package cn.edu.chd.myimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import cn.edu.chd.myimageloader.ImageDownloader.OnImageDownloadListener;
 
public class ImageAdapter extends BaseAdapter implements OnScrollListener
{
    private GridView gridView;
    private Context context;
    private String[] imageThumUrls;
    private ImageDownloader mImageDownloader;
    private boolean isFirstEnter = true;
    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    
    public ImageAdapter(Context context,String[] imageThumUrls,GridView gridView)
    {
        this.context = context;
        this.gridView = gridView;
        this.imageThumUrls = imageThumUrls;
        this.mImageDownloader = new ImageDownloader(context);
        gridView.setOnScrollListener(this);
    }
    @Override
    public int getCount()
    {
        return imageThumUrls.length;
    }

    @Override
    public Object getItem(int position)
    {
        return imageThumUrls[position];
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView mImageView;
        String imageUrl = imageThumUrls[position];
        if(convertView == null)
        {
            mImageView = new ImageView(context);
        }else
        {
            mImageView = (ImageView) convertView;
        }
        mImageView.setLayoutParams(new GridView.LayoutParams(90,90));
        mImageView.setTag(imageUrl);
        
        //只显示缓存图片，如果缓存中没有则设置一张默认的图片
        Bitmap bitmap = mImageDownloader.showCacheBitmap(imageUrl.replaceAll("[^\\w]",""));
        if(bitmap != null)
        {
            mImageView.setImageBitmap(bitmap);
        }else
        {
            mImageView.setImageResource(R.drawable.ic_launcher);
        }
        return mImageView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)//滑动停止时启动下载图片
        {
            showImage(mFirstVisibleItem, mVisibleItemCount);
        }else
        {
            cancellTask();
        }
    }
    
    /**
     * 滚动时执行此方法
     * 第一次进入会调用showImage显示图片
     * */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount)
    {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        
        if(isFirstEnter && visibleItemCount>0)
        {
            showImage(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;
        }
    }
    
    /**
     * 显示图片，先从缓存中找，如果没找到就开启线程下载
     * @param firstVisibleItem 第一个可见项的id
     * @param visibleItemCount 可见项的总数
     */
    private void showImage(int firstVisibleItem,int visibleItemCount)
    {
        for(int i = firstVisibleItem; i < firstVisibleItem+visibleItemCount;i++)
        {
            String mImageUrl = imageThumUrls[i];
            final ImageView mImageView = (ImageView) gridView.findViewWithTag(mImageUrl);
            mImageDownloader.downloadImage(mImageUrl, new OnImageDownloadListener()
            {
                @Override
                public void onImageDownload(String url, Bitmap bitmap)
                {
                    if(mImageView != null && bitmap!=null)
                    {
                        mImageView.setImageBitmap(bitmap);//下载后直接设置到view对象上
                    }
                }
            });
        }
    }
    
    /**
     * 取消下载任务
     */
    public void cancellTask()
    {
        mImageDownloader.cancellTask();
    }
    
}