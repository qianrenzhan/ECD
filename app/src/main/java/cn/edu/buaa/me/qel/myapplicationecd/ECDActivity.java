package cn.edu.buaa.me.qel.myapplicationecd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.view.menu.MenuBuilder;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ECDActivity extends Activity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String  TAG              = "ECD";

    private String Mode = "拍照模式";    // "测量模式"

    private CameraBridgeViewBase mOpenCvCameraView;

    private Mat mRgba;
    private Mat                  mPre_mat;
    private Scalar mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private int status;
    private TextView textView1;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ECDActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_ecd);
        //setHasOptionsMenu(true);
        mOpenCvCameraView = findViewById(R.id.ECD_activity_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        textView1 = (TextView)findViewById(R.id.textView);
        textView1.setText(Mode);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        super.onCreateOptionsMenu(menu);
        inflater.inflate(R.menu.ecd, menu);
        //setMenuBackground();

        int positionOfMenuItem = 0;
        MenuItem item1 = menu.getItem(positionOfMenuItem);
        SpannableString s1 = new SpannableString("文件");
        s1.setSpan(new ForegroundColorSpan(Color.RED), 0, s1.length(), 0);
        item1.setTitle(s1);

        positionOfMenuItem = 1;
        MenuItem item2 = menu.getItem(positionOfMenuItem);
        SpannableString s2 = new SpannableString("模式");
        s2.setSpan(new ForegroundColorSpan(Color.RED), 0, s2.length(), 0);
        item2.setTitle(s2);

        positionOfMenuItem = 2;
        MenuItem item3 = menu.getItem(positionOfMenuItem);
        SpannableString s3 = new SpannableString("报表生成");
        s3.setSpan(new ForegroundColorSpan(Color.RED), 0, s3.length(), 0);
        item3.setTitle(s3);

        positionOfMenuItem = 3;
        MenuItem item4 = menu.getItem(positionOfMenuItem);
        SpannableString s4 = new SpannableString("设置");
        s4.setSpan(new ForegroundColorSpan(Color.RED), 0, s4.length(), 0);
        item4.setTitle(s4);
        return true;
    }


//    //设置menu菜单的背景 ,年代久远，不管用
//    protected void setMenuBackground(){
//
//        Log.d(TAG, "Enterting setMenuBackGround");
//        getLayoutInflater().setFactory( new LayoutInflater.Factory() {
//            @Override
//            public View onCreateView ( String name, Context context, AttributeSet attrs ) {
//                if ( name.equalsIgnoreCase( "com.android.internal.view.menu.IconMenuItemView" ) ) {
//                    try { // Ask our inflater to create the view
//                        LayoutInflater f = getLayoutInflater();
//                        final View view = f.createView( name, null, attrs );
//                        new Handler().post( new Runnable() {
//                            public void run () {
//                                view.setBackgroundColor(new Color().GREEN);//设置背景色
//                            }
//                        } );
//                        return view;
//                    }
//                    catch ( InflateException e ) {}
//                    catch ( ClassNotFoundException e ) {}
//                }
//                return null;
//            }
//        });
//    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.file).setEnabled(true);
        menu.findItem(R.id.setting).setEnabled(true);
        menu.findItem(R.id.scan).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.file:
                Toast.makeText(this, "file", Toast.LENGTH_SHORT).show();
                return true;

                //切换模式，全局变量赋值
            case R.id.scan:
                item.setChecked(true);
                Mode = "拍照模式";
                textView1.setText(Mode);
                return true;
            case R.id.edit:
                item.setChecked(true);
                Mode = "测量模式";
                textView1.setText(Mode);
                return true;

//                mOnCameraFrameRender = new OnCameraFrameRender(new PreviewFrameRender());
//
//                new AsyncTask<Void, Void, Void>() {
//                    private ProgressDialog calibrationProgress;
//
//                    @Override
//                    protected void onPreExecute() {
////                        calibrationProgress = new ProgressDialog(MainActivity.this);
////                        calibrationProgress.setTitle(res.getString(R.string.calibrating));
////                        calibrationProgress.setMessage(res.getString(R.string.please_wait));
//                        calibrationProgress.setCancelable(false);
//                        calibrationProgress.setIndeterminate(true);
//                        calibrationProgress.show();
//                    }
//
//                    @Override
//                    protected Void doInBackground(Void... arg0) {
////                        mCalibrator.calibrate();
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Void result) {
//                        calibrationProgress.dismiss();
////                        mCalibrator.clearCorners();
////                        mOnCameraFrameRender = new OnCameraFrameRender(new CalibrationFrameRender(mCalibrator));
////                        String resultMessage = (mCalibrator.isCalibrated()) ?
////                                res.getString(R.string.calibration_successful) + " " + mCalibrator.getAvgReprojectionError() :
////                                res.getString(R.string.calibration_unsuccessful);
////                        (Toast.makeText(MainActivity.this, resultMessage, Toast.LENGTH_SHORT)).show();
////
////                        if (mCalibrator.isCalibrated()) {
////                            CalibrationResult.save(MainActivity.this,
////                                    mCalibrator.getCameraMatrix(), mCalibrator.getDistortionCoefficients());
////                        }
//                    }
//                }.execute();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        status = 0;
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        if(status == 0)         //0状态触摸转到1状态，开始处理，处理完转到2状态
            status = 1;
        else if(status == 2)    //2状态触摸转到0状态。
            status = 0;

        //关于状态间的切换，0是拍照状态，1是处理状态，2是等待状态。0到1,2到0需要手动切换，1到2自动切换（切换时间由处理时间决定）。
        if(status == 1)
        {
            int cols = mRgba.cols();
            int rows = mRgba.rows();
            int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
            int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
            int x = (int)event.getX() - xOffset;
            int y = (int)event.getY() - yOffset;
            //Toast.makeText(this, "Touch image coordinates: (" + x + ", " + y + ")", Toast.LENGTH_SHORT).show();
            if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
                return false;

            Rect touchedRect = new Rect();
            touchedRect.x = (x>4) ? x-4 : 0;
            touchedRect.y = (y>4) ? y-4 : 0;

            touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
            touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

            Mat touchedRegionRgba = mRgba.submat(touchedRect);

            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

            // Calculate average color of touched region
            mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width*touchedRect.height;
            for (int i = 0; i < mBlobColorHsv.val.length; i++)
                mBlobColorHsv.val[i] /= pointCount;

            mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

            Toast.makeText(this, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                    ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")", Toast.LENGTH_SHORT).show();

            touchedRegionRgba.release();
            touchedRegionHsv.release();
        }

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(status == 0)
        {
            //直接返回
            mPre_mat = inputFrame.rgba();
            return mPre_mat;
        }
        else if(status == 1)
        {
            //=============================================================================================================
            //进行处理并返回
            String str = "The minimum radius is: 12 pixels";
            mRgba = inputFrame.rgba();
            //Imgproc.putText(mRgba,str, new Point(100,100),1,5,new Scalar(0,255,0),2);
            //保存图片
            saveImg(mRgba);

//            if (mIsColorSelected) {
//                //mDetector,输入图片，在类的内部实现两个轮廓的提取，半径和直径的像素计算，折弯比计算。
//                mDetector.process(mRgba);
//
//                //从mDetector获取处理后的内容，叠加画到原图上，具体包括：两段轮廓，最小半径处，折弯比字符串显示。
//
//                List<MatOfPoint> contours = mDetector.getContours();
//                Log.e(TAG, "Contours count: " + contours.size());
//                Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);
//
//                Mat colorLabel = mRgba.submat(4, 68, 4, 68);
//                colorLabel.setTo(mBlobColorRgba);
//
//                Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
//                mSpectrum.copyTo(spectrumLabel);
//            }

            //============================================================================================================
            //切换到等待状态
            status = 2;
            mPre_mat = mRgba;
            return mRgba;
        }
        else if(status == 2)
        {
            return mPre_mat;
        }
        else
        {
            return inputFrame.rgba();
        }
    }

    private void saveImg(Mat rgba) {
        //先把mat转成bitmap
        Bitmap mBitmap = null;
        //Imgproc.cvtColor(seedsImage, rgba, Imgproc.COLOR_GRAY2RGBA, 4); //转换通道
        mBitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, mBitmap);

        FileOutputStream fileOutputStream = null;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳

        String fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + date + ".jpg";

        try {
            fileOutputStream = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            Log.d(TAG, "图片已保存至本地");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher_background);
        builder.setTitle("温馨提示");
        builder.setMessage("天冷多加衣！");
        builder.setPositiveButton("我知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }


}
