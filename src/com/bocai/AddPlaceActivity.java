
package com.bocai;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import com.bocai.map.DraggableOverlay;
import com.bocai.model.GoogleAddress;
import com.bocai.model.Place;
import com.bocai.util.Macros;
import com.google.android.maps.*;
import java.util.List;

public class AddPlaceActivity extends MapActivity implements HomeActivity.ToolbarItemSource
{

    public AddPlaceActivity()
    {
    	handler = new Handler();
    	addPlaceCallback = new android.view.View.OnClickListener() {
            public void onClick(View view)
            {
            	 Intent intent = new Intent();
                 Place place = collectFormData();
                 intent.putExtra("place", place);
                 ((HomeActivity)getParent()).popNavigationStack(-1, intent);
            }

        };

		skipCallback = new android.view.View.OnClickListener() {
            public void onClick(View view)
            {                     
            	   Place place = new Place();
            	   place.name = placeName.getText().toString();
                   Intent intent = new Intent();
                   intent.putExtra("place", place);
                   ((HomeActivity)getParent()).popNavigationStack(-1, intent);
            }
        };

    }

    Place collectFormData()
    {
        Place place = new Place();
        place.name = placeName.getText().toString();
        place.secondName = secondPlaceName.getText().toString();
       
        StringBuilder stringBuilder = new StringBuilder();
        if(address1.length() > 0)
        {
        	place.address = address1.getText().toString();
            stringBuilder.append(place.address);
        }
        if(city.length() > 0)
        {
        	place.city = city.getText().toString();
            stringBuilder.append(',');
            stringBuilder.append(place.city);
        }
        if(state.length() > 0)
        {
        	place.state = state.getText().toString();
            stringBuilder.append(',');
            stringBuilder.append(place.state);
        }
        place.fullAddress = stringBuilder.toString();
        if(mapOverlay.wasDragged)
        {
            GeoPoint geoPoint = mapOverlay.getItem(0).getPoint();
            place.latitude = (double)geoPoint.getLatitudeE6() / 1000000D;
            place.longitude = (double)geoPoint.getLongitudeE6() / 1000000D;
        }
        return place;
    }
 
    protected boolean isRouteDisplayed()
    {
        return false;
    }

    public void onConfigurationChanged(Configuration configuration)
    {
         mapOverlay.centerOnDraggable(map, true);
         super.onConfigurationChanged(configuration);
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.add_place);
        placeName = (EditText)findViewById(R.id.edit_name);
        secondPlaceName = (EditText)findViewById(R.id.edit_secondName);
        address1 = (EditText)findViewById(R.id.edit_address1);
        city = (EditText)findViewById(R.id.edit_city);
        state = (EditText)findViewById(R.id.edit_state);
        map = (MapView)findViewById(R.id.mapview);
        skipButton = (Button)findViewById(R.id.btn_skip);
        addButton = (Button)findViewById(R.id.btn_add);
        addButton.setOnClickListener(addPlaceCallback);
        skipButton.setOnClickListener(skipCallback);
        map.setBuiltInZoomControls(true);
        draggablePin = new ImageView(this);
        android.graphics.drawable.Drawable drawable = getResources().getDrawable(R.drawable.map_pin);
        draggablePin.setImageDrawable(drawable);
        byte byte0 = -1;
        int i = 0;
        com.google.android.maps.MapView.LayoutParams layoutParams = new com.google.android.maps.MapView.LayoutParams(-1, byte0, 0, i, 81);
        layoutParams.mode = 1;
        draggablePin.setLayoutParams(layoutParams);
        map.addView(draggablePin);
        
        android.graphics.drawable.Drawable drawable1 = getResources().getDrawable(R.drawable.map_pin);
        mapOverlay = new DraggableOverlay(drawable1, draggablePin);
        List<Overlay> list = map.getOverlays();
        list.add(mapOverlay);
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
		  return false;
		}
    	return super.onKeyDown(keyCode, event);
	}
    @Override
	public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            String s = bundle.getString("placeName");
            if(s != null && s.length() > 0)
                placeName.setText(s);
        }
        Location location = Macros.FS_APPLICATION().currentLocation;
        GeoPoint geopoint = null;
        if(location != null)
        {
            int i = (int)(location.getLatitude() * 1000000D);
            int j = (int)(location.getLongitude() * 1000000D);
            geopoint = new GeoPoint(i, j);
            map.getController().animateTo(geopoint);
        }
        
        map.getController().zoomToSpan(3200, 3200);
        mapOverlay.clear();
        if(geopoint == null)
            geopoint = map.getMapCenter();
        mapOverlay.add(geopoint, null, null);
        
        GoogleAddress address = Macros.FS_APPLICATION().currentAddress;
        if(address != null){
        	address1.setText(address.getStreet());
        	city.setText(address.getCity());
        	state.setText(address.getState());
        }
    }
    
	@Override
	public View[] getToolbarItems() {
		return null;
	}

    public static final String PLACE = "place";
    public static final String PLACE_NAME = "placeName";
    Button addButton;
    android.view.View.OnClickListener addPlaceCallback;
    EditText address1;
    OverlayItem addressMarker;
    EditText city;
    ImageView draggablePin;
    final Handler handler;
    MapView map;
    private DraggableOverlay mapOverlay;
    MyLocationOverlay myLocation;
    EditText placeName;
    EditText secondPlaceName;
    ProgressDialog progressDialog;
    Button skipButton;
    android.view.View.OnClickListener skipCallback;
    EditText state;

}
