package cn.edu.buaa.me.qel.myapplicationecd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.os.Bundle;


import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static cn.edu.buaa.me.qel.myapplicationecd.WireAlgorithm.findCircle1;
import static org.opencv.core.CvType.CV_8UC3;

public class ECDActivity extends Activity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String  TAG              = "ECD";

    private CameraBridgeViewBase mOpenCvCameraView;

    private Mat mRgba;
    private Mat mAfter_mat;
    private Mat                  mPre_mat;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private int status;    //拍照状态
    private int stage;      //测量阶段
    private int mode;       //拍照or测量模式
    private boolean ResultReady;
    int Thread_touch_x;
    int Thread_touch_y;

    List<CircleData> CDList = new ArrayList<>();

    List<MatOfPoint> contours_all = new ArrayList<>();    //原始轮廓
    int max_index = 0;                                     //原始最长轮廓编号
    List<MatOfPoint> contours = new ArrayList<>();        //分段后轮廓
    CircleData min_CD = new CircleData();    //最小圆结构体

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

        status = 0;
        stage = 0;
        mode = 0;
        ResultReady = false;
        setModeTextView(mode);
    }

    private void setModeTextView(int mode)
    {
        if(mode == 0){
            textView1 = (TextView)findViewById(R.id.textView);
            textView1.setText("拍照模式");
        }else{
            textView1 = (TextView)findViewById(R.id.textView);
            textView1.setText("测量模式");
        }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //文件
            case R.id.open_picture:
                Toast.makeText(this, "打开图片", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.save_picture:
                if(ResultReady == true){
                    //开始保存结果
                    Toast.makeText(this, "开始保存结果", Toast.LENGTH_SHORT).show();
                    ResultReady = false;
                }else
                    Toast.makeText(this, "测量未完成", Toast.LENGTH_SHORT).show();
                return true;

                //切换模式，全局变量赋值
            case R.id.scan:
                item.setChecked(true);
                mode = 0;
                setModeTextView(mode);
                status = 0;   //默认运动模式
                return true;
            case R.id.edit:
                if(mode == 0 && status == 1) {
                    item.setChecked(true);
                    mode = 1;
                    setModeTextView(mode);
                    stage = 0;   //默认初始阶段
                }else
                    Toast.makeText(this, "需要暂停拍照", Toast.LENGTH_SHORT).show();
                return true;

                //报表界面切换
            case R.id.report_gene:
                Intent intent = new Intent();
                intent.setClass(ECDActivity.this, ReportActivity.class);
                startActivity(intent);
                return true;
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
        Thread_touch_x = (int)event.getX();
        Thread_touch_y = (int)event.getY();
        if(mode == 0) {  //如果是拍照模式
            //在运动和暂停间切换
            status = 1 - status;
        }

        if(mode == 1) {    //如果是拍照模式，在原图和处理后之间切换
            //stage = 1 - stage;
            if(stage == 0) {    //选取控制点，进行计算
                ResultReady = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int cols = mRgba.cols();
                            int rows = mRgba.rows();
                            int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
                            int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
                            int x = Thread_touch_x - xOffset;
                            int y = Thread_touch_y - yOffset;

                            if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
                                return;

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

//                            Toast.makeText(ECDActivity.this, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
//                                    ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")", Toast.LENGTH_SHORT).show();
                            touchedRegionRgba.release();
                            touchedRegionHsv.release();

                            //这里写所有的算法,对mAfter_mat进行处理
                            function1(mBlobColorHsv);

                            stage = 1;     //0 和 1阶段返回的内容不同
                            //0阶段返回mRgba  ，   1阶段返回处理后图片 mAfter_mat
                            ResultReady = true;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                //====================================================================================
            }
            else if(stage == 1){   //测量完成再次点击，说明对结果不满意，可以重新测量
                ResultReady = false;
                stage = 0;
            }
        }
        return false; // don't need subsequent touch events
    }

    private void function1(Scalar mBlobColorHsv)
    {
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= 1;

        mAfter_mat = mRgba.clone();
        String str = "The minimum radius is: 12 pixels";
        Imgproc.putText(mAfter_mat,str,new Point(0,200),1,5,new Scalar(0,255,0),2);
        //====================================================================================
        Imgproc.cvtColor(mAfter_mat, mAfter_mat, Imgproc.COLOR_BGRA2GRAY, 4);
        Imgproc.GaussianBlur(mAfter_mat, mAfter_mat, new Size(3,3), 0);
        Imgproc.threshold(mAfter_mat, mAfter_mat,70,255,0);

        Mat hierarchy = new Mat();
        Imgproc.findContours(mAfter_mat,contours_all ,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_NONE, new Point(0,0));
        hierarchy.release();

        //轮廓截断，选择
        double top = mAfter_mat.size().height / 8;
        double left = mAfter_mat.size().width / 8;
        double height = 3 * mAfter_mat.size().height / 4;
        double width = 3 * mAfter_mat.size().width / 4;

        //选择最长的轮廓
        MatOfPoint contour = new MatOfPoint();

        double maxsize = 0;
        for (int i = 0; i < contours_all.size(); i++)
        {
            if(contours_all.get(i).size().height > maxsize)
            {
                maxsize = contours_all.get(i).size().height;
                max_index = i;
                contour = contours_all.get(i);
            }
        }

        //轮廓分段
        int wire_size = 200;       //半径小于10拟合不出圆
        MatOfPoint temp_contour = new MatOfPoint();
        contours.clear();
        for (int i = 0; i < contour.size().height; i += wire_size)
        {
            temp_contour.release();
            temp_contour = new MatOfPoint();

            for (int j = 0; j < wire_size; j++)
            {
                if (i + j < contour.size().height)
                {
                    Mat temp_point = new Mat();
                    Mat.eye(1, 1, CvType.CV_32SC2).copyTo(temp_point);
                    double[] xx = contour.get(i+j,0);
                    temp_point.put(0,0,xx);
                    temp_contour.push_back(temp_point);
                }
                else
                {
                    break;
                }
            }
            MatOfPoint aa = new MatOfPoint(temp_contour);
            contours.add(aa);
        }
        contour.release();

        //每段找拟合圆，前面没有问题
        Mat matDst = Mat.zeros(mAfter_mat.size(),CV_8UC3);
        double minvalue = 100000;
        CDList.clear();
        for (int i = 0; i < contours.size()-1; i++)
        {
            double[] a1 = contours.get(i).get(0,0);
            double[] a2 = contours.get(i).get(wire_size/2-1,0);
            double[] a3 = contours.get(i).get(wire_size-1,0);
            Point pt1 = new Point(a1[0],a1[1]);
            Point pt2 = new Point(a2[0],a2[1]);
            Point pt3 = new Point(a3[0],a3[1]);

            CircleData CD;
            CD = findCircle1(pt1, pt2, pt3);
            CDList.add(CD);
            double rec = 20000;

            if (CD.getRadius() > 0 && CD.getCenter().x < rec && CD.getCenter().x > -rec && CD.getCenter().y < rec && CD.getCenter().y > -rec
                    && pt1.x < left+width && pt1.x > left && pt1.y < top + height && pt1.y > top
                    && pt2.x < left + width && pt2.x > left && pt2.y < top + height && pt2.y > top
                    && pt3.x < left + width && pt3.x > left && pt3.y < top + height && pt3.y > top) {
                Random random = new Random((int) System.currentTimeMillis());
                Imgproc.circle(matDst,CD.getCenter(),(int)CD.getRadius(),
                        new Scalar(random.nextInt() % 255, random.nextInt() % 255, random.nextInt() % 255),8);
                if(CD.getRadius()<minvalue) {
                    minvalue = CD.getRadius();
                    min_CD = CD;
                }
            }
        }
        Imgproc.drawContours(matDst, contours_all, max_index, new Scalar(255, 0, 0), 10);
        Imgproc.circle(matDst, min_CD.getCenter(), (int) min_CD.getRadius(), new Scalar(255, 255, 0), 10);
        String str2 = "The minimum radius is:" + ((int) min_CD.getRadius()) + "pixels";
        Imgproc.putText(matDst, str2, new Point(0, 200), 1, 5, new Scalar(0, 255, 0), 2);

        mAfter_mat = matDst.clone();
        matDst.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(mode == 0 && status == 0)    //直接返回
        {
            mPre_mat = inputFrame.rgba();
            mRgba = mPre_mat;
            return mPre_mat;
        }
        else if(mode == 0 && status == 1)  //返回暂停前图片
        {
            //进行处理并返回
            String str = "The minimum radius is: 12 pixels";
            //mRgba = inputFrame.rgba();
            //mPre_mat = mRgba;
            return mPre_mat;
        } else {   //测量模式
            if(stage == 0)
                return mRgba;
            else
                return mAfter_mat;
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
