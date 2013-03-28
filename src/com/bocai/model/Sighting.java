// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Sighting.java

package com.bocai.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.bocai.R;
import com.bocai.net.AsyncHTTPRequest;
import com.bocai.net.FileCache;
import com.bocai.util.DateUtilities;
import com.bocai.util.Macros;
import java.io.File;
import java.text.*;
import java.util.*;
import org.json.*;

// Referenced classes of package com.bocai.model:
//            FSObject, User, Place, Item, 
//            Filter, Review, FSObjectDelegate, Promo

public class Sighting extends FSObject implements Parcelable {
	
	public static interface OnDecodeCompleteListener {

		public abstract void onDecodeComplete(Sighting sighting, Bitmap bitmap,
				Object obj);
	}
	
	class DecoderThread extends Thread {

		String imgURL;
		OnDecodeCompleteListener listener;
		Object userData;

		DecoderThread() {
			super();
			
			imgURL = null;
			listener = null;
			userData = null;
		}
		
		public void run() {
			Bitmap bitmap = null;
			bitmap = BitmapFactory.decodeFile(FileCache.getURL(imgURL)
					.getAbsolutePath());

			if (listener != null && bitmap != null) {
				listener.onDecodeComplete(Sighting.this, bitmap, userData);
			}
			imgURL = null;
			listener = null;
			userData = null;
			decoderThread = null;
		}


	}


	public Sighting() {
		latitude = 4.9406564584124654E-324D;
		longitude = 4.9406564584124654E-324D;
		distance = 4.9406564584124654E-324D;
		loadingReviews = false;
		
		reviewResponseData = new FSObject.ResponseDataHandler() {

			public void responseData(JSONObject jsonobject,
					AsyncHTTPRequest asynchttprequest) throws JSONException {
				if (jsonobject == null || jsonobject.length() == 0) {
					Log.d("Sighting", "*** bad review");
					if (delegate == null) {
						return;
					} else {
						Map map = Macros.ACTION_REVIEW(null);
						JSONObject jsonobject1 = new JSONObject(map);
						delegate.finishedAction(jsonobject1);
						return;
					}
				}
				Review review = new Review(jsonobject);
				if (delegate != null) {
					java.util.Map map1 = Macros.ACTION_REVIEW(review);
					JSONObject jsonobject2 = new JSONObject(map1);
					delegate.finishedAction(jsonobject2);
				}
				StringBuilder stringbuilder = (new StringBuilder())
						.append("DONE WITH REVIEW REPSONSE DATA : ");
				String s = asynchttprequest.url;
				String s1 = stringbuilder.append(s).toString();
				Log.d("Sighting", s1);
			}
		};

		decoderThread = null;
	}

	private Sighting(Parcel parcel) {
		this();

		latitude = 4.9406564584124654E-324D;
		longitude = 4.9406564584124654E-324D;
		distance = 4.9406564584124654E-324D;
		loadingReviews = false;

		decoderThread = null;
		user = (User) parcel.readParcelable(null);

		place = (Place) parcel.readParcelable(null);
		item = (Item) parcel.readParcelable(null);

		sightingID = parcel.readString();

		thumb32URL = parcel.readString();

		thumb32 = (Bitmap) parcel.readParcelable(null);
		
		thumb90URL = parcel.readString();

		thumb90 = (Bitmap) parcel.readParcelable(null);

		thumb280URL = parcel.readString();

		thumb280 = (Bitmap) parcel.readParcelable(null);

		latitude = parcel.readDouble();

		longitude = parcel.readDouble();

		distance = parcel.readDouble();

		ribbonsCount = parcel.readInt();

		reviewsCount = parcel.readInt();

		wantsCount = parcel.readInt();

		if (parcel.readByte() == 1)
			nommed = true;
		else
			nommed = false;

		if (parcel.readByte() == 1)
			wanted = true;
		else
			wanted = false;

		currentReviewID = parcel.readInt();
		lastReviewAt = new Date(parcel.readLong());

		currentReviewAt = new Date(parcel.readLong());

		detailInfo = parcel.readString();

	}

	public Sighting(Sighting sighting) {
		this();

		latitude = 4.9406564584124654E-324D;
		longitude = 4.9406564584124654E-324D;
		distance = 4.9406564584124654E-324D;
		loadingReviews = false;
		decoderThread = null;
		user = sighting.user;

		place = sighting.place;

		item = sighting.item;
		sightingID = sighting.sightingID;
		thumb32URL = sighting.thumb32URL;
		thumb32 = sighting.thumb32;
		thumb90URL = sighting.thumb90URL;
		thumb90 = sighting.thumb90;
		thumb280URL = sighting.thumb280URL;
		thumb280 = sighting.thumb280;
		latitude = sighting.latitude;

		longitude = sighting.longitude;

		distance = sighting.distance;

		ribbonsCount = sighting.ribbonsCount;
		reviewsCount = sighting.reviewsCount;
		wantsCount = sighting.wantsCount;
		nommed = sighting.nommed;
		wanted = sighting.wanted;
		currentReviewID = sighting.currentReviewID;
		lastReviewAt = sighting.lastReviewAt;
		currentReviewAt = sighting.currentReviewAt;
		detailInfo = sighting.detailInfo;
	}

	public Sighting(AsyncHTTPRequest asynchttprequest) {
		super(asynchttprequest);
		latitude = 4.9406564584124654E-324D;
		longitude = 4.9406564584124654E-324D;
		distance = 4.9406564584124654E-324D;
		loadingReviews = false;

		reviewResponseData = new FSObject.ResponseDataHandler() {

			public void responseData(JSONObject jsonobject,
					AsyncHTTPRequest asynchttprequest) throws JSONException {
				if (jsonobject == null || jsonobject.length() == 0) {
					Log.d("Sighting", "*** bad review");
					if (delegate == null) {
						return;
					} else {
						FSObjectDelegate fsobjectdelegate = delegate;
						java.util.Map map = Macros.ACTION_REVIEW(null);
						JSONObject jsonobject1 = new JSONObject(map);
						fsobjectdelegate.finishedAction(jsonobject1);
						return;
					}
				}
				Review review = new Review(jsonobject);
				if (delegate != null) {
					FSObjectDelegate fsobjectdelegate1 = delegate;
					java.util.Map map1 = Macros.ACTION_REVIEW(review);
					JSONObject jsonobject2 = new JSONObject(map1);
					fsobjectdelegate1.finishedAction(jsonobject2);
				}
				StringBuilder stringbuilder = (new StringBuilder())
						.append("DONE WITH REVIEW REPSONSE DATA : ");
				String s = asynchttprequest.url;
				String s1 = stringbuilder.append(s).toString();
				int j = Log.d("Sighting", s1);
			}
		};

		decoderThread = null;
	}

	public Sighting(JSONObject jsonobject) {
		latitude = 4.9406564584124654E-324D;
		longitude = 4.9406564584124654E-324D;
		distance = 4.9406564584124654E-324D;
		loadingReviews = false;
		// FSObject.ResponseDataHandler responsedatahandler = new _cls1();
		// reviewResponseData = responsedatahandler;
		reviewResponseData = new FSObject.ResponseDataHandler() {

			public void responseData(JSONObject jsonobject,
					AsyncHTTPRequest asynchttprequest) throws JSONException {
				if (jsonobject == null || jsonobject.length() == 0) {
					int i = Log.d("Sighting", "*** bad review");
					if (delegate == null) {
						return;
					} else {
						FSObjectDelegate fsobjectdelegate = delegate;
						java.util.Map map = Macros.ACTION_REVIEW(null);
						JSONObject jsonobject1 = new JSONObject(map);
						fsobjectdelegate.finishedAction(jsonobject1);
						return;
					}
				}
				Review review = new Review(jsonobject);
				if (delegate != null) {
					FSObjectDelegate fsobjectdelegate1 = delegate;
					java.util.Map map1 = Macros.ACTION_REVIEW(review);
					JSONObject jsonobject2 = new JSONObject(map1);
					fsobjectdelegate1.finishedAction(jsonobject2);
				}
				StringBuilder stringbuilder = (new StringBuilder())
						.append("DONE WITH REVIEW REPSONSE DATA : ");
				String s = asynchttprequest.url;
				String s1 = stringbuilder.append(s).toString();
				int j = Log.d("Sighting", s1);
			}
		};

		decoderThread = null;
		Object obj = jsonobject.opt("id");
		// if(obj == null)
		// break MISSING_BLOCK_LABEL_786;
		Object obj1 = JSONObject.NULL;
		// if(obj == obj1)
		// break MISSING_BLOCK_LABEL_786;
		StringBuilder stringbuilder = (new StringBuilder()).append("");
		Object obj2 = jsonobject.opt("id");
		String s = stringbuilder.append(obj2).toString();
		sightingID = s;
		// _L1:
		JSONObject jsonobject1 = jsonobject.optJSONObject("item");
		Item item1 = new Item(jsonobject1);
		item = item1;
		JSONObject jsonobject2 = jsonobject.optJSONObject("place");
		Place place1 = new Place(jsonobject2);
		place = place1;
		JSONObject jsonobject3 = jsonobject.optJSONObject("person");
		User user1 = new User(jsonobject3);
		user = user1;
		Object obj3 = jsonobject.opt("latitude");
		if (obj3 != null) {
			Object obj4 = JSONObject.NULL;
			if (obj3 != obj4) {
				//double d = ((Double) obj3).doubleValue();
				double d = new Double(obj3.toString()).doubleValue();
				latitude = d;
			}
		}
		Object obj5 = jsonobject.opt("longitude");
		if (obj5 != null) {
			Object obj6 = JSONObject.NULL;
			if (obj5 != obj6) {
				//double d1 = ((Double) obj5).doubleValue();
				double d1 = new Double(obj5.toString()).doubleValue();
				longitude = d1;
			}
		}
		Object obj7 = jsonobject.opt("distance");
		if (obj7 != null) {
			Object obj8 = JSONObject.NULL;
			if (obj7 != obj8) {
				double d2 = ((Double) obj7).doubleValue();
				distance = d2;
			}
		}
		Object obj9 = jsonobject.opt("thumb_90");
		if (obj9 != null) {
			Object obj10 = JSONObject.NULL;
			if (obj9 != obj10) {
				String s1 = (String) obj9;
				thumb90URL = s1;
			}
		}
		Object obj11 = jsonobject.opt("thumb_32");
		if (obj11 != null) {
			Object obj12 = JSONObject.NULL;
			if (obj11 != obj12) {
				String s2 = (String) obj11;
				thumb32URL = s2;
			}
		}
		Object obj13 = jsonobject.opt("thumb_280");
		if (obj13 != null) {
			Object obj14 = JSONObject.NULL;
			if (obj13 != obj14) {
				String s3 = (String) obj13;
				thumb280URL = s3;
			}
		}
		Object obj15 = jsonobject.opt("ribbons_count");
		if (obj15 != null) {
			Object obj16 = JSONObject.NULL;
			if (obj15 != obj16) {
				int i = ((Integer) obj15).intValue();
				ribbonsCount = i;
			}
		}
		Object obj17 = jsonobject.opt("reviews_count");
		if (obj17 != null) {
			Object obj18 = JSONObject.NULL;
			if (obj17 != obj18) {
				int j = ((Integer) obj17).intValue();
				reviewsCount = j;
			}
		}
		Object obj19 = jsonobject.opt("wants_count");
		if (obj19 != null) {
			Object obj20 = JSONObject.NULL;
			if (obj19 != obj20) {
				int k = ((Integer) obj19).intValue();
				wantsCount = k;
			}
		}
		Object obj21 = jsonobject.opt("nommed");
		if (obj21 != null) {
			Object obj22 = JSONObject.NULL;
			if (obj21 != obj22) {
				boolean flag = ((Boolean) obj21).booleanValue();
				nommed = flag;
			}
		}
		Object obj23 = jsonobject.opt("wanted");
		if (obj23 != null) {
			Object obj24 = JSONObject.NULL;
			if (obj23 != obj24) {
				boolean flag1 = ((Boolean) obj23).booleanValue();
				wanted = flag1;
			}
		}
		Object obj25 = jsonobject.opt("review_id");
		if (obj25 != null) {
			Object obj26 = JSONObject.NULL;
			if (obj25 != obj26) {
				int l = ((Integer) obj25).intValue();
				currentReviewID = l;
			}
		}
		
		long last_review_at = jsonobject.optLong("last_review_at");
		lastReviewAt = new Date(last_review_at);
		
		long taken_at = jsonobject.optLong("current_review_taken_at");
		currentReviewAt = new Date(taken_at);
		
		int i1 = Filter.filterSort();
		setSearchFilterSort(i1);
		return;
	}

	public static AsyncHTTPRequest listRequestWithParameters(HashMap hashmap) {
		return FSObject.requestWithPath("sightings", hashmap);
	}

	public boolean decodeThumb280(
			OnDecodeCompleteListener ondecodecompletelistener, Object obj) {
		Log.i(LOG_TAG, "decodeThumb280 method");
		boolean flag;
		if (decoderThread != null)
			flag = true;
		else if (thumb280URL != null && FileCache.urlExists(thumb280URL)) {
			Log.i(LOG_TAG, "thumb280URL exists in FileCache,just decode it!");
			decoderThread = new DecoderThread();
			decoderThread.imgURL = thumb280URL;
			decoderThread.listener = ondecodecompletelistener;
			decoderThread.userData = obj;
			decoderThread.start();
			flag = true;
		} else {
			flag = false;
		}
		return flag;

	}

	public int describeContents() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Bitmap getThumb32() {
		Bitmap bitmap;
		if (thumb32 != null)
			bitmap = thumb32;
		else if (FileCache.urlExists(thumb32URL))
			bitmap = BitmapFactory.decodeFile(FileCache.getURL(thumb32URL)
					.getAbsolutePath());
		else
			bitmap = null;
		return bitmap;

	}

	public Bitmap getThumb280() {
		Bitmap bitmap;
		if (thumb280 != null)
			bitmap = thumb280;
		else if (FileCache.urlExists(thumb280URL))
			bitmap = BitmapFactory.decodeFile(FileCache.getURL(thumb280URL)
					.getAbsolutePath());
		else
			bitmap = null;
		return bitmap;
	}

	public Bitmap getThumb90() {
		Bitmap bitmap = null;
		
		if (thumb90 != null)
			bitmap = thumb90;
		else if (FileCache.urlExists(thumb90URL))
			bitmap = BitmapFactory.decodeFile(FileCache.getURL(thumb90URL)
					.getAbsolutePath());
		else
			bitmap = null;
		return bitmap;
	}

	public void loadReview() {
		loadingReviews = true;
		AsyncHTTPRequest asynchttprequest = Review.reviewRequestWithReviewID(
				Integer.toString(currentReviewID), null);
		FSObject.ResponseDataHandler responsedatahandler = reviewResponseData;
		performRequest(asynchttprequest, null, responsedatahandler);
		Log.i(LOG_TAG, "loadReview with url=" + asynchttprequest.url);
	}

	public void loadReviews() {
		loadingReviews = true;
		AsyncHTTPRequest asynchttprequest = Review.reviewsRequestWithID(
				sightingID, null);
		performRequest(asynchttprequest);
	}

	public Location location() {
		Location location2;
		if (latitude != 4.9406564584124654E-324D
				&& longitude != 4.9406564584124654E-324D) {
			Location location1 = new Location("fsservice");
			double d = latitude;
			location1.setLatitude(d);
			double d1 = longitude;
			location1.setLongitude(d1);
			location2 = location1;
		} else {
			location2 = null;
		}
		return location2;
	}

	protected void responseData(JSONObject jsonobject,
			AsyncHTTPRequest asynchttprequest) throws JSONException {
		
		Log.i(LOG_TAG, "responseData method");
		Log.i(LOG_TAG, "url==========" + asynchttprequest.url);
		Log.i(LOG_TAG, "response==========" + jsonobject.toString());
		
		if (jsonobject == null) {
			if (delegate == null) {
				return;
			} else {
				delegate.finishedAction(null);
				delegate.FSResponse(null);
				return;
			}
		}
		
	//	Log.i("Sighting", "*******responseData************ " + jsonobject.toString());

		JSONArray jsonarray = jsonobject.optJSONArray("data");
		if (jsonarray == null && delegate != null){
			delegate.FSResponse(null);
			return;
		}
		
		LinkedList linkedlist = new LinkedList();
		if (loadingReviews) {
			int i = jsonarray.length();
			int k = 0;
			while (k < i) {
				JSONObject jsonobject1 = jsonarray.optJSONObject(k);
				if (jsonobject1 != null) {
					Review review = new Review(jsonobject1);
					linkedlist.add(review);
				} else {
					StringBuilder stringbuilder = (new StringBuilder())
							.append("responseData: Response object unknown! Object: ");
					Object obj = jsonarray.opt(k);
					String s = stringbuilder.append(obj).toString();
					int i1 = Log.w("Sighting", s);
				}
				k++;
			}
			loadingReviews = false;
		} else {
			int j = jsonarray.length();
			int l = 0;
			while (l < j) {
				JSONObject jsonobject3 = jsonarray.optJSONObject(l);
				if (jsonobject3 != null) {
					if (Promo.isPromo(jsonobject3)) {
						Promo promo = new Promo(jsonobject3);
						linkedlist.add(promo);
					} else {
						Sighting sighting = new Sighting(jsonobject3);
						linkedlist.add(sighting);
					}
				} else {
					StringBuilder stringbuilder1 = (new StringBuilder())
							.append("responseData: Response object unknown! Object: ");
					Object obj1 = jsonarray.opt(l);
					String s1 = stringbuilder1.append(obj1).toString();
					int j1 = Log.w("Sighting", s1);
				}
				l++;
			}
		}
		if (delegate != null) {
			Map map = Macros.ACTION_PAGES(jsonobject.get("total"));
			JSONObject jsonobject2 = new JSONObject(map);
			delegate.finishedAction(jsonobject2);
			delegate.FSResponse(linkedlist);
		}
	}

	public void setSearchFilterSort(int i) {
		if (detailInfo != null)
			detailInfo = null;
		switch (i) {
		default:
			return;

		case 1: // '\001'
			if (distance == 4.9406564584124654E-324D) {
				return;
			} else {
				if(distance >= 1000.0){
					DecimalFormat decimalformat = new DecimalFormat("#####.##");
					StringBuilder stringbuilder = new StringBuilder();
					String s = decimalformat.format(distance / 1000.0);
					detailInfo = stringbuilder.append(s).append(" ").append(Macros.FS_APPLICATION().getString(R.string.km)).toString();
				}else{
					StringBuilder stringbuilder = new StringBuilder();
					int m = (int)distance;
					m = (m/10 + 1) * 10;
					detailInfo = stringbuilder.append(m).append(" ").append(Macros.FS_APPLICATION().getString(R.string.m)).toString();
				}
				return;
			}

		case 2: // '\002'
			detailInfo = DateUtilities.getRelativeDate(lastReviewAt);
			return;

		case 3: // '\003'
			break;
		}
		if (ribbonsCount == 1) {
			detailInfo = "1 " + Macros.FS_APPLICATION().getString(R.string.nom);
			return;
		} else {
			StringBuilder stringbuilder1 = new StringBuilder();
			int j = ribbonsCount;
			String s3 = stringbuilder1.append(j).append(" ").append(Macros.FS_APPLICATION().getString(R.string.noms)).toString();
			detailInfo = s3;
			return;
		}
	}

	public String toString() {
		Object aobj[] = new Object[10];
		String s = sightingID;
		aobj[0] = s;
		Item item1 = item;
		aobj[1] = item1;
		Place place1 = place;
		aobj[2] = place1;
		User user1 = user;
		aobj[3] = user1;
		Date date = lastReviewAt;
		aobj[4] = date;
		Double double1 = Double.valueOf(longitude);
		aobj[5] = double1;
		Double double2 = Double.valueOf(latitude);
		aobj[6] = double2;
		Boolean boolean1 = Boolean.valueOf(wanted);
		aobj[7] = boolean1;
		Boolean boolean2 = Boolean.valueOf(nommed);
		aobj[8] = boolean2;
		Integer integer = Integer.valueOf(currentReviewID);
		aobj[9] = integer;
		return String
				.format("{[Sighting id %s] item: %s place: %s user: %s lastReviewAt: %s lng: %f lat: %f wanted: %b nommed: %b, currentReviewID: %d}",
						aobj);
	}

	public AsyncHTTPRequest viewRequestWithID(String s) {
		return FSObject
				.requestWithPath((new StringBuilder()).append("sightings/")
						.append(s).toString(), null);
	}

	public void writeToParcel(Parcel parcel, int i) {
		User user1 = user;
		parcel.writeParcelable(user1, i);
		Place place1 = place;
		parcel.writeParcelable(place1, i);
		Item item1 = item;
		parcel.writeParcelable(item1, i);
		String s = sightingID;
		parcel.writeString(s);
		
		String s2 = thumb32URL;
		parcel.writeString(s2);
		Bitmap bitmap1 = thumb32;
		parcel.writeParcelable(bitmap1, i);
		
		String s1 = thumb90URL;
		parcel.writeString(s1);
		Bitmap bitmap = thumb90;
		parcel.writeParcelable(bitmap, i);
		
		String s3 = thumb280URL;
		parcel.writeString(s3);
		Bitmap bitmap2 = thumb280;
		parcel.writeParcelable(bitmap2, i);
		double d = latitude;
		parcel.writeDouble(d);
		double d1 = longitude;
		parcel.writeDouble(d1);
		double d2 = distance;
		parcel.writeDouble(d2);
		int j = ribbonsCount;
		parcel.writeInt(j);
		int k = reviewsCount;
		parcel.writeInt(k);
		int l = wantsCount;
		parcel.writeInt(l);
		int i1;
		byte byte0;
		int j1;
		byte byte1;
		int k1;
		long l1;
		long l2;
		String s4;
		if (nommed)
			i1 = 1;
		else
			i1 = 0;
		byte0 = (byte) i1;
		parcel.writeByte(byte0);
		if (wanted)
			j1 = 1;
		else
			j1 = 0;
		byte1 = (byte) j1;
		parcel.writeByte(byte1);
		k1 = currentReviewID;
		parcel.writeInt(k1);
		l1 = lastReviewAt.getTime();
		parcel.writeLong(l1);
		l2 = currentReviewAt.getTime();
		parcel.writeLong(l2);
		s4 = detailInfo;
		parcel.writeString(s4);
	}

	public static final android.os.Parcelable.Creator CREATOR = new android.os.Parcelable.Creator() {

		public Sighting createFromParcel(Parcel parcel) {
			return new Sighting(parcel);
		}

		/*
		 * public volatile Object createFromParcel(Parcel parcel) { return
		 * createFromParcel(parcel); }
		 */

		public Sighting[] newArray(int i) {
			return new Sighting[i];
		}

		/*
		 * public volatile Object[] newArray(int i) { return newArray(i); }
		 */

	};
	private static final String LOG_TAG = "Sighting";
	public Date currentReviewAt;
	public int currentReviewID;
	DecoderThread decoderThread;
	public String detailInfo;
	public double distance;
	public Item item;
	public Date lastReviewAt;
	public double latitude;
	boolean loadingReviews;
	public double longitude;
	public boolean nommed;
	public Place place;
	FSObject.ResponseDataHandler reviewResponseData;
	public int reviewsCount;
	public int ribbonsCount;
	public String sightingID;
	public Bitmap thumb280;
	public String thumb280URL;
	public Bitmap thumb90;
	public String thumb90URL;
	public Bitmap thumb32;
	public String thumb32URL;
	public User user;
	public boolean wanted;
	public int wantsCount;

}
