package com.example.user.sadajura;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class emailverify extends AppCompatActivity implements View.OnClickListener,Dialog.OnCancelListener{

    EditText authEmail;
    Button authBtn,skipBtn;
    String id;
    private RequestQueue queue;

    /*Dialog에 관련된 필드*/

    LayoutInflater dialog; //LayoutInflater
    View dialogLayout; //layout을 담을 View
    Dialog authDialog; //dialog 객체

    /*카운트 다운 타이머에 관련된 필드*/

    TextView time_counter; //시간을 보여주는 TextView
    EditText emailAuth_number; //인증 번호를 입력 하는 칸
    Button emailAuth_btn; // 인증버튼
    CountDownTimer countDownTimer;
    final int MILLISINFUTURE = 60 * 1000; //총 시간 (300초 = 5분)
    final int COUNT_DOWN_INTERVAL = 1000; //onTick 메소드를 호출할 간격 (1초)
    int user_answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailverify);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        skipBtn = (Button)findViewById(R.id.skipBtn);
        authEmail=(EditText)findViewById(R.id.authEmail);
        authBtn=(Button)findViewById(R.id.authBtn);
        authBtn.setOnClickListener(this);
        skipBtn.setOnClickListener(this);


    }

    public void countDownTimer() { //카운트 다운 메소드

        time_counter = (TextView) dialogLayout.findViewById(R.id.emailAuth_time_counter);
        //줄어드는 시간을 나타내는 TextView
        emailAuth_number = (EditText) dialogLayout.findViewById(R.id.emailAuth_number);
        //사용자 인증 번호 입력창
        emailAuth_btn = (Button) dialogLayout.findViewById(R.id.emailAuth_btn);
        //인증하기 버튼
        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) { //(300초에서 1초 마다 계속 줄어듬)

                long emailAuthCount = millisUntilFinished / 1000;
                Log.d("Alex", emailAuthCount + "");

                if ((emailAuthCount - ((emailAuthCount / 60) * 60)) >= 10) { //초가 10보다 크면 그냥 출력
                    time_counter.setText((emailAuthCount / 60) + " : " + (emailAuthCount - ((emailAuthCount / 60) * 600)));
                } else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
                    time_counter.setText((emailAuthCount / 60) + " : 0" + (emailAuthCount - ((emailAuthCount / 60) * 60)));
                }

                //emailAuthCount은 종료까지 남은 시간임. 1분 = 60초 되므로,
                // 분을 나타내기 위해서는 종료까지 남은 총 시간에 60을 나눠주면 그 몫이 분이 된다.
                // 분을 제외하고 남은 초를 나타내기 위해서는, (총 남은 시간 - (분*60) = 남은 초) 로 하면 된다.

            }


            @Override
            public void onFinish() { //시간이 다 되면 다이얼로그 종료
//                authDialog.cancel();
                authDialog.dismiss();
            }
        }.start();
        emailAuth_btn.setOnClickListener(this);

    }

    public void emailverify(){

        String url = "http://13.125.107.155/SadaJura/email_send.php";
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    jsonResponse.toString();
                    boolean success = jsonResponse.getBoolean("success");
                    if(success) {
                        Toast.makeText(emailverify.this, "인증메일을 보냈습니다.", Toast.LENGTH_SHORT).show();
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

                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("email",authEmail.getText().toString());

                return params;
            }
        };
        queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
//        Intent intent = new Intent();
//        intent.putExtra("email",authEmail.getText().toString());
//        intent.putExtra("id",id);
//        startActivity(intent);
//        finish();
    }

    public void emailsuccess(){
        String url = "http://13.125.107.155/SadaJura/email_success.php";
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    jsonResponse.toString();
                    boolean success = jsonResponse.getBoolean("success");
                    if(success) {
                        Toast.makeText(emailverify.this, "이메일이 인증되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(emailverify.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(emailverify.this, "인증번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
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

                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("verifynum", String.valueOf(user_answer = Integer.parseInt(emailAuth_number.getText().toString())));

                return params;
            }
        };
        queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }



    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.authBtn :
                emailverify();
                dialog = LayoutInflater.from(this);
                dialogLayout = dialog.inflate(R.layout.activity_emailcer, null); // LayoutInflater를 통해 XML에 정의된 Resource들을 View의 형태로 반환 시켜 줌
                authDialog = new Dialog(this); //Dialog 객체 생성
                authDialog.setContentView(dialogLayout); //Dialog에 inflate한 View를 탑재 하여줌
                authDialog.setCanceledOnTouchOutside(false); //Dialog 바깥 부분을 선택해도 닫히지 않게 설정함.
                authDialog.setOnCancelListener(this); //다이얼로그를 닫을 때 일어날 일을 정의하기 위해 onCancelListener 설정
                authDialog.show(); //Dialog를 나타내어 준다.
                countDownTimer();
                break;

            case R.id.skipBtn:
                Intent intent = new Intent(emailverify.this,LoginActivity.class);
                Toast.makeText(emailverify.this,"이메일인증을 건너 뛰셨습니다.",Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
                break;

            case R.id.emailAuth_btn : //다이얼로그 내의 인증번호 인증 버튼을 눌렀을 시
                emailsuccess();
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        countDownTimer.cancel();
    } //다이얼로그 닫을 때 카운트 다운 타이머의 cancel()메소드 호출


}
