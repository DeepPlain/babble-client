package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class RoomIn extends Activity {

    String no;
    String title;
    String category;
    String nick;
    String msg;
    EditText edit1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.room_in);

        Intent in = getIntent();
        no = in.getStringExtra("no");
        title = in.getStringExtra("title");
        category = in.getStringExtra("category");

        Button inBtn = (Button) findViewById(R.id.inBtn);
        inBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit1 = (EditText) findViewById(R.id.edit1);
                nick = edit1.getText().toString();
                Log.d(ACTIVITY_SERVICE, nick);
                if(nick.equals("")) {
                    Log.d(ACTIVITY_SERVICE, "toast");
                    Toast.makeText(RoomIn.this, "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        MainActivity.out.writeUTF(no + "%^" + title + "%^" + category + "%^" + "in" + "%^" + "10" + "%^" + nick);

                        new HttpTask().execute();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(ACTIVITY_SERVICE, "inbTn");
            }
        });

        Button outBtn = (Button) findViewById(R.id.outBtn);
        outBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ACTIVITY_SERVICE, "out");
                finish();
            }
        });

    }
    class HttpTask extends AsyncTask<Void, Void, String> {
        boolean threadAlive = true;
        DataOutputStream output = null;
        DataInputStream input = null;

        @Override
        protected String doInBackground(Void... voids) {
            // TODO Auto-generated method stub
            try {
                msg = MainActivity.in.readUTF();
                //결과창뿌려주기 - ui 변경시 에러
                return msg;

            } catch (Exception e) {
                e.printStackTrace();
            }
            //오류시 null 반환
            return null;
        }
        //asyonTask 3번째 인자와 일치 매개변수값 -> doInBackground 리턴값이 전달됨
        //AsynoTask 는 preExcute - doInBackground - postExecute 순으로 자동으로 실행됩니다.
        //ui는 여기서 변경
        protected void onPostExecute(String value){
            if(value.equals("ok")) {
                Intent myIntent = new Intent(RoomIn.this, ChatActivity.class);
                myIntent.putExtra("nick", nick);
                startActivity(myIntent);
                finish();
            }
            else if(value.equals("인원이 초과되었습니다.")) {
                Toast.makeText(RoomIn.this, value, Toast.LENGTH_SHORT).show();
                Intent myIntent = new Intent(RoomIn.this, MainActivity.class);
                startActivity(myIntent);
                finish();
            }
            else {
                Toast.makeText(RoomIn.this, value, Toast.LENGTH_SHORT).show();
                edit1.setText("");
            }

        }

    }
}