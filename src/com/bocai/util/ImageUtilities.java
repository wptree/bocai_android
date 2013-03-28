// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ImageUtilities.java

package com.bocai.util;

import android.content.ContentResolver;
import android.graphics.*;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import com.bocai.BocaiApplication;
import java.io.*;

// Referenced classes of package com.bocai.util:
//            ExifProxy, Macros

public class ImageUtilities
{
    public static final int ERR_OUT_OF_MEMORY = 156;
    private static final String LOG_TAG = "ImageUtilities";
    private static final android.graphics.BitmapFactory.Options _opt = new android.graphics.BitmapFactory.Options();
    private static int lastError = 0;
    
    static class TransformOptions
    {

        ExifProxy exif;
        int height;
        int maxHeight;
        int maxWidth;
        android.graphics.Bitmap.Config pixelConfig;
        int width;

        TransformOptions()
        {
        }
    }


    public ImageUtilities()
    {
    }

    public static Bitmap decodeStream(InputStream inputstream, int i, int j, int k, android.graphics.Bitmap.Config config)
    {
        int l = 1;
        while(i / 2 >= k && j / 2 >= k) {
            i /= 2;
            j /= 2;
            l++;
        }
        android.graphics.BitmapFactory.Options options;
        options = new android.graphics.BitmapFactory.Options();
        options.inSampleSize = l;
        options.inDither = false;
        if(config == null)
            config = android.graphics.Bitmap.Config.ARGB_8888;
        options.inPreferredConfig = config;
        Bitmap bitmap = null;
        Rect rect = null;
        bitmap = BitmapFactory.decodeStream(inputstream, rect, options);
        
        return bitmap;
    }

    public static Bitmap getBorderedBitmap(Bitmap bitmap, int i, int j)
    {
        Bitmap bitmap1;
        if(bitmap == null)
        {
            bitmap1 = null;
        } else
        {
            int k = bitmap.getWidth();
            int l = j * 2;
            int i1 = k + l;
            int j1 = bitmap.getHeight();
            int k1 = j * 2;
            int l1 = j1 + k1;
            android.graphics.Bitmap.Config config = android.graphics.Bitmap.Config.ARGB_8888;
            Bitmap bitmap2 = Bitmap.createBitmap(i1, l1, config);
            Canvas canvas = new Canvas(bitmap2);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawColor(i);
            float f = j;
            float f1 = j;
            canvas.drawBitmap(bitmap, f, f1, paint);
            bitmap1 = bitmap2;
        }
        return bitmap1;
    }

    public static boolean getImageProperties(InputStream inputstream, android.graphics.BitmapFactory.Options options)
    {
        boolean flag;
        if(inputstream != null)
        {
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeStream(inputstream, null, options);
            flag = true;
        } else
        {
            flag = false;
        }
        return flag;
    }

    public static boolean getImageProperties(String s, android.graphics.BitmapFactory.Options options)
    {
        boolean flag = true;
        if(s != null)
        {
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(s, options);
        } else
        {
            flag = false;
        }
        return flag;
    }

    public static int getLastError()
    {
        int i = lastError;
        lastError = 0;
        return i;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int i)
    {
        int j = bitmap.getWidth();
        int k = bitmap.getHeight();
        android.graphics.Bitmap.Config config = android.graphics.Bitmap.Config.ARGB_8888;
        Bitmap bitmap1 = Bitmap.createBitmap(j, k, config);
        Canvas canvas = new Canvas(bitmap1);
        Paint paint = new Paint();
        int l = bitmap.getWidth();
        int i1 = bitmap.getHeight();
        Rect rect = new Rect(0, 0, l, i1);
        RectF rectf = new RectF(rect);
        float f = i;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);
        canvas.drawRoundRect(rectf, f, f, paint);
        android.graphics.PorterDuff.Mode mode = android.graphics.PorterDuff.Mode.SRC_IN;
        PorterDuffXfermode porterduffxfermode = new PorterDuffXfermode(mode);
        android.graphics.Xfermode xfermode = paint.setXfermode(porterduffxfermode);
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return bitmap1;
    }

    public static Bitmap scaleImage(InputStream inputstream, TransformOptions transformoptions)
    {
        Bitmap bitmap;
        StringBuilder stringbuilder = (new StringBuilder()).append("scaleImage: in ").append(inputstream).append(" (");
        int i = transformoptions.width;
        StringBuilder stringbuilder1 = stringbuilder.append(i).append("x");
        int j = transformoptions.height;
        String s = stringbuilder1.append(j).append(")").toString();
        int k = Log.d("ImageUtilities", s);
        int l = transformoptions.width;
        int i1 = transformoptions.height;
        int j1 = transformoptions.maxWidth;
        int k1 = transformoptions.maxHeight;
        int l1 = Math.max(j1, k1);
        android.graphics.Bitmap.Config config = transformoptions.pixelConfig;
        bitmap = decodeStream(inputstream, l, i1, l1, config);
        
        Bitmap bitmap1 = null;
        if(bitmap != null) {
            int i2;
            int j2;
            Matrix matrix;
            i2 = bitmap.getWidth();
            j2 = bitmap.getHeight();
            int k2;
            int l2;
            float f;
            float f1;
            float f2;
            float f3;
            float f4;
            float f5;
            float f6;
            float f7;
            if(i2 >= j2)
            {
                k2 = i2;
                l2 = j2;
            } else
            {
                k2 = j2;
                l2 = i2;
            }
            f = transformoptions.maxWidth;
            f1 = k2;
            f2 = f / f1;
            f3 = transformoptions.maxHeight;
            f4 = l2;
            f5 = f3 / f4;
            f6 = Math.min(f2, f5);
            f7 = Math.min(1F, f6);
            if((double)f7 >= 1D)
            {
                bitmap1 = bitmap;
                //return bitmap1;
            }
            matrix = new Matrix();
            matrix.postScale(f7, f7);
            if(transformoptions.exif != null)
            {
                if(transformoptions.exif.getOrientation() == 3)
                    matrix.postRotate(180F);
                else if(transformoptions.exif.getOrientation() == 8)
                    matrix.postRotate(270F);
                if(transformoptions.exif.getOrientation() == 6)
                	matrix.postRotate(90F);
            }
            bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, i2, j2, matrix, true);
            //bitmap.recycle();
            StringBuilder stringbuilder2 = (new StringBuilder()).append("scaleImage: rescaled image size (");
            int i3 = bitmap1.getWidth();
            StringBuilder stringbuilder3 = stringbuilder2.append(i3).append("x");
            int j3 = bitmap1.getHeight();
            String s1 = stringbuilder3.append(j3).append(")").toString();
            Log.d("ImageUtilities", s1);
        }
        return bitmap1;
    }

    public static boolean scaleImage(Uri uri, File file, int i, int j)
    {
        boolean flag;
        ExifProxy exifproxy2;
        TransformOptions transformoptions = new TransformOptions();
        transformoptions.maxWidth = i;
        transformoptions.maxHeight = j;
        ExifProxy exifproxy = new ExifProxy(uri);
        transformoptions.exif = exifproxy;
        StringBuilder stringbuilder = (new StringBuilder()).append("Dumping Exif data for ").append(uri).append("\n------------------------\n");
        ExifProxy exifproxy1 = transformoptions.exif;
        String s = stringbuilder.append(exifproxy1).toString();
        int k = Log.d("ImageUtilities", s);
        FileOutputStream fileoutputstream = null;
		try {
			fileoutputstream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
		}
        flag = scaleImage(uri, ((OutputStream) (fileoutputstream)), transformoptions);
        try {
			fileoutputstream.flush();
		} catch (IOException e) {
		}
        try {
			fileoutputstream.close();
		} catch (IOException e) {
		}
        if(!flag || transformoptions.exif == null)
        	return false;
        String s1 = file.getAbsolutePath();
        exifproxy2 = new ExifProxy(s1);
        if(exifproxy2 == null)
        	return false;
        transformoptions.exif.copy(exifproxy2);
        exifproxy2.setAttribute("Orientation", "0");
        synchronized(_opt)
        {
            String s2 = file.getAbsolutePath();
            android.graphics.BitmapFactory.Options options1 = _opt;
            if(getImageProperties(s2, options1))
            {
                StringBuilder stringbuilder1 = (new StringBuilder()).append("");
                int l = _opt.outWidth;
                String s3 = stringbuilder1.append(l).toString();
                exifproxy2.setAttribute("ImageWidth", s3);
                StringBuilder stringbuilder2 = (new StringBuilder()).append("");
                int i1 = _opt.outHeight;
                String s4 = stringbuilder2.append(i1).toString();
                exifproxy2.setAttribute("ImageLength", s4);
                exifproxy2.saveAttributes();
            }
        }
        StringBuilder stringbuilder3 = (new StringBuilder()).append("Dumping Exif data for ");
        String s5 = file.getAbsolutePath();
        String s6 = stringbuilder3.append(s5).append("\n------------------------\n").append(exifproxy2).toString();
        Log.d("ImageUtilities", s6);
        
        return flag;
    }

    public static boolean scaleImage(Uri uri, OutputStream outputstream, TransformOptions transformoptions)
    {
        ContentResolver contentresolver;
        boolean flag;
        contentresolver = Macros.FS_APPLICATION().getContentResolver();
        flag = false;
        synchronized(_opt) {
            InputStream inputstream = null;
			try {
				inputstream = contentresolver.openInputStream(uri);
			} catch (FileNotFoundException e) {
			}
            android.graphics.BitmapFactory.Options options = _opt;
            flag = getImageProperties(inputstream, options);
            if (flag) {
                int i = _opt.outWidth;
                transformoptions.width = i;
                int j = _opt.outHeight;
                transformoptions.height = j;

                {
                    android.graphics.Bitmap.Config config = android.graphics.Bitmap.Config.ARGB_8888;
                    transformoptions.pixelConfig = config;
                    try {
						flag = scaleImage(contentresolver.openInputStream(uri), outputstream, transformoptions);
					} catch (FileNotFoundException e) {
					}
                    if(flag || getLastError() != 65436)
                        return true;
                    int k = Log.d("ImageUtilities", "decodeStream: Failed to decode as RGB8888, attempting RGB565...");
                    android.graphics.Bitmap.Config config1 = android.graphics.Bitmap.Config.RGB_565;
                    transformoptions.pixelConfig = config1;
					try {
						flag = scaleImage(contentresolver.openInputStream(uri), outputstream, transformoptions);
					} catch (FileNotFoundException e) {
					}
                }
                return flag;
            }
            return false;
        }
    }

    public static boolean scaleImage(InputStream inputstream, OutputStream outputstream, TransformOptions transformoptions)
    {
        Bitmap bitmap = scaleImage(inputstream, transformoptions);
        boolean flag;
        if(bitmap == null)
        {
            flag = false;
        } else
        {
            android.graphics.Bitmap.CompressFormat compressformat = android.graphics.Bitmap.CompressFormat.JPEG;
            boolean flag1 = bitmap.compress(compressformat, 80, outputstream);
            bitmap.recycle();
            flag = flag1;
        }
        return flag;
    }

    public static Bitmap scaleImageForImageView(Uri uri, ImageView imageview)
    {
        Bitmap bitmap;
        ContentResolver contentresolver;
        bitmap = null;
        contentresolver = Macros.FS_APPLICATION().getContentResolver();
        TransformOptions transformoptions = new TransformOptions();
        synchronized(_opt) {
            InputStream inputstream = null;
            android.graphics.BitmapFactory.Options options;
            try {
				inputstream = contentresolver.openInputStream(uri);
			} catch (FileNotFoundException e) {
			}
            options = _opt;
            if(getImageProperties(inputstream, options)) {
                int j = _opt.outWidth;
                transformoptions.width = j;
                int k = _opt.outHeight;
                transformoptions.height = k;
                int j1;
                int k1;
                int l = imageview.getWidth();
                int i1 = imageview.getHeight();
                j1 = Math.max(l, i1);
                k1 = 1024;
                while (true) {
                if(k1 <= 32) {
                    transformoptions.maxWidth = k1;
                    transformoptions.maxHeight = k1;
                    android.graphics.Bitmap.Config config = android.graphics.Bitmap.Config.RGB_565;
                    transformoptions.pixelConfig = config;
                    String s2 = (new StringBuilder()).append("scaleImageForImageView: imageView size ").append(k1).append(" x ").append(k1).toString();
                    int i2 = Log.d("ImageUtilities", s2);
                    try {
						bitmap = scaleImage(contentresolver.openInputStream(uri), transformoptions);
					} catch (FileNotFoundException e) {
					}                  
                    return bitmap;
                } else {
                	int i = k1 / 2;
                	if (j1 > i) {
                        transformoptions.maxWidth = k1;
                        transformoptions.maxHeight = k1;
                        android.graphics.Bitmap.Config config = android.graphics.Bitmap.Config.RGB_565;
                        transformoptions.pixelConfig = config;
                        String s2 = (new StringBuilder()).append("scaleImageForImageView: imageView size ").append(k1).append(" x ").append(k1).toString();
                        Log.d("ImageUtilities", s2);
                        try {
							bitmap = scaleImage(contentresolver.openInputStream(uri), transformoptions);
						} catch (FileNotFoundException e) {
						}
                        return bitmap;
                	} else {
                		k1 >>= 1;              		
                	}
                }
                }
            }
            return bitmap;
        }
    }



}
