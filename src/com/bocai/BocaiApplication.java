package com.bocai;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.bocai.model.Filter;
import com.bocai.model.GoogleAddress;
import com.bocai.model.User;
import com.bocai.net.AsyncHTTPRequest;
import com.bocai.net.AsyncHTTPResponseHandler;
import com.bocai.net.FileCache;
import com.bocai.net.FileCacheImageDownloader;
import com.bocai.util.FlurryEvents;
import com.bocai.util.LocationUtilities;
import com.bocai.util.Macros;
import com.bocai.util.RestConstants;

public class BocaiApplication extends Application implements AsyncHTTPResponseHandler
{
    public static interface AppUpdateListener
    {
        public abstract void updateAvailable(int i, String s, boolean flag);
    }

    public static interface AddressChangeListener
    {
        public abstract void addressChanged(String s);
    }

    public static interface LocationChangeListener
    {
        public abstract void locationChanged(Location location);
    }

    public static interface StateChangeListener
    {
        public abstract void onStateChange(int i);
    }

	public static boolean isNetworkAvailable(Activity mActivity) {
		Context context = mActivity.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isHttpAvailable(final Activity mActivity) {
		if (!isNetworkAvailable(mActivity)) {
/*			AlertDialog.Builder builders = new AlertDialog.Builder(mActivity);
			builders.setTitle(mActivity.getString(R.string.network_na_prompt));
			LayoutInflater _inflater = LayoutInflater.from(mActivity);
			View convertView = _inflater.inflate(R.layout.error, null);
			builders.setView(convertView);
			builders.setPositiveButton(mActivity.getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//mActivity.finish();
						}
					});
			builders.show();*/

			final Toast toast = Toast.makeText(mActivity, R.string.network_na_prompt,
					Toast.LENGTH_LONG);
			
			new CountDownTimer(4000, 1000) {
				public void onTick(long millisUntilFinished) {
					toast.show();
				}

				public void onFinish() {
					toast.show();
				}
			}.start();
			return false;
		}
		return true;
	}

    public BocaiApplication()
    {
        deviceIsHiRes = false;
        flurryEnabled = false;
        firstLaunch = true;
        currentLocation = null;
        lastKnownLocation = null;
        fineListenerInUse = false;
        coarseListenerInUse = false;
        locationOneShotListeners = null;
        Runnable runnable = new Runnable() {

            public void run()
            {
                if(currentLocation == null)
                {
                    Log.i(LOG_TAG, "forceLocationUpdate runnable==currentLocation is null!!");
                	BocaiApplication bocaiApplication = BocaiApplication.this;
                    //bocaiApplication.currentLocation = lastKnownLocation;
                    bocaiApplication.currentLocation = null;

                }
              
                if(currentLocation == null)
                    return;
                if(locationOneShotListeners == null)
                    return;
                if(locationOneShotListeners.size() <= 0)
                    return;
                LocationChangeListener locationChangeListener;
            
                for(Iterator<LocationChangeListener> iterator = locationOneShotListeners.iterator(); iterator.hasNext(); locationChangeListener.locationChanged(currentLocation))
                {
                	locationChangeListener = iterator.next();
                }

                locationOneShotListeners.clear();
            }
        };
        
        forceLocationUpdate = runnable;
        handler = new Handler();
        locationManagerOverride = false;
        currentAddress = null;
        fileCacheTimer = null;
        checkedForAccount = false;
        //tracker = null;
        geocodeResponseHandler = new AsyncHTTPResponseHandler(){

        	@Override
			public void handleResponse(AsyncHTTPRequest asyncHttpRequest,
					InputStream stream, long length) throws IOException {
        		  String s = AsyncHTTPRequest.toString(stream, length);
                  try {
					JSONObject jsonObject = (new JSONObject(s)).getJSONArray("results").getJSONObject(0);
					BocaiApplication.this.currentAddress = new GoogleAddress(jsonObject);
					Log.w("BocaiApplication", "CurrentAddress===" + BocaiApplication.this.currentAddress);
					 if(asyncHttpRequest.userData != null)
	                    {
	                        AddressChangeListener listener = (AddressChangeListener)asyncHttpRequest.userData;
	                        listener.addressChanged(BocaiApplication.this.currentAddress.getFormattedAddress());
	                        asyncHttpRequest.userData = null;
	                    }
				} catch (JSONException e) {
					 	StringBuilder sb = (new StringBuilder()).append("Error parsing geocode response: ");
	                    sb.append(e.getLocalizedMessage());
	                    Log.e(LOG_TAG, sb.toString(), e);
				}
				
			}
        	
			@Override
			public void handleError(String errorMsg, InputStream stream,
					long length) throws IOException {
				StringBuilder sb = new StringBuilder();
				sb.append("geocodeResponseHandler.handleError: ");
				sb.append(errorMsg);			
	            Log.e(LOG_TAG, sb.toString());
	                if(stream == null)
	                {
	                    return;
	                } else{
	                    String s2 = AsyncHTTPRequest.toString(stream, length);	                  
	                    String s3 = (new StringBuilder())
	                    .append("geocodeResponseHandler: Error reverse geocoding location: ")
	                    .append(s2).toString();
	                    Log.e(LOG_TAG, s3);
	                    return;
	                }	
			}
        };
        
		versionInfoResponseHandler = new AsyncHTTPResponseHandler() {

            public void handleError(String s, InputStream inputstream, long l)
                throws IOException
            {
                String s1 = (new StringBuilder()).append("versionInfoResponseHandler.handleError: ").append(s).toString();
                Log.e("BocaiApplication", s1);
                if(inputstream == null)
                {
                    return;
                } else
                {
                    String s2 = AsyncHTTPRequest.toString(inputstream, l);
                    String s3 = (new StringBuilder()).append("versionInfoResponseHandler: Error retrieving app version information: ").append(s2).toString();
                    Log.e("BocaiApplication", s3);
                    return;
                }
            }

            public void handleResponse(AsyncHTTPRequest asynchttprequest, InputStream inputstream, long l)
                throws IOException
            {
                String s = AsyncHTTPRequest.toString(inputstream, l);
                Log.i("BocaiApplication", s);
                JSONObject jsonobject = null;
				try {
					jsonobject = new JSONObject(s);
				} catch (JSONException e) {
					e.printStackTrace();
				}
                PackageManager packagemanager = getPackageManager();
                String s1 = getPackageName();
                int i = 0;
				try {
					i = packagemanager.getPackageInfo(s1, 0).versionCode;
	                Log.i("BocaiApplication", "current installed version " + i);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
                int j = 0;
				try {
					j = jsonobject.getInt("version");
	                Log.i("BocaiApplication", "the latest available version " + j);
				} catch (JSONException e) {
					e.printStackTrace();
				}
                if(j <= i)
                    return;
                updateAvailable = true;
                updateNewVersion = j;
                
                Macros.FS_DEFAULT_SET_LONG("FSAppUpdateTime", System.currentTimeMillis());
                if(asynchttprequest.userData == null)
                {
                    return;
                } else
                {
                    AppUpdateListener appupdatelistener = (AppUpdateListener)asynchttprequest.userData;
                    BocaiApplication bocaiApplication = BocaiApplication.this;
                    String s2 = jsonobject.optString("message", null);
                    bocaiApplication.updateMessage = s2;
                    boolean flag1 = jsonobject.optBoolean("optional", true);
                    BocaiApplication.this.updateOptional = flag1;
                    String s4 = updateMessage;
                    boolean flag3 = updateOptional;
                    flag3 = true;
                    appupdatelistener.updateAvailable(j, s4, flag3);
                    return;
                }
            }
        };
    
    }
    
    private LocationListener createCoarseLocationListener()
    {
        return new LocationListener() {

            public void onLocationChanged(Location location)
            {
            	Log.d(LOG_TAG, "--- CoarseLocationListener ---");
                BocaiApplication.this.onLocationChanged(location);
            }

            public void onProviderDisabled(String s)
            {
            }

            public void onProviderEnabled(String s)
            {
            }

            public void onStatusChanged(String s, int i, Bundle bundle)
            {
            }
        }
;
    }

    private LocationListener createFineLocationListener()
    {
        return new LocationListener() {

            public void onLocationChanged(Location location)
            {
            	Log.d(LOG_TAG, "--- FineLocationListener ---");
                BocaiApplication.this.onLocationChanged(location);
            }

            public void onProviderDisabled(String s)
            {
            }

            public void onProviderEnabled(String s)
            {
            }

            public void onStatusChanged(String s, int i, Bundle bundle)
            {
            }
        }
;
    }

    public void addStateChangeListener(StateChangeListener stateChangeListener)
    {
        if(stateChangeListeners == null)
        {
            stateChangeListeners = new LinkedList<StateChangeListener>();
        }
       stateChangeListeners.add(stateChangeListener);
    }

    void authenticateWithEmail(String email, String password)
    {
    	Log.i("BocaiApplication", "authenticateWithEmail method");
    	if(email == null || password == null){
    		return;
    	}
    	AsyncHTTPRequest request = new AsyncHTTPRequest("/user_login.json");
    	request.responseHandler = this;
    	request.setTimeout(20000);
    	request.requestMethod = 2;
        request.addPostParam("email", email);
        request.addPostParam("password", password);
        request.addPostParam("remeberMe", "true");
        request.execute();
    }

    public boolean cancelLocationUpdate(LocationChangeListener locationChangeListener)
    {
    	if(locationOneShotListeners != null) {
    		return locationOneShotListeners.remove(locationChangeListener);
    	} else {
    		return false;
    	}
    }

    void checkForAccount()
    {
		Log.i("BocaiApplication", "checkForAccount method");
    	String email = Macros.FS_DEFAULT_GET_STRING("email");
		String password = Macros.FS_DEFAULT_GET_STRING("password");
		if (password != null && !password.equals("D3fau1tPa55word")) {
			Macros.FS_DEFAULTS_EDITOR().remove("password");
			Macros.FS_DEFAULTS_EDITOR().commit();
		}
		authenticateWithEmail(email, password);
		checkedForAccount = true;
		
		User user = User.archivedUser();
		
		if (user == null) {
			checkedForAccount = true;
			return;
		} else {
			if (user.email.equals(email)) {

				if (email == null || password == null || user == null) {
					User.deleteArchivedUser();
				}
				checkedForAccount = true;
				return;
			} else {
				authenticateWithEmail(email, password);
				user.email = email;
				User.archiveUser(user);
				checkedForAccount = true;
				return;
			}
		}
    }

    public void checkForAppUpdate(AppUpdateListener appupdatelistener)
    {
    	Log.i("BocaiApplication", "currentTimeMillis---" + System.currentTimeMillis());
    	Log.i("BocaiApplication", "FSAppUpdateTime-----" + Macros.FS_DEFAULT_GET_LONG("FSAppUpdateTime"));
        long l = Macros.FS_DEFAULT_GET_LONG("FSAppUpdateTime") + 1000L*60L*60L*24L;
        if(System.currentTimeMillis() < l)
        {
            updateAvailable = false;
            return;
        }
        if(updateAvailable)
        {
            if(appupdatelistener == null)
            {
                return;
            } else
            {
            	updateOptional = true;
                appupdatelistener.updateAvailable(updateNewVersion, updateMessage, updateOptional);
                return;
            }
        } else
        {
            AsyncHTTPRequest asynchttprequest = new AsyncHTTPRequest("http://www.bocai007.com/app_update.json?client=android");
            asynchttprequest.responseHandler = versionInfoResponseHandler;
            asynchttprequest.userData = appupdatelistener;
            asynchttprequest.execute();
            return;
        }
    }
    
    public boolean checkForAppUpdate2(AppUpdateListener appupdatelistener, Context context)
    {
    	if (hasChecked) {
            updateAvailable = false;
            return updateAvailable;
    	}
    	Log.i("BocaiApplication", "currentTimeMillis---" + System.currentTimeMillis());
    	Log.i("BocaiApplication", "FSAppUpdateTime-----" + Macros.FS_DEFAULT_GET_LONG("FSAppUpdateTime"));
        long l = Macros.FS_DEFAULT_GET_LONG("FSAppUpdateTime") + 1000L*60L*60L*24L;
        //long l = Macros.FS_DEFAULT_GET_LONG("FSAppUpdateTime") + 1000L*60L;
    	if(System.currentTimeMillis() < l)
        {
            updateAvailable = false;
            return updateAvailable;
        }
        Updater updater = new Updater("",context);
        if (!updater.needUpdate()) {
        	return false;
        }
        updater.showNewVersionUpdate();
        return true;
    }

    public void clearAccount(String account)
    {
    	Log.i("BocaiApplication", "clearAccount method");
    	  currentUser = null;
          User.clearCurrentUser();
    }
    
    void displayErrors(JSONObject jsonObject)
    {
        Log.i("BocaiApplication", "displayErrors");
        Log.i("BocaiApplication", "displayErrors=" + jsonObject.toString());
    	StringBuilder stringbuilder = null;
        JSONArray jsonArray = jsonObject.optJSONArray("errors");
        if(jsonArray != null)
        {
            stringbuilder = new StringBuilder();
            int i = jsonArray.length();
            for(int j = 0; j < i; j++)
            {
                Object obj = null;
				try {
					obj = jsonArray.get(j);
				} catch (JSONException e) {
					e.printStackTrace();
				}
                stringbuilder.append(obj).append('\n');
            }
        }
        (new android.app.AlertDialog.Builder(this)).setTitle("Whoops! You changed your email or password and we found the following problems:").setMessage(stringbuilder).setPositiveButton("Log In or Sign Up", null).show();
         showAuthControllerWithSpot(false);
    }

    public void displayLocation()
    {
    }

    void doStartupTasks()
    {
        Log.i(LOG_TAG, "doStartupTasks method");
    	if(checkedForAccount)
        {
            return;
        } else
        {
            checkForAccount();
            return;
        }
    }

    public void flurryLogEvent(String s)
    {
    }

    public void flurryLogEvent(String s, Map<String,String> map)
    {
    }

    public void flurrySetLocation(Location location)
    {
    }

    public String getUpdateURL()
    {
/*        StringBuilder stringbuilder = (new StringBuilder()).append("market://search?q=pname:");
        String s = getPackageName();
        return stringbuilder.append(s).toString();*/
    	return "http://www.bocai007.com/bocai/client/android/bocai_android.apk";
    }

    void initCache()
    {
    	 Log.i("BocaiApplication", "on initCache method");
    	FileCache.getInstance().initialize(this);
    	imageDownloader = new FileCacheImageDownloader();
    	fileCacheTimer = new Timer();
        TimerTask timerTask = new TimerTask() {

            public final void run()
            {
            	  FileCache.checkCache(true);
            }
        };
        
        fileCacheTimer.schedule(timerTask, 10L, 300000L);
    }

    void initDefaults()
    {
    	if(!Macros.FS_DEFAULT_GET_BOOL("first_run"))
        {
            Filter.initDefaultFilters();
            Macros.FS_DEFAULT_SET_BOOL("first_run", true);
            return;
        } else
        {
            Filter.initStartupFilters();
            return;
        }
    }

    void initFlurryAnalytics()
    {
        if(flurryEnabled)
            return;
        else
            return;
    }
    @Override
    public void onCreate()
    {
       Log.i("BocaiApplication", "on Create method");
       	initBaiduMapManager();
    	super.onCreate();
        instance = this;
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        boolean flag;
        StringBuilder stringbuilder;
        String s;
        String s1;
        String s2;
        if(displaymetrics.widthPixels == 480 && displaymetrics.heightPixels >= 800 || displaymetrics.widthPixels >= 800 && displaymetrics.heightPixels == 480)
            flag = true;
        else
            flag = false;
        deviceIsHiRes = flag;
        initCache();
        flurryEnabled = false;
        initFlurryAnalytics();
        initDefaults();
        deviceId = android.provider.Settings.Secure.getString(getContentResolver(), "android_id");
        if(deviceId == null)
            deviceId = "emulatoremulator";
        stringbuilder = (new StringBuilder()).append("onCreate: Device ID ");
        s = deviceId;
        s1 = stringbuilder.append(s).toString();
        Log.d("BocaiApplication", s1);
        AsyncHTTPRequest.initialize(RestConstants.SERVER_HOST);
        checkedForAccount = false;
        //tracker = GoogleAnalyticsTracker.getInstance();
        s2 = getString(R.string.ga_web_property_id);
        //tracker.start(s2, 20, this);
        //tracker.trackPageView("/fsandroidLaunch");
    }

    public void onLocationChanged(Location location)
    {
    	  Log.i(LOG_TAG, "onLocationChanged method=====================");
    	  StringBuilder stringbuilder = (new StringBuilder()).append("onLocationChanged: ");
          String s = LocationUtilities.toShortString(location);
          Log.i(LOG_TAG, stringbuilder.append(s).toString());

          if(!LocationUtilities.isBetterLocation(location, currentLocation)){
              return;
          }
     //     lastKnownLocation = location;
          
          boolean isLocationNotChanged = false;
          boolean flag1;
          boolean flag2;
          StringBuilder stringbuilder1;
          String s3;
          long currentTime;
          StringBuilder stringbuilder2;
          String s5;
          StringBuilder stringbuilder3;
          String s7;
          StringBuilder stringbuilder4;
          String s9;
          Iterator<LocationChangeListener> iterator;
          LocationChangeListener locationchangelistener;
          Location location2;
          if(location != null && currentLocation != null)
          {
              float f = currentLocation.distanceTo(location);
              String s2 = (new StringBuilder()).append("onLocationChanged: delta distance: ").append(f).toString();
              Log.d("BocaiApplication", s2);

              if(f < 25F)
            	  isLocationNotChanged = true;
              else
            	  isLocationNotChanged = false;
          }
          if(isLocationNotChanged)
              Log.d("BocaiApplication", "onLocationChanged: LOCATION NOT CHANGED!");
          FlurryEvents.FLURRY_SET_LOCATION(location);
          if(locationManagerOverride)
              return;
          if(isLocationNotChanged)
              return;
          currentLocation = location;
          stringbuilder1 = (new StringBuilder()).append("onLocationChanged: New location ");
          s3 = LocationUtilities.toShortString(location);
          Log.d(LOG_TAG, stringbuilder1.append(s3).toString());
          currentTime = System.currentTimeMillis();
          if(currentTime - locationListenStart > 5000L)
              flag1 = true;
          else
              flag1 = false;
          if(location != null && location.hasAccuracy() && location.getAccuracy() < 50F)
              flag2 = true;
          else
              flag2 = false;
          if(flag1 || flag2)
              flag1 = true;
          else
              flag1 = false;
          stringbuilder2 = (new StringBuilder()).append("onLocationChanged: good enough? (due to timeIsUp: ").append(flag1).append(", closeEnough: ").append(flag2).append(")! ");
          s5 = LocationUtilities.toShortString(location);
          Log.d("BocaiApplication", stringbuilder2.append(s5).toString());
          if(!flag1)
              return;
          handler.removeCallbacks(forceLocationUpdate);
          stringbuilder3 = (new StringBuilder()).append("onLocationChanged: New location ACCEPTED: ");
          s7 = LocationUtilities.toShortString(location);
          Log.d("BocaiApplication", stringbuilder3.append(s7).toString());
          if(locationOneShotListeners != null && locationOneShotListeners.size() > 0)
          {
              stringbuilder4 = (new StringBuilder()).append("One shot location listeners: ");
              s9 = stringbuilder4.append(locationOneShotListeners).toString();
              Log.d("BocaiApplication", s9);
              for(iterator = locationOneShotListeners.iterator(); iterator.hasNext(); locationchangelistener.locationChanged(location2))
              {
                  locationchangelistener = iterator.next();
                  location2 = currentLocation;
              }

              locationOneShotListeners.clear();
          }
          StringBuilder stringbuilder5 = (new StringBuilder()).append("AFTER: One shot location listeners: ");
          String s10 = stringbuilder5.append(locationOneShotListeners).toString();
          Log.d("BocaiApplication", s10);
    }
    @Override
    public void onTerminate()
    {
    	Log.i("BocaiApplication", "on onTerminate method");
    	fileCacheTimer.cancel();
        fileCacheTimer = null;
        //unregisterForLocationUpdates();
        unregisterForLocationUpdatesUsingBaidu();
        //tracker.stop();
        //tracker = null;
        if(stateChangeListeners != null)
            stateChangeListeners.clear();
        instance = null;
        
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
        Log.d("BocaiApplication", "onTerminate");
    }

    public void pause()
    {
    	 Log.i("BocaiApplication", "on pause method");
    	StringBuilder stringbuilder = (new StringBuilder()).append("+++ pause() at ");
        Date date = new Date();
        String s = stringbuilder.append(date).append(" +++").toString();
        Log.d("BocaiApplication", s);
        unregisterForLocationUpdates();
        unregisterForLocationUpdatesUsingBaidu();
        currentLocation = null;
        lastKnownLocation = null;
        if(stateChangeListeners == null)
            return;
        Iterator<StateChangeListener> iterator = stateChangeListeners.iterator();
        do
        {
            if(!iterator.hasNext())
                return;
            iterator.next().onStateChange(2);
        } while(true);
    }

    void pingBackgroundThreadStart()
    {
    	//TODO:
    }

    public boolean registerForLocationUpdates()
    {
        boolean flag;
        if(fineListenerInUse || coarseListenerInUse)
        {
            flag = true;
        } else
        {
            if(locationMgr == null)
            {
            	locationMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            }
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            
            String s = locationMgr.getBestProvider(criteria, true);
            if(s != null)
            {
            	fineLocationListener = createFineLocationListener();
                locationMgr.requestLocationUpdates(s, 0L, 0F, fineLocationListener);
                
                fineListenerInUse = true;
            }
            boolean flag1 = false;
            if(s != null)
            {
            	lastKnownLocation = locationMgr.getLastKnownLocation(s);
                if(lastKnownLocation != null)
                    flag1 = true;
            }
            Criteria criteria1 = new Criteria();
            criteria1.setAccuracy(Criteria.ACCURACY_COARSE);
         
            String s1 = locationMgr.getBestProvider(criteria1, true);
            if(s1 != null)
            {
            	coarseLocationListener = createCoarseLocationListener();
                locationMgr.requestLocationUpdates(s1, 0L, 0F, coarseLocationListener);
                coarseListenerInUse = true;
                if(!flag1)
                {
                	lastKnownLocation = locationMgr.getLastKnownLocation(s1);
                    if(lastKnownLocation != null)
                        flag1 = true;
                }
            }
                      
            if(fineLocationListener == null && coarseLocationListener == null)
            {
                flag = false;
            } else
            {
            	locationListenStart = System.currentTimeMillis();
                StringBuilder stringbuilder = (new StringBuilder()).append("^v^v^v^v Starting countdown to forced loc update. Time: ");
                long l1 = System.currentTimeMillis();
                String s2 = stringbuilder.append(l1).toString();
                Log.d(LOG_TAG, s2);
                handler.postDelayed(forceLocationUpdate, 5000L);
                StringBuilder stringbuilder2 = (new StringBuilder()).append("registerForLocationUpdates: providers enabled: ");
                String s5 = android.provider.Settings.Secure.getString(getContentResolver(), "location_providers_allowed");
                String s6 = stringbuilder2.append(s5).toString();
                Log.d(LOG_TAG, s6);
                flag = true;
            }
        }
        return flag;
    }

    public void removeStateChangeListener(StateChangeListener statechangelistener)
    {
        if(stateChangeListeners == null)
        {
            return;
        } else
        {
            stateChangeListeners.remove(statechangelistener);
            return;
        }
    }

    public boolean requestLocationUpdate(LocationChangeListener locationchangelistener, long l)
    {
        if(locationOneShotListeners == null)
        {
            locationOneShotListeners = new LinkedList<LocationChangeListener>();;
        }
   
        synchronized(locationOneShotListeners)
        {
            locationOneShotListeners.add(locationchangelistener);
        }
        registerForLocationUpdates();
        boolean flag = registerForLocationUpdatesUsingBaidu();
        if(!flag)
            synchronized(locationOneShotListeners)
            {
                locationOneShotListeners.remove(locationchangelistener);
            }
        return flag;
    }

    public void resume()
    {
    	 Log.i("BocaiApplication", " resume method");
    	StringBuilder stringbuilder = (new StringBuilder()).append("+++ resume() at ");
        Date date = new Date();
        String s = stringbuilder.append(date).append(" +++").toString();
        Log.d("BocaiApplication", s);
        if(firstLaunch)
            doStartupTasks();
        if(stateChangeListeners != null)
        {
            for(Iterator<StateChangeListener> iterator = stateChangeListeners.iterator(); iterator.hasNext(); ((StateChangeListener)iterator.next()).onStateChange(1));
        }
        firstLaunch = false;
    }

    public void reverseGeocode(Location location, AddressChangeListener addresschangelistener)
    {
       Log.i("BocaiApplication", "reverseGeocode method");
    	if(location == null)
        {
            return;
        } 
            Object aobj[] = new Object[3];
            Double double1 = Double.valueOf(location.getLatitude());
            aobj[0] = double1;
            Double double2 = Double.valueOf(location.getLongitude());
            aobj[1] = double2;
            aobj[2] = Locale.getDefault().toString();
            String s = String.format("http://maps.googleapis.com/maps/api/geocode/json?sensor=false&latlng=%f,%f&language=%s", aobj);
            AsyncHTTPRequest asynchttprequest = new AsyncHTTPRequest(s);
            AsyncHTTPResponseHandler asynchttpresponsehandler = geocodeResponseHandler;
            asynchttprequest.responseHandler = asynchttpresponsehandler;
            asynchttprequest.userData = addresschangelistener;
            asynchttprequest.execute();

    }

    void saveAccount(JSONObject jsonObject)
    {
    	  Log.i("BocaiApplication", "on saveAccount method");
    	  Log.i("BocaiApplication","jsonObject.toString==" + jsonObject.toString());
    	  String email = jsonObject.optString("email");
          Macros.FS_DEFAULT_SET_STRING("email", email);
          currentUser = new User(jsonObject);
          User.archiveUser(currentUser);
          StringBuilder stringbuilder = (new StringBuilder()).append("saveAccount: ");
          String str = stringbuilder.append(currentUser).append(" written").toString();
          Log.d("BocaiApplication", str);
          pingBackgroundThreadStart();
    }

    void showAuthControllerWithSpot(boolean flag)
    {
    	  Intent intent = new Intent(this, com.bocai.AuthenticationActivity.class);
      
          if(flag){
              intent.putExtra("loadSpot", flag);
          }
          intent.setFlags(0x20000000);
          startActivity(intent);
    }

    public void unregisterForLocationUpdates()
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("unregisterForLocationUpdates: fineListener ");
        LocationListener locationlistener = fineLocationListener;
        StringBuilder stringbuilder1 = stringbuilder.append(locationlistener).append(" used: ");
        boolean flag = fineListenerInUse;
        StringBuilder stringbuilder2 = stringbuilder1.append(flag).append(", coarseListener ");
        LocationListener locationlistener1 = coarseLocationListener;
        StringBuilder stringbuilder3 = stringbuilder2.append(locationlistener1).append(" used: ");
        boolean flag1 = coarseListenerInUse;
        String s = stringbuilder3.append(flag1).toString();
        Log.d("BocaiApplication", s);
        if(fineListenerInUse)
        {
            LocationManager locationmanager = locationMgr;
            LocationListener locationlistener2 = fineLocationListener;
            locationmanager.removeUpdates(locationlistener2);
            fineLocationListener = null;
            fineListenerInUse = false;
        }
        if(coarseListenerInUse)
        {
            LocationManager locationmanager1 = locationMgr;
            LocationListener locationlistener3 = coarseLocationListener;
            locationmanager1.removeUpdates(locationlistener3);
            coarseLocationListener = null;
            coarseListenerInUse = false;
        }
        locationMgr = null;
    }

    public static final int APP_PAUSE = 2;
    public static final int APP_RESUME = 1;
//    private static final float DISTANCE_TOLERANCE = 75F;
//    private static final String EMULATOR_DEVICE_ID = "emulatoremulator";
//    private static final int FILE_CHECK_INTERVAL = 0x493e0;
//    private static final String GEOCODE_URL = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&latlng=%f,%f";
//    private static final int LOCATION_LISTEN_WINDOW = 5000;
    private static final String LOG_TAG = "BocaiApplication";
//    private static final long UPDATE_CHECK_INTERVAL = 0x5265c00L;
    public static String deviceId = null;
    public static BocaiApplication instance = null;
    private boolean checkedForAccount;
    private boolean coarseListenerInUse;
    private LocationListener coarseLocationListener;
    public GoogleAddress currentAddress;
    public Location currentLocation;
    public User currentUser;
    public boolean deviceIsHiRes;
    private Timer fileCacheTimer;
    private boolean fineListenerInUse;
    private LocationListener fineLocationListener;
    private boolean firstLaunch;
    public boolean flurryEnabled;
    private final Runnable forceLocationUpdate;
    AsyncHTTPResponseHandler geocodeResponseHandler;
    private final Handler handler;
    public ImageDownloader imageDownloader;
    public Location lastKnownLocation;
    private long locationListenStart;
    private boolean locationManagerOverride;
    private LocationManager locationMgr;
    public List<LocationChangeListener> locationOneShotListeners;
    private LinkedList<StateChangeListener> stateChangeListeners;
    //GoogleAnalyticsTracker tracker;
    AsyncHTTPResponseHandler versionInfoResponseHandler;
    private boolean updateAvailable;
    private String updateMessage;
    private int updateNewVersion;
    private boolean updateOptional;
    
    private boolean hasChecked = false;
    
    BMapManager mBMapMan = null;
    com.baidu.mapapi.LocationListener mLocationListener = null;
    
	@Override
	public void handleResponse(AsyncHTTPRequest asyncHttpRequest,
			InputStream stream, long length) throws IOException {
		
		
		try {
			 String s = AsyncHTTPRequest.toString(stream, length);
	         JSONObject jsonObject = new JSONObject(s);
	         jsonObject.put("cookies", asyncHttpRequest.responseCookies);
	         boolean success = jsonObject.getBoolean("success");
	         if(success){
	        	 saveAccount(jsonObject);
	         }else{
	        	 displayErrors(jsonObject);
	         }
		} catch (JSONException e) {
			StringBuilder sb = (new StringBuilder()).append("Error parsing authentication response: ");
			sb.append(e.getLocalizedMessage());
	        Log.e("BocaiApplication", sb.toString(), e);
	        e.printStackTrace();
		}
         
		
	}
	
	@Override
	 public void handleError(String s, InputStream inputstream, long l)throws IOException  {
//			try{
//				(new android.app.AlertDialog.Builder(this)).setTitle("#*&^@*!&^@#*!:").setMessage(s).setPositiveButton("...ok...", null).show();
//			}catch(Exception e){
//				throw new IOException(e.getLocalizedMessage());
//			}
		//TODO:
		 Log.e(LOG_TAG, "handle http response error:" + s);
		
	  }
	
	public void setHasChecked(boolean checked) {
		this.hasChecked = checked;
	}
	
	private void initBaiduMapManager() {

		mBMapMan = new BMapManager(this);
		mBMapMan.init("DD1554BA48D27444258B3C583FC5464B5327E0C5", null);
		mBMapMan.start();
		
		mLocationListener = new com.baidu.mapapi.LocationListener(){
			@Override
			public void onLocationChanged(Location location) {
				if(location != null){
					Log.i(LOG_TAG, "--- com.baidu.mapapi.LocationListener ---");
					BocaiApplication.this.onLocationChanged(location);
				}
			}
        };
	}
	
    public void unregisterForLocationUpdatesUsingBaidu() {
    	Log.i(LOG_TAG, "--- unregisterForLocationUpdatesUsingBaidu ---");
    	mBMapMan.getLocationManager().removeUpdates(mLocationListener);
    }
    

	public boolean registerForLocationUpdatesUsingBaidu() {
    	Log.i(LOG_TAG, "--- registerForLocationUpdatesUsingBaidu ---");
		mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		return true;
	}
	
}
