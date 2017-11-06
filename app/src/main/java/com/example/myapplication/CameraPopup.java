package com.example.myapplication;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CameraPopup extends Activity {

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private ImageView imgview;
    private Button camera;
    private Button gallery;

    String nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.camera_popup);

        Intent intent = getIntent();
        nick = intent.getExtras().getString("nick");

        camera = (Button) findViewById(R.id.camera);
        gallery = (Button) findViewById(R.id.gallery);

        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // 카메라 호출
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());

                // 이미지 잘라내기 위한 크기
                /*intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 0);
                intent.putExtra("aspectY", 0);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 150);
*/
                try {
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, PICK_FROM_CAMERA);

                } catch (ActivityNotFoundException e) {
                    // Do nothing for now
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent();
                // Gallery 호출
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // 잘라내기 셋팅
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 0);
                intent.putExtra("aspectY", 0);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 150);
                try {
                    intent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(intent,
                            "Complete action using"), PICK_FROM_GALLERY);

                } catch (ActivityNotFoundException e) {
                    // Do nothing for now
                }
            }
        });
    }

    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        System.out.println(IntroActivity.send_image + "asdasdas dasdqwdzxczc");

        if (requestCode == PICK_FROM_CAMERA) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                byte[] photo_byte= bitmapToByteArray(photo);
                Intent intent = new Intent();
                intent.putExtra("photo_byte", photo_byte);
                setResult(1, intent);
            }
        }
        if (requestCode == PICK_FROM_GALLERY) {
            Bundle extras2 = data.getExtras();
            if (extras2 != null) {
                Bitmap photo = extras2.getParcelable("data");
                byte[] photo_byte = bitmapToByteArray(photo);
                Intent intent = new Intent();
                intent.putExtra("photo_byte", photo_byte);
                setResult(1, intent);
            }
        }
        finish();
    }
    public byte[] bitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    class SendThread extends Thread {
        private Socket socket;
        DataOutputStream output;
        byte[] data;

        public SendThread(Socket socket, byte[] photo) {
            this.socket = socket;
            output = MainActivity.out;
            data = photo;
        }

        public void run() {
            try {
                Log.d(ACTIVITY_SERVICE, "11111");

                if (output != null) {
                    output.writeUTF(nick +"%^image%^output");
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
                    finish();
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