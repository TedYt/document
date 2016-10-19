package com.tinno.launcher2;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.animation.*;
import android.content.DialogInterface;

import com.android.launcher.R;
import com.android.launcher2.CellLayout;
import com.android.launcher2.Launcher;
import com.android.launcher2.Workspace;

public class TuiWorkspaceManager extends Activity implements OnClickListener, OnLongClickListener{

	private final int ANIM_NONE = -1;
	private final int ANIM_LONG_CLICK = 10;
	private final int ANIM_LONG_CLICK_DROP = 20;
	private final int ANIM_DELETE_ITEM = 30;
	private final int ANIM_AFTER_DELETE_ITEM = 31;
	private final int ANIM_SET_ADD_ITEM = 40;
	
	private final int DURATION = 300;
	
	public final int MOVE_TO_RIGHT = 100;
	public final int MOVE_TO_LEFT = 200;
	
	private final int MAX_ITEM_COUNT = 9;
	private final int MIN_ITEM_COUNT = 1;
	
	private final float SCALE_TO = 1.05F;
	
	private RelativeLayout root;
	
//	private int mStatusBarH;//状态栏的高度
	
	private TuiThumbnailConfig mConfig;
	
	private int mSelectedIndex = -1; //长按所略图的索引
	
	//移动至某位置的索引，比如要将2移到5,那么mMovedToIndex初始值为2, 移动到5的时候，就为5
	private int mMovedToIndex = -1;
	
	private View mDragView;
	
	WindowManager mWindowManager;
	WindowManager.LayoutParams mWMLayouParams;
	
	private int mOld_x = -1;
	private int mOld_y = -1;
	private final int MOVE_MIN_SHIFT = 10; //产生移动，需要的最小位移差
	
	private final int ENTRY_MOVE = 1000;
	private final int IN_MOVE   = 1100;
	private final int END_MOVE  = 1200;
	private final int NO_ACTION = 1300;
	private int mMoveState = NO_ACTION;
	
	private boolean isInMove = false; //标识是否产生了移动
	private static boolean isAnimEnd = true; //标识动画是否结束
	
	public static int mStatusBarH;
	
	public int mLock = 0;
	public int mDeleteLock = 0;
	
	private View mDeletedView;
//	private ObjectAnimator mTransAnim; //位置变换的属性动画
	
	private ArrayList<View> mItemViews;
	
	private View mSelectedView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tui_workspacemanager_layout);
		ImageButton imbtn = (ImageButton)findViewById(R.id.add_screen);
		imbtn.setOnClickListener(this);
		root = (RelativeLayout)findViewById(R.id.root);
		root.post(new GetStatusBarHeightRun());
		mConfig = new TuiThumbnailConfig(this);
		
		mWindowManager = (WindowManager)getWindowManager();
		
//		mTransAnim = ObjectAnimator.clone();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		isInMove = false;
		mLock = 0;
		mDeleteLock = 0;
		
		mConfig.onDestory();
		if (mItemViews != null){
			mItemViews.clear();
		}
	}
	
	@Override
	protected void onPause() {
		for(View view : mItemViews){
			view.clearAnimation();
		}
		restroeNailToNormal(false);
		isInMove = false;
		mMoveState = END_MOVE;
		
		super.onPause();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		
		int x = (int)ev.getX();
		int y = (int)ev.getY();
//		Log.d("tuixy", "x = " + x  + ", y = " + y);
		//还要处理多点触摸的问题
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			Log.d("tui", "ACTION_DOWN");
			
			isInMove = false;
			mSelectedIndex = mConfig.getIndex(x,y);
			mMovedToIndex = mSelectedIndex;
			
			mOld_x = x;
			mOld_y = y;
			break;
		case MotionEvent.ACTION_UP:
			Log.d("tui", "ACTION_UP");
			mMoveState = END_MOVE;
			restroeNailToNormal(true);
//			mWMLayouParams = null;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMoveState == ENTRY_MOVE){
				moveDragItem(x - mOld_x, y - mOld_y);
				exchangeItems(x,y);
				isInMove = true;
			}
			mOld_x = x;
			mOld_y = y;
			break;
		default:
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	/**
	 * Exchange itmes if necessary
	 * @param x the coordination of x
	 * @param y the coordination of y
	 */
	private void exchangeItems(int x, int y) {
		//这里lock的作用是：保证某次交换完成后，再进行下次交换
		//这里的一次是指exchangeToRight 或者 exchangeToLeft 执行一次
		if (mLock > 0){
			return ;
		}
		
		int dest = mConfig.getIndex(x,y); //获取到达的目的地，即目标索引值
//		int start = mSelectedIndex;
		
		int count = mConfig.getThumbnailCount();
		int itemSize = mItemViews.size();
		if (dest >= count || dest >= itemSize){
			return;
		}
		
//		Log.d("tui", "exchangeItems --- dest = " + dest + ", mMovedToIndex = " + mMovedToIndex);
		if (dest > mMovedToIndex){
			exchangeToRight(mMovedToIndex, dest);
			mMovedToIndex = dest;
		}else if (dest < mMovedToIndex){
			exchangeToLeft(mMovedToIndex, dest);
			mMovedToIndex = dest;
		}
	}

	/**
	 * exchange item selected with left one
	 * @param from
	 * @param dest
	 */
	private void exchangeToLeft(int from, int to) {
		
		final int fFrom = from;
		final int fTo = to;
		
		Log.d("tui", "\n" + "Entry exchangeToLeft from = " + from +", to = " + to);
		
		View toView = mItemViews.get(to);
		final int toLeft = toView.getLeft();
		final int toTop = toView.getTop();
		final int toRight = toView.getRight();
		final int toBbottom = toView.getBottom();
		
		boolean test = true;
		while(from > to){
			Log.d("tui", "ToLeft from = " + from +", to = " + (from - 1));

			View destV = mItemViews.get(from);//要移动到的view
			View movedV = mItemViews.get(from - 1);//要移动的view

			movedV.setVisibility(View.VISIBLE);
			PropertyValuesHolder pvLeft = PropertyValuesHolder.ofInt("Left", destV.getLeft());
			PropertyValuesHolder pvTop = PropertyValuesHolder.ofInt("top", destV.getTop());
			PropertyValuesHolder pvRight = PropertyValuesHolder.ofInt("right", destV.getRight());
			PropertyValuesHolder pvBottom = PropertyValuesHolder.ofInt("bottom", destV.getBottom());
//			PropertyValuesHolder pvTag = PropertyValuesHolder.ofObject("tag", new IntEvaluator(), destV.getTag());
//			ObjectAnimator obAnim = ObjectAnimator.ofPropertyValuesHolder(movedV, pvLeft, pvTop, pvRight, pvBottom, pvTag);
			ObjectAnimator obAnim = ObjectAnimator.ofPropertyValuesHolder(movedV, pvLeft, pvTop, pvRight, pvBottom);
			obAnim.addListener(new AnimatorListenerAdapter() {
				public void onAnimationEnd(Animator animation) {
					Log.d("tui", "ToLeft --- AnimationEnd");
					updateViewGroup(MOVE_TO_LEFT, fFrom, fTo);
				}
			});
			obAnim.setDuration(DURATION);
			obAnim.start();
			
			mLock ++;
			
			from --;
			
			if (test){//test部分代码
				//这里是将from的坐标设置为to的坐标，为连续的交换做准备
				//之所以用test限制，是只让其执行一次，
				//在这个地方执行是要保证在to的坐标改变之前运行，并且要保证在upDateViewGroup方法之前执行
				View fromV = mItemViews.get(fFrom);
				PropertyValuesHolder pvToLeft = PropertyValuesHolder.ofInt("Left", toLeft);
				PropertyValuesHolder pvToTop = PropertyValuesHolder.ofInt("top", toTop);
				PropertyValuesHolder pvToRight = PropertyValuesHolder.ofInt("right", toRight);
				PropertyValuesHolder pvToBottom = PropertyValuesHolder.ofInt("bottom", toBbottom);
//				PropertyValuesHolder pvToTag = PropertyValuesHolder.ofObject("tag", new IntEvaluator(), fTo);
//				ObjectAnimator obAnim2 = ObjectAnimator.ofPropertyValuesHolder(fromV, pvToLeft, pvToTop, pvToRight, pvToBottom, pvToTag);
				ObjectAnimator obAnim2 = ObjectAnimator.ofPropertyValuesHolder(fromV, pvToLeft, pvToTop, pvToRight, pvToBottom);
				
				obAnim2.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator animation) {
						Log.d("tui", "ToKLeft --- obAnim2 AnimationEnd");
						updateViewGroup(MOVE_TO_LEFT, fFrom, fTo);
					}
				});
				obAnim2.setDuration(DURATION);
				obAnim2.start();
				mLock ++;
				test = false;
			}
		}
		
		Log.d("tui", "Exit exchangeToLeft" + "\n\n");
	}

	/**
	 * exchange item selected with right one
	 * @param from
	 * @param to
	 */
	private void exchangeToRight(int from, int to) {
		
		final int fFrom = from;
		final int fTo = to;
		Log.d("tui", "\n" + "Entry exchangeToRight from = " + from +", to = " + to);
		
		View toView = mItemViews.get(to);
		final int toLeft = toView.getLeft();
		final int toTop = toView.getTop();
		final int toRight = toView.getRight();
		final int toBbottom = toView.getBottom();
		final RelativeLayout.LayoutParams toParam = (RelativeLayout.LayoutParams)toView.getLayoutParams();
		boolean test = true;
		while(from < to){
			Log.d("tui", "ToRight from = " + from +", to = " + (from + 1));
			
			View destV = mItemViews.get(from); //要移动到的view
			View movedV = mItemViews.get(from + 1); //要移动的view
			
			movedV.setVisibility(View.VISIBLE);
			PropertyValuesHolder pvLeft = PropertyValuesHolder.ofInt("Left", destV.getLeft());
			PropertyValuesHolder pvTop = PropertyValuesHolder.ofInt("top", destV.getTop());
			PropertyValuesHolder pvRight = PropertyValuesHolder.ofInt("right", destV.getRight());
			PropertyValuesHolder pvBottom = PropertyValuesHolder.ofInt("bottom", destV.getBottom());
//			PropertyValuesHolder pvTag = PropertyValuesHolder.ofObject("tag", new IntEvaluator(), destV.getTag());
//			ObjectAnimator obAnim = ObjectAnimator.ofPropertyValuesHolder(movedV, pvLeft, pvTop, pvRight, pvBottom, pvTag);
			ObjectAnimator obAnim = ObjectAnimator.ofPropertyValuesHolder(movedV, pvLeft, pvTop, pvRight, pvBottom);
			obAnim.addListener(new AnimatorListenerAdapter() {
				public void onAnimationStart(Animator animation){
					Log.d("tui", "ToRight --- AnimationStart");
				}
				
				public void onAnimationEnd(Animator animation) {
					Log.d("tui", "ToRight --- AnimationEnd");
					View fromV = mItemViews.get(fFrom);
					updateViewGroup(MOVE_TO_RIGHT, fFrom, fTo);
				}
			});
			obAnim.setDuration(DURATION);
			obAnim.start();
			
			mLock ++;
			
			from ++;
			if (test){//test部分代码
				//这里是将from的坐标设置为to的坐标，为连续的交换做准备
				//之所以用test限制，是只让其执行一次，
				//在这个地方执行是要保证在to的坐标改变之前运行，并且要保证在upDateViewGroup方法之前执行
				View fromV = mItemViews.get(fFrom);
				PropertyValuesHolder pvToLeft = PropertyValuesHolder.ofInt("Left", toLeft);
				PropertyValuesHolder pvToTop = PropertyValuesHolder.ofInt("top", toTop);
				PropertyValuesHolder pvToRight = PropertyValuesHolder.ofInt("right", toRight);
				PropertyValuesHolder pvToBottom = PropertyValuesHolder.ofInt("bottom", toBbottom);
//				PropertyValuesHolder pvToTag = PropertyValuesHolder.ofObject("tag", new IntEvaluator(), fTo);
//				ObjectAnimator obAnim2 = ObjectAnimator.ofPropertyValuesHolder(fromV, pvToLeft, pvToTop, pvToRight, pvToBottom, pvToTag);
				ObjectAnimator obAnim2 = ObjectAnimator.ofPropertyValuesHolder(fromV, pvToLeft, pvToTop, pvToRight, pvToBottom);
				
				obAnim2.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator animation) {
						Log.d("tui", "ToRight --- obAnim2 AnimationEnd");
						updateViewGroup(MOVE_TO_RIGHT, fFrom, fTo);
					}
				});
				obAnim2.setDuration(DURATION);
				obAnim2.start();
				mLock ++;
				test = false;
			}
		}
		
		/*
		 * 如果在这里执行上面test部分的代码，那么upDateViewGroup方法会先执行
		 * View temp = mItemViews.get(fFrom);
		 */
		
		Log.d("tui", "Exit exchangeToRight" + "\n\n");
	}
	
	/**
	 * 
	 * @param direction the moving direction of selected-item
	 * @param from the original index
	 * @param to the destination index
	 */
	protected void updateViewGroup(int direction, int from, int to) {
		//这里lock的作用是：保证某次交换中所有动画都结束之后，再更新ViewGroup
		mLock --;
		if (mLock > 0){
			return;
		}
		
		final int fFrom = from;
		final int fTo = to;
		
		Log.d("tui", "\n" + "Entry updateViewGroup");
		final View selectedView = mItemViews.get(from);
		if (mSelectedView.equals(selectedView)){
			Log.d("tui", "mSelectedView == selectedView");
		}
		Log.d("tui", "from = " + from + ",from_left = " + selectedView.getLeft() + ", from_top = " + selectedView.getTop());
		final View toView = mItemViews.get(to);
		Log.d("tui", "to = " + to + ",to_left = " + toView.getLeft() + ", to_top = " + toView.getTop());

		if (direction == MOVE_TO_LEFT){
			while(from > to){
				View fromView = mItemViews.get(from - 1);
				RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)fromView.getLayoutParams();
				param.leftMargin = mConfig.getMarginLeft(from);
				param.topMargin = mConfig.getMarginTop(from);
				Log.d("tui", "left befor --- from = " + (from - 1) + ", left = " + param.leftMargin + ", top = " + param.topMargin + ", tag = " + fromView.getTag());
//				fromView.setTag(from);
				fromView.clearAnimation();
				fromView.setLayoutParams(param);
				Log.d("tui", "left after --- from = " + (from - 1) + ", left = " + fromView.getLeft() + ", top = " + fromView.getTop() + ", tag = " + fromView.getTag());
				mItemViews.set(from, fromView);
//				Test(from);
				
/*				fromView.clearAnimation();
//				fromView.setTag(from);
				mItemViews.remove(from);
				mItemViews.add(from, fromView);*/
				
				from--;
			}
		}else{
			while(from < to){
				View fromView = mItemViews.get(from + 1);
				RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)fromView.getLayoutParams();
				param.leftMargin = mConfig.getMarginLeft(from);
				param.topMargin = mConfig.getMarginTop(from);
				Log.d("tui", "right befor --- from = " + (from - 1) + ", left = " + param.leftMargin + ", top = " + param.topMargin + ", tag = " + fromView.getTag());
//				fromView.setTag(from);
				fromView.clearAnimation();
				fromView.setLayoutParams(param);
				Log.d("tui", "right after --- from = " + (from - 1) + ", left = " + fromView.getLeft() + ", top = " + fromView.getTop() + ", tag = " + fromView.getTag());
				mItemViews.set(from, fromView);
				
/*				fromView.clearAnimation();
//				fromView.setTag(from);
				mItemViews.remove(from);
				mItemViews.add(from, fromView);*/
				
				from++;
			}
		}
		
		RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)selectedView.getLayoutParams();
		param.leftMargin = mConfig.getMarginLeft(to);
		param.topMargin = mConfig.getMarginTop(to);
		Log.d("tui", "to= " + to + ", to_left = " + param.leftMargin + ", to_top = " + param.topMargin);
//		selectedView.setTag(to);
		selectedView.clearAnimation();
		selectedView.setLayoutParams(param);
		mItemViews.set(to, selectedView);
		
		/*selectedView.clearAnimation();
		mItemViews.remove(to);
		mItemViews.add(to, selectedView);*/
		
		mConfig.updateThumbnails(direction, fFrom, fTo);
		
		// 由于动画需要一定的时间执行，所以可能会出现，动画还没有结束，即updateViewGroup这个方法还没有执行
		// up事件之后restroeNailToNormal方法就被调用执行，这时会产生异常。
		// 当出现上述情况时，restroeNailToNormal被阻止执行，但是为了保证
		// restroeNailToNormal方法执行，故增加以下代码
		if (mMoveState == END_MOVE){
			restroeNailToNormal(true);
		}
		
		Log.d("tui", "Exit updateViewGroup" + "\n\n");
	}

	/**
	 * 暂时没有用到改函数
	 * @param start
	 * @param direction 
	 * @param dest
	 * @return
	 */
	private Animation getTranslatAnim(int start, int dest) {
		final int fs = start;
		final int fd = dest;
//		Test(start, dest);
		
		View startV = root.getChildAt(start);
		View destV = root.getChildAt(dest);
		
		int dx = startV.getLeft() - destV.getLeft();
		int dy = startV.getTop() - destV.getTop();
		
		Log.d("tui", "dx = " +dx + ", dy = " + dy);
		/*PropertyValuesHolder xshift = PropertyValuesHolder.ofFloat("x", destV.getLeft(), startV.getLeft());
		PropertyValuesHolder yshift = PropertyValuesHolder.ofFloat("y", destV.getTop(), startV.getTop());
		
		mTransAnim = ObjectAnimator.ofPropertyValuesHolder(destV,
				xshift, yshift);
		
		mTransAnim.addListener(new AnimatorListenerAdapter(){
			public void onAnimationEnd(Animator animation) {
				Test(fs, fd);
			}
		});
		
		mTransAnim.setDuration(DURATION);
		mTransAnim.start();*/
		
		Animation anim = new TranslateAnimation(0, dx, 0, dy);
		
		return anim;
	}
	
	private void Test(int start, int dest) {
		View view = mItemViews.get(dest);
		int x = view.getLeft();
		int y = view.getTop();
		
		Log.d("tui", "Test --  x = " + x + ", y = " + y);
	}

	private void Test(int dest) {
		View view = mItemViews.get(dest);
		int x = view.getLeft();
		int y = view.getTop();
		
		Log.d("tui", "Test --  x = " + x + ", y = " + y);
	}
	
	private void TestForAll() {
		for (int i = 0; i< mItemViews.size(); i++){
			//输出tag
			View view = mItemViews.get(i);
//			Log.d("tui", "TestForAll ---  child_" + i + ", tag = "  + view.getTag());
		}
	}
	
	private void moveDragItem(int dx, int dy) {
		
		if (mDragView != null){
			int itemW = mConfig.getNailWidth();
			int itemH = mConfig.getNailHeight();
			
			mWMLayouParams.x += dx;
			mWMLayouParams.y += dy;
			mWindowManager.updateViewLayout(mDragView, mWMLayouParams);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		Log.d("tui", "onLongClick");
		/*长按的时候会先出发Down事件,然后一直触发Move事件,
		 * 到了长按的时间后,就触发LongClick,然后就一直是Move事件*/
		int count = mItemViews.size();
		if (mSelectedIndex >= count){
			return false;
		}
		
		View nail = mItemViews.get(mSelectedIndex);
		
		Animation anim = getAnimation(ANIM_LONG_CLICK);
//		anim.setFillAfter(true);//不要设置为true，否则开始拖拽时， 动画结束的最后一帧会一直保留
		nail.startAnimation(anim);
		
		Vibrator vib = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
		vib.vibrate(40);
		
		return true;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		//先执行down和up事件，在执行onClick事件
		Log.d("tui", "Entry onClick");
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		switch(id){
		case R.id.screens_delete:
			int size = mItemViews.size();
			if (size == MIN_ITEM_COUNT){
				Toast.makeText(
						this,
						R.string.tinnoui_keep_one_screen,
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			final View v1 = v;
			AlertDialog dialog = new AlertDialog.Builder(this)
							.setMessage(R.string.tui_delete_workspace)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									deleteItem(v1);
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.create();
			dialog.show();		
//			deleteItem(v1);
			break;
		case R.id.screens_home_img:
			Log.d("tui", "R.id.screens_home_img");
			int defaulScr = AlmostNexusSettingsHelper.getDefaultScreen(null);
			View view = mItemViews.get(defaulScr);
			ImageView homeImg = (ImageView)view.findViewById(R.id.screens_home_img);
			homeImg.setBackgroundResource(R.drawable.preview_home_btn);
			homeImg.invalidate();
			
			int newdefaulScr = mConfig.getIndex(x, y);
			if (newdefaulScr >= mConfig.getThumbnailCount() ||
					newdefaulScr >= mItemViews.size()){
				return ;
			}
			ImageView homeNewImg = (ImageView)v.findViewById(R.id.screens_home_img);
			homeNewImg.setBackgroundResource(R.drawable.preview_home_btn_light);
			homeNewImg.invalidate();
			
			AlmostNexusSettingsHelper.setDefaultScreen(null,newdefaulScr);
			break;
		case R.id.add_screen:
			addItem();
			break;
		default:
			final int index = mConfig.getIndex(x, y);
			if (index < mConfig.getThumbnailCount()){
				Log.d("tui", "click thumbnail " + index);
			}
			//for yutao SNTZOTIU-41 2013.8.29
			final Workspace work = Launcher._instance.getWorkspace();
			Looper.myQueue().addIdleHandler(new IdleHandler() {
				public boolean queueIdle() {
					work.setCurrentPage(index);
					return false;
				}
			});
			
			finish();
			break;
		}
		
		Log.d("tui", "Exit onClick");
	}
	
	private void addItem() {
		
		int count = mItemViews.size();
		if (count == MAX_ITEM_COUNT){
			Toast.makeText(this, R.string.tui_max_count_worksapce, Toast.LENGTH_SHORT).show();
			return;
		}
		
//		int c1 = mItemViews.size();
		mConfig.addCellLayout();
		View view = mConfig.getLatesNailView();
//		int c2 = mItemViews.size();
		
		view.setOnClickListener(this);
		view.setOnLongClickListener(this);
		ImageView screens_home_img = (ImageView)view.findViewById(R.id.screens_home_img);
		screens_home_img.setOnClickListener(this);
		ImageView screens_delete = (ImageView)view.findViewById(R.id.screens_delete);
		screens_delete.setOnClickListener(this);
		
		//先将view加到viewgroup中
		root.addView(view);
		//再进行动画
		Animation scaleAnim1 = new ScaleAnimation(
						0.0f, SCALE_TO, 
						0.0f, SCALE_TO, 
						Animation.RELATIVE_TO_SELF, 0.5f, 
						Animation.RELATIVE_TO_SELF, 0.5f);
		Animation scaleAnim2 = new ScaleAnimation(
						SCALE_TO, 1.0f, 
						SCALE_TO, 1.0f, 
						Animation.RELATIVE_TO_SELF, 0.5f, 
						Animation.RELATIVE_TO_SELF, 0.5f);
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(scaleAnim1);
		animSet.addAnimation(scaleAnim2);
		animSet.setDuration(DURATION);
		animSet.setAnimationListener(new MyAnimationListener(ANIM_SET_ADD_ITEM));
		
		view.setAnimation(animSet);
		animSet.startNow();
		
	}

	protected void deleteItem(final View v) {
		
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		
		int index = mConfig.getIndex(x, y);
		Log.d("tui", "R.id.screens_delete -- x = " + x + ", y = "  + y + ", index = " + index);
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.tui_wp_manager_delete);
		anim.setFillAfter(true);
		anim.setDuration(DURATION);
		MyAnimationListener listener = new MyAnimationListener(ANIM_DELETE_ITEM);
		listener.setIndex(index);
		anim.setAnimationListener(listener);
		View view = mItemViews.get(index);
		view.startAnimation(anim);
		
//		mDeletedView = view;//root.getChildAt(index);
		root.removeView(view);
		updateLayoutAfterDel(index);
	}

	public void getWorkSpaceSnaps() {
		
		if (mItemViews == null){
			mItemViews  = new ArrayList<View>();
			//这步操作将mNailViews 与 mItemViews绑定在了一起，即这两个是统一体
			mItemViews = mConfig.getNailViews(this, this, this);
		}
		
		for(View view : mItemViews){
			root.addView(view);
		}
	}
	
	private Animation getAnimation(int anim) {
		
		Animation animation = null;
		int animId = 0;
		MyAnimationListener animListener;
		
		switch(anim){
		case ANIM_LONG_CLICK:
			animId = R.anim.tui_longclick_anim;
			animListener = new MyAnimationListener(ANIM_LONG_CLICK);
			break;
		case ANIM_LONG_CLICK_DROP:
			animId = R.anim.tui_longclick_drop_anim;
			animListener = new MyAnimationListener(ANIM_LONG_CLICK_DROP);
			break;
		default:
			animId = -1;
			animListener = new MyAnimationListener(ANIM_NONE);
			break;
		}
		
		animation = AnimationUtils.loadAnimation(this, animId);
		animation.setAnimationListener(animListener);
		return animation;
	}
	
	private void restroeNailToNormal(boolean anima) {
		
		// 由于动画需要一定的时间执行，所以可能会出现，动画还没有结束，即updateViewGroup这个方法还没有调用
		// 就执行了该方法，这时会产生异常。
		// 这里lock的作用就是防止上面情况的发生
		if(mLock > 0){
			Log.d("tui", "restroeNailToNormal wil be called after the animatoin is end!");
			return;
		}
		
		//没有产生移动的话，就不执行
		if(!isInMove){
			return;
		}
		
		Log.d("tui", "\n" + "Entry restroeNailToNormal");
		if(mSelectedIndex >= 0 && mSelectedIndex < root.getChildCount()){
			
			if (mDragView != null){
				mWindowManager.removeView(mDragView);
				mDragView = null;
			}
			View view = mItemViews.get(mMovedToIndex);
			if (anima){
				view.setVisibility(View.INVISIBLE);
				if (mSelectedView.equals(view)){
					Log.d("tui", "mSelectedView == view");
				}
				if (view == null ){
					Log.d("tui", "restroeNailToNormal --- view = null");
				}
				if (mWMLayouParams == null){
					Log.d("tui", "restroeNailToNormal --- mWMLayouParams = null");
				}
				int dx = view.getLeft() - mWMLayouParams.x;
				int dy = view.getTop()  - mWMLayouParams.y;
				
				Animation anim = new TranslateAnimation(-dx, 0, -dy, 0);
				anim.setFillAfter(true);
				anim.setDuration(DURATION);
				anim.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						updateRoot();
					}
				});
				view.startAnimation(anim);
			}else{
				view.setVisibility(View.VISIBLE);
			}
		}
		
		mConfig.updateWorkSpace(mSelectedIndex, mMovedToIndex);
		Log.d("tui", "Exit restroeNailToNormal" + "\n\n");
	}
	
	protected void updateRoot() {
/*		root.removeAllViews();
		
		for (int i = 0; i < mItemViews.size(); i ++){
			View view = mConfig.getViewAt(i);
			root.addView(view, i);
		}
		root.requestLayout();
//		root.invalidate();
*/		
		
		/*root.removeViewAt(mMovedToIndex);
		View child = mConfig.getViewAt(mMovedToIndex);
		root.addView(child, mMovedToIndex);*/
	}

	public void addDragViewToWM() {
		
		if (mWMLayouParams == null){
			mWMLayouParams = new WindowManager.LayoutParams();
		}
		
		mWMLayouParams.gravity = Gravity.TOP | Gravity.LEFT;
		
		int[] location = new int[2];
		View clickView = mItemViews.get(mSelectedIndex);//mConfig.getNailView(mSelectedIndex);
		clickView.getLocationOnScreen(location);
		
		mWMLayouParams.x = location[0];
		mWMLayouParams.y = location[1] - mStatusBarH;
		Log.d("tui", "addDragViewToWM: location x=" + location[0] + ", location y=" + location[1]);
		mWMLayouParams.width = (int)(clickView.getWidth() * 1.05f);//* 1.04f
		mWMLayouParams.height = (int)(clickView.getHeight() * 1.05f);//* 1.08f
		mWMLayouParams.format = PixelFormat.TRANSLUCENT;
		
		if (mDragView != null){
			mWindowManager.removeView(mDragView);
		}
		
		mDragView = mConfig.getViewForDrag(mSelectedIndex);//createDragView(clickView);
		mWindowManager.addView(mDragView, mWMLayouParams);

		clickView.setVisibility(View.GONE);
		
		mMoveState = ENTRY_MOVE;
		mSelectedView = clickView;
	}
	
	private View createDragView(View clickView) {
		View convertView = LayoutInflater.from(this).inflate(
				R.layout.screens_add_item, null);
		
		return null;
	}

	/*获取状态栏的高度*/
	class GetStatusBarHeightRun implements Runnable {
		
		public void run() {
			
			Rect rect = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
			int statusBarH = rect.top;
			mStatusBarH = statusBarH;
			mConfig.saveStatusBarH(statusBarH);
			getWorkSpaceSnaps();
		}
	}

	
	private class MyAnimationListener implements AnimationListener{
		
		private int mAnimType;
		
		private int mIndex;
		
		public MyAnimationListener(int type){
			mAnimType = type;
			mIndex = -1;
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			switch(mAnimType){
			case ANIM_LONG_CLICK_DROP:
				Log.d("tui", "ANIM_LONG_CLICK_DROP end");
				break;
			case ANIM_LONG_CLICK:
				Log.d("tui", "ANIM_LONG_CLICK end");
				addDragViewToWM();
				break;
			case ANIM_DELETE_ITEM:
				Log.d("tui", "ANIM_DELETE_ITEM");
//				try{
//					//防止之前的删除操作还没有完全结束，又进行了一次删除。
//					//以免造成混乱
//					/*if (mDeleteLock > 0){
//						return;
//					}*/
//					deleteWorkspaceCell(mIndex);
//					updateLayout(mIndex);
//				}catch (ArrayIndexOutOfBoundsException e){
//					e.printStackTrace();
//				}
				
				break;
			case ANIM_SET_ADD_ITEM:
				addWorkspaceCell();
				break;
			default:
				break;
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		public void setIndex(int index){
			mIndex = index;
		}
	}


	public void deleteWorkspaceCell(int index) throws ArrayIndexOutOfBoundsException{
		Log.d("tui", "Entry deleteWorkspaceCell");
		Log.d("tui", "index = " + index + ", size = " + mItemViews.size());
		
		int count = mConfig.getThumbnailCount();
		if (index < 0 || index >= count){
			throw new ArrayIndexOutOfBoundsException("Invalid index " + index + ", size = " + count);
		}
		
		mConfig.deleteData(index);
		
		final Workspace workspace = Launcher._instance.getWorkspace();
		
		Log.d("tui", "deleteWorkspaceCell -- index = " + index +" , ChildCount() = " + workspace.getChildCount());
		if (index >= workspace.getChildCount()){
			throw new ArrayIndexOutOfBoundsException("deleteWorkspaceCell --- size = " + workspace.getChildCount() + 
					", index = " + index);
		}
		
		if (workspace.getChildCount() > 1){
			workspace.TuiRemoveScreen(index);
			int current = workspace.getCurrentPage();
			Log.d("tui", "index = " + index + ", cureentPage = " + current);
			if (current >= index){
				workspace.setCurrentPage(current - 1);
			}
//			workspace.moveToDefaultScreen(true);
			workspace.requestLayout();
		}else {
			Toast.makeText(
					this,
					R.string.tinnoui_keep_one_screen,
					Toast.LENGTH_SHORT).show();
		}
		Log.d("tui", "Exit deleteWorkspaceCell");
	}
	
	/**
	 * 
	 * @param index the deleted-item's index
	 */
	public void updateLayoutAfterDel(final int index) {
		Log.d("tui", "updateLayoutAfterDel -- index = " + index);
//		mItemViews.get(index).setVisibility(View.GONE);
		int maxindex = mItemViews.size() - 1;
		
		if ((maxindex - index) == 0){
			mItemViews.remove(index);
			updateDefaultScreen(index);
			deleteWorkspaceCell(index);
			mConfig.Helper();
			return ;
		}
		
		Log.d("tui", "Entry --- updateLayout");
		int startIndex = index;
		//比如现在有7项，index为3, 那么要将4,5,6 3项各往前移动一位
		while (maxindex - startIndex > 0){
			//将movedV 移到 destV的位置上
			Log.d("tui", "updateLayoutAfterDel -- statrtIndex = " + startIndex);
			View destV = mItemViews.get(startIndex);
			View movedV = mItemViews.get(startIndex + 1);
			
			Log.d("tui", "mItemViews --- desetV left = " + destV.getLeft() + 
					", top = " + destV.getTop() + 
					", right = " + destV.getRight() +
					", bottom = " + destV.getBottom());
			Log.d("tui", "root      --- movedV left = " + movedV.getLeft() + 
					", top = " + movedV.getTop() + 
					", right = " + movedV.getRight() +
					", bottom = " + movedV.getBottom());
			movedV.setVisibility(View.VISIBLE);// 让每个移动的item都是可见的
			PropertyValuesHolder pvLeft = PropertyValuesHolder.ofInt("Left", destV.getLeft());
			PropertyValuesHolder pvTop = PropertyValuesHolder.ofInt("top", destV.getTop());
			PropertyValuesHolder pvRight = PropertyValuesHolder.ofInt("right", destV.getRight());
			PropertyValuesHolder pvBottom = PropertyValuesHolder.ofInt("bottom", destV.getBottom());
			ObjectAnimator obAnim = ObjectAnimator.ofPropertyValuesHolder(movedV, pvLeft, pvTop, pvRight, pvBottom);
			obAnim.addListener(new AnimatorListenerAdapter() {
				public void onAnimationStart(Animator animation){
					Log.d("tui", "updateLayout --- AnimationStart");
				}
				
				public void onAnimationEnd(Animator animation) {
					Log.d("tui", "updateLayout --- AnimationEnd");
					mDeleteLock --;
					if (mDeleteLock == 0 ){
						mItemViews.remove(index);
						Log.d("tui", "mItemViews size =" + mItemViews.size() + " , root size = " + root.getChildCount());
						updateParamsAfterDel(index);
						updateDefaultScreen(index);
						deleteWorkspaceCell(index);
					}
				}
			});
			obAnim.setDuration(DURATION);
			obAnim.start();
			
			mDeleteLock ++;
			startIndex ++;
		}
		
		Log.d("tui", "Exit --- updateLayout");
	}

	private void updateParamsAfterDel(int index){
		
		Log.d("tui", "Entry updateParamsAfterDel");
		Log.d("tui", "index = " + index);
		final int size = mItemViews.size();
		if (index >= size){
			Log.e("tui", "Invalid index " + index + ",  the size is " + size);
		}
		
		while(index < size){
			
			View view = mItemViews.get(index);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();
			params.leftMargin = mConfig.getMarginLeft(index);
			params.topMargin  = mConfig.getMarginTop(index);
			view.clearAnimation();
			view.setLayoutParams(params);
			
			index ++;
		}
		
		Log.d("tui", "Exit updateParamsAfterDel");
	}
	
	private void updateDefaultScreen(final int index){
		int defaultScr = AlmostNexusSettingsHelper.getDefaultScreen(null);
		Log.d("tui", "updateDefaultScreen --- index = " + index + ", defaultScr = " + defaultScr);
		if (index == defaultScr){
			View view = mItemViews.get(0);
			ImageView homeImg = (ImageView)view.findViewById(R.id.screens_home_img);
			homeImg.setBackgroundResource(R.drawable.preview_home_btn_light);
			homeImg.invalidate();
		}
	}
	
	public void deleteFromRoot(int index) {
		Log.d("tui", "Entry deleteFromRoot");
//		mItemViews.remove(index);
//		root.removeViewAt(index);
//		root.requestLayout();
//		View child = root.getChildAt(index);
//		child.clearAnimation();
		Log.d("tui", "Exit deleteFromRoot");
	}

	public void addWorkspaceCell() {
		mConfig.addWorkspaceCell();
	}
	
}
