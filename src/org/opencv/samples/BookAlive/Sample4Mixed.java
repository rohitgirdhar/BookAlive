package org.opencv.samples.BookAlive;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

public class Sample4Mixed extends Activity implements CvCameraViewListener {
    private static final String    TAG = "OCVSample::Activity";
    
    private static final int       CAPTURE_IMAGE	     = 1;
    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;


    private int                    mViewMode;
    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGrayMat;

    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;
    ImageView imageView;

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("mixed_sample");

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Sample4Mixed() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial4_surface_view);
       

        if (!OpenCVLoader.initDebug()) {
        	// Handle initialization error
        	Log.e("Here", "Here error");
        }
        imageView = (ImageView) findViewById(R.id.imageView1);
        //mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial4_activity_surface_view);
        //mOpenCvCameraView.setCvCameraViewListener(this);
        

        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent, CAPTURE_IMAGE);
        
        // TEMP FOR NOW
        String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
        File f1 = new File(SD_CARD_PATH + "/" + "test3.jpg");
        Mat test = Highgui.imread(f1.getAbsolutePath());
        process(test);
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == CAPTURE_IMAGE) {
    		if(resultCode == RESULT_OK) {
    			Mat test = new Mat();
    			Bitmap photo = (Bitmap) data.getExtras().get("data");
    			Utils.bitmapToMat(photo, test);
    			process(test);
    		}
    	}
    }
    
    protected void saveImg(Mat img, String fname) {
    	String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
    	String uri = SD_CARD_PATH + "/" + fname;
    	Highgui.imwrite(uri, img);
    }
    
    
    protected Mat imageRetrieve(Mat test) {
    	// TODO
    	String SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
        File f1 = new File(SD_CARD_PATH + "/" + "act.jpg");
        Mat orig = Highgui.imread(f1.getAbsolutePath());
        return orig;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewGray = menu.add("Preview GRAY");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewFeatures = menu.add("Find features");
        return true;
    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        
        
    }
    
    

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGrayMat = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGrayMat.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(Mat inputFrame) {
        final int viewMode = mViewMode;

        switch (viewMode) {
        case VIEW_MODE_GRAY:
            // input frame has gray scale format
            Imgproc.cvtColor(inputFrame, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            break;
        case VIEW_MODE_RGBA:
            // input frame has RBGA format
            inputFrame.copyTo(mRgba);
            break;
        case VIEW_MODE_CANNY:
            // input frame has gray scale format
            Imgproc.Canny(inputFrame, mIntermediateMat, 80, 100);
            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
            break;
        case VIEW_MODE_FEATURES:
            // input frame has RGBA format
            inputFrame.copyTo(mRgba);
            Imgproc.cvtColor(mRgba, mGrayMat, Imgproc.COLOR_RGBA2GRAY);
            FindFeatures(mGrayMat.getNativeObjAddr(), mRgba.getNativeObjAddr());
            break;
        }

        return mRgba;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewRGBA) {
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            mViewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewGray) {
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_GREY_FRAME);
            mViewMode = VIEW_MODE_GRAY;
        } else if (item == mItemPreviewCanny) {
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_GREY_FRAME);
            mViewMode = VIEW_MODE_CANNY;
        } else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        }

        return true;
    }
    
    public void process(Mat test) {
    	Mat orig = imageRetrieve(test);
		saveImg(test, "res.jpg");
		Imgproc.cvtColor(orig, orig, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.cvtColor(test, test, Imgproc.COLOR_RGBA2GRAY);
        double[] arr = new double[1];
        arr[0] = 10;
        Mat p = new Mat(2, 2, CvType.CV_64FC1);	
        p.put(0, 0, arr);
        arr[0] = 100;
        p.put(0, 1, arr);
        arr[0] = 10;
        p.put(0, 0, arr);
        arr[0] = 1000;
        p.put(0, 1, arr);
        
        mapPoints(orig.getNativeObjAddr(), test.getNativeObjAddr(), p.getNativeObjAddr());
        String res = Double.toString(p.get(0,0)[0]) + " " + Double.toString(p.get(0,1)[0]) + " ";
        res += Double.toString(p.get(1,0)[0]) + " " + Double.toString(p.get(1,1)[0]);
        Log.v("TAG", res);
        Core.line(test, new Point(p.get(0,0)[0], p.get(0,1)[0]), new Point(p.get(1,0)[0], p.get(1,1)[0]), new Scalar(0,255,0), 10);
        Bitmap bmp = Bitmap.createBitmap(test.cols(), test.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(test, bmp);
        imageView.setImageBitmap(bmp);
        
    }

    
	public native void FindFeatures(long matAddrGr, long matAddrRgba);
    public native void Draw(long matOrig, long matTest);
    public native void mapPoints(long addrOrig, long addrTest, long addrP);
    static {
    	 System.loadLibrary("opencv_java"); //load opencv_java lib	
         System.loadLibrary("mixed_sample");
    }
}
