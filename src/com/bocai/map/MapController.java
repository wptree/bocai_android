// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MapController.java

package com.bocai.map;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.bocai.BocaiApplication;
import com.bocai.R;
import com.bocai.model.Filter;
import com.bocai.model.Sighting;
import com.bocai.util.Macros;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import java.util.List;

// Referenced classes of package com.bocai.map:
//            MapOverlays

public class MapController
    implements MapOverlays.CalloutClickListener
{
    public static interface MapControllerListener
    {

        public abstract boolean calloutClickedForSighting(Sighting sighting);

        public abstract void loadSightingsAtCoordinate(Location location, double d, int i);

        public abstract void updatedSortFilter();
    }

    public static interface ZoomListener
    {

        public abstract void onZoom(int i);
    }


    public MapController()
    {
        regionDidChangeCounter = 0;
        lastRefreshCounter = 0;
        isScrolling = false;
        tmpLocation = new Location("explicit");
        
        gestureListener = new android.view.GestureDetector.SimpleOnGestureListener() {

            public boolean onDoubleTapEvent(MotionEvent motionevent)
            {
                boolean flag1;
                if(motionevent.getAction() == 0)
                {
                    int i = (int)motionevent.getX();
                    int j = (int)motionevent.getY();
                    controller.zoomInFixing(i, j);
                    flag1 = true;
                } else
                {
                    flag1 = super.onDoubleTapEvent(motionevent);
                }
                return flag1;
            }

            public boolean onScroll(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1)
            {
                
                MapController.this.regionDidChangeCounter = MapController.this.regionDidChangeCounter + 1;
                if(!isScrolling)
                {                 
                    MapController.this.scrollingStartX = motionevent.getX();                    
                    MapController.this.scrollingStartY = motionevent.getY();
                    isScrolling = true;
                }
                return super.onScroll(motionevent, motionevent1, f, f1);
            }

        }
;
  
			zoomListener = new ZoomListener() {

            public void onZoom(int i)
            {
                MapController.this.regionDidChangeCounter = MapController.this.regionDidChangeCounter + 1;
            }

        }
;

    }

    public MapController(MapView mapview)
    {
        regionDidChangeCounter = 0;
        lastRefreshCounter = 0;
        isScrolling = false;
        tmpLocation = new Location("explicit");
       
        gestureListener = new android.view.GestureDetector.SimpleOnGestureListener() {

            public boolean onDoubleTapEvent(MotionEvent motionevent)
            {
                boolean flag1;
                if(motionevent.getAction() == 0)
                {
                    com.google.android.maps.MapController mapcontroller = controller;
                    int i = (int)motionevent.getX();
                    int j = (int)motionevent.getY();
                    boolean flag = mapcontroller.zoomInFixing(i, j);
                    flag1 = true;
                } else
                {
                    flag1 = super.onDoubleTapEvent(motionevent);
                }
                return flag1;
            }

            public boolean onScroll(MotionEvent motionevent, MotionEvent motionevent1, float f, float f1)
            {
                MapController.this.regionDidChangeCounter = MapController.this.regionDidChangeCounter + 1;
                if(!isScrolling)
                {
                    MapController.this.scrollingStartX = motionevent.getX();
                    MapController.this.scrollingStartY = motionevent.getY();
                    isScrolling = true;
                }
                return super.onScroll(motionevent, motionevent1, f, f1);
            }

        }
;
			zoomListener = new ZoomListener() {

            public void onZoom(int i)
            {
                MapController.this.regionDidChangeCounter = MapController.this.regionDidChangeCounter + 1;
            }
        }
;
        
        setMapView(mapview);
    }

    private void scrollRelease(MotionEvent motionevent)
    {
        isScrolling = false;
    }

    public void animateTo(Location location)
    {
        if(controller == null)
        {
            return;
        } else
        {
            int i = (int)(location.getLatitude() * 1000000D);
            int j = (int)(location.getLongitude() * 1000000D);
            GeoPoint geopoint = new GeoPoint(i, j);
            controller.animateTo(geopoint);
            return;
        }
    }

    public boolean calloutClickedForSighting(Sighting sighting)
    {
        boolean flag;
        if(_flddelegate != null)
            flag = _flddelegate.calloutClickedForSighting(sighting);
        else
            flag = false;
        return flag;
    }

    public void clearSightings()
    {
        if(overlays == null)
        {
            return;
        } else
        {
            overlays.clear();
            return;
        }
    }

    public void disableMyLocation()
    {
        if(myLocationOverlay == null)
        {
            return;
        } else
        {
            myLocationOverlay.disableMyLocation();
            return;
        }
    }

    public void enableMyLocation()
    {
        if(myLocationOverlay == null)
        {
            return;
        } else
        {
            myLocationOverlay.enableMyLocation();
            return;
        }
    }

    @Deprecated
    public double latitudeDeltaInMiles()
    {
    	double d = (double)mapView.getLatitudeSpan() / 1000000D;
        return (ApproxSizeOfOneDegreeLatitudeInMiles * d) / 2D;
    }
    
    public double latitudeDeltaInKms(){
    	double d = (double)mapView.getLatitudeSpan() / 1000000D;
    	return d * 111.0/2D;
    }
    

    public void plotSightings(List list)
    {
        if(overlays == null)
            return;
        if(overlays.size() >= 50)
            return;
        overlays.add(list);
        if(zoomOutInclude)
        {
            Location location = Macros.FS_CURRENT_LOCATION();
            zoomOutToIncludeAnnotations(location);
        }
        mapView.postInvalidate();
    }

    public void refreshFromResize(boolean flag)
    {
        if(showScanBestButton)
        {
            Filter.setBest();
            if(_flddelegate != null)
                _flddelegate.updatedSortFilter();
        }
        GeoPoint geopoint = mapView.getMapCenter();
        double d = (double)geopoint.getLatitudeE6() / 1000000D;
        tmpLocation.setLatitude(d);
       
        double d1 = (double)geopoint.getLongitudeE6() / 1000000D;
        tmpLocation.setLongitude(d1);
               
        double d2 = latitudeDeltaInKms();
        int i = 1;
        if(Filter.areaIsAnywhere())
            return;
        if(!flag)
        {
            int j = lastRefreshCounter;
            int k = regionDidChangeCounter;
            if(j < k)
                if(_flddelegate == null)
                {
                    return;
                } else
                {
                    _flddelegate.loadSightingsAtCoordinate(tmpLocation, d2, i);
                    return;
                }
        }
        if(!flag)
            return;

        if(regionDidChangeCounter != lastRefreshCounter)
            return;
        if(_flddelegate != null)
        {

            _flddelegate.loadSightingsAtCoordinate(tmpLocation, d2, i);
        }
        lastRefreshCounter = regionDidChangeCounter;

    }

    public void setCenter(Location location)
    {
        if(controller == null)
        {
            return;
        } else
        {
            int i = (int)(location.getLatitude() * 1000000D);
            int j = (int)(location.getLongitude() * 1000000D);
            GeoPoint geopoint = new GeoPoint(i, j);
            controller.setCenter(geopoint);
            return;
        }
    }

    public void setMapView(MapView mapview)
    {
        mapView = mapview;
        controller = mapview.getController();
        
        BocaiApplication bocaiApplication = Macros.FS_APPLICATION();
        mapView.setBuiltInZoomControls(false);
        android.graphics.drawable.Drawable drawable = bocaiApplication.getResources().getDrawable(R.drawable.map_pin);
       
        overlays = new MapOverlays(drawable, mapView);
        
        myLocationOverlay = new MyLocationOverlay(bocaiApplication, mapView);
        

        GestureDetector gesturedetector = new GestureDetector(bocaiApplication, gestureListener) {

            public boolean onTouchEvent(MotionEvent motionevent)
            {
                if(isScrolling && motionevent.getAction() == 1)
                    scrollRelease(motionevent);
                return super.onTouchEvent(motionevent);
            }

        }
;
        overlays.setGestureDetector(gesturedetector);
          
        overlays.setZoomListener(zoomListener);
        overlays.setCalloutListener(this);       
        mapView.getOverlays().add(myLocationOverlay);      
        mapView.getOverlays().add(overlays);
        controller.zoomToSpan(0x23883, 0);
    }

    public void setShowScanBestButton(boolean flag)
    {
        showScanBestButton = flag;
       
        if(!flag)
        	showScanButton = true;
        else
        	showScanButton = false;
       
        showNoButtons = false;
    }

    public void setShowScanButton(boolean flag)
    {
        showScanButton = flag;
        if(!flag)
        	showScanBestButton = true;
        else
        	showScanBestButton = false;
        showNoButtons = false;
    }

    public void setZoomOutInclude(boolean flag)
    {
        zoomOutInclude = flag;
    }

    public void zoomOutToIncludeAnnotations(Location location)
    {
        int i;
        int j;
        GeoPoint geopoint;
        if(overlays.size() < 1)
            return;
        i = overlays.getLatSpanE6();
        j = overlays.getLonSpanE6();
        geopoint = overlays.getCenter();
        if(geopoint == null)
            //break MISSING_BLOCK_LABEL_51;
        	return ;
        controller.animateTo(geopoint);
        int k = i * 2;
        int l = j * 2;
        controller.zoomToSpan(k, l);
        return;
        //Exception exception;
        //exception;
        //String s = exception.getLocalizedMessage();
        //int i1 = Log.e("MapController", s, exception);
        //exception.printStackTrace();
        //return;
    }

    static final double ApproxSizeOfOneDegreeLatitudeInMiles = 68.709999999999994D;
    private static final String LOG_TAG = "MapController";
    private static final int MAX_MAP_ITEMS = 50;
    static final float SCROLL_CHANGE_TOLERANCE = 60F;
    com.google.android.maps.MapController controller;
    public MapControllerListener _flddelegate;
    android.view.GestureDetector.SimpleOnGestureListener gestureListener;
    boolean isScrolling;
    public int lastRefreshCounter;
    MapView mapView;
    MyLocationOverlay myLocationOverlay;
    MapOverlays overlays;
    public int regionDidChangeCounter;
    float scrollingStartX;
    float scrollingStartY;
    boolean showNoButtons;
    boolean showScanBestButton;
    boolean showScanButton;
    Location tmpLocation;
    ZoomListener zoomListener;
    boolean zoomOutInclude;

}
