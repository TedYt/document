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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.text.TextPaint;


import com.mediatek.common.featureoption.FeatureOption;

public abstract class AbsDateView extends View{
    
    protected TextPaint   mPaint;
    protected TextPaint   mSelectPaint;
    protected Paint   mHightPaint;
///////////// 16sp
    protected int  TEXT_SIZE = 26;
////////////  22sp
    protected int SELECT_TEXT_SIZE = 34;

    //private  int ACTIVE_TEXT_COLOR = 0x80969696;//com.android.internal.R.color.tinno_numberpicker_select;//Color.BLACK;
    //private  int HIGHLIGHT_TEXT_COLOR = 0xffd62d78;//com.android.internal.R.color.tinno_numberpicker_highlight;//Color.BLUE;
    //private  int SELECT_TEXT_COLOR = 0xffd62d78;//com.android.internal.R.color.tinno_numberpicker_highlight;//Color.BLACK;
    private  int ACTIVE_TEXT_COLOR = 0x80aeaeae;
    private  int HIGHLIGHT_TEXT_COLOR = 0xff0bada5;
    private  int SELECT_TEXT_COLOR = 0xff0bada5;

    /**
     * Indicates that we are not in the middle of a touch gesture
     */
    static final int TOUCH_MODE_REST = -1;

    /**
     * Indicates we just received the touch event and we are waiting to see if the it is a tap or a
     * scroll gesture.
     */
    static final int TOUCH_MODE_DOWN = 0;

    /**
     * Indicates the touch has been recognized as a tap and we are now waiting to see if the touch
     * is a longpress
     */
    static final int TOUCH_MODE_TAP = 1;

    /**
     * Indicates we have waited for everything we can wait for, but the user's finger is still down
     */
    static final int TOUCH_MODE_DONE_WAITING = 2;

    /**
     * Indicates the touch gesture is a scroll
     */
    static final int TOUCH_MODE_SCROLL = 3;
    
    /**
     * Indicates the view is in the process of being flung
     */
    static final int TOUCH_MODE_FLING = 4;
    /**
     * Indicates the duration of the last fling
     */
    protected static final int DEFAULT_DURATION = 500; //500
    /**
     * The last scroll state reported to clients through {@link OnScrollListener}.
     */
    private int mLastScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    /**
     * Optional callback to notify client when scroll position has changed
     */
    private OnScrollListener mOnScrollListener;
    /**
     * Optional callback to notify client when click one item happend
     */
    private OnItemClickListener mClickListener;

    protected int mMinimumVelocity;
    protected int mMaximumVelocity;
    private int mTouchSlop;

    protected int mPaddingBottom  =0;
    protected int mPaddingTop     =0;
    protected int mPaddingLeft    =0;
    protected int mPaddingRight   =0;
   
    protected boolean down = false;
    
    final int VISIBLE_COUNT = 3;

    //protected int ITEM_HEIGHT = 48;
    protected int ITEM_HEIGHT = 59;

    protected final int FIRST_INDEX   = 0;

    protected static final int INVALID_POINTER = -1;
    protected static final int INVALID_INDEX   = -1;

    /*** which only stand for the index for the string;***/
    public int mFirstItem     = FIRST_INDEX; 
    /*** which only stand for the length of the string;***/
    protected int mEnd           = FIRST_INDEX;
    /*** which only stand for the current value of the string but not the index;**/
    protected int mCurrent      = INVALID_INDEX;
    protected String[] mString  = null;

    protected int motionPosition = 0;
  
    protected int mFirstTop = 0;
    /**
     * Determines speed during touch scrolling
     */
    protected VelocityTracker mVelocityTracker;
    /**
    *Handles one frame of a fling
    **/
    protected FlingRunnable mFlingRunnable;
    protected boolean isFling = false;
    /**
     * One of TOUCH_MODE_REST, TOUCH_MODE_DOWN, TOUCH_MODE_TAP, TOUCH_MODE_SCROLL, or
     * TOUCH_MODE_DONE_WAITING
     */
    protected int mTouchMode = TOUCH_MODE_REST;
    
    protected int mScrollY = 0;
    protected int mMotionY = 0;
    
    private int mWidth = 0;
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final String TAG= "AbsDateView";
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    protected int mActivePointerId = INVALID_POINTER;
   
    public AbsDateView(Context context) {
        this(context, null);
    }
    
    public AbsDateView(Context context,AttributeSet attrs){
        this(context, attrs, 0);
    }
   
    public AbsDateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context) {
        setTextAndItemHeight();

        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
                | Paint.FAKE_BOLD_TEXT_FLAG | Paint.DITHER_FLAG);
        //valuePaint.density = getResources().getDisplayMetrics().density;
        mPaint.setTextSize(TEXT_SIZE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setAlpha(80);
        mPaint.setColor(ACTIVE_TEXT_COLOR);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mSelectPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
                | Paint.FAKE_BOLD_TEXT_FLAG | Paint.DITHER_FLAG);
        //valuePaint.density = getResources().getDisplayMetrics().density;
        mSelectPaint.setTextSize(SELECT_TEXT_SIZE);
        mSelectPaint.setAntiAlias(true);
        mSelectPaint.setDither(true);
        mSelectPaint.setAlpha(140);
        mSelectPaint.setColor(SELECT_TEXT_COLOR);
        mSelectPaint.setStyle(Paint.Style.FILL);
        mSelectPaint.setTextAlign(Paint.Align.CENTER);


        mHightPaint = new Paint(Paint.DITHER_FLAG);
        mHightPaint.setAntiAlias(true);
        mHightPaint.setDither(true);
        mHightPaint.setAlpha(192);
        mHightPaint.setColor(HIGHLIGHT_TEXT_COLOR);
        mHightPaint.setStyle(Paint.Style.FILL);
        mHightPaint.setTextSize(TEXT_SIZE);
        mHightPaint.setTextAlign(Paint.Align.CENTER);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    public void initDateTime(int start, int end, String[] displayString){
        //mFirstItem  = 0;
        if (null != displayString){
            mString = displayString;
            mEnd    = mString.length-1;
        }else{
            mString = new String[end-start+1];
            int number = start;
            for (int index=0; index < (end-start+1); index++){
                mString[index] = formatNumber(number);
                number++;
            }
            mEnd = mString.length-1;
        }
    }

    /**We used this function to format the single number**/
    private String formatNumber(int value){
        final StringBuilder mBuilder = new StringBuilder();
        final java.util.Formatter mFmt = new java.util.Formatter(mBuilder);
        final Object[] mArgs = new Object[1];
        mArgs[0] = value;
        mBuilder.delete(0, mBuilder.length());
        mFmt.format("%02d", mArgs);
        return mFmt.toString();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
    }
   
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /*float ppi = getContext().getResources().getDisplayMetrics().density; 

        int height = 0;
        if (isPortraitMode()){
            height = resolveSize((int)(168*ppi), heightMeasureSpec);
            setMeasuredDimension(resolveSize(mWidth,widthMeasureSpec), (int)(168*ppi));
            }else{
            height = resolveSize((int)(144*ppi), heightMeasureSpec);
            setMeasuredDimension(resolveSize(mWidth,widthMeasureSpec), (int)(144*ppi));
        }*/
    }
    
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
       
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                // Remember where the motion event started
                motionPosition = getMotionIndex(y);
                mMotionY = y;
                if (TOUCH_MODE_FLING != mTouchMode){
                    mActivePointerId = ev.getPointerId(0);
                    mTouchMode = TOUCH_MODE_DOWN;
                }else{
                    // Stopped a fling. It is a scroll.
                    if (TOUCH_MODE_FLING == mTouchMode){
                        mTouchMode = TOUCH_MODE_SCROLL;//here we only want a simple click
                        reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    }
                }
                break;
            }
            
            case MotionEvent.ACTION_MOVE:{
                    // Scroll to follow the motion event
                    //final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    //final int y = (int) ev.getY(pointerIndex);
                    final int y = (int) ev.getY();
                    final int deltaY = y - mMotionY;
                    //Log.i(TAG,"scroll, y="+y+",deltaY="+deltaY+",mMotionY="+mMotionY);
                    switch (mTouchMode) {
                        case TOUCH_MODE_DOWN:
                            if (startScrollIfNeeded(deltaY)){
                                Log.i(TAG,"we think this time can scroll");
                                mMotionY = y;
                            }
                            break;

                        case TOUCH_MODE_SCROLL:
                            mMotionY = y;
                            scrollView(0,deltaY);
                            break;

                        default:
                            break;
                    }
                break;
            }
               
            case MotionEvent.ACTION_UP: {
                final int y = (int) ev.getY();  
                int deltaY = mMotionY -y;
                //Log.i(TAG,"up, y="+y+",deltaY="+deltaY+",mMotionY="+mMotionY);
                switch (mTouchMode) {
                        case TOUCH_MODE_DOWN:{
                            //Then we think it's a simple click only
                            Log.i(TAG,"we think it's a click");
                            int index = getMotionIndex(y);
                            setCenterViewByPos(index);
                            performItemClick(this, index);
                            mTouchMode = TOUCH_MODE_REST;
                            break;
                        }

                        case TOUCH_MODE_SCROLL:{
                            //Then we think it's a scroll or fling
                            Log.i(TAG,"we think it's a scroll");
                            flingORScrollWhenUp();
                            break;
                        }

                        default:
                            break;
                    }
                    mActivePointerId = INVALID_POINTER;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    
                    mScrollY = 0;
                break;
            }
            
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
               
            case MotionEvent.ACTION_POINTER_UP:
                //onSecondaryPointerUp(ev);
                break;
        }
        return true;
    }

    private void setTextAndItemHeight(){
        float ppi = getContext().getResources().getDisplayMetrics().density;      
        ITEM_HEIGHT = (int)(ITEM_HEIGHT*ppi);
        TEXT_SIZE = (int)(TEXT_SIZE*ppi);
        SELECT_TEXT_SIZE = (int)(SELECT_TEXT_SIZE*ppi);
        //Log.i("doudou","height="+ITEM_HEIGHT+", size="+TEXT_SIZE);
    }

    private boolean startScrollIfNeeded(int deltaY) {
        // Check if we have moved far enough that it looks more like a
        // scroll than a tap
        final int distance = Math.abs(deltaY);
        if (distance > mTouchSlop) {
            mTouchMode = TOUCH_MODE_SCROLL;
            scrollView(0,deltaY);
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            return true;
        }
        return false;
    }

    /**
    ** Y: the value of the click value in Y axies
    ** This function used by click function
    **/
    protected abstract int getMotionIndex(int Y);

    /**
    ** index: the index of the array
    ** This function used by click function
    **/
    abstract void setCenterViewByPos(int index);

    /**
    ** int: the index of the array
    ** This function used by click function
    **/
    abstract int getCenterViewPos();

    /**
    ** value: mean the actual value
    ** This function used by setCurrent function
    **/
    //abstract void setCenterViewByVaule(Object value);

    /**
    ** Object: mean the actual value
    ** This function used by click function
    **/
    public Object getCenterViewValue(){
        // only used when we notity the datepicker or timepicker to chanage the time
        int index = getCenterViewPos();
        return mString[index];
    }


    protected abstract void flingORScrollWhenUp();

    protected int getMotionDelta(int Length){
        if (Math.abs(Length) > ITEM_HEIGHT/2){
            return ITEM_HEIGHT+Length;
        }else{
            return Length;
        }
    }

    protected void setFirstChildTop(int delta){
         Log.i(TAG,"mFirstTop="+Integer.toString(delta));
         mFirstTop = delta;
    }

    /**
    ***X: mean the X distance when scroll or fling
    ***Y: mean the Y distance when scroll or fling
    ***boolean: false:stand for there are view left, true mean we have draw the last one
    **/
    abstract boolean scrollView(int X, int Y);

    /**
    ***canvas: the bitmap which we want to draw
    **/
    protected abstract void onDraw(Canvas canvas);
    
    

    protected int getTopOfFirstItem(){
        return mFirstTop;
    }


    protected int getButtomOfFirstItem(){
        int buttom = ITEM_HEIGHT + mFirstTop; 
        return buttom;
   }

   protected abstract void setCenterItem(int index, boolean centerflag);
   abstract void dealwithFlingResult();
   abstract boolean getViewScrolling();

     /**
     * Responsible for fling behavior. Use {@link #start(int)} to
     * initiate a fling. Each frame of the fling is handled in {@link #run()}.
     * A FlingRunnable will keep re-posting itself until the fling is done.
     *
     */
    class FlingRunnable implements Runnable {
        /**
         * Tracks the decay of a fling scroll
         */
        private final MTKScroller mScroller;

        /**
         * Y value reported by mScroller on the previous fling
         */
        private int mLastFlingY;

        FlingRunnable() {
            mScroller = new MTKScroller(getContext());
        }

        void start(int initialVelocity) {
            int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            boolean down = initialVelocity<0?true:false;
            mLastFlingY = initialY;
            int offSet = down?getTopOfFirstItem():getButtomOfFirstItem();
          
            mScroller.setFlingInfo(down, ITEM_HEIGHT);
            mScroller.fling(0, initialY, 0, initialVelocity,
                0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, offSet);
            mTouchMode = TOUCH_MODE_FLING;
            post(this);
        }

        void startScroll(int distance, int duration) {
            int initialY = distance < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingY = initialY;
            //mLastFlingY = 0;
            mScroller.startScroll(0, initialY, 0, distance, duration);
            mTouchMode = TOUCH_MODE_FLING;
            post(this);
        }

        protected void endFling() {
            mTouchMode = TOUCH_MODE_REST;
            isFling = false;
            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            removeCallbacks(this);
        }

        public void run() {
            switch (mTouchMode) {
            default:
                return;
                
            case TOUCH_MODE_FLING: {
                boolean upFlag = false;
                final MTKScroller scroller = mScroller;
                boolean more = scroller.computeScrollOffset();
                final int y = scroller.getCurrY();
                // (e.g. finger moving down means list is moving towards the top)
                int delta = mLastFlingY - y;

                boolean bRet = scrollView(delta, delta);

                if (more && !bRet) {
                    mLastFlingY = y;
                    post(this);
                } else {
                    //we need to add this code to scroll correctly
                    
                    if (mLastScrollState == mOnScrollListener.SCROLL_STATE_LASTFLING){
                        //then we should not deal with it
                        endFling();
                    }else{
                        dealwithFlingResult();
                    }
                    //endFling();
                }
            }
            break;
            }
        }
    }
    
    /**
     * Fires an "on scroll state changed" event to the registered
     * {@link android.widget.AbsListView.OnScrollListener}, if any. The state change
     * is fired only if the specified state is different from the previously known state.
     *
     * @param newState The new scroll state.
     */
    void reportScrollStateChange(int newState) {
        //if (newState != mLastScrollState) {
            
            if (mOnScrollListener != null) {
               if (mLastScrollState != newState){
                  mOnScrollListener.onScrollStateChanged(this, newState);
                  mLastScrollState = newState;
               }
            }
        //}
    }

    /**
     * Smoothly scroll by distance pixels over duration milliseconds.
     * @param distance Distance to scroll in pixels.
     * @param duration Duration of the scroll animation in milliseconds.
     */
    protected void smoothScrollBy(int distance, int duration) {
        if (mFlingRunnable == null) {
            mFlingRunnable = new FlingRunnable();
        } else if (isFling){
            mFlingRunnable.endFling();
        }
        mFlingRunnable.startScroll(distance, duration);
    }

    /**
     * Set the listener that will receive notifications every time the list scrolls.
     *
     * @param l the scroll listener
     */
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

   
    /**
     * Interface definition for a callback to be invoked when the list or grid
     * has been scrolled.
     */
    public interface OnScrollListener {

        /**
         * The view is not scrolling. Note navigating the list using the trackball counts as
         * being in the idle state since these transitions are not animated.
         */
        public static int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and their finger is still on the screen
         */
        public static int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and had performed a fling. The
         * animation is now coasting to a stop
         */
        public static int SCROLL_STATE_FLING = 2;

        /**
         * In order to fling the distance which less than one item
         */
        public static int SCROLL_STATE_LASTFLING = 3;
        /**
         * Callback method to be invoked while the list view or grid view is being scrolled. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. In particular, it will be called before any calls to
         * {@link Adapter#getView(int, View, ViewGroup)}.
         *
         * @param view The view whose scroll state is being reported
         *
         * @param scrollState The current scroll state. One of {@link #SCROLL_STATE_IDLE},
         * {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
         */
        public void onScrollStateChanged(AbsDateView view, int scrollState);

        /**
         * Callback method to be invoked when the list or grid has been scrolled. This will be
         * called after the scroll has completed
         * @param view The view whose scroll state is being reported
         * @param firstVisibleItem the index of the first visible cell (ignore if
         *        visibleItemCount == 0)
         * @param visibleItemCount the number of visible cells
         * @param totalItemCount the number of items in the list adaptor
         */
        public void onScroll(AbsDateView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount);
    }
    
    /**
    * Interface definintion for a callback to be invoked when an item has been clicked
    **/
    public interface OnItemClickListener{
        /**
        *Callback function
        **/
        void onItemClick(AbsDateView absDateView, int postion);
    }

    /**
    * Register a callback to be invoked when an item has been clicked
    **/
    public void setOnItemClickListener(OnItemClickListener listener){
        mClickListener = listener;
    }

    /***
    * Call the OnItemClickListener, if it is defined
    **/
    protected boolean performItemClick(AbsDateView absDateView, int position){
        if (mClickListener != null){
            mClickListener.onItemClick(absDateView, position);
            return true;
        }
        return false;
    }
}


