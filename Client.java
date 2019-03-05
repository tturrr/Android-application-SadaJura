package com.example.user.sadajura;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.sadajura.Apprtc.CallActivity;
import com.example.user.sadajura.Apprtc.ConnectActivity;
import com.example.user.sadajura.Apprtc.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Client extends AppCompatActivity {
    ImageView chat_image_send_iv,chat_apprtc_btn;
    // 서버 접속 여부를 판별하기 위한 변수
    boolean isConnect = false;
    EditText edit1;
    Button btn1;
    LinearLayout container;
    ScrollView scroll;
    ProgressDialog pro;
    // 어플 종료시 스레드 중지를 위해...
    boolean isRunning = false;
    // 서버와 연결되어있는 소켓 객체
    Socket member_socket;
    // 사용자 닉네임( 내 닉넴과 일치하면 내가보낸 말풍선으로 설정 아니면 반대설정)
    String user_nickname;
    // 인텐트로오는 로그인한 사용자 닉네임
    String mem_id, GoogleId, ProductId;
    //로그인한 사용자닉네임 담을 변수
    String nickName,chat_nickName;
    //룸의 번호
    int ProductNo;
    //php서버에서 채팅내용을 한번 가지고 왔을때 값을 담기위한 변수.
    String send_id,recive_id,confirm_id, send_chat_content, send_chat_photo_path;
    //채팅이미지의 이름을 난수로 지정하기위한 랜덤함수.
    Random random = new Random();
    //갤러리에서의 비트맵값을 담기위한 변수.
    private Bitmap bitmap;

    int i;

    //webrtc
    private static final String TAG = "ConnectActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static final int REMOVE_FAVORITE_INDEX = 0;
    private static boolean commandLineRun = false;

    private SharedPreferences sharedPref;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        chat_image_send_iv = (ImageView) findViewById(R.id.chat_image_send_iv);
        edit1 = findViewById(R.id.editText);
        btn1 = findViewById(R.id.button);
        container = findViewById(R.id.container);
        scroll = findViewById(R.id.scroll);
        chat_apprtc_btn = findViewById(R.id.chat_apprtc_btn);
        Intent intent1 = getIntent();
        mem_id = intent1.getStringExtra("mem_id");
        GoogleId = intent1.getStringExtra("GoogleId");
        recive_id =intent1.getStringExtra("recive_id");
        ProductId = intent1.getStringExtra("Product_id");
        chat_nickName = intent1.getStringExtra("nickName");
        ProductNo = intent1.getIntExtra("ProductNo", 0);
        loadChat();



        chat_apprtc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Client.this, ConnectActivity.class);

                SendToServerThreadWetrec thread = new SendToServerThreadWetrec(member_socket, "페이스톡 해요",ProductNo);
                thread.start();
                chatSend("페이스톡 해요");
                lastMessage("페이스톡 해요",confirm_id);
                notisend("페이스톡 해요");
                connectToRoom(String.valueOf(ProductNo),false,false,false,0);
            }
        });

        if (ProductId == null && recive_id != null ) {
            confirm_id = recive_id;
        } else {
            confirm_id = ProductId;
        }

        chat_image_send_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(Client.this);
                dialog.setTitle("알림")
                        .setMessage("사진을 가져올 곳을 선택해 주세요.")
                        .setPositiveButton("카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Client.this, "카메라 버튼을 누르셨습니다.", Toast.LENGTH_SHORT).show();

                                boolean camera = ContextCompat.checkSelfPermission
                                        (view.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;


                                boolean write = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                                if (camera && write) {
                                    //사진찍는 인텐트 코드 넣기.

                                    takePicture();

                                } else {
                                    Toast.makeText(Client.this, "권한이 없습니다 권한을 받아주세요", Toast.LENGTH_LONG).show();

                                }


                            }
                        })
                        .setNegativeButton("갤러리", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Client.this, "갤러리 버튼을 누르셨습니다", Toast.LENGTH_SHORT).show();
                                chooseFile();
                            }
                        }).create().show();
            }
        });

        // 채팅창에 들어온 후 연결된 메소드

        if (isConnect == false) {   //접속전
            //사용자가 입력한 닉네임을 받는다.
            if (mem_id == null && GoogleId != null && chat_nickName ==null) {
                nickName = GoogleId;
            } else if(mem_id != null && GoogleId == null && chat_nickName ==null){
                nickName = mem_id;
            }else if(mem_id == null && GoogleId == null && chat_nickName == null){
                nickName = recive_id;
            }else{
                nickName = chat_nickName;
            }

            if (nickName.length() > 0 && nickName != null) {
                //자바서버에 접속한다.
                pro = ProgressDialog.show(this, null, "접속중입니다");
                // 접속 스레드 가동
                ConnectionThread thread = new ConnectionThread();
                thread.start();
            }
            // 닉네임이 입력되지않을경우 다이얼로그창 띄운다.
        }
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 접속 후
                // 입력한 문자열을 가져온다.
                String msg = edit1.getText().toString();
                // 송신 스레드 가동
                SendToServerThread thread = new SendToServerThread(member_socket, msg);
                thread.start();
                //php서버에 mysql에 보낸 내용을 저장한다.
                chatSend(msg);
                lastMessage(msg,confirm_id);
                notisend(msg);
            }
        });


        //webrtc
        // Get setting keys.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);



        // If an implicit VIEW intent is launching the app, go directly to that URL.
        final Intent intent = getIntent();
        if ("android.intent.action.VIEW".equals(intent.getAction()) && !commandLineRun) {
            boolean loopback = intent.getBooleanExtra(CallActivity.EXTRA_LOOPBACK, false);
            int runTimeMs = intent.getIntExtra(CallActivity.EXTRA_RUNTIME, 0);
            boolean useValuesFromIntent =
                    intent.getBooleanExtra(CallActivity.EXTRA_USE_VALUES_FROM_INTENT, false);
            String room = sharedPref.getString(keyprefRoom, "");
            connectToRoom(room, true, loopback, useValuesFromIntent, runTimeMs);
        }

    }

    // 자바서버접속 처리하는 스레드 클래스 - 안드로이드에서 네트워크 관련 동작은 항상
    // 메인스레드가 아닌 스레드에서 처리해야 한다.
    class ConnectionThread extends Thread {
        @Override
        public void run() {
            try {
                // 접속한다.
                final Socket socket = new Socket("13.125.107.155", 8888);
                member_socket = socket;
                // 미리 입력했던 닉네임을 서버로 전달한다.
                user_nickname = nickName;     // 화자에 따라 말풍선을 바꿔주기위해
                // 스트림을 추출
                OutputStream os = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                // 닉네임을 송신한다.
                dos.writeUTF(nickName + "@" + confirm_id + "@" + ProductNo);
                // ProgressDialog 를 제거한다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pro.dismiss();
                        edit1.setText("");
                        edit1.setHint("메세지 입력");
                        btn1.setText("전송");
                        // 접속 상태를 true로 셋팅한다.
                        isConnect = true;
                        // 메세지 수신을 위한 스레드 가동
                        isRunning = true;
                        MessageThread thread = new MessageThread(socket);
                        thread.start();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MessageThread extends Thread {
        Socket socket;
        DataInputStream dis;

        public MessageThread(Socket socket) {
            try {
                this.socket = socket;
                InputStream is = socket.getInputStream();
                dis = new DataInputStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (isRunning) {
                    // 자바서버로부터 데이터를 수신받는다.
                    final String msg = dis.readUTF();
                    final String[] filter = msg.split(";");
                    // 화면에 출력
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(filter[1].equals("image")){
                                try {
                                    sleep(1000);
                                    ImageView iv = new ImageView(Client.this);
                                    TextView tv = new TextView(Client.this);
                                    tv.setTextColor(Color.BLACK);
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                                    iv.setMaxWidth(500);
                                    iv.setMaxHeight(400);
                                    if(msg.startsWith(user_nickname)){
                                        tv.setGravity(Gravity.RIGHT);
                                        iv.setForegroundGravity(Gravity.RIGHT);
                                    }else{
                                        tv.setGravity(Gravity.LEFT);
                                        iv.setForegroundGravity(Gravity.LEFT);
                                    }
                                    tv.setText(filter[0]+": ");
                                    Glide.with(Client.this)
                                            .load(filter[2])
                                            .apply(new RequestOptions().override(500, 400))
                                            .into(iv);
                                    container.addView(iv);

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }else if(filter[1].equals("페이스톡 해요")){
                                try {
                                    ImageView iv = new ImageView(Client.this);
                                    iv.setMaxWidth(300);
                                    iv.setMaxHeight(300);
                                    // 텍스트뷰의 객체를 생성
                                    TextView tv = new TextView(Client.this);
                                    tv.setTextColor(Color.BLACK);
                                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                                    if(msg.startsWith(user_nickname)){

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                                        params.weight = 1.0f;
                                        params.gravity = Gravity.RIGHT;

                                        iv.setLayoutParams(params);

                                    }else{
                                        tv.setGravity(Gravity.LEFT);
                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                                        params.weight = 1.0f;
                                        params.gravity = Gravity.LEFT;
                                        tv.setText(filter[0]+": ");
                                         iv.setLayoutParams(params);
                                    }
                                    iv.setImageDrawable(getResources().getDrawable(R.drawable.facecall_icon));
                                    container.addView(tv);
                                    container.addView(iv);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
                                // 텍스트뷰의 객체를 생성
                                TextView tv = new TextView(Client.this);

                                tv.setTextColor(Color.BLACK);
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                                // 메세지의 시작 이름이 내 닉네임과 일치한다면
                                if (msg.startsWith(user_nickname)) {
                                    tv.setGravity(Gravity.RIGHT);
                                    tv.setText(filter[1]);
                                } else {
                                    tv.setGravity(Gravity.LEFT);
                                    tv.setText(filter[0]+": "+filter[1]);
                                }

                                container.addView(tv);
                            }
                            // 제일 하단으로 스크롤 한다.
                            scroll.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    scroll.fullScroll(View.FOCUS_DOWN);
                                }
                            }, 100);

                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


    // 자바서버에 데이터를 전달하는 스레드
    class SendToServerThread extends Thread {
        Socket socket;
        String msg;
        DataOutputStream dos;

        public SendToServerThread(Socket socket, String msg) {
            try {
                this.socket = socket;
                this.msg = msg;
                OutputStream os = socket.getOutputStream();
                dos = new DataOutputStream(os);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // 자바서버로 데이터를 보낸다.
                dos.writeUTF(msg + "@" + nickName + "@" + confirm_id + "@" + ProductNo);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edit1.setText("");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class SendToServerThreadWetrec extends Thread {
        Socket socket;
        String msg;
        DataOutputStream dos;
        int webrtc_room_no;

        public SendToServerThreadWetrec(Socket socket, String msg ,int webrtc_room_no) {
            try {
                this.webrtc_room_no = webrtc_room_no;
                this.socket = socket;
                this.msg = msg;
                OutputStream os = socket.getOutputStream();
                dos = new DataOutputStream(os);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // 자바서버로 데이터를 보낸다.
                dos.writeUTF(msg + "@" + nickName + "@" + confirm_id + "@" + ProductNo + "@" + webrtc_room_no);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edit1.setText("");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //마지막 채팅내용 저장.
    public void lastMessage(final String msg ,final String confirm_id){
        String chat_img_send = "http://13.125.107.155/SadaJura/LastMessageSave.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, chat_img_send, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");
                    if (success) {

                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("send_id", nickName);
                params.put("send_chat_content", msg);
                params.put("recive_id",confirm_id);
                params.put("room_no", String.valueOf(ProductNo));

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //채팅내용을 저장하기위해 서버로 채팅내용을 보낸다.
    public void chatSend(final String msg)

    {
        String chat_img_send = " http://13.125.107.155/SadaJura/ChatSave.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, chat_img_send, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");
                    if (success) {

                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("send_id", nickName);
                params.put("send_chat_content", msg);
                params.put("room_no", String.valueOf(ProductNo));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //채팅 내용을 처음 한번 불러와야 하기에 php 서버에서 채팅데이터를 가져온다.
    public void loadChat() {
        String url = "http://13.125.107.155/SadaJura/ChatLoad.php";
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // JSONObject 의 키 "response" 의 값들을 JSONArray 형태로 변환
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = new JSONArray(jsonResponse.getString("success"));

                    for (int i = 0; i < jsonArray.length(); i++) {
                        // Array 에서 하나의 JSONObject 를 추출
                        JSONObject dataJsonObject = jsonArray.getJSONObject(i);
                        // 추출한 Object 에서 필요한 데이터를 표시할 방법을 정해서 화면에 표시
                        // 필자는 RecyclerView 로 데이터를 표시 함
                        send_id = dataJsonObject.getString("send_id");
                        send_chat_content = dataJsonObject.getString("send_chat_content");
                        send_chat_photo_path = dataJsonObject.getString("send_chat_photo_path");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 텍스트뷰의 객체를 생성
                                TextView tv = new TextView(Client.this);
                                ImageView iv = new ImageView(Client.this);
                                tv.setTextColor(Color.BLACK);
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

                                    // 메세지의 시작 이름이 내 닉네임과 일치한다면
                                if (send_id.equals(user_nickname) && send_chat_content.equals("페이스톡 해요")) {
                                    tv.setGravity(Gravity.RIGHT);
                                    iv.setForegroundGravity(Gravity.RIGHT);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                                    params.weight = 1.0f;
                                    params.gravity = Gravity.RIGHT;

                                    iv.setLayoutParams(params);
                                    iv.setImageDrawable(getResources().getDrawable(R.drawable.facecall_icon));
                                }else if(send_id.equals(user_nickname)){
                                    tv.setGravity(Gravity.RIGHT);
                                    iv.setForegroundGravity(Gravity.RIGHT);
                                    Glide.with(Client.this)
                                            .load(send_chat_photo_path)
                                            .apply(new RequestOptions().override(500, 400))
                                            .into(iv);
                                }else if(send_id.equals(user_nickname) == false && send_chat_content.equals("페이스톡 해요")){
                                    tv.setGravity(Gravity.LEFT);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                                    params.weight = 1.0f;
                                    params.gravity = Gravity.LEFT;
                                    tv.setText(send_id+": ");
                                    iv.setLayoutParams(params);
                                    iv.setImageDrawable(getResources().getDrawable(R.drawable.facecall_icon));
                                    iv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            connectToRoom(String.valueOf(ProductNo),false,false,false,0);
                                        }
                                    });
                                }
                                else {
                                    tv.setGravity(Gravity.LEFT);
                                    iv.setForegroundGravity(Gravity.LEFT);
                                    tv.setText(send_id + ": " + send_chat_content);
                                    Glide.with(Client.this)
                                            .load(send_chat_photo_path)
                                            .apply(new RequestOptions().override(500, 400))
                                            .into(iv);
                                }


                                container.addView(tv);
                                container.addView(iv);
                                // 제일 하단으로 스크롤 한다
                                scroll.fullScroll(View.FOCUS_DOWN);
                            }
                        });

                    }


// 데이터를 스트링의 담는다 json데이터를 id라는 키값으로 value를 sd의 담는다..
// String sd = jsonResponse.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("room_no", String.valueOf(ProductNo));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //사진을 고르는 인텐트.
    public void takePicture() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 10);
    }

    //아이디가 방에서 마지막으로 채팅한 내용을 저장한다.
    private void LastMessageandimg(final String id, final String photo, final String msg ,final int x ,final String confirm_id) {
        String URL_UPLOAD = "http://13.125.107.155/SadaJura/LastMessageSave.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        Toast.makeText(Client.this, "사진을 보냈습니다.", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(Client.this, "사진을 보냈습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Client.this, "인터넷 연결을 확인 해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("send_id", id);
                params.put("room_no", String.valueOf(ProductNo));
                params.put("send_chat_content", msg);
                params.put("send_chat_photo_path", String.valueOf(x));
                params.put("recive_id",confirm_id);
                params.put("photo", photo);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    //이미지를 서버로 전송하며 동시에 채팅창에 이미지를 나타나게 한다.
    private void UploadPicture(final String id, final String photo, final String msg ,final int x) {
        String URL_UPLOAD = "http://13.125.107.155/SadaJura/ChatSave.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        Toast.makeText(Client.this, "사진을 보냈습니다.", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(Client.this, "사진을 보냈습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Client.this, "인터넷 연결을 확인 해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("send_id", id);
                params.put("room_no", String.valueOf(ProductNo));
                params.put("send_chat_content", msg);
                params.put("send_chat_photo_path", String.valueOf(x));
                params.put("photo", photo);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
        return encodedImage;
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    public void notisend(final String msg){
        String URL_NOTI = "http://13.125.107.155/SadaJura/push_notification.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_NOTI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("send_content", msg);
                String sdf = msg;
                params.put("send_id",nickName);
                params.put("room_no", String.valueOf(ProductNo));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 텍스트뷰의 객체를 생성

                        TextView tv = new TextView(Client.this);

                        tv.setTextColor(Color.BLACK);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

                        // 메세지의 시작 이름이 내 닉네임과 일치한다면
                        if (send_id.equals(user_nickname)) {
                            tv.setGravity(Gravity.RIGHT);
                        } else {
                            tv.setGravity(Gravity.LEFT);
                        }
                        tv.setText(mem_id);
                        container.addView(tv);
                        // 제일 하단으로 스크롤 한다
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            int x = random.nextInt(100000);
            //이미지를 tcp 통신한것처럼 보이게하기위한 php url 주소
            String imgUrl = "http://13.125.107.155/SadaJura/uploads/"+x+".jpeg";
            String imgUrlPath = "image;"+imgUrl;
            UploadPicture(mem_id, getStringImage(bitmap), "",x);
            LastMessageandimg(nickName,getStringImage(bitmap),"",x ,confirm_id);
            SendToServerThread thread = new SendToServerThread(member_socket, imgUrlPath);
            thread.start();

        }
        if (requestCode == 10) {
            final Bitmap bitmap2 = (Bitmap) data.getExtras().get("data");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 텍스트뷰의 객체를 생성

                    TextView tv = new TextView(Client.this);

                    tv.setTextColor(Color.BLACK);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

                    // 메세지의 시작 이름이 내 닉네임과 일치한다면
                    if (send_id.equals(user_nickname)) {
                        tv.setGravity(Gravity.RIGHT);

                    } else {
                        tv.setGravity(Gravity.LEFT);

                    }


                    tv.setText(mem_id);
                    container.addView(tv);
                    // 제일 하단으로 스크롤 한다
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            });
            int x = random.nextInt(100000);
            //이미지를 tcp 통신한것처럼 보이게하기위한 php url 주소
            String imgUrl = "http://13.125.107.155/SadaJura/uploads/"+x+".jpeg";
            String imgUrlPath = "image;"+imgUrl;
            UploadPicture(nickName, getStringImage(bitmap2), "",x);
            LastMessageandimg(nickName,getStringImage(bitmap2),"",x , confirm_id);
            SendToServerThread thread = new SendToServerThread(member_socket, imgUrlPath);

            thread.start();
        }
        if (requestCode == CONNECTION_REQUEST && commandLineRun) {
            Log.d(TAG, "Return: " + resultCode);
            setResult(resultCode);
            commandLineRun = false;
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//    getMenuInflater().inflate(R.menu.connect_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items.
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
//    else if (item.getItemId() == R.id.action_loopback) {
//      connectToRoom(null, false, true, false, 0);
//      return true;
//    }
        else {
            return super.onOptionsItemSelected(item);
        }
    }



    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.valueOf(getString(defaultId));
        if (useFromIntent) {
            return getIntent().getBooleanExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }

    private void connectToRoom(String roomId, boolean commandLineRun, boolean loopback,
                               boolean useValuesFromIntent, int runTimeMs) {
        this.commandLineRun = commandLineRun;

        // roomId is random for loopback.
        if (loopback) {
            roomId = Integer.toString((new Random()).nextInt(100000000));
        }

        String roomUrl = sharedPref.getString(
                keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                CallActivity.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                CallActivity.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, CallActivity.EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                CallActivity.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                CallActivity.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                CallActivity.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                CallActivity.EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                CallActivity.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                CallActivity.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Enable level control.
        boolean enableLevelControl = sharedPrefGetBoolean(R.string.pref_enable_level_control_key,
                CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, R.string.pref_enable_level_control_key,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, CallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_BITRATE, 0);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_AUDIO_BITRATE, 0);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                CallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, CallActivity.EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                CallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
                useValuesFromIntent);
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, CallActivity.EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                CallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                CallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, CallActivity.EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, CallActivity.EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                CallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // Start AppRTCMobile activity.
        Log.d(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);
        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Intent intent = new Intent(this, CallActivity.class);
            intent.setData(uri);
            intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
            intent.putExtra(CallActivity.EXTRA_LOOPBACK, loopback);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(CallActivity.EXTRA_SCREENCAPTURE, useScreencapture);
            intent.putExtra(CallActivity.EXTRA_CAMERA2, useCamera2);
            intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(CallActivity.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
            intent.putExtra(CallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
            intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
            intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
            intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
            intent.putExtra(CallActivity.EXTRA_CMDLINE, commandLineRun);
            intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);

            intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

            if (dataChannelEnabled) {
                intent.putExtra(CallActivity.EXTRA_ORDERED, ordered);
                intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS, maxRetr);
                intent.putExtra(CallActivity.EXTRA_PROTOCOL, protocol);
                intent.putExtra(CallActivity.EXTRA_NEGOTIATED, negotiated);
                intent.putExtra(CallActivity.EXTRA_ID, id);
            }

            if (useValuesFromIntent) {
                if (getIntent().hasExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA)) {
                    String videoFileAsCamera =
                            getIntent().getStringExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA);
                    intent.putExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                    String saveRemoteVideoToFile =
                            getIntent().getStringExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                    int videoOutWidth =
                            getIntent().getIntExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                    int videoOutHeight =
                            getIntent().getIntExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
                }
            }

            startActivityForResult(intent, CONNECTION_REQUEST);
        }
    }

    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
        return false;
    }

    private final AdapterView.OnItemClickListener roomListClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String roomId = ((TextView) view).getText().toString();
                    connectToRoom(roomId, false, false, false, 0);

                }
            };
}

