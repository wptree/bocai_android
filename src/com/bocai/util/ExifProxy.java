// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ExifProxy.java

package com.bocai.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.bocai.BocaiApplication;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Referenced classes of package com.bocai.util:
//            Macros

public class ExifProxy
{

    public ExifProxy(Uri uri)
    {
        String s;
        exif = null;
        loadInterface();
        s = null;
        
        if(!uri.getScheme().equals("file")) {
            String s1 = uri.toString();
            String s2 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
            if(s1.startsWith(s2))
            {
                ContentResolver contentresolver = Macros.FS_APPLICATION().getContentResolver();
                String as[] = new String[1];
                as[0] = "_data";
                Uri uri1 = uri;
                String as1[] = null;
                String s3 = null;
                Cursor cursor = contentresolver.query(uri1, as, null, as1, s3);
                if(cursor != null)
                {
                    int i = cursor.getColumnIndexOrThrow("_data");
                    boolean flag = cursor.moveToFirst();
                    s = cursor.getString(i);
                    cursor.close();
                }
            }
        } else {
            s = uri.getPath();
        }
        if(exifInterfaceCtor == null)
            return;
        if(s == null)
            return;
        Constructor constructor = exifInterfaceCtor;
        Object aobj[] = new Object[1];
        aobj[0] = s;
        try {
			exif = constructor.newInstance(aobj);
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

        return;
    }

    public ExifProxy(String s)
    {
        exif = null;
        loadInterface();
        if(exifInterfaceCtor == null)
            return;
        Constructor constructor = exifInterfaceCtor;
        Object aobj[] = new Object[1];
        aobj[0] = s;
		try {
			exif = constructor.newInstance(aobj);
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

    }

    private void loadInterface()
    {
        int i = Integer.valueOf(android.os.Build.VERSION.SDK).intValue();
        int j = i;

        if(j < 5)
            return;
        try {
        if(exifInterfaceClass == null)
            exifInterfaceClass = Class.forName("android.media.ExifInterface");
        if(exifInterfaceCtor == null)
        {
            Class class1 = exifInterfaceClass;
            Class aclass[] = new Class[1];
            aclass[0] = Class.forName("java.lang.String");
            exifInterfaceCtor = class1.getConstructor(aclass);
        }
        if(exifInterface_getAttribute == null)
        {
            Class class2 = exifInterfaceClass;
            Class aclass1[] = new Class[1];
            aclass1[0] = Class.forName("java.lang.String");
            exifInterface_getAttribute = class2.getMethod("getAttribute", aclass1);
        }
        if(exifInterface_setAttribute == null)
        {
            Class class3 = exifInterfaceClass;
            Class aclass2[] = new Class[2];
            aclass2[0] = Class.forName("java.lang.String");
            aclass2[1] = Class.forName("java.lang.String");
            exifInterface_setAttribute = class3.getMethod("setAttribute", aclass2);
        }
        if(exifInterface_saveAttributes != null)
        {
            return;
        } else
        {
            Class class4 = exifInterfaceClass;
            Class aclass3[] = (Class[])null;
            exifInterface_saveAttributes = class4.getMethod("saveAttributes", aclass3);
            return;
        }
        } catch(Exception e) {
        }
    }

    public void copy(ExifProxy exifproxy)
    {
        if(exif == null)
            return;
        if(exifproxy == null)
            return;
        String as[] = tags;
        int i = as.length;
        int j = 0;
        do
        {
            if(j >= i)
                return;
            String s = as[j];
            String s1 = getAttribute(s);
            if(s1 != null)
                exifproxy.setAttribute(s, s1);
            j++;
        } while(true);
    }

    public String getAttribute(String s)
    {
    	if(exif != null) {
            String s2 = null;
            Method method = exifInterface_getAttribute;
            Object obj = exif;
            Object aobj[] = new Object[1];
            aobj[0] = s;
            try {
				s2 = (String)method.invoke(obj, aobj);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
            return s2;
    	}
    	return null;
    }

    public int getOrientation()
    {
        String s = getAttribute("Orientation");
        if(s != null) {
            int j = Integer.valueOf(s).intValue();
            return j;
        }
        return 0;
    }

    public void saveAttributes()
    {
        if(exif == null)
            return;
        Method method = exifInterface_saveAttributes;
        Object obj = exif;
        Object aobj[] = new Object[0];
        try {
			method.invoke(obj, aobj);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
    }

    public void setAttribute(String s, String s1)
    {
        if(exif == null)
            return;
        Method method = exifInterface_setAttribute;
        Object obj = exif;
        Object aobj[] = new Object[2];
        aobj[0] = s;
        aobj[1] = s1;
        try {
			method.invoke(obj, aobj);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
    }

    public String toString()
    {
        String s2;
        if(exif != null)
        {
            StringBuilder stringbuilder = new StringBuilder();
            String as[] = tags;
            int i = as.length;
            for(int j = 0; j < i; j++)
            {
                String s = as[j];
                StringBuilder stringbuilder1 = stringbuilder.append(s).append(": ");
                String s1 = getAttribute(s);
                StringBuilder stringbuilder2 = stringbuilder1.append(s1).append('\n');
            }

            s2 = stringbuilder.toString();
        } else
        {
            s2 = null;
        }
        return s2;
    }

    public static final int ORIENTATION_FLIP_HORIZONTAL = 2;
    public static final int ORIENTATION_FLIP_VERTICAL = 4;
    public static final int ORIENTATION_NORMAL = 1;
    public static final int ORIENTATION_ROTATE_180 = 3;
    public static final int ORIENTATION_ROTATE_270 = 8;
    public static final int ORIENTATION_ROTATE_90 = 6;
    public static final int ORIENTATION_TRANSPOSE = 5;
    public static final int ORIENTATION_TRANSVERSE = 7;
    static Class exifInterfaceClass = null;
    static Constructor exifInterfaceCtor = null;
    static Method exifInterface_getAttribute = null;
    static Method exifInterface_saveAttributes = null;
    static Method exifInterface_setAttribute = null;
    static String tags[];
    Object exif;

    static 
    {
        String as[] = new String[18];
        as[0] = "Orientation";
        as[1] = "DateTime";
        as[2] = "Make";
        as[3] = "Model";
        as[4] = "Flash";
        as[5] = "ImageWidth";
        as[6] = "ImageLength";
        as[7] = "GPSLatitude";
        as[8] = "GPSLongitude";
        as[9] = "GPSLatitudeRef";
        as[10] = "GPSLongitudeRef";
        as[11] = "GPSTimeStamp";
        as[12] = "GPSDateStamp";
        as[13] = "WhiteBalance";
        as[14] = "FocalLength";
        as[15] = "GPSProcessingMethod";
        as[16] = "DocumentName";
        as[17] = "FileSource";
        tags = as;
    }
}
