package com.example.user.sadajura;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.Map;

public class ProductModifyActivity extends AppCompatActivity {
    String Product_id,Product_title,Product_price,Product_time,Product_contents,GoogleId,mem_id;
    TextView Product_time_tv,Product_id_tv,success_tv;
    ImageView back_btn,chat_iv;
    EditText Product_title_et,Product_price_et,Product_contents_et;
    private RequestQueue queue;
    int no;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_modify);
        success_tv = (TextView)findViewById(R.id.success_tv);
        back_btn = (ImageView)findViewById(R.id.back_btn);
        Product_title_et= (EditText) findViewById(R.id.Product_title_et);
        Product_price_et = (EditText)findViewById(R.id.Product_price_et);
        Product_time_tv = (TextView)findViewById(R.id.Product_time_tv);
        Product_contents_et = (EditText)findViewById(R.id.Product_contents_et);
        Product_id_tv = (TextView)findViewById(R.id.Product_id_tv);
        chat_iv = (ImageView)findViewById(R.id.chat_iv);
        queue = Volley.newRequestQueue(this);

         Intent intent = getIntent();
         no = intent.getIntExtra("ProductNo",0);
         Product_id = intent.getStringExtra("Product_id");
         Product_title = intent.getStringExtra("Product_title");
         Product_price = intent.getStringExtra("Product_price");
         Product_time = intent.getStringExtra("Product_time");
         Product_contents = intent.getStringExtra("Product_contents");
         SharedPreferences sharedPreferences = getSharedPreferences("id",0);
         mem_id = sharedPreferences.getString("mem_id","");
         GoogleId = sharedPreferences.getString("GoogleId","");

          Product_title_et.setText(Product_title);
          Product_price_et.setText(Product_price);
          Product_time_tv.setText(Product_time);
          Product_id_tv.setText(Product_id);
          Product_contents_et.setText(Product_contents);

        success_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductdetDetail();
            }
        });
    }

    public void ProductdetDetail(){
        String url = "http://13.125.107.155/SadaJura/ProductModify.php";
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
                params.put("no", String.valueOf(no));
                params.put("product_title",Product_title_et.getText().toString());
                params.put("product_price",Product_price_et.getText().toString());
                params.put("product_contents",Product_contents_et.getText().toString());
                return params;
            }
        };
        queue.add(stringRequest);
        Toast.makeText(ProductModifyActivity.this,"판매글이 수정 되었습니다.",Toast.LENGTH_SHORT).show();
        Intent BoadModifyIntent = new Intent(ProductModifyActivity.this,MainActivity.class);
        startActivity(BoadModifyIntent);
        finish();
    }

}
