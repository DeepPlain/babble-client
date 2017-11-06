package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

public class RoomMake extends Activity {

    String title;
    static String nick;
    String total;
    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.room_make);


        Spinner categorySpin = (Spinner) findViewById(R.id.category);
        final ArrayAdapter categoryAdapter = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        categorySpin.setAdapter(categoryAdapter);
        categorySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = categoryAdapter.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = "스포츠";
            }
        });

        Spinner totalSpin = (Spinner) findViewById(R.id.total);
        final ArrayAdapter totalAdapter = ArrayAdapter.createFromResource(this, R.array.total, android.R.layout.simple_spinner_item);
        totalAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        totalSpin.setAdapter(totalAdapter);
        totalSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                total = totalAdapter.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                total = "2";
            }
        });

        Button inBtn = (Button) findViewById(R.id.inBtn);
        inBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit1 = (EditText) findViewById(R.id.edit1);
                title = edit1.getText().toString();
                EditText edit2 = (EditText) findViewById(R.id.edit2);
                nick = edit2.getText().toString();

                if (title.equals("") && nick.equals(""))
                    Toast.makeText(RoomMake.this, "방 제목, 닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                else if (title.equals(""))
                    Toast.makeText(RoomMake.this, "방 제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                else if (nick.equals(""))
                    Toast.makeText(RoomMake.this, "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                else {
                    try {
                        MainActivity.out.writeUTF("0" + "%^" + title + "%^" + category + "%^" + "make" + "%^" + total + "%^" + nick);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent myIntent = new Intent(RoomMake.this, ChatActivity.class);
                    myIntent.putExtra("nick", nick);
                    startActivity(myIntent);
                    finish();

                    Log.d(ACTIVITY_SERVICE, "inbTn");
                }
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
}
