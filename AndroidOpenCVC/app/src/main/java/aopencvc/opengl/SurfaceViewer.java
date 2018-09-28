package aopencvc.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import org.opencv.core.Mat;

public class SurfaceViewer extends GLSurfaceView {

    private ObjectRenderer renderer;

    public SurfaceViewer(Context context){
        super(context);
        init();
    }

    public SurfaceViewer(Context context, AttributeSet attr){
        super(context);
        init();
    }

    public void init(){
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        renderer = new ObjectRenderer();
        setRenderer(renderer);

    }

    public void putCameraRotation(Mat cr){
        renderer.putCameraRotation(cr);
    }

    public void putCameraTranslation(Mat ct){
        renderer.putCameraTranslation(ct);
    }

    public void putWorldPosPoint(Mat wpp){
        renderer.putWorldPosPoint(wpp);
    }

}