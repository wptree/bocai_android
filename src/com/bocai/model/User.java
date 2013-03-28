
package com.bocai.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.bocai.BocaiApplication;
import com.bocai.net.SerializableCookie;
import com.bocai.util.FlurryEvents;
import com.bocai.util.Macros;
import java.io.*;
import java.util.*;

import org.apache.http.cookie.Cookie;
import org.json.JSONObject;

public class User implements Parcelable
{

    private User(Parcel parcel)
    {
    	uid = parcel.readInt();
    	email = parcel.readString();
    	name = parcel.readString();
        parcel.readInt();
        avatarURL = parcel.readString();
        avatar = (Bitmap)parcel.readParcelable(null);
    }
    
	public User(ObjectInputStream objectInputStream)
        throws IOException, ClassNotFoundException
    {
    	uid = objectInputStream.readInt();
    	email = objectInputStream.readUTF();
    	name = objectInputStream.readUTF();
        int j = objectInputStream.readInt();
        cookies = new LinkedList<Cookie>();

        for(int k = 0; k < j; k++)
        {
            SerializableCookie serializablecookie = new SerializableCookie();
            serializablecookie.readExternal(objectInputStream);
            cookies.add(serializablecookie);
        }

        avatarURL = objectInputStream.readUTF();
        if(avatarURL.length() == 0)
            avatarURL = null;
    }

    @SuppressWarnings("unchecked")
	public User(JSONObject jsonObject)
    {
        if(jsonObject == null)
        {
            return;
        } else
        {
        	uid = ((Integer)jsonObject.opt("id")).intValue();
            email = (String)jsonObject.opt("email");
            name = (String)jsonObject.opt("name");
            cookies = (List<Cookie>) jsonObject.opt("cookies");
            avatarURL = (String)jsonObject.opt("avatar");
            if(cookies == null){
            	cookies = new LinkedList<Cookie>();
            }
            return;
        }
    }

    public static void archiveUser(User user)
    {
    	
    	 if(user == null)
             return;
         FileOutputStream fileOutputStream = null;
         try {
        	 //TODO:mocked, change code back when use phone rather than emulator
 		 //	fileOutputStream = BocaiApplication.instance.openFileOutput("CurrentUser.data", 0);
        	 fileOutputStream = new FileOutputStream("/sdcard/CurrentUser.data");
 		} catch (FileNotFoundException e) {
 			Log.w("User", e.getMessage(), e);
 			return;
 		}
         if(fileOutputStream != null)
         {
 			try {
 				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
 	            user.writeToOutputStream(objectOutputStream);
 	            objectOutputStream.flush();
 			} catch (IOException e) {
 				Log.w("User", e.getMessage(), e);
 			}
         }
         if(fileOutputStream == null)
             return;

         try
         {
             fileOutputStream.close();
             return;
         }
         catch(IOException ioe)
         {
             Log.e("User", "Error closing out stream:", ioe);
         }
    }

    public static User archivedUser()
    {
    	Log.i("User", "archivedUser method"); 
    	User user = null;
    	    Date date = new Date();
    	    try
    	    {
    	     //TODO:mocked, change code back when use phone rather than emulator	
    	    //  FileInputStream fileInputStream = BocaiApplication.instance.openFileInput("CurrentUser.data");
    	    	FileInputStream fileInputStream = new FileInputStream("/sdcard/CurrentUser.data");  
    	    if (fileInputStream != null)
    	      {
    	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
    	        user = new User(objectInputStream);
    	        Object[] arrayOfObject = new Object[2];
    	        arrayOfObject[0] = user;
    	        long l1 = new Date().getTime();
    	        long l2 = date.getTime();
    	        Long localLong = Long.valueOf(l1 - l2);
    	        arrayOfObject[1] = localLong;
    	        String str = String.format("Unarchiving Current User: %s [%d ms]", arrayOfObject);
    	        Log.d("User", str);
    	      //  return user;
    	      }
    	    }
    	    catch (Exception e)
    	    {
    	     	//Log.w("User", e.getLocalizedMessage(), e);
    	       //	return user;
    	    	//NOTE: at first run the file does not exist!
    	    	return null;
    	    }
    	    return user;
    }

    public static void clearCurrentUser()
    {
        Log.i("User", "clearCurrentUser method");
    	Macros.CACHE_EXPIRE("current-user:");
        Macros.FS_DEFAULT_REMOVE("email");
        Macros.FS_DEFAULT_REMOVE("password");
        deleteArchivedUser();
        Filter.setFilterResults(1);
    }

    public static User currentUser()
    {
        Object obj = Macros.CACHE_GET("current-user:");
        User user;
        if(obj != null)
        {
            user = (User)obj;
        } else
        {
            User user1 = archivedUser();
            if(user1 != null)
            {
                Macros.CACHE_SET("current-user:", user1);
                if(user1.email != null)
                    FlurryEvents.FLURRY_SET_USER(user1.email);
                user = user1;
            } else
            {
                Log.d("User", "current user returning nil");
                user = null;
            }
        }
        return user;
    }

    public static boolean deleteArchivedUser()
    {
    	 Log.i("User", "deleteArchivedUser method");
    	 //TODO: mocked,change it back when move to android cellphone
    	 File file = new File("/sdcard/CurrentUser.data");
    	 return file.delete();
    	//return BocaiApplication.instance.deleteFile("/sdcard/CurrentUser.data");
    }

    public static boolean isLoggedIn()
    {
       Log.i("User", "isLoggedIn method");
    	boolean flag;
        if(currentUser() != null)
            flag = true;
        else
            flag = false;
        Log.i("User", "isLoggedIn method return " + flag);
        return flag;
    }

    public static boolean isNotLoggedIn()
    {
    	 Log.i("User", "isNotLoggedIn method");
    	boolean flag;
        if(currentUser() == null)
            flag = true;
        else
            flag = false;
        Log.i("User", "isNotLoggedIn method return " + flag);
        return flag;
    }

    public int describeContents()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String toString()
    {
        StringBuilder stringBuilder = (new StringBuilder()).append("{[User] uid: ");
        stringBuilder.append(uid).append(", email: ");
        stringBuilder.append(email).append(", name: ");
        stringBuilder.append(name).append(", avatarURL: ");
        return stringBuilder.append(avatarURL).append("}").toString();
    }

    public void writeToOutputStream(ObjectOutputStream objectOutputStream)
        throws IOException
    {
        objectOutputStream.writeInt(uid);
        objectOutputStream.writeUTF(email);
        objectOutputStream.writeUTF(name);
        int size = cookies.size();
        objectOutputStream.writeInt(size);
        Cookie cookie;
        for(Iterator<Cookie> iterator = cookies.iterator(); iterator.hasNext(); (new SerializableCookie(cookie)).writeExternal(objectOutputStream))
            cookie = iterator.next();

        if(avatarURL != null){
        	objectOutputStream.writeUTF(avatarURL);	
        }else{
        	objectOutputStream.writeUTF("");
        }
    }

    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeInt(uid);
        parcel.writeString(email);
        parcel.writeString(name);
        if(cookies != null){
        	parcel.writeInt(cookies.size());
        }else{
        	parcel.writeInt(0);
        }
        parcel.writeString(avatarURL);
        parcel.writeParcelable(avatar, 0);
    }

    public static final android.os.Parcelable.Creator<User> CREATOR = new android.os.Parcelable.Creator<User>() {

        public User createFromParcel(Parcel parcel)
        {
            return new User(parcel);
        }

        public User[] newArray(int i)
        {
            return new User[i];
        }
    }
;
    static final String LOG_TAG = "User";
    static final String USER_ARCHIVE = "CurrentUser.data";
    public Bitmap avatar;
    public String avatarURL;
    public List<Cookie> cookies;
    public String email;
    public String name;
    public int uid;

}
