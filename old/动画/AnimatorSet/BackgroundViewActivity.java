package cn.yw.lib.animation;

import android.app.Activity;
import android.os.Bundle;

public class BackgroundViewActivity extends Activity{
    private BackgroundView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new BackgroundView(this);
        setContentView(view);
    }

}