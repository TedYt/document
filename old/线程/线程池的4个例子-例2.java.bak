package ghj1976.AndroidTest;
 
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
 
public class MainActivity extends Activity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);
                loadImage3("http://www.baidu.com/img/baidu_logo.gif", R.id.imageView1);
                loadImage3("http://www.chinatelecom.com.cn/images/logo_new.gif",
                                R.id.imageView2);
                loadImage3("http://cache.soso.com/30d/img/web/logo.gif",
                                R.id.imageView3);
                loadImage3("http://csdnimg.cn/www/images/csdnindex_logo.gif",
                                R.id.imageView4);
                loadImage3("http://images.cnblogs.com/logo_small.gif",
                                R.id.imageView5);
        }
 
        private Handler handler = new Handler();
 
        private ExecutorService executorService = Executors.newFixedThreadPool(5);
 
        // 引入线程池来管理多线程
        private void loadImage3(final String url, final int id) {
                executorService.submit(new Runnable() {
                        public void run() {
                                try {
                                        final Drawable drawable = Drawable.createFromStream(
                                                        new URL(url).openStream(), "image.png");
                                        // 模拟网络延时
                                        SystemClock.sleep(2000);
                                        handler.post(new Runnable() {
                                                public void run() {
                                                        ((ImageView) MainActivity.this.findViewById(id))
                                                                        .setImageDrawable(drawable);
                                                }
                                        });
                                } catch (Exception e) {
                                        throw new RuntimeException(e);
                                }
                        }
                });
        }
}