package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.*;

public class IntroActivity extends Activity {

    Handler h;
    static boolean send_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        h= new Handler(); //딜레이를 주기 위해 핸들러 생성
        h.postDelayed(mrun, 3000);

    }
    Runnable mrun = new Runnable(){
        @Override
        public void run(){
            Intent i = new Intent(IntroActivity.this, MainActivity.class); //인텐트 생성(현 액티비티, 새로 실행할 액티비티)
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            //overridePendingTransition 이란 함수를 이용하여 fade in,out 효과를줌. 순서가 중요
        }
    };

    //인트로 중에 뒤로가기를 누를 경우 핸들러를 끊어버려 아무일 없게 만드는 부분
    //미 설정시 인트로 중 뒤로가기를 누르면 인트로 후에 홈화면이 나옴.
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        h.removeCallbacks(mrun);
    }

}