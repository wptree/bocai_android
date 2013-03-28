package com.bocai.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.text.DecimalFormat;
import org.json.JSONObject;


public class Promo extends Sighting
    implements Parcelable
{

    private Promo(Parcel parcel)
    {
        text = parcel.readString();
        url = parcel.readString();
        imageUrl = parcel.readString();
        distance = parcel.readDouble();
    }


    public Promo(JSONObject jsonObject)
    {
        if(jsonObject == null){
            return;
        }
        text = jsonObject.optString("text");
        url = jsonObject.optString("url");
        imageUrl = jsonObject.optString("image");
        distance = jsonObject.optDouble("distance");
       
        thumb280URL = imageUrl;
        int i = Filter.filterSort();
        setSearchFilterSort(i);
    }

    public static boolean isPromo(JSONObject jsonObject)
    {

        if(jsonObject.has("text") && 
           jsonObject.has("url") && 
           jsonObject.has("image") && 
           !jsonObject.has("item")){
        	return true;
        }
         
        return false;
    }

    public int describeContents()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSearchFilterSort(int i)
    {
        if(detailInfo != null){
            detailInfo = null;
        }
        
        switch(i){
          default: return;	
          case 1:
        	    if (distance == 4.9e-324D){
        	    	return;
        	    }
        	    DecimalFormat df = new DecimalFormat("#####.##");
        	    String str1 = df.format(distance);
        	    detailInfo = str1 + " miles";
        }
    }

    public String toString()
    {
        StringBuilder sb = (new StringBuilder()).append("{text: ");
        sb.append(text).append(", url: ");
        sb.append(url).append(", imageUrl: ");
        return sb.append(imageUrl).append("}").toString();
    }

    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeString(text);
        parcel.writeString(url);
        parcel.writeString(imageUrl);
        parcel.writeDouble(distance);
    }

    public static final android.os.Parcelable.Creator<Promo> CREATOR = new android.os.Parcelable.Creator<Promo>() {

        public Promo createFromParcel(Parcel parcel)
        {
            return new Promo(parcel);
        }

        public Promo[] newArray(int i)
        {
            return new Promo[i];
        }
    };
    
    public double distance = 4.9e-324D;
    public String imageUrl;
    public String text;
    public String url;

}
