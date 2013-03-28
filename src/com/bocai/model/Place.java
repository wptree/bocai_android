
package com.bocai.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.bocai.net.AsyncHTTPRequest;
import com.bocai.util.Macros;

import java.util.*;
import org.json.*;


public class Place extends FSObject
    implements Parcelable
{

    public Place()
    {
    	//blank
    }

    private Place(Parcel parcel)
    {
    	this();
    	id = parcel.readInt();
    	name = parcel.readString();
    	secondName = parcel.readString();
    	latitude = parcel.readDouble();
    	longitude = parcel.readDouble();
    	address = parcel.readString();
    	fullAddress = parcel.readString();
    	city = parcel.readString();
    	state = parcel.readString();
    	phone = parcel.readString();
    	sightingsCount = parcel.readInt();
    	distance = parcel.readDouble();
    	googleID = parcel.readString();
    }


    public Place(JSONObject jsonObject)
    {
    	this();
   
        if(jsonObject == null)
            return;
        
        id = jsonObject.optInt("id");
        name = jsonObject.optString("name");
        secondName = jsonObject.optString("secondName");
        latitude = jsonObject.optDouble("latitude");
        longitude = jsonObject.optDouble("longitude");
        address = jsonObject.optString("street_address");
        fullAddress = jsonObject.optString("full_address");
        city = jsonObject.optString("city");
        state = jsonObject.optString("state");
        phone = jsonObject.optString("phone_number");
        sightingsCount = jsonObject.optInt("sightings_count");
        googleID = jsonObject.optString("google_id");
     
        Object links = jsonObject.opt("links");
        if(links == null){
        	return;
        }
        if(links instanceof JSONObject){
        	JSONObject jsonLinks = (JSONObject)links;
        	link = jsonLinks.optString("uri");
        	link_title = jsonLinks.optString("title");
        }else if (links instanceof JSONArray){
        	JSONArray array = (JSONArray)links;
        	JSONObject jsonLinks = array.optJSONObject(0);
        	link = jsonLinks.optString("uri");
        	link_title = jsonLinks.optString("title");
        }
    }

    public int describeContents()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void listSighting(int pageNum,int pageSize){
    	HashMap<String,Object> requestParams = new HashMap<String,Object>();
    	requestParams.put("page", pageNum);
    	requestParams.put("per_page",pageSize);
    	StringBuilder sb = new StringBuilder();
    	sb.append("places/");
    	sb.append(id);
    	sb.append("/sightings");
    	AsyncHTTPRequest asyncHttpRequest = FSObject.requestWithPath(sb.toString(), requestParams);
        asyncHttpRequest.responseHandler = this;
        if(User.currentUser() != null){
        	performRequest(asyncHttpRequest, User.currentUser().cookies);
        }else{
        	performRequest(asyncHttpRequest, null);
        }
    		
    }
    
    

    public void nearestPlacesAtLocation(Location location)
    {
        Log.i(LOG_TAG, "nearestPlacesAtLocation method");
    	HashMap<String,Object> requestParams = new HashMap<String,Object>();
        if(location != null)
        {
           String latitude = Double.toString(location.getLatitude());
           requestParams.put("latitude", latitude);
           String longitude = Double.toString(location.getLongitude());
           requestParams.put("longitude", longitude);
        }
      AsyncHTTPRequest asyncHttpRequest = Search.placeRequestWithParameters(requestParams);
      performRequest(asyncHttpRequest);
    }

    protected void responseData(JSONObject jsonObject, AsyncHTTPRequest asyncHttpRequest)
        throws JSONException
    {
    
    	if(jsonObject == null){
        	if(delegate != null){
        		delegate.FSResponse(null);
        	}
        	return;
        }
    	
    	Log.i(LOG_TAG, "responseData method===" + jsonObject.toString());
    	
        JSONArray array1 = jsonObject.optJSONArray("data");
        JSONArray array2 = jsonObject.optJSONArray("results");
        
        if(array1 != null && array1.length() > 0){
        	LinkedList<FSObject> list = new LinkedList<FSObject>();
        	int length = array1.length();
        	for(int index = 0; index < length; index++){
        		JSONObject obj = array1.getJSONObject(index);
        		 Sighting sighting = new Sighting(obj);
        		 list.add(sighting);
        	}
        	if(delegate != null){
        		Map<String, Object> map = Macros.ACTION_PAGES(jsonObject.opt("total"));
        		JSONObject obj = new JSONObject(map);
        		delegate.FSResponse(list);
        		delegate.finishedAction(obj);
        	}
        }
        
        if(array2 != null && array2.length() > 0){
        	LinkedList<FSObject> list = new LinkedList<FSObject>();
        	int length = array2.length();
        	for(int index = 0; index < length; index++){
        		JSONObject obj = array2.getJSONObject(index);
        		Place place = new Place(obj);
        		list.add(place);
        	}
        	if(delegate != null){
        		Map<String, Object> map = Macros.ACTION_PAGES(jsonObject.opt("total"));
        		JSONObject obj = new JSONObject(map);
        		delegate.finishedAction(obj);
        		delegate.FSResponse(list);
        	}
        }
        	
    }

    public String toString()
    {
        StringBuilder sb = (new StringBuilder()).append("{uid: ");
        sb.append(id).append(", name: ");
        sb.append(name).append(", lat/lng: (");
        sb.append(latitude).append(',');
        sb.append(longitude).append("), address: ");
        sb.append(address).append(", city: ");
        sb.append(city).append(", state: ");
        sb.append(state).append(", sightings_count: ");
        return sb.append(sightingsCount).append("}").toString();
    }

    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(secondName);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(address);
        parcel.writeString(fullAddress);
        parcel.writeString(city);
        parcel.writeString(state);
        parcel.writeString(phone);
        parcel.writeInt(sightingsCount);
        parcel.writeDouble(distance);
        parcel.writeString(googleID);
    }

    public static final android.os.Parcelable.Creator<Place> CREATOR = new android.os.Parcelable.Creator<Place>() {

        public Place createFromParcel(Parcel parcel)
        {
            return new Place(parcel);
        }

        public Place[] newArray(int i)
        {
            return new Place[i];
        }
    };
    
    private static final String LOG_TAG = "Place";
    public String address;
    public String city;
    public double distance;
    public String fullAddress;
    public String googleID;
    public double latitude = (0.0D / 0.0D);
    public String link;
    public String link_title;
    public double longitude = (0.0D / 0.0D);
    public String name;
    public String secondName;
    public String phone;
    public int sightingsCount;
    public String state;
    public int id;

}
