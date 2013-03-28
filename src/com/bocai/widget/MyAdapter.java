package com.bocai.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bocai.ImageDownloader;
import com.bocai.R;
import com.bocai.model.Review;
import com.bocai.model.Sighting;
import com.bocai.util.DateUtilities;
import com.bocai.util.Macros;

public class MyAdapter extends ArrayAdapter
{

	private Context context;
	
    public View getView(int i, View view, ViewGroup viewgroup)
    {
        TextView textview;
        TextView textview1;
        TextView textview2;
        ImageView imageview;
        int k;
        if(view == null)
        {
            view = _inflater.inflate(_itemParentId, null);
            textview = (TextView)view.findViewById(R.id.title);
            textview1 = (TextView)view.findViewById(R.id.subtitle);
            textview2 = (TextView)view.findViewById(R.id.content);
            imageview = (ImageView)view.findViewById(R.id.img_photo);
            Object aobj[] = new Object[4];
            aobj[0] = textview;
            aobj[1] = textview1;
            aobj[2] = textview2;
            aobj[3] = imageview;
            view.setTag(((Object) (aobj)));
            android.widget.LinearLayout.LayoutParams layoutparams = new android.widget.LinearLayout.LayoutParams(-1, -1, 0F);
            layoutparams.setMargins(3, 2, 0, 4);
            view.setLayoutParams(layoutparams);
        } else
        {
            Object aobj1[] = (Object[])(Object[])view.getTag();
            textview = (TextView)aobj1[0];
            textview1 = (TextView)aobj1[0];
            textview2 = (TextView)aobj1[0];
            imageview = (ImageView)aobj1[0];
        }
        view.setClickable(true);
        k = _objects.size();
        
        Log.i("MyAdapter--MyAdapter--getView i,k", ""+ i + "," + k);
        
        if (i < k) {
        	Sighting sighting = (Sighting)_objects.get(i);
        	if(sighting.thumb90 == null) {
                if(sighting.thumb90URL != null)
                {
                
                	imageDownloader.download(sighting.thumb90URL, imageview);
                } else
                {
                    String s4 = (new StringBuilder()).append("Review has no thumb 90!: ").append(sighting).toString();
                    Log.i("ReviewActivity", s4);
                }

        	} else {
                Bitmap bitmap = sighting.thumb90;
                imageview.setImageBitmap(bitmap);
        	}
            
            if(sighting.item != null && sighting.item.name != null){
            	textview.setText(sighting.item.name);
            }
        	
            if(sighting.currentReviewAt != null)
            {
            	String s1 = DateUtilities.getRelativeDate(sighting.currentReviewAt);
                textview1.setText(s1);
            }
            
            String nomAndWant = context.getString(R.string.nom_and_want);
            String s2 = String.format(nomAndWant, sighting.ribbonsCount,sighting.wantsCount);
            textview2.setText(s2);
            
            return view;
        } else {
        	return view;
        }

    }

    private LayoutInflater _inflater;
    private int _itemParentId;
    private List _objects;
    ImageDownloader imageDownloader;

    public MyAdapter(Context context, int i, List list)
    {
        super(context, i, list);
        this.context = context;
        _objects = list;
        _itemParentId = i;
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        _inflater = layoutinflater;
        imageDownloader = Macros.FS_APPLICATION().imageDownloader;
    }
}
