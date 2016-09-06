package cn.edu.chd.myimageloader;

import cn.edu.chd.datasource.Images;
import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

public class MainActivity extends Activity
{
    private GridView gridView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(new ImageAdapter(this, Images.imageThumbUrls, gridView));
    }
}