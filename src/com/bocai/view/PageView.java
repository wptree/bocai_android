
package com.bocai.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class PageView extends RelativeLayout
{

    public PageView(Context context)
    {
        super(context);
    }

    public PageView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
    }

    public PageView(Context context, AttributeSet attributeset, int i)
    {
        super(context, attributeset, i);
    }

    public android.view.ViewGroup.LayoutParams getLayoutParams()
    {
        android.view.ViewGroup.LayoutParams layoutparams = super.getLayoutParams();
        View view = (View)getParent();
        if(view != null && layoutparams != null)
        {
            int i = view.getHeight();
            layoutparams.height = i;
        }
        String s = (new StringBuilder()).append("getLayoutParams <- ").append(layoutparams).toString();
        Log.d("PageView", s);
        return layoutparams;
    }
}
