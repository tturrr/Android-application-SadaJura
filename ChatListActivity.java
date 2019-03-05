package com.example.user.sadajura;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView chatRcv;
    ChatRcvAdapter chatRcvAdapter;
    ArrayList<ChatDataForm> chatList;
    private RequestQueue queue;
    private String mem_id,GoogleId,nickName,mem_photo_path,GooglePhotoString;
    int length;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        final Intent intent = getIntent();
        mem_id = intent.getStringExtra("mem_id");
        GoogleId = intent.getStringExtra("GoogleId");
        GooglePhotoString = intent.getStringExtra("GooglePhotoString");
        mem_photo_path = intent.getStringExtra("mem_photo_path");

        back_btn = (ImageView)findViewById(R.id.back_btn);
        //리사이클러뷰 관련
        chatRcv = (RecyclerView) findViewById(R.id.chat_list_rcv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        chatRcv.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        chatRcv.setLayoutManager(linearLayoutManager);
        chatList = new ArrayList<>();
        chatRcvAdapter = new ChatRcvAdapter(this, chatList);
        chatRcv.setAdapter(chatRcvAdapter);
        chatRcvAdapter.notifyDataSetChanged();


            //사용자가 입력한 닉네임을 받는다.
            if (mem_id == null && GoogleId != null) {
                nickName = GoogleId;
            } else {
                nickName = mem_id;
            }

        //버튼클릭시 뒤로 이동.
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(ChatListActivity.this,MainActivity.class);
                backIntent.putExtra("mem_id",mem_id);
                backIntent.putExtra("GoogleId",GoogleId);
                startActivity(backIntent);
                finish();
            }
        });




            String url = "http://13.125.107.155/SadaJura/LastMessageLoad.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        // JSONObject 의 키 "response" 의 값들을 JSONArray 형태로 변환
                        JSONArray jsonArray = new JSONArray(jsonResponse.getString("success"));

                        for (int i = 0; i < jsonArray.length(); i++) {
                            // Array 에서 하나의 JSONObject 를 추출
                            JSONObject dataJsonObject = jsonArray.getJSONObject(i);
                            // 추출한 Object 에서 필요한 데이터를 표시할 방법을 정해서 화면에 표시
                            // 필자는 RecyclerView 로 데이터를 표시 함

                            if(dataJsonObject.getString("send_chat_content").equals("")){
                                chatList.add(new ChatDataForm(dataJsonObject.getString("send_chat_photo_path"),dataJsonObject.getString("send_id"),dataJsonObject.getString("date_time"), dataJsonObject.getInt("room_no") ,"사진" ,nickName ,dataJsonObject.getString("recive_id")));
                            }else{
                                chatList.add(new ChatDataForm(dataJsonObject.getString("send_chat_photo_path"),dataJsonObject.getString("send_id"),dataJsonObject.getString("date_time"), dataJsonObject.getInt("room_no") ,dataJsonObject.getString("send_chat_content") ,nickName , dataJsonObject.getString("recive_id")));
                            }
                        }  length = jsonArray.length();
                        // Recycler Adapter 에서 데이터 변경 사항을 체크하라는 함수 호출
                        chatRcvAdapter.notifyDataSetChanged();
// 데이터를 스트링의 담는다 json데이터를 id라는 키값으로 value를 sd의 담는다..
// String sd = jsonResponse.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatListActivity.this,"통신이 실패했다.",Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("mem_id",mem_id);
                    return params;
                }
            };
            queue = Volley.newRequestQueue(this);
            queue.add(stringRequest);
        }

    }




