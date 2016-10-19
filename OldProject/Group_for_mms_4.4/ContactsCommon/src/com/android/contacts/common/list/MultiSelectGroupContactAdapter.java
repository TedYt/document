
package com.android.contacts.common.list;

import android.app.Activity;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.SearchSnippetColumns;
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
import android.net.Uri;
import android.content.Intent;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
class MultiSelectGroupContactAdapter extends MultiSelectAdapter {

    private static final String TAG = "MultiSelectContactAdapter";
    private Activity mActivity;
    private LayoutInflater inflater;
    private ContactPhotoManager mPhotoLoader;
    private String mAccountName;
    private String mAccountType;
    private String dataSet;
    private Intent intent;

    protected static class ContactQuery {
        static final String[] CONTACT_PROJECTION_PRIMARY = new String[] {
                Contacts._ID, // 0
                Contacts.DISPLAY_NAME_PRIMARY, // 1
                Contacts.CONTACT_PRESENCE, // 2
                Contacts.CONTACT_STATUS, // 3
                Contacts.PHOTO_ID, // 4
                Contacts.PHOTO_THUMBNAIL_URI, // 5
                Contacts.LOOKUP_KEY, // 6
                Contacts.IS_USER_PROFILE, // 7
        };

        private static final String[] CONTACT_PROJECTION_ALTERNATIVE = new String[] {
                Contacts._ID, // 0
                Contacts.DISPLAY_NAME_ALTERNATIVE, // 1
                Contacts.CONTACT_PRESENCE, // 2
                Contacts.CONTACT_STATUS, // 3
                Contacts.PHOTO_ID, // 4
                Contacts.PHOTO_THUMBNAIL_URI, // 5
                Contacts.LOOKUP_KEY, // 6
                Contacts.IS_USER_PROFILE, // 7
        };

        private static final String[] FILTER_PROJECTION_PRIMARY = new String[] {
                Contacts._ID, // 0
                Contacts.DISPLAY_NAME_PRIMARY, // 1
                Contacts.CONTACT_PRESENCE, // 2
                Contacts.CONTACT_STATUS, // 3
                Contacts.PHOTO_ID, // 4
                Contacts.PHOTO_THUMBNAIL_URI, // 5
                Contacts.LOOKUP_KEY, // 6
                Contacts.IS_USER_PROFILE, // 7
                SearchSnippetColumns.SNIPPET, // 8
        };

        private static final String[] FILTER_PROJECTION_ALTERNATIVE = new String[] {
                Contacts._ID, // 0
                Contacts.DISPLAY_NAME_ALTERNATIVE, // 1
                Contacts.CONTACT_PRESENCE, // 2
                Contacts.CONTACT_STATUS, // 3
                Contacts.PHOTO_ID, // 4
                Contacts.PHOTO_THUMBNAIL_URI, // 5
                Contacts.LOOKUP_KEY, // 6
                Contacts.IS_USER_PROFILE, // 7
                SearchSnippetColumns.SNIPPET, // 8
        };

        public static final int CONTACT_ID = 0;
        public static final int CONTACT_DISPLAY_NAME = 1;
        public static final int CONTACT_PRESENCE_STATUS = 2;
        public static final int CONTACT_CONTACT_STATUS = 3;
        public static final int CONTACT_PHOTO_ID = 4;
        public static final int CONTACT_PHOTO_URI = 5;
        public static final int CONTACT_LOOKUP_KEY = 6;
        public static final int CONTACT_IS_USER_PROFILE = 7;
        public static final int CONTACT_SNIPPET = 8;
    }

    public MultiSelectGroupContactAdapter(Activity activity,Intent intent ) {
        super();
        mActivity = activity;
        inflater = activity.getLayoutInflater();
        mPhotoLoader = ContactPhotoManager.getInstance(activity);
        mAccountName=intent.getStringExtra("account_name");
        mAccountType=intent.getStringExtra("account_type");
        dataSet=intent.getStringExtra("mDataSet");
       android.util.Log.d("wwf","mAccountName:"+mAccountName+",mAccountType:"+mAccountType+",dataSet:"+dataSet);
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ContactQuery.CONTACT_ID);
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
    public long getItemContactId(int position) {
        
        return 0;
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
        long id = mCursor.getLong(ContactQuery.CONTACT_ID);
        String display_name = mCursor.getString(ContactQuery.CONTACT_DISPLAY_NAME);
        ((TextView) view.findViewById(R.id.contact_name)).setText(display_name);
        ((TextView) view.findViewById(R.id.phone_info)).setVisibility(View.GONE);
        ImageView photoView = (ImageView) view.findViewById(R.id.photo);
        mPhotoLoader
                .loadThumbnail(photoView, mCursor.getLong(ContactQuery.CONTACT_PHOTO_ID), false);

        updateSectionHeader((TextView) view.findViewById(R.id.header), position);
        return view;
    }

    @Override
    CursorLoader configCursorLoader() {
        Builder builder = Contacts.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true");
        // builder.appendQueryParameter(ContactsContract.REMOVE_DUPLICATE_ENTRIES,
        // "true");
        builder=addAccountQueryParameterToUrl(builder);
        Log.d(TAG, "loadContactsCursor()  builder.build() = " + builder.build());
        CursorLoader loader = new CursorLoader(mActivity, builder.build(),
                MultiSelectGroupContactAdapter.ContactQuery.CONTACT_PROJECTION_PRIMARY, null, null, null);
        loader.setSortOrder(Contacts.SORT_KEY_PRIMARY);
        return loader;
    }
    public Uri.Builder addAccountQueryParameterToUrl(Uri.Builder uriBuilder) {
       // if (filterType != FILTER_TYPE_ACCOUNT) {
        //    throw new IllegalStateException("filterType must be FILTER_TYPE_ACCOUNT");
       // }
        uriBuilder.appendQueryParameter(RawContacts.ACCOUNT_NAME, mAccountName);
        uriBuilder.appendQueryParameter(RawContacts.ACCOUNT_TYPE, mAccountType);
        if (!TextUtils.isEmpty(dataSet)) {
            uriBuilder.appendQueryParameter(RawContacts.DATA_SET, dataSet);
        }
        return uriBuilder;
    }
    @Override
    public long[] getCheckedItemKeys(ListView listView) {
        Log.d(TAG, "getCheckedItemKeys()");
        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
        int count = checkedItems.size();
        long[] itemKeys = new long[count];
        for (int n = 0; n < count; n++) {
            mCursor.moveToPosition(checkedItems.keyAt(n));
            long key = mCursor.getLong(ContactQuery.CONTACT_ID);
            Log.d(TAG, "key = " + key);
            itemKeys[n] = key;
        }
        return itemKeys;
    }
   @Override
    public String getCheckedItemLookUpKeys(ListView listView){
	   StringBuilder uriListBuilder = new StringBuilder();
	   SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
	   int count = checkedItems.size();
       long[] itemKeys = new long[count];
       for (int n = 0; n < count; n++) {
    	   Log.d(TAG, "n = " + checkedItems.keyAt(n)+",count:"+count);
           mCursor.moveToPosition(checkedItems.keyAt(n));
           uriListBuilder.append(':');
           uriListBuilder.append(mCursor.getString(ContactQuery.CONTACT_LOOKUP_KEY));
           //long lookUpkey = mCursor.getLong(ContactQuery.CONTACT_ID);
           Log.d(TAG, "uriListBuild:" + uriListBuilder+",");
           //itemKeys[n] = key;
       }
       return uriListBuilder.toString();
   }

}
