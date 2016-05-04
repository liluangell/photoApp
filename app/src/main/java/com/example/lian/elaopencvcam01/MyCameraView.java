package com.example.lian.elaopencvcam01;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.util.List;

/**
 * Created by lian on 2016-04-25.
 */
public class MyCameraView extends JavaCameraView implements Camera.PictureCallback{

    private static final String TAG = "Sample::Tutorial3View";
    private String mPictureFileName;

    public MyCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //obrazy ------- efekty na obrazach
    /*public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }
    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }*/

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }




    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }
}
