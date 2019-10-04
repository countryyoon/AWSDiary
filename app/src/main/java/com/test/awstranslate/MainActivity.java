package com.test.awstranslate;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    DatePicker datePicker;  //  datePicker - 날짜를 선택하는 달력

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 앱 첫 시작 시 돌아가는 메소드
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("오늘 일기, 감정 미디어");

        // 뷰에 있는 위젯들 리턴 받아두기
        datePicker = (DatePicker) findViewById(R.id.datePicker);


        // datePick 기능 만들기
        // datePicker.init(연도,달,일)
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 이미 선택한 날짜에 일기가 있는지 없는지 체크해야할 시간이다
                int[] pickDate = new int[] {0, 0, 0};
                pickDate[0] = year;
                pickDate[1] = monthOfYear;
                pickDate[2] = dayOfMonth;
                System.out.println(year + "   " + monthOfYear + "   " + dayOfMonth);
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("pickDate", pickDate);
                startActivity(intent);
                finish();
            }
        });
    }
}