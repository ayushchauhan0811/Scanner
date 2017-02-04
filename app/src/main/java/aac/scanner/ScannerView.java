package aac.scanner;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by Ayush on 31-01-2017.
 */

public class ScannerView extends JavaCameraView implements Camera.PictureCallback {

    private String TAG = "Scanner";
    private String mPictureFileName;

    public ScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<String> getEffectList() {
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
    }

    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(int h,int w) {
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(mFrameWidth, mFrameHeight);
        mCamera.setParameters(params); // mCamera is a Camera object
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        System.gc();
        // PictureCallback is implemented by the current class
        try {
            mCamera.setPreviewCallback(null);
            mCamera.takePicture(null, null, this);
        } catch (Exception e) {
            Log.e(TAG,"takePicture failed",e);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            Toast.makeText(getContext(),"New Image saved:" + mPictureFileName,Toast.LENGTH_SHORT).show();
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in photoCallback", e);
            Toast.makeText(getContext(), "Image could not be saved", Toast.LENGTH_LONG).show();
        }
    }
}
