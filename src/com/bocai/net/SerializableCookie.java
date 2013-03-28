
package com.bocai.net;

import java.io.*;
import java.util.Date;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.util.Log;

public class SerializableCookie
    implements Cookie, Externalizable
{

    public SerializableCookie()
    {
        nullMask = 0;
    }

    public SerializableCookie(Cookie cookie1)
    {
        nullMask = 0;
        cookie = cookie1;
    }

    public String getComment()
    {
        return cookie.getComment();
    }

    public String getCommentURL()
    {
        return cookie.getCommentURL();
    }

    public String getDomain()
    {
        return cookie.getDomain();
    }

    public Date getExpiryDate()
    {
        return cookie.getExpiryDate();
    }

    public String getName()
    {
        return cookie.getName();
    }

    public String getPath()
    {
        return cookie.getPath();
    }

    public int[] getPorts()
    {
        return cookie.getPorts();
    }

    public String getValue()
    {
        return cookie.getValue();
    }

    public int getVersion()
    {
        return cookie.getVersion();
    }

    public boolean isExpired(Date date)
    {
        return cookie.isExpired(date);
    }

    public boolean isPersistent()
    {
        return cookie.isPersistent();
    }

    public boolean isSecure()
    {
        return cookie.isSecure();
    }

    public void readExternal(ObjectInput objectInput)
        throws IOException, ClassNotFoundException
    {
    	nullMask = objectInput.readInt();
        String str1 = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        Date date = null;
      
        if((nullMask & 0x1) == 0)
        	str1 = objectInput.readUTF();
        if((nullMask & 0x2) == 0)
        	str2 = objectInput.readUTF();
        if((nullMask & 0x4) == 0)
        	str3 = objectInput.readUTF();
        if((nullMask & 0x8) == 0)
        	str4 = objectInput.readUTF();
        if((nullMask & 0x10) == 0)
        {
        	date = new Date();
        	long l = objectInput.readLong();
        	date.setTime(l);
        }
//      objectInput.readBoolean();
        if((nullMask & 0x20) == 0)
        	str5 = objectInput.readUTF();
        if((nullMask & 0x40) == 0)
        	str6 = objectInput.readUTF();
        if((nullMask & 0x80) == 0)
        {
            int j = objectInput.readInt();
            int ai[] = new int[j];
            for(int k = 0; k < j; k++)
            {
            	ai[k] = objectInput.readInt();
            }
        }
        boolean flag1 = objectInput.readBoolean();
        int version = objectInput.readInt();
        BasicClientCookie basicClientCookie = new BasicClientCookie(str1, str2);
        basicClientCookie.setComment(str3);
        basicClientCookie.setDomain(str5);
        basicClientCookie.setExpiryDate(date);
        basicClientCookie.setPath(str6);
        basicClientCookie.setSecure(flag1);
        basicClientCookie.setVersion(version);
        cookie = basicClientCookie;
    }

    public String toString()
    {
        String s;
        if(cookie == null)
            s = "null";
        else
            s = cookie.toString();
        return s;
    }

    public void writeExternal(ObjectOutput objectOutput)
        throws IOException
    {
       if(getName() == null){
    	   nullMask = nullMask | 0x1;
       }else{
    	   nullMask = nullMask | 0;
       }
       
       if(getValue() == null){
    	   nullMask = nullMask | 0x2;
       }else{
    	   nullMask = nullMask | 0;
       }
       
       if(getComment() == null){
    	   nullMask = nullMask | 0x4;
       }else{
    	   nullMask = nullMask | 0;
       }
       
       if(getCommentURL() == null){
    	   nullMask = nullMask | 0x8;
       }else{
    	   nullMask = nullMask | 0;
       }
       
       if(getExpiryDate() == null){
    	   nullMask = nullMask | 0x10;
       }else{
    	   nullMask = nullMask | 0;
       }
       
       if(getDomain() == null){
    	   nullMask = nullMask | 0x20;
       }else{
    	   nullMask = nullMask | 0;
       }
       
       if(getPath() == null){
    	   nullMask = nullMask | 0x40;
       }else{
    	   nullMask = nullMask | 0;
       }
       
       if(getPorts() == null){
    	   nullMask = nullMask | 0x80;
       }else{
    	   nullMask = nullMask | 0;
       }
       
       objectOutput.writeInt(nullMask);
       if((nullMask & 0x1) == 0){
    	   objectOutput.writeUTF(getName());
       }
       if((nullMask & 0x2) == 0){
    	   objectOutput.writeUTF(getValue());
       }
       if((nullMask & 0x4) == 0){
    	   objectOutput.writeUTF(getComment());
       }
       if((nullMask & 0x8) == 0){
    	   objectOutput.writeUTF(getCommentURL());
       }
       if((nullMask & 0x10) == 0){
    	   objectOutput.writeLong(getExpiryDate().getTime());
       }
       if((nullMask & 0x20) == 0){
    	   objectOutput.writeUTF(getDomain());
       }
       if((nullMask & 0x40) == 0){
    	   objectOutput.writeUTF(getPath());
       }
       if((nullMask & 0x80) == 0){
    	   int arrayOfInt[] = getPorts();
    	   objectOutput.writeInt(arrayOfInt.length);
    	   for(int i = 0; i < arrayOfInt.length; i++){
    		   objectOutput.writeInt(arrayOfInt[i]);
    	   }
       }
      
       objectOutput.writeBoolean(isSecure());
       objectOutput.writeInt(getVersion());
    }

//    private static final int COMMENT = 4;
//    private static final int COMMENT_URL = 8;
//    private static final int DOMAIN = 32;
//    private static final int EXPIRY_DATE = 16;
//    private static final int NAME = 1;
//    private static final int PATH = 64;
//    private static final int PORTS = 128;
//    private static final int VALUE = 2;
    private transient Cookie cookie;
    private transient int nullMask;
}
