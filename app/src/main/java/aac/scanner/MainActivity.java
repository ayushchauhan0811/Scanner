package aac.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.fabric.sdk.android.Fabric;

import static aac.scanner.R.string.fileName;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static String TAG = "Scanner";
    private ScannerView scannerView;
    float[] points;
    private static boolean safeToTakePicture;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG,"onManagerConnected");
                    scannerView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private Mat rgba,original;

    static {
        System.loadLibrary("Scanner");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .debuggable(true)
                    .build();
            Fabric.with(fabric);
        } catch (Exception e){
            Log.e(TAG,"Exception",e);
        }

        checkPermissions();
        safeToTakePicture = true;
        scannerView = (ScannerView) findViewById(R.id.java_camera_view);
        scannerView.setVisibility(View.VISIBLE);
        scannerView.setCvCameraViewListener(this);
        scannerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(points != null && points.length == 8){
                    Log.d(TAG,"" + points[0] + "x" + points[4]);
                    Log.d(TAG,"" + points[1] + "x" + points[5]);
                    Log.d(TAG,"" + points[2] + "x" + points[6]);
                    Log.d(TAG,"" + points[3] + "x" + points[7]);

                    File pictureFileDir = getDir();

                    if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
                        Log.d(TAG, "Can't create directory to save image.");
                    } else if(safeToTakePicture) {
                        safeToTakePicture = false;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
                        String currentDateAndTime = sdf.format(new Date());
                        String fileName = pictureFileDir.getPath() + File.separator +
                                        "scanner_" + currentDateAndTime + ".jpg";
                        Imgcodecs.imwrite(fileName,original);
                        Log.d(TAG,fileName);
                        Intent intent = new Intent(getApplicationContext(),ScanImageActivity.class);
                        intent.putExtra(getString(R.string.points),points);
                        intent.putExtra(getString(R.string.fileName),fileName);
                        startActivity(intent);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"No document detected",Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    public static File getDir() {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "Scanner");
    }

    private void checkPermissions() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA);
        if(hasCameraPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    10);
        }

        int hasStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(hasStoragePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    10);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        rgba = new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        rgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        rgba = inputFrame.rgba();
        original = rgba.clone();
        // Finding contours that represents the piece of paper being scanned
        points = ScannerNative.drawContours(rgba.getNativeObjAddr());
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
            safeToTakePicture = true;
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scannerView != null) {
            scannerView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scannerView != null) {
            scannerView.disableView();
        }
    }
}
