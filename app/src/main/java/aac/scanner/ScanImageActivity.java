package aac.scanner;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class ScanImageActivity extends AppCompatActivity {

    private ImageView scannedImage;
    private float[] points;
    private float minX, minY, maxX, maxY;
    private String fileName;
    private Point p1,p2;
    private Mat src;
    private File image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_image);

        scannedImage = (ImageView) findViewById(R.id.scanned_image);

        Bundle bundle = getIntent().getExtras();
        points = bundle.getFloatArray(getString(R.string.points));
        fileName = bundle.getString(getString(R.string.fileName));
        image = new File(fileName);
        scannedImage.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
        drawRectangle();


    }

    private void drawRectangle() {
        assert points != null;
        minX = Math.min(Math.min(points[0],points[1]),Math.min(points[2],points[3]));
        maxX = Math.max(Math.max(points[0],points[1]),Math.max(points[2],points[3]));
        minY = Math.min(Math.min(points[4],points[5]),Math.min(points[6],points[7]));
        maxY = Math.max(Math.max(points[4],points[5]),Math.max(points[6],points[7]));

        p1 = new Point(minX,minY);
        p2 = new Point(maxX,maxY);

        src = Imgcodecs.imread(fileName);
        Imgproc.rectangle(src,p1,p2,new Scalar(0,255,0));
        Imgcodecs.imwrite(fileName,src);
        scannedImage.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
    }


}
