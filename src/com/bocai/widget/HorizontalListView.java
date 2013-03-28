package com.bocai.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.*;

import java.util.*;

import com.sonyericsson.util.Dynamics;


@SuppressWarnings({ "rawtypes" })
public class HorizontalListView extends AdapterView {

	public HorizontalListView(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		mTouchState = 0;
		mItemSpacing = 0;
		mCachedItemViews = new LinkedList();
		mLastSnapPos = 0x80000000;
		mFooterViewInfos = new ArrayList();
		mFooterViewSet = new HashSet();
	}

	private void addAndMeasureChild(View view, int i) {
		Log.i(LOG_TAG, "addAndMeasureChild method");
		LayoutParams layoutparams = view.getLayoutParams();
		if (layoutparams == null) {
			layoutparams = generateDefaultLayoutParams();
			view.setLayoutParams(layoutparams);
		}
		
		int j;
		int k;
		int l;
		int i1;
		int j1;
		int k1;
		int l1;
		int i2;
		int j2;
		if (i == 1)
			j = 0;
		else
			j = -1;
		view.setDrawingCacheEnabled(true);

		addViewInLayout(view, j, layoutparams, true);
		l = getPaddingTop();
		i1 = getPaddingBottom();
		j1 = l + i1;
		k1 = layoutparams.height;
		l1 = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec, j1, k1);
		i2 = layoutparams.width;
		if (i2 > 0)
			j2 = MeasureSpec.makeMeasureSpec(i2, 0x40000000);
		else
			j2 = MeasureSpec.makeMeasureSpec(0, 0);
		view.measure(j2, l1);
	}

	private void clickChildAt(int i, int j) {
		int k = getContainingChildIndex(i, j);
		if (k == -1) {
			return;
		} else {
			View view = getChildAt(k);
			int l = mFirstItemPosition + k;
			long l1 = mAdapter.getItemId(l);
			performItemClick(view, l, l1);
			return;
		}
	}

	private final int computeLeftRight(int i, boolean flag) {
		int j1;
		int k1;
		int l1;
		int j = getPaddingLeft();
		int k = mListLeft;
		int l = j + k;
		int i1 = mListLeftOffset;
		j1 = l + i1;
		k1 = getChildCount();
		l1 = 0;

		while (true) {
			int i2;
			int j2;
			if (l1 >= k1) {
				break;
			}
			View view = getChildAt(l1);
			i2 = view.getMeasuredWidth();
			j2 = getChildMargin(view);
			if (l1 == i) {
				int l2 = j2 * 2 + i2;
				j1 += l2;
				l1++;
			} else {
				int k2;
				if (flag)
					k2 = j2 * 2 + j1 + i2;
				else
					k2 = j1 + j2;
				return k2;
			}
		}
		return j1;
	
	}

	private void endTouch(float f) {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
		removeCallbacks(mLongPressRunnable);
		if (mDynamicsRunnable == null) {
			mDynamicsRunnable = new Runnable() {

				public void run() {
					if (mDynamics == null)
						return;
					View view = getChildAt(0);
					int i = getChildLeft(view);
					int j = mListLeftOffset;
					int k = i - j;
					mListLeftStart = k;
					long l1 = AnimationUtils.currentAnimationTimeMillis();
					mDynamics.update(l1);
					int j1 = (int) mDynamics.getPosition();
					int k1 = mListLeftStart;
					int i2 = j1 - k1;
					scrollList(i2);
					if (mDynamics.isAtRest(0.5F, 0.4F)) {
						return;
					} else {
						postDelayed(this, 16L);
						return;
					}
				}
			};
		}
		if (mDynamics != null) {
			float f1 = mListLeft;
			long l = AnimationUtils.currentAnimationTimeMillis();
			mDynamics.setState(f1, f, l);
			post(mDynamicsRunnable);
		}
		mTouchState = 0;
	}

	private void fillList(int i) {
	//	Log.i("HorizontalListView","--------fillList i-------- " + i);
		int j = getChildCount() - 1;
	//	Log.i("HorizontalListView","--------fillList j-------- " + j);
		View view = getChildAt(j);
		int k = getChildRight(view);
	//	Log.i("HorizontalListView","--------fillList k-------- " + k);
		fillListRight(k, i);
		View view1 = getChildAt(0);
		int l = getChildLeft(view1);
	//	Log.i("HorizontalListView","--------fillList l-------- " + l);
		fillListLeft(l, i);
	}

	private void fillListLeft(int i, int j) {
		do {
		//	Log.i("HorizontalListView",
		//			"!!!!!!!!!!!!!!!!!fillListLeft i,j,mFirstItemPosition $$$$$$$$$$$$$$$$ " + i + "," + j + "," + mFirstItemPosition);

			if (i + j <= 0)
				return;
			if (mFirstItemPosition <= 0)
				return;
			mFirstItemPosition--;
			View view = getCachedView();
			View view1 = mAdapter.getView(mFirstItemPosition, view, this);
			addAndMeasureChild(view1, 1);
			int i1 = getChildWidth(view1);
			i -= i1;
			mListLeftOffset = mListLeftOffset - i1;
		} while (true);
	}

	private void fillListRight(int i, int j) {
		/*		Log.i("HorizontalListView",
		"$$$$$$$$$$$$$$ i, j $$$$$$$$$$$$$$$$ " + i + "," + j);
	*/	do {
			View view1;
			int i2;
			do {
/*				Log.i("HorizontalListView",
						"$$$$$$$$$$$$$$fillListRight$$$$$$$$$$$$$$$$");
			*/	int k = i + j;
				int l = getWidth();
				if (k >= l) {
/*					Log.i("HorizontalListView",
					"$$$$$$$$$$$$$$fillListRight k >= l $$$$$$$$$$$$$$$$");*/
					return;
				}
				int i1 = mLastItemPosition;
				int j1 = mAdapter.getCount() - 1;
				if (i1 >= j1) {
/*					Log.i("HorizontalListView",
					"$$$$$$$$$$$$$$fillListRight i1 >= j1 $$$$$$$$$$$$$$$$");*/
					return;
				}
				int k1 = mLastItemPosition + 1;
				mLastItemPosition = k1;

				int l1 = mLastItemPosition;
				View view = getCachedView();
				view1 = mAdapter.getView(l1, view, this);
		        //Log.i("HorizontalListView", "!!!!!!!!!!!!!!!! view.getTag(R.id.key_objects) " + view1.getTag(R.id.key_objects));

				addAndMeasureChild(view1, 0);
				i2 = getChildWidth(view1);
				i += i2;
/*				Log.i("HorizontalListView",
				"$$$$$$$$$$$$$$ fillListRight while (i + j >= 0); $$$$$$$$$$$$$$$$");*/
			} while (i + j >= 0);
			removeViewInLayout(view1);
			if (shouldCacheView(mLastItemPosition))
				mCachedItemViews.addLast(view1);
			mFirstItemPosition = mFirstItemPosition + 1;
			mListLeftOffset = mListLeftOffset + i2;
		} while (true);
	}

	private View getCachedView() {
		View view;
		if (mCachedItemViews.size() != 0)
			view = (View) mCachedItemViews.removeFirst();
		else
			view = null;
		return view;
	}

	private int getChildLeft(View view) {
		int i = view.getLeft();
		int j = getChildMargin(view);
		return i - j;
	}

	private int getChildMargin(View view) {
		return mItemSpacing / 2;
	}

	private int getChildRight(View view) {
		int i = view.getRight();
		int j = getChildMargin(view);
		return i + j;
	}

	private int getChildWidth(View view) {
		int i = view.getMeasuredWidth();
		int j = getChildMargin(view) * 2;
		return i + j;
	}

	private int getContainingChildIndex(int i, int j) {
		int k;
		if (mRect == null) {
			Rect rect = new Rect();
			mRect = rect;
		}
		k = 0;

		int l = getChildCount();
		while (true) {
			if (k >= l) {
				break;
			}
			View view = getChildAt(k);
			view.getHitRect(mRect);
			if (!mRect.contains(i, j)) {
				k++;
			} else {
				return k;
			}
		}

		return -1;
		/*
		 * _L3: int l = getChildCount(); if(k >= l) break
		 * MISSING_BLOCK_LABEL_85; View view = getChildAt(k); Rect rect1 =
		 * mRect; view.getHitRect(rect1); if(!mRect.contains(i, j)) goto _L2;
		 * else goto _L1 _L1: int i1 = k; _L4: return i1; _L2: k++; goto _L3 i1
		 * = -1; goto _L4
		 */
		// return 0;
	}

	private void longClickChild(int i) {
		View view = getChildAt(i);
		int j = mFirstItemPosition + i;
		long l = mAdapter.getItemId(j);
		android.widget.AdapterView.OnItemLongClickListener onitemlongclicklistener = getOnItemLongClickListener();
		if (onitemlongclicklistener == null) {
			return;
		} else {
			HorizontalListView horizontallistview = this;
			onitemlongclicklistener.onItemLongClick(
					horizontallistview, view, j, l);
			return;
		}
	}

	private void positionItems() {
						
		int i = getPaddingLeft();
		int j = mListLeft;
		int k = i + j;
		int l = mListLeftOffset;
		int i1 = k + l;
		int i2 = getHeight();
		int i3 = getChildCount();
		int i4 = 0;
		
		while(i4 < i3){
			View view = getChildAt(i4);
			int i5 = view.getMeasuredWidth();
			int i6 = view.getMeasuredHeight();
			int i7 = (i2 - i6) / 2;
			int i8 = getChildMargin(view); 
			int i9 = i1 + i8;
			int i10 = i9 + i5;
			int i11 = i7 + i6;
			
			view.layout(i9, i7, i10, i11);
		
			int i12 = i8 * 2 + i5;
			i1 += i12;
			i4 += 1;
		}
		 ListAdapter localListAdapter = this.mAdapter;
		 int i13 = this.mFirstItemPosition;
		 long l1 = localListAdapter.getItemId(i13);
		 this.mFirstItemId = l1;
	
	}

	private void removeFixedViewInfo(View view, ArrayList arraylist) {
		int i = arraylist.size();
		int j = 0;
		do {
			if (j >= i)
				return;
			if (((FixedViewInfo) arraylist.get(j)).view == view) {
				Object obj = arraylist.remove(j);
				return;
			}
			j++;
		} while (true);
	}

	private void removeNonVisibleViews(int i) {
		int j = getChildCount();
		int k = mLastItemPosition;
		int l = mAdapter.getCount() - 1;
		if (k != l && j > 1) {
			for (View view = getChildAt(0); view != null
					&& getChildRight(view) + i < 0;) {
				removeViewInLayout(view);
				j--;
				int i1 = mFirstItemPosition;
				if (shouldCacheView(i1))
					mCachedItemViews.addLast(view);
				int j1 = mFirstItemPosition + 1;
				mFirstItemPosition = j1;
				int k1 = mListLeftOffset;
				int l1 = getChildWidth(view);
				int i2 = k1 + l1;
				mListLeftOffset = i2;
				if (j > 1)
					view = getChildAt(0);
				else
					view = null;
			}

		}
		if (mFirstItemPosition == 0)
			return;
		if (j <= 1)
			return;
		int j2 = j - 1;
		View view1 = getChildAt(j2);
		do {
			if (view1 == null)
				return;
			int k2 = getChildLeft(view1) + i;
			int l2 = getWidth();
			if (k2 <= l2)
				return;
			removeViewInLayout(view1);
			j--;
			int i3 = mLastItemPosition;
			if (shouldCacheView(i3))
				mCachedItemViews.addLast(view1);
			int j3 = mLastItemPosition - 1;
			mLastItemPosition = j3;
			if (j > 1) {
				int k3 = j - 1;
				view1 = getChildAt(k3);
			} else {
				view1 = null;
			}
		} while (true);
	}

	private void scrollList(int i) {
		int j = mListLeftStart + i;
		mListLeft = j;
		setSnapPoint();
		requestLayout();
	}

	private void setSnapPoint() {
		int i = getChildCount() - 1;
		int j = getWidth();
		if (mLastSnapPos != 0x80000000)
			return;
		int k = mLastItemPosition;
		int l = mAdapter.getCount() - 1;
		if (k != l)
			return;
		View view = getChildAt(i);
		if (getChildRight(view) >= j) {
			return;
		} else {
			int i1 = computeLeftRight(i, true);
			int j1 = mListLeft;
			int k1 = getPaddingRight();
			int l1 = j - k1 - i1;
			int i2 = j1 + l1;
			mLastSnapPos = i2;
			Dynamics dynamics = mDynamics;
			float f = mLastSnapPos;
			dynamics.setMinPosition(f);
			return;
		}
	}

	private boolean shouldCacheView(int i) {
		boolean flag;
		if (mAdapter.getItemViewType(i) != -1)
			flag = true;
		else
			flag = false;
		return flag;
	}

	private void startLongPressCheck() {
		if (mLongPressRunnable == null) {
			mLongPressRunnable = new Runnable() {

				public void run() {
					if (mTouchState != 1)
						return;
					HorizontalListView horizontallistview = HorizontalListView.this;
					int k = horizontallistview.getContainingChildIndex(mTouchStartX, mTouchStartY);
					if (k == -1) {
						return;
					} else {
						longClickChild(k);
						return;
					}
				}
			};
		}
		long l = ViewConfiguration.getLongPressTimeout();
		postDelayed(mLongPressRunnable, l);
	}

	private boolean startScrollIfNeeded(MotionEvent motionevent) {
		int i;
		int j;
		int k;
		i = (int) motionevent.getX();
		j = (int) motionevent.getY();
		k = mTouchStartX - 10;
		if (i < k) {
			removeCallbacks(mLongPressRunnable);
			mTouchState = 2;
			return true;
		} else {
			int l = mTouchStartX + 10;
			if (i > l) {

				removeCallbacks(mLongPressRunnable);
				mTouchState = 2;
				return true;
			} else {
				int i1 = mTouchStartY - 10;
				if (j < i1) {
					removeCallbacks(mLongPressRunnable);
					mTouchState = 2;
					return true;
				} else {
					int j1 = mTouchStartY + 10;
					if (j <= j1) {
						return false;
					} else {
						removeCallbacks(mLongPressRunnable);
						mTouchState = 2;
						return true;
					}
				}
			}
		}

		/*
		 * if(i < k) goto _L2; else goto _L1 _L1: int l = mTouchStartX + 10;
		 * if(i > l) goto _L2; else goto _L3 _L3: int i1 = mTouchStartY - 10;
		 * if(j < i1) goto _L2; else goto _L4 _L4: int j1 = mTouchStartY + 10;
		 * if(j <= j1) goto _L5; else goto _L2 _L2: boolean flag1; Runnable
		 * runnable = mLongPressRunnable; boolean flag =
		 * removeCallbacks(runnable); mTouchState = 2; flag1 = true; _L7: return
		 * flag1; _L5: flag1 = false; if(true) goto _L7; else goto _L6 _L6:
		 */
		// return true;
	}

	private void startTouch(MotionEvent motionevent) {
		removeCallbacks(mDynamicsRunnable);
		mTouchStartX = (int) motionevent.getX();
		mTouchStartY = (int) motionevent.getY();
		View view = getChildAt(0);
		if (view == null) {
			return;
		} else {
			int k = getChildLeft(view);
			int l = mListLeftOffset;
			mListLeftStart = k - l;
			startLongPressCheck();
			mVelocityTracker = VelocityTracker.obtain();
			mVelocityTracker.addMovement(motionevent);
			mTouchState = 1;
			return;
		}
	}

	public void addFooterView(View view) {
		
		addFooterView(view, null, true);
	}

	public void addFooterView(View view, Object obj, boolean flag) {
		Log.i(LOG_TAG, "addFooterView method");
		FixedViewInfo fixedviewinfo = new FixedViewInfo();
		fixedviewinfo.view = view;
		fixedviewinfo.data = obj;
		fixedviewinfo.isSelectable = flag;
		mFooterViewInfos.add(fixedviewinfo);
		mFooterViewSet.add(view);
		if (mDataSetObserver == null) {
			return;
		} else {
			mDataSetObserver.onChanged();
			return;
		}
	}

	@Override
	protected boolean drawChild(Canvas canvas, View view, long l) {
		
		android.graphics.Bitmap bitmap = view.getDrawingCache();
		boolean flag;
		if (bitmap == null) {
			flag = super.drawChild(canvas, view, l);
		} else {
			if (mPaint == null) {
				Paint paint = new Paint();
				mPaint = paint;
				mPaint.setAntiAlias(true);
				mPaint.setFilterBitmap(true);
			}
			if (mChildTransformation == null) {
				Transformation transformation = new Transformation();
				mChildTransformation = transformation;
			}
			int i = canvas.save();
			Object obj = null;
			boolean hasObject = false;
			Transformation transformation1 = mChildTransformation;
			if (getChildStaticTransformation(view, transformation1)) {
				int j = mChildTransformation.getTransformationType();
				int k = Transformation.TYPE_IDENTITY;

				if (j != k){
					obj = mChildTransformation;
					hasObject = true;
				}else{
					obj = null;
					hasObject = false;
				}
				if ((Transformation.TYPE_MATRIX & j) != 0){
					hasObject = true;
				}
				else{
					hasObject = false;
				}
			}
			float f;
			float f1;
			float f2;
			Paint paint1;
			android.graphics.Matrix matrix;

			int i1;
			Paint paint2;
			f = view.getLeft();
			f1 = view.getTop();
			canvas.translate(f, f1);
			f2 = 1F;
			if (obj != null) {
				 if(hasObject)
				 {
					 matrix = ((Transformation) (obj)).getMatrix();
					 canvas.concat(matrix);
				 }
				f2 = ((Transformation) (obj)).getAlpha();
			}
			if (f2 < 1F) {
				paint1 = mPaint;
				i1 = (int) (255F * f2);
				paint1.setAlpha(i1);
			}
			paint2 = mPaint;
			canvas.drawBitmap(bitmap, 0F, 0F, paint2);
			canvas.restoreToCount(i);
			flag = false;
		}
		return flag;
	}

	void findSyncPosition() {
		ListAdapter listadapter = mAdapter;
		int i = mFirstItemPosition;
		long l = listadapter.getItemId(i);
		long l1 = mFirstItemId;
		if (l == l1) {
			return;
		} else {
			mListLeft = 0;
			return;
		}
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(-1, -1);
	}
	
	@Override
	public Adapter getAdapter() {
		return getAdapter();
	}

	public ListAdapter getListAdapter() {
		return mAdapter;
	}

	public int getFirstVisiblePosition() {
		return mFirstItemPosition;
	}

	public int getLastVisiblePosition() {
		return mLastItemPosition;
	}

	public View getSelectedView() {
		throw new UnsupportedOperationException("Not supported");
	}

	public int getSpacing() {
		return mItemSpacing;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent motionevent) {
		Log.i(LOG_TAG, "onInterceptTouchEvent");
		Log.i("HorizontalListView", "-------onInterceptTouchEvent---------- " + this + "," + motionevent);
		int i = motionevent.getAction();
		if (i == MotionEvent.ACTION_UP) {
			endTouch(0F);
			return false;
		}
		if (i == MotionEvent.ACTION_DOWN) {
			startTouch(motionevent);
			return false;
		}
		if (i == MotionEvent.ACTION_MOVE) {
			return startScrollIfNeeded(motionevent);
		}
		/*
		 * motionevent.getAction(); JVM INSTR tableswitch 0 2: default 32 // 0
		 * 43 // 1 32 // 2 53; goto _L1 _L2 _L1 _L3 _L1: boolean flag;
		 * endTouch(0F); flag = false; _L5: return flag; _L2:
		 * startTouch(motionevent); flag = false; continue; Loop/switch isn't
		 * completed _L3: flag = startScrollIfNeeded(motionevent); if(true) goto
		 * _L5; else goto _L4 _L4:
		 */
		return true;
	}

	@Override
	protected void onLayout(boolean flag, int i, int j, int k, int l) {
		super.onLayout(flag, i, j, k, l);

		//Log.i("HorizontalListView", "---flag, i, j, k, l" + flag + "," + i + "," + j + "," + k + "," + l);
		if (mAdapter == null)
			return;
		if (getChildCount() == 0) {
			mLastItemPosition = -1;
			fillListRight(mListLeft, 0);
		} else {
			int l1 = mListLeft + mListLeftOffset;
			View view = getChildAt(0);
			int i2 = getChildLeft(view);
			int j2 = l1 - i2;
			removeNonVisibleViews(j2);
			fillList(j2);
		}
		
		positionItems();
		invalidate();
	}

	@Override
	protected void onMeasure(int i, int j) {
		super.onMeasure(i, j);
		mWidthMeasureSpec = i;
		mHeightMeasureSpec = j;
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionevent) {
	
		if(getChildCount() == 0){
			return false;
		}
		
		int i = motionevent.getAction();
		if(i == MotionEvent.ACTION_DOWN){
			startTouch(motionevent);
		}else if (i == MotionEvent.ACTION_UP){
			if(this.mTouchState == 1){
				int l = (int)motionevent.getX();
				int i1 = (int)motionevent.getY();
				clickChildAt(l, i1);
			}else if (this.mTouchState == 2){
				float f = 0.0F;
				this.mVelocityTracker.addMovement(motionevent);
				this.mVelocityTracker.computeCurrentVelocity(1000);
				f = this.mVelocityTracker.getXVelocity();
				endTouch(f);
			}
		}else if (i == MotionEvent.ACTION_MOVE){
			if(this.mTouchState == 1){
				startScrollIfNeeded(motionevent);
			}else if (this.mTouchState == 2){
				this.mVelocityTracker.addMovement(motionevent);
				int j = (int)motionevent.getX();
				int k = this.mTouchStartX;
				int l = j - k;
				scrollList(l);
			}
		}else{
			//nothing to do here
		}
	
		return true;
	}

	public boolean removeFooterView(View view) {
		boolean flag2;
		if (mFooterViewInfos.size() > 0) {
			boolean flag = false;
			if (((HeaderViewListAdapter) mAdapter).removeFooter(view)) {
				mDataSetObserver.onChanged();
				flag = true;
			}
			ArrayList arraylist = mFooterViewInfos;
			removeFixedViewInfo(view, arraylist);
			boolean flag1 = mFooterViewSet.remove(view);
			flag2 = flag;
		} else {
			flag2 = false;
		}
		return flag2;
	}

	public void setAdapter(Adapter adapter) {
		ListAdapter listadapter = (ListAdapter) adapter;
		setAdapter(listadapter);
	}

	public void setAdapter(ListAdapter listadapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		if (mFooterViewInfos.size() > 0) {
			mAdapter = new HeaderViewListAdapter(null, mFooterViewInfos,
					listadapter);
			Log.i("HorizontalListView", "-------HeaderViewListAdapter----- " + listadapter.getClass().getCanonicalName());
		} else {
			mAdapter = listadapter;
			Log.i("HorizontalListView", "-------listadapter----- " + listadapter.getClass().getCanonicalName());
		}
		removeAllViewsInLayout();
		requestLayout();
		if (mDataSetObserver == null) {
			mDataSetObserver = new DataSetObserver() {
				@Override
				public void onChanged() {
					Log.i(LOG_TAG, "mDataSetObserver.onChanged method");
					int i = getChildCount();
					for (int j = 0; j < i; j++) {
						View view = getChildAt(j);
						if (!mFooterViewSet.contains(view))
							mCachedItemViews.addLast(view);
					}

					removeAllViewsInLayout();
					findSyncPosition();
					int k = mFirstItemPosition = 0;
					int l = mLastItemPosition = 0;
					int i1 = mListLeftOffset = 0;
					int j1 = mLastSnapPos = 0x80000000;
					if (mDynamics != null)
						mDynamics.setMinPosition(-3.402823E+038F);
					HorizontalListView horizontallistview = HorizontalListView.this;
					Runnable runnable = mDynamicsRunnable;
					boolean flag = horizontallistview.removeCallbacks(runnable);
					requestLayout();
				}

				/*
				 * final HorizontalListView this$0;
				 * 
				 * 
				 * { this$0 = HorizontalListView.this; super(); }
				 */
			};
		}
		mAdapter.registerDataSetObserver(mDataSetObserver);
	}

	public void setDynamics(Dynamics dynamics) {
		if (mDynamics != null) {
			float f = mDynamics.getPosition();
			float f1 = mDynamics.getVelocity();
			long l = AnimationUtils.currentAnimationTimeMillis();
			dynamics.setState(f, f1, l);
		}
		mDynamics = dynamics;
		mDynamics.setMaxPosition(0F);
	}

	public void setSelection(int i) {
		throw new UnsupportedOperationException("Not supported");
	}

	public void setSpacing(int i) {
		mItemSpacing = i;
	}

	private static final int INVALID_INDEX = 255;
	private static final int LAYOUT_MODE_LEFT = 1;
	private static final int LAYOUT_MODE_RIGHT = 0;
	private static final String LOG_TAG = "HListView2";
	private static final int PIXELS_PER_SECOND = 1000;
	private static final float POSITION_TOLERANCE = 0.4F;
	private static final int TOUCH_SCROLL_THRESHOLD = 10;
	private static final int TOUCH_STATE_CLICK = 1;
	private static final int TOUCH_STATE_RESTING = 0;
	private static final int TOUCH_STATE_SCROLL = 2;
	private static final float VELOCITY_TOLERANCE = 0.5F;
	private ListAdapter mAdapter;
	private final LinkedList mCachedItemViews;
	private Transformation mChildTransformation;
	private DataSetObserver mDataSetObserver;
	private Dynamics mDynamics;
	private Runnable mDynamicsRunnable;
	private long mFirstItemId;
	private int mFirstItemPosition;
	private ArrayList mFooterViewInfos;
	private HashSet mFooterViewSet;
	private int mHeightMeasureSpec;
	private int mItemSpacing;
	private int mLastItemPosition;
	private int mLastSnapPos;
	private int mListLeft;
	private int mListLeftOffset;
	private int mListLeftStart;
	private Runnable mLongPressRunnable;
	private Paint mPaint;
	private Rect mRect;
	private int mTouchStartX;
	private int mTouchStartY;
	private int mTouchState;
	private VelocityTracker mVelocityTracker;
	private int mWidthMeasureSpec;

}
