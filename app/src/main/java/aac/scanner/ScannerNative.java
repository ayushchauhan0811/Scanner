package aac.scanner;

import org.opencv.core.MatOfPoint;

/**
 * Created by Ayush on 31-01-2017.
 */

public class ScannerNative {
    public native static float[] drawContours(long matAddrRgba);
}
