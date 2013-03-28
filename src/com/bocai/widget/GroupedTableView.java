package com.bocai.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

public class GroupedTableView  extends LinearLayout {

	 static final int ID_LIST_INDEX = 1812594688;
	  private Adapter adapter = null;
	  private DataSetObserver adapterDataSetObserver;
	  private View.OnClickListener childOnClickListener;
	  private Drawable divider;
	  private int dividerResource = 17301524;
	  private OnItemClickListener onItemClickListener;
	  private int selectorBackgroundResource;

	  public GroupedTableView(Context context)
	  {
	    super(context);
	    adapter = null;
	    dividerResource =0x1080014;
	    this.divider = null;
	    this.onItemClickListener = null;
	    adapterDataSetObserver = new DataSetObserver()
	    {
	      public void onChanged()
	      {
	        Log.d("GroupedTableView", "data set changed!");
	        removeAllViews();
	        int k = adapter.getCount();
	        int l = 0;
	        
	        do
            {
                if(l >= k)
                    return;
                GroupedTableView groupedTableView = GroupedTableView.this;
                View view = adapter.getView(l, null, groupedTableView);
                groupedTableView.addView(view);
                l++;
            } while(true);
	      }

	      public void onInvalidated()
	      {
	        Log.d("GroupedTableView", "data set invalidated!");
	      }
	    };

	    android.view.View.OnClickListener onclicklistener = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
                if(onItemClickListener == null)
                {
                    return;
                } else
                {               
                    int j = ((Integer)view.getTag(0x6c0a0000)).intValue();
                    onItemClickListener.onItemClick(GroupedTableView.this, view, j);
                    return;
                }
            }
        };
	    childOnClickListener = onclicklistener;
	    setOrientation(1);
	    Resources localResources = context.getResources();
	    int i = this.dividerResource;
	    Drawable localDrawable = localResources.getDrawable(i);
	    divider = localDrawable;
	  }

	  public GroupedTableView(Context context, AttributeSet attributeSet)
	  {
	    super(context, attributeSet);
	    adapter = null;
	    divider = null;
	    dividerResource = 0x1080014;
	    onItemClickListener = null;
	    DataSetObserver datasetobserver = new DataSetObserver()
	    {
	      public void onChanged()
	      {
	        Log.d("GroupedTableView", "data set changed!");
	        removeAllViews();
	        int j = adapter.getCount();
	        int l = 0;
	        
	        do
            {
                if(l >= j)
                    return;
                GroupedTableView groupedTableView = GroupedTableView.this;
                View view = adapter.getView(l, null, groupedTableView);
                groupedTableView.addView(view);
                l++;
            } while(true);
	      }

	      public void onInvalidated()
	      {
	        Log.d("GroupedTableView", "data set invalidated!");
	      }
	    };
	    this.adapterDataSetObserver = datasetobserver;
	    childOnClickListener = new View.OnClickListener()
	    {
	      public void onClick(View view)
	      {
	    	 if(onItemClickListener == null){
	    		 return;
	    	 }
	    	  OnItemClickListener onitemclicklistener = onItemClickListener;
              GroupedTableView groupedtableview = GroupedTableView.this;
              int j = ((Integer)view.getTag(0x6c0a0000)).intValue();
              onitemclicklistener.onItemClick(groupedtableview, view, j);
              return;
	      }
	    };
	
	    setOrientation(1);
	    Resources localResources = context.getResources();
	    int i = this.dividerResource;
	    Drawable localDrawable = localResources.getDrawable(i);
	    this.divider = localDrawable;
	    init(attributeSet);
	  }

	  private void init(AttributeSet paramAttributeSet)
	  {
	    Context localContext = getContext();
	    int[] arrayOfInt = ConstantR.styleable.GroupedTableView;
	    TypedArray localTypedArray = localContext.obtainStyledAttributes(paramAttributeSet, arrayOfInt);
	    int i = localTypedArray.getResourceId(0, 0);
	    this.selectorBackgroundResource = i;
	    localTypedArray.recycle();
	    int j = 0;
	    int k = paramAttributeSet.getAttributeCount();
	    if (j >= k)
	      return;
	    String str = paramAttributeSet.getAttributeName(j);
	    if (str.equals("listSelector"))
	    {
	    	selectorBackgroundResource = paramAttributeSet.getAttributeResourceValue(j, 0);
	    }else if (str.equals("divider")){
	    	dividerResource = paramAttributeSet.getAttributeResourceValue(j, 0);
	    	 Resources localResources = getContext().getResources();
	    	 divider = localResources.getDrawable(dividerResource);
	    }
	  }

	  public void addView(View view)
	  {
	    int i = getChildCount();
	    if (i > 0)
	    {
	      Context localContext = getContext();
	      View localView = new View(localContext);
	      localView.setBackgroundDrawable(divider);
	      LinearLayout.LayoutParams layoutParams = getDividerLayoutParams();
	      super.addView(localView, layoutParams);
	      i += 1;
	    }
	    LinearLayout.LayoutParams  layoutParams2 = (LinearLayout.LayoutParams)view.getLayoutParams();
	    if (layoutParams2 == null)
	    {
	    	layoutParams2 = generateDefaultLayoutParams();
	      if (layoutParams2 == null)
	        throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
	    }
	    super.addView(view, layoutParams2);
	    Integer localInteger = Integer.valueOf(i >> 1);
	    view.setTag(0x6c0a0000, localInteger);
	    if (this.selectorBackgroundResource != 0)
	    {
	      int j = this.selectorBackgroundResource;
	      view.setBackgroundResource(j);
	    }
	    if (!(view.isClickable()))
	      return;
	    View.OnClickListener localOnClickListener = this.childOnClickListener;
	    view.setOnClickListener(localOnClickListener);
	  }

	  public void addView(View paramView, LinearLayout.LayoutParams paramLayoutParams)
	  {
	    int i = getChildCount();
	    if (i > 0)
	    {
	      Context localContext = getContext();
	      View localView = new View(localContext);
	      Drawable localDrawable = this.divider;
	      localView.setBackgroundDrawable(localDrawable);
	      LinearLayout.LayoutParams localLayoutParams = getDividerLayoutParams();
	      super.addView(localView, localLayoutParams);
	      i += 1;
	    }
	    super.addView(paramView, paramLayoutParams);
	    Integer localInteger = Integer.valueOf(i >> 1);
	    paramView.setTag(0x6c0a0000, localInteger);
	    if (this.selectorBackgroundResource != 0)
	    {
	      int j = this.selectorBackgroundResource;
	      paramView.setBackgroundResource(j);
	    }
	    if (!(paramView.isClickable()))
	      return;
	    View.OnClickListener localOnClickListener = this.childOnClickListener;
	    paramView.setOnClickListener(localOnClickListener);
	  }

	  public Adapter getAdapter()
	  {
	    return this.adapter;
	  }

	  public LinearLayout.LayoutParams getDividerLayoutParams()
	  {
	    LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(-1, 1, 0.0F);
	    localLayoutParams.setMargins(0, 2, 0, 2);
	    return localLayoutParams;
	  }

	  public void setAdapter(Adapter paramAdapter)
	  {
	    if (this.adapter != null)
	    {
	      Adapter localAdapter1 = this.adapter;
	      DataSetObserver localDataSetObserver1 = this.adapterDataSetObserver;
	      localAdapter1.unregisterDataSetObserver(localDataSetObserver1);
	    }
	    this.adapter = paramAdapter;
	    if (this.adapter == null)
	      return;
	    int i = this.adapter.getCount();
	    int j = 0;
	    while (j < i)
	    {
	      View localView = this.adapter.getView(j, null, this);
	      addView(localView);
	      j += 1;
	    }
	    Adapter localAdapter2 = this.adapter;
	    DataSetObserver localDataSetObserver2 = this.adapterDataSetObserver;
	    localAdapter2.registerDataSetObserver(localDataSetObserver2);
	  }

	  public void setDivider(Drawable paramDrawable)
	  {
	    this.divider = paramDrawable;
	  }

	  public void setOnItemClickListener(OnItemClickListener paramOnItemClickListener)
	  {
	    this.onItemClickListener = paramOnItemClickListener;
	  }

	  public void setSelectorBackgroundResource(int paramInt)
	  {
	    this.selectorBackgroundResource = paramInt;
	  }

	  public static abstract interface OnItemClickListener
	  {
	    public abstract void onItemClick(GroupedTableView paramGroupedTableView, View paramView, int paramInt);
	  }
	
	
}
