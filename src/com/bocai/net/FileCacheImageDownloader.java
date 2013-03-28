package com.bocai.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import com.bocai.ImageDownloader;
import com.bocai.R;
import com.bocai.util.Macros;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class FileCacheImageDownloader extends ImageDownloader
{
    public static interface StreamFilter
    {

        public abstract InputStream filter(InputStream inputstream, long l);

        public abstract long getFilteredStreamLength();
    }


    public FileCacheImageDownloader()
    {
        filterMap = null;
    }

    public void download(String paramString, ImageView paramImageView, StreamFilter paramStreamFilter)
    {
       Log.i(LOG_TAG, "download method1");
       if (paramStreamFilter != null)
      {
        if (this.filterMap == null)
        {
        	this.filterMap = new ConcurrentHashMap<String, StreamFilter>();
        }
        this.filterMap.put(paramString, paramStreamFilter);
      }
      super.download(paramString, paramImageView);
    }

    public void download(String paramString, ImageView paramImageView, StreamFilter paramStreamFilter, ImageDownloader.OnFinishListener paramOnFinishListener)
    {
    	Log.i(LOG_TAG, "download method2");
    	if (paramStreamFilter != null)
      {
        if (this.filterMap == null)
        {
        	this.filterMap = new ConcurrentHashMap<String, StreamFilter>();
        }
        this.filterMap.put(paramString, paramStreamFilter);
      }
      super.download(paramString, paramImageView,paramOnFinishListener);
    }

    public Bitmap downloadBitmap(String url)
    {
    	Log.i(LOG_TAG, "downloadBitmap method===" + url);
    	HttpGet httpGet = new HttpGet(url);
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        HttpResponse httpResponse = null;
        try {
			httpResponse = defaultHttpClient.execute(httpGet);
		} catch (Exception e) {
			Log.w("FileCacheImageDownloader", e.getMessage());
		}
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if(statusCode == '\310') {
        	HttpEntity httpEntity = httpResponse.getEntity();
            if(httpEntity == null) {
            	return null;
            }
            InputStream inputStream = null;
            StreamFilter streamFilter;
            try {
            	inputStream = httpEntity.getContent();
			} catch (Exception e) {
				Log.w("FileCacheImageDownloader", e.getMessage());
			}
            streamFilter = null;
            if(filterMap != null)
            {
                streamFilter = filterMap.get(url);
            }
            if(streamFilter == null) {
                ImageDownloader.FlushedInputStream flushedInputStream = new FlushedInputStream(inputStream);
                int length = (int)httpEntity.getContentLength();
                File file = FileCache.putURL(url, flushedInputStream, length);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if(inputStream == null)
                    return null;
                try {
                	inputStream.close();
                	httpEntity.consumeContent();
				} catch (IOException e) {
					Log.w("FileCacheImageDownloader", e.getMessage());
				}
                return bitmap;
            } else {
            	 File file;
                 ImageDownloader.FlushedInputStream flushedInputStream = new FlushedInputStream(inputStream);
                long length = httpEntity.getContentLength();
                InputStream filteredStream = streamFilter.filter(flushedInputStream, length);
                int streamLength = (int)streamFilter.getFilteredStreamLength();
                file = FileCache.putURL(url, filteredStream, streamLength);
                filterMap.remove(url);
                
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
              
                if(inputStream == null)
                    return null;
                try {
                	inputStream.close();
                	httpEntity.consumeContent();
				} catch (IOException e) {
					Log.w("FileCacheImageDownloader",e.getMessage());
				}
				return bitmap;
            }
            
        } else {
            Bitmap bitmap;
            StringBuilder stringBuilder = (new StringBuilder()).append("Error ");
            stringBuilder.append(statusCode).append(" while retrieving bitmap from ");
            Log.w(LOG_TAG, stringBuilder.toString());
            if(statusCode == '\u0194')
                bitmap = BitmapFactory.decodeResource(Macros.FS_APPLICATION().getResources(), R.drawable.default_404);
            else
                bitmap = null;
            return bitmap;
        }
    }

    public Bitmap getBitmapFromCache(String url)
    {
    	Log.i(LOG_TAG, "getBitmapFromCache method");
    	Bitmap bitmap = super.getBitmapFromCache(url);
    	
    	Log.i(LOG_TAG, "=============================url==" + url );
    	Log.i(LOG_TAG, "=============================FileCache.urlExists=" + FileCache.urlExists(url));
    	
        if(bitmap == null && FileCache.urlExists(url))
        {
            bitmap = BitmapFactory.decodeFile(FileCache.getURL(url).getAbsolutePath());
            addBitmapToCache(url, bitmap);
        }
        return bitmap;
    }

    private static String LOG_TAG = "FCID";
    private ConcurrentHashMap<String, StreamFilter> filterMap;

}
