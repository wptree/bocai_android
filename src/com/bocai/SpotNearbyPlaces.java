package com.bocai;

import android.location.Location;
import android.util.Log;

import com.bocai.model.FSObject;
import com.bocai.model.FSObjectDelegate;
import com.bocai.model.Place;
import com.bocai.util.Macros;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;

public class SpotNearbyPlaces
    implements FSObjectDelegate
{
    public static interface SpotNearbyPlacesListener
    {

        public abstract void nearbyPlacesFinished();
    }


    public SpotNearbyPlaces()
    {
        place = null;
        nearbyPlaces = null;
        listener = null;
        locationIsUpdating = false;
        firstUpdatedLocation = false;
        lastKnownLocation = null;
    }

    public void FSResponse(List<FSObject> list)
    {
        if(list == null)
            return;
        Iterator<FSObject> iterator;
        if(nearbyPlaces == null)
        {
        	nearbyPlaces = new LinkedList<FSObject>();
        } else
        {
            nearbyPlaces.clear();
        }
        for(iterator = list.iterator(); iterator.hasNext();)
        {
            FSObject fsobject = iterator.next();
            nearbyPlaces.add(fsobject);
        }

        locationIsUpdating = false;
        if(listener == null)
        {
            return;
        } else
        {
            listener.nearbyPlacesFinished();
            return;
        }
    }

    public void displayErrors(JSONObject jsonobject)
        throws JSONException
    {
    }

    public void displaySuccess(JSONObject jsonobject)
        throws JSONException
    {
    }

    public void doSearchWithName(String s)
    {
    }

    public void finishedAction(JSONObject jsonobject)
        throws JSONException
    {
    }

    public boolean locationHasChanged()
    {
      if(firstUpdatedLocation && lastKnownLocation != null){
    	  Location location = Macros.FS_CURRENT_LOCATION();
    	  if(location == null){
    		  return true;
    	  }
    	  double d = lastKnownLocation.getLatitude();
    	  double d1 = location.getLatitude();
    	  if(d == d1)
          {
              double d2 = lastKnownLocation.getLongitude();
              double d3 = location.getLongitude();
              if(d2 == d3){
            	  return false;
              }
          }
    	  
      }else{
    	  return true;
      }
      return true;
    }

    public void updateLocation(Location location)
    {
        if(location == null)
        {
            return;
        } else
        {
        	lastKnownLocation = new Location(location);
            firstUpdatedLocation = true;
            return;
        }
    }

    public void updateNearbyPlaces()
    {
    	//TODO: just try the function
    	Log.w("SpotNearbyPlaces", "unimplemented method updateNearbyPlaces");
        locationIsUpdating = true;
        Location location = Macros.FS_CURRENT_LOCATION();
        updateLocation(location);
        if(place != null)
        {
            if(!place.wasCompleted || place.wasCancelled)
                place.cancelRequests();
            place = null;
        }
        place = new Place();
        place.delegate = this;
        place.nearestPlacesAtLocation(lastKnownLocation);
        
    }

    boolean firstUpdatedLocation;
    public Location lastKnownLocation;
    public SpotNearbyPlacesListener listener;
    public boolean locationIsUpdating;
    public List<FSObject> nearbyPlaces;
    Place place;
}
