#include <aac_scanner_ScannerNative.h>
#include <android/log.h>

#define APPNAME "Scanner"
/*
 * Class:     aac_scanner_ScannerNative
 * Method:    drawContours
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_aac_scanner_ScannerNative_drawContours
  (JNIEnv *, jclass, jlong addrRgba){

  Mat& rgba = *(Mat*) addrRgba;
  Mat gray = rgba.clone();
  Mat canny = rgba.clone();

  vector< vector<Point> > contours;
  vector<Vec4i> hierarchy;
  vector<Point> approx, approxCurves;

  double area,maxArea=0.0,epsilon;

  cvtColor(rgba, gray, CV_RGBA2GRAY);
  GaussianBlur(gray, gray, Size(5,5), 0, 0);
  Canny(gray,canny,75,200);
  findContours( canny, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

  for(int i=0;i<contours.size();i++){
    area = contourArea(contours[i]);
    epsilon = arcLength(contours[i], true)*0.02;
    approxPolyDP(contours[i], approxCurves, epsilon, true);

    if(area > maxArea && approxCurves.size() == 4){
      maxArea = area;
      approx = approxCurves;
    }
  }

  contours.erase(contours.begin(),contours.end());
  contours.push_back(approx);
  drawContours(rgba,contours,0, Scalar(0,255,0), 2);
}


