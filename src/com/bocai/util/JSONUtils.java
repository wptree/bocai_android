
package com.bocai.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JSONUtils
{

    @SuppressWarnings("rawtypes")
	public static Map toMap(JSONObject jsonObject)
        throws JSONException
    {
        HashMap<String,Object> hashMap = new HashMap<String,Object>();
        for(Iterator iterator = jsonObject.keys(); iterator.hasNext();)
        {
            String key = (String)iterator.next();
            Object obj = jsonObject.get(key);
        
            if(obj.getClass().getName().equalsIgnoreCase("org.json.JSONObject"))
            {
                Map map = toMap((JSONObject)obj);
                hashMap.put(key, map);
            } else if(obj.getClass().getName().equalsIgnoreCase("org.json.JSONArray")){
                ArrayList<Object> arraylist = new ArrayList<Object>();
                JSONArray jsonArray = (JSONArray)obj;
                int index = 0;
                int length = jsonArray.length();
                while(index < length){
                	Object element = jsonArray.get(index);
                	if(element instanceof JSONObject){
                	    Map tempMap = toMap((JSONObject)element);
                	    arraylist.add(tempMap);
                	}else{
                		arraylist.add(element);
                	}
                	index++;
                }
                hashMap.put(key,arraylist);
            }else{
            	 hashMap.put(key,obj);
            }
                
        }

        return hashMap;
    }
   
}
