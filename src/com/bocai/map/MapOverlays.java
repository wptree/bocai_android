// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MapOverlays.java

package com.bocai.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.*;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.TextView;
import com.bocai.BocaiApplication;
import com.bocai.R;
import com.bocai.model.Sighting;
import com.bocai.util.Macros;
import com.google.android.maps.*;

import java.util.*;

// Referenced classes of package com.bocai.map:
//            ClusterOverlayItem, SightingOverlayItem

public class MapOverlays extends ItemizedOverlay
{
    public static interface CalloutClickListener
    {

        public abstract boolean calloutClickedForSighting(Sighting sighting);
    }


    public MapOverlays(Drawable drawable, MapView mapview)
    {
        super(drawable);

        boundCenter(drawable);
        map = null;
        callout = null;
        calloutListener = null;
        items = null;
        clusters = null;
        deviceIsHiRes = false;
        mapZoomLevel = 0;
        calloutClickListener = new android.view.View.OnClickListener() {

            public void onClick(View view3)
            {
                OverlayItem overlayitem = (OverlayItem)view3.getTag(R.id.key_item);                              
                Sighting sighting = null;
                if(overlayitem instanceof SightingOverlayItem)
                    sighting = ((SightingOverlayItem)overlayitem).sighting;              
                if(calloutListener != null)
                    calloutListener.calloutClickedForSighting(sighting);
                callout.setVisibility(8);
            }
        };
        
        map = mapview;
        mapZoomLevel = map.getZoomLevel();
        BocaiApplication bocaiApplication = Macros.FS_APPLICATION();
        resources = bocaiApplication.getResources();
        deviceIsHiRes = bocaiApplication.deviceIsHiRes;
        callout = LayoutInflater.from(bocaiApplication).inflate(R.layout.map_callout, null);
        int j;
        GeoPoint geopoint;
        
        com.google.android.maps.MapView.LayoutParams layoutparams;
       
        if(deviceIsHiRes)
            j = 65502;
        else
            j = 65512;
        geopoint = new GeoPoint(0, 0);
        
     //   geopoint = map.getMapCenter();
        
        
        
        
      //  layoutparams = new com.google.android.maps.MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, geopoint, 0, 200, 81);
        //layoutparams = new com.google.android.maps.MapView.LayoutParams(-1, -1, geopoint, com.google.android.maps.MapView.LayoutParams.BOTTOM_CENTER);
        
        
        int y = 0 - drawable.getBounds().width()/2;
        
        layoutparams = new com.google.android.maps.MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, geopoint,0,y,81);
        
        map.addView(callout, layoutparams);
        callout.setVisibility(8);
        callout.setClickable(true);
        callout.setOnClickListener(calloutClickListener);
        calloutInAnimation = AnimationUtils.loadAnimation(Macros.FS_APPLICATION(), R.anim.grow_fade_in_from_bottom);
        calloutInAnimation.setInterpolator(new BounceInterpolator());
        populate();
    }

    /**
     * @deprecated Method createClusters is deprecated
     */

    private void createClusters()
    {
    	ArrayList arraylist;
        if(clusters == null)
        {
        	clusters = new LinkedList();
        }
        ClusterOverlayItem.projection = map.getProjection();
        ClusterOverlayItem.map = map;
        //clusters.clear();

        if(items != null) {
        	Iterator iterator = items.iterator();
        	while (iterator.hasNext()) {
                SightingOverlayItem sightingoverlayitem = (SightingOverlayItem)iterator.next();
                sightingoverlayitem.setThumbnailSize(THUMBNAIL_SIZE_HI);
                
                Iterator it = clusters.iterator();
                boolean flag = false;
                while(it.hasNext()) {
                    ClusterOverlayItem clusteroverlayitem = (ClusterOverlayItem)it.next();
                    if(clusteroverlayitem.isItemInBounds(sightingoverlayitem)) {
                        clusteroverlayitem.addItem(sightingoverlayitem);
                        Drawable drawable = boundCenter(getClusterMarker());
                        clusteroverlayitem.setMarker(drawable);
                        flag = true;
                    }      	
                }
                if (!flag) {
                	ClusterOverlayItem clusteroverlayitem = new ClusterOverlayItem(sightingoverlayitem);
                    Drawable drawable = boundCenter(getDefaultMarker());
                    clusteroverlayitem.setMarker(drawable);
                    clusteroverlayitem.setThumbnailSize(THUMBNAIL_SIZE_HI);
                    clusters.add(clusteroverlayitem);
                }
        	}
            setLastFocusedIndex(-1);
            populate();
        } else {
            populate();
        	return ;
        }
/*        //this;
       // JVM INSTR monitorenter ;
        ArrayList arraylist;
        if(clusters == null)
        {
            LinkedList linkedlist = new LinkedList();
            clusters = linkedlist;
        }
        ClusterOverlayItem.projection = map.getProjection();
        ClusterOverlayItem.map = map;
        clusters.clear();
        arraylist = items;
        //if(arraylist != null) goto _L2; else goto _L1
_L1:
        //this;
        //JVM INSTR monitorexit ;
        //return;
_L2:
        Iterator iterator = items.iterator();
_L4:
        int i;
        ClusterOverlayItem clusteroverlayitem1;
        if(!iterator.hasNext())
            break MISSING_BLOCK_LABEL_229;
        SightingOverlayItem sightingoverlayitem = (SightingOverlayItem)iterator.next();
        boolean flag = false;
        Iterator iterator1 = clusters.iterator();
        do
        {
            if(!iterator1.hasNext())
                break;
            ClusterOverlayItem clusteroverlayitem = (ClusterOverlayItem)iterator1.next();
            if(!clusteroverlayitem.isItemInBounds(sightingoverlayitem))
                continue;
            clusteroverlayitem.addItem(sightingoverlayitem);
            flag = true;
            Drawable drawable = boundCenter(getClusterMarker());
            clusteroverlayitem.setMarker(drawable);
            break;
        } while(true);
        if(flag)
            continue;  Loop/switch isn't completed 
        clusteroverlayitem1 = new ClusterOverlayItem(sightingoverlayitem);
        Drawable drawable1 = boundCenter(getDefaultMarker());
        clusteroverlayitem1.setMarker(drawable1);
        if(!deviceIsHiRes)
            break;  Loop/switch isn't completed 
        i = null;
_L5:
        clusteroverlayitem1.setThumbnailSize(i);
        boolean flag1 = clusters.add(clusteroverlayitem1);
        if(true) goto _L4; else goto _L3
        Exception exception;
        exception;
        throw exception;
_L3:
        i = null;
          goto _L5
        i = null;
        setLastFocusedIndex(i);
        populate();
          goto _L1*/
    }

    /**
     * @deprecated Method add is deprecated
     */

    public void add(List list)
    {
    	if (list == null) {
    		return ;
    	}
    	int i = list.size();
    	if (i != 0) {
    		Sighting sighting = null;
    		Iterator iterator = list.iterator();
    		while(iterator.hasNext()) {
    			Object obj = iterator.next();
                if(obj instanceof Sighting) {
                	sighting = (Sighting)obj;
                	
                    int j;
                    int k;
                    int l;
                    int i1;
                    if(nw != null)
                    {
                        j = nw.getLatitudeE6();
                        k = nw.getLongitudeE6();
                    } else
                    {
                        j = (int)(sighting.latitude * 1000000D);
                        k = (int)(sighting.longitude * 1000000D);
                    }
                    if(se != null)
                    {
                        l = se.getLatitudeE6();
                        i1 = se.getLongitudeE6();
                    } else
                    {
                        l = (int)(sighting.latitude * 1000000D);
                        i1 = (int)(sighting.longitude * 1000000D);
                    }
                    if(items == null)
                    {
                    	items = new ArrayList();
                    }
                	
                    SightingOverlayItem sightingoverlayitem = new SightingOverlayItem(sighting);
                    items.add(sightingoverlayitem);
                    GeoPoint geopoint = sightingoverlayitem.getPoint();
                    int j1 = geopoint.getLatitudeE6();
                    j = Math.max(j, j1);
                    int k1 = geopoint.getLongitudeE6();
                    k = Math.min(k, k1);
                    int l1 = geopoint.getLatitudeE6();
                    l = Math.min(l, l1);
                    int i2 = geopoint.getLongitudeE6();
                    i1 = Math.max(i1, i2);
                    
                    GeoPoint geopoint1 = new GeoPoint(j, k);
                    nw = geopoint1;
                    GeoPoint geopoint2 = new GeoPoint(l, i1);
                    se = geopoint2;
                    int j2 = (int)((double)(j + l) / 2D);
                    int k2 = (int)((double)(k + i1) / 2D);
                    GeoPoint geopoint3 = new GeoPoint(j2, k2);
                    center = geopoint3;
                }                
    		}
            createClusters();
    	}
/*        this;
        JVM INSTR monitorenter ;
        if(list == null) goto _L2; else goto _L1
_L1:
        int i = list.size();
        if(i != 0) goto _L3; else goto _L2
_L2:
        this;
        JVM INSTR monitorexit ;
        return;
_L3:
        Sighting sighting = null;
        Iterator iterator = list.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            Sighting sighting1 = (Sighting)iterator.next();
            if(sighting1.getClass() != com/bocai/model/Sighting)
                continue;
            sighting = sighting1;
            break;
        } while(true);
        if(sighting != null)
        {
            Iterator iterator1;
            int j;
            int k;
            int l;
            int i1;
            if(nw != null)
            {
                j = nw.getLatitudeE6();
                k = nw.getLongitudeE6();
            } else
            {
                j = (int)(sighting.latitude * 1000000D);
                k = (int)(sighting.longitude * 1000000D);
            }
            if(se != null)
            {
                l = se.getLatitudeE6();
                i1 = se.getLongitudeE6();
            } else
            {
                l = (int)(sighting.latitude * 1000000D);
                i1 = (int)(sighting.longitude * 1000000D);
            }
            if(items == null)
            {
                ArrayList arraylist = new ArrayList();
                items = arraylist;
            }
            iterator1 = list.iterator();
            do
            {
                if(!iterator1.hasNext())
                    break;
                Sighting sighting2 = (Sighting)iterator1.next();
                if(sighting2.getClass() == com/bocai/model/Sighting)
                {
                    SightingOverlayItem sightingoverlayitem = new SightingOverlayItem(sighting2);
                    boolean flag = items.add(sightingoverlayitem);
                    GeoPoint geopoint = sightingoverlayitem.getPoint();
                    int j1 = geopoint.getLatitudeE6();
                    j = Math.max(j, j1);
                    int k1 = geopoint.getLongitudeE6();
                    k = Math.min(k, k1);
                    int l1 = geopoint.getLatitudeE6();
                    l = Math.min(l, l1);
                    int i2 = geopoint.getLongitudeE6();
                    i1 = Math.max(i1, i2);
                }
            } while(true);
            GeoPoint geopoint1 = new GeoPoint(j, k);
            nw = geopoint1;
            GeoPoint geopoint2 = new GeoPoint(l, i1);
            se = geopoint2;
            int j2 = (int)((double)(j + l) / 2D);
            int k2 = (int)((double)(k + i1) / 2D);
            GeoPoint geopoint3 = new GeoPoint(j2, k2);
            center = geopoint3;
            createClusters();
        }
        if(true) goto _L2; else goto _L4
_L4:
        Exception exception;
        exception;
        throw exception;*/
    }

    /**
     * @deprecated Method clear is deprecated
     */

    public void clear()
    {
        if(items != null)
            items.clear();
        if(clusters != null)
            clusters.clear();
        center = null;
        se = null;
        nw = null;
        setLastFocusedIndex(-1);
        populate();
/*        this;
        JVM INSTR monitorenter ;
        if(items != null)
            items.clear();
        if(clusters != null)
            clusters.clear();
        center = null;
        se = null;
        nw = null;
        setLastFocusedIndex(-1);
        populate();
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;*/
    }

    protected OverlayItem createItem(int i)
    {
        Object obj;
        if(clusters == null)
        {
            obj = null;
        } else
        {
            ClusterOverlayItem clusteroverlayitem = (ClusterOverlayItem)clusters.get(i);
            clusteroverlayitem.updateMarker();
            obj = clusteroverlayitem;
        }
        return ((OverlayItem) (obj));
    }

    /**
     * @deprecated Method draw is deprecated
     */

    public void draw(Canvas canvas, MapView mapview, boolean flag)
    {
        int i = mapview.getZoomLevel();
        int j = mapZoomLevel;
        if(i != j)
        {
            mapZoomLevel = i;
            createClusters();
            if(zoomListener != null)
                zoomListener.onZoom(i);
        }

/*        if (center != null) {
        	Log.i("MapOverlays +++++++++++++++++++  ", center.toString());
        }*/
        // TODO temprorily
/*		Projection projection = mapview.getProjection();
		Point point = new Point();
		projection.toPixels(center, point);
		
		
		
		Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon);
		canvas.drawBitmap(bitmap, point.x-bitmap.getWidth()/2, point.y-bitmap.getHeight(),null);
*/      super.draw(canvas, mapview, false);
/*        this;
        JVM INSTR monitorenter ;
        int i = mapview.getZoomLevel();
        int j = mapZoomLevel;
        if(i != j)
        {
            mapZoomLevel = i;
            createClusters();
            if(zoomListener != null)
                zoomListener.onZoom(i);
        }
        super.draw(canvas, mapview, false);
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;*/
    }

    public final Drawable getClusterMarker()
    {
        Drawable drawable = resources.getDrawable(R.drawable.map_cluster_marker_bg);
        BitmapDrawable bitmapdrawable = new BitmapDrawable(resources);
        Drawable adrawable[] = new Drawable[2];
        adrawable[0] = drawable;
        adrawable[1] = bitmapdrawable;
        LayerDrawable layerdrawable = new LayerDrawable(adrawable);
        layerdrawable.setId(1, R.id.thumb);
        return layerdrawable;
    }

    public final Drawable getDefaultMarker()
    {
        Drawable drawable = resources.getDrawable(R.drawable.map_marker_bg);
        BitmapDrawable bitmapdrawable = new BitmapDrawable(resources);
        Drawable adrawable[] = new Drawable[2];
        adrawable[0] = drawable;
        adrawable[1] = bitmapdrawable;
        LayerDrawable layerdrawable = new LayerDrawable(adrawable);
        layerdrawable.setId(1, R.id.thumb);
        return layerdrawable;
    }

    public int getLatSpanE6()
    {
        int k;
        if(nw != null && se != null)
        {
            int i = nw.getLatitudeE6();
            int j = se.getLatitudeE6();
            k = i - j;
        } else
        {
            k = super.getLatSpanE6();
        }
        return k;
    }

    public int getLonSpanE6()
    {
        int k;
        if(nw != null && se != null)
        {
            int i = se.getLongitudeE6();
            int j = nw.getLongitudeE6();
            k = i - j;
        } else
        {
            k = super.getLonSpanE6();
        }
        return k;
    }

    protected boolean onTap(int i)
    {
    	Log.i("MapOverlays =====onTap===== ", "" + i + " " + clusters);

        boolean flag;
        if(clusters == null)
        {
            flag = false;
        } else
        {
            ClusterOverlayItem clusteroverlayitem = (ClusterOverlayItem)clusters.get(i);
            clusteroverlayitem.onTap();
            if(clusteroverlayitem.items.size() > 1)
                populate();
            OverlayItem overlayitem = clusteroverlayitem.getSelectedItem();
            com.google.android.maps.MapView.LayoutParams layoutparams = (com.google.android.maps.MapView.LayoutParams)callout.getLayoutParams();
            GeoPoint geopoint = clusteroverlayitem.getPoint();
            layoutparams.point = geopoint;
            TextView textview = (TextView)callout.findViewById(R.id.title);
            String s = overlayitem.getTitle();
        	Log.i("MapOverlays =====onTap===== ", "" + s);

            textview.setText(s);
            TextView textview1 = (TextView)callout.findViewById(R.id.detail);
            String s1 = overlayitem.getSnippet();
        	Log.i("MapOverlays =====onTap===== ", "" + s1);

            
            textview1.setText(s1);
            com.google.android.maps.MapController mapcontroller = map.getController();
            GeoPoint geopoint1 = clusteroverlayitem.getPoint();
            mapcontroller.animateTo(geopoint1);
            callout.setVisibility(0);
            callout.startAnimation(calloutInAnimation);
            callout.setTag(R.id.key_item, overlayitem);
            flag = true;
        }
        return flag;
    }

    public boolean onTouchEvent(MotionEvent motionevent, MapView mapview)
    {
    	Log.i("MapOverlays =====onTouchEvent===== ", motionevent.toString());
        boolean flag;
        if(items == null || items.size() == 0)
        {
            flag = false;
        } else
        {
            boolean flag1 = super.onTouchEvent(motionevent, mapview);
            if(!flag1)
                callout.setVisibility(8);
            if(!flag1 && gestureDetector != null && gestureDetector.onTouchEvent(motionevent))
                flag1 = true;
            flag = flag1;
        }
        return flag;
    }

    public void setCalloutListener(CalloutClickListener calloutclicklistener)
    {
        calloutListener = calloutclicklistener;
    }

    public void setGestureDetector(GestureDetector gesturedetector)
    {
        gestureDetector = gesturedetector;
    }

    public void setZoomListener(MapController.ZoomListener zoomlistener)
    {
        zoomListener = zoomlistener;
    }

    public int size()
    {
        int i;
        if(clusters == null)
            i = 0;
        else
            i = clusters.size();
        return i;
    }

    private static final int CALLOUT_Y_OFFSET = 232;
    private static final int CALLOUT_Y_OFFSET_HI = 222;
    public static final int KEY_MARKER_INDEX = 1;
    private static final int THUMBNAIL_SIZE = 38;
    private static final int THUMBNAIL_SIZE_HI = 58;
    View callout;
    android.view.View.OnClickListener calloutClickListener;
    Animation calloutInAnimation;
    CalloutClickListener calloutListener;
    GeoPoint center;
    LinkedList clusters;
    boolean deviceIsHiRes;
    GestureDetector gestureDetector;
    ArrayList items;
    MapView map;
    int mapZoomLevel;
    GeoPoint nw;
    Resources resources;
    GeoPoint se;
    MapController.ZoomListener zoomListener;
}
