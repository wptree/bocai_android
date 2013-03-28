// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SightingViewAdapter.java

package com.bocai.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.bocai.R;
import com.bocai.model.*;
import com.bocai.net.ImageDownloadRequest;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
public class SightingViewAdapter extends ArrayAdapter
    implements com.bocai.net.ImageDownloadRequest.OnDownloadCompleteListener, com.bocai.model.Sighting.OnDecodeCompleteListener
{
    class CallbackRec
    {

        Bitmap bitmap;
        ProgressBar progress;
        ImageView thumb;

        CallbackRec()
        {
            super();
        }
    }


    public SightingViewAdapter(Context context, int i, List list)
    {
        super(context, i, list);
        adapterView = null;
        android.os.Handler.Callback callback = new android.os.Handler.Callback() {

            public boolean handleMessage(Message message)
            {
                if(message.obj != null)
                {
                    CallbackRec callbackrec = (CallbackRec)message.obj;
                    if(callbackrec.thumb != null && callbackrec.bitmap != null)
                    {
                        ImageView imageview = callbackrec.thumb;
                        Bitmap bitmap = callbackrec.bitmap;
                        imageview.setImageBitmap(bitmap);
                    }
                    callbackrec.progress.setVisibility(View.GONE);
                    callbackrec.thumb = null;
                    callbackrec.bitmap = null;
                    callbackrec.progress = null;
                }
                return true;
            }
        };
        
        handlerCallback = callback;
        _objects = list;
        _itemParentId = i;
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        _inflater = layoutinflater;
        activeRequests = new ConcurrentHashMap();   
    }

    public long getItemId(int i)
    {
        Sighting sighting = (Sighting)_objects.get(i);
        long l1;
        if(sighting instanceof Promo)
        {
            long l = i;
            l1 = 0x8000000000000000L + l;
        } else
        {
            String s = sighting.sightingID;
            if(s != null)
            {
                l1 = Integer.parseInt(s);
            } else
            {
                StringBuilder stringbuilder = (new StringBuilder()).append("getItemId(").append(i).append("): Sighting ID is NULL! ");
                Object obj = _objects.get(i);
                String s1 = stringbuilder.append(obj).toString();
                Log.d("SightingViewAdapter", s1);
                for(Iterator iterator = _objects.iterator(); iterator.hasNext();)
                {
                    Sighting sighting1 = (Sighting)iterator.next();
                    String s2 = (new StringBuilder()).append("\t\t\t").append(sighting1).toString();
                    Log.d("SightingViewAdapter", s2);
                }

                l1 = 0L;
            }
        }
        return l1;
    }

    public View getView(int i, View view, ViewGroup viewgroup)
    {
        TextView textview2;
        ImageView imageview1;
        ProgressBar progressbar;
        TextView textview;
        TextView textview1;
        ImageView imageview;
        Sighting sighting;
        TextView textview4;
        String s3;
        Bitmap bitmap;

    	//Log.i("SightingViewAdapter", "***************** " + count);
    	count++;
    	
/*    	if (count < 1000000000) {
    		return view;
    	}*/
    	
        if(view == null)
        {

            view = _inflater.inflate(_itemParentId, null);       
            view.setWillNotCacheDrawing(true);
            Object aobj[] = new Object[6];
            
            textview = (TextView)view.findViewById(R.id.title);
            textview1 = (TextView)view.findViewById(R.id.detail);
            textview2 = (TextView)view.findViewById(R.id.ribbon_count);
            imageview = (ImageView)view.findViewById(R.id.thumb);
            imageview1 = (ImageView)view.findViewById(R.id.ribbon);
            progressbar = (ProgressBar)view.findViewById(R.id.progress);
            
            aobj[0] = textview;
            aobj[1] = textview1;
            aobj[2] = imageview;
            aobj[3] = textview2;
            aobj[4] = imageview1;
            aobj[5] = progressbar;
            view.setTag(R.id.key_objects, ((Object) (aobj)));
        } else
        {
            Object aobj2[] = (Object[])view.getTag(R.id.key_objects);
            textview = (TextView)aobj2[0];
            textview1 = (TextView)aobj2[1];
            imageview = (ImageView)aobj2[2];
            textview2 = (TextView)aobj2[3];
            imageview1 = (ImageView)aobj2[4];
            progressbar = (ProgressBar)aobj2[5];
            Drawable drawable = imageview.getDrawable();
            if(drawable != null && (drawable instanceof BitmapDrawable))
            {
                Bitmap bitmap1 = ((BitmapDrawable)drawable).getBitmap();
                if(bitmap1 != null)
                    bitmap1.recycle();
            }
            if(drawable != null)
            {
                android.graphics.drawable.Drawable.Callback callback = null;
                drawable.setCallback(callback);
            }
            imageview.setImageBitmap(null);
        }
 
        sighting = (Sighting)_objects.get(i);
        if(sighting instanceof Promo)
        {
            String s = ((Promo)sighting).text;
            textview.setText(s);
        } else
        {
            StringBuilder stringbuilder = new StringBuilder();
            String s6 = sighting.item.name;
            StringBuilder stringbuilder1 = stringbuilder.append(s6).append(" @ ");
            String s7 = sighting.place.name;
            String s8 = stringbuilder1.append(s7).toString();
            textview.setText(s8);
        }
        s3 = sighting.detailInfo;
        textview4 = textview1;
        textview4.setText(s3);
        bitmap = sighting.getThumb280();
        if(bitmap != null)
        {
            imageview.setImageBitmap(bitmap);
           // progressbar.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
        } else
        {
//label0:
            {
                imageview.setImageBitmap(null);
/*                if(sighting.decodeThumb280(this, Integer.valueOf(i))) {
                   // break MISSING_BLOCK_LABEL_815;
                	return view;
                }*/
                String s10 = sighting.thumb280URL;
                if(s10 == null) {
                    //break label0;
                	return view;
                }

                if(activeRequests.containsKey(s10)) {
                    //break label0;
                	return view;
                }
                
                //Log.i("SightingViewAdapter", "----- sighting.thumb280URL:" + s10);
                
                progressbar.setVisibility(0);
                ImageDownloadRequest imagedownloadrequest = new ImageDownloadRequest(s10);
                imagedownloadrequest.downloadListener = this;
                imagedownloadrequest.userData = i;
                imagedownloadrequest.execute();
                activeRequests.put(s10, imagedownloadrequest);
            }
        }
//_L1:
        if(sighting.ribbonsCount > 0)
        {
            imageview1.setVisibility(0);
            textview2.setText(Integer.toString(sighting.ribbonsCount));
            textview2.setVisibility(0);
        } else
        {
            imageview1.setVisibility(8);
            textview2.setVisibility(8);
        }
        view.setTag(R.id.key_position, i);
        //return view;
       // progressbar.setVisibility(8);
          //goto _L1
        //progressbar.setVisibility(View.GONE);
         // goto _L1
        
        //Log.i("SightingViewAdapter", "!!!!!!!!!!!!!!!! view.getId() " + view.getId());
        //Log.i("SightingViewAdapter", "!!!!!!!!!!!!!!!! view.getTag(R.id.key_objects) " + view.getTag(R.id.key_objects));
        return view;
    }

    public void notifyDataSetChanged()
    {
        activeRequests.clear();
        super.notifyDataSetChanged();
    }

    public void onDecodeComplete(Sighting sighting, Bitmap bitmap, Object obj)
    {
        int i = ((Integer)obj).intValue();
        setImageViewBitmap(i, bitmap);
    }

    public void onDownloadComplete(ImageDownloadRequest imagedownloadrequest, Bitmap bitmap)
    {
        int i = ((Integer)imagedownloadrequest.userData).intValue();
        setImageViewBitmap(i, bitmap);
        activeRequests.remove(imagedownloadrequest.url);
    }

    public void onDownloadError(ImageDownloadRequest imagedownloadrequest, String s)
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("Error downloading thumbnail for Sighting: ");
        String s1 = imagedownloadrequest.url;
        String s2 = stringbuilder.append(s1).append(": ").append(s).toString();
        Log.e("SightingViewAdapter", s2);
    }

    public void setAdapterView(AdapterView adapterview)
    {
        adapterView = adapterview;
    }

    void setImageViewBitmap(int i, Bitmap bitmap)
    {
        int j = adapterView.getFirstVisiblePosition();
        int k = i - j;
        //Log.i("SightingViewAdapter", "&&&&&&&&&&&&&& i,j " + i + "," + j);
        if(k < 0)
            return;
        int l = adapterView.getChildCount();
        //Log.i("SightingViewAdapter", "&&&&&&&&&&&&&& l " + l);

        if(k >= l)
            return;
        // TODO temprorily set k=1
        // k = 1;
        //Log.i("SightingViewAdapter", "&&&&&&&&&&&&&& testview(0).getTag(R.id.key_objects); " + adapterView.getChildAt(0).getTag(R.id.key_objects));
        //Log.i("SightingViewAdapter", "&&&&&&&&&&&&&& testview(1).getTag(R.id.key_objects); " + adapterView.getChildAt(1).getTag(R.id.key_objects));

        View view = adapterView.getChildAt(k);
        
        if(view == null)
            return;
        
        Object obj = view.getTag(R.id.key_objects);

        //Log.i("SightingViewAdapter", "&&&&&&&&&&&&&& obj" + obj );

        Object aobj[] = (Object[])obj;
        if(aobj == null)
            return;

        ImageView imageview = (ImageView)aobj[2];
        ProgressBar progressbar = (ProgressBar)aobj[5];
        Message message = uiHandler.obtainMessage();
        CallbackRec callbackrec = (CallbackRec)message.obj;
        if(callbackrec == null)
        {
            callbackrec = new CallbackRec();
            message.obj = callbackrec;
        }
        callbackrec.thumb = imageview;
        callbackrec.bitmap = bitmap;
        callbackrec.progress = progressbar;
        uiHandler.sendMessage(message);
    }

    public void setUIHandler(Handler handler)
    {
        android.os.Looper looper = handler.getLooper();
        android.os.Handler.Callback callback = handlerCallback;
        uiHandler = new Handler(looper, callback);
    }

    private static final String LOG_TAG = "SightingViewAdapter";
    private final LayoutInflater _inflater;
    private final int _itemParentId;
    private final List _objects;
    private final ConcurrentHashMap activeRequests;
    private AdapterView adapterView;
    final android.os.Handler.Callback handlerCallback;
    private Handler uiHandler;
    
    private int count = 0;
}
