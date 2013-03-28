package com.bocai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bocai.map.MapController;
import com.bocai.model.FSObject;
import com.bocai.model.FSObjectDelegate;
import com.bocai.model.Filter;
import com.bocai.model.Promo;
import com.bocai.model.Search;
import com.bocai.model.Sighting;
import com.bocai.model.User;
import com.bocai.net.AsyncHTTPRequest;
import com.bocai.util.FlurryEvents;
import com.bocai.util.Macros;
import com.bocai.view.StreamView;
import com.bocai.widget.SightingViewAdapter;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class BrowseActivity extends MapActivity
    implements 
    BocaiApplication.LocationChangeListener, 
    BocaiApplication.StateChangeListener, BocaiApplication.AddressChangeListener, 
    FSObjectDelegate, HomeActivity.ToolbarItemSource, 
    com.bocai.map.MapController.MapControllerListener
{

    public BrowseActivity()
    {
    	showingMap = false;
        currentPage = 1;
        totalPages = 0;
        currentSearch = null;
        currentPlaceId = null;
        justTappedLocate = false;
        ignoreFilterChange = false;
        firstLaunch = true;
        shouldRequestLocation = true;
        searchingWithPlaceId = false;
        searchChromeDisplayed = false;
        handler = new Handler();
        
        refreshStreamView = new Runnable() {

            public void run()
            {
                Log.i(LOG_TAG, "refreshStreamView runnable");
            	sightingViewAdapter.notifyDataSetChanged();
            }
        };
        
			startNextPageLoad = new Runnable() {

				public void run()
				{
					 Log.i(LOG_TAG, "startNextPageLoad runnable");
					if(pageNext.getVisibility() != View.VISIBLE)
					{
						return;
					} else
					{
						loadNextPage();
						return;
					}
				}
        };
       
			hideNextPage = new Runnable() {

            public void run()
            {
            	 Log.i(LOG_TAG, "hideNextPage runnable");
            	pageNext.setVisibility(View.GONE);
                pageNext.findViewById(R.id.progress).setVisibility(View.GONE);
            }
        };
        
			run_hideSearchBar = new Runnable() {

            public void run()
            {
            	Log.i(LOG_TAG, "run_hideSearchBar runnable");
            	hideSearchBar();
            }
        };
      
		showNoResultsView = new Runnable() {

            public void run()
            {
            	Log.i(LOG_TAG, "showNoResultsView runnable");
            	if(noResultsView == null)
                {
                    return;
                } else
                {
                    noResultsView.setVisibility(0);
                    return;
                }
            }
        };
        
			hideNoResultsView = new Runnable() {

            public void run()
            {
            	Log.i(LOG_TAG, "hideNoResultsView runnable");
            	if(noResultsView == null)
                {
                    return;
                } else
                {
                    noResultsView.setVisibility(8);
                    return;
                }
            }
        };
        
        
        run_showHintListView = new Runnable(){

			@Override
			public void run() {
				String keyWord = searchField.getText().toString();
				hints.clear();
				
				Resources res = getResources();
	
				String str1 = res.getText(R.string.search_dish).toString();
				str1 = String.format(str1,keyWord);
				search_dish = Html.fromHtml(str1);
				hints.add(search_dish);
			
			    String str2 = res.getText(R.string.search_place).toString();
				str2 = String.format(str2,keyWord);
			    search_place = Html.fromHtml(str2);
				hints.add(search_place);
			
				hintViewAdapter.notifyDataSetChanged();
				hintListView.setVisibility(View.VISIBLE);
			}
        	
        };
        
        run_hideHintListView = new Runnable(){

			@Override
			public void run() {
				
				hints.clear();
				hintViewAdapter.notifyDataSetChanged();
				hintListView.setVisibility(View.GONE);
			}
        	
        };
        
        
			refreshButtonListener = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
            	Log.i(LOG_TAG, "refreshButtonListener onClick");
				if (!BocaiApplication.isHttpAvailable(BrowseActivity.this)) {
					return ;
				}
                String locating = getString(R.string.browse_locating);
                BrowseActivity.this.showLoadingView(locating);
                BocaiApplication bocaiApplication = Macros.FS_APPLICATION();
                
                if(bocaiApplication.requestLocationUpdate(BrowseActivity.this, 5000L))
                {
                    return;
                } else
                {
                    hideLoadingView();
                    locationSettingsDialog().show();
                    return;
                }
            }
        };
       
			searchListener = new android.widget.TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView textview, int keyCode, KeyEvent keyEvent)
            {
            	Log.i(LOG_TAG, "onEditorAction");
            	boolean flag;
                if(keyEvent != null)
                {
                    if(keyEvent.getAction() == KeyEvent.KEYCODE_SOFT_LEFT)
                    {
                        String s = textview.getText().toString();
                        BrowseActivity.this.doSearchDish(s);
                        hideKeyboard();
                    }
                    flag = true;
                } else
                {
                    flag = false;
                }
                return flag;
            }
        };
        
			run_showLoadingView = new Runnable() {

            public void run()
            {
            	Log.i(LOG_TAG, "run_showLoadingView Runnable");
            	if(!isCurrentActivity())
                    return;
                if(loadingView == null)
                {
          
                	loadingView = new ProgressDialog(BrowseActivity.this);
                    loadingView.setIndeterminate(true);
                }
               
                loadingView.setMessage(loadingMessage);
                loadingView.show();
                View view = loadingView.findViewById(R.id.loading_msg);
                if(view == null)
                {
                    return;
                } else
                {
                    //view.setVisibility(8);
                    view.setVisibility(0);
                    return;
                }
            }
        };
        
		run_showGPSDialog = new Runnable() {

            public void run()
            {
            	Log.i(LOG_TAG, "run_showGPSDialog Runnable");
            	if(!isCurrentActivity())
                    return;
                if(loadingView == null)
                {
          
                	loadingView = new ProgressDialog(BrowseActivity.this);
                    loadingView.setIndeterminate(true);
                }
               
                loadingView.setMessage(loadingMessage);
                loadingView.show();
                View view = loadingView.findViewById(R.id.loading_msg);
                if(view == null)
                {
                    return;
                } else
                {
                    view.setVisibility(8);
                    //view.setVisibility(0);
                    return;
                }
            }
        };
       
			run_hideLoadingView = new Runnable() {

            public void run()
            {
            	Log.i(LOG_TAG, "run_hideLoadingView Runnable");
            	if(loadingView == null)
                    return;
                if(!loadingView.isShowing())
                {
                    return;
                } else
                {
                    loadingView.dismiss();
                    return;
                }
            }
        };
        
			clearSearchListener = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
            	Log.i(LOG_TAG, "clearSearchListener onClick");
            	clearSearch();
                Filter.setAnywhere();
                Filter.setNearest();
                refreshFromFilter();
            }
        };
        
			updateSearchResultsCaption = new Runnable() {

            public void run()
            {
            	Log.i(LOG_TAG, "updateSearchResultsCaption Runnable");
            	if(searchResultsCaption != null)
                {
                    searchLabel.setText(searchResultsCaption);
                }
            }
        };
        
        hintClickListener = new android.widget.AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
					hideSearchChrome();
					String query = BrowseActivity.this.searchField.getText().toString();
				if(position == 0){
					doSearchDish(query);
				}else if (position == 1){
					doSearchPlace(query);
				}else{
					//Nothing to do here
				}
				
			}
        	
        };
        
    }

    private AlertDialog locationSettingsDialog()
    {
    	Log.i(LOG_TAG, "locationSettingsDialog");
    	if(locationSettingsDialog == null)
        {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            String s = getString(R.string.app_name);
            builder.setTitle(s).setIcon(R.drawable.icon);
            String s1 = getString(R.string.location_disabled);
            builder.setMessage(s1);
            String s2 = getString(R.string.location_settings_btn);
            android.content.DialogInterface.OnClickListener onclicklistener = new android.content.DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialoginterface, int i)
                {
                    dialoginterface.dismiss();
                    BrowseActivity browseactivity = BrowseActivity.this;
                    Intent intent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
                    browseactivity.startActivityForResult(intent, 1);
                }
            };
			locationSettingsDialog = builder.setPositiveButton(s2, onclicklistener).setNegativeButton(0x1040000, null).create();
        }
        return locationSettingsDialog;
    }

    private void showMap()
    {
    	Log.i(LOG_TAG, "showMap");
    	streamMapGroup.setInAnimation(slideRightIn);
        streamMapGroup.setOutAnimation(slideRightOut);
        streamMapGroup.showPrevious();
    }

    private void showStream()
    {
    	Log.i("LOG_TAG", "showStream");
    	streamMapGroup.setInAnimation(slideLeftIn);
        streamMapGroup.setOutAnimation(slideLeftOut);
        streamMapGroup.showNext();
    }

    private void toggleView(ImageButton imagebutton)
    {
    	Log.i(LOG_TAG, "toggleView");
    	if(showingMap)
        {
            showStream();
            imagebutton.setImageResource(R.drawable.button_map);
            if(!Filter.sortNearest())
                mapController.refreshFromResize(false);
            showingMap = false;
        } else
        {
            showMap();
            imagebutton.setImageResource(R.drawable.button_photos);
            mapController.regionDidChangeCounter = 1;
            mapController.lastRefreshCounter = 1;
            showingMap = true;

        }
    }

	int BUTTON_ID_TO_FILTER(int i)
    {
		Log.i(LOG_TAG, "BUTTON_ID_TO_FILTER");
		if (i == R.id.filter_nearest)
			return 1;
		if (i == R.id.filter_latest)
			return 2;
		if (i == R.id.filter_best)
			return 3;
		return -1;
    }

    int FILTER_TO_BUTTON_ID(int i)
    {
    	Log.i(LOG_TAG, "FILTER_TO_BUTTON_ID");
    	if (i == R.id.filter_nearest)
    		return 1;
    	if (i == R.id.filter_latest)
    		return 2;
    	if (i == R.id.filter_best)
    		return 3;
    	return -1;
    }

    public void FSResponse(List<FSObject> paramList)
    {
    	Log.i(LOG_TAG, "FSResponse");
    	hideLoadingView();
        if (tmpSightings == null)
        {
        	tmpSightings = new LinkedList<Sighting>();
        }
        
        if(paramList == null || paramList.size() == 0){
        	this.mapController.clearSightings();  //clear history record
        	handler.post(refreshStreamView);  //clear history record
        	handler.post(showNoResultsView);
        	return;
        }
  	
        Iterator<FSObject> iterator = paramList.iterator();
          while(iterator.hasNext()){
        	Sighting sighting = (Sighting)iterator.next();
//        	if(sighting.sightingID != null && sighting instanceof Promo ){
//        		tmpSightings.add(sighting);
//        	}
        	//TODO 
        	tmpSightings.add(sighting);
          }

        //TODO:  
        //this.totalPages = 0;
          
        this.mapController.clearSightings();
        Object[] arrayOfObject = new Object[2];
        arrayOfObject[0] = Integer.valueOf(this.tmpSightings.size());
        
        arrayOfObject[1] = Integer.valueOf(this.totalPages);
        
        String str = String.format("BrowseViewController with %d sightings (%d total pages)", arrayOfObject);
        Log.d(LOG_TAG, str);
        if (tmpSightings.size() > 0)
        {
          handler.post(hideNoResultsView);
        }
        populateSightingsView(tmpSightings);
        tmpSightings.clear();
        if(!justTappedLocate){
        	return;
        }
        Macros.FS_APPLICATION().displayLocation();
        this.justTappedLocate = false;
        handler.post(hideNoResultsView);
        Log.d(LOG_TAG, "FSResponse end####");
    }

    void addRequestObject(FSObject fsobject)
    {
    	Log.i(LOG_TAG, "addRequestObject");
    	if(fsobject == null || fsobject.wasCancelled())
        {
            Log.d(LOG_TAG, "TRYING TO ADD BAD CURRENT OBJECT");
            return;
        } else
        {
            Object aobj[] = new Object[2];
            Class<?> class1 = fsobject.getClass();
            aobj[0] = class1;
            String s = fsobject.url();
            aobj[1] = s;
            String s1 = String.format("ADDING REQUEST %s : %s", aobj);
            Log.d(LOG_TAG, s1);
            requestObjects.add(fsobject);
            StringBuilder stringbuilder = (new StringBuilder()).append("Request Objects: ");
            int k = requestObjects.size();
            String s2 = stringbuilder.append(k).toString();
            Log.d(LOG_TAG, s2);
            return;
        }
    }

    public void addressChanged(final String address)
    {
       Log.i(LOG_TAG, "addressChanged method=" + address);
    	if(address == null)
        {
            return;
        } else
        {
            Runnable runnable = new Runnable() {

                public void run()
                {
                    locationLabel.setText(address);
                }
            };
            handler.post(runnable);
            return;
        }
    }

    public boolean calloutClickedForSighting(Sighting sighting1)
    {
        Log.i(LOG_TAG, "calloutClickedForSighting method");
    	if(sighting1 != null)
        {
            Intent intent = new Intent(this, com.bocai.ReviewActivity.class);
            intent.setFlags(0x20000000);
            intent.putExtra("sighting", sighting1);
            String s = sighting1.sightingID;
            intent.putExtra("sighting_id", s);
            android.app.Activity activity = getParent();
            if(activity instanceof TabStackActivityGroup)
                ((TabStackActivityGroup)activity).pushIntent(intent);
        }
        return true;
    }

    void cancelAllRequests()
    {
    	 Log.i(LOG_TAG, "cancelAllRequests method");
  
    	for(Iterator<FSObject> iterator = requestObjects.iterator(); iterator.hasNext();)
        {
            FSObject fsobject = iterator.next();
            if(!fsobject.wasCompleted() && !fsobject.wasCancelled())
                fsobject.cancelRequests();
            Object aobj[] = new Object[3];
            Class<?> class1 = fsobject.getClass();
            aobj[0] = class1;
            Boolean boolean1 = Boolean.valueOf(fsobject.wasCompleted());
            aobj[1] = boolean1;
            Boolean boolean2 = Boolean.valueOf(fsobject.wasCancelled());
            aobj[2] = boolean2;
            String s = String.format("Cancelling %s %b %b", aobj);
            Log.d(LOG_TAG, s);
        }

        requestObjects.clear();
    }

    void clearSearch()
    {
    	Log.i(LOG_TAG, "clearSearch method");
    	cancelAllRequests();
        currentSearch = null;
        searchField.setText(null);
        searchingWithPlaceId = false;
        if(!searchChromeDisplayed)
        {
            return;
        } else
        {
            hideSearchChrome();
            return;
        }
    }

    public void displayErrors(JSONObject jsonobject)
    {
    	 Log.i(LOG_TAG, "displayErrors method");
    	String s = (new StringBuilder()).append("displayErrors(").append(jsonobject).append(")").toString();
        Log.d(LOG_TAG, s);
    }

    public void displaySuccess(JSONObject jsonobject)
    {
    	//blank method
    }

    void doSearchDish(String s)
    {
    	Log.i(LOG_TAG, "doSearchDish method");
		if (!BocaiApplication.isHttpAvailable(BrowseActivity.this)) {
			return ;
		}
    	String s1 = (new StringBuilder()).append("doSearch: ").append(s).toString();
        Log.d(LOG_TAG, s1);
        String s2 = getString(R.string.browse_loading);
        showLoadingView(s2);
        Filter.setAnywhere();
        Filter.setNearest();
        updatedSortFilter();
        currentSearch = s;
        sightings.clear();
        mapController.clearSightings();
        currentPage = 1;
        totalPages = 0;
        mapController.setZoomOutInclude(true);
        cancelAllRequests();
        search = new Search();
       
        search.delegate = this;
        searchingWithPlaceId = false;
        if(!searchChromeDisplayed)
            showSearchChrome();
    
        searchLabel.setText(currentSearch);
        searchLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white, 0, 0, 0);
        float f = getResources().getDisplayMetrics().density;
        int j = (int)(6F * f);
        searchLabel.setCompoundDrawablePadding(j);        
        makePrimaryRequestObject(search);
        
        Location location = Macros.FS_CURRENT_LOCATION();
        if(location == null){
        	location = new Location("explicit");
        	double d1 = (double)mapView.getMapCenter().getLatitudeE6() / 1000000D;
        	location.setLatitude(d1);
        	double d2 = (double)mapView.getMapCenter().getLongitudeE6() / 1000000D;
        	location.setLongitude(d2);
        }
        
        search.doSightingSearch(s, location, 5D,null);
    }
    
    void doSearchPlace(String s)
    {
    	Log.i(LOG_TAG, "doSearchPlace method");
		if (!BocaiApplication.isHttpAvailable(BrowseActivity.this)) {
			return ;
		}
    	String s1 = (new StringBuilder()).append("doSearchPlace: ").append(s).toString();
        Log.d(LOG_TAG, s1);
        String s2 = getString(R.string.browse_loading);
        showLoadingView(s2);
        Filter.setAnywhere();
        Filter.setNearest();
        updatedSortFilter();
        currentSearch = s;
        sightings.clear();
        mapController.clearSightings();
        currentPage = 1;
        totalPages = 0;
        mapController.setZoomOutInclude(true);
        cancelAllRequests();
        search = new Search();
       
        search.delegate = this;
        searchingWithPlaceId = false;
        if(!searchChromeDisplayed)
            showSearchChrome();
    
        searchLabel.setText(currentSearch);
        searchLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white, 0, 0, 0);
        float f = getResources().getDisplayMetrics().density;
        int j = (int)(6F * f);
        searchLabel.setCompoundDrawablePadding(j);        
        makePrimaryRequestObject(search);
        //NOTE: search food with 5 km
      
        Location location = Macros.FS_CURRENT_LOCATION();
        if(location == null){
        	location = new Location("explicit");
        	double d1 = (double)mapView.getMapCenter().getLatitudeE6() / 1000000D;
        	location.setLatitude(d1);
        	double d2 = (double)mapView.getMapCenter().getLongitudeE6() / 1000000D;
        	location.setLongitude(d2);
        }
        
        search.doSightingSearch(s, location, 5D,"place");
    }

    public void doSearchWithName(String s)
    {
    	//blank method
    }

    void downloadSightings(int pageSize, int pageNum, String sort, Location location)
    {
    	downloadSightings(pageSize, pageNum, sort, location, 5D);
    }

    void downloadSightings(int pageSize, int pageNum, String sort, Location location, double within)
    {
    	 Log.i(LOG_TAG, "downloadSightings method");
    	cancelAllRequests();
        if(sighting != null)
            sighting = null;
        HashMap<String,Object> params = new HashMap<String,Object>();
        
        params.put("per_page", pageSize);
        
        params.put("page", pageNum);
        params.put("sort", sort);
        
        params.put("filter", Filter.filterResultsString());
        Filter.setFilterArea(1);

        AsyncHTTPRequest asyncHttpRequest;
        User user;

        if(location != null)
        {
            params.put("latitude", location.getLatitude());
           
            params.put("longitude", location.getLongitude());
            
            Object aobj[] = new Object[1];
            aobj[0] = Double.valueOf(within);
            String s2 = String.format("%1.3f", aobj);
            params.put("within", s2);
            //the param within is always required
            /*
            if(!Filter.sortNearest())
            {
                Object aobj[] = new Object[1];
                aobj[0] = Double.valueOf(within);
                String s2 = String.format("%1.3f", aobj);
                params.put("within", s2);
            }
            */
        } else
        {
            Log.d(LOG_TAG, "NO LOCATION FOR SIGHTINGS WTF!");
        }
        
        asyncHttpRequest = Sighting.listRequestWithParameters(params);
        user = User.currentUser();
        asyncHttpRequest.setUseCookiePersistence(true);
        if(user != null && user.cookies != null)
        {
            
            asyncHttpRequest.setRequestCookies(user.cookies);
        }
        StringBuilder stringbuilder = (new StringBuilder()).append("downloadSightings Request: ");
        
        Log.d(LOG_TAG, stringbuilder.append(asyncHttpRequest.url).toString());
        sighting = new Sighting(asyncHttpRequest);
        
        sighting.delegate = this;
        
        makePrimaryRequestObject(sighting);
    }

    public void finishedAction(JSONObject jsonObject)
        throws JSONException
    {
        Log.i(LOG_TAG, "finishedAction method");
    	String s = (new StringBuilder()).append("finishedAction(").append(jsonObject).append(")").toString();
        Log.d(LOG_TAG, s);
        if(jsonObject == null)
            return;
        if(jsonObject.getString("action").equals("total")) {
        	totalPages = jsonObject.getInt("total");
        	Log.i(LOG_TAG, "=============total pages======" + totalPages);
        }
    }

    public View[] getToolbarItems()
    {
    	if (toolbarItems != null) {
    		return toolbarItems;
    	}

        ViewGroup viewgroup = (ViewGroup)getLayoutInflater().inflate(R.layout.browse_toolbar_items, null);
        toolbarItems = new View[viewgroup.getChildCount()];
        
        int i = 0;
        while(viewgroup.getChildCount() > 0) 
        {
            View view = viewgroup.getChildAt(0);
            viewgroup.removeView(view);
            
            toolbarItems[i] = view;
            if(view.getId() == R.id.button_map)
            {
                android.view.View.OnClickListener onclicklistener = new android.view.View.OnClickListener() {

                    public void onClick(View view1)
                    {
                        BrowseActivity.this.toggleView((ImageButton)view1);
                    }
                }
;
                view.setOnClickListener(onclicklistener);
            } else
            if(view.getId() == R.id.button_shot)
            {
                android.view.View.OnClickListener onclicklistener1 = new android.view.View.OnClickListener() {

                    public void onClick(View view1)
                    {
                        
                        Runnable runnable = new Runnable() {

                            public void run()
                            {
                                if(User.isLoggedIn()){
                                	((HomeActivity)getParent()).showSpotActivity();
                                }else{
                                	((HomeActivity)getParent()).showAuthenticationActivity(true);
                                }
                            }
                        };
                        handler.post(runnable);
                    }
                };
                view.setOnClickListener(onclicklistener1);
            } else
            if(view.getId() == R.id.button_search)
            {
                android.view.View.OnClickListener onclicklistener2 = new android.view.View.OnClickListener() {

                    public void onClick(View view1)
                    {
                        if(searchBar.getVisibility() == 0)
                        {
                            hideSearchBar();
                            hideKeyboard();
                            return;
                        } else
                        {
                            showSearchBar();
                            return;
                        }
                    }
                };
                view.setOnClickListener(onclicklistener2);
            } else 
            if (view.getId() == R.id.button_manual_refresh) {
                android.view.View.OnClickListener onclicklistener = new android.view.View.OnClickListener() {

                    public void onClick(View view)
                    {
                    	Log.i(LOG_TAG, "button_manual_refresh onClick");
        				if (!BocaiApplication.isHttpAvailable(BrowseActivity.this)) {
        					return ;
        				}
                        String locating = getString(R.string.browse_locating);
                        BrowseActivity.this.showLoadingView(locating);
                        BocaiApplication bocaiApplication = Macros.FS_APPLICATION();
                        
                        if(bocaiApplication.requestLocationUpdate(BrowseActivity.this, 5000L))
                        {
                            return;
                        } else
                        {
                            hideLoadingView();
                            locationSettingsDialog().show();
                            return;
                        }
                    }
                };
                view.setOnClickListener(onclicklistener);
            }
            i++;
        }

        return toolbarItems;
    }

    public void handleRefresh()
    {
    	 Log.i(LOG_TAG, "handleRefresh method");
    	if(Filter.sortNearest())
        {
            refreshFromFilter();
        } else
        {
            ignoreFilterChange = true;
            filterModes.check(R.id.filter_nearest);
            ignoreFilterChange = false;
            Filter.setNearest();
            currentFilterSort = 1;
            refreshFromFilter();
        }
        justTappedLocate = true;
        if(showingMap)
        {
            FlurryEvents.FLURRY_LOG_LOCATE_ME("browse tab map");
            return;
        } else
        {
            FlurryEvents.FLURRY_LOG_LOCATE_ME("browse tab");
            return;
        }
    }

    void hideKeyboard()
    {
    	 Log.i(LOG_TAG, "hideKeyboard method");
    	View view = getCurrentFocus();
        if(view == null)
            view = getWindow().getDecorView();
        if(view != null) {
   
            InputMethodManager inputmethodmanager = (InputMethodManager)getSystemService("input_method");
            android.os.IBinder ibinder = view.getWindowToken();
            inputmethodmanager.hideSoftInputFromWindow(ibinder, 0);
        }
    }

    void hideLoadingView()
    {
    	 Log.i(LOG_TAG, "hideLoadingView method");
    	handler.post(run_hideLoadingView);
    }

    void hideSearchBar()
    {
    	 Log.i(LOG_TAG, "hideSearchBar method");
    	if(searchBar.getVisibility() == 8)
        {
            return;
        } else
        {
        	searchBar.startAnimation(pushUpOut);
            filterModes.setVisibility(0);
            
            hintListView.setVisibility(View.GONE);
            return;
        }
    }

    void hideSearchChrome()
    {
    	 Log.i(LOG_TAG, "hideSearchChrome method");
    	hideSearchBar();
        dismissButton.setVisibility(8);
        searchLabel.setCompoundDrawables(null, null, null, null);
        searchLabel.setCompoundDrawablePadding(0);
        searchChromeDisplayed = false;
        handler.post(run_hideHintListView);
        
    }

    void initSearchChrome()
    {
    	 Log.i(LOG_TAG, "initSearchChrome method");
    	 
    	search_dish = getText(R.string.search_dish).toString();
    	search_place = getText(R.string.search_place).toString();
    	 
    	searchBar = (ViewGroup)findViewById(R.id.search_bar);
    	    	
    	searchField = (EditText)searchBar.findViewById(R.id.search);
        
        searchField.setOnEditorActionListener(searchListener);
        
        searchField.setOnFocusChangeListener( new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(hasFocus){
					 BrowseActivity activity = BrowseActivity.this;
					if(activity.searchField.getText() != null && activity.searchField.getText().length() > 0){
						activity.handler.post(run_showHintListView);
					}
				}
			}
          }
        );
        
        
        
        View view = searchBar.findViewById(R.id.search_go_btn);
        
        TextWatcher searchTextWatcher = new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if(s.length() == 0){
					handler.post(run_hideHintListView);
				}else{
					handler.post(run_showHintListView);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				//blank
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				//blank
			}
        	
        };
        
        searchField.addTextChangedListener(searchTextWatcher);
       
        
        android.view.View.OnClickListener onclicklistener = new android.view.View.OnClickListener() {

            public void onClick(View view1)
            {
                BrowseActivity browseactivity = BrowseActivity.this;
                String s = searchField.getText().toString();
                browseactivity.doSearchDish(s);
                hideKeyboard();
            }
        };
        
        view.setOnClickListener(onclicklistener);
        pushDownIn = AnimationUtils.loadAnimation(this, R.anim.push_down_in);
        
        
        android.view.animation.Animation.AnimationListener animationlistener = new android.view.animation.Animation.AnimationListener() {

            public void onAnimationEnd(Animation animation4)
            {
                filterModes.setVisibility(8);
                BrowseActivity browseactivity = BrowseActivity.this;
                browseactivity.showKeyboard(searchField);
            }

            public void onAnimationRepeat(Animation animation4)
            {
            }

            public void onAnimationStart(Animation animation4)
            {
            }
        };
        
		pushDownIn.setAnimationListener(animationlistener);
		pushUpOut = AnimationUtils.loadAnimation(this, R.anim.push_up_out);
        
        
        android.view.animation.Animation.AnimationListener animationlistener1 = new android.view.animation.Animation.AnimationListener() {

            public void onAnimationEnd(Animation animation4)
            {
                searchBar.setVisibility(8);
                filterModes.setVisibility(0);
            }

            public void onAnimationRepeat(Animation animation4)
            {
            }

            public void onAnimationStart(Animation animation4)
            {
            }
        }
;
		pushUpOut.setAnimationListener(animationlistener1);
		dismissButton = (ImageButton)findViewById(R.id.btn_dismiss);
        
        dismissButton.setOnClickListener(clearSearchListener);
        hideSearchBar();
        hideSearchChrome();

        hintListView = (ListView) findViewById(R.id.list_hint);
        hints = new LinkedList<CharSequence>();
        hintViewAdapter = new ArrayAdapter<CharSequence>(this,R.layout.hint_list_item,R.id.hintTextItem,hints);
        hintListView.setAdapter(hintViewAdapter);
        hintListView.setOnItemClickListener(hintClickListener);
        
    }

    boolean isCurrentActivity()
    {
    	Log.i(LOG_TAG, "isCurrentActivity method");
    	boolean flag;
        if(((HomeActivity)getParent()).getCurrentActivity() == this)
            flag = true;
        else
            flag = false;
        return flag;
    }

    protected boolean isRouteDisplayed()
    {
        return false;
    }

    void loadNextPage()
    {
    	Log.i(LOG_TAG, "loadNextPage method");
    	currentPage++;
        requestCurrentPageResults();
    }

    public void loadSightingsAtCoordinate(Location location, double d, int i)
    {
    	Log.i(LOG_TAG, "loadSightingsAtCoordinate method");
    	if(!showingMap)
            return;
		if (!BocaiApplication.isHttpAvailable(BrowseActivity.this)) {
			return ;
		}
        showLoadingView(null);
        sightings.clear();
        mapController.clearSightings();
        currentPage = 1;
        totalPages = 0;
        if(currentSearch != null && currentSearch.length() > 0)
        {
            cancelAllRequests();
            if(search != null)
                search = null;
            search = new Search();
          
            search.delegate = this;
            
            makePrimaryRequestObject(search);
            StringBuilder stringbuilder = (new StringBuilder()).append("loadSightingsAtCoordinate: Calling search.doSightingsSearch(");
            String s = currentSearch;
            String s1 = stringbuilder.append(s).append(",").append(location).append(",").append(d).append(")").toString();
            Log.d(LOG_TAG, s1);
      
            search.doSightingSearch(currentSearch, location, d,null);
            return;
        } else
        {
            currentPage = i;
            showLoadingView(null);
            Log.d(LOG_TAG, "loadSightingsAtCoordinate: Calling downloadSightings");
            
            String s3 = Filter.filterSortString();

            this.downloadSightings(10, currentPage, s3, location, d);
            Macros.FS_APPLICATION().reverseGeocode(location, this);
            return;
        }
    }

    public void locationChanged(Location location)
    {
    	
    	locationChanged(location, true);
    }

    public void locationChanged(Location location, boolean flag)
    {
    	Log.i(LOG_TAG, "locationChanged method");
    	Log.i(LOG_TAG, "location.Latitude" + location.getLatitude());
    	Log.i(LOG_TAG, "location.Longitude" + location.getLongitude());
    	cancelTimer();
    	String s = (new StringBuilder()).append("locationChanged(").append(location).append(",").append(flag).append(")").toString();
        Log.d(LOG_TAG, s);
        BocaiApplication bocaiApplication = Macros.FS_APPLICATION();
        bocaiApplication.unregisterForLocationUpdates();
        bocaiApplication.unregisterForLocationUpdatesUsingBaidu();
        if(flag && !isCurrentActivity())
        {
            Log.d(LOG_TAG, "\tNOT ON TOP OF STACK! Deferring location change");
            pendingLocationChange = location;
            return;
        } else
        {
			if (!BocaiApplication.isHttpAvailable(BrowseActivity.this)) {
				return ;
			}
            showLoadingView(null);
            mapController.setZoomOutInclude(true);
            mapController.setShowScanBestButton(true);
            mapController.setCenter(location);
            handleRefresh();
            bocaiApplication.reverseGeocode(location, this);
            return;
        }
    }

    void makePrimaryRequestObject(FSObject fsobject)
    {
    	Log.i(LOG_TAG, "makePrimaryRequestObject method");
    	cancelAllRequests();
        addRequestObject(fsobject);
    }

    protected void onActivityResult(int i, int j, Intent intent)
    {
        switch(i)
        {
        default:
            return;

        case 1: // '\001'
            break;
        case 2:
        	//Log.i(LOG_TAG, "aaaaaaaaaaaaaaaaaaaaaaaa");
        	break;
        }
        if(android.provider.Settings.Secure.getString(getContentResolver(), "location_providers_allowed") == null)
        {
            return;
        } else
        {
            shouldRequestLocation = true;
            return;
        }
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        Log.i(LOG_TAG,"onCreate");
        setContentView(R.layout.browse);
        
        requestObjects = new LinkedList<FSObject>();
        
        currentFilterSort = Filter.filterSort();
       
        sightings = new LinkedList<Sighting>();
        
        sightingViewAdapter = new SightingViewAdapter(this, R.layout.sighting_view, sightings);
        
        sightingViewAdapter.setUIHandler(handler);
        streamView = (StreamView)findViewById(R.id.streamView);
        sightingViewAdapter.setAdapterView(streamView);
        pageNext = getLayoutInflater().inflate(R.layout.page_next, null);
        pageNext.setVisibility(View.GONE);

        streamView.addFooterView(pageNext, null, false);
        streamView.setAdapter(sightingViewAdapter);
        
        android.widget.AdapterView.OnItemClickListener onitemclicklistener = new android.widget.AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

            	if(position < 0)
                    return;
                
                if(position >= sightings.size())
                    return;
                Sighting sighting1 = (Sighting)sightings.get(position);
                Intent intent;
                android.app.Activity activity;
                if(sighting1 instanceof Promo)
                {
                    BrowseActivity browseactivity = BrowseActivity.this;
                    intent = new Intent(browseactivity, com.bocai.WebViewActivity.class);
                    String s = ((Promo)sighting1).url;
                    intent.putExtra("url", s);
                } else
                {
                    intent = new Intent(BrowseActivity.this, com.bocai.ReviewActivity.class);
                    intent.putExtra("sighting", sighting1);
                    String s1 = sighting1.sightingID;
                    intent.putExtra("sighting_id", s1);
                }
                activity = getParent();
                if(!(activity instanceof TabStackActivityGroup))
                {
                    return;
                } else
                {
                    ((TabStackActivityGroup)activity).pushIntent(intent);
                    return;
                }
            }
        };
        
		streamView.setOnItemClickListener(onitemclicklistener);
        
        android.view.ViewGroup.OnHierarchyChangeListener onhierarchychangelistener = new android.view.ViewGroup.OnHierarchyChangeListener() {

            public void onChildViewAdded(View parent, View child)
            {
               
            	 Log.i("OnHierarchyChangeListener","onChildViewAdded method"); 
                if(child != pageNext)
                    return;
                
                Log.i("OnHierarchyChangeListener", "currentPage=======" + currentPage);
                Log.i("OnHierarchyChangeListener", "totalPages========" + totalPages);
      
                if(currentPage < totalPages)
                {
                    pageNext.setVisibility(View.VISIBLE);
                    pageNext.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                                       
                    handler.postDelayed(startNextPageLoad, 2500L);
                    return;
                } else
                {
                    pageNext.setVisibility(View.GONE);
                    pageNext.findViewById(R.id.progress).setVisibility(8);
                    return;
                }
            }

            public void onChildViewRemoved(View parent, View child)
            {
               
            	 Log.i("BR-StreamView","onChildViewRemoved==="); 
                if(child != pageNext)
                {
                    return;
                } else
                {
                    pageNext.setVisibility(View.GONE);
                    pageNext.findViewById(R.id.progress).setVisibility(View.GONE);
                    handler.removeCallbacks(startNextPageLoad);
                    return;
                }
            }
        };
        
		streamView.setOnHierarchyChangeListener(onhierarchychangelistener);
		noResultsView = findViewById(0x1020004);
      
		refreshButton = (ImageButton)findViewById(R.id.btn_refresh);
        refreshButton.setOnClickListener(refreshButtonListener);
        streamMapGroup = (ViewFlipper)findViewById(R.id.stream_map_group);
        
        streamMapGroup.setDisplayedChild(1);
        filterModes = (RadioGroup)findViewById(R.id.filter_modes);
        
        
        android.widget.RadioGroup.OnCheckedChangeListener oncheckedchangelistener = new android.widget.RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup radiogroup2, int j)
            {
                if(ignoreFilterChange)
                    return;
                int k = BUTTON_ID_TO_FILTER(j);
                if(k == -1)
                {
                    return;
                } else
                {
                    Filter.setFilterSort(k);
                    currentFilterSort = k;
                    refreshFromFilter();
                    return;
                }
            }
        };
        
		filterModes.setOnCheckedChangeListener(oncheckedchangelistener);
		searchLabel = (TextView)findViewById(R.id.label_search);
        
		locationLabel = (TextView)findViewById(R.id.label_location);
        
		mapView = (MapView)findViewById(R.id.mapview);
        
		mapController = new MapController(mapView);
        
        mapController._flddelegate = this;
        Button button = (Button)findViewById(R.id.btn_scan);
        android.view.View.OnClickListener onclicklistener1 = new android.view.View.OnClickListener() {

            public void onClick(View view3)
            {
                mapController.refreshFromResize(false);
            }
        };
        
        button.setOnClickListener(onclicklistener1);
        
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        initSearchChrome();
    }

    protected void onDestroy()
    {
        Log.d(LOG_TAG, "onDestroy");
        BocaiApplication bocaiApplication = Macros.FS_APPLICATION();
        bocaiApplication.removeStateChangeListener(this);
        bocaiApplication.cancelLocationUpdate(this);
        super.onDestroy();
    }

    public boolean onKeyDown(int i, KeyEvent keyevent)
    {
    	if (i == 4) {
    		if (!showingMap) {
    			if(searchBar.getVisibility() != 0) {
    				return false;
    			} else {
    				hideSearchBar();
    				return true;
    			}
    		} else {
    			if(toolbarItems == null) {
    				return false;
    			} else {
    		        View aview[];
    		        int j;
    		        int k;
    		        aview = toolbarItems;
    		        j = aview.length;
    		        k = 0;
    		        while(k < j) {
    		        	View view = aview[k];
    		        	if(view.getId() == R.id.button_map) {
    		                ImageButton imagebutton = (ImageButton)view;
    		                toggleView(imagebutton);
    		                return true;
    		        	}
    		        	k++;
    		        }
    		        return false;
    			}
    		}
    	}
    	
    	 if(i == 84)
         {
             showSearchBar();
             return true;
         } else
         if(i == 25)
         {
             StringBuilder stringbuilder = new StringBuilder();
             stringbuilder.append('[');
             for(Iterator<Sighting> iterator = sightings.iterator(); iterator.hasNext();)
             {
                 String s = iterator.next().sightingID;
                 stringbuilder.append(s).append(',');
             }

             stringbuilder.append(']');
             String s1 = (new StringBuilder()).append("Sightings: ").append(stringbuilder).toString();
             Log.d(LOG_TAG, s1);
             return true;
         } 
         
         return super.onKeyDown(i, keyevent);
    }

    protected void onPause()
    {
        Log.i(LOG_TAG, "onPause");
        mapController.disableMyLocation();
        cancelAllRequests();
        if(locationSettingsDialog != null && locationSettingsDialog.isShowing())
            locationSettingsDialog.dismiss();
        if(loadingView != null && loadingView.isShowing())
            loadingView.dismiss();
        super.onPause();
    }

    protected void onResume()
    {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        mapController.enableMyLocation();
        if(pendingLocationChange != null)
        {
            Log.i(LOG_TAG, "\tHandling pending location change!");
            
            locationChanged(pendingLocationChange, false);
            pendingLocationChange = null;
        }
        if(firstLaunch)
        {
            Macros.FS_APPLICATION().addStateChangeListener(this);
            firstLaunch = false;
        }
        if(!shouldRequestLocation)
            return;
        //String s = getString(R.string.browse_locating);
       
		if (!BocaiApplication.isHttpAvailable(BrowseActivity.this)) {
			return ;
		}
		if (!hasCheckedGPS) {
			checkGPS();
		} else {
			showLoadingView();
		}
		/*        Log.i(LOG_TAG, "start showLoadingView...");
        showLoadingView(s);
       
        if(!Macros.FS_APPLICATION().requestLocationUpdate(this, 5000L))
        {
            hideLoadingView();
            locationSettingsDialog().show();
        } */
        
        shouldRequestLocation = false;
        Log.i(LOG_TAG, "onResume End");
    }
    
	private void showLoadingView() {
		Log.i(LOG_TAG, "start showLoadingView...");
		showLoadingView(getString(R.string.browse_locating));

		if (!Macros.FS_APPLICATION().requestLocationUpdate(this, 5000L)) {
			hideLoadingView();
			locationSettingsDialog().show();
		}

		shouldRequestLocation = false;
	}

    public void onStateChange(int i)
    {
        if(i == 1)
        {
            if(shouldRequestLocation)
                return;
            if(isCurrentActivity())
            {
                return;
            } else
            {
                Macros.FS_APPLICATION().requestLocationUpdate(this, 5000L);
                return;
            }
        }
        if(i != 2)
        {
            return;
        } else
        {
            Macros.FS_APPLICATION().cancelLocationUpdate(this);
            return;
        }
    }

    void populateSightingsView(List<Sighting> list)
    {
        Log.i(LOG_TAG, "populateSightingsView method");
    	
    	mapController.plotSightings(list);
        hideLoadingView();
   
        handler.post(hideNextPage);
        sightings.addAll(list);

       
        if(currentSearch == null || currentSearch.length() < 1)
        {
        	searchResultsCaption = Filter.sortCaptionString();
            
        	handler.post(updateSearchResultsCaption);
        } else
        {
        	handler.post(run_hideSearchBar);
        }
        handler.post(refreshStreamView);
    }

    void refreshFromFilter()  
    {
		if (!BocaiApplication.isHttpAvailable(BrowseActivity.this)) {
			return ;
		}
        showLoadingView(null);

        if(currentFilterSort != Filter.filterSort())
        {
            
            int k = Filter.filterSort();
            int l = FILTER_TO_BUTTON_ID(k);
            filterModes.check(l);
        }
        if(!Filter.sortNearest())
            Filter.setWithinMap();
        sightings.clear();
        mapController.clearSightings();
        sightingViewAdapter.notifyDataSetChanged();
        currentPage = 1;
        totalPages = 0;
        requestCurrentPageResults();
    }

    void requestCurrentPageResults()
    {
        if(Filter.sortNearest())
        {
            Filter.setAnywhere();
            mapController.setZoomOutInclude(true);
            mapController.setShowScanBestButton(true);
        } else
        {
            mapController.setZoomOutInclude(false);
            mapController.setShowScanButton(true);
        }
        if(searchingWithPlaceId && currentSearch != null)
            return;
        if(currentSearch != null && currentSearch.length() > 0)
        {
            cancelAllRequests();
            search = new Search();
            
            search.delegate = this;
            
            makePrimaryRequestObject(search);
            if(Filter.areaIsWithinMap())
            {
                Location location = new Location("explicit");
                double d = (double)mapView.getMapCenter().getLatitudeE6() / 1000000D;
                location.setLatitude(d);
                double d1 = (double)mapView.getMapCenter().getLongitudeE6() / 1000000D;
                location.setLongitude(d1);
               
                
                double d2 = mapController.latitudeDeltaInKms();
                
                search.doSightingSearch(currentSearch, location, d2, currentPage,null);
                return;
            } else
            {
           
                Location location1 = Macros.FS_CURRENT_LOCATION();
                
          //      search.doSightingSearch(currentSearch, location1, 50000D, currentPage);
                //NOTE: adjust the within param,5km around
                search.doSightingSearch(currentSearch, location1, 5D, currentPage,null);  //NOTE: not correct for place search
                return;
            }
        }
        searchResultsCaption = Filter.sortCaptionString();
        
     
         handler.post(updateSearchResultsCaption);
        if(Filter.areaIsWithinMap())
        {
            Location location2 = new Location("explicit");
            double d3 = (double)mapView.getMapCenter().getLatitudeE6() / 1000000D;
            location2.setLatitude(d3);
            double d4 = (double)mapView.getMapCenter().getLongitudeE6() / 1000000D;
            location2.setLongitude(d4);
            
            String s3 = Filter.filterSortString();
            double d5 = mapController.latitudeDeltaInKms();
           
            
            this.downloadSightings(10, currentPage, s3, location2, d5);
            return;
        } else
        {
            
            String s4 = Filter.filterSortString();
            Location location4 = Macros.FS_CURRENT_LOCATION();
            //NOTE:change the within param to 5,indicate 5km around
            this.downloadSightings(10, currentPage, s4, location4, 5D);
            //this.downloadSightings(10, currentPage, s4, location4, 50000D);
            return;
        }
    }

    void showKeyboard(View view)
    {
        view.setFocusable(true);
        view.requestFocus();
        ((InputMethodManager)getSystemService("input_method")).showSoftInput(view, 1);
    }

    void showLoadingView(String s)
    {

        if(s != null)
        {
            loadingMessage = s;
        } else
        {
        	loadingMessage = getString(R.string.browse_loading);
            
        }
   
        if (loadingMessage.equals(getString(R.string.browse_locating))) {
        	setLocateTimer();
        }
        
         handler.post(run_showLoadingView);
    }

    void showSearchBar()
    {
        if(searchBar.getVisibility() == View.VISIBLE)
        {
            return;
        } else
        {
            searchBar.setVisibility(View.VISIBLE);

            searchBar.startAnimation(pushDownIn);
            return;
        }
    }

    void showSearchChrome()
    {
        dismissButton.setVisibility(0);
        searchChromeDisplayed = true;
    }

    public void updatedSortFilter()
    {
        ignoreFilterChange = true;
  
        if(currentFilterSort != Filter.filterSort())
        {
            
            int k = Filter.filterSort();
            int l = FILTER_TO_BUTTON_ID(k);
            filterModes.check(l);
        }
        ignoreFilterChange = false;
        if(Filter.sortNearest())
        {
            return;
        } else
        {
            Filter.setWithinMap();
            return;
        }
    }
    
	public void checkGPS() {

		LocationManager locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			
	    	android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
	    	//builder.setTitle(getString(R.string.exit_msg));
	    	builder.setMessage(getString(R.string.open_gps_confirm));
	    	builder.setPositiveButton(getString(R.string.ok), new OnClickListener(){
	    		@Override
	    		public void onClick(DialogInterface dialog, int which) {
	    			Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
	    			startActivityForResult(intent,2);
	    		}
	    	} );
			builder.setNegativeButton(getString(R.string.cancel_btn), new OnClickListener(){
	    		@Override
	    		public void onClick(DialogInterface dialog, int which) {
	    			showLoadingView();
	    		}
	    	});
	    	builder.create().show();
	        //handler.post(run_showGPSDialog);

		} else {
			showLoadingView();
		}
		hasCheckedGPS = true;

	}
	
	private void setLocateTimer() {
		Log.i(LOG_TAG, "-----------setLocateTimer---------");
		timer = new CountDownTimer(10000, 1000) {
			public void onTick(long millisUntilFinished) {
				Log.i(LOG_TAG, "-----------setLocateTimer----onTick-----");
			}

			public void onFinish() {
				Log.i(LOG_TAG, "-----------setLocateTimer---onFinish------"
						+ locationLabel.getText());

				hideLoadingView();
				BocaiApplication bocaiApplication = Macros.FS_APPLICATION();
				bocaiApplication.unregisterForLocationUpdates();
				bocaiApplication.unregisterForLocationUpdatesUsingBaidu();
				Toast.makeText(BrowseActivity.this,R.string.locate_me_fail, Toast.LENGTH_LONG)
						.show();

			}
		};
		timer.start();
	}
	private void cancelTimer() {
		if (timer != null) {
			timer.cancel();
		}
	}

 // private static final boolean DEBUG = false;
    static final int LOCATION_SETTINGS = 1;
 // private static final int LOCATION_UPDATE_TIMEOUT = 5000;
    private static final String LOG_TAG = "BrowseActivity";
    android.view.View.OnClickListener clearSearchListener;
    int currentFilterSort;
    int currentPage;
    String currentPlaceId;
    String currentSearch;
    ImageButton dismissButton;
    RadioGroup filterModes;
    boolean firstLaunch;
    final Handler handler;
    final Runnable hideNextPage;
    Runnable hideNoResultsView;
    boolean ignoreFilterChange;
    boolean justTappedLocate;
    String loadingMessage;
    ProgressDialog loadingView;
    TextView locationLabel;
    AlertDialog locationSettingsDialog;
    MapController mapController;
    MapView mapView;
    View noResultsView;
    View pageNext;
    Location pendingLocationChange;
    Animation pushDownIn;
    Animation pushUpOut;
    ImageButton refreshButton;
    android.view.View.OnClickListener refreshButtonListener;
    final Runnable refreshStreamView;
    List<FSObject> requestObjects;
    final Runnable run_hideLoadingView;
    final Runnable run_hideSearchBar;
    final Runnable run_showLoadingView;
    final Runnable run_showGPSDialog;
    Search search;
    ViewGroup searchBar;
    private boolean searchChromeDisplayed;
    EditText searchField;
    TextView searchLabel;
    android.widget.TextView.OnEditorActionListener searchListener;
    String searchResultsCaption;
    private boolean searchingWithPlaceId;
    boolean shouldRequestLocation;
    Runnable showNoResultsView;
    boolean showingMap;
    Sighting sighting;
    SightingViewAdapter sightingViewAdapter;
    LinkedList<Sighting> sightings;
    
    LinkedList<CharSequence> hints;
    ArrayAdapter<CharSequence> hintViewAdapter;
    ListView hintListView;
    android.widget.AdapterView.OnItemClickListener hintClickListener;
    final Runnable run_showHintListView;
    final Runnable run_hideHintListView;
    CharSequence search_place;
    CharSequence search_dish;
    
    Animation slideLeftIn;
    Animation slideLeftOut;
    Animation slideRightIn;
    Animation slideRightOut;
    final Runnable startNextPageLoad;
    ViewFlipper streamMapGroup;
    StreamView streamView;
    LinkedList<Sighting> tmpSightings;
    View toolbarItems[];
    int totalPages;
    final Runnable updateSearchResultsCaption;
    boolean hasCheckedGPS = false;
    CountDownTimer timer ;
}
