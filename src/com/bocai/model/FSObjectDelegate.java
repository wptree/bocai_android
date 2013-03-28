
package com.bocai.model;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public interface FSObjectDelegate
{

    public abstract void FSResponse(List<FSObject> list);

    public abstract void displayErrors(JSONObject jsonObject)
        throws JSONException;

    public abstract void displaySuccess(JSONObject jsonObject)
        throws JSONException;

    public abstract void doSearchWithName(String name);

    public abstract void finishedAction(JSONObject jsonObject)
        throws JSONException;
}
