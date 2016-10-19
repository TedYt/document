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

public class ElasticView extends AbsDateView{
    private static final String TAG = "elasticview";
    public ElasticView(Context context) {
        this(context, null);
    }
    
    public ElasticView(Context context,AttributeSet attrs){
        this(context, attrs, 0);
    }
   
    public ElasticView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
    ** This function used to find which item has been selected
    **/
    protected int getMotionIndex(int Y){
        int top = getTopOfFirstItem();
        for (int index=0; index<=VISIBLE_COUNT; index++){
            if (mMotionY>top && mMotionY<=(top+ITEM_HEIGHT)){
                return mFirstItem+index;
            }
            top += ITEM_HEIGHT;
        }
        return INVALID_INDEX;
    }

    /**
    ** This function used to get the center array index
    **/
    public int getCenterViewPos(){
        // only used when we notity the datepicker or timepicker to chanage the time
        int center = mFirstItem;
        for (int index=0; index<(VISIBLE_COUNT-1)/2; index++){
            center++;
        }
        return center;
    }

    public void setCenterItem(int index, boolean changeflag){
        mCurrent = index;
        setCenterViewByPos(index);
    }

    /**
    ** index: mean the string array index
    ** This function used set the center value by arrry index 
    **/
    protected void setCenterViewByPos(int index){
        //This function only used for click function
        if (INVALID_INDEX == mCurrent){
            mCurrent = index;
        }
        if (index < FIRST_INDEX){
            return ;
        }else if (index > mEnd){
            return ;
        }else{
            mFirstItem = index - (VISIBLE_COUNT-1)/2;
        }
        setFirstChildTop(0);
        invalidate();
    }

    @Override
    void dealwithFlingResult() {
        // TODO Auto-generated method stub
        int index = getCenterViewPos();
        if (index < FIRST_INDEX){
            setCenterViewByPos(FIRST_INDEX);
        }else if (index > mEnd){
            setCenterViewByPos(mEnd);
        }else{
            int firstTop = getTopOfFirstItem();
            if (0 != firstTop){// then it can run down
               //scrollView(-(ITEM_HEIGHT+firstTop), -(ITEM_HEIGHT+firstTop));
               //invalidate();
                setCenterViewByPos(mFirstItem+(VISIBLE_COUNT-1)/2);
                //smoothScrollBy(-(ITEM_HEIGHT+firstTop), DEFAULT_DURATION/2);
            }
        }
        reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
    }
   
    @Override
    protected void flingORScrollWhenUp(){
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        final int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);

        if (Math.abs(initialVelocity) > mMinimumVelocity) {
            if (null == mFlingRunnable){
                mFlingRunnable = new FlingRunnable();   
            }
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
            mFlingRunnable.start(-initialVelocity);
        }else{
            Log.i(TAG,"elastic, mFirstItem="+Integer.toString(mFirstItem));
            if (mFirstItem < FIRST_INDEX - (VISIBLE_COUNT-1)/2){
                setCenterViewByPos(FIRST_INDEX);
            }else if (mFirstItem + (VISIBLE_COUNT-1)/2 >= mEnd){
                setCenterViewByPos(mEnd);
            }else{
                int firstTop = getTopOfFirstItem();
                Log.i(TAG,"the firstTop="+Integer.toString(firstTop));
                if (0 != firstTop){
                    int delta = getMotionDelta(firstTop);
                    /*Log.i(TAG,"elastic, firstTop="+Integer.toString(firstTop)
                           +", delta="+Integer.toString(delta)
                           +", mFirstItem="+Integer.toString(mFirstItem)
                           +", mEnd="+Integer.toString(mEnd));*/
                    //scrollView(delta, delta);
                    smoothScrollBy(delta, DEFAULT_DURATION);
                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_LASTFLING);
                    return ;
                }
            }
            mTouchMode = TOUCH_MODE_REST;
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        } 
    }

     boolean scrollView(int X, int Y){
        down = Y<0?false:true;
        //down mean the Y < 0;
        int temp = Y;
        if ((FIRST_INDEX == mFirstItem && !down) 
            ||(mEnd - VISIBLE_COUNT == mFirstItem && down)){
            //Log.i(TAG,"we can scroll as usual");
            Y = Y/3;
        }
        int count = 0;
        int Top = getTopOfFirstItem();
        int delta = 0;
        int firstSpaceAbove = mPaddingTop- Top;
        int firstSpaceBelow = ITEM_HEIGHT - Math.abs(Top);

        if (down){
            count = Y/ITEM_HEIGHT;//This is only used for the big rate when fling
            if (0 != count){
                Y = Y%ITEM_HEIGHT;
                for (int index=count; index>0; index--){
                    mFirstItem--;
                }
            }
            final int above = mPaddingTop - Top;
            if (above >= Y){//only need to refresh the item
                Top += Y;
                if (Top == ITEM_HEIGHT){
                    mFirstItem--;
                    Top = 0;
                }
                setFirstChildTop(Top);
            }else{//need to modify the first item
                mFirstItem--;
                delta = Top + Y - ITEM_HEIGHT;
                setFirstChildTop(delta);
            }
        }else{//up
            count = Math.abs(Y/ITEM_HEIGHT);
            if (0 != count){//This is only used for the big rate when fling
                    Y = Y%ITEM_HEIGHT;
                    for (int index=count; index>0; index--){
                    mFirstItem++;
                    }
                delta = Y;
            }
            final int below = getButtomOfFirstItem();
            if (below >= Math.abs(Y)){// only need to refresh the item
                Top += Y;
                if (Top == -ITEM_HEIGHT){
                    mFirstItem++;
                    Top = 0;
                }
                setFirstChildTop(Top);
            }else{//Here we should modify the mFirstItem date
                    mFirstItem++;
                    delta = Y+below;
                    setFirstChildTop(delta);
            }
        }

        invalidate();
        if (mFirstItem <= FIRST_INDEX - VISIBLE_COUNT
            || mFirstItem > mString.length-1){
            return true;
        }else{
            return false;
        }
    }
    
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);       
        Bitmap bufferBmp = null;
        Canvas bmpCanvas = null;
        if (!isFling){
            bufferBmp = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                       Bitmap.Config.ARGB_8888);
            bmpCanvas = new Canvas(bufferBmp);
        }
        Canvas drawcanvas = isFling?canvas:bmpCanvas;
        Log.i(TAG, "we enter ondraw, mFirstItem="+Integer.toString(mFirstItem));
        
        int Item = mFirstItem;
        if (Item >= mString.length){
            Log.i(TAG, "we need not draw this time,mFirstItem="+Integer.toString(mFirstItem));
            if (null != bufferBmp){
                bufferBmp.recycle();
                bufferBmp = null;
                return ;
            }
        }
		
        if (null != bufferBmp){
            canvas.drawBitmap(bufferBmp, 0, 0, mPaint);
            bufferBmp.recycle();
            bufferBmp = null;
        }
		
        int Top  = getTopOfFirstItem();

        int halfTextHeight = TEXT_SIZE/2-3;
        int pos  = Top;
        int Y    = Top + (ITEM_HEIGHT/2+halfTextHeight);
        int X    = getMeasuredWidth()/2;
        
        for (int i=0; i<VISIBLE_COUNT+1; i++){
            /// in this situation, we should draw nonthing, 
            //util we find it return back to the actual value
            if (Item < FIRST_INDEX){
                Item++;
                pos += ITEM_HEIGHT;
                Y = pos+(ITEM_HEIGHT/2+halfTextHeight);
                continue;
            }
            try{
		   if(i == 1)
		   {
                	canvas.drawText(mString[Item], X, Y, mSelectPaint);
		   }
		   else
		   {
	               canvas.drawText(mString[Item], X, Y, mPaint);
		   }
                //drawcanvas.drawText(mString[Item], X, Y, mCurrent==Item?mHightPaint:mPaint);
            }catch (ArrayIndexOutOfBoundsException e){
                Log.w(TAG,"item = "+Integer.toString(Item));
                e.printStackTrace();
            }
            Item++;
            pos += ITEM_HEIGHT;
            Log.i(TAG, "we enter ondraw, pos="+Integer.toString(pos));
            Y = pos+(ITEM_HEIGHT/2+halfTextHeight);
            
            if (Item > mEnd){
                break;
            }
        }
/*
        if (null != bufferBmp){
            canvas.drawBitmap(bufferBmp, 0, 0, mPaint);
            bufferBmp.recycle();
            bufferBmp = null;
        }
*/
   }

    @Override
    public boolean getViewScrolling() {
        // TODO Auto-generated method stub
        return false;
    }
}
