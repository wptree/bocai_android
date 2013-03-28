
package com.bocai.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.*;

import com.bocai.ImageDownloader;
import com.bocai.ImageDownloader.FlushedInputStream;
import com.bocai.util.Macros;

public class ImageDownloadRequest extends AsyncHTTPRequest
    implements AsyncHTTPResponseHandler
{
    public ImageDownloadRequest(String url) {
		super(url);
        downloadListener = null;
        responseHandler = this;
        imageDownloader = Macros.FS_APPLICATION().imageDownloader;
	}
	public static interface OnDownloadCompleteListener
    {

        public abstract void onDownloadComplete(ImageDownloadRequest imageDownloadRequest, Bitmap bitmap);

        public abstract void onDownloadError(ImageDownloadRequest imageDownloadRequest, String message);
    }

    public void execute()
    {
    	super.execute();
    	
    }
    
    @Override
	public void handleError(String errorMsg, InputStream stream, long length)
			throws IOException {
    	  if (this.downloadListener == null){
    	      return;
    	  }
    	 this.downloadListener.onDownloadError(this, errorMsg);
	}
    
    @Override
    public void handleResponse(AsyncHTTPRequest asyncHttpRequest, InputStream stream, long length)
        throws IOException{
        ImageDownloader.FlushedInputStream flushedInputStream = new FlushedInputStream(stream);
        int i = (int)length;
        File file = FileCache.putURL(url, flushedInputStream, i);
        if(downloadListener == null)
            return;
        if(file == null)
            return;
        try
        {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            downloadListener.onDownloadComplete(this, bitmap);
            return;
        }
        catch(OutOfMemoryError outOfMemoryError)
        {
            downloadListener.onDownloadError(this, outOfMemoryError.getLocalizedMessage());
            return;
        }
    }

//    private static final String LOG_TAG = "IDR";
    public OnDownloadCompleteListener downloadListener;
    
    private ImageDownloader imageDownloader ;
	

}
