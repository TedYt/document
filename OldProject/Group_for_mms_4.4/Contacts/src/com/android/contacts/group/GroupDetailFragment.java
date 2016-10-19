/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.contacts.group;

import android.app.Activity;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GroupMemberLoader;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.interactions.GroupDeletionDialogFragment;
import com.android.contacts.common.list.ContactTileAdapter;
import com.android.contacts.common.list.ContactTileView;
import com.android.contacts.list.GroupMemberTileAdapter;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;

// The following lines are provided and maintained by Mediatek Inc.
import android.widget.Toast;
import android.widget.ProgressBar;
import android.view.animation.AnimationUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.content.IntentFilter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.app.ProgressDialog;
import com.android.contacts.common.util.WeakAsyncTask;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.IccCardConstants;
import android.accounts.Account;

import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.common.ContactsUtils;
import com.android.contacts.common.util.Constants;
import com.android.contacts.editor.AggregationSuggestionEngine.RawContact;
import com.android.contacts.util.PhoneCapabilityTester;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
/**
 * Displays the details of a group and shows a list of actions possible for the group.
 */
public class GroupDetailFragment extends Fragment implements OnScrollListener {

    public static interface Listener {
        /**
         * The group title has been loaded
         */
        public void onGroupTitleUpdated(String title);

        /**
         * The number of group members has been determined
         */
        public void onGroupSizeUpdated(String size);

        /**
         * The account type and dataset have been determined.
         */
        public void onAccountTypeUpdated(String accountTypeString, String dataSet);

        /**
         * User decided to go to Edit-Mode
         */
        public void onEditRequested(Uri groupUri);

        /**
         * Contact is selected and should launch details page
         */
        public void onContactSelected(Uri contactUri);
    }

    private static final String TAG = "GroupDetailFragment";

    private static final int LOADER_METADATA = 0;
    private static final int LOADER_MEMBERS = 1;

    private Context mContext;

    private View mRootView;
    private ViewGroup mGroupSourceViewContainer;
    private View mGroupSourceView;
    private TextView mGroupTitle;
    private TextView mGroupSize;
    private ListView mMemberListView;
    private View mEmptyView;

    private Listener mListener;

    private ContactTileAdapter mAdapter;
    private ContactPhotoManager mPhotoManager;
    private AccountTypeManager mAccountTypeManager;

    private Uri mGroupUri;
    private long mGroupId;
    private String mGroupName;
    private String mAccountTypeString;
    private String mDataSet;
    private boolean mIsReadOnly;
    private boolean mIsMembershipEditable;

    private boolean mShowGroupActionInActionBar;
    private boolean mOptionsMenuGroupDeletable;
    private boolean mOptionsMenuGroupEditable;
    private boolean mCloseActivityAfterDelete;

    public GroupDetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mAccountTypeManager = AccountTypeManager.getInstance(mContext);

        Resources res = getResources();
        int columnCount = res.getInteger(R.integer.contact_tile_column_count);

        mAdapter = new GroupMemberTileAdapter(activity, mContactTileListener, columnCount);

        configurePhotoLoader();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        setHasOptionsMenu(true);
        mRootView = inflater.inflate(R.layout.group_detail_fragment, container, false);
        mGroupTitle = (TextView) mRootView.findViewById(R.id.group_title);
        mGroupSize = (TextView) mRootView.findViewById(R.id.group_size);
        mGroupSourceViewContainer = (ViewGroup) mRootView.findViewById(
                R.id.group_source_view_container);
        mEmptyView = mRootView.findViewById(android.R.id.empty);
        mMemberListView = (ListView) mRootView.findViewById(android.R.id.list);
        mMemberListView.setItemsCanFocus(true);
        mMemberListView.setAdapter(mAdapter);

        return mRootView;
    }

    public void loadGroup(Uri groupUri) {
        mGroupUri= groupUri;
        startGroupMetadataLoader();
    }

    public void setQuickContact(boolean enableQuickContact) {
        mAdapter.enableQuickContact(enableQuickContact);
    }

    private void configurePhotoLoader() {
        if (mContext != null) {
            if (mPhotoManager == null) {
                mPhotoManager = ContactPhotoManager.getInstance(mContext);
            }
            if (mMemberListView != null) {
                mMemberListView.setOnScrollListener(this);
            }
            if (mAdapter != null) {
                mAdapter.setPhotoLoader(mPhotoManager);
            }
        }
    }

    public void setListener(Listener value) {
        mListener = value;
    }

    public void setShowGroupSourceInActionBar(boolean show) {
        mShowGroupActionInActionBar = show;
    }

    public Uri getGroupUri() {
        return mGroupUri;
    }

    /**
     * Start the loader to retrieve the metadata for this group.
     */
    private void startGroupMetadataLoader() {
        getLoaderManager().restartLoader(LOADER_METADATA, null, mGroupMetadataLoaderListener);
    }

    /**
     * Start the loader to retrieve the list of group members.
     */
    private void startGroupMembersLoader() {
        getLoaderManager().restartLoader(LOADER_MEMBERS, null, mGroupMemberListLoaderListener);
    }

    private final ContactTileView.Listener mContactTileListener =
            new ContactTileView.Listener() {

        @Override
        public void onContactSelected(Uri contactUri, Rect targetRect) {
            mListener.onContactSelected(contactUri);
        }

        @Override
        public void onCallNumberDirectly(String phoneNumber) {
            // No need to call phone number directly from People app.
            Log.w(TAG, "unexpected invocation of onCallNumberDirectly()");
        }

        @Override
        public int getApproximateTileWidth() {
            return getView().getWidth() / mAdapter.getColumnCount();
        }
    };

    /**
     * The listener for the group metadata loader.
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupMetadataLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return new GroupMetaDataLoader(mContext, mGroupUri);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            data.moveToPosition(-1);
            if (data.moveToNext()) {
                boolean deleted = data.getInt(GroupMetaDataLoader.DELETED) == 1;
                if (!deleted) {
                    bindGroupMetaData(data);

                    // Retrieve the list of members
                    startGroupMembersLoader();
                    return;
                }
            }
            updateSize(-1);
            updateTitle(null);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };

    /**
     * The listener for the group members list loader
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupMemberListLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return GroupMemberLoader.constructLoaderForGroupDetailQuery(mContext, mGroupId);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            updateSize(data.getCount());
            mAdapter.setContactCursor(data);
            mMemberListView.setEmptyView(mEmptyView);
	    groupMemberSize = data.getCount();
Log.i(TAG,"---groupMemberSize--- = "+groupMemberSize);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };

    private void bindGroupMetaData(Cursor cursor) {
        cursor.moveToPosition(-1);
        if (cursor.moveToNext()) {
            mAccountTypeString = cursor.getString(GroupMetaDataLoader.ACCOUNT_TYPE);
            mDataSet = cursor.getString(GroupMetaDataLoader.DATA_SET);
            mGroupId = cursor.getLong(GroupMetaDataLoader.GROUP_ID);
            mGroupName = cursor.getString(GroupMetaDataLoader.TITLE);
            mIsReadOnly = cursor.getInt(GroupMetaDataLoader.IS_READ_ONLY) == 1;
            updateTitle(mGroupName);
            // Must call invalidate so that the option menu will get updated
            getActivity().invalidateOptionsMenu ();

            final String accountTypeString = cursor.getString(GroupMetaDataLoader.ACCOUNT_TYPE);
            final String dataSet = cursor.getString(GroupMetaDataLoader.DATA_SET);
            updateAccountType(accountTypeString, dataSet);
        }
    }

    private void updateTitle(String title) {
        if (mGroupTitle != null) {
            mGroupTitle.setText(title);
        } else {
            mListener.onGroupTitleUpdated(title);
        }
    }

    /**
     * Display the count of the number of group members.
     * @param size of the group (can be -1 if no size could be determined)
     */
    private void updateSize(int size) {
        String groupSizeString;
        if (size == -1) {
            groupSizeString = null;
        } else {
            String groupSizeTemplateString = getResources().getQuantityString(
                    R.plurals.num_contacts_in_group, size);
            AccountType accountType = mAccountTypeManager.getAccountType(mAccountTypeString,
                    mDataSet);
            groupSizeString = String.format(groupSizeTemplateString, size,
                    accountType.getDisplayLabel(mContext));
        }

        if (mGroupSize != null) {
            mGroupSize.setText(groupSizeString);
        } else {
            mListener.onGroupSizeUpdated(groupSizeString);
        }
    }

    /**
     * Once the account type, group source action, and group source URI have been determined
     * (based on the result from the {@link Loader}), then we can display this to the user in 1 of
     * 2 ways depending on screen size and orientation: either as a button in the action bar or as
     * a button in a static header on the page.
     * We also use isGroupMembershipEditable() of accountType to determine whether or not we should
     * display the Edit option in the Actionbar.
     */
    private void updateAccountType(final String accountTypeString, final String dataSet) {
        final AccountTypeManager manager = AccountTypeManager.getInstance(getActivity());
        final AccountType accountType =
                manager.getAccountType(accountTypeString, dataSet);

        mIsMembershipEditable = accountType.isGroupMembershipEditable();

        // If the group action should be shown in the action bar, then pass the data to the
        // listener who will take care of setting up the view and click listener. There is nothing
        // else to be done by this {@link Fragment}.
        if (mShowGroupActionInActionBar) {
            mListener.onAccountTypeUpdated(accountTypeString, dataSet);
            return;
        }

        // Otherwise, if the {@link Fragment} needs to create and setup the button, then first
        // verify that there is a valid action.
        if (!TextUtils.isEmpty(accountType.getViewGroupActivity())) {
            if (mGroupSourceView == null) {
                mGroupSourceView = GroupDetailDisplayUtils.getNewGroupSourceView(mContext);
                // Figure out how to add the view to the fragment.
                // If there is a static header with a container for the group source view, insert
                // the view there.
                if (mGroupSourceViewContainer != null) {
                    mGroupSourceViewContainer.addView(mGroupSourceView);
                }
            }

            // Rebind the data since this action can change if the loader returns updated data
            mGroupSourceView.setVisibility(View.VISIBLE);
            GroupDetailDisplayUtils.bindGroupSourceView(mContext, mGroupSourceView,
                    accountTypeString, dataSet);
            mGroupSourceView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI, mGroupId);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setClassName(accountType.syncAdapterPackageName,
                            accountType.getViewGroupActivity());
                    startActivity(intent);
                }
            });
        } else if (mGroupSourceView != null) {
            mGroupSourceView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            mPhotoManager.pause();
        } else {
            mPhotoManager.resume();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.view_group, menu);
    }

    public boolean isOptionsMenuChanged() {
        return mOptionsMenuGroupDeletable != isGroupDeletable() &&
                mOptionsMenuGroupEditable != isGroupEditableAndPresent();
    }

    public boolean isGroupDeletable() {
        return mGroupUri != null && !mIsReadOnly;
    }

    public boolean isGroupEditableAndPresent() {
        return mGroupUri != null && mIsMembershipEditable;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mOptionsMenuGroupDeletable = isGroupDeletable() && isVisible();
        mOptionsMenuGroupEditable = isGroupEditableAndPresent() && isVisible();

        final MenuItem editMenu = menu.findItem(R.id.menu_edit_group);
        editMenu.setVisible(mOptionsMenuGroupEditable);

        final MenuItem deleteMenu = menu.findItem(R.id.menu_delete_group);
        deleteMenu.setVisible(mOptionsMenuGroupDeletable);
        final MenuItem sendMsgMenu = menu.findItem(R.id.menu_message_group);
        final MenuItem sendEmailMenu = menu.findItem(R.id.menu_email_group);

	Log.i(TAG,"    groupMemberSize  = "+groupMemberSize);

            sendMsgMenu.setVisible(true);
            sendEmailMenu.setVisible(true);
       
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_group: {
                if (mListener != null) mListener.onEditRequested(mGroupUri);
                break;
            }
            case R.id.menu_delete_group: {
		if(groupMemberSize > 0){
                GroupDeletionDialogFragment.show(getFragmentManager(), mGroupId, mGroupName,
                        mCloseActivityAfterDelete);
		}
                return true;
	    }
            case R.id.menu_message_group: {
                new SendGroupSmsTask(this.getActivity()).execute(mGroupName);
                break;
            }
            case R.id.menu_email_group: {
                new SendGroupEmailTask(this.getActivity()).execute(mGroupName);
                break;
            }
	}
       
        return false;
    }

    public void closeActivityAfterDelete(boolean closeActivity) {
        mCloseActivityAfterDelete = closeActivity;
    }

    public long getGroupId() {
        return mGroupId;
    }
    // The following lines are provided and maintained by Mediatek Inc.
    private static final boolean DEBUG = true;
    private String mCategoryId = null;
    private int mSlotId = -1;
    private int mSimId = -1;
    private String mSimName;
    private String mAccountName;
    private int groupMemberSize = -1;
    private boolean DISABLE_MOVE_MENU = false;
    String ACTION_PHB_STATE_CHANGED = "android.intent.action.PHB_STATE_CHANGED";
    public void loadExtras(String CategoryId, int slotId, int simIndicator, String simName) {
        mCategoryId = CategoryId;
        mSlotId = slotId;
        mSimId = simIndicator;
        mSimName = simName;
        registerSimReceiver();
    }
    
    public void loadExtras(int slotId) {
        mSlotId = slotId;
        registerSimReceiver();
    }
    
    private class SendGroupSmsTask extends
            WeakAsyncTask<String, Void, String, Activity> {
        private WeakReference<ProgressDialog> mProgress;

        public SendGroupSmsTask(Activity target) {
            super(target);
        }

        @Override
        protected void onPreExecute(Activity target) {
            mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(
                    target, null, 
                    target.getText(R.string.please_wait), true));
        }

        @Override
        protected String doInBackground(final Activity target, String... group) {
            return getSmsAddressFromGroup(target.getBaseContext(), getGroupId());
        }

        @Override
        protected void onPostExecute(final Activity target, String address) {
            ProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing() 
                    && getActivity() != null && !getActivity().isFinishing()) {
                progress.dismiss();
            }
            if (address == null || address.length() == 0) {
                Toast.makeText(target, R.string.no_valid_number_in_group,
                        Toast.LENGTH_SHORT).show();
            } else {
                String[] list = address.split(";");
                if (list.length > 1) {
                    Toast.makeText(target, list[1], Toast.LENGTH_SHORT).show();
                }
                address = list[0];
                if (address == null || address.length() == 0) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.fromParts(CallUtil.SCHEME_SMSTO, address,
                        null));
                try {
                    target.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(target,
                            getString(R.string.quickcontact_missing_app),
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "ActivityNotFoundException for secondaryIntent");
                }
            }
        }

        public String getSmsAddressFromGroup(Context context, long groupId) {
            Log.d(TAG, "groupId:" + groupId);
            StringBuilder builder = new StringBuilder();
            ContentResolver resolver = context.getContentResolver();

            HashSet<Long> allContacts = new HashSet<Long>();
            StringBuilder where = getWhere(resolver, groupId, allContacts);
            if (null == where) {
                return "";
            }
            where.append(Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'");
            Log.i(TAG, "getSmsAddressFromGroup where " + where);
            Cursor cursor = resolver.query(Data.CONTENT_URI, 
                    new String[] {Data.DATA1, Phone.TYPE, Data.CONTACT_ID,Data.IS_PRIMARY},
                    where.toString(), null, Data.CONTACT_ID + " ASC, " + Data._ID + " ASC ");
            if (cursor != null) {
                long candidateContactId = -1;
                int candidateType = -1;
                String candidateAddress = "";
                // The following lines are provided and maintained by Mediatek Inc.
                int isDefault = 0;
                // The previous lines are provided and maintained by Mediatek Inc.
                while (cursor.moveToNext()) {
                    Long id = cursor.getLong(2);
                    if (allContacts.contains(id)) {
                        allContacts.remove(id);
                    }
                    int type = cursor.getInt(1);
                    String number = cursor.getString(0);
                    // The following lines are provided and maintained by Mediatek Inc.
                    isDefault = cursor.getInt(3);
                    // The previous lines are provided and maintained by Mediatek Inc.
                    int numIndex = number.indexOf(",");
                    int tempIndex = -1;
                    if ((tempIndex = number.indexOf(";")) >= 0) {
                        if (numIndex < 0) {
                            numIndex = tempIndex;
                        } else {
                            numIndex = numIndex < tempIndex ? numIndex
                                    : tempIndex;
                        }
                    }
                    if (numIndex == 0) {
                        continue;
                    } else if (numIndex > 0) {
                        number = number.substring(0, numIndex);
                    }

                    if (candidateContactId == -1) {
                        candidateContactId = id;
                        candidateType = type;
                        candidateAddress = number;
                    } else {
                        if (candidateContactId != id) {
                            if (candidateAddress != null
                                    && candidateAddress.length() > 0) {
                                if (builder.length() > 0) {
                                    builder.append(",");
                                }
                                builder.append(candidateAddress);
                            }
                            candidateContactId = id;
                            candidateType = type;
                            candidateAddress = number;
                            // The following lines are provided and maintained by Mediatek Inc.
                        } else if (isDefault == 1) {
                            candidateContactId = id;
                            candidateType = type;
                            candidateAddress = number;
                            // The previous lines are provided and maintained by Mediatek Inc.
                        } else {
                            if (candidateType != Phone.TYPE_MOBILE
                                    && type == Phone.TYPE_MOBILE) {
                                candidateContactId = id;
                                candidateType = type;
                                candidateAddress = number;
                            }
                        }
                    }
                    if (cursor.isLast()) {
                        if (candidateAddress != null
                                && candidateAddress.length() > 0) {
                            if (builder.length() > 0) {
                                builder.append(",");
                            }
                            builder.append(candidateAddress);
                        }
                    }

                }
                cursor.close();
            }
            Log.i(TAG, "[getSmsAddressFromGroup]address:" + builder);

            return showNoTelphoneOrEmailToast(context, builder, resolver,
                    allContacts, "sms");
        }
    }

    /**
     *  M: to show hints those contacts with no valid tel or email adds.
     * @param context
     * @param builder
     * @param resolver
     * @param allContacts
     * @param emailOrSms
     * @return  The hints Msg String.
     */
    private String showNoTelphoneOrEmailToast(Context context,
            StringBuilder builder, ContentResolver resolver,
            HashSet<Long> allContacts, String emailOrSms) {
        StringBuilder ids;
        StringBuilder where;
        ids = new StringBuilder();
        where = new StringBuilder();
        List<String> noNumberContactList = new ArrayList<String>();
        if (allContacts.size() > 0) {
            Long[] allContactsArray = allContacts.toArray(new Long[0]);
            for (Long id : allContactsArray) {
                if (ids.length() > 0) {
                    ids.append(",");
                }
                ids.append(id.toString());
            }
        }

        /** M: to fix ALPS00962454, Send group sms/email hints. */
        if (ids.length() > 0) {
            ids = getAllRawContactIds(resolver, ids);
            Log.d(TAG, "[getSmsAddressFromGroup]the best available rawcontactsids:" + ids.toString());
        } else {
            Log.d(TAG, "[getSmsAddressFromGroup] without any empty properties contacts, return.");
            return builder.toString();
        }

        if (ids.length() > 0) {
            where.append(RawContacts._ID + " IN(");
            where.append(ids.toString());
            where.append(")");
        } else {
            return builder.toString();
        }
        where.append(" AND ");
        where.append(RawContacts.DELETED + "= 0");
        Log.i(TAG, "[getSmsAddressFromGroup]query no name cursor selection:" + where.toString());

        Cursor cursor = resolver.query(RawContacts.CONTENT_URI,
                new String[] { RawContacts.DISPLAY_NAME_PRIMARY }, where.toString(), null,
                Data.CONTACT_ID + " ASC ");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                noNumberContactList.add(cursor.getString(0));
            }
            cursor.close();
        }
        String str = "";
        if (noNumberContactList.size() == 1) {
            str = context.getString(
                            emailOrSms.equals("sms") ? R.string.send_groupsms_no_number_1
                                    : R.string.send_groupemail_no_number_1,
                            noNumberContactList.get(0));
        } else if (noNumberContactList.size() == 2) {
            str = context.getString(
                            emailOrSms.equals("sms") ? R.string.send_groupsms_no_number_2
                                    : R.string.send_groupemail_no_number_2,
                            noNumberContactList.get(0), noNumberContactList
                                    .get(1));
        } else if (noNumberContactList.size() > 2) {
            str = context.getString(
                            emailOrSms.equals("sms") ? R.string.send_groupsms_no_number_more
                                    : R.string.send_groupemail_no_number_more,
                            noNumberContactList.get(0), String
                                    .valueOf(noNumberContactList.size() - 1));
        }
        String result = builder.toString();
        Log.i(TAG, "[getSmsAddressFromGroup]result:" + result);
        if (str != null && str.length() > 0) {
            return result + ";" + str;
        } else {
            return result;
        }
    }

    private class SendGroupEmailTask extends
            WeakAsyncTask<String, Void, String, Activity> {
        private WeakReference<ProgressDialog> mProgress;

        public SendGroupEmailTask(Activity target) {
            super(target);
        }

        @Override
        protected void onPreExecute(Activity target) {
            mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(
                    target, null, target.getText(R.string.please_wait)));
        }

        @Override
        protected String doInBackground(final Activity target, String... group) {
            return getEmailAddressFromGroup(target, getGroupId());
        }

        @Override
        protected void onPostExecute(final Activity target, String address) {
            ProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing()
                    && getActivity() != null && !getActivity().isFinishing()) {
                progress.dismiss();
            }
            try {
                // Intent intent = new Intent(Intent.ACTION_SENDTO,
                // Uri.fromParts(Constants.SCHEME_MAILTO, address, null));
                // String[] addrList = address.split(",");
                //        
                // Intent intent = new Intent(Intent.ACTION_SEND);
                // intent.setType("*/*");
                // intent.putExtra(Intent.EXTRA_EMAIL, addrList);
                Uri dataUri = null;

                if (address == null || address.length() == 0) {
                    Toast.makeText(target, R.string.no_valid_email_in_group,
                            Toast.LENGTH_SHORT).show();
                } else {
                    String[] list = address.split(";");
                    if (list.length > 1) {
                        Toast.makeText(target, list[1], Toast.LENGTH_SHORT)
                                .show();
                    }
                    address = list[0];
                    if (address == null || address.length() == 0) {
                        return;
                    }
                    dataUri = Uri.parse("mailto:" + address);
                    Intent intent = new Intent(Intent.ACTION_SENDTO, dataUri);
                    target.startActivity(intent);
                }
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No activity found for Eamil");
                Toast
                        .makeText(target, R.string.email_error,
                                Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "SendGroupEmail error", e);
            }
        }

        public String getEmailAddressFromGroup(Context context, long groupId) {
            Log.d(TAG, "groupId:" + groupId);
            StringBuilder builder = new StringBuilder();
            ContentResolver resolver = context.getContentResolver();

            HashSet<Long> allContacts = new HashSet<Long>();
            StringBuilder where = getWhere(resolver, groupId, allContacts);
            if (null == where) {
                return "";
            }
            where.append(Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'");
            Log.i(TAG, "[getEmailAddressFromGroup]where " + where);
            Cursor cursor = resolver.query(Data.CONTENT_URI, 
            // The following lines are provided and maintained by Mediatek Inc.
                    new String[] {Data.DATA1, Phone.TYPE, Data.CONTACT_ID,Data.IS_PRIMARY},
                    // The previous lines are provided and maintained by Mediatek Inc.
                    where.toString(), null, Data.CONTACT_ID + " ASC ");
            if (cursor != null) {
                long candidateContactId = -1;
                String candidateAddress = "";
                // The following lines are provided and maintained by Mediatek Inc.
                int isDefault = 0;
                // The previous lines are provided and maintained by Mediatek Inc.
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(2);
                    if (allContacts.contains(id)) {
                        allContacts.remove(id);
                    }
                    int type = cursor.getInt(1);
                    String email = cursor.getString(0);
                 // The following lines are provided and maintained by Mediatek Inc.
                    isDefault = cursor.getInt(3);
                 // The previous lines are provided and maintained by Mediatek Inc. 
                    if (candidateContactId == -1) {
                        candidateContactId = id;
                        candidateAddress = email;
                    } else {
                        if (candidateContactId != id) {
                            if (candidateAddress != null && candidateAddress.length() > 0) {
                                if (builder.length() > 0) {
                                    builder.append(",");
                                }
                                builder.append(candidateAddress);
                            }
                            candidateContactId = id;
                            candidateAddress = email;
                        // The following lines are provided and maintained by Mediatek Inc.
                        } else if (isDefault == 1) {
                            candidateContactId = id;
                            candidateAddress = email;
                        }
                        // The previous lines are provided and maintained by Mediatek Inc.
                    }
                    if (cursor.isLast()) {
                        if (candidateAddress != null && candidateAddress.length() > 0) {
                            if (builder.length() > 0) {
                                builder.append(",");
                            }
                            builder.append(candidateAddress);
                        }
                    }
                }
                cursor.close();
            }
            Log.i(TAG, "[getEmailAddressFromGroup]builder String:" + builder.toString());
            return showNoTelphoneOrEmailToast(context, builder, resolver,
                    allContacts, "email");
        }
    }

    private StringBuilder getWhere(ContentResolver resolver, long groupId, HashSet<Long> allContacts) {
        Cursor contactCursor = resolver.query(Data.CONTENT_URI,
                new String[] {Data.CONTACT_ID},
                Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
                new String[] {GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId)}, null);
        StringBuilder ids = new StringBuilder();
        if (contactCursor != null) {
            Log.d(TAG, "contactCusor count:" + contactCursor.getCount());
            while (contactCursor.moveToNext()) {
                Long contactId = contactCursor.getLong(0);
                if (!allContacts.contains(contactId)) {
                    ids.append(contactId).append(",");
                    allContacts.add(contactId);
                }
            }
            contactCursor.close();
        }
        StringBuilder where = new StringBuilder();
        if (ids.length() > 0) {
            ids.deleteCharAt(ids.length() - 1);
            where.append(Data.CONTACT_ID + " IN (");
            where.append(ids.toString());
            where.append(")");
        } else {
            return null;
        }
        where.append(" AND ");
        return where;
    }

    @Override
    public void onDestroy() {
        unregisterSimReceiver();
        super.onDestroy();
    }

    private BroadcastReceiver mSimReceiver = null;
    
    private void registerSimReceiver() {
        Log.d(TAG, "[registerSimReceiver]mSimReceiver:" + mSimReceiver);
        if (mSimReceiver == null) {
            mSimReceiver = new SimReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_PHB_STATE_CHANGED);
            this.getActivity().registerReceiver(mSimReceiver,filter);
        }
    }
    
    private void unregisterSimReceiver() {
        Log.d(TAG, "[unregisterSimReceiver]mSimReceiver:" + mSimReceiver);
        if (mSimReceiver != null) {
            getActivity().unregisterReceiver(mSimReceiver);
            mSimReceiver = null;
        }
    }
    
    class SimReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "[onReceive]action is: " + action);
            if (ACTION_PHB_STATE_CHANGED.equals(action)) {
                boolean phbReady = intent.getBooleanExtra("ready", false);
                int slotId = intent.getIntExtra("simId", -10);
                Log.d(TAG, "[onReceive]phbReady:" + phbReady
                        + "|slotId:" + slotId);
                if (mSlotId >= 0) {
                    Log.d(TAG, "[onReceive]activity finish is: " + getActivity());
                    getActivity().finish();
                }
            }
        }

    };

  
    /**
     * M: To get all sole raw_contact_id of common contact and joined contacts.
     * @param resolver
     * @param ids
     * @return the best available rawcontactId for next query.
     */
    private StringBuilder getAllRawContactIds(ContentResolver resolver, StringBuilder ids) {

        StringBuilder whereId = new StringBuilder();
        StringBuilder rawContactIds = new StringBuilder();

        whereId.append(Contacts._ID + " IN(");
        whereId.append(ids.toString());
        whereId.append(")");

        Log.i(TAG, "[getAllRawContactIds]query allrawcontactids cursor selection:" + whereId.toString());

        /** to query all name_raw_contact_id of contacts table.*/
        Cursor cursor = resolver.query(Contacts.CONTENT_URI, new String[] { Contacts.Entity.NAME_RAW_CONTACT_ID },
                whereId.toString(), null, null);
        HashSet<Long> rawContactIdSet = new HashSet<Long>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long rawContactId = cursor.getLong(0);
                rawContactIdSet.add(rawContactId);
            }
            cursor.close();
        }

        /** to build a raw_contact_id stringbuilder.*/
        if (!rawContactIdSet.isEmpty()) {
            Long[] allRawContactsIdArray = rawContactIdSet.toArray(new Long[0]);
            for (Long id : allRawContactsIdArray) {
                if (rawContactIds.length() > 0) {
                    rawContactIds.append(",");
                }
                rawContactIds.append(id.toString());
            }
        }

        return rawContactIds;
    }
}
