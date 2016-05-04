package com.example.lian.elaopencvcam01;

// Android Classes
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

//OpenCV Classes
import org.opencv.android.JavaCameraView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;


import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, View.OnClickListener{

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean              mIsJavaCamera = true;
    private MenuItem  mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    private Button buttonElaDown;  //przycciiiisk do zmianry orientacji
    private Button buttonTakePhoto;
    private TextView textViewInfo;
    int licznik=0;
    int screenRotation=1; //0 - pionowo, 1-poziomo, 2 - pionowo odwrotnie tel, 3-poziomo w 2 strone


    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.opencv_tutorial_activity_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        //mOpenCvCameraView.setMaxFrameSize(1000,1000);
       /* Camera.Parameters params = (Camera) mCamera.getParameters();
        List<Size> resList = mCamera.getParameters().getSupportedPictureSizes();
        int listNum = 1;// 0 is the maximum resolution
        int width = (int) resList.get(listNum).width;
        int height = (int) resList.get(listNum).height;
        params.setPictureSize(width, height);
        mCamera.setParameters(params);*/



        buttonElaDown = (Button) findViewById(R.id.button_orientation);
        buttonElaDown.setOnClickListener(this);
        buttonTakePhoto = (Button) findViewById(R.id.button_take_photo);
        buttonTakePhoto.setOnClickListener(this);

        textViewInfo = (TextView) findViewById(R.id.text_view_info);
    }

    //bylo puste
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        //mRgbaT = new Mat(width, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, height, CvType.CV_8UC4);//ela
    }

    //bylo puste
    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    //dopisane, bo bylo tylko> return inputFrame.rgba();
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        if (screenRotation == 1)return mRgba;  //landscape
        if (screenRotation == 0){ //portrait
            //obraca w prawo juz bo zmienilam
            Core.transpose(mRgba, mRgbaT);
            Core.flip(mRgbaT, mRgbaT, 1);
            Imgproc.resize(mRgbaT, mRgbaT, mRgba.size()); // Imgproc.resize(src, dst, dsize )   OK bylo
            return mRgbaT;
        }
        if (screenRotation == 3) {  //reverse landscape
            Core.flip(mRgba, mRgbaF, -1); //Core.flip(src, dst, -1); odwraca do gory nogami
            return mRgbaF;
        }
        return inputFrame.rgba();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onClick(View v) {
        String orientationName="orient: none";
        switch (v.getId()){
            case R.id.button_take_photo:
                saveImage(mRgba);
                break;
            case R.id.button_orientation:
                Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                screenRotation = display.getRotation();

                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    orientationName = "orient: landscape";
                }else  if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                    orientationName = "orient: portrait";
                }
                textViewInfo.setText("Klikk" + licznik + orientationName + "obrot" + screenRotation );
                licznik++;
                break;
        }
    }

    private boolean saveImage(Mat mat){

        Mat matToSave = new Mat();
        Imgproc.cvtColor(mat,matToSave,Imgproc.COLOR_BGR2RGBA);


        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyAppDir");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                textViewInfo.setText("failed to create directory");
                return false;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "testimage_" + timeStamp + ".png");

        Boolean bool = Imgcodecs.imwrite(mediaFile.getPath(),matToSave);

        if (bool) {
            Log.i(TAG, "SUCCESS writing image to external storage");
            textViewInfo.setText("SUCCESS writing image to external storage" + mat.size());
        }
        else {
            Log.i(TAG, "Fail writing image to external storage");
            textViewInfo.setText("Fail writing image to external storage");
        }

        return true;
    }




}
