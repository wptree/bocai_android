// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StreamView.java

package com.bocai.view;

import android.content.Context;
import android.util.AttributeSet;

import com.bocai.widget.HorizontalListView;
import com.sonyericsson.util.Dynamics;

public class StreamView extends HorizontalListView
{
    class SimpleDynamics extends Dynamics
    {

        protected void onUpdate(int i)
        {
            float f = getDistanceToLimit();
            if(f != 0F)
            {
                float f1 = mSnapToFactor * f;
                mVelocity = f1;
                if(Math.abs(f) < 50F)
                {
                    float f2 = mVelocity * 1.25F;
                    mVelocity = f2;
                }
            }
            float f3 = mPosition;
            float f4 = mVelocity;
            float f5 = i;
            float f6 = (f4 * f5) / 1000F;
            float f7 = f3 + f6;
            mPosition = f7;
            float f8 = mVelocity;
            float f9 = mFrictionFactor;
            float f10 = f8 * f9;
            mVelocity = f10;
        }

        private float mFrictionFactor;
        private float mSnapToFactor;
        final StreamView this$0;

        public SimpleDynamics(float f, float f1)
        {
        	super();
            this$0 = StreamView.this;
            
            mFrictionFactor = f;
            mSnapToFactor = f1;
        }
    }


    public StreamView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        super.setClipChildren(false);
        super.setClipToPadding(false);
        SimpleDynamics simpledynamics = new SimpleDynamics(0.9F, 4F);
        super.setDynamics(simpledynamics);
        super.setSpacing(2);
    }

    public static final int PAGING_VIEW_TAG = 1;
}
