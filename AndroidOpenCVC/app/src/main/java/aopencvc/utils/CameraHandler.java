package aopencvc.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.sql.Timestamp;

import aopencvc.opengl.SurfaceViewer;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_64F;

public class CameraHandler implements CameraBridgeViewBase.CvCameraViewListener2 {


    private Context context;
    private SLAMHandler slamHandler;
    private Mat cameraRotation;
    private Mat planeEq;
    private Mat cameraPose;
    private SurfaceViewer mGLView;

    public CameraHandler(Context context, SLAMHandler slamHandler, SurfaceViewer mGLView) {
        this.context = context;
        this.slamHandler = slamHandler;
        cameraRotation = new Mat(1,3, CV_64F, new Scalar(0.0));
        planeEq = new Mat(1,4, CV_32F, new Scalar(0.0));
        cameraPose = new Mat(4,4, CV_64F, new Scalar(0.0));
        this.mGLView = mGLView;



    }



    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat image = inputFrame.rgba();
        /*int irows = image.rows();
        int icols = image.cols();

        int rows = 480;
        int cols = rows*icols/irows;

        Mat resized = new Mat();
        Size size = new Size(cols,rows);
        Imgproc.resize(image,resized,size);

        Log.d("SD-SLAM", "Image resized to ");


        */

        cameraRotation = new Mat(1,3, CV_64F, Scalar.all(0.0));
        // Process frame
        String res = slamHandler.TrackFrame(image, cameraRotation, planeEq, cameraPose);
        Log.d("SD-SLAM", "Pose es " + res);
        mGLView.putCameraRotation(cameraRotation);
        mGLView.putPlaneEquation(planeEq);
        mGLView.putCameraPose(cameraPose);

        // Resize to original size
        /*Size isize = new Size(icols, irows);
        Imgproc.resize(resized, image, isize);
           */



        return image;
    }
}
