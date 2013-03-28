package com.bocai;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.bocai.model.FSObject;
import com.bocai.model.FSObjectDelegate;
import com.bocai.model.Place;
import com.bocai.model.Sighting;
import com.bocai.util.Macros;
import com.bocai.widget.GroupedTableView;
import com.bocai.widget.MyAdapter;
import com.google.android.maps.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceDetailActivity extends MapActivity implements FSObjectDelegate, HomeActivity.ToolbarItemSource
  
{
    class MapOverlay extends ItemizedOverlay<OverlayItem>
    {

        void addOverlay(OverlayItem overlayitem)
        {
            overlays.add(overlayitem);
            populate();
        }

        protected OverlayItem createItem(int i)
        {
            return (OverlayItem)overlays.get(i);
        }

        void refresh()
        {
            populate();
        }

        public int size()
        {
            return overlays.size();
        }

        ArrayList<OverlayItem> overlays;
    
        public MapOverlay(Drawable drawable)
        {
        	super(drawable);
            boundCenterBottom(drawable);
            overlays = new ArrayList<OverlayItem>();
        }
    }
    
    private void listSightings(){
    	place.delegate = this;
    	place.listSighting(0, 10);
    }
    
    public PlaceDetailActivity()
    {
    	handler = new Handler();
    	run_refreshSightings = new Runnable(){

			@Override
			public void run() {
				if(sightingList.size() > 0){
					 moreSightingLable.setVisibility(View.VISIBLE);
					 sightingTable.setVisibility(View.VISIBLE);
				}else{
					moreSightingLable.setVisibility(View.GONE); 
					sightingTable.setVisibility(View.GONE);
				}
				sightingAdapter.notifyDataSetChanged();
			}
    	};
    	
    	run_listSightings = new Runnable(){

			@Override
			public void run() {
				listSightings();	
			}
    	};
    	
    	
    	showSighing = new com.bocai.widget.GroupedTableView.OnItemClickListener(){

			@Override
			public void onItemClick(GroupedTableView groupedTableView,
					View view, int i) {
				view.setClickable(false);
				 final View clickedView = view;
				 Runnable runnable4 = new Runnable() {

	                 public void run(){
	                    	clickedView.setClickable(true);
	                    }
	              };
	              
	              view.postDelayed(runnable4, 250L);
	              PlaceDetailActivity placeDetailActivity = PlaceDetailActivity.this;
	              Intent intent = new Intent(placeDetailActivity,com.bocai.ReviewActivity.class);
	              intent.putExtra("init_mode", 1);
	              Sighting sighting = (Sighting)sightingList.get(i);
	              intent.putExtra("sighting", sighting);
	              Activity activity = getParent();
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
    	
    }

    public void FSResponse(List<FSObject> list)
    {
     
     Log.i(LOG_TAG, "FSResponse method=" + list);
         if(list == null)
         {
             Log.i(LOG_TAG, "NO RESPONSE returned");
             return;
         }
         if(list != null)
         {
        	 sightingList.clear();
             Iterator iterator = list.iterator();
             do
             {
                 if(!iterator.hasNext())
                     break;
                 FSObject fsobject = (FSObject)iterator.next();
                 if(fsobject instanceof Sighting)
                 {
                	 Sighting sighting = (Sighting)fsobject;
                	 sightingList.add(sighting);
                 }
             } while(true);
         }
        
         Log.i(LOG_TAG, "done reloading from fsresponse");
    	 handler.post(run_refreshSightings);
    	
    }

    public void displayErrors(JSONObject jsonobject)
        throws JSONException
    {
    	Log.e(PLACE, "displayErrors method=" + jsonobject);
    }

    public void displaySuccess(JSONObject jsonobject)
        throws JSONException
    {
    	//blank
    }

    public void doSearchWithName(String s)
    {
    	//blank
    }

    void initWithPlace(Place paramPlace)
    {
      this.place = paramPlace;
      MapView localMapView = (MapView)findViewById(R.id.mapview);
      Drawable localDrawable = getResources().getDrawable(R.drawable.map_pin);
      mapOverlay = new MapOverlay(localDrawable);
      List<Overlay> localList = localMapView.getOverlays();
      localList.clear();
      localList.add(mapOverlay);
      int i = (int)(place.latitude * 1000000.0D);
      int j = (int)(place.longitude * 1000000.0D);
      GeoPoint geoPoint = new GeoPoint(i, j);
      addressMarker = new OverlayItem(geoPoint, null, null);
      mapOverlay.addOverlay(addressMarker);
      localMapView.getController().animateTo(geoPoint);
      TextView localTextView = (TextView)findViewById(R.id.label_name);
      localTextView.setText(place.name);
      reloadMetadataTable();
      handler.post(run_listSightings);
      
    }
    
    public void finishedAction(JSONObject jsonobject)
        throws JSONException
    {
        String s = (new StringBuilder()).append("FSResponse: finishedAction: ").append(jsonobject).toString();
        Log.d("PlaceDetail", s);

    }

 
    protected boolean isRouteDisplayed()
    {
        return false;
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);    
        setContentView(R.layout.place_detail);
        ((MapView)findViewById(R.id.mapview)).getController().zoomToSpan(3200, 3200);
        sightingTable = (GroupedTableView)findViewById(R.id.more_sightings_table);
        moreSightingLable = (TextView)findViewById(R.id.label_more_sightings);
        sightingList = new LinkedList();
        sightingAdapter = new MyAdapter(this,R.layout.place_sighting_list_item,sightingList);
        sightingTable.setAdapter(sightingAdapter);
        sightingTable.setOnItemClickListener(showSighing);
   
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean flag;
        if(keyCode == KeyEvent.KEYCODE_BACK){
            flag = false;
        }
        else{
            flag = super.onKeyDown(keyCode,event);
        }
        return flag;
    }

    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected void onResume()
    {
        super.onResume();
        Bundle bundle = getIntent().getExtras();
        if(bundle == null)
            return;
        Place localPlace = (Place)bundle.getParcelable("place");
        if (localPlace == null)
          return;
        initWithPlace(localPlace);

    }

    void reloadMetadataTable()
    {
    	if (place == null)
    	      return;
    	    GroupedTableView localGroupedTableView = (GroupedTableView)findViewById(R.id.metadata_table);
    	    localGroupedTableView.removeAllViews();
    	    LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(-1, -1);
    	    localLayoutParams.setMargins(4, 2, 2, 4);
    	    LayoutInflater localLayoutInflater = LayoutInflater.from(this);
    	    if ((this.place.phone != null) && (this.place.phone.length() > 0))
    	    {
    	      RelativeLayout localRelativeLayout1 = (RelativeLayout)localLayoutInflater.inflate(R.layout.place_list_item, null);
    	      localRelativeLayout1.setClickable(true);
    	      TextView localTextView1 = (TextView)localRelativeLayout1.findViewById(R.id.title);
    	      ImageView localImageView1 = (ImageView)localRelativeLayout1.findViewById(R.id.image);
    	      localGroupedTableView.addView(localRelativeLayout1, localLayoutParams);
    	      localImageView1.setImageResource(R.drawable.icon_phone);
    	      localImageView1.setBackgroundDrawable(null);
    	      String str1 = this.place.phone;
    	      localTextView1.setText(str1);
    	      View.OnClickListener listener = new View.OnClickListener()
    	      {
				@Override
				public void onClick(View v) {
					 Uri localUri = Uri.parse("tel:" + place.phone);
					 Intent localIntent = new Intent(Intent.ACTION_CALL, localUri);
					 startActivity(localIntent);
				}
    	      };
    	      localRelativeLayout1.setOnClickListener(listener);
    	    }
    	    RelativeLayout localRelativeLayout2 = (RelativeLayout)localLayoutInflater.inflate(R.layout.place_list_item, null);
    	    localRelativeLayout2.setClickable(true);
    	    TextView localTextView2 = (TextView)localRelativeLayout2.findViewById(R.id.title);
    	    ImageView localImageView2 = (ImageView)localRelativeLayout2.findViewById(R.id.image);
    	    localGroupedTableView.addView(localRelativeLayout2, localLayoutParams);
    	    localImageView2.setImageResource(R.drawable.ic_directions_small);
    	    localImageView2.setBackgroundDrawable(null);
    	    String str2 = null;
    	    if ((this.place.fullAddress != null) && (this.place.fullAddress.length() > 0))
    	    {
    	      String str3 = this.place.fullAddress;
    	      localTextView2.setText(str3);
    	      str2 = this.place.fullAddress;
    	    }
    	    
    	    if(place.address != null && place.address.length() > 0){
    	    	localTextView2.setText(place.address);
    	    	Object[] arrayOfObject = new Object[3];
    	    	arrayOfObject[0] = place.address;
    	    	arrayOfObject[1] = place.city;
    	    	arrayOfObject[2] = place.state;
    	    	 str2 = String.format("%s %s, %s", arrayOfObject);
    	    }else{
    	    	localTextView2.setText("Unknown Address");
    	    }
    	    
    	    if(str2 != null){
    	    	final String addr = str2;
    	    	View.OnClickListener listener2 = new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						Location location = Macros.FS_APPLICATION().currentLocation;
						if(location == null){
							location = Macros.FS_APPLICATION().lastKnownLocation;
						}
						if(location != null){
							double d1 = location.getLatitude();
							double d2 = location.getLongitude();
							StringBuilder localStringBuilder = new StringBuilder().append("http://maps.google.com/maps?saddr=").append(d1).append(",").append(d2).append("&daddr=");
	    	                String str3;
							try {
								str3 = URLEncoder.encode(addr, "UTF-8");
								  localStringBuilder.append(str3);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
	    	                Uri localUri = Uri.parse(localStringBuilder.toString());
	    	                Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
	    	                startActivity(localIntent);
						}
					}
    	    	};
    	    	localRelativeLayout2.setOnClickListener(listener2);
    	    }
    	    
    	    if(place.link != null && place.link_title != null){
    	    	RelativeLayout localRelativeLayout3 = (RelativeLayout)localLayoutInflater.inflate(R.layout.place_list_item, null);
     	        localRelativeLayout3.setClickable(true);
     	        TextView localTextView3 = (TextView)localRelativeLayout3.findViewById(R.id.title);
     	        ImageView localImageView3 = (ImageView)localRelativeLayout3.findViewById(R.id.image);
     	        localGroupedTableView.addView(localRelativeLayout3, localLayoutParams);
     	        localImageView3.setImageResource(R.drawable.ic_link_small);
     	        localImageView3.setBackgroundDrawable(null);
     	        localTextView3.setText(place.link_title);
     	        
     	       View.OnClickListener listener3 = new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						  Uri localUri = Uri.parse(place.link);
		    	          Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
		    	          startActivity(localIntent);
					}
     	       };
     	      localRelativeLayout3.setOnClickListener(listener3);
    	    }
    }
    
    @Override
	public View[] getToolbarItems() {
		return null;
	}

    private static final String LOG_TAG = "PlaceDetail";
    public static final String PLACE = "place";
    private OverlayItem addressMarker;
    final Handler handler;
    private MapOverlay mapOverlay;
    private Place place;
    private ProgressDialog progressDialog;
    private GroupedTableView sightingTable;
    final Runnable run_refreshSightings;
    final Runnable run_listSightings;
    MyAdapter sightingAdapter;
    com.bocai.widget.GroupedTableView.OnItemClickListener showSighing;
    List sightingList;
    TextView moreSightingLable;
	
}
