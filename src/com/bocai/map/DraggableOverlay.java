// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DraggableOverlay.java

package com.bocai.map;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.ImageView;
import android.widget.ScrollView;
import com.google.android.maps.*;
import java.util.*;

public class DraggableOverlay extends ItemizedOverlay
{

    public DraggableOverlay(Drawable drawable, ImageView imageview)
    {
    	super(drawable);
        boundCenterBottom(drawable);
        
        items = new ArrayList();
      
        draggedItem = null;
        scrollView = null;
        wasDragged = false;
        defaultMarker = drawable;
        draggableView = imageview;
        draggableParent = (View)draggableView.getParent();
        
        markerAnchorX = imageview.getDrawable().getIntrinsicWidth() / 2;
        
        markerAnchorY = imageview.getDrawable().getIntrinsicHeight();
        
        yOff = 0;
        xOff = 0;
        tmpPoint = new Point(0, 0);
        
        ViewParent viewparent = imageview.getParent();
        do
        {
            if(viewparent == null)
                return;
            if(viewparent != null && (viewparent instanceof ScrollView))
            {
            	scrollView = (ScrollView)viewparent;              
                return;
            }
            viewparent = viewparent.getParent();
        } while(true);
    }

    private void moveDraggableTo(int i, int j)
    {
        android.view.ViewGroup.LayoutParams layoutparams = draggableView.getLayoutParams();
        if(layoutparams instanceof com.google.android.maps.MapView.LayoutParams)
        {
            com.google.android.maps.MapView.LayoutParams layoutparams1 = (com.google.android.maps.MapView.LayoutParams)layoutparams;
            int k = xOff;
            int l = i - k;
            layoutparams1.x = l;
            int i1 = yOff;
            int j1 = j - i1;
            layoutparams1.y = j1;
            draggableView.setLayoutParams(layoutparams1);
            return;
        }
        if(!(layoutparams instanceof android.view.ViewGroup.MarginLayoutParams))
        {
            return;
        } else
        {

            int i2 = draggableParent.getHeight() - draggableView.getHeight();
            int k2 = j - markerAnchorY;
           
            int i3 = Math.min(k2 - yOff, i2);
            android.view.ViewGroup.MarginLayoutParams marginlayoutparams = (android.view.ViewGroup.MarginLayoutParams)layoutparams;
            int k3 = i - markerAnchorX;
         
            int i4 = k3 - xOff;
            marginlayoutparams.setMargins(i4, i3, 0, 0);
            draggableView.setLayoutParams(marginlayoutparams);
            return;
        }
    }

    public void add(GeoPoint geopoint, String s, String s1)
    {
        OverlayItem overlayitem = new OverlayItem(geopoint, s, s1);
        items.add(overlayitem);
        populate();
    }

    public void centerOnDraggable(MapView mapview, boolean flag)
    {
        if(items == null)
            return;
        if(items.size() <= 0)
            return;
        GeoPoint geopoint = ((OverlayItem)items.get(0)).getPoint();
        int i;
        int j;
        if(flag)
            mapview.getController().animateTo(geopoint);
        else
            mapview.getController().setCenter(geopoint);
        i = draggableParent.getWidth() / 2;
        j = draggableParent.getHeight() / 2;
        yOff = 0;
        xOff = 0;
        moveDraggableTo(i, j);
    }

    public void clear()
    {
        items.clear();
        populate();
    }

    protected OverlayItem createItem(int i)
    {
        return (OverlayItem)items.get(i);
    }

    public boolean onTouchEvent(MotionEvent motionevent, MapView mapview)
    {
        int i = motionevent.getAction();
        int j = (int)motionevent.getX();
        int k = (int)motionevent.getY();
        boolean flag = false;
        boolean flag2;
        if(i == 0)
        {
            Iterator iterator = items.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                OverlayItem overlayitem = (OverlayItem)iterator.next();
                Projection projection = mapview.getProjection();
                GeoPoint geopoint = overlayitem.getPoint();
                Point point = tmpPoint;
                projection.toPixels(geopoint, point);
                Drawable drawable = defaultMarker;
             
                int i1 = j - tmpPoint.x;               
                int k1 = k - tmpPoint.y;
                if(!hitTest(overlayitem, drawable, i1, k1))
                    continue;
                flag = true;
                draggedItem = overlayitem;
                items.remove(draggedItem);
                populate();
                xOff = 0;
                yOff = 0;

                moveDraggableTo(tmpPoint.x, tmpPoint.y);
                draggableView.setVisibility(0);
                xOff = j - tmpPoint.x;              
                yOff = k - tmpPoint.y;
            
                break;
            } while(true);
            if(scrollView != null)
                scrollView.requestDisallowInterceptTouchEvent(true);
        } else
        if(i == 2 && draggedItem != null)
        {
            moveDraggableTo(j, k);
            flag = true;
        } else
        if(i == 1 && draggedItem != null)
        {
            wasDragged = true;
            Projection projection1 = mapview.getProjection();
           
            int k3 = j - xOff;
            int i4 = k - yOff;
            GeoPoint geopoint1 = projection1.fromPixels(k3, i4);
           
            OverlayItem overlayitem2 = new OverlayItem(geopoint1, draggedItem.getTitle(), draggedItem.getSnippet());
            items.add(overlayitem2);
            populate();
            draggedItem = null;
            flag = true;
            moveDraggableTo(j, k);
            draggableView.setVisibility(8);
        }
        if(flag)
            flag2 = true;
        else
            flag2 = super.onTouchEvent(motionevent, mapview);
        return flag2;
    }

    public int size()
    {
        return items.size();
    }

    private Drawable defaultMarker;
    private View draggableParent;
    private View draggableView;
    private OverlayItem draggedItem;
    private List items;
    private int markerAnchorX;
    private int markerAnchorY;
    private ScrollView scrollView;
    private Point tmpPoint;
    public boolean wasDragged;
    private int xOff;
    private int yOff;
}
