
package com.bocai.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import com.bocai.net.AsyncHTTPRequest;
import com.bocai.util.Macros;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;


public class Person extends FSObject
    implements Parcelable
{

	public Person(){
		
	}
	private Person(Parcel parcel)
    {
    	uid = parcel.readInt();
    	name = parcel.readString();
    	avatarURL = parcel.readString();
    	avatar = (Bitmap)parcel.readParcelable(null);
    	hugeAvatarURL = parcel.readString();
    	hugeAvatar = (Bitmap)parcel.readParcelable(null);
    	location = parcel.readString();
    	ribbonsCount = parcel.readInt();
    	tipsCount = parcel.readInt();
    	sightingsCount = parcel.readInt();
    	wantsCount = parcel.readInt();
    	achievementsCount = parcel.readInt();
    	recentAchievementsCount = parcel.readInt();
    	followingsCount = parcel.readInt();
    	guidesCount = parcel.readInt();
        if(parcel.readByte() == 1)
        	isFollowing = true;
        else
        	isFollowing = false;
    }


    public Person(JSONObject jsonObject)
    {
        if(jsonObject == null)
        {
            return;
        } else
        {
            init(jsonObject);
            return;
        }
    }

    public static final AsyncHTTPRequest personRequestWithID(int uid, HashMap<String,Object> params)
    {
        return FSObject.requestWithPath((new StringBuilder()).append("people/").append(uid).toString(), params);
    }

    public int describeContents()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void init(JSONObject jsonObject)
    {
        
    	uid = jsonObject.optInt("id");
    	name = jsonObject.optString("name");
    	avatarURL = jsonObject.optString("avatar_50");
    	hugeAvatarURL = jsonObject.optString("avatar_300");
    	location = jsonObject.optString("location");
    	ribbonsCount = jsonObject.optInt("noms_count");
    	tipsCount = jsonObject.optInt("tips_count");
    	sightingsCount = jsonObject.optInt("sightings_count");
    	wantsCount = jsonObject.optInt("wants_count");
    	achievementsCount = jsonObject.optInt("notifications_count");
    	recentAchievementsCount = jsonObject.optInt("recent_notifications_count");
    	followingsCount = jsonObject.optInt("followings_count");
    	guidesCount = jsonObject.optInt("guides_count");
    	isFollowing = jsonObject.optBoolean("following");
    }

    public void loadPerson(int userId)
    {
        uid = userId;
        personLoading = true;
        HashMap<String,Object> requestParams = new HashMap<String,Object>();
        User user = User.currentUser();
        if(user != null)
        {
            String s = Integer.toString(user.uid);
            requestParams.put("u", s);
        }
        AsyncHTTPRequest asyncHttpRequest = personRequestWithID(userId, requestParams);
        performRequest(asyncHttpRequest);
    }

    protected void responseData(JSONObject jsonObject, AsyncHTTPRequest asyncHttpRequest)
        throws JSONException
    {
        if(!personLoading)
            return;
        personLoading = false;
        init(jsonObject);
        if(delegate == null)
        {
            return;
        }
        JSONObject obj = new JSONObject(Macros.ACTION_PERSON_LOADED());
        delegate.finishedAction(obj);
        delegate.FSResponse(null);
    }

    public String toString()
    {
        StringBuilder stringBuilder = (new StringBuilder()).append("{[Person] uid: ");
        stringBuilder.append(uid).append(", name: ");
        stringBuilder.append(name).append(", avatarURL: ");
        return stringBuilder.append(avatarURL).append("}").toString();
    }

    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeInt(uid);
        parcel.writeString(name);
        parcel.writeString(avatarURL);
        parcel.writeParcelable(avatar, 0);
        parcel.writeString(hugeAvatarURL);
        parcel.writeParcelable(hugeAvatar, 0);
        parcel.writeString(location);
        parcel.writeInt(ribbonsCount);
        parcel.writeInt(tipsCount);
        parcel.writeInt(sightingsCount);
        parcel.writeInt(wantsCount);
        parcel.writeInt(achievementsCount);
        parcel.writeInt(recentAchievementsCount);
        parcel.writeInt(followingsCount);
        parcel.writeInt(guidesCount);
        
        if(isFollowing){
        	 parcel.writeByte((byte)1);
        }else{
        	 parcel.writeByte((byte)0);
        }
    }

    public static final android.os.Parcelable.Creator<Person> CREATOR = new android.os.Parcelable.Creator<Person>() {

        public Person createFromParcel(Parcel parcel)
        {
            return new Person(parcel);
        }

        public Person[] newArray(int i)
        {
            return new Person[i];
        }
    };
    
    static final String LOG_TAG = "Person";
    public int achievementsCount;
    public Bitmap avatar;
    public String avatarURL;
    public int followingsCount;
    public int guidesCount;
    public Bitmap hugeAvatar;
    public String hugeAvatarURL;
    public boolean isFollowing;
    boolean isSettingFollowing;
    public String location;
    public String name;
    boolean personLoading;
    public int recentAchievementsCount;
    boolean reviewsLoaded;
    boolean reviewsLoading;
    public int ribbonsCount;
    public int sightingsCount;
    public int tipsCount;
    public int uid;
    public int wantsCount;

}
