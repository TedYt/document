
package com.android.contacts.common.list;

import android.app.Activity;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;

import com.android.contacts.common.R;

import java.util.ArrayList;

class MultiSelectPhoneAdapter extends MultiSelectAdapter {

    private static final String TAG = "MultiSelectPhoneAdapter";
    private Activity mActivity;
    private LayoutInflater inflater;
    private ContactPhotoManager mPhotoLoader;

    protected static class PhoneQuery {
        static final String[] PROJECTION_PRIMARY = new String[] {
                Phone._ID, // 0
                Phone.TYPE, // 1
                Phone.LABEL, // 2
                Phone.NUMBER, // 3
                Phone.CONTACT_ID, // 4
                Phone.LOOKUP_KEY, // 5
                Phone.PHOTO_ID, // 6
                Phone.DISPLAY_NAME_PRIMARY, // 7
        };

        private static final String[] PROJECTION_ALTERNATIVE = new String[] {
                Phone._ID, // 0
                Phone.TYPE, // 1
                Phone.LABEL, // 2
                Phone.NUMBER, // 3
                Phone.CONTACT_ID, // 4
                Phone.LOOKUP_KEY, // 5
                Phone.PHOTO_ID, // 6
                Phone.DISPLAY_NAME_ALTERNATIVE, // 7
        };

        public static final int PHONE_ID = 0;
        public static final int PHONE_TYPE = 1;
        public static final int PHONE_LABEL = 2;
        public static final int PHONE_NUMBER = 3;
        public static final int PHONE_CONTACT_ID = 4;
        public static final int PHONE_LOOKUP_KEY = 5;
        public static final int PHONE_PHOTO_ID = 6;
        public static final int PHONE_DISPLAY_NAME = 7;
    }

    public MultiSelectPhoneAdapter(Activity activity) {
        super();
        mActivity = activity;
        inflater = activity.getLayoutInflater();
        mPhotoLoader = ContactPhotoManager.getInstance(activity);
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(MultiSelectPhoneAdapter.PhoneQuery.PHONE_ID);
    }
    
     @Override
    public long getItemContactId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(MultiSelectPhoneAdapter.PhoneQuery.PHONE_CONTACT_ID);
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.multi_select_item, parent, false);
        } else {
            view = convertView;
        }
        mCursor.moveToPosition(position);

        long id = mCursor.getLong(MultiSelectPhoneAdapter.PhoneQuery.PHONE_ID);
        String display_name = mCursor
                .getString(MultiSelectPhoneAdapter.PhoneQuery.PHONE_DISPLAY_NAME);
        String phone = mCursor.getString(MultiSelectPhoneAdapter.PhoneQuery.PHONE_NUMBER);
        CharSequence label = getLabel();
        ((TextView) view.findViewById(R.id.contact_name)).setText(display_name);
        ((TextView) view.findViewById(R.id.phone_info)).setText(label + "\t" + phone);
        ImageView photoView = (ImageView) view.findViewById(R.id.photo);
        mPhotoLoader.loadThumbnail(photoView,
                mCursor.getLong(MultiSelectPhoneAdapter.PhoneQuery.PHONE_PHOTO_ID), false);

        updateSectionHeader((TextView) view.findViewById(R.id.header), position);
        return view;
    }

    /**
     * Get proper label of phone
     * @return standard type or custom type label
     */
    private CharSequence getLabel() {
        int type = mCursor.getInt(MultiSelectPhoneAdapter.PhoneQuery.PHONE_TYPE);
        return Phone.getTypeLabel(mActivity.getResources(), type, 
                mCursor.getString(MultiSelectPhoneAdapter.PhoneQuery.PHONE_LABEL));
    }

    @Override
    CursorLoader configCursorLoader() {
        // TODO Search list ?
        // Builder builder = Phone.CONTENT_FILTER_URI.buildUpon();
        // builder.appendPath("");
        Builder builder = Phone.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true");
        builder.appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES, "true");
        Log.d(TAG, "loadPhoneCursor()  builder.build() = " + builder.build());
        CursorLoader loader = new CursorLoader(mActivity, builder.build(),
                MultiSelectPhoneAdapter.PhoneQuery.PROJECTION_PRIMARY, null, null, null);
        loader.setSortOrder(Phone.SORT_KEY_PRIMARY);
        return loader;
    }

    @Override
    public long[] getCheckedItemKeys(ListView listView) {
        Log.d(TAG, "getCheckedItemKeys()");
        return listView.getCheckedItemIds();
    }

   @Override
    public String getCheckedItemLookUpKeys(ListView listView){
      return null;
   }

}
