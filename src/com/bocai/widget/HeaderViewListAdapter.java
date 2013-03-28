
package com.bocai.widget;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.ArrayList;
import java.util.Iterator;

import com.bocai.R;

public class HeaderViewListAdapter
    implements WrapperListAdapter, Filterable
{

    public HeaderViewListAdapter(ArrayList<FixedViewInfo> arraylist, ArrayList<FixedViewInfo> arraylist1, ListAdapter listadapter)
    {
        mAdapter = listadapter;
        mIsFilterable = listadapter instanceof Filterable;
     
        if(arraylist == null)
        {
        	mHeaderViewInfos = EMPTY_INFO_LIST;
        } else
        {
            mHeaderViewInfos = arraylist;
        }
        if(arraylist1 == null)
        {
        	mFooterViewInfos = EMPTY_INFO_LIST;
        } else
        {
            mFooterViewInfos = arraylist1;
        }
        
        if(!areAllListInfosSelectable(mHeaderViewInfos)) {
            mAreAllFixedViewsSelectable = false;
            return;
        } else {
        	if(!areAllListInfosSelectable(mFooterViewInfos)) {
                mAreAllFixedViewsSelectable = false;
                return;
        	} else {
                mAreAllFixedViewsSelectable = true;
                return;
        	}
        }
    }

    private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> arraylist)
    {
    	if(arraylist == null) {
    		return true;
    	} else {
    		Iterator<FixedViewInfo> iterator = arraylist.iterator();
    		while (iterator.hasNext()) {
    			if(!iterator.next().isSelectable) {
    				return false;
    			}
    		}
    		return true;
    	}
    }

    public boolean areAllItemsEnabled()
    {
        boolean flag;
        if(mAdapter != null)
        {
            if(mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled())
                flag = true;
            else
                flag = false;
        } else
        {
            flag = true;
        }
        return flag;
    }

    public int getCount()
    {
        int i1;
        if(mAdapter != null)
        {
            int i = getFootersCount();
            int j = getHeadersCount();
            int k = i + j;
            int l = mAdapter.getCount();
            i1 = k + l;
        } else
        {
            int j1 = getFootersCount();
            int k1 = getHeadersCount();
            i1 = j1 + k1;
        }
        return i1;
    }

    public Filter getFilter()
    {
        Filter filter;
        if(mIsFilterable)
            filter = ((Filterable)mAdapter).getFilter();
        else
            filter = null;
        return filter;
    }

    public int getFootersCount()
    {
        return mFooterViewInfos.size();
    }

    public int getHeadersCount()
    {
        return mHeaderViewInfos.size();
    }

    public Object getItem(int i)
    {
    	int j = getHeadersCount();

    	while (true) {
    		if(i >= j) {
    	        int k = i - j;
    	        int l = 0;
    	        if(mAdapter != null)
    	        {
    	            l = mAdapter.getCount();
    	            if(k < l)
    	            {
    	                mAdapter.getItem(k);
    	                continue;
    	            }
    	        }
    	        int i1 = k - l;
    	        return ((FixedViewInfo)mFooterViewInfos.get(i1)).data;
    	        
    		} else {
    			return ((FixedViewInfo)mHeaderViewInfos.get(i)).data;
    		}
    	}
    }

    public long getItemId(int i)
    {
    	
    	 int j = getHeadersCount();
    	 if(mAdapter == null || i < j) {
    		 return 65535L;
    	 }
         int k;
         int l;
         k = i - j;
         l = mAdapter.getCount();
         if(k >= l) {
        	 return 65535L;
         }
         return mAdapter.getItemId(k);
    }

    public int getItemViewType(int i)
    {
    	int j = getHeadersCount();
    	if(mAdapter == null || i < j) {
    		return -1;
    	}
        int k;
        int l;
        k = i - j;
        l = mAdapter.getCount();
        if(k >= l) {
        	return  -1;
        }
        return mAdapter.getItemViewType(k);
    	
    }

    public View getView(int i, View view, ViewGroup viewgroup)
    {
    	
    	int j = getHeadersCount();
    	while(true) {
    		if (i >= j) {
    		        int k = i - j;
    		        int l = 0;
    		        if(mAdapter != null)
    		        {
    		            l = mAdapter.getCount();
    		            if(k < l)
    		            {
    		            	Log.i("HeaderViewListAdapter", "%%%%%%%%% i, j, k " + i + "," + j + "," + k);
    		                View view1 = mAdapter.getView(k, view, viewgroup);
    				        Log.i("HeaderViewListAdapter", "!!!!!!!!!!!!!!!! view.getTag(R.id.key_objects) " + view1.getTag(R.id.key_objects));
    				        return view1;
    				        //TODO: temprorily by lifeng
    		            }
    		        }
    		        int i1 = k - l;
    		        return  ((FixedViewInfo)mFooterViewInfos.get(i1)).view;
    		} else {
    			return ((FixedViewInfo)mHeaderViewInfos.get(i)).view;
    		}
    	}
    }

    public int getViewTypeCount()
    {
        int i;
        if(mAdapter != null)
            i = mAdapter.getViewTypeCount();
        else
            i = 1;
        return i;
    }

    public ListAdapter getWrappedAdapter()
    {
        return mAdapter;
    }

    public boolean hasStableIds()
    {
        boolean flag;
        if(mAdapter != null)
            flag = mAdapter.hasStableIds();
        else
            flag = false;
        return flag;
    }

    public boolean isEmpty()
    {
        boolean flag;
        if(mAdapter == null || mAdapter.isEmpty())
            flag = true;
        else
            flag = false;
        return flag;
    }

    public boolean isEnabled(int i)
    {
    	
    	int j = getHeadersCount();
    	
    	while (true) {
    		if(i >= j) {
    	        int k = i - j;
    	        int l = 0;
    	        if(mAdapter != null)
    	        {
    	            l = mAdapter.getCount();
    	            if(k < l)
    	            {
    	                mAdapter.isEnabled(k);
    	                continue;  
    	            }
    	        }
    	        int i1 = k - l;
    	        return ((FixedViewInfo)mFooterViewInfos.get(i1)).isSelectable;
    		} else {
    			return ((FixedViewInfo)mHeaderViewInfos.get(i)).isSelectable;
    		}
    	}    	
    }

    public void registerDataSetObserver(DataSetObserver datasetobserver)
    {
        if(mAdapter == null)
        {
            return;
        } else
        {
            mAdapter.registerDataSetObserver(datasetobserver);
            return;
        }
    }

    public boolean removeFooter(View view)
    {
    	int i = 0;
    	while(true) {
            int j = mFooterViewInfos.size();
            if(i >= j) {
            	break;
            }
            if(((FixedViewInfo)mFooterViewInfos.get(i)).view != view) {
            	i++;
            } else {
            	mFooterViewInfos.remove(i);
                if(!areAllListInfosSelectable(mHeaderViewInfos)) {
                    mAreAllFixedViewsSelectable = false;
                    return true;
                } else {
                	if(!areAllListInfosSelectable(mFooterViewInfos)) {
                        mAreAllFixedViewsSelectable = false;
                        return true;
                	} else {
                		return true;
                	}
                }
            }
    	}
    	return false;
    	
    }

    public boolean removeHeader(View view)
    {
    	int i = 0;
    	while(true) {
    		int j = mHeaderViewInfos.size();
    		if (i >= j) { 
    			break;
    		}
    		if(((FixedViewInfo)mHeaderViewInfos.get(i)).view != view) {
    			i++;
    		} else {
    	         mHeaderViewInfos.remove(i);
    	        if(!areAllListInfosSelectable(mHeaderViewInfos)) {
    	        	mAreAllFixedViewsSelectable = false;
    	        	return true;
    	        }
    	        if(!areAllListInfosSelectable(mFooterViewInfos)) {
    	        	mAreAllFixedViewsSelectable = false;
    	        	return true;
    	        }
    	        return true;
    		}
    	}
    	return false;
    }

    public void unregisterDataSetObserver(DataSetObserver datasetobserver)
    {
        if(mAdapter == null)
        {
            return;
        } else
        {
            mAdapter.unregisterDataSetObserver(datasetobserver);
            return;
        }
    }

    static final ArrayList<FixedViewInfo> EMPTY_INFO_LIST = new ArrayList<FixedViewInfo>();
    private final ListAdapter mAdapter;
    boolean mAreAllFixedViewsSelectable;
    ArrayList<FixedViewInfo> mFooterViewInfos;
    ArrayList<FixedViewInfo> mHeaderViewInfos;
    private final boolean mIsFilterable;

}
