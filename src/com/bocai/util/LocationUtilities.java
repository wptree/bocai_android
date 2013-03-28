package com.bocai.util;

import android.location.Location;

public class LocationUtilities
{

    public LocationUtilities()
    {
    }

    public static boolean isBetterLocation(Location location, Location location1)
    {
        boolean flag;
        if(location1 == null)
        {
            flag = true;
        } else
        {
            long l = location.getTime();
            long l1 = location1.getTime();
            long l2 = l - l1;
            boolean flag1;
            boolean flag4;
            if(l2 > 0x1d4c0L)
                flag1 = true;
            else
                flag1 = false;
            if(l2 < 0xfffffffffffe2b40L)
                l2 = 1;
            else
                l2 = 0;
            if(l2 > 0L)
                flag4 = true;
            else
                flag4 = false;
            if(flag1)
                flag = true;
            else
            if(l2 != 0)
            {
                flag = false;
            } else
            {
                float f = location.getAccuracy();
                float f1 = location1.getAccuracy();
                int i = (int)(f - f1);
                boolean flag2;
                boolean flag3;
                boolean flag5;
                String s;
                String s1;
                boolean flag6;
                if(i > 0)
                    flag3 = true;
                else
                    flag3 = false;
                if(i < 0)
                    flag5 = true;
                else
                    flag5 = false;
                if(i > 200)
                    flag2 = true;
                else
                    flag2 = false;
                s = location.getProvider();
                s1 = location1.getProvider();
                flag6 = isSameProvider(s, s1);
                if(flag5)
                    flag = true;
                else
                if(flag4 && !flag3)
                    flag = true;
                else
                if(flag4 && !flag2 && flag6)
                    flag = true;
                else
                    flag = false;
            }
        }
        return flag;
    }

    private static boolean isSameProvider(String s, String s1)
    {
        boolean flag;
        if(s == null)
        {
            if(s1 == null)
                flag = true;
            else
                flag = false;
        } else
        {
            flag = s.equals(s1);
        }
        return flag;
    }

    public static String toShortString(Location location)
    {
        String s;
        if(location == null)
        {
            s = null;
        } else
        {
            StringBuilder sb = new StringBuilder();
            sb.append("{provider:");
            String s1 = location.getProvider();
            sb.append(s1).append(", lat/lng:(");
            double d = location.getLatitude();
            sb.append(d).append(',');
            double d1 = location.getLongitude();
            sb.append(d1).append("), accuracy:");
            float f = location.getAccuracy();
            sb.append(f).append('}');
            s = sb.toString();
        }
        return s;
    }

//    private static final int TWO_MINUTES = 0x1d4c0;
}
