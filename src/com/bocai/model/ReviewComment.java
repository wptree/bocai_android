// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReviewComment.java

package com.bocai.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.bocai.util.DateUtilities;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

// Referenced classes of package com.bocai.model:
//            FSObject, User

public class ReviewComment extends FSObject
    implements Parcelable
{

    private ReviewComment(Parcel parcel)
    {
    }


    public ReviewComment(JSONObject jsonobject)
    {
        if(jsonobject == null)
            return;
        Object obj = jsonobject.opt("text");
        if(obj != null)
        {
            String s = (String)obj;
            comment = s;
        }
        Object obj1 = jsonobject.opt("id");
        if(obj1 != null)
        {
            int i = ((Integer)obj1).intValue();
            commentID = i;
        }
        Object obj2 = jsonobject.opt("created_at");
        if(obj2 == null)
            return;
        
        Object obj3 = jsonobject.opt("person");
        if(obj3 != null)
        {
            if(obj3 != JSONObject.NULL)
            {
            	user = new User((JSONObject)obj3);
            }
        }
        
        try
        {
            SimpleDateFormat simpledateformat = DateUtilities.ISO8601Format;
            String s1 = (String)obj2;
            Date date = simpledateformat.parse(s1);
            createdAt = date;
            return;
        }
        catch(ParseException parseexception)
        {
            return;
        }
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i)
    {
    }

    public static final android.os.Parcelable.Creator CREATOR = new android.os.Parcelable.Creator() {

        public ReviewComment createFromParcel(Parcel parcel)
        {
            return new ReviewComment(parcel);
        }

/*        public volatile Object createFromParcel(Parcel parcel)
        {
            return createFromParcel(parcel);
        }*/

        public ReviewComment[] newArray(int i)
        {
            return new ReviewComment[i];
        }

/*        public volatile Object[] newArray(int i)
        {
            return newArray(i);
        }*/

    };
    
    public String comment;
    public int commentID;
    public Date createdAt;
    public int reviewID;
    public User user;
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("comment=")
    	.append(comment)
    	.append(",")
    	.append("commentdID=")
    	.append(commentID)
    	.append(",")
    	.append("reviewID=")
    	.append(reviewID)
    	.append(",")
    	.append("user=")
    	.append(user);
    	
    	return sb.toString();
    	
    }

}
