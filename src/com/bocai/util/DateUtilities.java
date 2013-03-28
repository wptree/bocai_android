
package com.bocai.util;

import java.text.SimpleDateFormat;
import java.util.*;

import com.bocai.R;

public class DateUtilities
{

    public DateUtilities()
    {
    }

    private static String _unitHelper(int num, String unit)
    {
        String s1;
        if(num == 1)
            s1 = (new StringBuilder()).append("1 ").append(unit).append(" ").append(Macros.FS_APPLICATION().getString(R.string.from_now)).toString();
        else
        if(num == -1)
            s1 = (new StringBuilder()).append("1 ").append(unit).append(" ").append(Macros.FS_APPLICATION().getString(R.string.ago)).toString();
        else
        if(num > 0)
            s1 = (new StringBuilder()).append(num).append(" ").append(unit).append(Macros.FS_APPLICATION().getString(R.string.from_now_s)).toString();
        else
        if(num < 0)
        {
            StringBuilder stringbuilder = new StringBuilder();
            int j = Math.abs(num);
            s1 = stringbuilder.append(j).append(" ").append(unit).append(Macros.FS_APPLICATION().getString(R.string.ago_s)).toString();
        } else
        {
            s1 = null;
        }
        return s1;
    }

    private static String computeRelativeDate(Calendar calendar, int i, int j, int k, int l, int i1, int j1)
    {
        String s;
        if(i != 0)
        {
            SimpleDateFormat simpledateformat = regularFormat;
            Date date = calendar.getTime();
            s = simpledateformat.format(date);
        } else
        if(j != 0)
            s = _unitHelper(j, Macros.FS_APPLICATION().getString(R.string.month));
        else
        if(k != 0)
            s = _unitHelper(k, Macros.FS_APPLICATION().getString(R.string.day));
        else
        if(l != 0)
            s = _unitHelper(l, Macros.FS_APPLICATION().getString(R.string.hour));
        else
        if(i1 != 0)
            s = _unitHelper(i1,Macros.FS_APPLICATION().getString(R.string.minute));
        else
            s = _unitHelper(j1,Macros.FS_APPLICATION().getString(R.string.second));
        return s;
    }

    private static String getRelativeDate(Calendar calendar)
    {
        Calendar calendar1 = GregorianCalendar.getInstance();
        int i = calendar.get(1);
        int j = calendar1.get(1);
        int k = i - j;
        int l = calendar.get(2);
        int i1 = calendar1.get(2);
        int j1 = l - i1;
        int k1 = calendar.get(5);
        int l1 = calendar1.get(5);
        int i2 = k1 - l1;
        int j2 = calendar.get(11);
        int k2 = calendar1.get(11);
        int l2 = j2 - k2;
        int i3 = calendar.get(12);
        int j3 = calendar1.get(12);
        int k3 = i3 - j3;
        int l3 = calendar.get(13);
        int i4 = calendar1.get(13);
        int j4 = l3 - i4;
        return computeRelativeDate(calendar, k, j1, i2, l2, k3, j4);
    }

    public static String getRelativeDate(Date date)
    {
        String s;
        if(date == null)
        {
            s = null;
        } else
        {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            s = getRelativeDate(calendar);
        }
        return s;
    }

    public static final SimpleDateFormat ISO8601Format;
    public static final SimpleDateFormat regularFormat = new SimpleDateFormat("EEE, MMM d yyyy");

    static 
    {
        ISO8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat simpledateformat = ISO8601Format;
        TimeZone timezone = TimeZone.getTimeZone("UTC");
        simpledateformat.setTimeZone(timezone);
    }
}
