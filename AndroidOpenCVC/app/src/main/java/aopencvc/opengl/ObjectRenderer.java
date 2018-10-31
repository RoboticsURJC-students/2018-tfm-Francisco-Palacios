package aopencvc.opengl;

import javax.microedition.khronos.egl.EGLConfig;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.lang.reflect.Array;
import java.nio.FloatBuffer;
import java.util.Arrays;


import javax.microedition.khronos.opengles.GL10;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_64F;


public class ObjectRenderer implements GLSurfaceView.Renderer {

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    private float[] mOrthoMatrix = new float[16];
    private float[] mCurrentRotationTranslation = new float[16];



    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** This will be used to pass in model color information. */
    private int mColorHandle;

    private int width = 1280;

    private int height = 720;

    private ObjectInit init;

    /**
     * How many bytes per float.
     */
    private final int mBytesPerFloat = 4;


    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];

    /** How many elements per vertex. */
    private final int mStrideBytes = 3 * mBytesPerFloat;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;


    private float cx = 644.2552f;

    private float cy = 347.0074f;

    private float fx = 1080.3387f;

    private float fy = 1081.3989f;

    private Mat cameraRotation;
    private Mat planeEquation;
    private Mat cameraPose;

    private CamTrail camTrail;




    public ObjectRenderer(){
        cameraRotation = new Mat(1,3, CV_64F, Scalar.all(0.0));
        planeEquation = new Mat(1,4, CV_32F, Scalar.all(0.0));
        cameraPose = new Mat(4,4, CV_64F, Scalar.all(0.0));
        camTrail = new CamTrail();
    }

    public void putCameraRotation(Mat cr){
        cameraRotation = cr;
    }

    public void putPlaneEquation(Mat pe){
        planeEquation = pe;
    }

    public void putCameraPose(Mat cp){
        cameraPose = cp;
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                | GLES20.GL_DEPTH_BUFFER_BIT);


        Matrix.setIdentityM(mModelMatrix, 0);

       // System.out.println("Ortho: " +Arrays.toString(mOrthoMatrix));
        float[] mPlaneParams = new float[4];
        for (int i = 0;mPlaneParams.length>i;i++){
            mPlaneParams[i] = (float) planeEquation.get(0,i)[0];
            //System.out.println("Punto param z: " + (float) planeEquation.get(0,i)[0]);
        }
        planeEquation.get(0,0,mPlaneParams);

        for (int i = 0;mPlaneParams.length>i;i++){
            System.out.println("Params plano: " + mPlaneParams[i]);
        }

        float[] point = {0.0f,0.0f,-mPlaneParams[2]/mPlaneParams[3]};


        System.out.println("Valor de z del modelo: " + point[2]);

        //Debemos rotar la normal 180º alrededor de la X como hicimos con la pose. Esto es por que
        //aun que anteriormente no rotamos lo puntos, ahora la normal esta mal calculada y esta apuntando e
        // Z positivo, cuando lo que queremos es que apunte en Z negativo.

        //Una rotacion de ese estilo pondria los parametros de Y y Z en direccion contraria, ya que
        //estamos dando media vuelta alrededor de la X.

        float[] normal = {mPlaneParams[0],-mPlaneParams[1],-mPlaneParams[2]};


        Matrix.translateM(mModelMatrix,0, point[0], point[1], point[2]);
        float[] modelRotation = parallelizeVectors(new float[] {0.0f,1.0f,0.0f}, normal);
        System.out.println("Modelo: "+Arrays.toString(modelRotation));
        Matrix.rotateM(mModelMatrix, 0, modelRotation[3]*57.2958f, modelRotation[0],
                modelRotation[1],modelRotation[2]);

        Matrix.scaleM(mModelMatrix,0,0.25f,0.25f,0.25f);



        Matrix.setIdentityM(mCurrentRotationTranslation, 0);



        //System.out.println("Modelo: "+Arrays.toString(mModelMatrix))


/*
        Matrix.translateM(mCurrentRotationTranslation, 0, (float) cameraTranslation.get(0,0)[0],
                (float) cameraTranslation.get(0,1)[0],
                (float) -cameraTranslation.get(0,2)[0]);

        System.out.println("Solo translacion: "+Arrays.toString(mCurrentRotationTranslation));

        camTrail.AddTrailData(new float[] {(float) cameraTranslation.get(0,0)[0],
                                            (float) cameraTranslation.get(0,1)[0],
                                            (float) -cameraTranslation.get(0,2)[0]});
                                            */
/*
        Matrix.rotateM(mCurrentRotationTranslation, 0, (float) cameraRotation.get(0,0)[0], -1.0f,
                0.0f, 0.0f);
        Matrix.rotateM(mCurrentRotationTranslation, 0, (float) cameraRotation.get(0,1)[0], 0.0f,
                1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotationTranslation, 0, (float) cameraRotation.get(0,2)[0], 0.0f,
                0.0f, 1.0f);
*/
        for (int i = 0;i<mViewMatrix.length;i++) {
            mViewMatrix[i] = (float) cameraPose.get(i % cameraPose.cols(),
                    i / cameraPose.cols())[0];
        }

       // mCurrentRotationTranslation[15] = 1.0f;

        //Matrix.setIdentityM(mCurrentRotationTranslation, 0);
        //Matrix.rotateM(mCurrentRotationTranslation,0, -180,1.0f,0.0f,0.0f);

        //Matrix.rotateM(mViewMatrix,0, -180,1.0f,0.0f,0.0f);

       /*
        camTrail.AddTrailData(new float[] { mCurrentRotationTranslation[12],
                mCurrentRotationTranslation[13],
                mCurrentRotationTranslation[14]});
                */

       // Matrix.setIdentityM(mCurrentRotationTranslation, 0);

/*
        for (int i = 0;i<mCurrentRotationTranslation.length;i++){
            if(i%cameraPose.cols() == 0 || i%cameraPose.cols() == 3) {
                mCurrentRotationTranslation[i] = (float) cameraPose.get(i % cameraPose.cols(),
                        i / cameraPose.cols())[0];
            }else{
                mCurrentRotationTranslation[i] = -(float) cameraPose.get(i % cameraPose.cols(),
                        i / cameraPose.cols())[0];
            }
        }
        */

        System.out.println(Arrays.toString(mViewMatrix));



        //System.out.println("XYZ: "+cameraPose.get(0, 3)[0]+ " , " + cameraPose.get(1, 3)[0] + " , " + cameraPose.get(2,3)[0]);

       // System.out.println("Tranlacion y rotacion desde pose: "+Arrays.toString(mCurrentRotationTranslation));





        drawObject(init.getObjectcoordinates(),init.getGrid(),init.getCube(), camTrail.getFloatBufferTrail());


    }


    public float[] vectorialProduct(float[] v1, float[] v2){
        float[] vProdV1V2 = {v1[1]*v2[2]-v1[2]*v2[1],-(v1[0]*v2[2]-v1[2]*v2[0]),v1[0]*v2[1]-v1[1]*v2[0]};
        return vProdV1V2;
    }


    //v1 es el vector de (0,0,1)
    //Este metodo paraleliza el vector 1 con el vector 2.
    public float[] parallelizeVectors(float[] v1, float[] v2){
        float modV1 = (float) Math.sqrt(Math.pow(v1[0],2)+Math.pow(v1[1],2)+Math.pow(v1[2],2));
        float modV2 = (float) Math.sqrt(Math.pow(v2[0],2)+Math.pow(v2[1],2)+Math.pow(v2[2],2));


        float cosAngle = (v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2])/(modV1*modV2);
        float angle = (float) Math.acos(cosAngle);

        float[] vProdV1V2 = vectorialProduct(v1,v2);

        return new float[] {vProdV1V2[0],vProdV1V2[1],vProdV1V2[2],angle};
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        init = new ObjectInit();

        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_FASTEST);

        GLES20.glClearColor(0,0,0,0);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);


        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 0.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 1.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;



        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        int programHandle = init.getProgramHandle();
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetUniformLocation(programHandle, "v_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);


        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;

        float zfar = 100.0f;
        float znear = 0.5f;

        float L = -cx * znear/fx;
        float R = (width-cx) * znear/fx;
        float T = -cy * znear/fy;
        float B = (height-cy) * znear/fy;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, znear, zfar);

/*
        mProjectionMatrix[0] = 2*znear/(R-L);
        mProjectionMatrix[1] = 0.0f;
        mProjectionMatrix[2] = 0.0f;
        mProjectionMatrix[3] = 0.0f;

        mProjectionMatrix[4] = 0.0f;
        mProjectionMatrix[5] = 2*znear/(T-B);
        mProjectionMatrix[6] = cy;
        mProjectionMatrix[7] = 0.0f;

        mProjectionMatrix[8] = (R+L)/(L-R);
        mProjectionMatrix[9] = (T+B)/(B-T);
        mProjectionMatrix[10] = (zfar+znear)/(zfar-znear);
        mProjectionMatrix[11] = 1.0f;

        mProjectionMatrix[12] = 0.0f;
        mProjectionMatrix[13] = 0.0f;
        mProjectionMatrix[14] = 2.0f * zfar * znear / (znear-zfar);
        mProjectionMatrix[15] = 0.0f;
*/
        System.out.println( width + "x" + height);

        mProjectionMatrix[0] = 2*fx/width;
        mProjectionMatrix[1] = 0.0f;
        mProjectionMatrix[2] = 0.0f;
        mProjectionMatrix[3] = 0.0f;

        mProjectionMatrix[4] = 0.0f;
        mProjectionMatrix[5] = 2*fy/height;
        mProjectionMatrix[6] = 0.0f;
        mProjectionMatrix[7] = 0.0f;

        mProjectionMatrix[8] = 1.0f - (2*cx/width);
        mProjectionMatrix[9] = 2*cy/height - 1.0f;
        mProjectionMatrix[10] = -(zfar+znear)/(zfar-znear);
        mProjectionMatrix[11] = -1.0f;

        mProjectionMatrix[12] = 0.0f;
        mProjectionMatrix[13] = 0.0f;
        mProjectionMatrix[14] = -2.0f * zfar * znear / (zfar-znear);
        mProjectionMatrix[15] = 0.0f;



        //Matrix.multiplyMM(mProjectionMatrix, 0, mOrthoMatrix, 0, mProjectionMatrix, 0);



    }



    public void drawObject(final FloatBuffer CoordinatesBuffer, final FloatBuffer GridBuffer,
                           final FloatBuffer CubeBuffer, final FloatBuffer TrailBuffer){


        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        //Matrix.multiplyMM(mMVPMatrix, 0, mCurrentRotationTranslation, 0, mMVPMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glLineWidth(3.0f);




        GLES20.glEnableVertexAttribArray(mPositionHandle);


        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, GridBuffer);
        GLES20.glUniform4fv(mColorHandle, 1, new float[]{1.0f,1.0f,1.0f,1.0f}, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 40);

        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, CubeBuffer);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 8);

        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, CoordinatesBuffer);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform4fv(mColorHandle, 1, new float[]{1.0f,0.0f,0.0f,1.0f}, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
        GLES20.glUniform4fv(mColorHandle, 1, new float[]{0.0f,1.0f,0.0f,1.0f}, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 2, 2);
        GLES20.glUniform4fv(mColorHandle, 1, new float[]{0.0f,0.0f,1.0f,1.0f}, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 4, 2);

        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, TrailBuffer);
        GLES20.glUniform4fv(mColorHandle, 1, new float[]{0.0f,1.0f,0.0f,1.0f}, 0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, (int) camTrail.getTrail().length/3);


        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisable(mColorHandle);

    }


}