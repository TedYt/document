
package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.CursorLoader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;

import com.android.contacts.ContactsActivity;
import com.android.contacts.common.list.ContactMultiSelectListFragment;
import com.android.contacts.common.list.PhoneNumberPickerFragment;
import com.android.contacts.R;

public class ContactsMultiSelectActivity extends ContactsActivity {

    private Fragment mListFragment;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_picker);

        // Override style of ContactPickerTheme, xml way dosen`t work
        getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_TITLE
                        | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_USE_LOGO);

        configFragment();

        // hide search view default
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setVisibility(View.GONE);
    }

    private void configFragment() {
        mListFragment = ContactMultiSelectListFragment.newInstance(getIntent());
        getFragmentManager().beginTransaction().replace(R.id.list_container,
                mListFragment).commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
   //add by laiyan for fix home button to exit, change the system language, and then click on the contact/mms
   //will be reported InstantiationException.
  // @Override
   //protected void onPause() {
  //    super.onPause();
    //    finish();
    //}

}
