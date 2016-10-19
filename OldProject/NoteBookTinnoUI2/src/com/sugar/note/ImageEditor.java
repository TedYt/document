package com.sugar.note;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.ImageView;
//import android.widget.FrameLayout;

public class ImageEditor extends RelativeLayout {

    private String mPhoto;
    private ImageView mDelete;

    public ImageEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mPhoto = "";
    }

    public ImageEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mPhoto = "";
    }

    public ImageEditor(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mPhoto = "";
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setDeleteView(ImageView delete) {
        mDelete = delete;
    }

    public ImageView getDeleteView() {
        return mDelete;
    }
}
