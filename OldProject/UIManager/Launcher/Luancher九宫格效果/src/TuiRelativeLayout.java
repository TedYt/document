package com.tinno.launcher2;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

public class TuiRelativeLayout extends RelativeLayout{
	
	private static final String TAG = "TuiRelativeLayout";
	private int mCount = 1;

	public TuiRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public TuiRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public TuiRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onLayout " + (mCount ++));
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onMeasure" + (mCount ++));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	

}
