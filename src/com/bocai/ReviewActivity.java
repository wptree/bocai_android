// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReviewActivity.java

package com.bocai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.bocai.ImageDownloader;
import com.bocai.model.FSObject;
import com.bocai.model.FSObjectDelegate;
import com.bocai.model.Item;
import com.bocai.model.Person;
import com.bocai.model.Place;
import com.bocai.model.Review;
import com.bocai.model.Sighting;
import com.bocai.model.User;
import com.bocai.util.DateUtilities;
import com.bocai.util.Macros;
import com.bocai.util.ToastFire;
import com.bocai.widget.GroupedTableView;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;

// Referenced classes of package com.bocai:
//            TabStackActivityGroup, BrowseActivity, HomeActivity, BocaiApplication, 
//            PlaceDetailActivity

public class ReviewActivity extends Activity
    implements FSObjectDelegate, HomeActivity.ToolbarItemSource
{
    public class SightingReviewAdapter extends ArrayAdapter
    {

        public View getView(int i, View view, ViewGroup viewgroup)
        {
            TextView textview;
            TextView textview1;
            TextView textview2;
            ImageView imageview;
            int k;
            if(view == null)
            {
                view = _inflater.inflate(_itemParentId, null);
                textview = (TextView)view.findViewById(R.id.title);
                textview1 = (TextView)view.findViewById(R.id.subtitle);
                textview2 = (TextView)view.findViewById(R.id.content);
                imageview = (ImageView)view.findViewById(R.id.img_photo);
                Object aobj[] = new Object[4];
                aobj[0] = textview;
                aobj[1] = textview1;
                aobj[2] = textview2;
                aobj[3] = imageview;
                view.setTag(((Object) (aobj)));
                android.widget.LinearLayout.LayoutParams layoutparams = new android.widget.LinearLayout.LayoutParams(-1, -1, 0F);
                layoutparams.setMargins(3, 2, 0, 4);
                view.setLayoutParams(layoutparams);
            } else
            {
                Object aobj1[] = (Object[])(Object[])view.getTag();
                textview = (TextView)aobj1[0];
                textview1 = (TextView)aobj1[0];
                textview2 = (TextView)aobj1[0];
                imageview = (ImageView)aobj1[0];
            }
            view.setClickable(true);
            k = _objects.size();
            
            Log.i("ReviewActivity--SightingReviewAdapter--getView i,k", ""+ i + "," + k);
            
            if (i < k) {
            	Review review = (Review)_objects.get(i);
            	if(review.thumb90 == null) {
                    if(review.thumb90URL != null)
                    {
                        imageDownloader.download(review.thumb90URL, imageview);
                    } else
                    {
                        String s4 = (new StringBuilder()).append("Review has no thumb 90!: ").append(review).toString();
                        Log.i("ReviewActivity", s4);
                    }

            	} else {
                    Bitmap bitmap = review.thumb90;
                    imageview.setImageBitmap(bitmap);
            	}
                
                if(review.user != null && review.user.name != null)
                {
                	textview.setText(review.user.name);
                }
                if(review.takenAt != null)
                {
                	String s1 = DateUtilities.getRelativeDate(review.takenAt);
                    textview1.setText(s1);
                }
                if(review.note != null)
                {
                	textview2.setText(review.note);
                } else
                {
                    textview2.setText("");
                }
                return view;
            } else {
            	return view;
            }

        }

        private LayoutInflater _inflater;
        private int _itemParentId;
        private List _objects;
        ImageDownloader imageDownloader;

        public SightingReviewAdapter(Context context, int i, List list)
        {
            super(context, i, list);

            _objects = list;
            _itemParentId = i;
            LayoutInflater layoutinflater = LayoutInflater.from(context);
            _inflater = layoutinflater;
            imageDownloader = Macros.FS_APPLICATION().imageDownloader;
        }
    }


    public ReviewActivity()
    {
        uniqueToken = null;
        handler = new Handler();
        run_refreshMetadata = new Runnable() {

            public void run()
            {
                reloadMetadataTable();
            }
        };
        
        run_refreshReviews = new Runnable() {

            public void run()
            {
            	 Log.i(LOG_TAG, "run_refreshReviews");
            	if(sightingReviews.size() > 0)
                {
                	Log.i("ReviewActivity--sightingReviews.size()", ""+sightingReviews.size());
                    reviewsTable.setVisibility(0);
                    sightingReviewsLabel.setVisibility(0);
                } else
                {
                    reviewsTable.setVisibility(8);
                    sightingReviewsLabel.setVisibility(8);
                }
                sightingReviewsAdapter.notifyDataSetChanged();
            }
        };
        
        run_updateScoreboard = new Runnable() {

            public void run()
            {
                updateScoreboard();
            }
        };
        
        run_updateScoreboardForCurrentReview = new Runnable() {

            public void run()
            {
                updateScoreboardForCurrentReview();
            }
        };
        
        scoreboardButtonListener = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
                String s;
                if(User.isNotLoggedIn())
                {
                    ((HomeActivity)getParent()).showAuthenticationActivity(false);
                    return;
                }
                if(currentReview == null)
                    return;
                s = null;
				switch (view.getId()) {
				case R.id.sb_want:
	                s = "want";
	                CheckedTextView checkedtextview = (CheckedTextView)findViewById(R.id.label_sb_want_qty);
	                boolean flag = sighting.wanted;
	                int i = sighting.wantsCount;
	                sighting.wanted = true;
	                toggleCheck(checkedtextview, flag, i);
	
				case R.id.label_sb_want_qty:
	                if(s == null)
	                {
	                    return;
	                } else
	                {
	                    view.setClickable(false);
	                    currentReview.performAction(s);
	                    return;
	                }					
				case R.id.sb_nom:
					s = "nom";
	                ReviewActivity reviewactivity1 = ReviewActivity.this;
	                CheckedTextView checkedtextview1 = (CheckedTextView)findViewById(R.id.label_sb_nom_qty);
	                boolean flag1 = sighting.nommed;
	                int j = sighting.ribbonsCount;
	                reviewactivity1.toggleCheck(checkedtextview1, flag1, j);
				case R.id.label_sb_nom_qty:
					if (s == null){
						return;
					}else{
						view.setClickable(false);
						currentReview.performAction(s);
						return;
					}
				
				default:
	                if(s == null)
	                {
	                    return;
	                } else
	                {
	                    view.setClickable(false);
	                    currentReview.performAction(s);
	                    return;
	                }
				}
        } };
        
        metadataTableListener = new com.bocai.widget.GroupedTableView.OnItemClickListener() {

            public void onItemClick(GroupedTableView groupedtableview, View view, int i)
            {
                view.setClickable(false);
                final View clicked = view;
                Runnable runnable4 = new Runnable() {

                    public void run()
                    {
                        clicked.setClickable(true);
                    }
                };
                
                view.postDelayed(runnable4, 250L);
                if(i == 1)
                {
                    showPlaceDetail();
                    return;
                }
                if(i != 2)
                {
                    return;
                } else
                {
                    doItemSearch();
                    return;
                }
            }
        };
        
        showReview = new com.bocai.widget.GroupedTableView.OnItemClickListener() {

            public void onItemClick(GroupedTableView groupedtableview, View view, int i)
            {
                view.setClickable(false);
                final View clicked = view;
                Runnable runnable4 = new Runnable() {

                    public void run()
                    {
                        clicked.setClickable(true);
                    }
                };
                
                view.postDelayed(runnable4, 250L);
                ReviewActivity reviewactivity = ReviewActivity.this;
                Intent intent = new Intent(reviewactivity, com.bocai.ReviewActivity.class);
                intent.putExtra("init_mode", 2);
                intent.putExtra("sighting", sighting);
                Parcelable parcelable = (Parcelable)sightingReviews.get(i);
                intent.putExtra("review", parcelable);
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

    private void hideLoadingView()
    {
    }

    public void FSResponse(List list)
    {
        Log.i(LOG_TAG, "FSResponse method");
    	hideLoadingView();
        if(list == null)
        {
            Log.i("ReviewActivity", "NO RESPONSE, incrementing comments count");
            return;
        }
        if(list != null)
        {
            sightingReviews.clear();
            Iterator iterator = list.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                FSObject fsobject = (FSObject)iterator.next();
                if(fsobject instanceof Review)
                {
                    Review review = (Review)fsobject;
                    if(currentReviewID != review.reviewID) {
                        sightingReviews.add(review);
                    }
                }
            } while(true);
        }
        StringBuilder stringbuilder = (new StringBuilder()).append("got this many reviews/sightings: ");
        String s = stringbuilder.append(sightingReviews.size()).toString();
        Log.i("ReviewActivity--FSResponse ", "sightingReviews.size()" + s);
        handler.post(run_refreshReviews);
        Log.i("ReviewActivity", "done reloading from fsresponse");
    }

    void disableScoreboard()
    {
        ViewGroup viewgroup = (ViewGroup)findViewById(R.id.scoreboard);
        int i = viewgroup.getChildCount();
        int j = 0;
        do
        {
            if(j >= i)
                return;
            viewgroup.getChildAt(j).setClickable(false);
            j++;
        } while(true);
    }

    public void displayErrors(JSONObject jsonobject)
        throws JSONException
    {
        hideLoadingView();
        StringBuilder stringbuilder = null;
        if(jsonobject != null)
        {
            stringbuilder = new StringBuilder();
            for(Iterator iterator = jsonobject.keys(); iterator.hasNext();)
            {
                String s = (String)iterator.next();
                Object obj = jsonobject.get(s);
                StringBuilder stringbuilder1 = stringbuilder.append(obj).append('\n');
            }

        }
        final String error = stringbuilder.toString();
        Runnable runnable = new Runnable() {

            public void run()
            {
                ReviewActivity reviewactivity = ReviewActivity.this;
                android.app.AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(reviewactivity)).setTitle("Whoops! We ran into some problems:");
                String s1 = error;
                android.app.AlertDialog alertdialog = builder.setMessage(s1).setPositiveButton("Try again", null).show();
            }
        };
        handler.post(runnable);
    }

    public void displaySuccess(JSONObject jsonobject)
    {
    }

    void doItemSearch()
    {
        Item item;
        if(loadedReview)
            item = currentReview.item;
        else
            item = sighting.item;
        if(item == null)
            return;
        if(item.name == null)
            return;
        Activity activity = getParent();
        if(!(activity instanceof TabStackActivityGroup))
            return;
        TabStackActivityGroup tabstackactivitygroup = (TabStackActivityGroup)activity;
        Activity activity1 = tabstackactivitygroup.getRootActivity();
        if(activity1 == null)
            return;
        if(!(activity1 instanceof BrowseActivity))
        {
            return;
        } else
        {
            BrowseActivity browseactivity = (BrowseActivity)activity1;
            browseactivity.doSearchDish(item.name);
            tabstackactivitygroup.popNavigationStackToRoot();
            return;
        }
    }

    public void doSearchWithName(String s)
    {
    }

    void enableScoreboard()
    {
        ViewGroup viewgroup = (ViewGroup)findViewById(0x7f08003f);
        int i = viewgroup.getChildCount();
        int j = 0;
        do
        {
            if(j >= i)
                return;
            viewgroup.getChildAt(j).setClickable(true);
            j++;
        } while(true);
    }

    public void finishedAction(JSONObject jsonobject)
        throws JSONException
    {
        hideLoadingView();
        if(jsonobject == null)
            return;
        String s = jsonobject.optString("action", null);
        if(s == null)
        {
            Log.i("ReviewActivity", ">>> NULL action returned");
            return;
        }
        String s1 = (new StringBuilder()).append("REVIEW finishedAction: ").append(s).toString();
        Log.i("ReviewActivity", s1);
        if(s.equals("want"))
        {
            CheckedTextView checkedtextview = (CheckedTextView)findViewById(R.id.label_sb_want_qty);
            sighting.wanted = checkedtextview.isChecked();
         //   sighting.wantsCount = jsonobject.getInt("count");
            //TODO:
            sighting.wantsCount = sighting.wantsCount + 1;
            handler.post(run_updateScoreboard);
            return;
        }
        if(s.equals("nom"))
        {
            CheckedTextView checkedtextview1 = (CheckedTextView)findViewById(R.id.label_sb_nom_qty);
            sighting.nommed = checkedtextview1.isChecked();
         //   sighting.ribbonsCount = jsonobject.getInt("count");
            //TODO:
            sighting.ribbonsCount = sighting.ribbonsCount + 1;
            handler.post(run_updateScoreboard);
            return;
        }
        if(s.equals("great_shot"))
        {
            CheckedTextView checkedtextview2 = (CheckedTextView)findViewById(R.id.label_sb_shot_qty);
            currentReview.greatShot = checkedtextview2.isChecked();
            currentReview.greatShotsCount = jsonobject.getInt("count");
            handler.post(run_updateScoreboardForCurrentReview);
            return;
        }
        if(s.equals("great_find"))
        {
            CheckedTextView checkedtextview3 = (CheckedTextView)findViewById(R.id.label_sb_find_qty);
            currentReview.greatFind = checkedtextview3.isChecked();
            currentReview.greatFindsCount = jsonobject.getInt("count");
            handler.post(run_updateScoreboardForCurrentReview);
            return;
        }
        if(s.equals("unauthorized"))
        {
            ((HomeActivity)getParent()).showAuthenticationActivity(false);
            return;
        }
        if(s.equals("review"))
        {
            Log.i("ReviewActivity", "ACTION: LOADED REVIEW..");
            Object obj = jsonobject.opt("review");
            if(obj == null)
                return;
            StringBuilder stringbuilder = (new StringBuilder()).append("finishedAction: REVIEW: Got review ");
            String s2 = stringbuilder.append((Review)obj).toString();
            Log.i("ReviewActivity", s2);
            currentReview = (Review)obj;
            currentReview.delegate = this;
            currentReview.sighting = sighting;
            loadedReview = true;
            Runnable runnable4 = new Runnable() {

                public void run()
                {
                    updateScoreboardForCurrentReview();
                    enableScoreboard();
                }
            };
            handler.post(runnable4);
            if(currentReview.note != null && currentReview.note.length() > 0)
                reviewHasNote = true;
            handler.post(run_refreshMetadata);
            if(currentReview.user != null)
            {
            	loadPersonID = currentReview.user.uid;
            }
            if(!loadedPerson && loadPersonID != 0)
                updatePerson();
            currentReview.loadCommentsAction();
            return;
        }
        if(s.equals("person-loaded"))
        {
            Log.i("ReviewActivity", "ACTION: LOADED PERSON...");
            loadedPerson = true;
            handler.post(run_refreshMetadata);
            return;
        }
        if(s.equals("comments-loaded"))
        {
            Log.i("ReviewActivity", "ACTION: LOADED COMMENTS..");
            loadedComments = true;
            hideLoadingView();
            if(reviewComments != null)
                reviewComments = null;
            if(currentReview != null && currentReview.commentsLoaded)
            {
            	reviewComments = currentReview.comments;
            	int i = 0;
            	if (reviewComments != null) {
            	while(i < reviewComments.size()) {
            		Log.i("ReviewActivity--iterate review comments ", i + ":" + reviewComments.get(i));
            		i++;
            	}
            	}
            } else
            {
            	reviewComments = new LinkedList();
            }
            handler.post(run_refreshReviews);
            // TODO temprorily
            //handler.post(run_refreshMetadata);
            return;
        }
        if(!s.equals("comment-added"))
        {
            return;
        } else
        {
            Log.i("ReviewActivity", "ACTION: ADDED COMMENTS..");
            currentReview.incrementCommentsCount();
            updateReviewComments();
            return;
        }
    }

    String generateToken(Bundle bundle)
    {
        String s;
        if(bundle == null)
        {
            s = null;
        } else
        {
            Sighting sighting1 = (Sighting)bundle.getParcelable("sighting");
            Review review = (Review)bundle.getParcelable("review");
            String s1;
            if(review == null)
            {
                s1 = sighting1.sightingID;
            } else
            {
                StringBuilder stringbuilder = new StringBuilder();
                String s2 = sighting1.sightingID;
                StringBuilder stringbuilder1 = stringbuilder.append(s2);
                int i = review.reviewID;
                s1 = stringbuilder1.append(i).toString();
            }
            s = s1;
        }
        return s;
    }

    public View[] getToolbarItems()
    {
        return null;
    }

    void initImpl()
    {
    	sightingReviews = new LinkedList();
    	sightingReviewsAdapter = new SightingReviewAdapter(this, R.layout.review_cell, sightingReviews);
    	reviewsTable = (GroupedTableView)findViewById(R.id.more_reviews_table);
        reviewsTable.setAdapter(sightingReviewsAdapter);
        reviewsTable.setOnItemClickListener(showReview);
        sightingReviewsLabel = findViewById(R.id.label_more_reviews);
        View view1 = findViewById(R.id.sb_want);
        view1.setOnClickListener(scoreboardButtonListener);
        View view2 = findViewById(R.id.sb_nom);
        view2.setOnClickListener(scoreboardButtonListener);
        loadedComments = false;
    }

    void initWithSighting(Sighting sighting1)
    {
        sighting = sighting1;
        currentReviewID = sighting1.currentReviewID;
        initImpl();
    }

    void initWithSingleSighting(Sighting sighting1)
    {
        sighting = sighting1;
        currentReviewID = sighting.currentReviewID;
        initImpl();
    }

    void initWithSingleSighting(Sighting sighting1, Review review)
    {
    	sighting = new Sighting(sighting1);
    	sighting.currentReviewID = review.reviewID;
    	sighting.currentReviewAt = review.takenAt;
    	sighting.user = review.user;
    	sighting.thumb280URL = review.thumb280URL;
        sighting.user.avatarURL = review.user.avatarURL;
        loadPersonID = review.user.uid;
        currentReviewID = review.reviewID;
        updatePerson();
        initImpl();
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.review);
    }

    public boolean onKeyDown(int i, KeyEvent keyevent)
    {
        boolean flag;
        if(i == 4)
            flag = false;
        else
            flag = super.onKeyDown(i, keyevent);
        return flag;
    }

    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle bundle = getIntent().getExtras();
        if(bundle == null)
            return;
        String s = bundle.getString("sighting_id");
        if(sighting == null)
            return;
        if(sighting.sightingID.equals(s))
        {
            return;
        } else
        {
            resetLoading();
            return;
        }
    }

	protected void onResume() {
		Bundle bundle;
		super.onResume();
		Log.i("ReviewActivity", "onResume");
		bundle = getIntent().getExtras();
		if (bundle == null) {
			if (!loadedReview && !loadedReviewsForSighting) {
				updateReview();
				populateReview();
			}
			if (!loadedPerson && loadPersonID > 0)
				updatePerson();
			if (currentReview == null) {
				return;
			} else {
				currentReview.delegate = this;
				return;
			}
		} else {
			Sighting sighting1;
			int j;
			if (uniqueToken != null) {
				String s = uniqueToken;
				String s1 = generateToken(bundle);
				if (s.equals(s1))
					return;
			}
			sighting1 = (Sighting) bundle.getParcelable("sighting");
			j = bundle.getInt("init_mode", 0);
			if (j != 0) {
				if (j == 1)
					initWithSingleSighting(sighting1);
				else if (j == 2) {
					Review review = (Review) bundle.getParcelable("review");
					initWithSingleSighting(sighting1, review);
				}
			} else {
				initWithSighting(sighting1);
			}
			uniqueToken = generateToken(bundle);
			if (!loadedReview && !loadedReviewsForSighting) {
				updateReview();
				populateReview();
			}
			if (!loadedPerson && loadPersonID > 0)
				updatePerson();
			if (currentReview == null) {
				return;
			} else {
				currentReview.delegate = this;
				return;
			}
		}
	}

    void populateReview()
    {
        Bitmap bitmap;
        String s;
        ImageView imageview;
        ImageDownloader imagedownloader;
        if(currentReview != null)
        {
            bitmap = currentReview.thumb280;
            s = currentReview.thumb280URL;
        } else
        {
            bitmap = sighting.thumb280;
            s = sighting.thumb280URL;
        }
        imageview = (ImageView)findViewById(R.id.img_photo);
        imagedownloader = Macros.FS_APPLICATION().imageDownloader;
        if(bitmap != null)
            imageview.setImageBitmap(bitmap);
        else
        if(s != null)
        {
            ((ProgressBar)findViewById(R.id.progress)).setVisibility(0);
            com.bocai.ImageDownloader.OnFinishListener onfinishlistener = new com.bocai.ImageDownloader.OnFinishListener() {

                public void onFinish(String s1, ImageView imageview1, Bitmap bitmap1)
                {
                    ((ProgressBar)findViewById(R.id.progress)).setVisibility(8);
                }
            };
            imagedownloader.download(s, imageview, onfinishlistener);
        }
        if(currentReview == null)
            return;
        if(currentReview.nommed)
        {
            ((ImageView)findViewById(R.id.ribbon)).setVisibility(0);
            return;
        } else
        {
            ((ImageView)findViewById(R.id.ribbon)).setVisibility(8);
            return;
        }
    }

    void reloadMetadataTable()
    {
        if(sighting == null)
            return;
        ReviewActivity reviewactivity = this;
        GroupedTableView groupedtableview = (GroupedTableView)reviewactivity.findViewById(R.id.metadata_table);
        groupedtableview.removeAllViews();
        android.widget.LinearLayout.LayoutParams layoutparams =  new android.widget.LinearLayout.LayoutParams(-1,-1);
        layoutparams.setMargins(4, 2, 2, 4);
        ImageDownloader imagedownloader = Macros.FS_APPLICATION().imageDownloader;
        LayoutInflater layoutinflater = LayoutInflater.from(this);
        groupedtableview.setOnItemClickListener(metadataTableListener);
        RelativeLayout relativelayout = (RelativeLayout)layoutinflater.inflate(R.layout.review_list_item, null);
        relativelayout.setClickable(false);
        TextView textview = (TextView)relativelayout.findViewById(R.id.title);
        TextView textview1 = (TextView)relativelayout.findViewById(R.id.subtitle);
        TextView textview2 = (TextView)relativelayout.findViewById(R.id.content);
        ImageView imageview = (ImageView)relativelayout.findViewById(R.id.image);
        ImageView imageview1 = (ImageView)relativelayout.findViewById(R.id.chevron);
        groupedtableview.addView(relativelayout, layoutparams);
        
        TextView textview7;
        Place place;
        ImageView imageview8;
        Item item;           
        StringBuilder stringbuilder;
        String s1;
        String s2;
        RelativeLayout relativelayout7;
        TextView textview6;  
        TextView textview8;
        ImageView imageview4;
        ImageView imageview5;
        RelativeLayout relativelayout15;
        TextView textview11;
        TextView textview12;
        TextView textview13;
        ImageView imageview9;  
        String s10;
        
        if(loadedPerson)
        {

            if(person.avatar != null)
            {
                imageview.setImageBitmap(person.avatar);
            } else if(person.avatarURL != null)
            {
                imagedownloader.download(person.avatarURL, imageview);
            }
            textview.setText(person.name);
        } else
        {
            if(currentReview != null && currentReview.user != null && currentReview.user.avatar != null)
            {
                imageview.setImageBitmap(currentReview.user.avatar);
            } else
            if(currentReview != null && currentReview.user != null && currentReview.user.avatarURL != null)
            {
                imagedownloader.download(currentReview.user.avatarURL, imageview);
            } else
            if(sighting != null && sighting.user != null && sighting.user.avatar != null)
            {
                imageview.setImageBitmap(sighting.user.avatar);
            } else
            if(sighting != null && sighting.user != null && sighting.user.avatarURL != null)
            {
                imagedownloader.download(sighting.user.avatarURL, imageview);
            }
            if(loadedReview && currentReview != null && currentReview.user != null)
            {
                textview.setText(currentReview.user.name);
            } else
            {
                textview.setText(sighting.user.name);
            }
        }
        if(loadedReview && currentReview != null && currentReview.takenAt != null)
        {
            stringbuilder = (new StringBuilder()).append(getString(R.string.spotted_this));
            s1 = DateUtilities.getRelativeDate(currentReview.takenAt);
            s2 = stringbuilder.append(s1).toString();
            textview1.setText(s2);
        } else
        {
            StringBuilder stringbuilder1 = (new StringBuilder()).append(getString(R.string.spotted_this));
            String s20 = DateUtilities.getRelativeDate(sighting.currentReviewAt);
            String s21 = stringbuilder1.append(s20).toString();
            textview1.setText(s21);
        }
        if(loadedReview && currentReview != null && currentReview.note != null && currentReview.note.length() > 0)
        {
            textview2.setText(currentReview.note);
            textview2.setVisibility(0);
        } else
        {
            textview2.setVisibility(8);
        }
        imageview1.setVisibility(8);
        relativelayout7 = (RelativeLayout)layoutinflater.inflate(R.layout.review_list_item, null);
        relativelayout7.setClickable(true);
        relativelayout7.setPadding(30, 30, 30, 30);
        textview6 = (TextView)relativelayout7.findViewById(R.id.title);
        textview7 = (TextView)relativelayout7.findViewById(R.id.subtitle);
        textview8 = (TextView)relativelayout7.findViewById(R.id.content);
        imageview4 = (ImageView)relativelayout7.findViewById(R.id.image);
        imageview5 = (ImageView)relativelayout7.findViewById(R.id.chevron);
        groupedtableview.addView(relativelayout7, layoutparams);
        if(loadedReview && currentReview != null)
            place = currentReview.place;
        else
            place = sighting.place;
        imageview4.setImageResource(R.drawable.place_placeholder);
        textview6.setText(place.name);
        if(place.fullAddress != null && place.fullAddress.length() > 0)
        {
            textview7.setText(place.fullAddress);
        } else
        if(place.address != null && place.address.length() > 0)
        {
            textview7.setText(place.address);
        } else
        {
            textview7.setText(getString(R.string.unknown_address));
        }
        textview8.setVisibility(8);
        imageview5.setVisibility(0);
        relativelayout15 = (RelativeLayout)layoutinflater.inflate(R.layout.review_list_item, null);
        relativelayout15.setClickable(true);
        textview11 = (TextView)relativelayout15.findViewById(R.id.title);
        textview12 = (TextView)relativelayout15.findViewById(R.id.subtitle);
        textview13 = (TextView)relativelayout15.findViewById(R.id.content);
        imageview8 = (ImageView)relativelayout15.findViewById(R.id.image);
        imageview9 = (ImageView)relativelayout15.findViewById(R.id.chevron);
        groupedtableview.addView(relativelayout15, layoutparams);
        if(sighting.thumb90 != null)
        {
            imageview8.setImageBitmap(sighting.thumb90);
        } else
        {
            imagedownloader.download(sighting.thumb90URL, imageview8);
        }
        if(loadedReview)
            item = currentReview.item;
        else
            item = sighting.item;
        textview11.setText(item.name);
        s10 = this.getString(R.string.review_search_food);
        textview12.setText(s10);
        textview13.setVisibility(8);
        imageview9.setVisibility(0);
    }

    void resetLoading()
    {
        CheckedTextView checkedtextview = (CheckedTextView)findViewById(R.id.label_sb_want_qty);
        checkedtextview.setText("");
        checkedtextview.setChecked(false);
        Drawable drawable = checkedtextview.getCompoundDrawables()[0];
        int ai[] = checkedtextview.getDrawableState();
        drawable.setState(ai);
        
        CheckedTextView checkedtextview1 = (CheckedTextView)findViewById(R.id.label_sb_nom_qty);
        checkedtextview1.setText("");
        checkedtextview1.setChecked(false);
        Drawable drawable1 = checkedtextview1.getCompoundDrawables()[0];
        int ai1[] = checkedtextview1.getDrawableState();
        drawable1.setState(ai1);
           
        sighting.cancelRequests();
        if(currentReview != null)
        {
            currentReview.cancelRequests();
            currentReview.delegate = null;
            currentReview = null;
        }
        if(person != null)
        {
            person.cancelRequests();
            person.delegate = null;
            person = null;
        }
        if(sightingReviews != null)
        {
            sightingReviews.clear();
            sightingReviews = null;
        }
        reviewsTable.setVisibility(8);
        sightingReviewsLabel.setVisibility(8);
        loadedReview = false;
        loadedReviewsForSighting = false;
        loadedPerson = false;
        loadedComments = false;
        reviewHasNote = false;
        loadPersonID = 0;
        ((ScrollView)findViewById(R.id.scroll)).scrollTo(0, 0);
        ((GroupedTableView)findViewById(R.id.metadata_table)).removeAllViews();
        ((ImageView)findViewById(R.id.ribbon)).setVisibility(8);
    }

    void showPlaceDetail()
    {
        Log.i(LOG_TAG, "showPlaceDetail method");
    	Intent intent = new Intent(this, com.bocai.PlaceDetailActivity.class);
        intent.setFlags(0x20000000);
        Place place;
        Activity activity;
        if(loadedReview)
            place = currentReview.place;
        else
            place = sighting.place;
        intent.putExtra("place", place);
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

    void toggleCheck(CheckedTextView checkedtextview, boolean flag, int i)
    {
        boolean flag1;
        Drawable drawable;
        int ai[];
        if(!flag)
            flag1 = true;
        else
            flag1 = false;
        checkedtextview.setChecked(flag1);
        if(flag1)
        {
            String s = Integer.toString(i + 1);
            checkedtextview.setText(s);
        } else
        {
            int j = i - 1;
            String s1 = Integer.toString(Math.max(0, j));
            checkedtextview.setText(s1);
        }
        drawable = checkedtextview.getCompoundDrawables()[0];
        ai = checkedtextview.getDrawableState();
        drawable.setState(ai);
    }

    void updateOtherReviews()
    {
//        sighting.delegate = this;
//        sighting.loadReviews();
    	//NOTE: 1.don't load other spot within aggSpot
    	//2. load other spot with the same place id..
    	
    	Log.i(LOG_TAG, "updateOtherReview method");
    	sighting.place.delegate = this;
    	sighting.place.listSighting(0, 10);
    }

    void updatePerson()
    {
        Log.i("ReviewActivity", "updating person..");
        person = new Person();
        person.loadPerson(loadPersonID);
        person.delegate = this;
    }

    void updateReview()
    {
        disableScoreboard();
        if(!loadedReview)
        {
            Log.i("ReviewActivity", "UPDATE REVIEW: loading review object");
            sighting.delegate = this;
            sighting.loadReview();
        }
        updateScoreboard();
        Log.i("ReviewActivity", "UPDATE REVIEW COMMENTS..");
        updateReviewComments();
        if(!loadedReviewsForSighting)
        {
            Log.i("ReviewActivity", "UPDATE REVIEW: loading all reviews");
            updateOtherReviews();
            return;
        } else
        {
            Log.i("ReviewActivity", "UPDATE REVIEW: already loaded all reviews");
            return;
        }
    }

    void updateReviewComments()
    {
        if(currentReview != null)
        {
            currentReview.loadCommentsAction();
            return;
        } else
        {
            Log.i("ReviewActivity", "NO CURRENT REVIEW: not requesting comments yet");
            return;
        }
    }

    void updateScoreboard()
    {
        CheckedTextView checkedtextview = (CheckedTextView)findViewById(R.id.label_sb_want_qty);
        checkedtextview.setText(Integer.toString(sighting.wantsCount));
        checkedtextview.setChecked(sighting.wanted);
        Drawable drawable = checkedtextview.getCompoundDrawables()[0];
        drawable.setState(checkedtextview.getDrawableState());
        checkedtextview.setEnabled(true);
        findViewById(R.id.sb_want).setClickable(true);
        CheckedTextView checkedtextview1 = (CheckedTextView)findViewById(R.id.label_sb_nom_qty);
        checkedtextview1.setText(Integer.toString(sighting.ribbonsCount));
        checkedtextview1.setChecked(sighting.nommed);
        Drawable drawable1 = checkedtextview1.getCompoundDrawables()[0];
        drawable1.setState(checkedtextview1.getDrawableState());
        checkedtextview1.setEnabled(true);
        findViewById(R.id.sb_nom).setClickable(true);
    }

    void updateScoreboardForCurrentReview()
    {

        if(currentReview.nommed)
            ((ImageView)findViewById(R.id.ribbon)).setVisibility(0);
        else
            ((ImageView)findViewById(R.id.ribbon)).setVisibility(8);
 
    }

    public static final String INIT_MODE = "init_mode";
    private static final String LOG_TAG = "ReviewActivity";
    public static final int MODE_DEFAULT = 0;
    public static final int MODE_SINGLE_SIGHTING = 1;
    public static final int MODE_SINGLE_SIGHTING_AND_REVIEW = 2;
    public static final String REVIEW = "review";
    public static final String SIGHTING = "sighting";
    public static final String SIGHTING_ID = "sighting_id";
    Review currentReview;
    int currentReviewID;
    final Handler handler;
    int loadPersonID;
    boolean loadedComments;
    boolean loadedPerson;
    boolean loadedReview;
    boolean loadedReviewsForSighting;
    com.bocai.widget.GroupedTableView.OnItemClickListener metadataTableListener;
    Person person;
    List reviewComments;
    boolean reviewHasNote;
    GroupedTableView reviewsTable;
    final Runnable run_refreshMetadata;
    final Runnable run_refreshReviews;
    final Runnable run_updateScoreboard;
    final Runnable run_updateScoreboardForCurrentReview;
    android.view.View.OnClickListener scoreboardButtonListener;
    com.bocai.widget.GroupedTableView.OnItemClickListener showReview;
    Sighting sighting;
    List sightingReviews;
    SightingReviewAdapter sightingReviewsAdapter;
    View sightingReviewsLabel;
    boolean toggleNommed;
    String uniqueToken;
}
