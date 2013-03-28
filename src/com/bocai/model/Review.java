// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Review.java

package com.bocai.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.bocai.BocaiApplication;
import com.bocai.net.AsyncHTTPRequest;
import com.bocai.net.AsyncHTTPResponseHandler;
import com.bocai.util.DateUtilities;
import com.bocai.util.Macros;
import com.bocai.util.RestConstants;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

// Referenced classes of package com.bocai.model:
//            FSObject, User, Place, Item, 
//            Sighting, ReviewComment, FSObjectDelegate

public class Review extends FSObject
    implements Parcelable
{

    public Review()
    {
        commentsLoaded = false;
        dirty = false;
        uploadResponseHandler = new AsyncHTTPResponseHandler() {

            public void handleError(String s, InputStream inputstream, long l)
                throws IOException
            {
                String s1 = (new StringBuilder()).append("uploadResponseHandler.handleError: ").append(s).toString();
                Log.i("Review", s1);
                if(delegate == null)
                    return;
                JSONArray jsonarray = new JSONArray();
                jsonarray.put(s);
                if(inputstream != null)
                {
                    String s2 = AsyncHTTPRequest.toString(inputstream, l);
                    jsonarray.put(s2);
                }
                JSONObject jsonobject = new JSONObject();
                try
                {
                    jsonobject.put("errors", jsonarray);
                    delegate.displayErrors(jsonobject);
                    return;
                }
                catch(JSONException jsonexception)
                {
                    return;
                }
            }

            public void handleResponse(AsyncHTTPRequest asynchttprequest, InputStream inputstream, long l)
                throws IOException
            {
                try
                {
                    String s = AsyncHTTPRequest.toString(inputstream, l);
                    JSONObject jsonobject = new JSONObject(s);
                    String s1 = (new StringBuilder()).append("uploadResponseHandler: response: ").append(jsonobject).toString();
                    Log.i("Review","uploadResponseHandler.response=" + s);
                    Log.i("Review", s1);

                    jsonobject.put("cookies", asynchttprequest.responseCookies);
                    boolean success = jsonobject.getBoolean("success");
                    if(!success){
                    	 Log.i("Review", "uploadResponseHandler: Clearing account...");
                         Macros.FS_APPLICATION().clearAccount("review-upload");
                         if(delegate == null)
                         {
                             return;
                         } else
                         {
                            
                             JSONObject jsonobject2 = new JSONObject(Macros.ACTION_DONE_UNAUTHORIZED());
                             delegate.finishedAction(jsonobject2);
                             delegate.displayErrors(jsonobject);
                             return;
                         }
                    }
                    
                    if(delegate == null)
                    {
                        return;
                    } else
                    {
                        delegate.displaySuccess(jsonobject);
                        return;
                    }
                }
                catch(JSONException jsonexception)
                {
                    StringBuilder stringbuilder = (new StringBuilder()).append("Error parsing upload response: ");
                    String s2 = jsonexception.getLocalizedMessage();
                    String s3 = stringbuilder.append(s2).toString();
                    Log.e("Review", s3, jsonexception);
                    jsonexception.printStackTrace();
                    return;
                }
            }
        };
        
        commentsActionResponseData = new FSObject.ResponseDataHandler() {

            public void responseData(JSONObject jsonobject, AsyncHTTPRequest asynchttprequest)
                throws JSONException
            {
                String s = (new StringBuilder()).append("ACTION RESPONSE DATA.. ").append(jsonobject).toString();
                Log.i("Review", s);
                if(jsonobject == null)
                {
                    delegate.FSResponse(null);
                    return;
                }
                JSONArray jsonarray = jsonobject.optJSONArray("data");
                LinkedList linkedlist = null;
                if(jsonarray != null && jsonarray.length() > 0)
                {
                    linkedlist = new LinkedList();
                    int j = jsonarray.length();
                    for(int k = 0; k < j; k++)
                    {
                        JSONObject jsonobject1 = jsonarray.getJSONObject(k);
                        ReviewComment reviewcomment = new ReviewComment(jsonobject1);
                        linkedlist.add(reviewcomment);
                    }

                }
                comments = linkedlist;
                commentsLoaded = true;
                if(delegate == null)
                {
                    return;
                } else
                {
                    JSONObject jsonobject2 = new JSONObject(Macros.ACTION_COMMENTS_LOADED());
                    delegate.finishedAction(jsonobject2);
                    return;
                }
            }
        };
    }

    private Review(Parcel parcel)
    {
    	this();
        //AsyncHTTPResponseHandler asynchttpresponsehandler = new _cls1();
        //uploadResponseHandler = asynchttpresponsehandler;
        //FSObject.ResponseDataHandler responsedatahandler = new _cls2();
        //commentsActionResponseData = responsedatahandler;
    	user = (User)parcel.readParcelable(null);
        
    	place = (Place)parcel.readParcelable(null);
       
    	item = (Item)parcel.readParcelable(null);
        
    	sighting = (Sighting)parcel.readParcelable(null);
        
    	reviewID = parcel.readInt();
        
    	thumb32URL = parcel.readString();
        
    	thumb32 = (Bitmap)parcel.readParcelable(null);
    	
    	thumb90URL = parcel.readString();
        
    	thumb90 = (Bitmap)parcel.readParcelable(null);
        
    	thumb280URL = parcel.readString();
        
    	thumb280 = (Bitmap)parcel.readParcelable(null);
        
        if(parcel.readByte() == 1)
        	nommed = true;
        else
        	nommed = false;
        
        if(parcel.readByte() == 1)
        	wanted = true;
        else
        	wanted = false;
       
        if(parcel.readByte() == 1)
        	greatShot = true;
        else
        	greatShot = false;
        
        if(parcel.readByte() == 1)
        	greatFind = true;
        else
        	greatFind = false;
        
        greatShotsCount = parcel.readInt();
       
        greatFindsCount = parcel.readInt();
        
        commentsCount = parcel.readInt();
        
       
        takenAt = new Date(parcel.readLong());
        
        
        createdAt = new Date(parcel.readLong());
        
        sightingID = parcel.readString();
        
        note = parcel.readString();
        
        parcel.readTypedList(comments, ReviewComment.CREATOR);
        
        if(parcel.readByte() == 1)
        	commentsLoaded = true;
        else
        	commentsLoaded = false;
       
        if(parcel.readByte() == 1)
        	dirty = true;
        else
        	dirty = false;
      
    }


    public Review(Item item1, Place place1)
    {
    	this();
/*        AsyncHTTPResponseHandler asynchttpresponsehandler = new _cls1();
        uploadResponseHandler = asynchttpresponsehandler;
        FSObject.ResponseDataHandler responsedatahandler = new _cls2();
        commentsActionResponseData = responsedatahandler;*/
        item = item1;
        place = place1;
        user = User.currentUser();
    }

    public Review(JSONObject jsonobject)
    {
    	this();
//        AsyncHTTPResponseHandler asynchttpresponsehandler = new _cls1();
//        uploadResponseHandler = asynchttpresponsehandler;
//        FSObject.ResponseDataHandler responsedatahandler = new _cls2();
//        commentsActionResponseData = responsedatahandler;
        if(jsonobject == null)
            return;
        Object obj = jsonobject.opt("item");
        if(obj != null)
        {
            if(obj != JSONObject.NULL)
            {
            	item = new Item((JSONObject)obj);
            }
        }
        Object obj2 = jsonobject.opt("place");
        if(obj2 != null)
        {
            if(obj2 != JSONObject.NULL)
            {
            	place = new Place((JSONObject)obj2);
            }
        }
        Object obj4 = jsonobject.opt("person");
        if(obj4 != null)
        {
            if(obj4 != JSONObject.NULL)
            {
            	user = new User((JSONObject)obj4);
            }
        }
        Object obj6 = jsonobject.opt("id");
        if(obj6 != null)
        {
            if(obj6 != JSONObject.NULL)
            {
            	reviewID = ((Integer)obj6).intValue();
            }
        }
        Object obj8 = jsonobject.opt("thumb_32");
        if(obj8 != null)
        {
            if(obj8 != JSONObject.NULL)
            {
            	thumb32URL = (String)obj8;
            }
        }
        
        Object obj9 = jsonobject.opt("thumb_90");
        if(obj9 != null)
        {
            if(obj9 != JSONObject.NULL)
            {
            	thumb90URL = (String)obj9;
            }
        }
       
        Object obj12 = jsonobject.opt("thumb_280");
        if(obj12 != null)
        {
            if(obj12 != JSONObject.NULL)
            {
            	thumb280URL = (String)obj12;
            }
        }
        Object obj14 = jsonobject.opt("nommed");
        if(obj14 != null)
        {
            if(obj14 != JSONObject.NULL)
            {
            	nommed = ((Boolean)obj14).booleanValue();
            }
        }
        Object obj16 = jsonobject.opt("wanted");
        if(obj16 != null)
        {
            if(obj16 != JSONObject.NULL)
            {
            	wanted = ((Boolean)obj16).booleanValue();
            }
        }
        Object obj18 = jsonobject.opt("great_shot");
        if(obj18 != null)
        {
            if(obj18 != JSONObject.NULL)
            {
            	greatShot = ((Boolean)obj18).booleanValue();
            }
        }
        Object obj20 = jsonobject.opt("great_find");
        if(obj20 != null)
        {
            if(obj20 != JSONObject.NULL)
            {
            	greatFind = ((Boolean)obj20).booleanValue();
            }
        }
        Object obj22 = jsonobject.opt("great_shots_count");
        if(obj22 != null)
        {
            if(obj22 != JSONObject.NULL)
            {
            	greatShotsCount = ((Integer)obj22).intValue();
            }
        }
        Object obj24 = jsonobject.opt("great_finds_count");
        if(obj24 != null)
        {
            if(obj24 != JSONObject.NULL)
            {
            	greatFindsCount = ((Integer)obj24).intValue();
            }
        }
        Object obj26 = jsonobject.opt("comments_count");
        if(obj26 != null)
        {
            if(obj26 != JSONObject.NULL)
            {
            	commentsCount = ((Integer)obj26).intValue();
            }
        }
        Object obj28 = jsonobject.opt("taken_at");
        if(obj28 != null)
        {
//            if(obj28 != JSONObject.NULL)
//                try
//                {
//                    SimpleDateFormat simpledateformat = DateUtilities.ISO8601Format;               
//                    takenAt = simpledateformat.parse((String)obj28);
//                }
//                catch(ParseException parseexception1) { }
        	takenAt = new Date((Long)obj28);
        }
        Object obj30;          
        Object obj32;      
        Object obj34;
        Object obj35;
      
        obj30 = jsonobject.opt("created_at");
        if(obj30 != null)
        {
            if(obj30 != JSONObject.NULL)
//                try
//                {
//                   
//                    createdAt = DateUtilities.ISO8601Format.parse((String)obj30);
//                	
//                }
//                // Misplaced declaration of an exception variable
//                catch(ParseException parseexception) { }
            	createdAt = new Date((Long)obj30);
        }
        obj32 = jsonobject.opt("note");
        if(obj32 != null)
        {
            if(obj32 != JSONObject.NULL)
            {
            	note = (String)obj32;
            }
        }
        obj34 = jsonobject.opt("sighting_id");
        if(obj34 == null)
            return;
        obj35 = JSONObject.NULL;
        if(obj34 == obj35)
        {
            return;
        } else
        {
        	sightingID = obj34.toString();
            return;
        }
    }

    public static AsyncHTTPRequest commentsForReviewId(int i)
    {
        return FSObject.requestWithPath((new StringBuilder()).append("reviews/").append(i).append("/comments").toString(), null);
    }

    public static AsyncHTTPRequest reviewRequestWithReviewID(String s, HashMap hashmap)
    {
        return FSObject.requestWithPath((new StringBuilder()).append("reviews/").append(s).toString(), hashmap);
    }

    public static AsyncHTTPRequest reviewsCommentRequestWithID(int i, String s, HashMap hashmap)
    {
        return FSObject.requestWithPath((new StringBuilder()).append("reviews/").append(i).append("/").append(s).toString(), hashmap);
    }

    public static AsyncHTTPRequest reviewsRequestWithID(String s, String s1, HashMap hashmap)
    {
        return FSObject.requestWithPath((new StringBuilder()).append("sightings/").append(s).append("/").append(s1).toString(), hashmap);
    }

    public static AsyncHTTPRequest reviewsRequestWithID(String s, HashMap hashmap)
    {
        return FSObject.requestWithPath((new StringBuilder()).append("sightings/").append(s).append("/reviews").toString(), hashmap);
    }

    public int describeContents()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementCommentsCount()
    {
        commentsCount++;
        dirty = true;
    }

    public void loadCommentsAction()
    {
        AsyncHTTPRequest asynchttprequest = commentsForReviewId(reviewID);
        performRequest(asynchttprequest, null, commentsActionResponseData);
    }

    public void performAction(String s)
    {
        HashMap hashmap = new HashMap();
        AsyncHTTPRequest asynchttprequest;
        List list;
        if(s.startsWith("great_"))
        {
            asynchttprequest = reviewsCommentRequestWithID(reviewID, s, hashmap);
        } else
        {
            asynchttprequest = reviewsRequestWithID(sightingID, s, hashmap);
            asynchttprequest.requestMethod = 2;
        }
        list = User.currentUser().cookies;
        performRequest(asynchttprequest, list);
    }

    protected void responseData(JSONObject jsonobject, AsyncHTTPRequest asynchttprequest)
        throws JSONException
    {
        Log.i(LOG_TAG, "responseData method");
        Log.i(LOG_TAG, "url==========" + asynchttprequest.url);
        Log.i(LOG_TAG, "response==========" + jsonobject.toString());
    	StringBuilder stringbuilder = (new StringBuilder()).append("responseData: request: ");
        String s = asynchttprequest.url;
        StringBuilder stringbuilder1 = stringbuilder.append(s).append(" response: ");
        String s1;
        String s2;
        String s3;
        int j;
        String s4;
        JSONObject jsonobject1;
        if(jsonobject != null)
            s1 = jsonobject.toString();
        else
            s1 = "null";
        s2 = stringbuilder1.append(s1).toString();
        Log.i("Review", s2);
        s3 = Uri.parse(asynchttprequest.url).getLastPathSegment();
        j = s3.length() - 5;
        s4 = s3.substring(0, j);
        jsonobject1 = jsonobject.put("action", s4);
        FSObjectDelegate fsobjectdelegate;
        java.util.Map map;
        try
        {
            if(jsonobject.getBoolean("success"))
                if(delegate == null)
                {
                    return;
                } else
                {
                    delegate.finishedAction(jsonobject);
                    return;
                }
        }
        catch(JSONException jsonexception)
        {
            StringBuilder stringbuilder2 = (new StringBuilder()).append("Error parsing response: ");
            String s5 = jsonexception.getLocalizedMessage();
            String s6 = stringbuilder2.append(s5).toString();
            Log.e("Review", s6, jsonexception);
            jsonexception.printStackTrace();
            return;
        }
        if(jsonobject.optInt("status", -1) != 403)
            return;
        Log.i("Review", "responseData: Clearing account...");
        Macros.FS_APPLICATION().clearAccount(s4);
        if(delegate == null)
        {
            return;
        } else
        {
            fsobjectdelegate = delegate;
            map = Macros.ACTION_DONE_UNAUTHORIZED();
            fsobjectdelegate.finishedAction(new JSONObject(map));
            return;
        }
    }

    public String toString()
    {
        Object aobj[] = new Object[7];
        Integer integer = Integer.valueOf(reviewID);
        aobj[0] = integer;
        Item item1 = item;
        aobj[1] = item1;
        Place place1 = place;
        aobj[2] = place1;
        User user1 = user;
        aobj[3] = user1;
        Date date = takenAt;
        aobj[4] = date;
        Boolean boolean1 = Boolean.valueOf(wanted);
        aobj[5] = boolean1;
        Boolean boolean2 = Boolean.valueOf(nommed);
        aobj[6] = boolean2;
        return String.format("{[Review] id: %d, item: %s place: %s user: %s takenAt: %s wanted: %b nommed: %b}", aobj);
    }

    public void upload(File imgFile, String comment,float price, boolean send2Sina)
    {
    	AsyncHTTPRequest asyncHttpRequest = new AsyncHTTPRequest(RestConstants.SPOT_URL);
        asyncHttpRequest.setDebug(true);
        asyncHttpRequest.setTimeout(60000);
        asyncHttpRequest.responseHandler = uploadResponseHandler;
        asyncHttpRequest.setUseCookiePersistence(true);
        if(user != null && user.cookies != null)
        {
           asyncHttpRequest.setRequestCookies(user.cookies);
        }
        String s1 = Integer.toString(user.uid);
        asyncHttpRequest.addPostParam("reviewUserId", s1);
      
        if(comment != null && comment.length() > 0){
            asyncHttpRequest.addPostParam("reviewNote", comment);
    	}
        asyncHttpRequest.addPostParam("reviewSource", "Android");
       
        if(place.id > 0)
        {
           asyncHttpRequest.addPostParam("placeId", Integer.toString(place.id));
        }
        asyncHttpRequest.addPostParam("placeName", place.name);
        
        asyncHttpRequest.addPostParam("placeSecondName", place.secondName);
        
        if(!Double.isNaN(place.latitude)){
        	asyncHttpRequest.addPostParam("placeLat", Double.toString(place.latitude));
        }
        
        if(!Double.isNaN(place.longitude)){
        	asyncHttpRequest.addPostParam("placeLnt", Double.toString(place.longitude));
        }
        
        if(place.address != null && place.address.length() > 0)
        {
            asyncHttpRequest.addPostParam("placeStreetAddr", place.address);
        }
        if(place.city != null && place.city.length() > 0)
        {
            asyncHttpRequest.addPostParam("placeCity", place.city);
        }
        if(place.state != null && place.state.length() > 0)
        {
            asyncHttpRequest.addPostParam("placeState", place.state);
        }
        if(place.phone != null && place.phone.length() > 0)
        {
            asyncHttpRequest.addPostParam("placePhone", place.phone);
        }
        if(place.googleID != null && place.googleID.length() > 0)
        {
            asyncHttpRequest.addPostParam("placeGoogleId", place.googleID);
        }
        asyncHttpRequest.addPostParam("itemName", item.name);
        asyncHttpRequest.addPostParam("itemId", Integer.toString(item.uid));
        asyncHttpRequest.addPostParam("price", Float.toString(price));
        asyncHttpRequest.addPostParam("send2Sina", Boolean.toString(send2Sina));
        asyncHttpRequest.addFile("reviewPhoto", imgFile, imgFile.getName(), "image/jpeg");
        asyncHttpRequest.execute();
    }


    public void writeToParcel(Parcel parcel, int i)
    {
        User user1 = user;
        parcel.writeParcelable(user1, i);
        Place place1 = place;
        parcel.writeParcelable(place1, i);
        Item item1 = item;
        parcel.writeParcelable(item1, i);
        Sighting sighting1 = sighting;
        parcel.writeParcelable(sighting1, i);
        int j = reviewID;
        parcel.writeInt(j);
        String s = thumb32URL;
        parcel.writeString(s);
        Bitmap bitmap = thumb32;
        parcel.writeParcelable(bitmap, 0);
        String s1 = thumb90URL;
        parcel.writeString(s1);
        Bitmap bitmap1 = thumb90;
        parcel.writeParcelable(bitmap1, 0);
        String s2 = thumb280URL;
        parcel.writeString(s2);
        Bitmap bitmap2 = thumb280;
        parcel.writeParcelable(bitmap2, 0);
        int k;
        byte byte0;
        int l;
        byte byte1;
        int i1;
        byte byte2;
        int j1;
        byte byte3;
        int k1;
        int l1;
        int i2;
        long l2;
        long l3;
        String s3;
        String s4;
        List list;
        int j2;
        byte byte4;
        int k2;
        byte byte5;
        if(nommed)
            k = 1;
        else
            k = 0;
        byte0 = (byte)k;
        parcel.writeByte(byte0);
        if(wanted)
            l = 1;
        else
            l = 0;
        byte1 = (byte)l;
        parcel.writeByte(byte1);
        if(greatShot)
            i1 = 1;
        else
            i1 = 0;
        byte2 = (byte)i1;
        parcel.writeByte(byte2);
        if(greatFind)
            j1 = 1;
        else
            j1 = 0;
        byte3 = (byte)j1;
        parcel.writeByte(byte3);
        k1 = greatShotsCount;
        parcel.writeInt(k1);
        l1 = greatFindsCount;
        parcel.writeInt(l1);
        i2 = commentsCount;
        parcel.writeInt(i2);
        l2 = takenAt.getTime();
        parcel.writeLong(l2);
        l3 = createdAt.getTime();
        parcel.writeLong(l3);
        s3 = sightingID;
        parcel.writeString(s3);
        s4 = note;
        parcel.writeString(s4);
        list = comments;
        parcel.writeTypedList(list);
        if(commentsLoaded)
            j2 = 1;
        else
            j2 = 0;
        byte4 = (byte)j2;
        parcel.writeByte(byte4);
        if(dirty)
            k2 = 1;
        else
            k2 = 0;
        byte5 = (byte)k2;
        parcel.writeByte(byte5);
    }

    public static final android.os.Parcelable.Creator CREATOR = new android.os.Parcelable.Creator() {

        public Review createFromParcel(Parcel parcel)
        {
            return new Review(parcel);
        }

/*        public volatile Object createFromParcel(Parcel parcel)
        {
            return createFromParcel(parcel);
        }*/

        public Review[] newArray(int i)
        {
            return new Review[i];
        }

/*        public volatile Object[] newArray(int i)
        {
            return newArray(i);
        }*/

    }
;
    private static final String LOG_TAG = "Review";
    public List comments;
    FSObject.ResponseDataHandler commentsActionResponseData;
    public int commentsCount;
    public boolean commentsLoaded;
    public Date createdAt;
    public boolean dirty;
    public boolean greatFind;
    public int greatFindsCount;
    public boolean greatShot;
    public int greatShotsCount;
    public Item item;
    public boolean nommed;
    public String note;
    public Place place;
    public int reviewID;
    public Sighting sighting;
    public String sightingID;
    public Date takenAt;
    public Bitmap thumb280;
    public String thumb280URL;
    public Bitmap thumb90;
    public String thumb90URL;
    public Bitmap thumb32;
    public String thumb32URL;
    AsyncHTTPResponseHandler uploadResponseHandler;
    public User user;
    public boolean wanted;

}
