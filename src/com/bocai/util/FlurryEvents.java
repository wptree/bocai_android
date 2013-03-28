
package com.bocai.util;

import android.location.Location;
import com.bocai.R;
import java.util.Collections;
import java.util.Map;

public class FlurryEvents
{

    public FlurryEvents()
    {
    }

    public static final String FLURRY_LOCATE_ME()
    {
        return Macros.FS_APPLICATION().getString(R.string.flurry_locate_me);
    }

    public static final void FLURRY_LOG(String s)
    {
        Macros.FS_APPLICATION().flurryLogEvent(s);
    }

    public static final void FLURRY_LOG_FROM_PAGE(String s, String s1)
    {
        Map<String,String> map = Collections.singletonMap("page", s1);
        FLURRY_LOG_MAP(s, map);
    }

    public static final void FLURRY_LOG_LOCATE_ME(String s)
    {
        FLURRY_LOG_FROM_PAGE(FLURRY_LOCATE_ME(), s);
    }

    public static final void FLURRY_LOG_MAP(String s, Map<String,String> map)
    {
        Macros.FS_APPLICATION().flurryLogEvent(s, map);
    }

    public static final void FLURRY_LOG_SPOT_NEW_FOOD(String s)
    {
        FLURRY_LOG_WITH_NAME(FLURRY_SPOT_NEW_FOOD(), s);
    }

    public static final void FLURRY_LOG_WITH_NAME(String s, String s1)
    {
        Map<String,String> map = Collections.singletonMap("name", s1);
        FLURRY_LOG_MAP(s, map);
    }

    public static final String FLURRY_SEARCH_EMPTY()
    {
        return Macros.FS_APPLICATION().getString(R.string.flurry_search_empty);
    }

    public static final void FLURRY_SET_LOCATION(Location location)
    {
        Macros.FS_APPLICATION().flurrySetLocation(location);
    }

    public static final void FLURRY_SET_USER(String s)
    {
    }

    public static final String FLURRY_SPOT_NEW_FOOD()
    {
        return Macros.FS_APPLICATION().getString(R.string.flurry_spot_new_food);
    }

    public static final String FLURRY_SPOT_UPLOADED()
    {
        return Macros.FS_APPLICATION().getString(R.string.flurry_spot_uploaded);
    }

    public static final String FLURRY_STREAM_PAGES()
    {
        return Macros.FS_APPLICATION().getString(R.string.flurry_stream_pages);
    }

    public static final String FLURRY_WANTED_EMPTY()
    {
        return Macros.FS_APPLICATION().getString(R.string.flurry_wanted_empty);
    }

    public static final String FLURRY_API_KEY = "VXPZYJGUGUPQMF56N166";
}
