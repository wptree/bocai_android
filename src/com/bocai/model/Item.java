
package com.bocai.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.bocai.net.AsyncHTTPRequest;
import com.bocai.net.AsyncHTTPResponseHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import org.json.*;

public class Item extends FSObject
    implements Parcelable
{

    public Item()
    {
        AsyncHTTPResponseHandler asyncHttpResponseHandler = new AsyncHTTPResponseHandler() {
        	
   
        	@Override
			public void handleError(String errorMsg, InputStream stream,
					long length) throws IOException {
	
        		String s1 = (new StringBuilder()).append("itemArrayHandler.handleError: ").append(errorMsg).toString();
                Log.d(LOG_TAG, s1);
                if(delegate == null){
                    return;
                }
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(errorMsg);
                if(stream != null)
                {
                    String s2 = AsyncHTTPRequest.toString(stream, length);
                    jsonArray.put(s2);
                }
                JSONObject jsonObject = new JSONObject();
                try
                {
                	jsonObject.put("errors", jsonObject);
                	delegate.displayErrors(jsonObject);
                    return;
                }
                catch(JSONException e)
                {
                    return;
                }
			}
        	
        	@Override
			public void handleResponse(AsyncHTTPRequest asyncHttpRequest,
					InputStream stream, long length) throws IOException {
        		 try
                 {
                     String s = AsyncHTTPRequest.toString(stream, length);                      
                     JSONObject obj = new JSONObject(s);
                     JSONArray jsonArray = obj.optJSONArray("items");
                     if(jsonArray != null && jsonArray.length() > 0)
                     {
                         LinkedList<FSObject> linkedList = new LinkedList<FSObject>();
                         int i = jsonArray.length();
                         for(int j = 0; j < i; j++)
                         {
                             JSONObject jsonObject = jsonArray.getJSONObject(j);
                             Item item = new Item(jsonObject);
                             if(item.name != null && item.name.length() > 0)
                                linkedList.add(item);
                         }

                         if(delegate == null)
                         {
                             return;
                         } else {
                        	 delegate.FSResponse(linkedList);
                             return;
                         }
                     }
                 }
                 catch(JSONException e)
                 {
                     StringBuilder sb = (new StringBuilder()).append("Error parsing follow response: ");
                     sb.append(e.getLocalizedMessage());
                     Log.e(LOG_TAG, sb.toString(), e);
                     e.printStackTrace();
                 }
			}	
        };
        
        itemsArrayResponseHandler = asyncHttpResponseHandler;
    }

    private Item(Parcel parcel)
    {
    	this();
    	uid = parcel.readInt();
    	name = parcel.readString();
        sightingsCount = parcel.readInt();
    }


    public Item(JSONObject jsonObject)
    {
     
    	this();
    	
        if(jsonObject == null){
            return;
        }
        uid = jsonObject.optInt("id");
        name = jsonObject.optString("name");
        sightingsCount = jsonObject.optInt("sightings_count");
       
    }

    public int describeContents()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void itemsAtPlace(Place place)
    {
        HashMap<String,Object> params = new HashMap<String,Object>();
        StringBuilder stringbuilder = (new StringBuilder()).append("places/");
        AsyncHTTPRequest asyncHttpRequest = FSObject.requestWithPath(stringbuilder.append(place.id).append("/items").toString(), params);
        asyncHttpRequest.responseHandler = itemsArrayResponseHandler;
        asyncHttpRequest.execute();
    }

    public String toString()
    {
        StringBuilder sb = (new StringBuilder()).append("{uid: ");
        sb.append(uid).append(", name: ");
        sb.append(name).append(", sightings_count: ");
        return sb.append(sightingsCount).append("}").toString();
    }

    public void writeToParcel(Parcel parcel,int flags)
    {
        parcel.writeInt(uid);
        parcel.writeString(name);
        parcel.writeInt(sightingsCount);
    }

    public static final android.os.Parcelable.Creator<Item> CREATOR = new android.os.Parcelable.Creator<Item>() {

        public Item createFromParcel(Parcel parcel)
        {
            return new Item(parcel);
        }
        public Item[] newArray(int i)
        {
            return new Item[i];
        }
    };
    
    private static final String LOG_TAG = "Item";
    AsyncHTTPResponseHandler itemsArrayResponseHandler;
    public String name;
    public int sightingsCount;
    public int uid;

}
