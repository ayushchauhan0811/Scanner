package aac.scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static java.lang.Boolean.TRUE;
import static org.opencv.imgproc.Imgproc.contourArea;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static String TAG = "Scanner";
    private JavaCameraView javaCameraView;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG,"onManagerConnected");
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private Mat rgba,gray,canny;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try{
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .debuggable(true)
                    .build();
            Fabric.with(fabric);
        } catch (Exception e){
            Log.e(TAG,"Exception",e);
        }

        int hasReadPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA);
        if(hasReadPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    10);
        }

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        rgba = new Mat(height,width, CvType.CV_8UC4);
        gray = new Mat(height,width, CvType.CV_8UC1);
        canny = new Mat(height,width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        rgba.release();
        gray.release();
        canny.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        List<MatOfPoint> contours = new ArrayList<>();
        MatOfPoint2f matOfPoint2f_1 ;
        MatOfPoint2f approxCurves = new MatOfPoint2f();
        MatOfPoint2f approx = new MatOfPoint2f();
        MatOfPoint mat = new MatOfPoint();
        Double epsilon, maxArea = 0.0, area;
        rgba = inputFrame.rgba();
        Imgproc.cvtColor(rgba,gray,Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(gray,gray,new Size(5,5),0);
        Imgproc.Canny(gray,canny,75,200);
        Imgproc.findContours(canny,contours, new Mat(),Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        for(int i=0;i<contours.size();i++){
            area = contourArea(contours.get(i));
            matOfPoint2f_1 = new MatOfPoint2f(contours.get(i).toArray());
            epsilon = Imgproc.arcLength(matOfPoint2f_1,TRUE)*0.02;
            Imgproc.approxPolyDP(matOfPoint2f_1,approxCurves,epsilon,TRUE);

            if(area > maxArea && approxCurves.total() == 4){
                maxArea = area;
                approx = approxCurves;
            }
        }

        contours.clear();
        approx.convertTo(mat,CvType.CV_32S);
        contours.add(mat);
        Imgproc.drawContours(rgba,contours,-1, new Scalar(0,255,0),2);
        return rgba;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }
}
