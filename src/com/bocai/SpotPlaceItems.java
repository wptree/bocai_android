package com.bocai;

import com.bocai.model.FSObject;
import com.bocai.model.FSObjectDelegate;
import com.bocai.model.Item;
import com.bocai.model.Place;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;

public class SpotPlaceItems
    implements FSObjectDelegate
{
    public static interface SpotPlaceItemsListener
    {
        public abstract void placeItemsFinished();
    }


    public SpotPlaceItems()
    {
        item = null;
        placeItems = null;
        listener = null;
        isUpdating = false;
        lastPlace = null;
    }

    public void FSResponse(List<FSObject> list)
    {
        if(list == null)
            return;
        Iterator<FSObject> iterator;
        if(placeItems == null)
        {
        	placeItems = new LinkedList<FSObject>();
        } else
        {
            placeItems.clear();
        }
        for(iterator = list.iterator(); iterator.hasNext();)
        {
            FSObject fsobject = iterator.next();
            placeItems.add(fsobject);
        }

        isUpdating = false;
        if(listener == null)
        {
            return;
        } else
        {
            listener.placeItemsFinished();
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

    public boolean isSamePlace(Place place)
    {
      if(place == null || lastPlace == null){
    	  return false;
      }
      int i = place.id;
      int j = lastPlace.id;
      if(i == j){
    	  return false;
      }
      return true;
    }

    public void updatePlaceItems(Place place)
    {
        if(isSamePlace(place))
            return;
        isUpdating = true;
        if(item != null)
        {
            if(!item.wasCompleted || item.wasCancelled){
                item.cancelRequests();
            }
            item = null;
        }
        lastPlace = place;
        item = new Item();
        item.delegate = this;
        item.itemsAtPlace(lastPlace);
    }

    public boolean isUpdating;
    Item item;
    public Place lastPlace;
    public SpotPlaceItemsListener listener;
    public List<FSObject> placeItems;
}
