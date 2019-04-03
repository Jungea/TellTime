/*
 * 작성자: 정은애
 * 작성일: 2019.04.03.
 * 스톱워치
 */

package com.example.telltime;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    TextView timeOutput;  //시간 출력 뷰
    TextView record;  //기록 출력 뷰
    Button startBtn;  //시작버튼
    Button recBtn;  //기록버튼

    //상태
    final static int Init = 0;  //초기
    final static int Run = 1;  //실행
    final static int Pause = 2;  //중지

    int current = Init;
    int count = 1;  //기록 갯수
    long baseTime;  //처음시각
    long pauseTime;  //중지버튼을 눌렀을 때 시각
    long pre = 0;  //전 시간기록

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //뷰 저장
        timeOutput = (TextView) findViewById(R.id.timeOutput);
        record = (TextView) findViewById(R.id.record);
        record.setMovementMethod(new ScrollingMovementMethod());
        startBtn = (Button) findViewById(R.id.startBtn);
        recBtn = (Button) findViewById(R.id.resBtn);
    }

    public void myOnClick(View view) {
        switch (view.getId()) {
            case R.id.startBtn:  //첫번째버튼 클릭시(시작, 중지)
                switch (current) {
                    case Init:  //00:00:00에서 시작버튼을 누른경우
                        baseTime = SystemClock.elapsedRealtime();
                        timer.sendEmptyMessage(0); // Handler 실행(id=0);
                        startBtn.setText("중지");  //시작버튼이 중지버튼으로 변경
                        recBtn.setEnabled(true);  //기록버튼 활성화
                        current = Run; //[현재상태 : 실행]
                        break;

                    case Run:  //실행중에 중지버튼을 누른경우
                        timer.removeMessages(0);  //Handler 실행 중지
                        pauseTime = SystemClock.elapsedRealtime();
                        startBtn.setText("계속");
                        recBtn.setText("초기화");
                        current = Pause; //[현재상태 : 중지]
                        break;

                    case Pause:   //중지상태일 때 계속버튼을 누른경우
                        long now = SystemClock.elapsedRealtime();
                        timer.sendEmptyMessage(0);
                        baseTime += (now - pauseTime);
                        startBtn.setText("중지");
                        recBtn.setText("기록");
                        current = Run;  //[현재상태 : 실행]
                        break;
                }
                break;

            case R.id.resBtn:  //두번째버튼 클릭시
                switch (current) {
                    case Run:  //기록버튼 클릭
                        long plus = getTime() - pre;  //전 기록과 시간차이

                        String s = record.getText().toString();
                        s = String.format("%d. %s    + %s\n", count++, formatTime(getTime()), formatTime(plus)) + s;
                        record.setText(s);

                        pre = getTime();
                        break;

                    case Pause:  //초기화 버튼 클릭
                        timer.removeMessages(0);  //Handler 실행 중지

                        timeOutput.setText("00:00:00");
                        record.setText("");

                        startBtn.setText("시작");
                        recBtn.setText("기록");
                        recBtn.setEnabled(false);

                        current = Init;
                        count = 1;
                        break;
                }
                break;
        }
    }

    Handler timer = new Handler() {  //시간갱신을 반복
        @Override
        public void handleMessage(Message msg) {
            timeOutput.setText(formatTime(getTime()));

            //Handler 반복 실행
            timer.sendEmptyMessage(0);
        }
    };

    //현재시간을 계속 구하여 리턴
    public long getTime() {
        long now = SystemClock.elapsedRealtime(); //갱신할 때 현재 시각
        long outTime = now - baseTime;

        return outTime;
    }

    //해당 형태로 시간을 변경
    public String formatTime(long outTime) {
        String easy_outTime = String.format("%02d:%02d:%02d", outTime / 1000 / 60, (outTime / 1000) % 60, (outTime % 1000) / 10);
        return easy_outTime;
    }
}
