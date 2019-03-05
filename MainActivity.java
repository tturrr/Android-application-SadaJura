package com.example.user.sadajura;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private String mem_photo_path , mem_id , GooglePhotoString ,GoogleId;
    private Uri GooglePhotoUri;
    private String mem_photo_title;
    ArrayList<String> arrayList;

    RcvAdapter rcvAdapter;
    private RecyclerView rcv;
    private int RequestCodeBoard = 10;
    private String url = "http://13.125.107.155/SadaJura/ProductSelect.php";
    private RequestQueue queue;
    int length;
    ArrayList<DataForm> list;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Intent intent = getIntent();
        mem_photo_path = intent.getStringExtra("mem_photo_path");
        mem_photo_title = intent.getStringExtra("mem_photo_title");
        mem_id = intent.getStringExtra("mem_id");
        GooglePhotoString = intent.getStringExtra("GooglePhotoUri");
        GoogleId = intent.getStringExtra("GoogleId");

        queue = Volley.newRequestQueue(this);


        //리사이클러뷰 관련
        rcv = (RecyclerView) findViewById(R.id.main_rcv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcv.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        rcv.setLayoutManager(linearLayoutManager);
        list = new ArrayList<>();
        rcvAdapter = new RcvAdapter(this, list);
        rcv.setAdapter(rcvAdapter);

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
                        list.add(new DataForm(dataJsonObject.getString("title"),dataJsonObject.getString("Product_id"), dataJsonObject.getString("photo_path") ,dataJsonObject.getInt("no")));
                    }  length = jsonArray.length();
                    // Recycler Adapter 에서 데이터 변경 사항을 체크하라는 함수 호출
                    rcvAdapter.notifyDataSetChanged();
// 데이터를 스트링의 담는다 json데이터를 id라는 키값으로 value를 sd의 담는다..
// String sd = jsonResponse.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"통신이 실패했다.",Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        queue.add(stringRequest);


        SharedPreferences sharedPreferences = getSharedPreferences("id",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mem_id",mem_id);
        editor.putString("GoogleId",GoogleId);
        editor.commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navigation_item_attachment:
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                        Intent MainActivityIntent = new Intent(MainActivity.this,MainActivity.class);
                        MainActivityIntent.putExtra("mem_id",mem_id);
                        MainActivityIntent.putExtra("GoogleId",GoogleId);
                        MainActivityIntent.putExtra("GooglePhotoString",GooglePhotoString);
                        MainActivityIntent.putExtra("mem_photo_path",mem_photo_path);
                        startActivity(intent);
                        finish();
                        break;

                    case R.id.navigation_item_chatting:
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                        Intent ChattingIntetnt = new Intent(MainActivity.this,ChatListActivity.class);
                        ChattingIntetnt.putExtra("mem_id",mem_id);
                        ChattingIntetnt.putExtra("GoogleId",GoogleId);
                        ChattingIntetnt.putExtra("GooglePhotoString",GooglePhotoString);
                        ChattingIntetnt.putExtra("mem_photo_path",mem_photo_path);
                        startActivity(ChattingIntetnt);
                        finish();
                        break;

                    case R.id.navigation_item_location:
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                        Intent ProxyPurchaseIntent = new Intent(MainActivity.this,ShoppingBasketActivity.class);
                        ProxyPurchaseIntent.putExtra("mem_id",mem_id);
                        ProxyPurchaseIntent.putExtra("GoogleId",GoogleId);
                        ProxyPurchaseIntent.putExtra("GooglePhotoString",GooglePhotoString);
                        ProxyPurchaseIntent.putExtra("mem_photo_path",mem_photo_path);
                        startActivity(ProxyPurchaseIntent);
                        finish();
                        break;

                    case R.id.nav_sub_menu_item01:
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                        break;

                    case R.id.nav_sub_menu_item02:
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                        break;

                }

                return true;
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        ImageView profile_img = (ImageView) findViewById(R.id.profile_image);
        TextView id_txt = (TextView)findViewById(R.id.id_txt);
        if(GooglePhotoString != null){
            Glide.with(this).load(GooglePhotoString).into(profile_img);
        }
        else if(mem_photo_path != null) {
            Glide.with(this).load(mem_photo_path).into(profile_img);
        }else{
            Glide.with(this).load(R.drawable.profile_user).into(profile_img);
        }
        if(GoogleId != null){
            id_txt.setText(GoogleId);
        }
        if(mem_id != null){
            id_txt.setText(mem_id);
        }
        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                intent.putExtra("GooglePhotoString",GooglePhotoString);
                intent.putExtra("GoogleId",GoogleId);
                intent.putExtra("mem_photo_path",mem_photo_path);
                intent.putExtra("mem_photo_title",mem_photo_title);
                intent.putExtra("mem_id",mem_id);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this,WirtePurchaseActivity.class);
                intent.putExtra("mem_id",mem_id);
                intent.putExtra("GoogleId",GoogleId);
                startActivityForResult(intent,RequestCodeBoard);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RequestCodeBoard && resultCode == 1111){
            arrayList = new ArrayList<>();
            arrayList.clear();
            list.clear();
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
                            list.add(new DataForm(dataJsonObject.getString("title"),mem_id, dataJsonObject.getString("photo_path") , dataJsonObject.getInt("no")));
                        }  length = jsonArray.length();
                        // Recycler Adapter 에서 데이터 변경 사항을 체크하라는 함수 호출
                        rcvAdapter.notifyDataSetChanged();
// 데이터를 스트링의 담는다 json데이터를 id라는 키값으로 value를 sd의 담는다..
// String sd = jsonResponse.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this,"통신이 실패했다.",Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }
            };
            queue.add(stringRequest);
        }rcvAdapter.notifyDataSetChanged();
    }

}