package com.sugar.note.picture;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sugar.note.PublicUtils;
import com.sugar.note.R;

/**
 * Created by user on 7/24/14.
 */
public class PictureFolderActivity extends ListActivity implements AdapterView.OnItemClickListener, View.OnClickListener, StorageMountedReceiver.MountedChangeListener {

    private PictureFolderAdapter mAdapter;

    private ListView mList;
    private TextView mHint;

    private Boolean isNeedUpdate = false;
    private StorageMountedReceiver mReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReceiver = new StorageMountedReceiver(this);
        mReceiver.registerMountReceiver(this);

        setContentView(R.layout.folder_list_layout);
        mHint = (TextView)findViewById(R.id.folder_hint);

        init();
        mList = getListView();
        mList.setOnItemClickListener(this);

        boolean isStorageMounted = PublicUtils.isStorageMounted();
        if (isStorageMounted) {
            mAdapter = new PictureFolderAdapter(this, mList);
            mList.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isStorageMounted = PublicUtils.isStorageMounted();
        if (isStorageMounted) {
            if (isNeedUpdate) {
                isNeedUpdate = false;
                mAdapter.updateFolderAttrs();
            }
            int count = mAdapter.getCount();
            if (count == 0) {
                Log.d("tui", "onResume,count == 0");
                mList.setVisibility(View.GONE);
                mHint.setVisibility(View.VISIBLE);
                mHint.setText(R.string.picture_no_picture);
            } else {
                Log.d("tui", "onResume,count != 0");
                mHint.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            mList.setVisibility(View.GONE);
            mHint.setVisibility(View.VISIBLE);
            mHint.setText(R.string.picture_storage_used);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isNeedUpdate = true;
        if (mAdapter != null) {
            mAdapter.resetFirstEnter();
        }
    }

    private void init() {
        ActionBar bar = getActionBar();
        if (bar != null){
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.actionbar_layout, null);
            TextView text = (TextView)view.findViewById(R.id.title_hint);
            text.setText(R.string.picture_select_gallery);

            Button btn = (Button)view.findViewById(R.id.selectall);
            btn.setVisibility(View.INVISIBLE);
            //btn.setOnClickListener(this);

            btn = (Button)view.findViewById(R.id.cancle);
            btn.setText(R.string.str_cancel);
            btn.setOnClickListener(this);

            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(view);
        }
    }

    @Override
    protected void onDestroy() {
        isNeedUpdate = false;
        mReceiver.unRegisterMountReceiver(this);
        if (mAdapter != null) {
            mAdapter.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {

        String name = mAdapter.getItemFolderName(index);
        Intent intent = new Intent(this, PictureSelectActivity.class);
        intent.putExtra("picture_folder_name", name);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.cancle:
                finish();
                break;
        }
    }

    @Override
    public void onMountedChanged(String state) {
        if (state.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            Log.d("tui", "Intent.ACTION_MEDIA_MOUNTED");
            mHint.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
            mAdapter = new PictureFolderAdapter(this, mList);//必须重新生成
            mList.setAdapter(mAdapter);
        }else if (state.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            Log.d("tui", "Intent.ACTION_MEDIA_UNMOUNTED");
            mList.setAdapter(null);
            mList.setVisibility(View.GONE);
            mHint.setVisibility(View.VISIBLE);
            mHint.setText(R.string.picture_storage_used);
        }
    }
}