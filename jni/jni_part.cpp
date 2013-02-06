#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/calib3d/calib3d.hpp"
#include <vector>
#include <android/log.h>

#define APPNAME "BookAlive"
using namespace cv;
using namespace std;

extern "C" {
Mat findHomography(Mat orig, Mat test) {
    vector<KeyPoint> kp_orig, kp_test;
    FAST(orig, kp_orig, 10);
    FAST(test, kp_test, 10);

    FREAK ext;
    Mat desc_orig, desc_test;
    ext.compute(orig, kp_orig, desc_orig);
    ext.compute(test, kp_test, desc_test);

    BFMatcher matcher(NORM_HAMMING);
    vector<DMatch> matches;
    matcher.match(desc_orig, desc_test, matches);

    double min_dist = 100, max_dist = 0;
    for(size_t i=0; i<desc_orig.rows; i++) {
        double dist = matches[i].distance;
        if(dist < min_dist) min_dist = dist;
        if(dist > max_dist) max_dist = dist;
    }
    double acceptable_dist = 2*min_dist;
    vector<DMatch> good_matches;
    for(size_t i=0; i<desc_orig.rows; i++) {
        if(matches[i].distance < acceptable_dist) {
            good_matches.push_back(matches[i]);
        }
    }
    vector<Point2f> orig_pts;
    vector<Point2f> test_pts;

    for( size_t i = 0; i < good_matches.size(); i++ ) {
        //-- Get the keypoints from the good matches
        orig_pts.push_back( kp_orig[ good_matches[i].queryIdx ].pt );
        test_pts.push_back( kp_test[ good_matches[i].trainIdx ].pt );
    }
    Mat H = findHomography( orig_pts, test_pts, CV_RANSAC );
    return H;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_BookAlive_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);
JNIEXPORT void JNICALL Java_org_opencv_samples_BookAlive_Sample4Mixed_Draw(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);
    JNIEXPORT void JNICALL Java_org_opencv_samples_BookAlive_Sample4Mixed_mapPoints(JNIEnv*, jobject, jlong, jlong, jlong);

JNIEXPORT void JNICALL Java_org_opencv_samples_BookAlive_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> v;

    FastFeatureDetector detector(50);
    detector.detect(mGr, v);
    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }
}

JNIEXPORT void JNICALL Java_org_opencv_samples_BookAlive_Sample4Mixed_Draw(JNIEnv*, jobject, jlong orig1, jlong test1)
{
    Mat& orig  = *(Mat*)orig1;
    Mat& test = *(Mat*)test1;

    Mat H = findHomography(orig,test);
    vector< Point2f > n,n2;
    n.push_back(Point2f(10,10));
    n.push_back(Point2f(10,1000));
    perspectiveTransform(n, n2, H);
    line(test, n2[0], n2[1], Scalar(0,255,0), 10);
}

JNIEXPORT void JNICALL Java_org_opencv_samples_BookAlive_Sample4Mixed_mapPoints(JNIEnv*, jobject, jlong addrOrig, jlong addrTest, jlong addrPts)
{
    
    Mat& orig  = *(Mat*)addrOrig;
    Mat& test = *(Mat*)addrTest;
    Mat& pts =  *(Mat*)addrPts;



    Mat H = findHomography(test,orig);
       
    
    vector< Point2f > n,n2;
    for(size_t i=0; i<pts.rows; i++) {
	n.push_back(Point2f(pts.at<double>(i,0),pts.at<double>(i,1)));
    }

    perspectiveTransform(n, n2, H);
    //__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "The size of pts is %lf %lf", pts.at<double>(0,0), pts.at<double>(0,1));    
    for(size_t i=0; i<n2.size(); i++) {
        pts.at<double>(i,0) = n2[i].x;
        pts.at<double>(i,1) = n2[i].y;
    }
    
}
}
