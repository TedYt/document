
package com.android.contacts.common.list;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import android.provider.ContactsContract.ContactCounts;

import java.util.ArrayList;

public abstract class MultiSelectAdapter extends BaseAdapter implements SectionIndexer {

    private static final String TAG = "MultiSelectAdapter";
    ContactsSectionIndexer mIndexer;
    Cursor mCursor;
    String[] mSections;
    int mType; // adapter type

    /** Intent definitions **/
    public static final String INT_TYPE = "type";
    public static final String RESULT = "result";
    public static final int TYPE_CONTACT = 1;
    public static final int TYPE_PHONE = 2;
    public static final int TYPE_ACCOUNT_CONTACT = 3;
    public static final String INTENT = "com.android.contacts.MULTI_SELECT";

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mIndexer == null) {
            return -1;
        }
        return mIndexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        if (mIndexer == null) {
            return -1;
        }
        return mIndexer.getSectionForPosition(position);
    }

    @Override
    public Object[] getSections() {
        if (mIndexer == null) {
            return new String[] {
                    " "
            };
        } else {
            return mIndexer.getSections();
        }
    }

    // TODO android SectionIndexer can not be updated bug
    void updateIndexer(Cursor cursor) {
        Log.d(TAG, "updateIndexer()");
        if (cursor == null) {
            mIndexer = null;
            return;
        }

        Bundle bundle = cursor.getExtras();
        if (bundle.containsKey(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
            String sections[] =
                    bundle.getStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
            int counts[] = bundle.getIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
            for (String section : sections) {
                Log.d(TAG, "section = " + section);
            }
            for (int count : counts) {
                Log.d(TAG, "count = " + count);
            }
            mIndexer = new ContactsSectionIndexer(sections, counts);
            mSections = (String[]) mIndexer.getSections();
        } else {
            mIndexer = null;
        }
    }

    /*
     * Show header on every section`s first item.
     */
    void updateSectionHeader(TextView v, final int position) {
        int sectionId = mIndexer.getSectionForPosition(position);
        // first item or item`s index diff from it`s predecessor
        if (position == 0
                || mIndexer.getSectionForPosition(position) != mIndexer
                        .getSectionForPosition(position - 1)) {
            v.setVisibility(View.VISIBLE);
            v.setText(mSections[sectionId]);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    void setCursor(Cursor c) {
        mCursor = c;
        updateIndexer(c);
    }

    Cursor getCursor() {
        return mCursor;
    }

    abstract CursorLoader configCursorLoader();

    @Override
    public boolean hasStableIds() {
        // To get item ids
        return true;
    }

    /**
     * Get id of selected items
     * 
     * @param listView contains selection info
     * @return list of proper id
     */
    abstract public long[] getCheckedItemKeys(ListView listView);
    abstract public String getCheckedItemLookUpKeys(ListView listView);
    abstract public long getItemContactId(int position);
    
}
