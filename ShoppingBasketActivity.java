package com.example.user.sadajura;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ShoppingBasketActivity extends AppCompatActivity {

    private ImageView back_btn;
    private ShoppingBasketRcvAdapter ShoppingRcvAdapter;
    private ArrayList<ShoppingBasketDataForm> ShoppingList;
    private RecyclerView ShoppingRcv;
    private String GooglePhotoString,mem_photo_path,nick_name;
    private RequestQueue queue;
    private String url = "http://13.125.107.155/SadaJura/ShoppingBasket.php";
    String mem_id,GoogleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_basket);

        back_btn = (ImageView)findViewById(R.id.back_btn);
        Intent intent = getIntent();
        SharedPreferences pref = getSharedPreferences("id",0);
        String mem_id = pref.getString("mem_id","");
        String GoogleId = pref.getString("GoogleId","");

        GooglePhotoString = intent.getStringExtra("GooglePhotoString");
        mem_photo_path = intent.getStringExtra("mem_photo_path");

        if(mem_id == null && GoogleId != null){
            nick_name = GoogleId;
        }else{
            nick_name = mem_id;
        }


        ShoppingRcv = (RecyclerView) findViewById(R.id.main_rcv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ShoppingRcv.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        ShoppingRcv.setLayoutManager(linearLayoutManager);
        ShoppingList = new ArrayList<>();
        ShoppingRcvAdapter = new ShoppingBasketRcvAdapter(this, ShoppingList);
        ShoppingRcv.setAdapter(ShoppingRcvAdapter);

        ShoppingListFromServer(nick_name);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShoppingBasketActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void ShoppingListFromServer(final String nick_name){
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
                        ShoppingList.add(new ShoppingBasketDataForm(dataJsonObject.getString("item_name"),dataJsonObject.getString("time_value") , dataJsonObject.getString("product_img"),dataJsonObject.getString("total_amount")));
                    }
                    // Recycler Adapter 에서 데이터 변경 사항을 체크하라는 함수 호출
                    ShoppingRcvAdapter.notifyDataSetChanged();
// 데이터를 스트링의 담는다 json데이터를 id라는 키값으로 value를 sd의 담는다..
// String sd = jsonResponse.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ShoppingBasketActivity.this,"통신이 실패했다.",Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id",nick_name);
                return params;
            }
        };
        queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
