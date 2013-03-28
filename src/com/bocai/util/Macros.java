
package com.bocai.util;

import android.content.SharedPreferences;
import android.location.Location;
import com.bocai.BocaiApplication;
import java.util.*;

public class Macros
{

    public Macros()
    {
    }

    public static Map<String, String> ACTION_COMMENTS_LOADED()
    {
        return Collections.singletonMap("action", "comments-loaded");
    }

    public static Map<String, String> ACTION_DONE_UNAUTHORIZED()
    {
        return Collections.singletonMap("action", "unauthorized");
    }

    public static Map<String, Object> ACTION_PAGES(Object obj)
    {
        HashMap<String, Object> hashMap = new HashMap<String, Object>(2);
        hashMap.put("action", "total");
        hashMap.put("total", obj.toString());
        return hashMap;
    }

    public static Map<String, String> ACTION_PERSON_LOADED()
    {
        return Collections.singletonMap("action", "person-loaded");
    }

    public static Map<String, Object> ACTION_REVIEW(Object obj)
    {
        HashMap<String, Object> hashMap = new HashMap<String, Object>(2);
        hashMap.put("action", "review");
        hashMap.put("review", obj);
        return hashMap;
    }

    public static final void CACHE_EXPIRE(String s)
    {
        gObjectCache.remove(s);
    }

    public static final Object CACHE_GET(String s)
    {
        return gObjectCache.get(s);
    }

    public static final void CACHE_SET(String s, Object obj)
    {
        gObjectCache.put(s, obj);
    }

    public static final BocaiApplication FS_APPLICATION()
    {
        return BocaiApplication.instance;
    }

    public static final Location FS_CURRENT_LOCATION()
    {
        return BocaiApplication.instance.currentLocation;
    }

    public static final SharedPreferences FS_DEFAULTS()
    {
        return BocaiApplication.instance.getSharedPreferences("FSDefaults", 0);
    }

    public static final android.content.SharedPreferences.Editor FS_DEFAULTS_EDITOR()
    {
        return FS_DEFAULTS().edit();
    }

    public static final boolean FS_DEFAULT_GET_BOOL(String s)
    {
        return FS_DEFAULTS().getBoolean(s, false);
    }

    public static final int FS_DEFAULT_GET_INT(String s)
    {
        return FS_DEFAULTS().getInt(s, 0);
    }

    public static final long FS_DEFAULT_GET_LONG(String s)
    {
        return FS_DEFAULTS().getLong(s, 0L);
    }

    public static final String FS_DEFAULT_GET_STRING(String s)
    {
        return FS_DEFAULTS().getString(s, null);
    }

    public static final void FS_DEFAULT_REMOVE(String s)
    {
        android.content.SharedPreferences.Editor editor = FS_DEFAULTS_EDITOR();
        if(editor == null)
        {
            return;
        } else
        {
            editor.remove(s);
            editor.commit();
            return;
        }
    }

    public static final void FS_DEFAULT_SET_BOOL(String s, boolean flag)
    {
        android.content.SharedPreferences.Editor editor = FS_DEFAULTS_EDITOR();
        if(editor == null)
        {
            return;
        } else
        {
            editor.putBoolean(s, flag);
            editor.commit();
            return;
        }
    }

    public static final void FS_DEFAULT_SET_INT(String s, int i)
    {
        android.content.SharedPreferences.Editor editor = FS_DEFAULTS_EDITOR();
        if(editor == null)
        {
            return;
        } else
        {
            editor.putInt(s, i);
            editor.commit();
            return;
        }
    }

    public static final void FS_DEFAULT_SET_LONG(String s, long l)
    {
        android.content.SharedPreferences.Editor editor = FS_DEFAULTS_EDITOR();
        if(editor == null)
        {
            return;
        } else
        {
            editor.putLong(s, l);
            editor.commit();
            return;
        }
    }

    public static final void FS_DEFAULT_SET_STRING(String s, String s1)
    {
        android.content.SharedPreferences.Editor editor = FS_DEFAULTS_EDITOR();
        if(editor == null)
        {
            return;
        } else
        {
            editor.putString(s, s1);
            editor.commit();
            return;
        }
    }

    public static final String FS_ACTION = "action";
    public static final String FS_ACTION_COMMENTS_LOADED = "comments-loaded";
    public static final String FS_ACTION_COMMENT_ADDED = "comment-added";
    public static final String FS_ACTION_GREAT_FIND = "great_find";
    public static final String FS_ACTION_GREAT_SHOT = "great_shot";
    public static final String FS_ACTION_NOM = "nom";
    public static final String FS_ACTION_PAGES = "total";
    public static final String FS_ACTION_PERSON_LOADED = "person-loaded";
    public static final String FS_ACTION_REVIEW = "review";
    public static final String FS_ACTION_UNAUTHORIZED = "unauthorized";
    public static final String FS_ACTION_WANT = "want";
    public static final String FS_CURRENT_USER = "current-user:";
    public static final String FS_DEFAULTS_NAME = "FSDefaults";
    public static final int FS_DEFAULT_FILTER_AREA = 2;
    public static final int FS_DEFAULT_FILTER_GUIDE = 1;
    public static final int FS_DEFAULT_FILTER_PERSON_FOLLOWING = 1;
    public static final int FS_DEFAULT_FILTER_PERSON_SPOTTED_SORT = 1;
    public static final int FS_DEFAULT_FILTER_PERSON_WANTED_SORT = 1;
    public static final int FS_DEFAULT_FILTER_RESULTS = 1;
    public static final int FS_DEFAULT_FILTER_SORT = 1;
    public static final String FS_DEFAULT_KEY_FILTER_AREA = "FSFilterArea";
    public static final String FS_DEFAULT_KEY_FILTER_GUIDE = "FSGuideFilter";
    public static final String FS_DEFAULT_KEY_FILTER_PERSON_FOLLOWING = "FSFilterFollowing";
    public static final String FS_DEFAULT_KEY_FILTER_PERSON_SPOTTED_SORT = "FSFilterSortSpotted";
    public static final String FS_DEFAULT_KEY_FILTER_PERSON_WANTED_SORT = "FSFilterSortWanted";
    public static final String FS_DEFAULT_KEY_FILTER_RESULTS = "FSFilterResults";
    public static final String FS_DEFAULT_KEY_FILTER_SORT = "FSFilterSort";
    public static final String FS_DEFAULT_KEY_UPDATE_TIME = "FSAppUpdateTime";
    public static HashMap<String, Object> gObjectCache = new HashMap<String, Object>();

}
