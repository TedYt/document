package com.tinno.launcher2;

import java.util.ArrayList;

import com.android.launcher2.CellLayout;
import com.android.launcher2.Launcher;
import com.android.launcher2.Workspace;
import com.tinno.launcher2.DateAdapter.ViewHolder;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.launcher.R;

public class TuiThumbnailConfig {

	private final int ORIGINAL_LEFT = 2; //第一个item起始位置的x值
	private final int ORIGINAL_TOP = 134; //第一个item起始位置的y值
	private final int H_GAP = 10; //水平间隔
	private final int V_GAP = 8; //垂直间隔
	private final int ITEMS_ROW = 3; //每行缩略图的数量
	
	//这里的两个变量的值必须和文件TuiWorkspaceManager中的两个同名变量的值一样
	public final int MOVE_TO_RIGHT = 100;
	public final int MOVE_TO_LEFT = 200;
	
	private int mWidth;
	private int mHeight;
	
	private int mStatusBarH; //状态栏的高度
	
	private int mItemsCount;
	
	private Context mContext;
	
	private ArrayList<CellLayout> mCellLayouts;
	
	private ArrayList<View> mNailViews;
	
//	static private ArrayList<Bitmap> mNailsBitmap;
	
	TuiThumbnailConfig(Context context){
		mContext = context;
		mNailViews = new ArrayList<View>();
//		mNailsBitmap = new ArrayList<Bitmap>();
		
		init();
	}
	
	/**
	 * 
	 */
	private void init() {
		final Workspace workspace = Launcher._instance.getWorkspace();
		
		if (workspace == null){
			//do something
			return;
		}
		
		int count = workspace.getChildCount();
//		workspace.enableChildrenCache(0, count);
		
		mCellLayouts = new ArrayList<CellLayout>();
		for (int i = 0; i < workspace.getChildCount(); i++) {
			mCellLayouts.add((CellLayout) workspace.getChildAt(i));
		}
//		boolean is_add_flg = false;
		//增加一个“添加”项
		/*if (workspace.getChildCount() < 9) {
			CellLayout newCellLayout = (CellLayout) LayoutInflater.from(
					Launcher._instance).inflate(R.layout.workspace_screen, null);
			ImageView img = new ImageView(Launcher._instance);
			img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			img.setImageResource(R.drawable.preview_addscreen_bg);
			newCellLayout.addView(img);
			mCellLayouts.add(newCellLayout);
//			is_add_flg = true;
		}*/
		mWidth = (int)(mCellLayouts.get(0).getWidth() / 3.1f);//最终结果是232
		mHeight = (int)(mCellLayouts.get(0).getHeight() / 2.9f);//最终结果是335
	}

	/**
	 * 
	 * @param context
	 * @param longClick
	 * @param click
	 * @return
	 */
	public void initNailViews(Context context, OnLongClickListener longClick, OnClickListener click){
//		View[] views = new View[mCellLayouts.size()];
		
		for(int i =0; i<mCellLayouts.size(); i++){
			mCellLayouts.get(i).setDrawingCacheEnabled(true);
			Bitmap bitmap = mCellLayouts.get(i).getDrawingCache();
			
			View convertView;
			ViewHolder viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.screens_add_item, null);
			viewHolder.screens_layout = (RelativeLayout) convertView
					.findViewById(R.id.screens_layout);
			viewHolder.screens_item = (ImageView) convertView
					.findViewById(R.id.screens_item);
			viewHolder.screens_home_img = (ImageView) convertView
					.findViewById(R.id.screens_home_img);
			viewHolder.screens_delete = (ImageView)convertView
					.findViewById(R.id.screens_delete);
			
			viewHolder.screens_item.setVisibility(View.VISIBLE);
			viewHolder.screens_home_img.setVisibility(View.VISIBLE);
			viewHolder.screens_layout.setVisibility(View.VISIBLE);
			viewHolder.screens_layout.setBackgroundResource(R.drawable.preview_background);
			
			viewHolder.screens_home_img.setOnClickListener(click);
			
			viewHolder.screens_delete.setOnClickListener(click);

			if (bitmap != null) {
				viewHolder.screens_item
						.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				viewHolder.screens_item.setImageBitmap(bitmap);
				int defaultScreen = AlmostNexusSettingsHelper.getDefaultScreen(mContext);
				if(defaultScreen == i){
					viewHolder.screens_home_img.setBackgroundResource(R.drawable.preview_home_btn_light);
				}else{
					viewHolder.screens_home_img.setBackgroundResource(R.drawable.preview_home_btn);
				}
			} /*else if(holdPosition < position){
				viewHolder.screens_item
						.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				viewHolder.screens_item
						.setImageResource(R.drawable.preview_addscreen_bg);
				viewHolder.screens_home_img.setVisibility(View.GONE);
				viewHolder.screens_delete.setVisibility(View.GONE);
			} else if(IS_ADD_NEW_SREEN == true){
				//Tinno ++ 
				int defaultScreen = AlmostNexusSettingsHelper.getDefaultScreen(mContext);
				if(defaultScreen == position){
					viewHolder.screens_home_img.setBackgroundResource(R.drawable.preview_home_btn_light);
				}else{
					viewHolder.screens_home_img.setBackgroundResource(R.drawable.preview_home_btn);
				}
				//Tinno --end
			}*/
			
			/*if (IS_LAST_ADD == true) {
				if ((mScreens.size() - 1) == position) {
					if (IS_LAST_HIDEN == false) {
						viewHolder.screens_item
								.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
						viewHolder.screens_item
								.setImageResource(R.drawable.preview_addscreen_bg);
						viewHolder.screens_home_img.setVisibility(View.INVISIBLE);
						viewHolder.screens_delete.setVisibility(View.GONE);
					} else {
						viewHolder.screens_item.setVisibility(View.INVISIBLE);
						viewHolder.screens_home_img.setVisibility(View.INVISIBLE);
						viewHolder.screens_layout.setVisibility(View.INVISIBLE);
					}
				}
			}*/
			
			/*if (isMoving && position == gonePosition) {
				convertView.setVisibility(View.INVISIBLE);
			}else{
				convertView.setVisibility(View.VISIBLE);
			}*/
			convertView.setVisibility(View.VISIBLE);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);
			params.leftMargin = getMarginLeft(i);
			params.topMargin = getMarginTop(i);
			convertView.setLayoutParams(params);
			
//			convertView.setTag(i);
			convertView.setOnClickListener(click);
			convertView.setOnLongClickListener(longClick);
			
			mNailViews.add(convertView);
			
			/*convertView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			convertView.layout(0, 0, convertView.getMeasuredWidth(), convertView.getMeasuredHeight());
//			convertView.setDrawingCacheEnabled(true);
			convertView.buildDrawingCache();
//			convertView.setDrawingCacheBackgroundColor(0);
//			convertView.setWillNotCacheDrawing(false);
			Bitmap bm = convertView.getDrawingCache();*/
			//Log.d("tui", "bitmap w = " + bm.getWidth() + ", h = " + bm.getHeight());//w = 730, h = 982
			
//			mNailsBitmap.add(bm);
			
//			mNailViews.add(convertView);
//			views[i] = convertView;
		}
		
//		return views;
	}
	
	public View getViewForNewItem(int index){
		
		View convertView;
		ViewHolder viewHolder = new ViewHolder();
		convertView = LayoutInflater.from(mContext).inflate(
				R.layout.screens_add_item, null);
		viewHolder.screens_layout = (RelativeLayout) convertView
				.findViewById(R.id.screens_layout);
		viewHolder.screens_item = (ImageView) convertView
				.findViewById(R.id.screens_item);
		viewHolder.screens_home_img = (ImageView) convertView
				.findViewById(R.id.screens_home_img);
		viewHolder.screens_delete = (ImageView)convertView
				.findViewById(R.id.screens_delete);
		
		viewHolder.screens_item.setVisibility(View.VISIBLE);
		
		viewHolder.screens_home_img.setVisibility(View.VISIBLE);
		viewHolder.screens_home_img.setBackgroundResource(R.drawable.preview_home_btn);

		viewHolder.screens_layout.setVisibility(View.VISIBLE);
		viewHolder.screens_layout.setBackgroundResource(R.drawable.preview_background);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);
		params.leftMargin = getMarginLeft(index);
		params.topMargin = getMarginTop(index);
		convertView.setLayoutParams(params);
		
		return convertView;
	}
	
	public View getViewForDrag(int index){
		mCellLayouts.get(index).setDrawingCacheEnabled(true);
		Bitmap bitmap = mCellLayouts.get(index).getDrawingCache();
		
		View convertView;
		ViewHolder viewHolder = new ViewHolder();
		convertView = LayoutInflater.from(mContext).inflate(
				R.layout.screens_add_item, null);
		viewHolder.screens_layout = (RelativeLayout) convertView
				.findViewById(R.id.screens_layout);
		viewHolder.screens_item = (ImageView) convertView
				.findViewById(R.id.screens_item);
		viewHolder.screens_home_img = (ImageView) convertView
				.findViewById(R.id.screens_home_img);
		viewHolder.screens_delete = (ImageView)convertView
				.findViewById(R.id.screens_delete);
		
		viewHolder.screens_item.setVisibility(View.VISIBLE);
		viewHolder.screens_home_img.setVisibility(View.VISIBLE);
		viewHolder.screens_layout.setVisibility(View.VISIBLE);
		viewHolder.screens_layout.setBackgroundResource(R.drawable.preview_background);
		
		if (bitmap != null) {
			viewHolder.screens_item
					.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			viewHolder.screens_item.setImageBitmap(bitmap);
			int defaultScreen = AlmostNexusSettingsHelper.getDefaultScreen(mContext);
			if(defaultScreen == index){
				viewHolder.screens_home_img.setBackgroundResource(R.drawable.preview_home_btn_light);
			}else{
				viewHolder.screens_home_img.setBackgroundResource(R.drawable.preview_home_btn);
			}
		}
		
		convertView.setVisibility(View.VISIBLE);
		
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);
//		params.leftMargin = getMarginLeft(index);
//		params.topMargin = getMarginTop(index);
//		convertView.setLayoutParams(params);
		
		return convertView;
	}
	
	public int getMarginTop(int index) {
		int top = (index / ITEMS_ROW) * (V_GAP + mHeight) + ORIGINAL_TOP - mStatusBarH; 
		return top;
	}

	public int getMarginLeft(int index) {
		int left = (index % ITEMS_ROW) * (H_GAP + mWidth) + ORIGINAL_LEFT;
		return left;
	}

	class ViewHolder {
		RelativeLayout screens_layout;
		ImageView screens_item;
		ImageView screens_home_img;
		ImageView screens_delete;
	}

	public void saveStatusBarH(int statusBarH) {
		mStatusBarH = statusBarH;
	}
	
	public void onDestory(){
		if (mCellLayouts != null){
			for (CellLayout cell : mCellLayouts){
				//这一步是必要的，要不然，在桌面上icon位置的改变或这其他的改变，不能反映到桌面编辑上
				cell.setDrawingCacheEnabled(false);
			}
			mCellLayouts.clear();
			mCellLayouts = null;
		}
		if (mNailViews != null){
			mNailViews.clear();
			mNailViews = null;
		}
//		for (Bitmap bm : mNailsBitmap){
//			if(!bm.isRecycled()){
//				bm.recycle();
//			}
//		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int getThumbnailCount(){
		return mCellLayouts.size();
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public View getNailView(int index){
		return mNailViews.get(index);
	}
	
	public View getLatesNailView(){
		int index = mNailViews.size() - 1;
		return mNailViews.get(index);
	}
	
	public ArrayList<View> getNailViews(Context context, OnLongClickListener longClick, OnClickListener click){
		initNailViews(context, longClick, click);
		return mNailViews;
	}
	
	/**
	 * 
	 * @param one the index of one item
	 * @param another the index of another item
	 *//*
	public void exchangeNailView(int one, int another){
		if (one >= mNailViews.size() || another >= mNailViews.size()){
			new Throwable("the index of item exceed the size of mNailViews , the size is" + mNailViews.size());
			return;
		}
		
		View temp = mNailViews.get(one);
		mNailViews.set(one, mNailViews.get(another));
		mNailViews.set(another, temp);
	}*/
	
	/**
	 * 
	 * @param index
	 * @return
	 */
//	public Bitmap getThumbnail(int index){
		
//		return Bitmap.createBitmap(mNailsBitmap.get(index));
		
		/*mCellLayouts.get(index).setDrawingCacheEnabled(true);
		Bitmap bitmap = mCellLayouts.get(index).getDrawingCache(true);*/
		
		/*View view = mNailViews.get(index);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();*/
		
		/*view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();*/
		
//		return bitmap;
//		return null;
//	}
	
	public int getNailHeight(){
		return mHeight;
	}
	
	public int getNailWidth(){
		return mWidth;
	}
	
	/**
	 * Calculate the index of item moved in according to the x and y 
	 * @param x
	 * @param y
	 * @return the index of item
	 */
	public int getIndex(int x, int y) {
		int index;
		int row = (y + mStatusBarH - ORIGINAL_TOP)/(V_GAP + mHeight);
		index = row * ITEMS_ROW + (x - ORIGINAL_LEFT)/(H_GAP + mWidth);
		//Log.d("tui", "getIndex row = " + row + ", index = " + index);
		
		return index;
	}

	public void updateThumbnails(int direction, int from, int to) {
		
		CellLayout cellFrom = mCellLayouts.get(from);
		
		if(direction == MOVE_TO_LEFT){
			while(from > to){
				CellLayout cell = mCellLayouts.get(from - 1);
				mCellLayouts.remove(from);
				mCellLayouts.add(from, cell);
				
				//不能在这里更新worksapce，否则会造成连续移动过程很卡
				//workspace.TuiSwapWorkSpace(from - 1, from);
				
				from --;
			}
		}else{
			while(from < to){
				CellLayout cell = mCellLayouts.get(from + 1);
				mCellLayouts.remove(from);
				mCellLayouts.add(from, cell);
				
				//不能在这里更新worksapce，否则会造成连续移动过程很卡
				//workspace.TuiSwapWorkSpace(from + 1, from);
				
				from ++;
			}
		}
		
		mCellLayouts.remove(to);
		mCellLayouts.add(to, cellFrom);
		
//		workspace.removeViewAt(to);
//		workspace.addView(childFrom, to);
	}
	
	public void updateWorkSpace(int from, int to) {
		
		final Workspace workspace = Launcher._instance.getWorkspace();
		int childCount = workspace.getChildCount();
		if (from >= childCount || to >= childCount){
			throw new ArrayIndexOutOfBoundsException("Invalid index from = " + from + ", to = " + to + ", size = " + childCount);
		}
		
		if (from == to){
			return;
		}
		
		if (from > to){ //向左
			while(from > to){
				workspace.TuiSwapWorkSpace(from - 1, from);
				from --;
			}
		}else{//向右
			while(from < to){
				workspace.TuiSwapWorkSpace(from + 1, from);
				from ++;
			}
		}
	}

	public void addCellLayout() {
		
		//Begin bug SNTZOTIU-44 yutao 2013.8.29
		final Workspace work = Launcher._instance.getWorkspace();
		int size = mCellLayouts.size();
		work.TuiAddScreen(size);
		//End bug SNTZOTIU-44 yutao 2013.8.29
		
		CellLayout newCellLayout = (CellLayout) LayoutInflater.from(
				Launcher._instance).inflate(R.layout.workspace_screen, null);
		ImageView img = new ImageView(Launcher._instance);
		img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		img.setImageResource(R.drawable.preview_addscreen_bg);
		newCellLayout.addView(img);
		mCellLayouts.add(newCellLayout);
		
		Log.d("tui", "TuiThumbnailConfig --- addCellLayout size = " + mNailViews.size());
		View view = getViewForNewItem(mNailViews.size());//mNailViews.size()
		mNailViews.add(view);
	}

	public void addWorkspaceCell() {
		
//		final Workspace work = Launcher._instance.getWorkspace();
//		
//		CellLayout cell = (CellLayout) LayoutInflater
//				.from(Launcher._instance).inflate(R.layout.workspace_screen, null);
//		cell.setLayoutParams(
//				new CellLayout.LayoutParams(
//						new LayoutParams(
//								LayoutParams.FILL_PARENT,
//								LayoutParams.FILL_PARENT)));
//		cell.setGridSize(4, 4);
//		cell.requestLayout();
//		cell.setOnLongClickListener(work.TuiGetLongClickListener());
////		newWorkSpace.buildLayer();
//		work.addView(cell);
	}
	
	public void Helper(){
		int size = mNailViews.size();
		Log.d("tui", "TuiThumbnailConfig --- Helper: siez = " + size);
	}

	public void deleteData(int index) {
		
		int size = mCellLayouts.size();
		if (index >= size){
			throw new ArrayIndexOutOfBoundsException("Invalid index " + index + ", size = " + size);
		}
		
		mCellLayouts.remove(index);
	}
}
