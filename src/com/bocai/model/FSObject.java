package com.bocai.model;

import android.net.Uri;
import android.util.Log;
import com.bocai.net.AsyncHTTPRequest;
import com.bocai.net.AsyncHTTPResponseHandler;
import com.bocai.util.RestConstants;

import java.io.*;
import java.util.*;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;


public class FSObject
    implements AsyncHTTPResponseHandler
{
    public static interface ResponseDataHandler
    {

        public abstract void responseData(JSONObject jsonObject, AsyncHTTPRequest asyncHttpRequest)
            throws JSONException;
    }


    public FSObject()
    {
        wasCancelled = false;
        wasCompleted = false;
    }

    public FSObject(AsyncHTTPRequest asyncHttpRequest)
    {
        wasCancelled = false;
        wasCompleted = false;
        request = asyncHttpRequest;
        performRequest(asyncHttpRequest);
    }

    static String getQueryString(HashMap<String, Object> hashMap)
    {
        String s;
        if(hashMap == null)
        {
            s = "";
        } else
        {
            StringBuilder stringBuilder = new StringBuilder();
            for(Iterator<Map.Entry<String, Object>> iterator = hashMap.entrySet().iterator(); iterator.hasNext();)
            {
                java.util.Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                stringBuilder.append(key).append('=');
                String value = Uri.encode(entry.getValue().toString(), null);
                stringBuilder.append(value).append('&');
            }

            int i = stringBuilder.length();
            if(i > 0)
            {
                int j = i - 1;
                if(stringBuilder.charAt(j) == '&')
                {
                    int k = i - 1;
                    stringBuilder.setLength(k);
                }
            }
            s = stringBuilder.toString();
        }
        return s;
    }

    public static AsyncHTTPRequest requestWithPath(String path, HashMap<String, Object> params)
    {
        Object aobj[] = new Object[2];
        aobj[0] = path;
        String queryStr = getQueryString(params);
        aobj[1] = queryStr;
        String url = String.format(RestConstants.BC_WS_TEMPLETE_URL, aobj);
        Log.d("FSObject", url);
        return new AsyncHTTPRequest(url);
    }

    public void cancelRequests()
    {
    	//blank method
    }
    
    @Override
	public void handleError(String errorMsg, InputStream stream, long length)
			throws IOException {
    	if (wasCancelled)
			return;
		wasCompleted = true;
		Log.d(LOG_TAG, "handleError=" + errorMsg);
		
		try {
			responseData(null, request);
		} catch (JSONException e) {
			Log.w(LOG_TAG, e.getMessage(),e);
		}
		
	}

	@Override
	public void handleResponse(AsyncHTTPRequest asyncHttpRequest,
			InputStream stream, long length) throws IOException {
		if (wasCancelled){
			return;
		}
		wasCompleted = true;
		if (startTime != null) {
			long l1 = (new Date()).getTime();
			long l2 = startTime.getTime();
			float f = l1 - l2;
	        f = f / 1000.0f;
	        Log.i("FSObject", "httpRequest execute time(second):" + f);
		}
		
		JSONObject jsonObject = null;
		try {
			String s = AsyncHTTPRequest.toString(stream, length);
			jsonObject = new JSONObject(s);
		} catch (JSONException e) {
			Log.e("FSObject", e.getLocalizedMessage(), e);
		}
		
		try {
			Object obj = asyncHttpRequest.userData;
        	if(obj != null && obj instanceof ResponseDataHandler){
        		ResponseDataHandler handler = (ResponseDataHandler)obj;
        		handler.responseData(jsonObject, asyncHttpRequest);
        		asyncHttpRequest.userData = null;
        	}
			responseData(jsonObject, asyncHttpRequest);
		} catch (JSONException e) {
			Log.e("FSObject", e.getLocalizedMessage(),e);
		}
	}
   
    void performRequest(AsyncHTTPRequest asyncHttpRequest)
    {
        performRequest(asyncHttpRequest, null);
    }

    void performRequest(AsyncHTTPRequest asyncHttpRequest, List<Cookie> list)
    {
        performRequest(asyncHttpRequest, list, null);
    }

    void performRequest(AsyncHTTPRequest asyncHttpRequest, List<Cookie> list, Object obj)
    {
    	startTime= new Date();
        request = asyncHttpRequest;
        request.responseHandler = this;
        if(obj != null)
            request.userData = obj;
        if(list != null)
        {
            request.setUseCookiePersistence(false);
            request.setRequestCookies(list);
        }
        request.execute();
    }

    String readResponse(InputStream inputStream)
        throws IOException
    {
        InputStreamReader inputstreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputstreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        do
        {
            String s = bufferedReader.readLine();
     
            if(s != null)
                stringBuilder.append(s);
            else
                return stringBuilder.toString();
        } while(true);
    }

    void resetCancelled()
    {
    	//blank method
    }

    protected void responseData(JSONObject jsonobject, AsyncHTTPRequest asynchttprequest)
        throws JSONException
    {
    	//blank method
    }

    public String url()
    {
        String snswer;
        if(request != null)
        	snswer = request.url;
        else
        	snswer = null;
        return snswer;
    }

    public boolean wasCancelled()
    {
        return false;
    }

    public boolean wasCompleted()
    {
        return false;
    }

    static final String LOG_TAG = "FSObject";
    public FSObjectDelegate delegate;
    AsyncHTTPRequest request;
    Object responseDataSel;
    Date startTime;
    public boolean wasCancelled = false;
    public boolean wasCompleted = false;
	
}
