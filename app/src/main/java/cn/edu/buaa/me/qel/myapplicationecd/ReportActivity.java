package cn.edu.buaa.me.qel.myapplicationecd;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class ReportActivity extends AppCompatActivity {

    TextView textView_start_time,textView_end_time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        textView_start_time = findViewById(R.id.start_time);
        textView_end_time = findViewById(R.id.end_time);
    }

    public void on_start_time()
    {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        int year = calendar.get(Calendar.YEAR);
//        int monthOfyear = calendar.get(Calendar.MONTH);
//        int dayOfmonth = calendar.get(Calendar.DAY_OF_MONTH);
//        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                textView_start_time.setText(dayOfMonth);
//            }
//        },year,monthOfyear,dayOfmonth);
//        dpd.show();
    }

    public void on_end_time()
    {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        int year = calendar.get(Calendar.YEAR);
//        int monthOfyear = calendar.get(Calendar.MONTH);
//        int dayOfmonth = calendar.get(Calendar.DAY_OF_MONTH);
//        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                textView_end_time.setText(dayOfMonth);
//            }
//        },year,monthOfyear,dayOfmonth);
//        dpd.show();
    }
}
