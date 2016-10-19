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

package com.android.contacts.common.list;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.common.R;

/**
 * The BladeView provide user to location a specific item in list by clicking
 * the category instead of scrolling the list.
 * 
 * In order to use this control, your adapter must implement the Indexer interface,
 * then if you want the thumb synchronize when the list scrolling, you must implement
 * the onScroll function to calculate the current section and set it to BladeView, so
 * the BladeView will redraw itself to perform synchronization.
 * 
 * As an blade view, the full sections and replaced section should set before drawing 
 * process start, you can either set it in xml file as attributes set or call functions,
 * we don't judge whether it is valid, so user need to pay attention, same with section 
 * baselines and indicator tops.
 * 
*/

public class BladeView extends View {
    private static final String TAG = "BladeView";
    private static final boolean DBG = true;
    
    private static final int DEFAULT_SECTION_ENABLE_COLOR = Color.BLACK;
    private static final int DEFAULT_SECTION_DISABLE_COLOR = Color.argb( 0xFF, 0xA5, 0xA5, 0xA5 );
    private static final int DEFAULT_PROMPT_ENABLE_COLOR = Color.WHITE;
    private static final int DEFAULT_PROMPT_DISABLE_COLOR = Color.argb( 0xFF, 0xA5, 0xA5, 0xA5 );
    
    private static final int PROMPT_ANIM_DURATION = 300;
    private static final int PROMPT_HIDE_ANGLE = -90;
    private static final int PROMPT_SHOW_ANGLE = 0;
    
    /* Sections include the absent ones. */
    private String[] mFullSections;
    
    /* Contains full sections, but some of its elements are replaced with dashes to save space. */
    private String[] mReplacedSections;
    private int mFullSectionLen;
    
    /* The baselines of each sections and top of indicators. */
    private int[] mSectionBaselines;
    private int[] mIndicatorTops;
    
    /* The real sections got from list. */
    private Object[] mListSections;
    private int mListSectionLen;
    
    /* The related list view and adapter. */
    private AbsListView mList;
    private BaseAdapter mListAdapter;
    /* When it is an header-footer list, represents the count of headers, 0 in normal list. */
    private int mListOffset;
    private SectionIndexer mSectionIndexer;

    /* A boolean array used to indicate whether the section is in the list, true if absent. */
    private boolean[] mIsAbsent;
    
    /* Use enable color and disable color to distinguish sections present and absent. */
    private Paint mEnableSectionPaint;
    private int mEnablePaintColor;

    private Paint mDisableSectionPaint;
    private int mDisablePaintColor;
    
    private int mEnablePromptColor;
    private int mDisablePromptColor;
    
    /* The prompt window and the text view showing in the window. */ 
    private PopupWindow mPromptWindow;
    private TextView mPromptText;
    private int mPromptHorzOffset;
    private int mPromptVertOffset;
    private Handler mHandler;
    
    /* The enter and exit rotate animation of the prompt window. */
    private PromptRotateAnimation mEnterAnim;
    private PromptRotateAnimation mExitAnim;
    private int mAnimationDuration;    
    
    /* Indicator drawable overlay the current section to prompt user whether he/she is. */
    private Drawable mIndicatorDrawable;
    
    /* The width and height of the indicator. */
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private Rect mIndicatorRect = new Rect();
    
    /* 
     * In order to optimize the drawing time, we only draw the dirty area of the view,
     * mRedrawRect restore information of the area.
     */
    private Rect mRedrawRect = new Rect();
    
    /* Font size of the sections. */
    private int mSectionFontSize; 
	
    /*TAO.JIANG, DATE20130329, s9201b, 
    	*add variable mMaxSectionFontSize, Font size of the current section, LINE*/
    private int mMaxSectionFontSize; 

    /* The current selected position in full sections. */
    private int mCurrentSection;
    
    /* The position of the previous selected section in full sections. */
    private int mOldSection;
    
    /* Record the previous motion position to reduce unnecessary list operation. */
    private int mLastMotionY;
    
    /* The section should be resurrected when user release his/her finger.*/
    private int mResurrectSection = -1;
    
    /* When handling touch event, block the setCurrentSection message sent by client.*/
    private boolean mBlockSetCurrent = false;
    
    /* Has or not user profile add for android4.1 contact*/
    private boolean mIsHasProfile = false;

    /*TAO.JIANG, DATE20130401, s9201b, add a Boolean variable to indicate whether the user touch the view, LINE */
    private boolean isTouched = false;

    /*TAO.JIANG, DATE20130403, s9201b, record the y position of touch, LINE */
    private int touchY;
    public BladeView(Context context) {
        this(context, null);
    }   
    
    public BladeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public BladeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);   
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BladeView);
        final Resources resources = context.getResources();
        
        /*Get the rotate animation duration from attributes set. */
        mAnimationDuration = a.getInt(R.styleable.BladeView_promptAnimationDuration, PROMPT_ANIM_DURATION);

        mSectionFontSize = a.getDimensionPixelSize(R.styleable.BladeView_sectionFontSize, 
                resources.getDimensionPixelSize(R.dimen.blade_font_size));
	 /*TAO.JIANG, DATE20130329, s9201b, init mMaxSectionFontSize, LINE*/
        mMaxSectionFontSize = a.getDimensionPixelSize(R.styleable.BladeView_sectionFontSize, 
                resources.getDimensionPixelSize(R.dimen.blade_font_size_max));
	 
        mEnablePaintColor = a.getColor(R.styleable.BladeView_enableSectionColor,
        		DEFAULT_SECTION_ENABLE_COLOR);
        mDisablePaintColor = a.getColor(R.styleable.BladeView_disableSectionColor,
        		DEFAULT_SECTION_DISABLE_COLOR);
        
        mEnablePromptColor = a.getColor(R.styleable.BladeView_enablePromptColor,
        		DEFAULT_PROMPT_ENABLE_COLOR);
        mDisablePromptColor = a.getColor(R.styleable.BladeView_disablePromptColor,
        		DEFAULT_PROMPT_DISABLE_COLOR);
        
        /* Get the indicator drawable and its size from attributes set. */
        final Drawable d = a.getDrawable(R.styleable.BladeView_bladeIndicator);
        if (d != null) {
            mIndicatorDrawable = d;
        } else {
            mIndicatorDrawable = resources.getDrawable(R.drawable.blade_indicator_normal);
        }
        
        mIndicatorWidth = a.getDimensionPixelSize(R.styleable.BladeView_bladeIndicatorWidth, 
                resources.getDimensionPixelSize(R.dimen.blade_indicator_width));
        mIndicatorHeight = a.getDimensionPixelSize(R.styleable.BladeView_bladeIndicatorHeight,
                resources.getDimensionPixelSize(R.dimen.blade_indicator_height));
        
        /* Get the offset of the prompt window. */
        mPromptHorzOffset = a.getDimensionPixelSize(R.styleable.BladeView_promptHorzOffset, 
                resources.getDimensionPixelSize(R.dimen.blade_prompt_horz_offset));
        mPromptVertOffset = a.getDimensionPixelSize(R.styleable.BladeView_promptVertOffset, 
                resources.getDimensionPixelSize(R.dimen.blade_prompt_vert_offset));
        //Log.d("liang"+"BladeView","mPromptHorzOffset = "+mPromptHorzOffset+", mPromptVertOffset = "+mPromptVertOffset);
        
        /* Get full sections and replaced sections from attributes set. */
//        int fullSectionId = a.getResourceId(R.styleable.BladeView_fullSectionsId, 0);
//        int replacedSectionId = a.getResourceId(R.styleable.BladeView_replacedSectionsId, 0); 
//        if (fullSectionId == 0 || replacedSectionId == 0) {
//            throw new Resources.NotFoundException("You have to specify section resources!");
//        }
        String [] fullSection = resources.getStringArray(R.array.blade_full_sections);
        String [] replacedSection = resources.getStringArray(R.array.blade_replaced_sections);

        setSections(fullSection, replacedSection);
        
        /* Get section baselines from attributes set. */
//        int blResId = a.getResourceId(R.styleable.BladeView_sectionBaselinesId, 0);
//        if (blResId == 0) {
//            throw new Resources.NotFoundException("You have to specify section baselines!");
//        }
        
        int [] baselineStrings = resources.getIntArray(R.array.blade_section_baselines);
        setSectionBaseLines(baselineStrings);
        
        /* Get indicator tops from attributes set. */
//        int indResId = a.getResourceId(R.styleable.BladeView_indicatorTopsId, 0);
//        if (indResId == 0) {
//            throw new Resources.NotFoundException("You have to specify indicator tops!");
//        }
        
        int [] indicatorTops = resources.getIntArray(R.array.blade_indicator_tops);
        setIndicatorTops(indicatorTops);
        
        a.recycle();
        
        init(context);
    }
    
    private void init(Context context) {         
        /* Get array and other attributes from xml. */
        final Resources resources = context.getResources();
        
        /* Define the attributes of the paint used to draw sections. */
        mEnableSectionPaint = new Paint();
        mEnableSectionPaint.setColor(mEnablePaintColor);
        mEnableSectionPaint.setTypeface(Typeface.DEFAULT);
        mEnableSectionPaint.setAntiAlias(true);
        mEnableSectionPaint.setTextAlign(Paint.Align.CENTER);
        mEnableSectionPaint.setTextSize(mSectionFontSize);
        
        mDisableSectionPaint = new Paint(mEnableSectionPaint);
        mDisableSectionPaint.setColor(mDisablePaintColor);      
        
        /* Inflate prompt window content view resource from xml file. */
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mPromptText = (TextView) layoutInflater.inflate(R.layout.blade_popup_text, null);
        mPromptText.setFocusable(false);
        
        /* Handler used to post dismiss the prompt window message. */
        mHandler = new Handler();
        
        /* Enter and exit animation of pop up window content. */
        int promptWidth = resources.getDimensionPixelSize(R.dimen.blade_prompt_width);
        mEnterAnim = new PromptRotateAnimation(-PROMPT_HIDE_ANGLE, PROMPT_SHOW_ANGLE, promptWidth >> 1);
        mEnterAnim.setDuration(mAnimationDuration);
        mEnterAnim.setFillAfter(true);
        mEnterAnim.setInterpolator(new LinearInterpolator());
        
        mExitAnim = new PromptRotateAnimation(PROMPT_SHOW_ANGLE, PROMPT_HIDE_ANGLE, promptWidth >> 1);
        mExitAnim.setDuration(mAnimationDuration);
        mExitAnim.setFillAfter(true);
        mExitAnim.setInterpolator(new LinearInterpolator());
    }

	
    /*TAO.JIANG, DATE20130329, s9201b,modify onDraw method to realize the visual effect below, DATE20130329-1 START*/
		        /*
									A                  A
									B                  B
									C                  C
									D              D
									E           E
									F          F
			modify bladeview from		G    to  G
									H          H
									I            I
									J               J
									K                  K
									L			 L
									M			 M
	        */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

 	 /*DELETE original code start*/
	 /* Draw indicator. */ 
	 /*
	  int offsetX = (getMeasuredWidth() - mIndicatorWidth) >> 1 ;
	  Log.i(TAG, "offsetX = "+offsetX+", getMeasureWidth = "+getMeasuredWidth()+", mIndicatorWidth = "+mIndicatorWidth);
        mIndicatorRect.set(offsetX, mIndicatorTops[mCurrentSection], offsetX + mIndicatorWidth,
                mIndicatorTops[mCurrentSection] + mIndicatorHeight);
        mIndicatorDrawable.setBounds(mIndicatorRect);
        mIndicatorDrawable.draw(canvas); 
	 
        /* Draw replaced sections, different color indicates whether the section is present. */
 	 /*
	  int centerWidth = getMeasuredWidth() >>1;
        for (int i = 0; i < mFullSectionLen; i++) {

            if (mIsAbsent[i]) {
                canvas.drawText(mFullSections[i], centerWidth,
                        mSectionBaselines[i], mDisableSectionPaint);
            } else {
                canvas.drawText(mFullSections[i], centerWidth,
                        mSectionBaselines[i], mEnableSectionPaint);
            }
        }
        DELETE original code end*/ 
        
       /*ADD new code start*/
	  int measuredWidth = getMeasuredWidth();
	 if(!isTouched){//when user release his/her finger, make the bladeview straight
	 	  /* Draw indicator. */ 
		  int offsetX = measuredWidth *4/5 - mIndicatorWidth/2 ;
		  Log.i(TAG, "offsetX = "+offsetX+", getMeasureWidth = "+measuredWidth+", mIndicatorHeight = "+mIndicatorHeight);
	        mIndicatorRect.set(offsetX, mIndicatorTops[mCurrentSection], offsetX + mIndicatorWidth,
	                mIndicatorTops[mCurrentSection] + mIndicatorHeight);
	        mIndicatorDrawable.setBounds(mIndicatorRect);
	        mIndicatorDrawable.draw(canvas); 
		  /* Draw replaced sections, different color indicates whether the section is present. */
	 	  mEnableSectionPaint.setTextSize(mSectionFontSize);
		  mDisableSectionPaint.setTextSize(mSectionFontSize);

		  int centerWidth = measuredWidth *4/5;
	        for (int i = 0; i < mFullSectionLen; i++) {
	            if (mIsAbsent[i]) {
	                canvas.drawText(mFullSections[i], centerWidth,
	                        mSectionBaselines[i], mDisableSectionPaint);
	            } else {
	                canvas.drawText(mFullSections[i], centerWidth,
	                        mSectionBaselines[i], mEnableSectionPaint);
	            }
	        }
		//when user touch the blade view, curve the bladeview 
 	  }else{
		  
	        for (int i = 0; i < mFullSectionLen; i++) {
			
			int centerWidth = measuredWidth *4/5;
			int fontSize;
			int offsetY = Math.abs(mSectionBaselines[i] - touchY);
			//mIndicatorHeight' value equals the distance between two adjacent sections
			if( offsetY <  mIndicatorHeight* 4 ){
				centerWidth = (offsetY * offsetY * (centerWidth - measuredWidth / 10)) / (mIndicatorHeight*mIndicatorHeight*4*4) + measuredWidth / 10 + 5;
				fontSize = (mSectionFontSize - mMaxSectionFontSize)*offsetY/(4*mIndicatorHeight) 
										+ mMaxSectionFontSize;
				mEnableSectionPaint.setTextSize(fontSize);
			  	mDisableSectionPaint.setTextSize(fontSize);
				Log.i(TAG, "mSectionFontSize = "+mSectionFontSize+", mMaxSectionFontSize = "+mMaxSectionFontSize);
				Log.i(TAG, "offsetY = "+offsetY+", final centerWidth = "+centerWidth+", fontSize = "+fontSize);
			}else{
			 	mEnableSectionPaint.setTextSize(mSectionFontSize);
			      mDisableSectionPaint.setTextSize(mSectionFontSize);				
			}
			
	            if (mIsAbsent[i]) {
	                canvas.drawText(mFullSections[i], centerWidth,
	                        mSectionBaselines[i], mDisableSectionPaint);
	            } else {
	                canvas.drawText(mFullSections[i], centerWidth,
	                        mSectionBaselines[i], mEnableSectionPaint);
	            }
	        }
 	  	}
	   /*ADD new code end*/
    }
    /*TAO.JIANG, DATE20130329-1, END*/
    @Override
	public boolean onTouchEvent(MotionEvent ev) {
		/* If there is no list related, use the default touch event handling process. */
		if (mList == null) {
			return super.onTouchEvent(ev);
		}

		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();
		 /*TAO.JIANG, DATE20130403, s9201b, record y for using in onDraw method, LINE*/
		touchY = y;
		int toSection;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/* If the list is in a flinging, stop it. */
			cancelFling();
			 /*TAO.JIANG, DATE20130401, s9201b, set isTouched true, LINE*/
			isTouched = true;
			if (mListAdapter == null && mList != null) {
				getSectionsFromIndexer();
			}

			/*
			 * Records the old section position and the calculate the current for later use.
			 */
			mBlockSetCurrent = true;
			mLastMotionY = y;
			mOldSection = mCurrentSection; // Record the old section first.

			toSection = calcSectionIndex(x, y);
			mCurrentSection = toSection;

			if (DBG) {
				Log.i(TAG, "onTouchEvent:ACTION_DOWN,x = " + x + ",y = " + y
						+ ",mCurrentSection = " + mCurrentSection);
			}

			setPromptText(toSection);
			showPromptWindow();

			/* Start the rotate animation as enter effect. */
			mPromptText.startAnimation(mEnterAnim);
			//Log.d("liang"+"BladeView","mCurrentSection = "+mCurrentSection);
			/* Move the list to the related position. */
			moveListToSection(mCurrentSection);

			/* Invalidate the invalid rectangle to do self redraw process if needed. */
			redrawIfNeeded();
			break;

		case MotionEvent.ACTION_MOVE:
			  /*TAO.JIANG, DATE20130401, s9201b, set isTouched true, LINE*/
			isTouched = true;
			/* Judge whether current motion position and the last motion position are in the same slot. */
			if (y == mLastMotionY) {
				return true;
			}
			/*TAO.JIANG, DATE20130403, s9201b, no need to judge, delete!!!, DATE20130403-1 START */
	 		/* else if (Math.abs(y - mLastMotionY) < mIndicatorHeight) {
				for (int i = 1; i < mFullSectionLen; i++) {
					if (y >= mSectionBaselines[i - 1] && y < mSectionBaselines[i]
							&& mLastMotionY >= mSectionBaselines[i - 1] && mLastMotionY < mSectionBaselines[i]) {
						return true;
					}
				}
			}*/
			/*TAO.JIANG, DATE20130403-1 END */
			mLastMotionY = y;
			mOldSection = mCurrentSection;
			toSection = calcSectionIndex(x, y);
			mCurrentSection = toSection;

			setPromptText(toSection);
			showPromptWindow();

			if (DBG) {
				Log.i(TAG, "onTouchEvent:ACTION_MOVE,x = " + x + ",y = " + y
						+ ",mCurrentSection = " + mCurrentSection);
			}

			/* Move the list to the related position. */
			moveListToSection(mCurrentSection);
			invalidate();
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (DBG) {
//				Xlog.i(TAG, "onTouchEvent:ACTION_UP or CANCEL,x = " + x + ",y = " + y + 
//						",mCurrentSection = " + mCurrentSection);
			}
			 /*TAO.JIANG, DATE20130329, s9201b, set isTouched false when user release his/her finger, LINE*/
			isTouched = false;
			/* Dismiss the pop up window if it is currently visible. */
			dismissPromptWindow();

			mBlockSetCurrent = false;
			mLastMotionY = -1;
			Log.i(TAG, "onTouchEvent:ACTION_UP or CANCEL,mResurrectSection = " + mResurrectSection +
					",mCurrentSection = " + mCurrentSection);
			if (mCurrentSection != mResurrectSection && mResurrectSection != -1) {
				mCurrentSection = mResurrectSection;
				mResurrectSection = -1;
				/*TAO.JIANG, DATE20130401, s9201b, move outside, LINE*/
				//invalidate();
			}
			/*TAO.JIANG, DATE20130401, s9201b, redraw always, LINE*/
			invalidate();
			break;

		default:
			/* Dismiss the pop up window if it is currently visible. */
			dismissPromptWindow();
			mBlockSetCurrent = false;
			break;
		}

		return true;
	} 

    /**
     * Calculate the current action affect which section, use section baselines
     * to locate, the bound of each section is between its previous section
     * bottom and the baseline of itself. 
     * 
     * @param x The x axis of the action.
     * @param y The y axis of the action.
     * @return The index in the full sections, revise it when the result is out of bounds.
     */
    private int calcSectionIndex(final float x, final float y) {
        if (DBG) {
//            Xlog.i(TAG, "calcSectionIndex: x = " + x + ",y = " + y);
        }        

        for (int i = 1; i < mFullSectionLen; i++) {
            if (y >= mSectionBaselines[i-1] && y < mSectionBaselines[i]) {
                return i;
            }
        }

        if (y < mSectionBaselines[0]) {
            return 0;
        }

        return mFullSectionLen - 1;
    }

    /**
     * Move the list to the start position of the current selected section.
     * 
     * @param fullSection The index in list sections.
     */
    private void moveListToSection(final int fullSection) {
        int listSection = getListSectionIndex(fullSection);
        Log.d("liang"+"BladeView", "listSection = "+listSection);
        int position = mSectionIndexer.getPositionForSection(listSection);
        Log.d("liang"+"BladeView","position = "+position);
        if (DBG) {
//            Xlog.i(TAG, "moveListToSection: fullSection = " + fullSection + 
//                    ",listSection = " + listSection + ",position = " + position);
        }   
        Log.d("liang"+"BladeView", "mListOffset = "+mListOffset);

        /* Add mListOffset for all list view, because it will be 0 if it has no header. */
       ((ListView) mList).setSelectionFromTop(position + mListOffset, 0);        
    }
    
    /**
     * Translate list section position into index in full section.
     * 
     * @param listIndex The list section position(mListSections).
     * @return The index in full section(mOriginSections), if there is
     *      not return -1.
     */
    private int getFullSectionIndex(int listIndex) {
    	Log.d("liang"+TAG, "mFullSectionLen = "+mFullSectionLen);
        for (int i = 0; i < mFullSectionLen; i++) {
            if (mFullSections[i].equals(mListSections[listIndex])) {
                return i;
            }
        }

        return -1;
    }
    
    /**
     * Translate full section index(mOriginSections) to position in list section.
     *  
     * @param fullIndex The index in full section.
     * @return The index in list section.
     */
    private int getListSectionIndex(int fullIndex) {
    	Log.d("liang"+"BladeView", "mListSectionLen = "+mListSectionLen);
    	Log.d("liang"+"BladeView", "mIsHasProfile= " + mIsHasProfile);
//        for (int i = 0; i < mListSectionLen; i++) {
//        	Log.d("liang"+"BladeView","i = "+i);
//           	Log.d("liang"+"BladeView", "mListSections[i] = "+mListSections[i]);
//        	Log.d("liang"+"BladeView", "mFullSections[fullIndex] = "+mFullSections[fullIndex]);
//			if (mIsHasProfile) {
//				if (mListSections[i+1] != null
//						&& mFullSections[fullIndex]
//								.compareToIgnoreCase(mListSections[i+1]
//										.toString()) <= 0) {
//					Log.d("liang"+"BladeView","i = "+i);
//					return i+1;
//				}
//			} else {
//				if (mListSections[i] != null
//						&& mFullSections[fullIndex]
//								.compareToIgnoreCase(mListSections[i]
//										.toString()) <= 0) {
//					Log.d("liang"+"BladeView","i = "+i);
//					return i;
//				}
//			}
//        }
        
        if (mIsHasProfile) {
        	for (int i = 1; i < mListSectionLen; i++) {
        		if (mListSections[i] != null
						&& mFullSections[fullIndex]
								.compareToIgnoreCase(mListSections[i]
										.toString()) <= 0) {
					Log.d("liang"+"BladeView","i = "+i);
					return i;
				}
        	}
        	
        }else {
        	for (int i = 0; i < mListSectionLen; i++) {
        		if (mListSections[i] != null
						&& mFullSections[fullIndex]
								.compareToIgnoreCase(mListSections[i]
										.toString()) <= 0) {
					Log.d("liang"+"BladeView","i = "+i);
					return i;
				}
        	}
        }
        
        return (mListSectionLen - 1);
    }
    
    /**
     * Send an ACTION_CANCEL message to stop list fling.
     */
    private void cancelFling() {
        MotionEvent cancelFling = MotionEvent.obtain(
                0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }
    
    /**
     * Set text content of the prompt text view.
     * 
     * @param section The index of the current section.
     */
    private void setPromptText(final int section) {
        /* Show pop up window to prompt user where he/she is currently. */
        mPromptText.setText(mFullSections[section]);
        if (!mIsAbsent[section]) {
            mPromptText.setTextColor(mEnablePromptColor);
        } else {
            mPromptText.setTextColor(mDisablePromptColor);
        }
    }

    /**
     * Redraw parts of the blade view to optimize if needed.
     */
    private void redrawIfNeeded() {
        mRedrawRect.setEmpty();
        /* Optimized, redraw parts of itself only if the thumb changes its position. */
        if (mIndicatorTops[mCurrentSection] != mIndicatorTops[mOldSection]) {
            if (mCurrentSection > mOldSection) {
                mRedrawRect.set(0, mIndicatorTops[mOldSection], getWidth(),
                        mIndicatorTops[mCurrentSection] + mIndicatorHeight);
            } else {
                mRedrawRect.set(0, mIndicatorTops[mCurrentSection], getWidth(),
                        mIndicatorTops[mOldSection] + mIndicatorHeight);
            }
        }
        invalidate(mRedrawRect);
    }
    
    /**
     * Set full sections and replaced sections with string array resource id.
     * 
     * @param fullSectionId The full section string array resource id.
     * @param replacedSectionId The replaced section string array resource id.
     */
    public void setSections(final int fullSectionId, final int replacedSectionId) {
        final Resources resources = getContext().getResources();
        String[] fullSections = resources.getStringArray(fullSectionId);
        String[] replacedSections = resources.getStringArray(replacedSectionId);
        setSections(fullSections, replacedSections);
    }
    
    /**
     * Set full sections and replaced sections with string arrays.
     * 
     * @param fullSectionArray The full section string array.
     * @param replacedSectionArray The replaced section string array.
     */
    public void setSections(final String[] fullSectionArray, final String[] replacedSectionArray) {
        if (fullSectionArray == null || replacedSectionArray == null) {
//            throw new InvalidParameterException("Origin sections and replaced section should not be a null pointer!");
        }
        
        if (fullSectionArray.length != replacedSectionArray.length) {
//            throw new InvalidParameterException("The length of origin and replaced sections should be equal !");
        }
        
        mFullSections = fullSectionArray;
        mReplacedSections = replacedSectionArray;
        
        mFullSectionLen = mFullSections.length;
        mIsAbsent = new boolean[mFullSectionLen];
        
        setAbsentSections();
        invalidate();
    }
    
    /**
     * Get the length of full sections.
     * 
     * @return The length of full sections.
     */
    public int getFullSectionLength() {
        return mFullSectionLen;
    }
    
    /**
     * Set the section drawing baselines array resource id.
     * 
     * @param blResId The baseline array resource id.
     */
    public void setSectionBaseLines(final int blResId) {
        final Resources resources = getContext().getResources();
        int[] baselines = resources.getIntArray(blResId);
        setSectionBaseLines(baselines);
    }
    
    /**
     * Set the section drawing baselines.
     * 
     * @param baselines The baselines integer array.
     */
    public void setSectionBaseLines(final int[] baselines) {        
        if (baselines == null) {
//            throw new InvalidParameterException("Baselines should not be a null pointer.");
        }
        
        if (baselines.length != mFullSections.length) {
//            throw new InvalidParameterException(
//                    "The length of baselines and full section should be equal!");
        }
        
        mSectionBaselines = baselines;
        invalidate();
    }
    
    /**
     * Set the tops of each indicator with resource id.
     * 
     * @param indResId The tops array resource id.
     */
    public void setIndicatorTops(final int indResId) {
        final Resources resources = getContext().getResources();
        int[] indTops = resources.getIntArray(indResId);
        setIndicatorTops(indTops);
    }
    
    /**
     * Set the tops of each indicator with an integer array.
     * 
     * @param indTops The tops array.
     */
    public void setIndicatorTops(final int[] indTops) {
        if (indTops == null) {
//            throw new InvalidParameterException("Indicator tops should not be a null pointer.");
        }
        
        if (indTops.length != mFullSections.length) {
//            throw new InvalidParameterException(
//                    "The length of indicator tops and full section should be equal!");
        }
        
        mIndicatorTops = indTops;   
        invalidate();
    }
    
    /**
     * Set the indicator drawable to the specified resource.
     * 
     * @param indicator The drawable resource.
     */
    public void setIndicatorDrawable(final Drawable indicator) {
        if (indicator != null) {
            mIndicatorDrawable = indicator;
            invalidate();
        }
    }
    
    /**
     * Get the indicator drawable.
     * 
     * @return The indicator drawable.
     */
    public Drawable getIndicatorDrawable() {
        return mIndicatorDrawable;
    }
    
    /**
     * Set the height of the indicator.
     * 
     * @param height The height of the indicator.
     */
    public void setIndicatorHeight(final int height) {
        mIndicatorHeight = height;
        invalidate();
    }
    
    /**
     * Get the height of the indicator.
     * 
     * @return The height of the indicator.
     */
    public int getIndicatorHeight() {
        return mIndicatorHeight;
    }
    
    /**
     * Get the length of list sections.
     * 
     * @return The length of list sections.
     */
    public int getListSectionLength() {
        return mListSectionLen;
    }
    
    /**
     * Get the current position in full sections.
     * 
     * @return The current section index.
     */
    public int getCurrentSection() {
        return mCurrentSection;
    }
    
    /**
     * Set current index to the specified sectionIndex, this will be
     * called after the related list scrolled, the indicator will also
     * update it position if needed. 
     * 
     * @param listIndex The position in the list section array, it will be 
     *      translated to the index in the full origin section array. 
     */
    public void setCurrentSection(final int listIndex) {
        /* If there isn't any sections, do nothing, just return. */
        if (mListSections == null) {
            return;
        }
        
        int fullIndex = getFullSectionIndex(listIndex);
        Log.d("liang"+TAG, "mCurrentSection = "+mCurrentSection+", fullIndex = "+fullIndex);
        if (mCurrentSection == fullIndex) {
            mResurrectSection = mCurrentSection;
            return;
        }

        if (mBlockSetCurrent) {
        	mResurrectSection = fullIndex; 
        	if (mResurrectSection < 0) {
        		mResurrectSection = 0;
            } else if (mResurrectSection > mFullSectionLen - 1) {
            	mResurrectSection = mFullSectionLen - 1;
            }
        } else {        	
            mOldSection = mCurrentSection;
            mCurrentSection = fullIndex; 
            
            if (mCurrentSection < 0) {
                mCurrentSection = 0;
            } else if (mCurrentSection > mFullSectionLen - 1) {
                mCurrentSection = mFullSectionLen - 1;
            }
            
            redrawIfNeeded();
        }
    }

    /**
     * Set the related list view and get indexer information from it, 
     * every time when you change the data of list, you should call it 
     * to reset the list.
     * 
     * @param listView The related list view.
     */
    public void setList(final AbsListView listView) {
        if (listView != null) {
            mList = listView;
            
            /* Disable fast scroller and hide the vertical scroll bar. */
            mList.setFastScrollEnabled(false);
            mList.setVerticalScrollBarEnabled(false);
            
            /* Get section indexers information form list. */
            getSectionsFromIndexer();
            invalidate();
        } else {
            throw new IllegalArgumentException("Can not set a null list!");
        }
    }
    
    /**
     * Get the related list view.
     * 
     * @return The related list.
     */
    public AbsListView getList() {
        return mList;
    }
     
    /**
     * Get sections and section indexers, then initialize adapter with the list
     * adapter, in the end of the function, the absent array will be updated.
     */
    private void getSectionsFromIndexer() {
    	Log.d("liang"+TAG, "getSectionsFromIndexer()");
        Adapter adapter = mList.getAdapter();
        mSectionIndexer = null;
        if (adapter instanceof HeaderViewListAdapter) {
        	Log.d("liang"+TAG, "adapter instanceof HeaderViewListAdapter");
            mListOffset = ((HeaderViewListAdapter) adapter).getHeadersCount();
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }

        if (adapter instanceof SectionIndexer) {
        	Log.d("liang"+TAG, "adapter instanceof SectionIndexer");
            mListAdapter = (BaseAdapter) adapter;
            mSectionIndexer = (SectionIndexer) adapter;
            mListSections = mSectionIndexer.getSections();
            mListSectionLen = mListSections.length;
        } else {
        	Log.d("liang"+TAG, "else----------->");
            mListAdapter = (BaseAdapter) adapter;
            mListSections = new String[] { "" };
            mListSectionLen = 0;
        }
        
        setAbsentSections();
    }
    
    /**
     * Set absent section array, if the section in full sections is not
     * in the list sections, set absent flag to true, otherwise false.
     */
    private void setAbsentSections() {
        /*
         * If the section is not included in the list, set the corresponding
         * flag in mIsAbsent to true, else false.
         */
        for (int i = 0; i < mFullSectionLen; i++) {
            mIsAbsent[i] = true;
            for (int j = 0; j <= i && j < mListSectionLen; j++) {
                if (mListSections[j].equals(mFullSections[i])) {
                    mIsAbsent[i] = false;
                    break;
                }
            }
        }
    }
    
    /**
     * Judge whether the given section is absent or present.
     * 
     * @param section
     * @return whether the section is absent.
     */
    public boolean isAbsentSection(Object section) {
        if (section == null) {
            return true;
        }

        for (int i = 0, len = mListSections.length; i < len; i++) {
            if (section.equals(mListSections[i])) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Set the rotate animation duration to the specified interval.
     * 
     * @param duration The animation duration want to be set.
     */
    public void setAnimationDuration(final int duration) {
        mAnimationDuration = duration;
    }
    
    /**
     * Get the rotate animation duration.
     * @return The duration of the rotate animation.
     */
    public int getAnimationDuration() {
        return mAnimationDuration;
    }
    
    /**
     * Set the paint color of present sections.
     */
    public void setEnableSectionColor(int color) {
    	mEnablePaintColor = color;
        mEnableSectionPaint.setColor(mEnablePaintColor);
    }
    
    /**
     * Set the paint color of absent sections.
     */
    public void setDisableSectionColor(int color) {
    	mDisablePaintColor = color;
        mDisableSectionPaint.setColor(mDisablePaintColor);
    }

    /**
     * Set the text color of present prompt window.
     */
    public void setEnablePromptColor(int color) {
    	mEnablePromptColor = color;
    }
    
    /**
     * Set the text color of absent prompt window.
     */
    public void setDisablePromptColor(int color) {
    	mDisablePromptColor = color;
    }

    /**
     * Start the window exit animation, and then dismiss the pop up window
     * after the animation has been played.
     */
    private void dismissPromptWindow() {
        mPromptText.startAnimation(mExitAnim);
        
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (mPromptWindow != null && mPromptText != null) {
                    mPromptWindow.dismiss();
                } 
            }
        }, mAnimationDuration);
    }


    public PopupWindow getPromptWindow(){
	return mPromptWindow;
    }

	
    /**
     * Create the prompt window and then show it at appropriate position.
     */
    private void showPromptWindow() {
        /* Make sure we have a window before showing the pop up window. */
        if (getWindowVisibility() == View.VISIBLE) {
            createPromptWindow();
            positionPromptWindow();
        }
    }

    /**
     * Create the prompt window and set its background to be transparent.
     */
    private void createPromptWindow() {
        if (mPromptWindow == null) {
            Context c = getContext();
            PopupWindow p = new PopupWindow(c);

            p.setContentView(mPromptText);
            p.setWidth(LayoutParams.WRAP_CONTENT);
            p.setHeight(LayoutParams.WRAP_CONTENT);
            
            /* Set background of the prompt window to transparent. */
            ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
            p.setBackgroundDrawable(dw);
          
            mPromptWindow = p;
        }
    }
    
    public void setHasProfile(boolean hasOrNot) {
    	this.mIsHasProfile = hasOrNot;
    }
    
    /**
     * Calculate the pop up window offset and position it in the right place.
     */
    private void positionPromptWindow() {
        if (DBG) {
//            Xlog.i(TAG, "positionPopup: mPromptHorzOffset = " + mPromptHorzOffset + 
//            		",mPromptVertOffset = " + mPromptVertOffset);
        }
        
        /* When touch event move out of the view, mHintText becomes null causes window leak. */
        if (mPromptText == null) {
            return;
        }
        
        /* Show the prompt window or update its position if it is already visible. */
	  /*TAO.JIANG, DATE20130329, 
	    *make the location of promptWindow dynamically changing depend on currentSection, DATE20130329-3 START
	    */
        if (!mPromptWindow.isShowing()) {
            mPromptWindow.showAtLocation(this, 
                    Gravity.TOP |Gravity.RIGHT, mPromptHorzOffset, mSectionBaselines[mCurrentSection]+mPromptVertOffset);
        } else {
            mPromptWindow.update(mPromptHorzOffset, mSectionBaselines[mCurrentSection]+mPromptVertOffset, -1, -1);
        }
	  /*TAO.JIANG, DATE20130329-3, END*/
    }
}
