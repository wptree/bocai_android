
package com.bocai.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Rotate3dAnimation extends Animation
{

    public Rotate3dAnimation(float fromDegrees, float toDegrees, float centerX, float CenterY, float depthZ, boolean reverse)
    {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = CenterY;
        mDepthZ = depthZ;
        mReverse = reverse;
    }

    protected void applyTransformation(float f, Transformation transformation)
    {
        float f2 = (mToDegrees - mFromDegrees) * f;
        float f3 = mFromDegrees + f2;
        float f4 = mCenterX;
        float f5 = mCenterY;
        Camera camera = mCamera;
        Matrix matrix = transformation.getMatrix();
        camera.save();
        float f7;
        float f8;
        if(mReverse)
        {
            float f6 = mDepthZ * f;
            camera.translate(0F, 0F, f6);
        } else
        {
            float f9 = mDepthZ;
            float f10 = 1F - f;
            float f11 = f9 * f10;
            camera.translate(0F, 0F, f11);
        }
        camera.rotateY(f3);
        camera.getMatrix(matrix);
        camera.restore();
        f7 = -f4;
        f8 = -f5;
        matrix.preTranslate(f7, f8);
        matrix.postTranslate(f4, f5);
    }

    public void initialize(int i, int j, int k, int l)
    {
        super.initialize(i, j, k, l);
        Camera camera = new Camera();
        mCamera = camera;
    }

    private Camera mCamera;
    private final float mCenterX;
    private final float mCenterY;
    private final float mDepthZ;
    private final float mFromDegrees;
    private final boolean mReverse;
    private final float mToDegrees;
}
