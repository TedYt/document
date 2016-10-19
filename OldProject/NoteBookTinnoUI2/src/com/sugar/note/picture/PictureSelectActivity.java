package com.sugar.note.picture;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sugar.note.NoteImageView;
import com.sugar.note.NotePad;
import com.sugar.note.PublicUtils;
import com.sugar.note.R;

/**
 * Created by user on 7/21/14.
 */
public class PictureSelectActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener,
            StorageMountedReceiver.MountedChangeListener {

    private GridViewAdapter mAdapter;

    private final int MODE_VIEW = 0;
    private final int MODE_SELECT = 1;
    private int mMode = MODE_SELECT;

    private int mSelectedCount = 0;

    private final int MAX_SELECT_NUM = 20;

    private String mFolderName;
    private GridView mGridView;
    private TextView mHint;

    private boolean isNeedUpdate = false;

    private StorageMountedReceiver mReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String folderName = null;
        if (intent != null){
            folderName = intent.getStringExtra("picture_folder_name");
        }
        Log.d("tui", "PictureSelectActivity, onCreate, path = " + folderName);
        mFolderName = folderName;

        mReceiver = new StorageMountedReceiver(this);
        mReceiver.registerMountReceiver(this);

        setContentView(R.layout.note_select_picture_grid);
        mGridView = (GridView)findViewById(R.id.picture_grid);
        mHint = (TextView)findViewById(R.id.no_picture);

        mGridView.setOnItemClickListener(this);
        //mGridView.setOnItemLongClickListener(this);

        boolean isStorageMounted = PublicUtils.isStorageMounted();
        if (isStorageMounted) {
            mAdapter = new GridViewAdapter(this, mGridView, folderName);
            mGridView.setAdapter(mAdapter);
        }

       initActionBar(folderName);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isStorageMounted = PublicUtils.isStorageMounted();
        if (isStorageMounted) {
            if (isNeedUpdate) {
                Log.d("tui", "onResume,isNeedUpdate");
                isNeedUpdate = false;
                mAdapter.updateCursor(mFolderName);
            }
            int count = mAdapter.getCount();
            if (count == 0) {
                Log.d("tui", "onResume,count == 0");
                mGridView.setVisibility(View.GONE);
                mHint.setVisibility(View.VISIBLE);
                mHint.setText(R.string.picture_no_picture);
            } else {
                Log.d("tui", "onResume,count != 0");
                mHint.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            Log.d("tui", "onResume,unMounted");
            mGridView.setVisibility(View.GONE);
            mHint.setVisibility(View.VISIBLE);
            mHint.setText(R.string.picture_storage_used);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("tui", "onPause");
        isNeedUpdate = true;
        if (mAdapter != null) {
            mAdapter.resetFirstEnter();
        }
    }

    private void initActionBar(String folderName) {

        ActionBar bar = getActionBar();

        if (bar != null){
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.actionbar_layout, null);
            TextView text = (TextView)view.findViewById(R.id.title_hint);
            if (folderName == null){
                text.setText(R.string.picture_select_recent);
            }else {
                text.setText(folderName);
            }

            Button btn = (Button)view.findViewById(R.id.selectall);
            btn.setText(R.string.picture_select_select);
            btn.setOnClickListener(this);
            btn.setEnabled(false);

            btn = (Button)view.findViewById(R.id.cancle);
            btn.setText(R.string.picture_select_gallery);
            btn.setOnClickListener(this);

            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(view);
        }
    }

    /**
     *
     * @param path like " Pictures  "
     * @return
     */
    private String getFolderName(String path) {
        String[] s = path.split("/");

        return s[s.length - 1];
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.cancle:
                Intent intent = new Intent(this, PictureFolderActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.selectall:
                confirmPicture();
                break;
            default:
                break;
        }
    }

    private void confirmPicture() {
        Log.d("tui", "NotePad.mPaths.size = " + NotePad.mPaths.size());

        if (NotePad.mPaths != null){
            for (int i =0; i < mAdapter.getCount(); i++){
                if (mAdapter.isItemSelected(i)){
                    String path = mAdapter.getItemPath(i);
                    NotePad.mPaths.add(path);
                }
            }
        }else {
            //throw exception
        }

        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {

        if (view instanceof ImageView){
            Log.d("tui", "view instanceof ImageView");
        }
        if (view instanceof LinearLayout){// Bingo
            Log.d("tui", "view instanceof LinearLayout");
        }

        if (mSelectedCount >= MAX_SELECT_NUM){
            Toast.makeText(this,
                    getResources().getString(R.string.picture_select_max_num),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (mMode == MODE_SELECT){
            if (mAdapter.isItemSelected(index)){
                mSelectedCount --;
                mAdapter.setItemSelected(index, false);
                view.setBackgroundColor(0x00ffffff);
            }else{
                mSelectedCount ++;
                mAdapter.setItemSelected(index, true);
                view.setBackgroundColor(getItemSelectedColor());
            }

            /*if (mSelectedCount == 0){
                mMode = MODE_VIEW;
            }*/

            updateTitle();
        }else{
            String path = mAdapter.getItemPath(index);

            Log.d("tui", "onItemClick, i = " + index + ", path = " + path);

            Intent intent = new Intent(this, NoteImageView.class);
            intent.putExtra("note_img_path", path);
            startActivity(intent);
        }
    }

   /* @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
        mMode = MODE_SELECT;
        mSelectedCount ++;
        mAdapter.setItemSelected(index, true);
        view.setBackgroundColor(getItemSelectedColor());
        updateTitle();
        return true;
    }*/

    private int getItemSelectedColor(){
        return getResources().getColor(R.color.grid_item_selected_color);
    }

    private void updateTitle() {
        String text;
        Button btn = (Button)findViewById(R.id.selectall);
        if (mSelectedCount == 0){
            btn.setEnabled(false);
            if (mFolderName == null){
                text = getString(R.string.picture_select_recent);
            }else {
                text = mFolderName;
            }
        }else {
            btn.setEnabled(true);
            text = getString(R.string.picture_select_num, mSelectedCount);
        }

        TextView textView = (TextView)findViewById(R.id.title_hint);
        textView.setText(text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isNeedUpdate = false;
        mReceiver.unRegisterMountReceiver(this);

        if (mAdapter != null) {
            mAdapter.onDestroy();
        }
    }

    @Override
    public void onMountedChanged(String state) {
        if (state.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            Log.d("tui", "Intent.ACTION_MEDIA_MOUNTED");
            mHint.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            mAdapter = new GridViewAdapter(this, mGridView, mFolderName);//必须重新生成
            mGridView.setAdapter(mAdapter);
        }else if (state.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            Log.d("tui", "Intent.ACTION_MEDIA_UNMOUNTED");
            mGridView.setAdapter(null);
            mGridView.setVisibility(View.GONE);
            mHint.setVisibility(View.VISIBLE);
            mHint.setText(R.string.picture_storage_used);
        }
    }
}