/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.sugar.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.VelocityTracker;

//import android.widget.AbsDateView;

import android.text.TextPaint;
import android.graphics.Rect;
import android.graphics.Paint;

public class WheelView extends AbsDateView{
	
	private String mMarkText;
	protected TextPaint   mMarkPaint;
	//private int mMarkTextSize = 24; // 8sp
	private int mMarkTextSize = 44;
    
    private static final String TAG= "WheelView";

    public WheelView(Context context) {
        this(context, null);
    }
    
    public WheelView(Context context,AttributeSet attrs){
        this(context, attrs, 0);
    }
   
    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mMarkPaint = new TextPaint(mSelectPaint);
        mMarkPaint.setTextSize(mMarkTextSize);
        mMarkPaint.setTextAlign(Paint.Align.LEFT);
    }

    /**
    ** This function used to find which item has been selected
    **/
    protected int getMotionIndex(int Y){
        int top = getTopOfFirstItem();
        for (int index=0; index<VISIBLE_COUNT; index++){
            if (mMotionY>top && mMotionY<=(top+ITEM_HEIGHT)){
                if (mFirstItem+index > mEnd){
                    return mFirstItem+index-mString.length;
                }else{
                    return mFirstItem+index;
                }
            }
            top += ITEM_HEIGHT;
        }
        return INVALID_INDEX;
    }

    public boolean getViewScrolling(){
        return isFling?true:false;
    }

    public void setCenterItem(int index, boolean changeflag){
        if (changeflag){
            mCurrent = index;
        }
        setCenterViewByPos(index);
    }
    /**
    ** index: mean the string array index
    ** This function used set the center value by arrry index 
    **/
    protected void setCenterViewByPos(int index){
        //Log.i(TAG,"setCenterViewByPos is"+Integer.toString(index));
        //int centerPos = getCenterViewPos();
        //if (index == centerPos){
        //    return ;
        //}
        int number = index;
        for (int i=0; i<(VISIBLE_COUNT-1)/2; i++){
            number--;
            if (number < FIRST_INDEX){
                number = mEnd;
            }else if (number > mEnd){
                number = FIRST_INDEX;
            }
        }
        mFirstItem = number;
        Log.i(TAG,"setCenterViewByPos mFirstItem is"+Integer.toString(mFirstItem));
        setFirstChildTop(0);
        invalidate();
    }

    /**
    ** This function used to get the center array index
    **/
    public int getCenterViewPos(){
        // only used when we notity the datepicker or timepicker to chanage the time
        int center = mFirstItem;
        for (int index=0; index<(VISIBLE_COUNT-1)/2; index++){
            center++;
            if (center > mEnd){
                center = FIRST_INDEX;
            }
        }
        return center;
    }
 

    @Override
    protected  void flingORScrollWhenUp(){
        final VelocityTracker velocityTracker = mVelocityTracker;
        //velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity/2);
        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity/10);
        Log.i(TAG, "mMaximumVelocity is"+Integer.toString(mMaximumVelocity));
        final int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
        if (null == mFlingRunnable){
                mFlingRunnable = new FlingRunnable();   
        }
        if (Math.abs(initialVelocity) > mMinimumVelocity) {// then we goto Fling
            isFling = true;
            Log.i(TAG, "initialVelocity is"+Integer.toString(initialVelocity));
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
            mFlingRunnable.start(-initialVelocity);
        }else{
            //we need to add the revisze code in here, after scroll to the right postion, 
            //then we can send the idle message;
            int firstTop = getTopOfFirstItem();
            if (0 != firstTop){
                int delta = getMotionDelta(firstTop);
                //scrollView(delta, delta);
                smoothScrollBy(delta, DEFAULT_DURATION);
                reportScrollStateChange(OnScrollListener.SCROLL_STATE_LASTFLING);
            }else{
                mFlingRunnable.endFling();
            } 
        }
    }

    boolean scrollView(int X, int Y){
        //Log.i(TAG,"we enter scrollView, Y="+Integer.toString(Y));
        down = Y<0?false:true; 
        mScrollY += Y;
        //Log.i("stone","Y ="+Integer.toString(Y)
        //        +",mScrollY="+Integer.toString(mScrollY));
        //down mean the Y < 0;
        int count = 0;
        int delta = 0;
        int Top = getTopOfFirstItem();
        int below = getButtomOfFirstItem();
        int firstSpaceAbove = mPaddingTop- Top;
        int firstSpaceBelow = ITEM_HEIGHT - Math.abs(Top);
	Log.e(TAG, "andy7755 WheelView scrollView Top11 = "+Top+"  below = "+below+"  down = "+down+
		"  mPaddingTop = "+mPaddingTop+"  X = "+X+"  Y = "+Y);

        if (down){
            final int above = mPaddingTop - Top;
            count = Y/ITEM_HEIGHT;
	    Log.e(TAG, "andy7755 WheelView scrollView down Top22 = "+Top+"  above = "+above+"  count = "+count);
            if (0 != count){//we should filter the date which will be less than one item height;
                Y = Y%ITEM_HEIGHT;
                //Log.i(TAG, "up after deal with, Y="+Y);
                for (int index=count; index>0; index--){
                    if (mFirstItem == FIRST_INDEX){
                        mFirstItem = mEnd;
                    }else{
                        mFirstItem--;
                    }
                }
            }
            if (above >= Y){//only need to refresh the item
                Top += Y;
                if (Top == ITEM_HEIGHT){
                    //Log.i(TAG4, "above >= Y, Top == ITEM_HEIGHT");
                    if (mFirstItem == FIRST_INDEX){
                        mFirstItem = mEnd;
                    }else{
                        mFirstItem--;
                    }
                    Top = 0;
                }
                setFirstChildTop(Top);
                //Log.i(TAG, "down Y="+Y+", Top="+Top);
            }else{//need to modify the first item
                if (mFirstItem == FIRST_INDEX){
                    mFirstItem = mEnd;
                }else{
                    mFirstItem--;
                }
                //delta = -(Y + Top);
                delta = Y + Top - ITEM_HEIGHT;
                setFirstChildTop(delta);
                //Log.i(TAG, "down Y="+Y+", delta"+delta+", Top="+Top);
            }
            
        }else{//up
            //Log.i(TAG,"below ="+Integer.toString(below));
            count = Math.abs(Y/ITEM_HEIGHT);
	    Log.e(TAG, "andy7755 WheelView scrollView up Top33 = "+Top+"  below = "+below+"  count = "+count);
            if (0 != count){//This is only used for the big rate when fling
                Y = Y%ITEM_HEIGHT;
                for (int index=count; index>0; index--){
                    if (mFirstItem == mEnd){
                        mFirstItem = FIRST_INDEX;
                    }else{
                        mFirstItem++;
                    }
                }
            }
            Log.i(TAG,"up, after deal with Y="+Y);
            if (below >= Math.abs(Y)){// only need to refresh the item
                Top += Y;
                if (Top == -ITEM_HEIGHT){
                    if (mFirstItem == mEnd){
                        mFirstItem = FIRST_INDEX;
                    }else{
                        mFirstItem++;
                    }
                    Top = 0;
                }
                Log.i(TAG, "up Y="+Y+", Top"+Top);
                setFirstChildTop(Top);
            }else{//Here we should modify the mFirstItem date
                if (mFirstItem == mEnd){
                    mFirstItem = FIRST_INDEX;
                }else{
                    mFirstItem++;
                }
                delta = Y + below;
                Log.i(TAG, " Y="+Y+", below"+below);
                setFirstChildTop(delta);
            }
        }
        invalidate();
        return false;
    }

    private static int getStringWidth(String str, Paint paint) {
        int iRet = 0;
        float width = 0f;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                width += widths[j];
            }
            iRet += (int) Math.ceil(width);
        }
        
        return iRet;
    }
        
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas)
        Bitmap bufferBmp = null;
        Canvas bmpCanvas = null;
        if (!isFling){
            bufferBmp = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                       Bitmap.Config.ARGB_8888);
            bmpCanvas = new Canvas(bufferBmp);
        }

        Canvas drawcanvas = isFling?canvas:bmpCanvas;
 
        if (null != bufferBmp){
            canvas.drawBitmap(bufferBmp, 0, 0, mPaint);
            bufferBmp.recycle();
            bufferBmp = null;
        }
        int halfTextHeight = TEXT_SIZE/2-5;
        int Item = mFirstItem;
        
        int Top = getTopOfFirstItem();
        int pos = Top+ITEM_HEIGHT;
        int y   = Top + (ITEM_HEIGHT/2+halfTextHeight);
	Log.e(TAG, "andy7755 WheelView onDraw Top = "+Top+"  mFirstItem = "+mFirstItem+"  halfTextHeight = "+halfTextHeight);

        int x = getMeasuredWidth()/2;
        for (int i=0; i<VISIBLE_COUNT+1; i++){
	    Log.e(TAG, "andy7755 WheelView onDraw i = "+i+"  x = "+x+"  y = "+y+"  Item = "+Item + "  mString[Item] = "+mString[Item]);
            try {
		   if(i==1)
		   {
               	       canvas.drawText(mString[Item], x, y, mSelectPaint);
               	       Rect rect = new Rect();
               	       mSelectPaint.getTextBounds("1", 0, 1, rect);
               	       int sw = getStringWidth(mString[Item], mSelectPaint);
		       //canvas.drawText(mMarkText, x + sw/2, y - rect.height() + 5, mMarkPaint);
		   }
		   else
		   {
                	canvas.drawText(mString[Item], x, y, mPaint);
		   }
                   //canvas.drawText(mString[Item], x, y, mCurrent==Item?mHightPaint:mPaint);
            }catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }
            Log.i(TAG,"pos="+pos+", ItemHeight="+ITEM_HEIGHT);
            y = pos+(ITEM_HEIGHT/2+halfTextHeight);
            pos += ITEM_HEIGHT;
            //here because we draw first and modify the variable second
            if (Item == mEnd){
                Item = FIRST_INDEX;
            }else{
                Item++;
            }
        }
/*
        if (null != bufferBmp){
            canvas.drawBitmap(bufferBmp, 0, 0, mPaint);
            bufferBmp.recycle();
            bufferBmp = null;
        }*/
    }

    @Override
    void dealwithFlingResult() {
        // TODO Auto-generated method stub
        /*int firstTop = getTopOfFirstItem();
        Log.i("stone","firstTop is "+Integer.toString(firstTop)
              +", string="+mString[mFirstItem]);
        if (0 != firstTop){
            int delta = getMotionDelta(firstTop);
            //smoothScrollBy(delta, DEFAULT_DURATION);
        }*/
       mFlingRunnable.endFling();
        //reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
    }
    
    public void setText(String text){
    	mMarkText = text;
    	invalidate();
    }
}
