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
        
        //ֻ��ʾ����ͼƬ�����������û��������һ��Ĭ�ϵ�ͼƬ
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
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)//����ֹͣʱ��������ͼƬ
        {
            showImage(mFirstVisibleItem, mVisibleItemCount);
        }else
        {
            cancellTask();
        }
    }
    
    /**
     * ����ʱִ�д˷���
     * ��һ�ν�������showImage��ʾͼƬ
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
     * ��ʾͼƬ���ȴӻ������ң����û�ҵ��Ϳ����߳�����
     * @param firstVisibleItem ��һ���ɼ����id
     * @param visibleItemCount �ɼ��������
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
                        mImageView.setImageBitmap(bitmap);//���غ�ֱ�����õ�view������
                    }
                }
            });
        }
    }
    
    /**
     * ȡ����������
     */
    public void cancellTask()
    {
        mImageDownloader.cancellTask();
    }
    
}