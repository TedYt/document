package com.sugar.note.handing;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.sugar.note.R;
import android.util.AttributeSet;
import java.util.HashMap;
import android.graphics.Color;
import android.widget.HorizontalScrollView;

/**
 * Created by user on 9/4/14.
 * 设置画笔粗细，颜色的界面
 */
public class PaintSetAreaWindow extends PopupWindow implements View.OnClickListener {

    public static final int BASE_BOLD = 5;

    private BaseCanvas mWrite;
    private static final HashMap<Integer, Integer> mColorsHashMap = new HashMap<Integer, Integer>();

    static {
        mColorsHashMap.put(R.id.color1, Color.WHITE);
        mColorsHashMap.put(R.id.color2, Color.BLACK);
        mColorsHashMap.put(R.id.color3, Color.RED);
        mColorsHashMap.put(R.id.color4, Color.GREEN);
        mColorsHashMap.put(R.id.color5, Color.BLUE);
        mColorsHashMap.put(R.id.color6, Color.YELLOW);
        mColorsHashMap.put(R.id.color7, Color.GRAY);
    }

    private int[] mBolds = new int[]{
            R.id.bold1,
            R.id.bold2,
            R.id.bold3,
            R.id.bold4,
            R.id.bold5,
            R.id.bold6,
    };

    public static final int PAINT_BOLD = 0;
    public static final int PAINT_COLOR = 1;
    private int mType = PAINT_BOLD;


    public PaintSetAreaWindow(Context context) {
        this(context, null);
    }


    public PaintSetAreaWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public PaintSetAreaWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    
    public void initWindow(Context c, BaseCanvas wc, WindowManager wm, int type) {

        mWrite = wc;
        mType = type;

        LayoutInflater inflater = LayoutInflater.from(c);
        View contentView;
        
        if (mType == PAINT_COLOR) {
            contentView = inflater.inflate(R.layout.paint_color_layout, null);
            for (int resID : mColorsHashMap.keySet()) {
                View v = contentView.findViewById(resID);
                v.setTag(mColorsHashMap.get(resID));
                v.setOnClickListener(this);
            }

            View colorScroll = contentView.findViewById(R.id.color_area_scroll);
            colorScroll.setHorizontalScrollBarEnabled(false);
        } else {
            contentView = inflater.inflate(R.layout.paint_bold_layout, null);
            for (int i = 0; i < mBolds.length; i++) {
                View v = contentView.findViewById(mBolds[i]);
                v.setTag((i + 1 ) * BASE_BOLD);
                v.setOnClickListener(this);
            }
        }

        
        
        setContentView(contentView);//设置popupwindow的布局

        DisplayMetrics metrics = new DisplayMetrics();
        wm .getDefaultDisplay().getMetrics(metrics);
        setWidth(metrics.widthPixels);//设置popupwindow的宽度

        int h = 0;
        if (mType == PAINT_BOLD) {
            h = c.getResources().getDimensionPixelSize(R.dimen.paint_bold_area_height);
        } else {
            h = c.getResources().getDimensionPixelSize(R.dimen.paint_color_area_height);
        }
        setHeight(h);//设置popupwindow的高度
        
    }


    @Override
    public void onClick(View view) {
        int tagValue = (Integer)view.getTag();
        Log.d("tui","PaintSetAreaWindow onClick tagValue = " + tagValue);

        if (mType == PAINT_COLOR) {
            mWrite.setPaintColor(tagValue);
        } else {
            mWrite.setPaintBoldValue(tagValue);
        }
    }


    public void close() {
        if (isShowing()) {
            dismiss();
        }
    }


    public void show(View anchor) {
        showAsDropDown(anchor, 0, 0);
    }


    public void toggle(View anchor) {
        if (isShowing()) {
            close();
        } else {
            show(anchor);
        }
    }
}
