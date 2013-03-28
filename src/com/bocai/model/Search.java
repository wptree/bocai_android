
package com.bocai.model;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.bocai.net.AsyncHTTPRequest;
import com.bocai.util.Macros;

import java.util.*;
import org.json.*;


public class Search extends FSObject
{

    public Search()
    {
    }

    static AsyncHTTPRequest itemRequestWithParameters(HashMap<String, Object> params)
    {
        return FSObject.requestWithPath("items/search", params);
    }

    static AsyncHTTPRequest placeRequestWithParameters(HashMap<String, Object> params)
    {
        return FSObject.requestWithPath("places/search", params);
    }

    public void doItemSearch(String query){
    	Log.i("Search", "doItemSearch method:" + query);
    	
    	HashMap<String, Object> params = new HashMap<String, Object>();
        if(query != null && query.length() > 0){
            params.put("query", Uri.encode(query));
        }
       
        AsyncHTTPRequest asyncHttpRequest = itemRequestWithParameters(params);
        performRequest(asyncHttpRequest);
    }
    
    public void doPlaceSearch(String query,Location location){
    	Log.i("Search", "doItemSearch method:" + query);
    	HashMap<String, Object> params = new HashMap<String, Object>();
        
    	if(query != null && query.length() > 0){
            params.put("query", Uri.encode(query));
        }
    	
    	if(location != null){
    		params.put("altitude", Double.toString(location.getAltitude()));
    		params.put("longitude",Double.toString(location.getLongitude()));
    	}
    	
       AsyncHTTPRequest asyncHttpRequest = placeRequestWithParameters(params);
       performRequest(asyncHttpRequest);
    	
    }
    

    public void doSightingSearch(String s, Location location, double d,String type)
    {
        doSightingSearch(s, location, d, 0,type);
    }
    
   
    public void doSightingSearch(String s, Location location, double d, int i,String type)
    {
    	HashMap<String, Object> params = new HashMap<String, Object>();
        if(s != null && s.length() > 0){
			params.put("query", Uri.encode(s,null));
        }
        if(type != null && type.length() > 0){
        	params.put("type",type);
        }
        
        String s1 = Filter.filterSortString();
        params.put("sort", s1);
        String s2 = Filter.filterResultsString();
        params.put("filter", s2);
        String s3 = Integer.toString(i);
        params.put("page", s3);
        params.put("per_page", "10");
        AsyncHTTPRequest asyncHttpRequest;
      
        if(location == null){
        	Log.e(LOG_TAG, "param location is null==============================");
        }
        
        if(location != null && (Filter.areaIsWithinMap() || Filter.sortNearest()))
        {
            String s4 = Double.toString(location.getLatitude());
            params.put("latitude", s4);
            String s5 = Double.toString(location.getLongitude());
            params.put("longitude", s5);
            String s6 = Double.toString(d);
            params.put("within", s6);
        } else if(location != null && Filter.sortNearest())
        {
            String s9 = Double.toString(location.getLatitude());
            params.put("latitude", s9);
            String s10 = Double.toString(location.getLongitude());
            params.put("longitude", s10);
            params.put("within", "5");
        }else if (location != null){
        	 params.put("latitude", Double.toString(location.getLatitude()));
        	 params.put("longitude", Double.toString(location.getLongitude()));
        	 params.put("within", "5");
        	 //NOTE: add more cases,without location,the search result is blank
        }
        asyncHttpRequest = Sighting.listRequestWithParameters(params);
        asyncHttpRequest.setUseCookiePersistence(false);
        if(User.currentUser() != null)
        {
         performRequest(asyncHttpRequest, User.currentUser().cookies);
            return;
        } else
        {
          performRequest(asyncHttpRequest, null);
            return;
        }
    }

    void responseData(HashMap<String,Object> params, AsyncHTTPRequest asyncHttpRequest)
    {
    	//blank
    }

    protected void responseData(JSONObject jsonObject, AsyncHTTPRequest asyncHttpRequest)
        throws JSONException
    {
        if(jsonObject == null)
            if(delegate == null)
            {
                return;
            } else
            {
                delegate.FSResponse(null);
                return;
            }
        JSONArray jsonarray = jsonObject.optJSONArray("data");
        JSONArray jsonarray1 = jsonObject.optJSONArray("results");
        JSONArray jsonarray2 = jsonObject.optJSONArray("items");
        if(jsonarray != null)
        {
            int i = Filter.filterSort();
            LinkedList<FSObject> linkedlist = new LinkedList<FSObject>();
            int length = jsonarray.length();
            for(int index = 0; index < length; index++)
            {
                JSONObject obj = jsonarray.getJSONObject(index);
                Sighting sighting = new Sighting(obj);
                sighting.setSearchFilterSort(i);
                linkedlist.add(sighting);
            }

            if(delegate != null)
            {
            
                Map<String, Object> map = Macros.ACTION_PAGES(jsonObject.opt("total"));
                JSONObject obj2 = new JSONObject(map);
                delegate.finishedAction(obj2);
                delegate.FSResponse(linkedlist);
            }
            
            return;
        }

        
        if(jsonarray1 != null && jsonarray1.length() > 0)
        {
            LinkedList<FSObject> linkedlist = new LinkedList<FSObject>();
            int length = jsonarray1.length();
            for(int index = 0; index < length ; index++){
            	JSONObject obj = jsonarray1.optJSONObject(index);
            	FSObject fsObj = new Place(obj);
            	linkedlist.add(fsObj);
            }

            if(delegate != null)
            {
                Map<String, Object> map1 = Macros.ACTION_PAGES(jsonObject.opt("total"));
                JSONObject jsonobject4 = new JSONObject(map1);
                delegate.finishedAction(jsonobject4);
                delegate.FSResponse(linkedlist);
            }
            
            return;
        }
        
        //items
        if (jsonarray2 != null && jsonarray2.length() > 0){
        	  LinkedList<FSObject> linkedlist = new LinkedList<FSObject>();
              int length = jsonarray2.length();
              for(int index = 0; index < length ; index++){
              	JSONObject obj = jsonarray2.optJSONObject(index);
              	FSObject fsObj = new Item(obj);
              	linkedlist.add(fsObj);
              }

              if(delegate != null)
              {
                 delegate.FSResponse(linkedlist);
              }
              
              return;
        }
        
        
    }

 //   private static final String LOG_TAG = "Search";
}
