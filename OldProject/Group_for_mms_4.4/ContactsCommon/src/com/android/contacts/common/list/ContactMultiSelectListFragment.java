
package com.android.contacts.common.list;

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

import com.android.contacts.common.R;

import java.util.ArrayList;
import java.util.Arrays;
import android.provider.ContactsContract;
//import com.android.contacts.activities.ContactsGroupMultiSelectActivity;
import android.content.ComponentName;
public class ContactMultiSelectListFragment extends Fragment {

    private static final String TAG = "ContactMultiSelectListFragment";
    private View mView;
    private ListView mListView;
    private Context mContext;
    private MultiSelectAdapter mAdapter;
    // TODO move loader to adapter? do in separate thread?
    private CursorLoader mCursorLoader;
    private Intent mIntent;
    private static final String KEY_INTENT = "intent";
    private static final int GROUP_MULTI_MEMBER_PICK=1;
    
    public ContactMultiSelectListFragment() {
        super();
    }
    public ContactMultiSelectListFragment(Intent intent) {
        mIntent = intent;
    }
     
 public static ContactMultiSelectListFragment newInstance(Intent intent) {
        final ContactMultiSelectListFragment frag = new ContactMultiSelectListFragment();
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
        configureAdapter();
    }

    /**
     * Choose adapter for either Contact or Phone
     */
    private void configureAdapter() {
           mIntent = getArguments().getParcelable(KEY_INTENT);
        switch (mIntent.getIntExtra(MultiSelectAdapter.INT_TYPE, -1)) {
            case MultiSelectAdapter.TYPE_CONTACT:
                mAdapter = new MultiSelectContactAdapter(getActivity());
                mAdapter.mType = MultiSelectAdapter.TYPE_CONTACT;
                break;
            case MultiSelectAdapter.TYPE_PHONE:
                mAdapter = new MultiSelectPhoneAdapter(getActivity());
                mAdapter.mType = MultiSelectAdapter.TYPE_PHONE;
                break;
              case MultiSelectAdapter.TYPE_ACCOUNT_CONTACT:
                mAdapter = new MultiSelectGroupContactAdapter(getActivity(),mIntent);
                mAdapter.mType = MultiSelectAdapter.TYPE_ACCOUNT_CONTACT;
                break;
            default:
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
                return;
        }
        mCursorLoader = mAdapter.configCursorLoader();
        Cursor cursor = mCursorLoader.loadInBackground();
        mAdapter.setCursor(cursor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        mView = inflater.inflate(R.layout.multi_select_fragment, container, false);
        mListView = (ListView) mView.findViewById(android.R.id.list);
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
        inflater.inflate(R.menu.multi_select_group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuSelection = menu.findItem(R.id.select_all);
        MenuItem menuGroupSelection = menu.findItem(R.id.select_group);
        // Update menu string
        if (mListView.getCheckedItemCount() == mListView.getCount()&&mListView.getCount()!=0) {
            menuSelection.setTitle(R.string.menu_multi_deselect_all);
        } else {
            menuSelection.setTitle(R.string.menu_multi_select_all);
        }
        if (mAdapter instanceof MultiSelectPhoneAdapter) {
           
           menuGroupSelection.setVisible(true);
          }
          else menuGroupSelection.setVisible(false);
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
            case R.id.test_key:
                testKey();
                return true;
             case R.id.select_group:
                selectGroup();
                return true;
            case R.id.menu_ok:
                if(mIntent.getIntExtra("isShare", -1)==1){
                menuOkSharePressed();
                }else{
                 menuOkPressed();
                }
                 
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
   private void selectGroup(){
    /* Log.d(TAG, "select group");
    Intent intent = new Intent(this.getActivity(),ContactsGroupMultiSelectActivity.class);
         intent.setAction("com.android.contacts.MULTI_SELECT_GROUP");
     startActivityForResult(intent, GROUP_MULTI_MEMBER_PICK);*/
       Log.d(TAG, "select group");
     Intent intent=new Intent(); 
    intent.addCategory(Intent.CATEGORY_DEFAULT);
ComponentName cn=new ComponentName("com.android.contacts","com.android.contacts.activities.ContactsGroupMultiSelectActivity"); 
    //intent.setClassName(,);
         intent.setAction("com.android.contacts.MULTI_SELECT_GROUP");
    intent.setComponent(cn);
     startActivityForResult(intent, GROUP_MULTI_MEMBER_PICK);
   }
  @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        super.onActivityResult(requestCode, resultCode, data);
         if(requestCode==GROUP_MULTI_MEMBER_PICK&&data!=null){
           //android.util.Log.d("wwf","m...........");  
         long[] contactIds = data.getLongArrayExtra("result");
           for(int j=0;j<contactIds.length;j++){
           // android.util.Log.d("wwf","contactIds:"+contactIds[j]+",mAdapter:"+mAdapter);
            for (int i = 0; i < mAdapter.getCount(); i++) {
            //android.util.Log.d("wwf","contactIds:"+contactIds[j]);
            //android.util.Log.d("wwf","mAdapter.getItemId(i):"+mAdapter.getItemContactId(i));    
              if(contactIds[j]==mAdapter.getItemContactId(i)){
                mListView.setItemChecked(i, true);
                //android.util.Log.d("wwf","mAdapter.getItemId(i):"+mAdapter.getItemContactId(i));
                  continue;
                }
            }
         }
      }
   }
    

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

    private void testKey() {
        /**** Item id dump ****/
        // long[] ids = mListView.getCheckedItemIds();
        // for (long id : ids) {
        // Log.d(TAG, "testKey() id = " + id);
        // }
        // Log.d(TAG, "testKey() ids.length = " + ids.length);

        /**** Toggle adapter ****/
        if (mAdapter instanceof MultiSelectContactAdapter) {
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
    }

    /**
     * Return selected items to caller
     */
    private void menuOkPressed() {
       // long[] items = mListView.getCheckedItemIds();
        long[] charList = mAdapter.getCheckedItemKeys(mListView);
        if (charList.length == 0) {
            // Cancel on nothing selected
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        for (long item : charList) {
            Log.d(TAG, "menuOkPressed() selected item = " + item);
        }

        Intent data = new Intent();
        data.putExtra(MultiSelectAdapter.INT_TYPE, mAdapter.mType);
        data.putExtra(MultiSelectAdapter.RESULT, charList);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

	/*
	 * reture share item to share action
	 */

	private void menuOkSharePressed() {

		String uriListBuilder = mAdapter.getCheckedItemLookUpKeys(mListView);
               // Log.d("wwf","uriListBuilder:"+uriListBuilder+"uriListBuilde:"+(uriListBuilder == null));
		if (uriListBuilder == null||uriListBuilder =="") {
			// Cancel on nothing selected
			getActivity().setResult(Activity.RESULT_CANCELED);
			getActivity().finish();
		}
		Uri uri = Uri.withAppendedPath(Contacts.CONTENT_MULTI_VCARD_URI,
				Uri.encode(uriListBuilder.toString()));
        Intent data = new Intent();
		data.putExtra(Intent.EXTRA_STREAM, uri);
		getActivity().setResult(Activity.RESULT_OK, data);
		getActivity().finish();

    }
}
