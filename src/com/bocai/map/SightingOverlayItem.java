
package com.bocai.map;

import android.graphics.Bitmap;
import android.graphics.drawable.*;
import android.widget.ImageView;
import com.bocai.BocaiApplication;
import com.bocai.ImageDownloader;
import com.bocai.R;
import com.bocai.model.*;
import com.bocai.util.Macros;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class SightingOverlayItem extends OverlayItem
{
    public static interface ImageListener
    {

        public abstract void onImageDownloaded(SightingOverlayItem sightingOverlayItem, Bitmap bitmap);
    }


    public SightingOverlayItem(Sighting sighting)
    {

        this(new GeoPoint((int)(sighting.latitude * 1000000D), (int)(sighting.longitude * 1000000D)),sighting.item.name,sighting.place.name);
        this.sighting = sighting;
        getBitmap();
        
    }

    public SightingOverlayItem(GeoPoint geoPoint, String title, String snippet)
    {
        super(geoPoint, title, snippet);
    }

    public Bitmap getBitmap()
    {
        Bitmap bitmap = null;
        if(sighting.thumb90 != null)
        {
            bitmap = sighting.thumb90;
        } else
        {
            String s = sighting.thumb90URL;
            if(s == null)
            {
                bitmap = null;
            } else
            {
                BocaiApplication bocaiApplication = Macros.FS_APPLICATION();
                ImageDownloader imageDownloader = bocaiApplication.imageDownloader;
                if(imageDownloader != null && s.startsWith("http"))
                {
                    ImageView imageView = new ImageView(bocaiApplication);
                    ImageDownloader.OnFinishListener onFinishListener = new ImageDownloader.OnFinishListener() {

                        public void onFinish(String url, ImageView imageview, Bitmap bitmap)
                        {
                            setBitmap(bitmap);
                        }

                    };
                   imageDownloader.download(s, imageView,onFinishListener);
                }
                bitmap = null;
            }
        }
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap)
    {
        sighting.thumb90 = bitmap;
        if(marker instanceof LayerDrawable)
        {
            LayerDrawable layerdrawable = (LayerDrawable)marker;
            BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
            bitmapDrawable.setAntiAlias(true);
            bitmapDrawable.setFilterBitmap(true);
            if(thumbnailSize > 0)
            {
                int i = thumbnailSize / 2;
                int j = -i;
                int k = -i;
                bitmapDrawable.setBounds(j, k, i, i);
                layerdrawable.setDrawableByLayerId(R.id.thumb, bitmapDrawable);
            }
        }
        if(listener == null)
        {
            return;
        } else
        {
            listener.onImageDownloaded(this, bitmap);
            return;
        }
    }

    public void setMarker(Drawable drawable)
    {
        super.setMarker(drawable);
        marker = drawable;
    }

    public void setThumbnailSize(int size)
    {
        thumbnailSize = size;
    }

    public ImageListener listener;
    public Drawable marker;
    public Sighting sighting;
    public int thumbnailSize;
}
