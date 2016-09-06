package ghj1976.AndroidTest;
 
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
 
import android.widget.ImageView;
 
public class MainActivity extends Activity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);
                loadImage4("http://www.baidu.com/img/baidu_logo.gif", R.id.imageView1);
                loadImage4("http://www.chinatelecom.com.cn/images/logo_new.gif",
                                R.id.imageView2);
                loadImage4("http://cache.soso.com/30d/img/web/logo.gif",
                                R.id.imageView3);
                loadImage4("http://csdnimg.cn/www/images/csdnindex_logo.gif",
                                R.id.imageView4);
                loadImage4("http://images.cnblogs.com/logo_small.gif",
                                R.id.imageView5);
        }
 
        private AsyncImageLoader3 asyncImageLoader3 = new AsyncImageLoader3();
 
        // �����̳߳أ��������ڴ滺�湦��,�����ⲿ���÷�װ�˽ӿڣ��򻯵��ù���
        private void loadImage4(final String url, final int id) {
                // ���������ͻ�ӻ�����ȡ��ͼ��ImageCallback�ӿ��з���Ҳ���ᱻִ��
                Drawable cacheImage = asyncImageLoader3.loadDrawable(url,
                                new AsyncImageLoader3.ImageCallback() {
                                        // ��μ�ʵ�֣������һ�μ���urlʱ���淽����ִ��
                                        public void imageLoaded(Drawable imageDrawable) {
                                                ((ImageView) findViewById(id))
                                                                .setImageDrawable(imageDrawable);
                                        }
                                });
                if (cacheImage != null) {
                        ((ImageView) findViewById(id)).setImageDrawable(cacheImage);
                }
        }
 
}