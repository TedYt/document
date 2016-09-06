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
 */

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.preference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Handler;
import android.preference.Preference.OnPreferenceChangeInternalListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * An adapter that returns the {@link Preference} contained in this group.
 * In most cases, this adapter should be the base class for any custom
 * adapters from {@link Preference#getAdapter()}.
 * <p>
 * This adapter obeys the
 * {@link Preference}'s adapter rule (the
 * {@link Adapter#getView(int, View, ViewGroup)} should be used instead of
 * {@link Preference#getView(ViewGroup)} if a {@link Preference} has an
 * adapter via {@link Preference#getAdapter()}).
 * <p>
 * This adapter also propagates data change/invalidated notifications upward.
 * <p>
 * This adapter does not include this {@link PreferenceGroup} in the returned
 * adapter, use {@link PreferenceCategoryAdapter} instead.
 * 
 * @see PreferenceCategoryAdapter
 */
class PreferenceGroupAdapter extends BaseAdapter implements OnPreferenceChangeInternalListener {
    
    private static final String TAG = "PreferenceGroupAdapter";

    /**
     * The group that we are providing data from.
     */
    private PreferenceGroup mPreferenceGroup;
    
    /**
     * Maps a position into this adapter -> {@link Preference}. These
     * {@link Preference}s don't have to be direct children of this
     * {@link PreferenceGroup}, they can be grand children or younger)
     */
    private List<Preference> mPreferenceList;
    
    /**
     * List of unique Preference and its subclasses' names. This is used to find
     * out how many types of views this adapter can return. Once the count is
     * returned, this cannot be modified (since the ListView only checks the
     * count once--when the adapter is being set). We will not recycle views for
     * Preference subclasses seen after the count has been returned.
     */
    private ArrayList<PreferenceLayout> mPreferenceLayouts;

    private PreferenceLayout mTempPreferenceLayout = new PreferenceLayout();

    /**
     * Blocks the mPreferenceClassNames from being changed anymore.
     */
    private boolean mHasReturnedViewTypeCount = false;
    
    private volatile boolean mIsSyncing = false;
    
    private Handler mHandler = new Handler(); 
    
    //add for wifi access point cache
    private int accessPointFlag=0;
    private int additionalTypeCount=0;
    
    private ArrayList<Integer> mPreferenceListBackgroundIndex;
    private final int SINGLE_LINE_ROUND_CORNER_BACKGROUND = 1;
    private final int TOP_ROUND_CORNER_BACKGROUND = 2;
    private final int BOTTOM_ROUND_CORNER_BACKGROUND = 3;
    private final int CENTER_RECTANGLE_BACKGROUND = 4;
    private final int NO_BACKGOUND = 5;
    private Runnable mSyncRunnable = new Runnable() {
        public void run() {
            syncMyPreferences();
        }
    };

    private static class PreferenceLayout implements Comparable<PreferenceLayout> {
        private int resId;
        private int widgetResId;
        private String name;

        public int compareTo(PreferenceLayout other) {
            int compareNames = name.compareTo(other.name);
            if (compareNames == 0) {
                if (resId == other.resId) {
                    if (widgetResId == other.widgetResId) {
                        return 0;
                    } else {
                        return widgetResId - other.widgetResId;
                    }
                } else {
                    return resId - other.resId;
                }
            } else {
                return compareNames;
            }
        }
    }

    public PreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
        if(preferenceGroup!=null && "key_wifi_settings".equals(preferenceGroup.getKey())){
            //add an additional type for AccessPoint
            Log.i(TAG, "For WiFi settings page, add an additional type to cache AccessPoint");
            additionalTypeCount = 1;
        }
        mPreferenceGroup = preferenceGroup;
        // If this group gets or loses any children, let us know
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferenceList = new ArrayList<Preference>();
        mPreferenceLayouts = new ArrayList<PreferenceLayout>();

        syncMyPreferences();
    }

    private void syncMyPreferences() {
        synchronized(this) {
            if (mIsSyncing) {
                return;
            }

            mIsSyncing = true;
        }

        List<Preference> newPreferenceList = new ArrayList<Preference>(mPreferenceList.size());
        mPreferenceListBackgroundIndex = new ArrayList<Integer>(mPreferenceList.size());
        flattenPreferenceGroup(newPreferenceList, mPreferenceGroup);
        mPreferenceList = newPreferenceList;
        
        notifyDataSetChanged();

        synchronized(this) {
            mIsSyncing = false;
            notifyAll();
        }
    }
    
    private void flattenPreferenceGroup(List<Preference> preferences, PreferenceGroup group) {
        // TODO: shouldn't always?
        group.sortPreferences();

        final int groupSize = group.getPreferenceCount();
        final int[] tempIndexOfPrefrence  = calcItemsBetweenCategory(group);
        
        for (int i = 0; i < groupSize; i++) {
            final Preference preference = group.getPreference(i);
            preferences.add(preference);
            
            if(tempIndexOfPrefrence[i] == 0){
            	
            	mPreferenceListBackgroundIndex.add(NO_BACKGOUND);
			} else if (tempIndexOfPrefrence[i] == 1
					&& (i == 0 ? true : tempIndexOfPrefrence[i - 1] <= 1)){
				if(i == (groupSize - 1) ? true	: tempIndexOfPrefrence[i + 1] <= 1){
					mPreferenceListBackgroundIndex.add(SINGLE_LINE_ROUND_CORNER_BACKGROUND);
				}else {
					mPreferenceListBackgroundIndex.add(TOP_ROUND_CORNER_BACKGROUND);
				}
			}else if(tempIndexOfPrefrence[i] > 1){
				if(i == (groupSize - 1) ? true	: tempIndexOfPrefrence[i + 1] <= 1){
					mPreferenceListBackgroundIndex.add(BOTTOM_ROUND_CORNER_BACKGROUND);
				}else {
					mPreferenceListBackgroundIndex.add(CENTER_RECTANGLE_BACKGROUND);
				}
			}
					
            
            if (!mHasReturnedViewTypeCount && !preference.hasSpecifiedLayout()) {
                addPreferenceClassName(preference);
            }else 
            if(additionalTypeCount>0 && accessPointFlag <= 0 && "com.android.settings.wifi.AccessPoint".equals(preference.getClass().getName())){
                //as WiFi access point list will be generated dynamically, the AccessPoint preference will be cached later
                Log.i(TAG, "Add Wifi Access Point preference into cache  == mtk80906");
                accessPointFlag++;
                addPreferenceClassName(preference);
            }
            
            if (preference instanceof PreferenceGroup) {
                final PreferenceGroup preferenceAsGroup = (PreferenceGroup) preference;
                if (preferenceAsGroup.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(preferences, preferenceAsGroup);
                }
            }

            preference.setOnPreferenceChangeInternalListener(this);
        }
    }

    /**
     * Creates a string that includes the preference name, layout id and widget layout id.
     * If a particular preference type uses 2 different resources, they will be treated as
     * different view types.
     */
    private PreferenceLayout createPreferenceLayout(Preference preference, PreferenceLayout in) {
        PreferenceLayout pl = in != null? in : new PreferenceLayout();
        pl.name = preference.getClass().getName();
        pl.resId = preference.getLayoutResource();
        pl.widgetResId = preference.getWidgetLayoutResource();
        return pl;
    }

    private void addPreferenceClassName(Preference preference) {
       // Log.i(TAG, "addPreferenceClassName(), class name = "+((preference==null)?"--":preference.getClass().getName()));
        final PreferenceLayout pl = createPreferenceLayout(preference, null);
        int insertPos = Collections.binarySearch(mPreferenceLayouts, pl);

        // Only insert if it doesn't exist (when it is negative).
        if (insertPos < 0) {
            // Convert to insert index
            insertPos = insertPos * -1 - 1;
            mPreferenceLayouts.add(insertPos, pl);
        }
      //  Log.i(TAG, "cached mPreferenceLayouts size = "+mPreferenceLayouts.size());
    }
    
    public int getCount() {
        return mPreferenceList.size();
    }

    public Preference getItem(int position) {
        if (position < 0 || position >= getCount()) return null;
        return mPreferenceList.get(position);
    }

    public long getItemId(int position) {
        if (position < 0 || position >= getCount()) return ListView.INVALID_ROW_ID;
        return this.getItem(position).getId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final Preference preference = this.getItem(position);
        // Build a PreferenceLayout to compare with known ones that are cacheable.
        mTempPreferenceLayout = createPreferenceLayout(preference, mTempPreferenceLayout);
Log.i(TAG,mPreferenceListBackgroundIndex.get(position) +"   "+ preference.getClass().getName());

        // If it's not one of the cached ones, set the convertView to null so that 
        // the layout gets re-created by the Preference.
        if (Collections.binarySearch(mPreferenceLayouts, mTempPreferenceLayout) < 0) {
            convertView = null;
        }
        View mView = preference.getView(convertView, parent);
     // he.liu added at 2012.5.02
            
             if(mPreferenceListBackgroundIndex.get(position) == SINGLE_LINE_ROUND_CORNER_BACKGROUND){
             	mView.setBackgroundResource(com.android.internal.R.drawable.item_back_ground);
             }else if(mPreferenceListBackgroundIndex.get(position) == TOP_ROUND_CORNER_BACKGROUND){
             	mView.setBackgroundResource(com.android.internal.R.drawable.item_back_ground_one);
             }else if(mPreferenceListBackgroundIndex.get(position) == CENTER_RECTANGLE_BACKGROUND){
             	mView.setBackgroundResource(com.android.internal.R.drawable.item_back_ground_two);
             }else if(mPreferenceListBackgroundIndex.get(position) == BOTTOM_ROUND_CORNER_BACKGROUND){
             	mView.setBackgroundResource(com.android.internal.R.drawable.item_back_ground_three);
             }
     //he.liu end at 2012.5.02
             return mView;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position < 0 || position >= getCount()) return true;
        return this.getItem(position).isSelectable();
    }

    @Override
    public boolean areAllItemsEnabled() {
        // There should always be a preference group, and these groups are always
        // disabled
        return false;
    }

    public void onPreferenceChange(Preference preference) {
        notifyDataSetChanged();
    }

    public void onPreferenceHierarchyChange(Preference preference) {
        mHandler.removeCallbacks(mSyncRunnable);
        mHandler.post(mSyncRunnable);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        if (!mHasReturnedViewTypeCount) {
            mHasReturnedViewTypeCount = true;
        }
        
        final Preference preference = this.getItem(position);
        if (preference.hasSpecifiedLayout()) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

        mTempPreferenceLayout = createPreferenceLayout(preference, mTempPreferenceLayout);

        int viewType = Collections.binarySearch(mPreferenceLayouts, mTempPreferenceLayout);
        if (viewType < 0) {
            // This is a class that was seen after we returned the count, so
            // don't recycle it.
            return IGNORE_ITEM_VIEW_TYPE;
        } else {
            return viewType;
        }
    }

    @Override
    public int getViewTypeCount() {
        if (!mHasReturnedViewTypeCount) {
            mHasReturnedViewTypeCount = true;
        }
        
        return Math.max(1, mPreferenceLayouts.size()+additionalTypeCount);
    }

    private int[] calcItemsBetweenCategory(PreferenceGroup group){
        final int groupSize = group.getPreferenceCount();
        
        int[] indexOfPreference = new int[groupSize];
        for (int i = 0; i < groupSize; i++) {
        	final Preference preference = group.getPreference(i);
        	 if (preference instanceof PreferenceCategory) {
                	 indexOfPreference[i]  = 0 ;
             }else if(i == 0){
            	 indexOfPreference[i]  = 1 ;
             }else {
            	 indexOfPreference[i] = indexOfPreference[i - 1] + 1;
             }
        }
        return indexOfPreference;
    }
}
