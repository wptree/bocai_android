package com.bocai.net;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.android.internal.http.multipart.*;
import com.bocai.BocaiApplication;
import com.bocai.R;
import com.bocai.util.Macros;
import com.bocai.util.RestConstants;
import com.bocai.util.ToastFire;

import java.io.*;
import java.net.URI;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;


public class AsyncHTTPRequest extends AsyncTask<Void, Void, Boolean> {
    private static class CurlLogger
        implements HttpRequestInterceptor
    {

        public void process(HttpRequest httpRequest, HttpContext httpContext)
            throws HttpException, IOException
        {
            if(httpContext.getAttribute("debug_mode") == null)
            {
                return;
            } else
            {
                String s = AsyncHTTPRequest.toCurl((HttpUriRequest)httpRequest, true);
                Log.d("AsyncHTTPRequest", s);
                return;
            }
        }

        private CurlLogger()
        {
        }

    }
 
    public AsyncHTTPRequest(String url)
    {
        responseHandler = null;
        postParams = null;
        files = null;
        requestCookies = null;
        responseCookies = null;
        useCookiePersistence = false;
        userData = null;
        debug = false;
        this.url = url;
        requestMethod = 1;     
    }

    private boolean doGet()
    {
    	Log.i(LOG_TAG, "execute doGet===" + url);
    	HttpResponse httpResponse = null;
    	List<Cookie> cookieList = null;
        HttpEntity httpEntity;
        HttpGet httpGet = new HttpGet(url);   
        httpGet.addHeader("Content-Type", "application/json");
        httpGet.addHeader("Encoding", "UTF-8");

//        HttpParams httpparams = httpGet.getParams();    
//        httpparams.setIntParameter("http.connection.timeout", timeout);
        
        //TODO: change this for encoding issue
        
        HttpParams httpparams = new BasicHttpParams();
        HttpProtocolParams.setVersion(httpparams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpparams, "UTF-8");
        HttpProtocolParams.setHttpElementCharset(httpparams, "UTF-8");
        httpGet.setParams(httpparams);
        
        BasicHttpContext basicHttpContext = new BasicHttpContext();
        CookieSpecRegistry cookiespecregistry = client.getCookieSpecs();
        basicHttpContext.setAttribute("http.cookiespec-registry", cookiespecregistry);
        if(debug)
        {         
            basicHttpContext.setAttribute("debug_mode", 1);
        }
        
        
        httpEntity = null;
        
        if (requestCookies != null) {
            CookieStore cookiestore;
            cookiestore = client.getCookieStore();
            Cookie cookie;
            for(Iterator<Cookie> iterator = requestCookies.iterator(); iterator.hasNext(); cookiestore.addCookie(cookie))
                cookie = iterator.next();
            
            basicHttpContext.setAttribute("http.cookie-store", cookiestore);
        }
          
	        try {
	        	if(httpGet.getURI().isAbsolute()){
	        		httpResponse = client.execute(httpGet, basicHttpContext);
	        	}else{
	        		httpResponse = client.execute(host,httpGet, basicHttpContext);
	        	}
			} catch (Exception e) {
				Log.e("AsyncHTTPRequest", e.getLocalizedMessage(), e);
			    return false;
			}
		
		InputStream inputStream = null;
		long length = 0L;
		
		if (httpResponse.getStatusLine().getStatusCode() == '\310') {
			httpEntity = httpResponse.getEntity();
			
			cookieList = client.getCookieStore().getCookies();
			try {
				inputStream= httpEntity.getContent();
			} catch (Exception e) {
				Log.e("AsyncHTTPRequest", e.getLocalizedMessage(), e);
			    e.printStackTrace();
			}
			length = httpEntity.getContentLength();

			if (!cookieList.isEmpty()) {
				responseCookies = cookieList;
			}else{
				responseCookies = null;
			}

		try {
			responseHandler.handleResponse(this,inputStream,length);
		} catch (Exception e) {
			Log.w("AsyncHTTPRequest", "Exception type-" + e.getClass().getName(),e);
		}

	       	  if(httpEntity != null){
	              try
	              {
	                  httpEntity.consumeContent();
	              }catch(IOException e) { 
	            	  Log.w("AsyncHTTPRequest", e.getMessage());
	              }
	          }
	            httpGet.abort();
               return true; 
		}else{
			Log.e("AsyncHTTPRequest", httpResponse.getStatusLine().getReasonPhrase());
			responseCookies = null;
			httpEntity = httpResponse.getEntity();
			try {
				inputStream = httpEntity.getContent();
			} catch (Exception e) {
				Log.w("AsyncHTTPRequest", e.getMessage());
			}
			
			length = httpEntity.getContentLength();
			
			 try{
			   responseHandler.handleError(httpResponse.getStatusLine().getReasonPhrase(),
					   						inputStream, length);
			} catch (Exception e) {
				Log.w("AsyncHTTPRequest", "Exception type-" + e.getClass().getName(),e);
			}
			
       		if(httpEntity != null){
              try
              {
                 httpEntity.consumeContent();
              }catch(IOException e) { 
            	  e.printStackTrace(); 
              }
       		}
       		
       	 httpGet.abort();
       	return false;
	  }
    }

    @SuppressWarnings("unchecked")
	private boolean doPost()
    {
    	Log.i(LOG_TAG, "execute doPost method");
    	HttpResponse httpResponse = null;
        HttpPost httpPost = null;
        HttpEntity httpEntity = null;
        httpPost = new HttpPost(url);
        httpPost.addHeader("Encoding", "UTF-8");
        HttpParams httpParams = httpPost.getParams();
        httpParams.setIntParameter("http.connection.timeout", timeout);
        BasicHttpContext basicHttpContext = new BasicHttpContext();
        basicHttpContext.setAttribute("http.cookiespec-registry", client.getCookieSpecs());
        
       if(files == null || files.size() == 0){
    	   httpPost.addHeader("Content-Type", "application/json");
    	   if(postParams != null && postParams.length() !=0){
    		   try {
				httpEntity = new StringEntity(postParams.toString(),HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				Log.w("AsyncHttpRequest", e.getMessage());
			}
    	   }
    	   if(httpEntity != null){
    		   httpPost.setEntity(httpEntity);
    	   }
    	   if(debug) {
               basicHttpContext.setAttribute("debug_mode", 1);
           }
    	   if(requestCookies != null) {
           	CookieStore cookiestore = client.getCookieStore();
           	Cookie cookie;
               for(Iterator<Cookie> iterator = requestCookies.iterator(); iterator.hasNext(); cookiestore.addCookie(cookie))
                   cookie = iterator.next();
               basicHttpContext.setAttribute("http.cookie-store", cookiestore);
           }
    	   
    	   try {
    		   if(httpPost.getURI().isAbsolute()){
    			   httpResponse = client.execute(httpPost, basicHttpContext);
    		   }else{
    			   httpResponse = client.execute(host,httpPost, basicHttpContext);
    		   }
    	   } catch (Exception e) {
    		   Log.e("AsyncHTTPRequest", e.getMessage(),e);
    		   try {
				responseHandler.handleError(e.getLocalizedMessage(), null, 0L);
			} catch (IOException e1) {
				//the exception was ignored.
			}
    		   return false; 
    	   }
    	   
    	   InputStream inputStream = null;
    	   long length = 0L;
    	   
    	   if(httpResponse.getStatusLine().getStatusCode() == '\310') {
    		   httpEntity = httpResponse.getEntity();
    		   List<Cookie> list = client.getCookieStore().getCookies();               
               if(!list.isEmpty()) {                	
               	responseCookies = list;
               } else {
               	responseCookies = null;
               }
            
               try {
				inputStream = httpEntity.getContent();
			} catch (Exception e) {
				 Log.e("AsyncHTTPRequest", e.getMessage(),e);
			}
              length = httpEntity.getContentLength();
            
           try {
			responseHandler.handleResponse(this,inputStream,length);
		} catch (Exception e) {
			 Log.w("AsyncHTTPRequest", e.getMessage()); 
		}
       	
	       	  if(httpEntity != null)
	              try
	              {
	                  httpEntity.consumeContent();
	              }catch(IOException e) { 
	            	  Log.w("AsyncHTTPRequest", e.getMessage()); 
	            	  }
	            httpPost.abort();
               return true;
    	   }else{
    		   
    		   Log.e("AsyncHTTPRequest", httpResponse.getStatusLine().getReasonPhrase());
    		   
    		   httpEntity = httpResponse.getEntity();
    		   try {
				inputStream = httpEntity.getContent();
			} catch (Exception e) {
				 Log.w("AsyncHTTPRequest", e.getMessage()); 
			}
    		 length = httpEntity.getContentLength();  
    		   try{
            	   responseHandler.handleError(httpResponse.getStatusLine().getReasonPhrase(),inputStream,length);
       			} catch (Exception e1) {
       				Log.w("AsyncHTTPRequest", e1.getMessage());
       			} 
       			
       			if(httpEntity != null)
	              try
	              {
	                  httpEntity.consumeContent();
	              }catch(IOException e) { 
	            	  Log.w("AsyncHTTPRequest", e.getMessage());
	            	  e.printStackTrace(); 
	            	}
	            httpPost.abort();
               return false;
    	   }
       }else{
    	   //contain file
    	   Log.w("doPost", "upload files");	      	   
    	   List<Part> parts = new ArrayList<Part>();
    	   Iterator<String> keys = postParams.keys();
    	   
    	   Log.w("doPost", "Log post params");
    	   
    	   while(keys.hasNext()){
    		   String key = keys.next();
    		   StringPart sp = new StringPart(key,postParams.opt(key).toString());
    		   sp.setCharSet("UTF-8");
    		   parts.add(sp);
    	   }
    	   parts.addAll(files);
    	   MultipartEntity entity = new MultipartEntity(parts.toArray(new Part[0]),httpPost.getParams());
    	   entity.setContentEncoding("UTF-8");
    	   httpPost.setEntity(entity);
    	   httpPost.addHeader(entity.getContentType());
    	   try {
   			httpResponse = client.execute(httpPost, basicHttpContext);
       	   } catch (Exception e) {
       		   Log.w("AsyncHTTPRequest", e.getMessage());
       	   }
       	   if(httpResponse.getStatusLine().getStatusCode() == '\310') {
       		   httpEntity = httpResponse.getEntity();
       		   List<Cookie> list = client.getCookieStore().getCookies();               
                  if(!list.isEmpty()) {                	
                  	responseCookies = list;
                  } else {
                  	responseCookies = null;
                  }   
                  
              try {
				responseHandler.handleResponse(this,httpEntity.getContent(),httpEntity.getContentLength());
			} catch (Exception e) {
				 e.printStackTrace();
			}
          	
   	       	  if(httpEntity != null)
   	              try
   	              {
   	                  httpEntity.consumeContent();
   	              }catch(IOException e) { e.printStackTrace(); }
   	            httpPost.abort();
                  return true;
       	   }else{
       		   String errorMessage = httpResponse.getStatusLine().getReasonPhrase();
       		   Log.e("AsyncHTTPRequest", errorMessage);
       		   httpEntity = httpResponse.getEntity();
       		   try{
               	   responseHandler.handleError(errorMessage,httpEntity.getContent(),httpEntity.getContentLength());
          			} catch (Exception e) {
          				 Log.e("AsyncHTTPRequest", e.getMessage());
          			} 
          			
          			if(httpEntity != null)
   	              try
   	              {
   	                  httpEntity.consumeContent();
   	              }catch(IOException e) {
   	            	Log.w("AsyncHTTPRequest", e.getMessage()); 
   	            	  }
   	            httpPost.abort();
                  return false;
       	   }
         }
    }

	public static void initialize(String hostName) {
		if (hostName != null) {
			host = new HttpHost(hostName, port, protocolScheme);
		}
		if (userAgent != null) {
			return;
		} else {
			userAgent = makeUserAgent();
			HttpParams httpparams = client.getParams();

			httpparams.setParameter("http.useragent", userAgent);
			StringBuilder stringbuilder = (new StringBuilder())
					.append("User-Agent: ");
			String s3 = userAgent;
			String s4 = stringbuilder.append(s3).toString();
			Log.d("AsyncHTTPRequest", s4);
			return;
		}
	}

	public static String makeUserAgent() {
		StringBuilder stringbuilder = new StringBuilder();

		try {
			BocaiApplication bocaiApplication = BocaiApplication.instance;
			PackageManager packagemanager = bocaiApplication
					.getPackageManager();

			PackageInfo packageinfo = packagemanager.getPackageInfo(
					bocaiApplication.getPackageName(), 0);

			String s = bocaiApplication
					.getString(packageinfo.applicationInfo.labelRes);
			stringbuilder.append(s).append('/')

			.append(packageinfo.versionName).append(" (")

			.append(packageinfo.versionCode).append(')');
		} catch (Exception e) {
			Log.w("AsyncHTTPRequest", e.getMessage());
		}
		stringbuilder.append("; Android ")

		.append(android.os.Build.VERSION.RELEASE).append("; ")

		.append(Build.BRAND).append(' ')

		.append(Build.DEVICE);
		String s6 = BocaiApplication.instance.getString(R.string.app_name);
		if (s6 != null)
			stringbuilder.append("; ").append(s6);
		return stringbuilder.toString();
	}

    public static void shutdown()
    {
        client.getConnectionManager().shutdown();
    }

	private static String toCurl(HttpUriRequest httpurirequest, boolean flag)
			throws IOException {
		StringBuilder stringbuilder = new StringBuilder();

		stringbuilder.append("curl ");
		Header aheader[] = httpurirequest.getAllHeaders();

		int j = 0;
		while (j < aheader.length) {
			Header header = aheader[j];
			if (flag || !header.getName().equals("Authorization")
					&& !header.getName().equals("Cookie")) {
				stringbuilder.append("--header \"");
				stringbuilder.append(header.toString().trim());
				stringbuilder.append("\" ");
			}
			j++;
		}
		URI uri = httpurirequest.getURI();

		stringbuilder.append("\"");
		stringbuilder.append(uri);
		stringbuilder.append("\"");
		if (httpurirequest instanceof HttpEntityEnclosingRequest) {
			HttpEntity httpEntity = ((HttpEntityEnclosingRequest) httpurirequest).getEntity();

			if (httpurirequest != null && httpEntity.isRepeatable())
				if (httpEntity.getContentLength() < 1024L) {
					ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
					httpEntity.writeTo(bytearrayoutputstream);
					stringbuilder.append(" --data-ascii \"")
							.append(bytearrayoutputstream.toString())
							.append("\"");
				} else {
					stringbuilder.append(" [TOO MUCH DATA TO INCLUDE]");
				}
		}
		return stringbuilder.toString();
	}

    public AsyncHTTPRequest addFile(String name, File file, String fileName, String contentType)
    {
        try
        {
            FilePartSource filepartsource = new FilePartSource(fileName, file);
            FilePart filepart = new FilePart(name, filepartsource, contentType, "UTF-8");
            if(files == null)
            {
            	files = new LinkedList<FilePart>();
            }
            files.add(filepart);
        }
        catch(FileNotFoundException fileNotFoundException)
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("Error adding file part for ").append(file).append(": ");
            stringbuilder.append(fileNotFoundException.getLocalizedMessage()).toString();
            Log.e(LOG_TAG, stringbuilder.toString(), fileNotFoundException);
            fileNotFoundException.printStackTrace();
        }
        return this;
    }

    public AsyncHTTPRequest addFile(String name, byte abyte0[], String fileName, String contentType)
    {
        ByteArrayPartSource bytearraypartsource = new ByteArrayPartSource(fileName, abyte0);
        FilePart filepart = new FilePart(name, bytearraypartsource, contentType, "UTF-8");
        if(files == null)
        {
        	files = new LinkedList<FilePart>();
        }
        files.add(filepart);
        return this;
    }

    public AsyncHTTPRequest addPostParam(String key, String value)
    {
        if(key != null)
        {
            if(postParams == null)
            {
                postParams = new JSONObject();
            }
            try {
				postParams.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.i(LOG_TAG, "addPostParam:" + key + "=" + value );
			Log.i(LOG_TAG, "addPostParam:" + postParams.toString() );
            requestMethod = 2;
        }
        return this;
    }

    protected  Boolean doInBackground(Void... avoid)
    {
        Boolean answer;
        if(requestMethod == 1){
        	answer = Boolean.valueOf(doGet());
        }	
        else if(requestMethod == 2){
        	answer = Boolean.valueOf(doPost());
        }
        else
        	answer = Boolean.valueOf(false);
        return answer;
    }

    public void execute()
    {
        Void[] arrayOfVoid = (Void[])null;
        execute(arrayOfVoid);
    }
    
    public AsyncHTTPRequest setDebug(boolean flag)
    {
        debug = flag;
        return this;
    }

    public AsyncHTTPRequest setRequestCookies(List<Cookie> list)
    {
        if(list != null && list.size() != 0)
            requestCookies = list;
        return this;
    }

    public AsyncHTTPRequest setTimeout(int timeout)
    {
        this.timeout = timeout;
        return this;
    }

    public AsyncHTTPRequest setUseCookiePersistence(boolean flag)
    {
        useCookiePersistence = flag;
        return this;
    }
    
    public static String toString(InputStream inputstream, long length)throws IOException 
    {
    	InputStreamReader inputstreamreader;
		CharArrayBuffer chararraybuffer;
		char c;
		if (length <= 0L)
			length = 4096L;
		inputstreamreader = new InputStreamReader(inputstream, "UTF-8");
		int i = (int) length;
		chararraybuffer = new CharArrayBuffer(i);
		c = '\u0400';
		char ac[] = new char[c];
		do {
			int j = inputstreamreader.read(ac);
			if (j == -1)
				break;
			chararraybuffer.append(ac, 0, j);
		} while (true);
		inputstreamreader.close();
		return chararraybuffer.toString();
    }
    
    

    static final int CONNECTION_TIMEOUT = 60000;
    public static final String DEBUG_MODE = "debug_mode";
    public static final int GET_METHOD = 1;
    private static final String LOG_TAG = "AsyncHTTPRequest";
    public static final int POST_METHOD = 2;
    static final DefaultHttpClient client;
    static HttpHost host;
    static int port = 80;
    static String protocolScheme = "http";
	private int timeout = 60000*2;
    static String userAgent = null;
    public boolean debug;
    public List<FilePart> files;
    public JSONObject postParams;
    public List<Cookie> requestCookies;
    public int requestMethod;
    public List<Cookie> responseCookies;
    public AsyncHTTPResponseHandler responseHandler;
    public String url;
    public boolean useCookiePersistence;
    public Object userData;

    static 
    {
        BasicHttpParams basicHttpParams = new BasicHttpParams();
        basicHttpParams.setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
        basicHttpParams.setParameter("http.protocol.content-charset", "UTF-8");
        basicHttpParams.setParameter("http.connection.timeout", 60000*2);
        basicHttpParams.setParameter("http.socket.timeout", 60000*2);
        basicHttpParams.setParameter("http.socket.buffer-size", 8192);
        basicHttpParams.setParameter("http.connection.stalecheck", false);
        SchemeRegistry schemeregistry = new SchemeRegistry();
        PlainSocketFactory plainsocketfactory = PlainSocketFactory.getSocketFactory();
        Scheme scheme = new Scheme("http", plainsocketfactory, 80);
        schemeregistry.register(scheme);
        SSLSocketFactory sslsocketfactory = SSLSocketFactory.getSocketFactory();
        Scheme scheme2 = new Scheme("https", sslsocketfactory, 443);
        schemeregistry.register(scheme2);
        ThreadSafeClientConnManager threadsafeclientconnmanager = new ThreadSafeClientConnManager(basicHttpParams, schemeregistry);
        client = new DefaultHttpClient(threadsafeclientconnmanager, basicHttpParams);
        int i = client.getRequestInterceptorCount();
        CurlLogger curllogger = new CurlLogger();
        client.addRequestInterceptor(curllogger, i);
    }

}
