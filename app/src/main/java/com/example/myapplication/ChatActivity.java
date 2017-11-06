package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends Activity {
    TextView roomInfo;
    Button roomExit;
    //Button button_camera;
    Button button_send;
    EditText editText_message;
    Handler msghandler;
    DiscussArrayAdapter adapter;
    ListView lv;
    Boolean checkConst = false;

    SocketClient client;
    ReceiveThread receive;
    SendThread send;
    Socket socket;

    LinkedList<SocketClient> threadList;

    String nick;

    ImageButton button_camera;

    private boolean mPressFirstBackKey = false;
    private Timer timer;
    private String temp_roomInfo;

    ArrayList<String> temp_chat;

    String temp_nick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d(ACTIVITY_SERVICE, "onStart");

        /*if(savedInstanceState != null) {
            cnt = savedInstanceState.getInt("num");
            Log.d(ACTIVITY_SERVICE, String.valueOf(cnt));
            //Toast.makeText(ChatActivity.this, cnt, Toast.LENGTH_SHORT).show();
        }*/
        button_camera = (ImageButton) findViewById(R.id.button_camera);

        Intent in = getIntent();
        nick = in.getStringExtra("nick");
        lv = (ListView) findViewById(R.id.listView1);
        Log.d(ACTIVITY_SERVICE, "make listview");
        adapter = new DiscussArrayAdapter(getApplicationContext(), R.layout.list_chat_me);
        lv.setAdapter(adapter);
        Log.d(ACTIVITY_SERVICE, "make adapter");
        System.out.println(adapter);


        roomExit = (Button) findViewById(R.id.roomExit);
        roomInfo = (TextView) findViewById(R.id.roomInfo);
        editText_message = (EditText) findViewById(R.id.editText_message);
        //button_camera = (Button) findViewById(R.id.button_camera);
        button_send = (Button) findViewById(R.id.button_send);

        threadList = new LinkedList<SocketClient>();

        client = new SocketClient();
        threadList.add(client);
        client.start();

        temp_chat = new ArrayList<String>();

        msghandler = new Handler() {
            public void handleMessage(Message hdmsg) {
                SimpleDateFormat date = new SimpleDateFormat("HH:mm", Locale.KOREA);

                if (hdmsg.what == 1202) { // 나 이미지
                    byte[] data = (byte[]) hdmsg.obj;
                    adapter.add(new OneComment(false, false, data, temp_nick, "[" + date.format(new Date()) + "]"));
                    String ByteToString = Base64.encodeToString(data, Base64.DEFAULT);
                    String temp_string = "image%^" + temp_nick + "%^" +ByteToString;
                    temp_chat.add(temp_string);
                    lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

                }
                else if(hdmsg.what == 1203) { // 상대방 이미지
                    byte[] data = (byte[]) hdmsg.obj;
                    adapter.add(new OneComment(false, true, data, temp_nick, "[" + date.format(new Date()) + "]"));
                    String ByteToString = Base64.encodeToString(data, Base64.DEFAULT);
                    String temp_string = "image%^" + temp_nick + "%^" +ByteToString;
                    temp_chat.add(temp_string);
                    lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

                }
                else if (hdmsg.what == 1204) { // 나 이미지 복구
                    byte[] data = (byte[]) hdmsg.obj;
                    adapter.add(new OneComment(false, false, data, temp_nick, "[" + date.format(new Date()) + "]"));                    lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                    lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

                }
                else if(hdmsg.what == 1205) { // 상대방 이미지 복구
                    byte[] data = (byte[]) hdmsg.obj;
                    adapter.add(new OneComment(false, true, data, temp_nick, "[" + date.format(new Date()) + "]"));
                    lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

                }
                else {
                    Log.d(ACTIVITY_SERVICE, "handler!!");
                    StringTokenizer str = new StringTokenizer(hdmsg.obj.toString(), "%^");
                    StringTokenizer inOut = new StringTokenizer(hdmsg.obj.toString(), "$&");

                    StringTokenizer cutNickToken = new StringTokenizer(hdmsg.obj.toString(), " : "); // 닉네임만 짜르기
                    String cutNick = cutNickToken.nextToken(); // 닉네임
                    int cutNickLength = cutNick.length(); // 닉네임 길이

                if (hdmsg.what == 1101) { // receive 스레드 종료
                    Log.d(ACTIVITY_SERVICE, "hdmsg 1101 " + hdmsg.obj.toString());
                    send = new SendThread(socket, hdmsg.obj.toString());
                    send.start();
                }

                String text = null;
                if (cutNickToken.countTokens() != 0)
                    text = hdmsg.obj.toString().substring(cutNickLength + 3, hdmsg.obj.toString().length()); // 내용 짜르기

                if (hdmsg.what == 1111) { // 채팅 (나가기, 접속, 나, 상대방 채팅)
                    temp_chat.add(hdmsg.obj.toString());
                    Log.d(ACTIVITY_SERVICE, "handlerOut : " + hdmsg.obj.toString());
                    if (inOut.countTokens() == 3) { // 나가기, 접속
                        adapter.add(new OneComment(true, false, inOut.nextToken(), "", ""));
                    } else if (nick.equals(cutNick)) { // 나 채팅
                        adapter.add(new OneComment(false, false, text, cutNick, "[" + date.format(new Date()) + "]"));
                        System.out.println(adapter + "handler");
                        Log.d(ACTIVITY_SERVICE, "handlerIn : " + hdmsg.obj.toString());
                    } else { // 상대방 채팅
                        adapter.add(new OneComment(false, true, text, cutNick, "[" + date.format(new Date()) + "]"));
                    }
                    lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

                } else if (hdmsg.what == 3333) { // 채팅 백업 복구 (나가기, 접속, 나, 상대방 채팅)
                    Log.d(ACTIVITY_SERVICE, "handlerOut : " + hdmsg.obj.toString());
                    if (inOut.countTokens() == 3) { // 나가기, 접속
                        adapter.add(new OneComment(true, false, inOut.nextToken(), "", ""));
                    } else if (nick.equals(cutNick)) { // 나 채팅
                        adapter.add(new OneComment(false, false, text, cutNick, "[" + date.format(new Date()) + "]"));
                        System.out.println(adapter + "handler");
                        Log.d(ACTIVITY_SERVICE, "handlerIn : " + hdmsg.obj.toString());
                    } else { // 상대방 채팅
                        adapter.add(new OneComment(false, true, text, cutNick, "[" + date.format(new Date()) + "]"));
                    }
                    lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                } else if (hdmsg.what == 2222) { // 방 정보 세팅
                    String roomTitle = str.nextToken();
                    String roomNumber = str.nextToken();
                    String roomTotalNumber = str.nextToken();
                    roomInfo.setText(roomTitle + " (" + roomNumber + " / " + roomTotalNumber + ")");
                }
            }
            }
        };

        roomExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.out.writeUTF(nick + "%^exit");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent myIntent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(myIntent);
                finish();
            }
        });

        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ChatActivity.this, CameraPopup.class);
                myIntent.putExtra("nick", nick);
                startActivityForResult(myIntent, 1);
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText_message.getText().toString().equals("")) {
                    send = new SendThread(socket);
                    send.start();
                    editText_message.setText("");
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(ACTIVITY_SERVICE, "Pause");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(ACTIVITY_SERVICE, "Stop");

        System.out.println("isAlive" + receive.isAlive());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(ACTIVITY_SERVICE, "Destroy");
        Message hdmsg = msghandler.obtainMessage();
        hdmsg.what = 1101;
        hdmsg.obj = nick + " : break : thread_break";
        msghandler.sendMessage(hdmsg);

        System.out.println("isAlive" + receive.isAlive());

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(ACTIVITY_SERVICE, "ReStart");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(ACTIVITY_SERVICE, "save");
        temp_roomInfo = roomInfo.getText().toString();
        outState.putString("info", temp_roomInfo);
        outState.putStringArrayList("temp_chat", temp_chat);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        temp_roomInfo = savedInstanceState.getString("info");
        Log.d(ACTIVITY_SERVICE, "restore");
        roomInfo.setText(String.valueOf(temp_roomInfo));
        temp_chat = savedInstanceState.getStringArrayList("temp_chat");
        Iterator<String> it = temp_chat.iterator();
        while(it.hasNext()) {
            String temp_String = it.next();
            StringTokenizer token = new StringTokenizer(temp_String, "%^");
            if(token.nextToken().toString().equals("image")) {
                Log.d(ACTIVITY_SERVICE, "image backup");
                temp_nick = token.nextToken().toString();
                String bytee = token.nextToken().toString();

                if(nick.equals(temp_nick)) {
                    Log.d(ACTIVITY_SERVICE, "image backup1204");

                    Message hdmsg = msghandler.obtainMessage();
                    hdmsg.what = 1204;
                    hdmsg.obj = Base64.decode(bytee,Base64.DEFAULT);
                    msghandler.sendMessage(hdmsg);
                }
                else {
                    Log.d(ACTIVITY_SERVICE, "image backup1205");
                    Message hdmsg = msghandler.obtainMessage();
                    hdmsg.what = 1205;
                    hdmsg.obj = Base64.decode(bytee, Base64.DEFAULT);
                    msghandler.sendMessage(hdmsg);
                }
            }
            else {
                Message hdmsg = msghandler.obtainMessage();
                hdmsg.what = 3333;
                hdmsg.obj = temp_String;
                msghandler.sendMessage(hdmsg);
            }
        }


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 1:
                ImageSendThread send = new ImageSendThread(socket, data.getByteArrayExtra("photo_byte"));
                send.start();
                Log.d(ACTIVITY_SERVICE, "image send!");
                break;

            default:
                break;
        }
    }
    public void onBackPressed() {
        if (mPressFirstBackKey == false) {
            Toast.makeText(ChatActivity.this, "\'뒤로\' 버튼을 한번 더 누르면 처음으로 돌아갑니다.", Toast.LENGTH_SHORT).show();
            mPressFirstBackKey = true;
            TimerTask second = new TimerTask() {
                @Override
                public void run() {
                    timer.cancel();
                    timer = null;
                    mPressFirstBackKey = false;
                }
            };
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new Timer();
            timer.schedule(second, 2000);
        } else {
            try {
                MainActivity.out.writeUTF(nick + "%^exit");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent myIntent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(myIntent);
            finish();
        }
    }

    public class DiscussArrayAdapter extends ArrayAdapter<OneComment> {

        private TextView countryName;
        private TextView nick;
        private TextView time;
        private TextView time2;
        private ImageView photo;
        private List<OneComment> countries = new ArrayList<OneComment>();
        private LinearLayout wrapper;
        private RelativeLayout wrapper2;

        @Override
        public void add(OneComment object) {
            countries.add(object);
            super.add(object);
        }

        public DiscussArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public int getCount() {
            return this.countries.size();
        }

        public OneComment getItem(int index) {
            return this.countries.get(index);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            //if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_chat_me, parent, false);
            //}

            wrapper = (LinearLayout) row.findViewById(R.id.wrapper);
            wrapper2 = (RelativeLayout) row.findViewById(R.id.wrapper2);

            OneComment coment = getItem(position);

            countryName = (TextView) row.findViewById(R.id.comment);
            nick = (TextView) row.findViewById(R.id.nick);
            time = (TextView) row.findViewById(R.id.time);
            time2 = (TextView) row.findViewById(R.id.time2);
            photo = (ImageView) row.findViewById(R.id.photo);

            countryName.setText(coment.comment);
            nick.setText(coment.nick);
            time.setText(coment.time);
            time2.setText(coment.time);

            if (coment.center == true) {
                wrapper.setGravity(Gravity.CENTER);
                wrapper2.setGravity(Gravity.CENTER);
                time.setVisibility(View.GONE);
                time2.setVisibility(View.GONE);
            } else if (coment.center == false) {
                if(coment.comment == null) {
                    wrapper.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);
                    wrapper2.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);
                    time.setVisibility(coment.left ? View.GONE : View.VISIBLE);
                    time2.setVisibility(coment.left ? View.VISIBLE : View.GONE);
                    countryName.setVisibility(View.GONE);
                    photo.setVisibility(View.VISIBLE);
                    Bitmap bitmap_data = BitmapFactory.decodeByteArray(coment.data, 0, coment.data.length);
                    photo.setImageBitmap(resizeBitmap(bitmap_data, 200));
                    if (!coment.left)
                        nick.setPadding(0, 0, 12, 0);

                }
                else {
                    countryName.setBackgroundResource(coment.left ? R.drawable.grey2 : R.drawable.blue3);
                    countryName.setTextColor(coment.left ? Color.parseColor("#000000") : Color.parseColor("#ffffff"));

                    wrapper.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);
                    wrapper2.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);
                    time.setVisibility(coment.left ? View.GONE : View.VISIBLE);
                    time2.setVisibility(coment.left ? View.VISIBLE : View.GONE);
                    if (!coment.left)
                        nick.setPadding(0, 0, 12, 0);
                }
            }

            return row;
        }

    }

    public class OneComment {
        public boolean center;
        public boolean left;
        public String comment;
        public String nick;
        public String time;
        public byte[] data;

        public OneComment(boolean center, boolean left, String comment, String nick, String time) {
            super();
            this.center = center;
            this.left = left;
            this.comment = comment;
            this.nick = nick;
            this.time = time;
        }
        public OneComment(boolean center, boolean left, byte[] data, String nick, String time) {
            super();
            this.center = center;
            this.left = left;
            this.data = data;
            this.nick = nick;
            this.time = time;
            comment = null;
        }

    }

    public Bitmap resizeBitmap(Bitmap bmpSource, int maxResolution){
        int iWidth = bmpSource.getWidth();      //비트맵이미지의 넓이
        int iHeight = bmpSource.getHeight();     //비트맵이미지의 높이
        int newWidth = iWidth ;
        int newHeight = iHeight ;
        float rate = 0.0f;

        //이미지의 가로 세로 비율에 맞게 조절
        if(iWidth > iHeight ){
            if(maxResolution < iWidth ){
                rate = maxResolution / (float) iWidth ;
                newHeight = (int) (iHeight * rate);
                newWidth = maxResolution;
            }
        }else{
            if(maxResolution < iHeight ){
                rate = maxResolution / (float) iHeight ;
                newWidth = (int) (iWidth * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(
                bmpSource, newWidth, newHeight, true);
    }

    class SocketClient extends Thread {
        boolean threadAlive;

        private DataOutputStream output = null;

        public SocketClient() {
            threadAlive = true;
        }

        @Override
        public void run() {

            socket = MainActivity.sock;
            output = MainActivity.out;
            receive = new ReceiveThread(socket);
            receive.start();
            Log.d(ACTIVITY_SERVICE, "SocketClient");

        }
    }

    class ReceiveThread extends Thread {
        private Socket socket = null;
        DataInputStream input;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
            input = MainActivity.in;
        }

        public void run() {
            try {
                Log.d(ACTIVITY_SERVICE, "receive");
                while (input != null) {
                    System.out.println(this + "       THREAD!");
                    Log.d(ACTIVITY_SERVICE, "receive while");
                    String msg = input.readUTF();
                    if (msg != null) {
                        Log.d(ACTIVITY_SERVICE, "receive msg != null : " + msg);
                        StringTokenizer str = new StringTokenizer(msg, "%^");

                        StringTokenizer check_break = new StringTokenizer(msg, " : ");
                        if(check_break.countTokens() > 3) { // receive스레드 종료
                            String msg1 = check_break.nextToken().toString();
                            String msg2 = check_break.nextToken().toString();
                            String msg3 = check_break.nextToken().toString();
                            Log.d(ACTIVITY_SERVICE, msg1 + "          " + msg2);
                            if(msg1.equals(msg2) && msg3.equals("break")) {
                                if(msg1.equals(nick)) {
                                    Log.d(ACTIVITY_SERVICE, "nick + _!@#!@#break");
                                    break;
                                }
                                else {
                                    Log.d(ACTIVITY_SERVICE, "nick + _!@#!@#continue");
                                    continue;
                                }
                            }
                        }
                        if (str.countTokens() == 4) { // image
                            temp_nick = str.nextToken();
                            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                            byte[] imagebuffer = null;
                            int size = 0;
                            byte[] buffer = new byte[10240];
                            int read;
                            int temp_max = 0;
                            str.nextToken();
                            int max = Integer.valueOf(str.nextToken());
                            while ((read = bis.read(buffer)) != -1 && max > temp_max) {
                                System.out.print("image 1");
                                if (imagebuffer == null) {
                                    System.out.print("image 2");
                                    //처음 4byte에서 비트맵이미지의 총크기를 추출해 따로 저장한다
                                    byte[] sizebuffer = new byte[4];
                                    System.arraycopy(buffer, 0, sizebuffer, 0, sizebuffer.length);
                                    size = getInt(sizebuffer);
                                    read -= sizebuffer.length;

                                    //나머지는 이미지버퍼 배열에 저장한다
                                    imagebuffer = new byte[read];
                                    System.arraycopy(buffer, sizebuffer.length, imagebuffer, 0, read);
                                } else {
                                    System.out.print("image 3");
                                    //이미지버퍼 배열에 계속 이어서 저장한다
                                    byte[] preimagebuffer = imagebuffer.clone();
                                    imagebuffer = new byte[read + preimagebuffer.length];
                                    System.arraycopy(preimagebuffer, 0, imagebuffer, 0, preimagebuffer.length);

                                    System.arraycopy(buffer, 0, imagebuffer, imagebuffer.length - read, read);

                                }
                                temp_max += read;
                                System.out.println("temp_max :  " + temp_max);
                                if (max <= temp_max) {
                                    break;
                                }

                            }
                            if (temp_nick.equals(nick)) {
                                Message hdmsg = msghandler.obtainMessage();
                                hdmsg.what = 1202; // 나
                                hdmsg.obj = imagebuffer;
                                msghandler.sendMessage(hdmsg);
                            }
                            else {
                                Message hdmsg = msghandler.obtainMessage();
                                hdmsg.what = 1203; // 상대방
                                hdmsg.obj = imagebuffer;
                                msghandler.sendMessage(hdmsg);
                            }
                        }

                        else if (str.countTokens() == 3) {
                            Message hdmsg = msghandler.obtainMessage();
                            hdmsg.what = 2222;
                            hdmsg.obj = msg;
                            msghandler.sendMessage(hdmsg);

                        } else {
                            Message hdmsg = msghandler.obtainMessage();
                            hdmsg.what = 1111;
                            hdmsg.obj = msg;
                            msghandler.sendMessage(hdmsg);
                            Log.d(ACTIVITY_SERVICE, "receive : " + hdmsg.obj.toString());

                        }
                        Log.d(ACTIVITY_SERVICE, "receive run");

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(ACTIVITY_SERVICE, "receive thread 끝");
        }
    }
    private int getInt(byte[] data) {
        int s1 = data[0] & 0xFF;
        int s2 = data[1] & 0xFF;
        int s3 = data[2] & 0xFF;
        int s4 = data[3] & 0xFF;

        return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
    }

    class SendThread extends Thread {
        private Socket socket;
        String sendmsg = editText_message.getText().toString();
        DataOutputStream output;

        public SendThread(Socket socket) {
            this.socket = socket;
            output = MainActivity.out;
        }

        public SendThread(Socket socket, String msg) {
            this.socket = socket;
            output = MainActivity.out;
            sendmsg = msg;
        }

        public void run() {
            try {
                Log.d(ACTIVITY_SERVICE, "11111");
                Log.d(ACTIVITY_SERVICE, sendmsg);

                if (output != null) {
                    if (sendmsg != null) {
                        output.writeUTF(nick + " : " + sendmsg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
    }

    class ImageSendThread extends Thread {
        private Socket socket;
        DataOutputStream output;
        byte[] data;

        public ImageSendThread(Socket socket, byte[] photo) {
            this.socket = socket;
            output = MainActivity.out;
            data = photo;
        }

        public void run() {
            try {
                Log.d(ACTIVITY_SERVICE, "image    11111");

                if (output != null) {
                    output.writeUTF(nick +"%^image%^" + data.length);
                    output.flush();
                    //if (sendmsg != null) {
                    //byte의 총 크기를 4바이트에 담아 보낸다
                    byte[] size = getByte(data.length);
                    output.write(size, 0, size.length);
                    output.flush();

                    //실제 데이터를 보낸다
                    output.write(data, 0, data.length);
                    output.flush();
                    System.out.println("data 크기 =========== " + data.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
        private byte[] getByte(int num) {
            byte[] buf = new byte[4];
            buf[0] = (byte)( (num >>> 24) & 0xFF );
            buf[1] = (byte)( (num >>> 16) & 0xFF );
            buf[2] = (byte)( (num >>>  8) & 0xFF );
            buf[3] = (byte)( (num >>>  0) & 0xFF );

            return buf;
        }
    }


}

