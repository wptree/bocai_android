
package com.bocai.map;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;

import com.bocai.R;
import com.google.android.maps.*;
import java.util.LinkedList;


public class ClusterOverlayItem extends OverlayItem
{

    public ClusterOverlayItem(GeoPoint geopoint, String s, String s1)
    {
        super(geopoint, s, s1);
        items = null;
        selectedIndex = 0;
        bounds = null;
        tmpPoint = new Point();
       
        SightingOverlayItem.ImageListener imagelistener = new SightingOverlayItem.ImageListener() {

            public void onImageDownloaded(SightingOverlayItem sightingoverlayitem, Bitmap bitmap)
            {
                Object obj = items.get(selectedIndex);
                if(sightingoverlayitem != obj)
                    return;
                updateMarker();
                if(ClusterOverlayItem.map == null)
                {
                    return;
                } else
                {
                    int j = thumbnailSize / 2;
                    MapView mapview = ClusterOverlayItem.map;
                    int k = bounds.left - j;
                    int l = bounds.top - j;
                    int i1 = bounds.right + j;
                    int j1 = bounds.bottom + j;
                    mapview.postInvalidate(k, l, i1, j1);
                    return;
                }
            }
        };
        
        sightingImageListener = imagelistener;
    }

    public ClusterOverlayItem(OverlayItem overlayitem)
    {
    	super(overlayitem.getPoint(), overlayitem.getTitle(), overlayitem.getSnippet());
        GeoPoint geopoint = overlayitem.getPoint();
        String s = overlayitem.getTitle();
        String s1 = overlayitem.getSnippet();
        
        items = null;
        selectedIndex = 0;
        bounds = null;
        tmpPoint = new Point();
        ;
        sightingImageListener = new SightingOverlayItem.ImageListener() {

            public void onImageDownloaded(SightingOverlayItem sightingoverlayitem, Bitmap bitmap)
            {
                Object obj = items.get(selectedIndex);
                if(sightingoverlayitem != obj)
                    return;
                updateMarker();
                if(ClusterOverlayItem.map == null)
                {
                    return;
                } else
                {
                    int j = thumbnailSize / 2;
                    MapView mapview = ClusterOverlayItem.map;
                    int k = bounds.left - j;
                    int l = bounds.top - j;
                    int i1 = bounds.right + j;
                    int j1 = bounds.bottom + j;
                    mapview.postInvalidate(k, l, i1, j1);
                    return;
                }
            }

        }
;
        items = new LinkedList();
        items.add(overlayitem);
        Point point2 = projection.toPixels(overlayitem.getPoint(), tmpPoint);
        int i = tmpPoint.x;
        int j = threshold;
        int k = i - j;
        int l = tmpPoint.y;
        int i1 = threshold;
        int j1 = l - i1;
        int k1 = tmpPoint.x;
        int l1 = threshold;
        int i2 = k1 + l1;
        int j2 = tmpPoint.y;
        int k2 = threshold;
        int l2 = j2 + k2;
        Rect rect = new Rect(k, j1, i2, l2);
        bounds = rect;
    }

    public void addItem(OverlayItem overlayitem)
    {
        if(items == null)
        {
        	items = new LinkedList();           
            projection.toPixels(overlayitem.getPoint(), tmpPoint);
            int i = tmpPoint.x;
            int j = threshold;
            int k = i - j;
            int l = tmpPoint.y;
            int i1 = threshold;
            int j1 = l - i1;
            int k1 = tmpPoint.x;
            int l1 = threshold;
            int i2 = k1 + l1;
            int j2 = tmpPoint.y;
            int k2 = threshold;
            int l2 = j2 + k2;
            Rect rect = new Rect(k, j1, i2, l2);
            bounds = rect;
        }
        items.add(overlayitem);
    }

    public OverlayItem getItem(int i)
    {
    	if (items == null) {
    		return null;
    	} else {
    		int j = items.size();
    		if(i < j) {
    			OverlayItem overlayitem = (OverlayItem)items.get(i);
    			return overlayitem;
    		} else {
    			return null;
    		}
    	}
    }

    public OverlayItem getSelectedItem()
    {
        OverlayItem overlayitem;
        if(items == null)
        {
            overlayitem = null;
        } else
        {
            overlayitem = (OverlayItem)items.get(selectedIndex);
        }
        return overlayitem;
    }

    public boolean isItemInBounds(OverlayItem overlayitem)
    {
        projection.toPixels(overlayitem.getPoint(), tmpPoint);
        return bounds.contains(tmpPoint.x, tmpPoint.y);
    }

    public boolean onTap()
    {
        int i = items.size();
        if(i > 0)
        {
        	selectedIndex = selectedIndex + 1;
            if(selectedIndex >= i)
                selectedIndex = 0;
            updateMarker();
        }
        return false;
    }

    public void setThumbnailSize(int i)
    {
        thumbnailSize = i;
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("ClusterOverlayItem { bounds: ");
        String s = bounds.toShortString();
        stringbuilder.append(s).append(", items: ");
        stringbuilder.append(items).append("}");
        return stringbuilder.toString();
    }

    public void updateMarker()
    {
        android.graphics.drawable.Drawable drawable = getMarker(0);
        if(!(drawable instanceof LayerDrawable))
            return;

        SightingOverlayItem sightingoverlayitem = (SightingOverlayItem)items.get(selectedIndex);
        Bitmap bitmap = sightingoverlayitem.getBitmap();
        if(bitmap == null)
        {
        	sightingoverlayitem.listener = sightingImageListener;
            return;
        }
        LayerDrawable layerdrawable = (LayerDrawable)drawable;
        BitmapDrawable bitmapdrawable = new BitmapDrawable(bitmap);
        bitmapdrawable.setAntiAlias(true);
        bitmapdrawable.setFilterBitmap(true);
        if(thumbnailSize <= 0)
        {
            return;
        } else
        {
            int j = thumbnailSize / 2;
            int k = -j;
            int l = -j;
            bitmapdrawable.setBounds(k, l, j, j);
            layerdrawable.setDrawableByLayerId(R.id.thumb, bitmapdrawable);
            return;
        }
    }

    private static final String LOG_TAG = "ClusterOverlayItem";
    public static MapView map = null;
    public static Projection projection = null;
    public static int threshold = 8;
    public Rect bounds;
    public LinkedList items;
    public int selectedIndex;
    SightingOverlayItem.ImageListener sightingImageListener;
    public int thumbnailSize;
    private Point tmpPoint;

}
