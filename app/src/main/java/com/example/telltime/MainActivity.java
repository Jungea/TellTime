/*
 * 작성자: 정은애
 */

package com.example.telltime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    TextView timeView;  //시간 출력 뷰
    TextView totalTimeView;  //전체 시간

    //상태
    int current = Init;
    final static int Init = 0;  //초기
    final static int Run = 1;  //실행
    final static int Pause = 2;  //중지

    long baseTime;  //처음시각
    long pauseTime;  //중지버튼을 눌렀을 때 시각
    long totalTime = 0;  //전체 사용시간

    final long ResetTime = 5000;  //초기화 시간

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //뷰 저장
        timeView = (TextView) findViewById(R.id.timeView);
        totalTimeView = (TextView) findViewById(R.id.totalTimeView);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(br, filter);
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() == Intent.ACTION_SCREEN_ON) {

                if (current == Pause) {
                    long continueTime = SystemClock.elapsedRealtime();  //다시 화면이 켜진 때
                    timer.sendEmptyMessage(0);
                    long diff = continueTime - pauseTime; //꺼진 시간

                    if (diff >= ResetTime) {  // 화면이 꺼진 시간이 초기화 시간이상일 때
                        timeView.setText("00:00:00");
                        totalTime += pauseTime - baseTime;  //전 화면에 켜있던 시간
                        current = Init;

                    } else {
                        baseTime += diff;
                        current = Run;  //[현재상태 : 실행]
                    }
                }

                if (current == Init) {
                    baseTime = SystemClock.elapsedRealtime();
                    timer.sendEmptyMessage(0); // Handler 실행(id=0);
                    current = Run; //[현재상태 : 실행]
                }

            } else {
                if (current == Run) { // 실행 중일 때 화면이 꺼진경우
                    timer.removeMessages(0);  //Handler 실행 중지
                    pauseTime = SystemClock.elapsedRealtime();
                    current = Pause; //[현재상태 : 중지]
                }
            }

        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    Handler timer = new Handler() {  //시간갱신을 반복
        @Override
        public void handleMessage(Message msg) {
            timeView.setText(timeFormat(getTime("base")));
            totalTimeView.setText(timeFormat(getTime("total")));

            //Handler 반복 실행
            timer.sendEmptyMessage(0);
        }
    };

    //현재시간을 계속 구하여 리턴
    public long getTime(String timeName) {
        long now = SystemClock.elapsedRealtime(); //갱신할 때 현재 시각
        long outTime = now - baseTime;
        if (timeName.equals("total"))
            outTime += totalTime;

        return outTime;
    }

    //해당 형태로 시간을 변경
    public String timeFormat(long outTime) {
        String format = String.format("%02d:%02d:%02d", outTime / (1000 * 60 * 60) % 24, (outTime / 1000 * 60) % 60, (outTime / 1000) % 60);
        return format;
    }
}