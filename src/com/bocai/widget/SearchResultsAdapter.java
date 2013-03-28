package com.bocai.widget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.bocai.model.FSObject;
import com.bocai.model.Item;
import com.bocai.model.Place;
import java.util.List;

public class SearchResultsAdapter extends ArrayAdapter<FSObject>
{
  private LayoutInflater _inflater;
  private int _itemParentId;
  private List<FSObject> _objects;
  private Filter filter = null;

  public SearchResultsAdapter(Context paramContext, int paramInt, List<FSObject> paramList)
  {
    super(paramContext, paramInt, paramList);
    this._objects = paramList;
    this._itemParentId = paramInt;
    LayoutInflater localLayoutInflater = LayoutInflater.from(paramContext);
    this._inflater = localLayoutInflater;
  }

  public Filter getFilter()
  {
    Filter localFilter;
    if (this.filter != null){
      localFilter = this.filter;
    }
    localFilter = super.getFilter();
    return localFilter;
  }

  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
  {
	  
	  TextView titleView = null;
	  TextView detailView = null;
	  FSObject localFSObject;
	  Place localPlace;
	 	  
	  if(paramView == null){
		  LayoutInflater localLayoutInflater = this._inflater;
	      int i = this._itemParentId;
	     paramView = localLayoutInflater.inflate(i, null);
	  }
	  
	  
	  
//	  titleView = (TextView)paramView.findViewById(com.bocai.R.id.text1);
//	  titleView.setHorizontallyScrolling(true);
//	  detailView = (TextView)paramView.findViewById(com.bocai.R.id.text2);
//	  detailView.setHorizontallyScrolling(true);
	  
	  titleView = (TextView)paramView.findViewById(0x1020014);
	  titleView.setHorizontallyScrolling(true);
	  detailView = (TextView)paramView.findViewById(0x1020015);
	  detailView.setHorizontallyScrolling(true);
	  
	  
	  Object[] arrayOfObject = new Object[2];
	  arrayOfObject[0] = titleView;
	  arrayOfObject[1] = detailView;
	  paramView.setTag(arrayOfObject);
	  
	  localFSObject = (FSObject)this._objects.get(paramInt);
	  if(localFSObject instanceof Place){
		  localPlace = (Place)localFSObject;
		  if(localPlace.sightingsCount >0){
			  StringBuilder localStringBuilder = new StringBuilder();
			  localStringBuilder.append(localPlace.name).append(" (");
			  int j = localPlace.sightingsCount;
			  String str2 = j + ")";
			  localStringBuilder.append(str2);
			  titleView.setText(localStringBuilder.toString());
		  }else{
			  StringBuilder sb = new StringBuilder();
			  sb.append(localPlace.name).append("(").append(localPlace.secondName).append(")");
			  titleView.setText(sb.toString());
			  detailView.setVisibility(8);
		  }
		  if(localPlace.address != null){
			  if(localPlace.city != null){
				  detailView.setText(localPlace.city + "-" + localPlace.address);
			  }else{
				  detailView.setText(localPlace.address);
			  }
			  detailView.setVisibility(0);
		  }
	  }else if (localFSObject instanceof Item){
		  Item localItem = (Item)localFSObject;
   	    	if (localItem.sightingsCount > 0){
   	    		StringBuilder localStringBuilder3 = new StringBuilder();
   	    		localStringBuilder3.append(localItem.name).append(" (");
   	    		int k = localItem.sightingsCount;
   	    		String str6 =localStringBuilder3.append(k).append(")").toString();
   	    		titleView.setText(str6);
   	    }else{
   	    	titleView.setText(localItem.name);
   	    }
   	    	detailView.setVisibility(8);
	  }
	  
	  return paramView;
	 
  }

  public void setFilter(Filter paramFilter)
  {
    this.filter = paramFilter;
  }
}