#include <aac_scanner_ScannerNative.h>
#include <android/log.h>

#define APPNAME "Scanner"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, APPNAME, __VA_ARGS__))

/*
 * Class:     aac_scanner_ScannerNative
 * Method:    drawContours
 * Signature: (J)V
 */
JNIEXPORT jfloatArray JNICALL Java_aac_scanner_ScannerNative_drawContours(JNIEnv *env, jclass thiz, jlong addrRgba){

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

    canny.release();
    gray.release();

    for(int i=0;i<contours.size();i++){
        area = contourArea(contours[i]);
        epsilon = arcLength(contours[i], true)*0.02;
        approxPolyDP(contours[i], approxCurves, epsilon, true);

        if(area > maxArea && approxCurves.size() == 4 && isContourConvex(Mat(approxCurves))){
            maxArea = area;
            approx = approxCurves;
        }
    }

    contours.erase(contours.begin(),contours.end());
    contours.push_back(approx);
    drawContours(rgba,contours,-1, Scalar(0,255,0), 2);

    if(maxArea != 0.0) {
        jfloatArray jArray = env->NewFloatArray(8);
        if (jArray != NULL) {
            jfloat *ptr = env->GetFloatArrayElements(jArray, NULL);

            for (int i=0,j=i+4; j<8; i++,j++) {
                ptr[i] = approx[i].x;
                ptr[j] = approx[i].y;
            }
            env->ReleaseFloatArrayElements(jArray, ptr, NULL);
        }
        return jArray;
    }
    else {
        return 0;
    }
}


