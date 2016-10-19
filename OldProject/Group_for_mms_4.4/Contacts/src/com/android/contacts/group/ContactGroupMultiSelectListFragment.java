
package com.android.contacts.group;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import com.android.contacts.GroupListLoader;
import com.android.contacts.common.ContactsUtils;

import com.android.contacts.common.R;

import java.util.ArrayList;
import java.util.Arrays;
import android.provider.ContactsContract;
import com.android.contacts.GroupMemberLoader;
import android.os.Handler;
import android.os.Message;
public class ContactGroupMultiSelectListFragment extends Fragment {

    private static final String TAG = "ContactGroupMultiSelectListFragment";
    private View mView;
    private ListView mListView;
    private Context mContext;
    private GroupBrowseListAdapter mAdapter;
    // TODO move loader to adapter? do in separate thread?
    private CursorLoader mCursorLoader;
    private Intent mIntent;
    private static final String KEY_INTENT = "intent";
    private static final int LOADER_GROUPS = 1;
    private static final int LOADER_MEMBERS = 2;
    private Cursor mGroupListCursor;
    private long[] mGroupIdList;
    private long[] mContactsIdList;
    private long groupId;
  //  private String[] contactIds;
    
    public ContactGroupMultiSelectListFragment() {
        super();
    }
    public ContactGroupMultiSelectListFragment(Intent intent) {
        mIntent = intent;
    }
     
 public static ContactGroupMultiSelectListFragment newInstance(Intent intent) {
        final ContactGroupMultiSelectListFragment frag = new ContactGroupMultiSelectListFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_INTENT, intent);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setHasOptionsMenu(true);
        mContext = (Context) getActivity();
       // configureAdapter();
    }

    /**
     * Choose adapter for either Contact or Phone
     */
    private void configureAdapter() {
          // mIntent = getArguments().getParcelable(KEY_INTENT);
       // switch (mIntent.getIntExtra(MultiSelectAdapter.INT_TYPE, -1)) {
           // case MultiSelectAdapter.TYPE_CONTACT:
                
              //  mAdapter.mType = MultiSelectAdapter.TYPE_CONTACT;
              //  break;
            //case MultiSelectAdapter.TYPE_PHONE:
              //  mAdapter = new MultiSelectPhoneAdapter(getActivity());
               // mAdapter.mType = MultiSelectAdapter.TYPE_PHONE;
              //  break;
          //  default:
             //   getActivity().setResult(Activity.RESULT_CANCELED);
             //   getActivity().finish();
             //   return;
       // }
       // mCursorLoader = mAdapter.configCursorLoader();
      //  Cursor cursor = mCursorLoader.loadInBackground();
        //mAdapter.setCursor(cursor);
    }
 @Override
    public void onStart() {
        getLoaderManager().initLoader(LOADER_GROUPS, null, mGroupLoaderListener);
        super.onStart();
    }
      private final LoaderManager.LoaderCallbacks<Cursor> mGroupLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
           // mEmptyView.setText(null);
            return new GroupListLoader(mContext);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mGroupListCursor = data;
            bindGroupList();
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
 private void bindGroupList() {
        //mEmptyView.setText(R.string.noGroups);
        //setAddAccountsVisibility(!ContactsUtils.areGroupWritableAccountsAvailable(mContext));
        if (mGroupListCursor == null) {
            return;
        }
        mAdapter.setCursor(mGroupListCursor);

       // if (mSelectionToScreenRequested) {
         //   mSelectionToScreenRequested = false;
       //     requestSelectionToScreen();
       // }

      //  mSelectedGroupUri = mAdapter.getSelectedGroup();
      ///  if (mSelectionVisible && mSelectedGroupUri != null) {
       //     viewGroup(mSelectedGroupUri);
       // }
    }
 // public void setAddAccountsVisibility(boolean visible) {
      //  if (mAddAccountsView != null) {
       //     mAddAccountsView.setVisibility(visible ? View.VISIBLE : View.GONE);
       // }
   // }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        mView = inflater.inflate(R.layout.multi_select_fragment, container, false);
        mListView = (ListView) mView.findViewById(android.R.id.list);
        mAdapter = new GroupBrowseListAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Update option menu string
                 Log.d(TAG, "onItemClick:"+position+"id:"+id);
                if(mListView.isItemChecked(position))
                   mListView.setItemChecked(position,true);
                else mListView.setItemChecked(position,false);
           
                getActivity().invalidateOptionsMenu();
            }
        });

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.multi_select_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuSelection = menu.findItem(R.id.select_all);
        // Update menu string
        if (mListView.getCheckedItemCount() == mListView.getCount()&&mListView.getCount()!=0) {
            menuSelection.setTitle(R.string.menu_multi_deselect_all);
        } else {
            menuSelection.setTitle(R.string.menu_multi_select_all);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_all:
                if (mAdapter.getCount() != mListView.getCheckedItemCount()) {
                    selectAll();
                    item.setTitle(R.string.menu_multi_deselect_all);
                } else {
                    deselectAll();
                    item.setTitle(R.string.menu_multi_select_all);
                }
                return true;
           // case R.id.test_key:
           //     testKey();
              //  return true;
             //case R.id.select_group:
              //  selectGroup();
              //  return true;
            case R.id.menu_ok:
               // if(mIntent.getIntExtra("isShare", -1)==1){
              //  menuOkSharePressed();
               // }else{
                 menuOkPressed();
               // }
                 
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
 //  private void selectGroup(){
   //  Log.d(TAG, "select group");
    // Intent intent = new Intent(this.getActivity(),ContactsGroupMultiSelectActivity.class);
    //     intent.setAction(MultiSelectAdapter.INTENT);
  // }

    
    private void selectAll() {
        Log.d(TAG, "selectAll()");
        for (int i = 0; i < mAdapter.getCount(); i++) {
            mListView.setItemChecked(i, true);
        }
    }

    private void deselectAll() {
        Log.d(TAG, "deselectAll()");
        for (int i = 0; i < mAdapter.getCount(); i++) {
            mListView.setItemChecked(i, false);
        }
    }

  //  private void testKey() {
        /**** Item id dump ****/
        // long[] ids = mListView.getCheckedItemIds();
        // for (long id : ids) {
        // Log.d(TAG, "testKey() id = " + id);
        // }
        // Log.d(TAG, "testKey() ids.length = " + ids.length);

        /**** Toggle adapter ****/
     /*   if (mAdapter instanceof MultiSelectContactAdapter) {
            Log.d(TAG, "testKey() 1");
            mAdapter = new MultiSelectPhoneAdapter(getActivity());
        } else {
            Log.d(TAG, "testKey() 2");
            mAdapter = new MultiSelectContactAdapter(getActivity());
        }
        mCursorLoader = mAdapter.configCursorLoader();
        Cursor cursor = mCursorLoader.loadInBackground();
        mAdapter.setCursor(cursor);
        // must clear before setting adapter, otherwise choices will stay
        // checked
        mListView.clearChoices();
        mListView.setAdapter(mAdapter);
    }*/

    /**
     * Return selected items to caller
     */
    private void menuOkPressed() {
        long[] items = mListView.getCheckedItemIds();
        mGroupIdList = mAdapter.getCheckedItemGroupKeys(mListView);
        if (mGroupIdList.length == 0) {
            // Cancel on nothing selected
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }
       // Log.d("wwf", "mGroupIdList= " + mGroupIdList.length());
        for (int i=0 ;i<mGroupIdList.length;i++) {
            Log.d("wwf", "menuOkPressed() selected item = " + mGroupIdList[i]);
           // groupId=mGroupIdList[i];
            //startGroupMembersLoader(i+1);
        }
        Handler handler = new Handler();
     startGroupMembersLoader();
///////////////////

    };
   private Handler handler=new Handler(){
         @Override
         public void handleMessage(Message msg) {
                   super.handleMessage(msg);
                   switch(msg.what){
                   case 1:
               // android.util.Log.d("wwf","lalla");
                    Intent data = new Intent();
              data.putExtra("result", mContactsIdList);
              getActivity().setResult(Activity.RESULT_OK, data);
              getActivity().finish();
                      break;
                   }
         }
};
  private void startGroupMembersLoader() {
      getLoaderManager().initLoader(LOADER_MEMBERS, null, mGroupMemberListLoaderListener);
    }

    /**
     * The listener for the group members list loader
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupMemberListLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return GroupMemberLoader.constructLoaderForGroupDetailQuery(mContext, mGroupIdList);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        android.util.Log.d("wwf","data.getCount():"+data.getCount());
           int i=0;
         data.moveToPosition(-1);
         mContactsIdList=new long[data.getCount()];
         while (data.moveToNext()) {
          long contactId=data.getLong(GroupMemberLoader.GroupDetailQuery.CONTACT_ID);
          //android.util.Log.d("wwf","contactId:"+contactId);
          mContactsIdList[i++]=contactId;
         }
            Message message = new Message();  
            message.what = 1;  
            handler.sendMessage(message);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };

}
