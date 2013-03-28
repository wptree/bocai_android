
package com.bocai.model;

import com.bocai.R;
import com.bocai.util.Macros;

public class Filter
{

    public Filter()
    {
    }

    public static boolean areaIsAnywhere()
    {
        boolean flag;
        if(filterArea() == 2)
            flag = true;
        else
            flag = false;
        return flag;
    }

    public static boolean areaIsWithinMap()
    {
        boolean flag;
        if(filterArea() == 1)
            flag = true;
        else
            flag = false;
        return flag;
    }

    public static int filterArea()
    {
        return Macros.FS_DEFAULT_GET_INT("FSFilterArea");
    }

    public static String filterResultStringFor(int i)
    {
    	String answer ;
    	
    	switch(i){
    	  
    	 default: answer = "all"; break;
    	 case 1:  answer = "all"; break;
    	 case 2:  answer = "wanted"; break;
    	 case 3:  answer = "following"; break;
    	}
    	return answer;
    }

    public static int filterResults()
    {
        return Macros.FS_DEFAULT_GET_INT("FSFilterResults");
    }

    public static String filterResultsString()
    {
        return filterResultStringFor(filterResults());
    }

    public static int filterSort()
    {
        return Macros.FS_DEFAULT_GET_INT("FSFilterSort");
    }
    
    public static String filterSortStringForView()
    {
    	return filterSortStringForView(filterSort());
    }

    public static String filterSortStringForView(int i)
    {
    	String answer ;
    	
    	switch(i){
    	  
    	 default: answer = Macros.FS_APPLICATION().getString(R.string.Nearest); break;
    	 case 1:  answer = Macros.FS_APPLICATION().getString(R.string.Nearest); break;
    	 case 2:  answer = Macros.FS_APPLICATION().getString(R.string.Latest); break;
    	 case 3:  answer = Macros.FS_APPLICATION().getString(R.string.Best); break;
    	}
    	return answer;
    }
    

    public static String filterSortString()
    {
    	return filterSortStringFor(filterSort());
    }

    public static String filterSortStringFor(int i)
    {
    	String answer ;
    	
    	switch(i){
    	  
    	 default: answer = "nearest"; break;
    	 case 1:  answer = "nearest"; break;
    	 case 2:  answer = "latest"; break;
    	 case 3:  answer = "best"; break;
    	}
    	return answer;
    }

    public static final void initDefaultFilters()
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterResults", 1);
        Macros.FS_DEFAULT_SET_INT("FSFilterSort", 1);
        Macros.FS_DEFAULT_SET_INT("FSFilterArea", 2);
        Macros.FS_DEFAULT_SET_INT("FSFilterSortSpotted", 1);
        Macros.FS_DEFAULT_SET_INT("FSFilterSortWanted", 1);
        Macros.FS_DEFAULT_SET_INT("FSFilterFollowing", 1);
        Macros.FS_DEFAULT_SET_INT("FSGuideFilter", 1);
    }

    public static final void initStartupFilters()
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterSort", 1);
    }

    public static boolean resultsFollowing()
    {
        boolean flag;
        if(filterResults() == 3)
            flag = true;
        else
            flag = false;
        return flag;
    }

    public static boolean resultsWanted()
    {
        boolean flag;
        if(filterResults() == 2)
            flag = true;
        else
            flag = false;
        return flag;
    }

    public static void setAnywhere()
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterArea", 2);
    }

    public static void setBest()
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterSort", 3);
    }

    public static void setFilterArea(int i)
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterArea", i);
    }

    public static void setFilterResults(int i)
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterResults", i);
    }

    public static void setFilterSort(int i)
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterSort", i);
    }

    public static void setNearest()
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterSort", 1);
    }

    public static void setWithinMap()
    {
        Macros.FS_DEFAULT_SET_INT("FSFilterArea", 1);
    }

    public static final String sortCaptionString()
    {
    	String answer ;
    	
    	switch(filterSort()){
    	  
    	 default: answer = ""; break;
    	 case 1:  answer = Macros.FS_APPLICATION().getString(R.string.what_good_around); break;
    	 case 2:  answer = Macros.FS_APPLICATION().getString(R.string.see_what_new); break;
    	 case 3:  answer = Macros.FS_APPLICATION().getString(R.string.see_what_good); break;
    	}
    	return answer;
    }

    public static boolean sortNearest()
    {
        boolean flag;
        if(filterSort() == 1)
            flag = true;
        else
            flag = false;
        return flag;
    }
}
