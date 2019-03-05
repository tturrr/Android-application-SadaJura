package com.example.user.sadajura;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    ImageView back_btn;
    ImageView profile_iv;
    ImageButton profile_iv_btn;
    EditText ph_num_et;
    Button sign_up_btn;
    EditText id_et;
    EditText password1_et;
    private RequestQueue queue;
    EditText address1_et;
    EditText address2_et;
    EditText password2_et;
    Button confirm_btn;
    //이미지 업로드 관련
    Bitmap bitmap;
    private final int IMG_REQUEST = 777;
    Button upload_btn;
    //사진촬영위한
    FrameLayout previewFrame;
    //테드퍼미션 리스너
    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
//            Toast.makeText(SignUpActivity.this,"권한 허가",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(SignUpActivity.this,"권한 거부\n"+deniedPermissions.toString(),Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        address1_et = (EditText)findViewById(R.id.address1_et);
        address2_et = (EditText)findViewById(R.id.address2_et);
        back_btn = (ImageView)findViewById(R.id.back_btn);
        ph_num_et = (EditText)findViewById(R.id.ph_num_et);
        sign_up_btn = (Button)findViewById(R.id.sign_up_btn);
        id_et = (EditText)findViewById(R.id.id_et);
        password1_et = (EditText)findViewById(R.id.password1_et);
        password2_et = (EditText)findViewById(R.id.password2_et);
        confirm_btn = (Button) findViewById(R.id.confirm_btn);
        final String token = FirebaseInstanceId.getInstance().getToken();



        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라를 실행하려면 권한이 필요합니다.")
                .setDeniedMessage("왜거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요")
                .setPermissions(Manifest.permission.CAMERA)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();


        //볼리 queue 생성
        queue = Volley.newRequestQueue(this);
        //다음주소검색에서 주소를 가지고 온다.
        Intent intent = getIntent();
        final String address1 = intent.getStringExtra("arg1");

//        if(photoString == null){
//            //기본 프로필이미지 입니다. 이미지를 등록하지않으면 기본이미지를 등록한다.
//            Glide.with(this)
//                    .load(R.drawable.profile_user)
//                    .apply(new RequestOptions().centerCrop().override(300,300))
//                    .into(profile_iv);
//
//        }else {
//            Glide.with(SignUpActivity.this)
//                    .load(Uri.parse(photoString))
//                    .apply(new RequestOptions().centerCrop().override(300,300))
//                    .into(profile_iv);
//        }
        address1_et.setText(address1);
        //1.아이디 중복체크확인. id_et 의 내용을 서버로 전송하여 서버에서 기존에 있는 아이디인지 체크를한다.
        //2. 체크 후 없는아디면 success boolean형식의 데이터가 온다
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://13.125.107.155/SadaJura/ConfirmId.php";
                final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            jsonResponse.toString();
                            boolean success = jsonResponse.getBoolean("success");
                            if(success){
                                Toast.makeText(SignUpActivity.this,"사용할 수 있는 아이디 입니다.",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(SignUpActivity.this,"사용할 수 없는 아이디 입니다.",Toast.LENGTH_SHORT).show();
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
                        params.put("id", id_et.getText().toString());
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });


        //쉐어드 프리퍼런스로 주소검색창을 눌러도 텍스트가 지워지지않게 임시로 저장한다.
        SharedPreferences pref = getSharedPreferences("pref", 0);
        String Temporaryid  = pref.getString("id", "");
        String Temporarypassword1  = pref.getString("password1", "");
        String Temporarypassword2  = pref.getString("password2", "");
        String Temporaryph_num  = pref.getString("ph_num", "");

        id_et.setText(Temporaryid);
        password1_et.setText(Temporarypassword1);
        password2_et.setText(Temporarypassword2);
        ph_num_et.setText(Temporaryph_num);

        //volley 를 통한 php 서버와의 통신.
        //url 주소에 post 방식으로 값을 전달한다 .키와 벨류로 보낸다.
        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "http://13.125.107.155/SadaJura/SignUp.php";
                        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                                public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {

                                Map<String, String> params = new HashMap<>();
                                params.put("id", id_et.getText().toString());
                                params.put("password", password1_et.getText().toString());
                                params.put("ph_num",ph_num_et.getText().toString());
                                params.put("address1",address1);
                                params.put("address2",address2_et.getText().toString());
                                params.put("token",token);
                                return params;
                            }
                        };
                        queue.add(stringRequest);
                SharedPreferences pref = getSharedPreferences("pref", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();
                FirebaseMessaging.getInstance().subscribeToTopic("news");
                MyFirebaseInstanceIDService myFirebaseInstanceIDService = new MyFirebaseInstanceIDService(SignUpActivity.this);


                Intent intent = new Intent(SignUpActivity.this, emailverify.class);
                intent.putExtra("id",id_et.getText().toString());
                startActivity(intent);
                finish();
                    }
                });

        //전화번호의 하이픈(-)을 넣어준다. 그러나 에뮬레이터는 한글이 지원되지않아서 에뮬로할려면 한글키보드를 다운받아 사용해야하며
        //현물 스마트폰으로는 적용이 된다.
        ph_num_et.addTextChangedListener(new PhoneNumberFormattingTextWatcher());


        //기본주소를 클릭시 DaumAddressActivity 로 이동하여 웹뷰가 실행되고 다음api주소검색이 나오며
        //검색결과를 setResult 로 가져와 기본주소와 상세주소에 기입한다.
        address1_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("pref",0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("id", id_et.getText().toString());
                editor.putString("password1", password1_et.getText().toString());
                editor.putString("password2", password2_et.getText().toString());
                editor.putString("ph_num", ph_num_et.getText().toString());
                editor.commit();

                Intent intent = new Intent(SignUpActivity.this,DaumAddressActivity.class);
                startActivity(intent);

            }
        });

        //뒤로 가기 버튼 구현.
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("pref", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();

                Intent BackMoveIntent = new Intent(SignUpActivity.this,LoginActivity.class);
                startActivity(BackMoveIntent);
                finish();
            }
        });
    }
    //resiultCode ==10 : 다음 주소검색 (DaumAddressActivity) 에서 주소데이터를 가지고 온다.
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == DaumReCode) {
//            if (resultCode == 10) {
//                String address1 = data.getStringExtra("arg1");
//                String address2 = data.getStringExtra("arg2");
//                address1_et.setText(address1);
//                address2_et.setText(address2);
//                queue = Volley.newRequestQueue(this);
//                sign_up_btn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String url = "http://13.125.107.155/SadaJura/SignUp.php";
//                        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//
//                            }
//                        }, new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//
//                            }
//                        }) {
//                            @Override
//                            protected Map<String, String> getParams() throws AuthFailureError {
//                                Map<String, String> params = new HashMap<String, String>();
//                                params.put("id", id_et.getText().toString());
//                                params.put("password", password1_et.getText().toString());
//                                params.put("ph_num",ph_num_et.getText().toString());
//                                params.put("address1",address1_et.getText().toString());
//                                params.put("address2",address2_et.getText().toString());
//                                return params;
//                            }
//                        };
//                        queue.add(stringRequest);
//                    }
//                });
//
//
//            } else {   // RESULT_CANCEL
//                Toast.makeText(SignUpActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                Log.d("address1","22");
//            }
////        } else if (requestCode == REQUEST_ANOTHER) {
////            ...
//        }
//    }
//


}
