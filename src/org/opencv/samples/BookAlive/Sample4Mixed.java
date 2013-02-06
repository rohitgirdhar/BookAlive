package org.opencv.samples.BookAlive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Sample4Mixed extends Activity implements CvCameraViewListener {
    private static final String    TAG = "OCVSample::Activity";
    
    private static final int       CAPTURE_IMAGE	     = 1;
    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;
    private String SD_CARD_PATH;

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
        SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
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
        try {
			process(test);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == CAPTURE_IMAGE) {
    		if(resultCode == RESULT_OK) {
    			Mat test = new Mat();
    			Bitmap photo = (Bitmap) data.getExtras().get("data");
    			Utils.bitmapToMat(photo, test);
    			try {
					process(test);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
    
    public void readFile(String name, List< List<Double> > pts, List< String > fnames) throws IOException {
    	InputStream    fis;
    	BufferedReader br;
    	String         line;

    	fis = new FileInputStream(name);
    	br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
    	while ((line = br.readLine()) != null) {
    	    String[] parts = line.split(" ");
    	    List<Double> temp = new ArrayList<Double>();
    	    temp.add(Double.parseDouble(parts[0])); temp.add(Double.parseDouble(parts[1]));
    	    pts.add(temp);
    	    fnames.add(parts[2]);
    	}
    }
    
    public void process(Mat test) throws IOException {
    	Mat orig = imageRetrieve(test);
    	Mat test2 = test.clone();
		saveImg(test, "res.jpg");
		Imgproc.cvtColor(orig, orig, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.cvtColor(test, test, Imgproc.COLOR_RGBA2GRAY);
        
        
        // the file name to read the points from
        String uri = SD_CARD_PATH + "/" + "pts.txt";
        
        List< List<Double> > pts = new ArrayList< List<Double> >();
        List<String> fnames  = new ArrayList<String>();
        readFile(uri, pts, fnames);
        Mat p = new Mat(fnames.size(), 2, CvType.CV_64FC1);
        for(int i=0; i<fnames.size(); i++) {
        	p.put(i,0,pts.get(i).get(0));
        	p.put(i,1,pts.get(i).get(1));
        }
        
        //get screen resolutions
        double screenY = imageView.getHeight();
        double screenX = imageView.getWidth();
        
        //get image resolutions
        double imgY = test2.height();
        double imgX = test2.width();
        
        // get the multiply ratio
        double ratX = screenX/imgX;
        double ratY = screenY/imgY;
        Log.v("TAG", Double.toString(ratX) + " " + Double.toString(ratY));
        
        mapPoints(orig.getNativeObjAddr(), test.getNativeObjAddr(), p.getNativeObjAddr());
        
        //Core.line(test, new Point(p.get(0,0)[0], p.get(0,1)[0]), new Point(p.get(1,0)[0], p.get(1,1)[0]), new Scalar(0,255,0), 10);
        Bitmap bmp = Bitmap.createBitmap(test2.cols(), test2.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(test2, bmp);
        imageView.setImageBitmap(bmp);
        	
        
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30, 40);
        
        
        for(int i=0; i<fnames.size(); i++) {
        	Button play = getVideoButton("play", (float)0.5, fnames.get(i));
        	RelativeLayout.LayoutParams buttonParams = 
            		new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
            										 ViewGroup.LayoutParams.WRAP_CONTENT);
        	buttonParams.leftMargin = (int) (p.get(i,1)[0]);
            buttonParams.topMargin = (int) (p.get(i,0)[0]);
            String res = Double.toString(buttonParams.leftMargin) + " " + Double.toString(buttonParams.topMargin);
            Log.v("TAG", res);
            rl.addView(play, buttonParams);
        }
        
    }

    private Button getVideoButton(String name, float alpha, String vid_fname) {
    	final String fname = vid_fname;
    	Button play = new Button(getApplicationContext());
        play.setText(name);
        play.setAlpha((float) alpha);
        play.setOnClickListener(new View.OnClickListener() {	
			
			@Override
			public void onClick(View v) {
				
		    	String uri = SD_CARD_PATH + "/" + fname;
		    	Uri u = Uri.parse(uri);
		    	Log.v("TAG", uri);
		    	Intent vid = new Intent(Intent.ACTION_VIEW);
		    	vid.setDataAndType(u, "video/*");
		    	startActivity(vid);
			}
		});
        return play;
    }
    
	public native void FindFeatures(long matAddrGr, long matAddrRgba);
    public native void Draw(long matOrig, long matTest);
    public native void mapPoints(long addrOrig, long addrTest, long addrP);
    static {
    	 System.loadLibrary("opencv_java"); //load opencv_java lib	
         System.loadLibrary("mixed_sample");
    }
}
