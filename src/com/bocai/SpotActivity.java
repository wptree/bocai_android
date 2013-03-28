package com.bocai;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.bocai.model.FSObject;
import com.bocai.model.FSObjectDelegate;
import com.bocai.model.Item;
import com.bocai.model.Place;
import com.bocai.model.Review;
import com.bocai.model.Search;
import com.bocai.model.User;
import com.bocai.util.FlurryEvents;
import com.bocai.util.ImageUtilities;
import com.bocai.util.Macros;
import com.bocai.widget.SearchResultsAdapter;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.json.*;

public class SpotActivity extends Activity
    implements FSObjectDelegate, 
    TabStackActivityGroup.ActivityResultListener, 
    HomeActivity.ToolbarItemSource, 
    HomeActivity.ActivityTitleSource, 
    SpotNearbyPlaces.SpotNearbyPlacesListener, 
    SpotPlaceItems.SpotPlaceItemsListener, 
    BocaiApplication.StateChangeListener
{
    class ProcessImageTask extends AsyncTask<Uri, Void, Bitmap>
    {  
    	
    	@Override
        protected Bitmap doInBackground(Uri auri[])
        {
            System.gc();
            Uri uri = auri[0];
            SpotActivity spotactivity = SpotActivity.this;
            File file = tmpFile;
            boolean flag = ImageUtilities.scaleImage(uri, file, 800, 800);
            spotactivity.haveImage = flag;
            Bitmap bitmap;
            if(!haveImage)
            {
                Log.e("Spot", "ProcessImageTask.doInBackground(): Scaling image failed!");
                bitmap = null;
            } else
            {
                Uri uri1 = Uri.fromFile(tmpFile);
                ImageView imageview = imageView;
                bitmap = ImageUtilities.scaleImageForImageView(uri1, imageview);
            }
            return bitmap;
        }
        
    	@Override
		protected void onPostExecute(Bitmap bitmap) {
          if(progress != null){
        	  progress.dismiss();
          }
          progress = null;
          if(bitmap == null)
          {
        	  SpotActivity spotactivity = SpotActivity.this;
        	  (new android.app.AlertDialog.Builder(spotactivity))
        	  .setTitle(R.string.photo_fail_title)
        	  .setMessage(R.string.photo_fail)
        	  .setPositiveButton(R.string.try_again, null)
        	  .show();
        	  return;
          }
          validate();
          imageView.setImageBitmap(bitmap);
          if(haveImage){
        	  nextButton.setPressed(true);
          }
          if(!deleteCameraImage)
          {
        	  return;
          } else{
        	  cameraFile.delete();
        	  return;
      }
	}

		@Override
		protected void onPreExecute() {
			android.graphics.drawable.Drawable drawable = imageView.getDrawable();
            if(drawable instanceof BitmapDrawable)
            {
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                if(bitmap != null)
                    bitmap.recycle();
            }
            imageView.setImageBitmap(null);
            progress = new ProgressDialog(SpotActivity.this);
            progress.setIndeterminate(true);
            String s = getString(R.string.processing_image);
            progress.setMessage(s);
            progress.show();
		}
		boolean deleteCameraImage;
        ProgressDialog progress;
        
        ProcessImageTask()
        {
        	 super();
            deleteCameraImage = false;
        }
    }

    class ListDataUpdater implements Runnable
    {

        public void run()
        {
            if(listData != null)
                listData.clear();
            if(newData == null)
            {
                return;
            } else
            {
                listData.addAll(newData);
                return;
            }
        }

        List<FSObject> listData;
        List<FSObject> newData;

        ListDataUpdater()
        {
        	super();          
            listData = null;
            newData = null;
        }
    }


    public SpotActivity()
    {
        item = null;
        place = null;
        search = null;
        photoUri = null;
        searchTerm = new StringBuilder();
      
        placeResults = null;
        itemResults = null;
        searchResultsPlaces = null;
        spotNearbyPlaces = null;
        spotPlaceItems = null;
        currentPage = 0;
        flipperHeight = 0;
        showingNearestPlaces = true;
        haveImage = false;
        waitingOnSubactivity = false;
        manuallySettingText = true;
        pickedPhoto = false;
        tookPhoto = false;
        currentDeviceConfig = null;
        File file = Environment.getExternalStorageDirectory();
        tmpFile = new File(file, "spot_jpg.dat");
        
        File file2 = Environment.getExternalStorageDirectory();
        cameraFile = new File(file2, "spot_camera.jpg");
        
        handlerCallback = new android.os.Handler.Callback() {

            public boolean handleMessage(Message message)
            {
            	if(message.arg1 == 2)
                {
                    if(currentPage == 1)
                    {
                    	View view = placesListHeader.getChildAt(0);
                        SpotActivity.this.showHeaderView(view);
                    } else
                    if(currentPage == 2)
                    {
                        View view = itemsListHeader.getChildAt(0);
                        SpotActivity.this.showHeaderView(view);
                    }
                } else if(message.arg1 == 3){
                    if(currentPage == 1)
                    {
                        View view = placesListHeader.getChildAt(0);
                        SpotActivity.this.hideHeaderView(view);
                    } else
                    if(currentPage == 2)
                    {
                        View view = itemsListHeader.getChildAt(0);
                        SpotActivity.this.hideHeaderView(view);
                    }
                }
               
                SpotActivity.this.setAddHeaderText(searchTerm);
                if(currentPage == 2)
                {
                    itemsProgress.setVisibility(8);
                    itemResultsAdapter.notifyDataSetChanged();
                } else if(currentPage == 1){
                	placesProgress.setVisibility(8);
                    placeResultsAdapter.notifyDataSetChanged();
                }
                return true;
            }
        };
        
        handler = new Handler(handlerCallback);
        
        part1AnimationListener = new android.view.animation.Animation.AnimationListener() {

            public void onAnimationEnd(Animation animation)
            {
                if(animation != null)
                    animation.setAnimationListener(null);
                View view;
                android.widget.FrameLayout.LayoutParams layoutParams;
                ViewParent viewParent;
                if(flipperHeight == 0)
                    if(isLandscape())
                    {
                        int i = flipper.getRootView().getHeight();
                        int j = flipper.getHeight();
                        int k = i - j;
                       
                        int l = flipper.getWidth() - k;
                        SpotActivity.this.flipperHeight = l;
                    } else
                    {                                            
                        SpotActivity.this.flipperHeight = flipper.getHeight();
                    }
                view = findViewById(R.id.content);
                layoutParams = (android.widget.FrameLayout.LayoutParams)view.getLayoutParams();
                
                layoutParams = new android.widget.FrameLayout.LayoutParams(-1, flipperHeight);
                view.setLayoutParams(layoutParams);
                viewParent = view.getParent();
                do
                {
                    if(viewParent == null)
                        return;
                    if(viewParent != null && (viewParent instanceof ScrollView))
                    {
                        ScrollView scrollview = (ScrollView)viewParent;
                        int j1 = flipperHeight / 2;
                        scrollview.scrollTo(0, j1);
                        return;
                    }
                    viewParent = viewParent.getParent();
                } while(true);
            }

            public void onAnimationRepeat(Animation animation)
            {
            }

            public void onAnimationStart(Animation animation)
            {
            }
        };
        
        part4AnimationListener = new android.view.animation.Animation.AnimationListener() {

            public void onAnimationEnd(Animation animation)
            {
                animation.setAnimationListener(null);
                android.widget.LinearLayout.LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams)uploadButton.getLayoutParams();
                int i = flipperHeight;
                int j = uploadButton.getBottom();
                int k = i - j;
                int l = layoutParams.bottomMargin;
                int i1 = k - l;
                int j1 = layoutParams.leftMargin;
                int k1 = layoutParams.topMargin + i1;
                int l1 = layoutParams.rightMargin;
                int i2 = layoutParams.bottomMargin;
                layoutParams.setMargins(j1, k1, l1, i2);
                uploadButton.setLayoutParams(layoutParams);
            }

            public void onAnimationRepeat(Animation animation)
            {
            }

            public void onAnimationStart(Animation animation)
            {
            }
        };
            
			takePhotoCallback = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
                hideKeyboard();
                tookPhoto = true;
                pickedPhoto = false;
                String s = getString(R.string.starting_camera);
                SpotActivity.this.showLoadingView(s);
                waitingOnSubactivity = true;
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                Uri uri = Uri.fromFile(cameraFile);
                intent.putExtra("output", uri);
                intent.putExtra("android.intent.extra.videoQuality", 1);
                startActivityForResult(intent,Activity.DEFAULT_KEYS_DIALER);
            }
        };
        
        pickPhotoCallback = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
                hideKeyboard();
                pickedPhoto = true;
                tookPhoto = false;
                SpotActivity spotactivity = SpotActivity.this;
                String s = getString(R.string.loading_photos);
                spotactivity.showLoadingView(s);
                waitingOnSubactivity = true;
                Uri uri = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                Intent intent = new Intent("android.intent.action.PICK",uri);
                startActivityForResult(intent, 2);
            }
        };
        
			uploadPhoto = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
                Log.w("SpotActivity", "uploadPhoto clicked");
            	hideKeyboard();
            	
                if(User.isNotLoggedIn())
                {
                    waitingOnSubactivity = true;
                    ((HomeActivity)getParent()).showAuthenticationActivity(false);
                    return;
                } else
                {
                    SpotActivity.this.showLoadingView(getString(R.string.uploading));
                    Review review = new Review(item, place);                  
                    review.delegate = SpotActivity.this;
                    String edit_comment = ((EditText)findViewById(R.id.edit_comments)).getText().toString();
                    boolean send2sina = ((CheckBox)findViewById(R.id.check_sinaweibo)).isChecked();
                    String text = editPrice.getText().toString();
                    float price = 10.0f; //default is 10
                    if(text != null && !text.equals("")){
                    	price = Float.parseFloat(text);
                    }
                    review.upload(spotImgFile, edit_comment,price,send2sina); //the last, send2sina
                    return;
                }
            }
        };
        
			searchListener = new android.widget.TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView textView, int i, KeyEvent keyevent)
            {
                boolean flag;
                if(keyevent != null)
                {
                    if(keyevent.getAction() == 1)
                    {
                        doSearch(textView);
                        if(isLandscape())
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
        
			searchStringChangeListener = new TextWatcher() {

            public void afterTextChanged(Editable editable)
            {
                View view;
                ViewGroup viewgroup;
                searchTerm.setLength(0);
                searchTerm.append(editable);
                setAddHeaderText(editable);
                if(manuallySettingText)
                {
                    manuallySettingText = false;
                    return;
                }
                view = null;
                viewgroup = null;
                if (currentPage != 2) {
                	if(currentPage == 1) {
                        view = placesProgress;
                        viewgroup = placesListHeader;
                    }
                } else {
                    view = itemsProgress;
                    viewgroup = itemsListHeader;                    
                }
                
                if(view != null && view.getVisibility() != 0) {
                    view.setVisibility(0);
                }
                if(viewgroup != null) {
                    SpotActivity spotactivity = SpotActivity.this;
                    View view1 = viewgroup.getChildAt(0);
                    spotactivity.showHeaderView(view1);
                }

                handler.removeCallbacks(doSearchTask);
                handler.postDelayed(doSearchTask, 1000L);

            }

            public void beforeTextChanged(CharSequence charsequence, int i, int j, int k)
            {
            }

            public void onTextChanged(CharSequence charsequence, int i, int j, int k)
            {
            }
        };
       
			itemResultsListener = new android.widget.AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterview, View view, int i, long l)
            {
                if(view.getId() == R.id.list_header)
                {
                    if(searchTerm.length() == 0)
                        return;
                    manuallySettingText = true;

                    editFood.setText(searchTerm);
                    
                    int j = searchTerm.length();
                    editFood.setSelection(j);
                    SpotActivity spotactivity = SpotActivity.this;
                    
                    spotactivity.item = new Item();
                    
                    item.name = searchTerm.toString();
                    if(itemsProgress != null && itemsProgress.getVisibility() == 0)
                        itemsProgress.setVisibility(8);
                    showNext();
                    return;
                }
                manuallySettingText = true;
                if(currentPage == 2)
                {
             
                    SpotActivity.this.item = (Item)adapterview.getItemAtPosition(i);

                    editFood.setText(item.name);
                    if(item.name != null)
                    {
                        editFood.setSelection(item.name.length());
                    }
                }
                showNext();
            }
        };
        
			placeResultsListener = new android.widget.AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterview, View view, int i, long l)
            {
                if(view.getId() == R.id.list_header)
                {
                    manuallySettingText = true;
                    editPlace.setText(searchTerm);
                    int j = searchTerm.length();
                    editPlace.setSelection(j);
                    hideKeyboard();
                    waitingOnSubactivity = true;
                    SpotActivity spotactivity = SpotActivity.this;
                    Intent intent = new Intent(spotactivity, com.bocai.AddPlaceActivity.class);
                    intent.setFlags(0x20000000);
                    String s = searchTerm.toString();
                    intent.putExtra("placeName", s);
                    ((HomeActivity)getParent()).pushIntentForResult(intent, 3);
                    return;
                }
                manuallySettingText = true;
                SpotActivity.this.place = (Place)adapterview.getItemAtPosition(i);
                editPlace.setText(place.name);
                if(place.name != null)
                {
                    int k = place.name.length();
                    editPlace.setSelection(k);
                }
                if(place != null)
                {
                    searchTerm.setLength(0);
                    manuallySettingText = true;
                    editFood.setText(null);
                    itemResults.clear();
                    itemResultsAdapter.notifyDataSetChanged();
                    if(spotPlaceItems.placeItems != null)
                    {
                        if(!spotPlaceItems.isSamePlace(place)){
                        	spotPlaceItems.placeItems.clear();
                        }
                    }
//                    if(place.sightingsCount > 0)
//                    {
//                        if(itemsProgress != null && itemsProgress.getVisibility() != 0)
//                            itemsProgress.setVisibility(0);
//                        spotPlaceItems.updatePlaceItems(place);
//                        View view1 = itemsListHeader.getChildAt(0);
//                        SpotActivity.this.showHeaderView(view1);
//                    }
                       //NOTE: query every time
                      if(itemsProgress != null && itemsProgress.getVisibility() != 0){
                    	  itemsProgress.setVisibility(0);
                      }
                      spotPlaceItems.updatePlaceItems(place);
                      View view1 = itemsListHeader.getChildAt(0);
                      SpotActivity.this.showHeaderView(view1);
                }
                showNext();
            }
        };
        
			doSearchTask = new Runnable() {

            public void run()
            {
                if(currentPage == 1)
                {
                    SpotActivity.this.doSearch(editPlace);
                    return;
                }
                if(currentPage != 2)
                {
                    return;
                } else
                {
                    SpotActivity.this.doSearch(editFood);
                    return;
                }
            }
        };
        
			showRibbon = new Runnable() {

            public void run()
            {
                ribbon.setVisibility(0);
            }
        };
        
			hideRibbon = new Runnable() {

            public void run()
            {
                ribbon.setVisibility(8);
            }
        };
        
        listDataUpdater = new ListDataUpdater();
    }

    private boolean isLandscape()
    {
        Log.i(LOG_TAG, "isLandscape method");
    	boolean flag;
        if(currentDeviceConfig != null && currentDeviceConfig.orientation == 2)
            flag = true;
        else
            flag = false;
        return flag;
    }

    private void updateSearchUIState(int i)
    {
        Log.w("SpotActivity", "updateSearchUIState " + i);
    	if(i == 1)
        {
            if(showingNearestPlaces && searchResultsPlaces != null && searchResultsPlaces.size() > 0)
            {
                placeResults.clear();
                placeResults.addAll(searchResultsPlaces);
                manuallySettingText = true;
                editPlace.setText(null);
                Message message = handler.obtainMessage(1, 3, 0);
                handler.sendMessage(message);
            }
            showKeyboard(editPlace);
            placesListView.setSelectionAfterHeaderView();
            return;
        }
        if(i == 0)
        {
            if(editFood.getText().length() == 0 && spotPlaceItems.placeItems != null)
            {
                itemResults.clear();
                itemResults.addAll(spotPlaceItems.placeItems);
                manuallySettingText = true;
                editFood.setText(null);
                if(itemsProgress != null && itemsProgress.getVisibility() == 0)
                    itemsProgress.setVisibility(8);
               handler.sendMessage(handler.obtainMessage(1, 3, 0));
            }
            showKeyboard(editFood);
            itemsListView.setSelectionAfterHeaderView();
            return;
        }
        
        if(editPlace.getText() != null && editPlace.getText().length() != 0)
        {
            return;
        } else
        {
            showingNearestPlaces = true;
            return;
        }
     
    }

    public void FSResponse(List<FSObject> list)
    {
    	Log.i(LOG_TAG, "FSResponse method===" + list.toString());
    	LinkedList linkedList = null;
    	if (currentPage != 1) {
    		if (currentPage == 2) {
    			linkedList = itemResults;
    		}
    		if (linkedList != null) {
                listDataUpdater.listData = linkedList;
                listDataUpdater.newData = list;
                ListDataUpdater listdataupdater = listDataUpdater;
                runOnUiThread(listdataupdater);
    		}
            Message message = handler.obtainMessage(1, 2, 0);
            handler.sendMessage(message);
    	} else {
    		linkedList = placeResults;
    		if (linkedList != null) {
                listDataUpdater.listData = linkedList;
                listDataUpdater.newData = list;
                ListDataUpdater listdataupdater = listDataUpdater;
                runOnUiThread(listdataupdater);
           
    		}
            Message message = handler.obtainMessage(1, 2, 0);
            handler.sendMessage(message);
    	}
    
    	Log.i(LOG_TAG, "####FSRespone end");
    	
    }

    public void displayErrors(JSONObject jsonobject)
        throws JSONException
    {
        Log.i(LOG_TAG, "displayErrors method");
    	hideLoadingView();
        if(jsonobject == null)
        {          
            Runnable runnable = new Runnable() {

                public void run()
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SpotActivity.this);
                    String s = getString(R.string.network_error);
                    builder.setTitle(s);
                    String s1 = getString(R.string.fs_offline);
                    builder.setMessage(s1);
                    String s2 = getString(R.string.try_again);
                    builder.setPositiveButton(s2, null).show();
                }
            };
            handler.post(runnable);
            return;
        }
        StringBuilder stringbuilder = null;
        JSONArray jsonArray = jsonobject.optJSONArray("errors");
        if(jsonArray != null)
        {
            stringbuilder = new StringBuilder();
            int i = jsonArray.length();
            for(int j = 0; j < i; j++)
            {
                Object obj = jsonArray.get(j);
                stringbuilder.append(obj).append('\n');
            }

        }
        final String error = stringbuilder.toString();
        
        Runnable runnable1 = new Runnable() {

            public void run()
            {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SpotActivity.this);
                String s = getString(R.string.whoops);
                builder.setTitle(s);
                String s1 = error;
                builder.setMessage(s1);
                String s2 = getString(R.string.try_again);
                builder.setPositiveButton(s2, null).show();
            }
        };
        
        handler.post(runnable1);
    }

    public void displaySuccess(JSONObject jsonobject)
        throws JSONException
    {
        Log.i(LOG_TAG, "displaySuccess method");
    	hideLoadingView();
        waitingOnSubactivity = false;
        tmpFile.delete();
     //   final String newReviewID = jsonobject.getString("id");
        FlurryEvents.FLURRY_LOG(FlurryEvents.FLURRY_SPOT_UPLOADED());
        Runnable runnable = new Runnable() {

            public void run()
            {
                waitingOnSubactivity = false;
            //    Intent intent = new Intent(SpotActivity.this, com.bocai.ReviewConfirmActivity.class);
           //     intent.setFlags(0x20000000);
            //    intent.putExtra("reviewID", newReviewID);
             //   ((HomeActivity)getParent()).pushIntent(intent);
                //TODO: reviewConfirm is not needed
                ((HomeActivity)getParent()).popNavigationStackToRoot();
                
            }
        };
        
        handler.post(runnable);
    }

    void doSearch(TextView textView)
    {
      	
    	Log.i(LOG_TAG, "doSearch method");
    	handler.removeCallbacks(doSearchTask);
        if(searchTerm.length() > 0)
            if(currentPage == 2)
            {
            
            	
            	search.doItemSearch(searchTerm.toString());
                return;
            } else
            {
                showingNearestPlaces = false;
                android.location.Location location = Macros.FS_CURRENT_LOCATION();
               
                
              search.doPlaceSearch(searchTerm.toString(), location);
                
                return;
            }
        if(currentPage == 1)
        {
            showingNearestPlaces = true;
            updateSearchUIState(1);
            return;
        }
        if(currentPage != 2)
        {
            return;
        } else
        {
            itemResults.clear();
            updateSearchUIState(0);
            return;
        }
    }

    public void doSearchWithName(String s)
    {
    }

    @Override
    public void finishedAction(JSONObject jsonObject)
    {
        Log.i(LOG_TAG, "finishedAction method");
    	if(jsonObject == null)
            return;
        String s = jsonObject.optString("action", null);
        if(s == null)
            return;
        if(!s.equals("unauthorized"))
        {
            return;
        } else
        {
            Runnable runnable = new Runnable() {

                public void run()
                {
                    waitingOnSubactivity = true;
                    ((HomeActivity)getParent()).showAuthenticationActivity(false);
                }
            };
            
		handler.post(runnable);
            return;
        }
    }

    public String getActivityTitle()
    {
    	String s = null;
    	switch(currentPage) {
    	case 0:
    		s = getString(R.string.title_choose_img);
    		break;
    	case 1:
            s = getString(R.string.title_choose_place);
            break;
    	case 2:
            s = getString(R.string.title_choose_item);
            break;
    	case 3:
            s = getString(R.string.title_choose_other);
            break;
        default:
        	break;
    	}
    	return s;
    }

    public View[] getToolbarItems()
    {
        return null;
    }

    void gotoPage(int pageNum)
    {
        Log.w("SotActivity", "gotoPage " + pageNum);
    	HomeActivity homeActivity;
        homeActivity = (HomeActivity)getParent();
        if(pageNum < 0)
            return;
        if(pageNum > 3)
            return;
        if(flipperHeight == 0)
        {
        	flipperHeight = flipper.getHeight();
        }
    
        if(pageNum ==0){
        	   hideKeyboard();
               Animation animation = flipper.getInAnimation();
               if(animation != null)
               {
                   animation.setAnimationListener(part1AnimationListener);
               }
        }
        
        flipper.setDisplayedChild(pageNum);
        currentPage = pageNum;
        String str1 = getActivityTitle();
        homeActivity.setTitle(str1);
        
        if(pageNum == 1){
        	searchTerm.setLength(0);
        	 updateSearchUIState(1);
        }else if (pageNum == 2){
        	searchTerm.setLength(0);
        	updateSearchUIState(0);
        }else if (pageNum == 3){
        	 hideKeyboard();
        	 validate();
        	 if (uploadButton.isEnabled()){
                 uploadButton.setPressed(true);
        	 }
               StringBuilder localStringBuilder = new StringBuilder();
               if (item != null)
               {
                 localStringBuilder.append(item.name);
               }
               localStringBuilder.append(" @ ");
               if (place != null)
               {
                 localStringBuilder.append(place.name);
               }
               TextView textview = (TextView)findViewById(R.id.part4).findViewById(R.id.title);
               String s3 = localStringBuilder.toString();
               textview.setText(s3);
               Animation animation1 = flipper.getInAnimation();
               if(animation1 != null)
               {
                   animation1.setAnimationListener(part4AnimationListener);
               }
        }
      
    }

    
    public void handleActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	Log.w("SpotActivity", "handleActivityResult method");
    	if(requestCode == 1 || requestCode == 2){
    		onActivityResult(requestCode, resultCode, intent);
    		 return;
    	}
    	if(resultCode != -1){
    		return;
    	}
    	
    	Bundle bundle = intent.getExtras();
    	if(bundle == null){
    		 validate();
    		 return;
    	}else{
    		 place = (Place)bundle.getParcelable("place");
    		 
    		if(place == null){
    			 if(bundle.containsKey("reset-spot")){
    		        resetState();
    			 }
    		}else{    			
    			  manuallySettingText = true;
    			  editPlace.setText(place.name);
    			  editPlace.setSelection(place.name.length());
    			  showNext();
    		}
    	}
    }

    void hideHeaderView(View view)
    {
    	Log.i(LOG_TAG, "hideHeaderView method");
    	android.view.ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = 0;
        layoutParams.height = 0;
        view.setLayoutParams(layoutParams);
        Log.i(LOG_TAG, "hideHeaderView method##");
    }

    void hideKeyboard()
    {
        View view = getCurrentFocus();
        if(view == null)
            view = getWindow().getDecorView();
        if(view == null)
        {
            return;
        } else
        {
            InputMethodManager inputmethodmanager = (InputMethodManager)getSystemService("input_method");
            android.os.IBinder ibinder = view.getWindowToken();
            inputmethodmanager.hideSoftInputFromWindow(ibinder, 0);
            return;
        }
    }

    void hideLoadingView()
    {
        if(progressDialog == null)
        {
            return;
        } else
        {
            progressDialog.dismiss();
            return;
        }
    }

    void hidePhotoButtons()
    {
        pickPhotoButton.setVisibility(8);
        takePhotoButton.setVisibility(8);
    }

    @Override
    public void nearbyPlacesFinished()
    {
        Log.i(LOG_TAG, "nearbyPlacesFinished method");
    	searchResultsPlaces.clear();
        searchResultsPlaces.addAll(spotNearbyPlaces.nearbyPlaces);
        if(currentPage != 1)
            return;
        placeResults.clear();
        if(spotNearbyPlaces.nearbyPlaces != null)
             placeResults.addAll(spotNearbyPlaces.nearbyPlaces);
        Message message = handler.obtainMessage(1, 3, 0);
        handler.sendMessage(message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	Log.w("SotActivity", "onActivityResult");
        
    	switch(requestCode)
        {
        default:
            return;

        case 1: // '\001'
            pickedPhoto = false;
            tookPhoto = true;
            if(resultCode != -1)
            {
                return;
            } else
            {
                Uri uri = Uri.fromFile(cameraFile);     
                ProcessImageTask processImageTask = new ProcessImageTask();
                processImageTask.deleteCameraImage = true;
                Uri auri[] = new Uri[1];
                auri[0] = uri;
                processImageTask.execute(auri);
                photoUri = Uri.parse(tmpFile.getAbsolutePath());
                hidePhotoButtons();
                return;
            }

        case 2: // '\002'
            pickedPhoto = true;
            tookPhoto = false;
            break;
        }
        if(resultCode != -1)
        {
            return;
        } else
        {
        	photoUri = intent.getData();
        	Log.w("Spot", "photoUri===" + photoUri.toString());
        	 String[] proj = { MediaStore.Images.Media.DATA };
        	 Cursor cursor = managedQuery(photoUri, proj, null, null, null);
        	 int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        	 cursor.moveToFirst();
        	 String realPath = cursor.getString(column_index);
        	 
        	 Log.w("Spot","real path=" + realPath);
        	 spotImgFile = new File(realPath);
        	 Log.w("Spot","file.length=" + spotImgFile.length());

            ProcessImageTask processImageTask1 = new ProcessImageTask();
            Uri auri1[] = new Uri[1];
            auri1[0] = photoUri;
            processImageTask1.execute(auri1);
            photoUri = Uri.parse(tmpFile.getAbsolutePath());
            hidePhotoButtons();
            return;
        }
    }

    public void onConfigurationChanged(Configuration configuration)
    {
        String s = (new StringBuilder()).append("onConfigurationChanged(").append(configuration).append(")").toString();
        Log.d("Spot", s);
        currentDeviceConfig = configuration;
        super.onConfigurationChanged(configuration);
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.spot);
        View view = findViewById(R.id.part2);
        View view1 = findViewById(R.id.part3);
        editPlace = (EditText)view.findViewById(R.id.search_place);
        editFood = (EditText)view1.findViewById(R.id.search_item);
        placesListView = (ListView)view.findViewById(R.id.list_place);
        itemsListView = (ListView)view1.findViewById(R.id.list_item);
        imageView = (ImageView)findViewById(R.id.img_photo);
        ribbon = findViewById(R.id.ribbon);
        pickPhotoButton = (Button)findViewById(R.id.btn_pick_photo);
        takePhotoButton = (Button)findViewById(R.id.btn_take_photo);
        uploadButton = (Button)findViewById(R.id.btn_add);
        editPrice = (EditText)findViewById(R.id.edit_price);
        android.view.View.OnClickListener onClickListener = new android.view.View.OnClickListener() {

            public void onClick(View view8)
            {
                if(pickedPhoto)
                {
                    pickPhotoCallback.onClick(null);
                    return;
                }
                if(!tookPhoto)
                {
                    return;
                } else
                {
                    takePhotoCallback.onClick(null);
                    return;
                }
            }
        };
        
        imageView.setOnClickListener(onClickListener);
        String s = getString(R.string.edit_item_hint);
        editFood.setHint(s);
        String s1 = getString(R.string.edit_place_hint);
        editPlace.setHint(s1);
        placesListView.setOnItemClickListener(placeResultsListener);
        itemsListView.setOnItemClickListener(itemResultsListener);
        editFood.setOnEditorActionListener(searchListener);
        editPlace.setOnEditorActionListener(searchListener);
        editFood.addTextChangedListener(searchStringChangeListener);
        editPlace.addTextChangedListener(searchStringChangeListener);
        View view3 = view.findViewById(R.id.search_go_btn_place);
        android.view.View.OnClickListener onClickListener1 = new android.view.View.OnClickListener() {

            public void onClick(View view8)
            {
                SpotActivity.this.doSearch(editPlace);
            }
        };
        
        view3.setOnClickListener(onClickListener1);
        View view4 = view1.findViewById(R.id.search_go_btn_item);
        android.view.View.OnClickListener onClickListener2 = new android.view.View.OnClickListener() {

            public void onClick(View view8)
            {
                SpotActivity.this.doSearch(editFood);
            }
        };
        
        view4.setOnClickListener(onClickListener2);
     
        placesListHeader = (ViewGroup)getLayoutInflater().inflate(R.layout.search_result_header, null);
        itemsListHeader = (ViewGroup)getLayoutInflater().inflate(R.layout.search_result_header, null);
  
        placesListView.addHeaderView(placesListHeader);
        itemsListView.addHeaderView(itemsListHeader);
        
        itemsAddHeader = (TextView)itemsListHeader.findViewById(R.id.search_result_text);
        TextView textview1 = (TextView)itemsListHeader.findViewById(R.id.search_result_message);
        String s2 = getString(R.string.searching);
        textview1.setText(s2);
        itemsProgress = itemsListHeader.findViewById(R.id.search_result_message);
        placesAddHeader = (TextView)placesListHeader.findViewById(R.id.search_result_text);        
        TextView textview3 = (TextView)placesListHeader.findViewById(R.id.search_result_message);   
        String s3 = getString(R.string.searching);
          textview3.setText(s3);
        placesProgress = placesListHeader.findViewById(R.id.search_result_message);
        placeResults = new LinkedList<FSObject>();
        itemResults = new LinkedList<FSObject>();
        
        placeResultsAdapter = new SearchResultsAdapter(this, R.layout.search_result_item, placeResults);
        placesListView.setAdapter(placeResultsAdapter);
        itemResultsAdapter = new SearchResultsAdapter(this, R.layout.search_result_item, itemResults);
        itemsListView.setAdapter(itemResultsAdapter);
        
        pickPhotoButton.setOnClickListener(pickPhotoCallback);
        takePhotoButton.setOnClickListener(takePhotoCallback);
        uploadButton.setOnClickListener(uploadPhoto);
        spotNearbyPlaces = new SpotNearbyPlaces();
        spotNearbyPlaces.listener = this;
        spotPlaceItems = new SpotPlaceItems();
        spotPlaceItems.listener = this;
        searchResultsPlaces = new LinkedList<FSObject>();

        android.view.View.OnClickListener onClickListener6 = new android.view.View.OnClickListener() {

            public void onClick(View view8)
            {
                boolean flag;
                if(!ribbon.isSelected())
                    flag = true;
                else
                    flag = false;
                ribbon.setSelected(flag);
            }
        };
        
	    ribbon.setOnClickListener(onClickListener6);
	    flipper = (ViewFlipper)findViewById(R.id.flipper);
	    slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
	    slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
	    slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
	    slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
	    nextButton = (Button)findViewById(R.id.btn_next);
        nextButton.setEnabled(false);
        android.view.View.OnClickListener onClickListener7 = new android.view.View.OnClickListener() {

            public void onClick(View view8)
            {
                showNext();
            }
        };
        
        nextButton.setOnClickListener(onClickListener7);
        search = new Search();
        search.delegate = this;
        Macros.FS_APPLICATION().addStateChangeListener(this);
    }

    protected void onDestroy()
    {
        Log.d("Spot", "onDestroy");
        Macros.FS_APPLICATION().removeStateChangeListener(this);
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent keyEvent)
    {
        boolean flag;
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(currentPage > 0)
            {
                hideKeyboard();
                searchCancel();
                showPrevious();
                flag = true;
            } else
            {
                flag = false;
            }
        } else
        {
            flag = super.onKeyDown(keyCode, keyEvent);
        }
        return flag;
    }

    protected void onPause()
    {
        hideLoadingView();
        super.onPause();
        StringBuilder stringBuilder = (new StringBuilder()).append("onPause: waitingOnSubactivity? ");
        String s;
        String s1;
        if(waitingOnSubactivity)
            s = "true";
        else
            s = "false";
        s1 = stringBuilder.append(s).toString();
        Log.i("Spot", s1);
    }

    protected void onResume()
    {
        super.onResume();
        StringBuilder stringBuilder = (new StringBuilder()).append("onResume: waitingOnSubactivity? ");
        String s;
        String s1;
     //   Configuration configuration;
        if(waitingOnSubactivity)
            s = "true";
        else
            s = "false";
        s1 = stringBuilder.append(s).toString();
        Log.i("Spot", s1);
        currentDeviceConfig = getResources().getConfiguration();
        hideLoadingView();
        if(waitingOnSubactivity){
        	Log.i(LOG_TAG, "onResume===waitingOnSubactivity==true");
          	waitingOnSubactivity = false;
	        updateSearchUIState(-1);
	        return;
        }else{
        	 resetState();
        	 if(!spotNearbyPlaces.locationHasChanged() || 
        		spotNearbyPlaces.locationIsUpdating){
        		  	searchResultsPlaces.clear();
        	        if(spotNearbyPlaces.nearbyPlaces != null)
        	        {
        	           searchResultsPlaces.addAll( spotNearbyPlaces.nearbyPlaces);
        	           Log.i(LOG_TAG, "searchResultsPlaces.addAll===" + spotNearbyPlaces.nearbyPlaces.toString());
        	        }
        	 }else{
        		 spotNearbyPlaces.updateNearbyPlaces();
        	 }
        }
    }

    public void onStateChange(int i)
    {
        if(i != 2)
        {
            return;
        } else
        {
            waitingOnSubactivity = true;
            return;
        }
    }

    @Override
    public void placeItemsFinished()
    {
        Log.i(LOG_TAG, "placeItemsFinished method"); 
    	if(currentPage != 2)
         {
             return;
         } else
         {
             listDataUpdater.listData = itemResults;
             listDataUpdater.newData = spotPlaceItems.placeItems;
             runOnUiThread(listDataUpdater);
             Message message = handler.obtainMessage(1, 3, 0);
             handler.sendMessage(message);
             return;
         }
    }

    void resetState()
    {
        Log.d(LOG_TAG, "resetState method");
        haveImage = false;
        photoUri = null;
        pickedPhoto = false;
        tookPhoto = false;
        showingNearestPlaces = true;
        nextButton.setEnabled(false);
        uploadButton.setEnabled(false);
        waitingOnSubactivity = false;
        item = null;
        place = null;
        searchTerm.setLength(0);
        placeResults.clear();
        itemResults.clear();
        android.graphics.drawable.Drawable drawable = imageView.getDrawable();
        if(drawable != null && (drawable instanceof BitmapDrawable))
        {
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            if(bitmap != null)
                bitmap.recycle();
        }
        imageView.setImageDrawable(null);
        showPhotoButtons();
        ribbon.setVisibility(8);
        manuallySettingText = true;
        editFood.setText(null);
        manuallySettingText = true;
        editPlace.setText(null);
        placesAddHeader.setVisibility(8);
        itemsAddHeader.setVisibility(8);
        flipper.setAnimateFirstView(true);
        flipper.setInAnimation(slideLeftIn);
        gotoPage(0);
    }

    void searchCancel()
    {
        Log.i(LOG_TAG, "searchCancel method");
    	handler.removeCallbacks(doSearchTask);
        if(currentPage != 2){
        	 if(currentPage == 1)
             {
                 manuallySettingText = true;
                 if(place != null)
                 {
                     editPlace.setText(place.name);
                     if(place.name != null)
                     {
                         int j = place.name.length();
                         editPlace.setSelection(j);
                     }
                 } else
                 {
                     editPlace.setText(null);
                     showingNearestPlaces = true;
                 }
             }
        }else{
        	 manuallySettingText = true;
        	  if(item != null)
              {
                  editFood.setText(item.name);
                  if(item.name != null)
                  {
                      int i = item.name.length();
                      editFood.setSelection(i);
                  }
              } else
              {
                  editFood.setText(null);
              }
        	  updateSearchUIState(-1);
        	  return;
        }
    }

    void setAddHeaderText(CharSequence charSequence)
    {
        Log.i(LOG_TAG, "setAddHeaderText method===" + charSequence);
    	if(currentPage == 2)
            if(charSequence != null && charSequence.length() > 0)
            {
                String addNewFood = getString(R.string.add_new_food);
            	String s = (new StringBuilder()).append(addNewFood).append(" \"").append(charSequence).append('"').toString();
                itemsAddHeader.setText(s);
                itemsAddHeader.setVisibility(0);
                return;
            } else
            {
                itemsAddHeader.setVisibility(8);
                return;
            }
        if(currentPage != 1)
            return;
        if(charSequence != null && charSequence.length() > 0)
        {
        	String addNewPlace = getString(R.string.add_new_place);
        	String s1 = (new StringBuilder()).append(addNewPlace).append(" \"").append(charSequence).append('"').toString();
        	placesAddHeader.setText(s1);
            placesAddHeader.setVisibility(0);
            return;
        } else
        {
        	placesAddHeader.setVisibility(8);
            return;
        }
    }

    void showHeaderView(View view)
    {
       Log.i(LOG_TAG, "showHeaderView method");
    	android.view.ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        view.setLayoutParams(layoutParams);
    }

    void showKeyboard(View view)
    {
        view.setFocusable(true);
        view.requestFocus();
        ((InputMethodManager)getSystemService("input_method")).showSoftInput(view, 1);
    }

    void showLoadingView(String s)
    {
        Log.i(LOG_TAG, "showLoadingView method===" + s);
    	if(progressDialog == null)
        {
        	progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
        }
        progressDialog.setMessage(s);
        progressDialog.show();
        View view = progressDialog.findViewById(0x102000d);
        if(view == null)
        {
            return;
        } else
        {
            view.setVisibility(8);
            view.setVisibility(0);
            return;
        }
    }

    void showNext()
    {
        Log.w("SotActivity", "showNext method");
        flipper.setInAnimation(slideLeftIn);
        flipper.setOutAnimation(slideLeftOut);
        int pageNum = currentPage + 1;
        gotoPage(pageNum);
    }

    void showPhotoButtons()
    {
        pickPhotoButton.setVisibility(View.VISIBLE);
        takePhotoButton.setVisibility(View.VISIBLE);
    }

    void showPrevious()
    {
        flipper.setInAnimation(slideRightIn);
        flipper.setOutAnimation(slideRightOut);
        int pageNum = currentPage - 1;
        gotoPage(pageNum);
    }

    protected void validate()
    {
        StringBuilder stringBuilder = (new StringBuilder()).append("validate: haveImage: ");
       
        stringBuilder.append(haveImage).append(", item? ");
        boolean flag1;
        boolean flag2;


        boolean flag4;
        if(item != null)
            flag1 = true;
        else
            flag1 = false;
        stringBuilder.append(flag1).append(", place? ");
        if(place != null)
            flag2 = true;
        else
            flag2 = false;

        Log.d("Spot", stringBuilder.append(flag2).toString());
        if(haveImage)
        {
            handler.post(showRibbon);
            nextButton.setEnabled(true);
        } else
        {
            handler.post(hideRibbon);
        }
        if(item != null && place != null && haveImage)
            flag4 = true;
        else
            flag4 = false;
        if(!flag4)
        {
            return;
        } else
        {
            uploadButton.setEnabled(true);
            return;
        }
    }

    static final int ADD_PLACE = 3;
    static final int EDIT_FOOD = 0;
    static final int EDIT_NONE = 255;
    static final int EDIT_PLACE = 1;
//    private static final int HIDE_HEADER = 3;
    static final String LOG_TAG = "SpotActivity";
    static final int MAX_IMAGE_DIMENSION = 800;
    static final int PAGE_FOOD = 2;
    static final int PAGE_IMAGE = 0;
    static final int PAGE_OTHER = 3;
    static final int PAGE_PLACE = 1;
    static final int PHOTO_PICK = 2;
    static final int PHOTO_TAKE = 1;
//    private static final int REFRESH = 1;
//    private static final int SHOW_HEADER = 2;
    final File cameraFile;
    Configuration currentDeviceConfig;
    int currentPage;
    final Runnable doSearchTask;
    EditText editFood;
    EditText editPlace;
    ViewFlipper flipper;
    int flipperHeight;
    final Handler handler;
    final android.os.Handler.Callback handlerCallback;
    boolean haveImage;
    final Runnable hideRibbon;
    ImageView imageView;
    Item item;
    LinkedList<FSObject> itemResults;
    SearchResultsAdapter itemResultsAdapter;
    android.widget.AdapterView.OnItemClickListener itemResultsListener;
    TextView itemsAddHeader;
    ViewGroup itemsListHeader;
    ListView itemsListView;
    View itemsProgress;
    final ListDataUpdater listDataUpdater;
    boolean manuallySettingText;
    Button nextButton;
    android.view.animation.Animation.AnimationListener part1AnimationListener;
    android.view.animation.Animation.AnimationListener part4AnimationListener;
    Uri photoUri;
    Button pickPhotoButton;
    android.view.View.OnClickListener pickPhotoCallback;
    boolean pickedPhoto;
    Place place;
    LinkedList<FSObject> placeResults;
    SearchResultsAdapter placeResultsAdapter;
    android.widget.AdapterView.OnItemClickListener placeResultsListener;
    TextView placesAddHeader;
    ViewGroup placesListHeader;
    ListView placesListView;
    View placesProgress;
    ProgressDialog progressDialog;
    View ribbon;
    Search search;
    android.widget.TextView.OnEditorActionListener searchListener;
    LinkedList<FSObject> searchResultsPlaces;
    TextWatcher searchStringChangeListener;
    StringBuilder searchTerm;
    final Runnable showRibbon;
    boolean showingNearestPlaces;
    Animation slideLeftIn;
    Animation slideLeftOut;
    Animation slideRightIn;
    Animation slideRightOut;
    SpotNearbyPlaces spotNearbyPlaces;
    SpotPlaceItems spotPlaceItems;
    Button takePhotoButton;
    android.view.View.OnClickListener takePhotoCallback;
    EditText editPrice;
    final File tmpFile;
    boolean tookPhoto;
    Button uploadButton;
    android.view.View.OnClickListener uploadPhoto;
    boolean waitingOnSubactivity;
    File spotImgFile;

}
