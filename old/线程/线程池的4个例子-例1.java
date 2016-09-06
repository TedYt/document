package ghj1976.AndroidTest;
 
import java.io.IOException;
import java.net.URL;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
 
public class MainActivity extends Activity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);
                loadImage("http://www.baidu.com/img/baidu_logo.gif", R.id.imageView1);
                loadImage(<img id="\"aimg_W4qPt\"" onclick="\"zoom(this," this.src,="" 0,="" 0)\"="" class="\"zoom\"" file="\"http://www.chinatelecom.com.cn/images/logo_new.gif\"" onmouseover="\"img_onmouseoverfunc(this)\"" onload="\"thumbImg(this)\"" border="\"0\"" alt="\"\"" src="\"http://www.chinatelecom.com.cn/images/logo_new.gif\"" lazyloaded="true">",
                                R.id.imageView2);
                loadImage("http://cache.soso.com/30d/img/web/logo.gif, R.id.imageView3);
                loadImage("http://csdnimg.cn/www/images/csdnindex_logo.gif",
                                R.id.imageView4);
                loadImage("http://images.cnblogs.com/logo_small.gif",
                                R.id.imageView5);
        }
 
        private Handler handler = new Handler();
 
        private void loadImage(final String url, final int id) {
                handler.post(new Runnable() {
                        public void run() {
                                Drawable drawable = null;
                                try {
                                        drawable = Drawable.createFromStream(
                                                        new URL(url).openStream(), "image.gif");
                                } catch (IOException e) {
                                        Log.d("test", e.getMessage());
                                }
                                if (drawable == null) {
                                        Log.d("test", "null drawable");
                                } else {
                                        Log.d("test", "not null drawable");
                                }
                                // 为了测试缓存而模拟的网络延时 
                                SystemClock.sleep(2000); 
                                ((ImageView) MainActivity.this.findViewById(id))
                                                .setImageDrawable(drawable);
                        }
                });
        }
}