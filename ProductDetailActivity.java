package com.example.user.sadajura;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//안드로이드에서 카카오페이를 바로 결제할수있는 코드
//php서버로 데이터를보내며 db에 저장후 결제를완료한다.
public class ProductDetailActivity extends AppCompatActivity {
    ImageView back_btn,chat_iv,product_img_iv;
    TextView Product_title_tv,Product_price_tv,Product_time_tv,Product_contents_iv,mem_id_tv,modify_tv,delete_tv;
    private RequestQueue queue;
    int no;
    int length;
    String title,mem_id,GoogleId;
    String Product_id,Product_title,Product_price,Product_time,Product_contents;
    Button payment_btn;

    ViewPager viewPager;
    ProductVIewPagerAdapter viewPagerAdapter;
    ArrayList arrayList; // viewPager에서보여줄 item

    //티맵뷰 관련
    LinearLayout linearLayout;
    private TMapView tMapView = null;
    private static String mAPIKEY = "acf3670f-d1e5-44ff-b77c-02c83c0f7280";
    private Context mContext = null;
    private static int mMarKerID;
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();
    double latitude;
    double longitude;
    String addressName;


    public void ProductdetDetail(){
        String url = "http://13.125.107.155/SadaJura/ProductDetailSelect.php";
        arrayList = new ArrayList();
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
//                        Product_id = dataJsonObject.getString("Product_id");
//                        Product_title = dataJsonObject.getString("title");
//                        Product_price = dataJsonObject.getString("price");
//                        Product_time = dataJsonObject.getString("write_time");
//                        Product_contents = dataJsonObject.getString("contents");
//                        no = dataJsonObject.getInt("no");

                        Product_title_tv.setText(dataJsonObject.getString("title"));
                        Product_price_tv.setText(dataJsonObject.getString("price"));
                        Product_time_tv.setText(dataJsonObject.getString("write_time"));
                        Product_contents_iv.setText(dataJsonObject.getString("contents"));
                        mem_id_tv.setText(dataJsonObject.getString("Product_id"));
                        String total_photo_path = dataJsonObject.getString("total_photo_path");
                        String [] total_photo_path_filter = total_photo_path.split(",");
                        String strlatitude = dataJsonObject.getString("latitude");
                        String strlongitude = dataJsonObject.getString("longitude");
                        addressName = dataJsonObject.getString("addressName");
                        if(!strlatitude.isEmpty()){
                            latitude = Double.parseDouble(strlatitude);
                            longitude = Double.parseDouble(strlongitude);
                            showMarKerPoint(dataJsonObject.getString("addressName"),latitude,longitude);
                            tMapView.setCenterPoint(longitude,latitude);
                        }
                        for(int a = 0; a < dataJsonObject.getInt("photo_count"); a++){

                            arrayList.add(total_photo_path_filter[a]);
                        }

                    }


                    viewPager = (ViewPager)findViewById(R.id.vp);
                    viewPagerAdapter = new ProductVIewPagerAdapter(getLayoutInflater(),arrayList);
                    viewPager.setAdapter(viewPagerAdapter);

                        //수정버튼 클릭.
                        modify_tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent1 = new Intent(ProductDetailActivity.this,ProductModifyActivity.class);
                                intent1.putExtra("Product_id",Product_id);
                                intent1.putExtra("Product_title",Product_title);
                                intent1.putExtra("Product_price",Product_price);
                                intent1.putExtra("Product_time",Product_time);
                                intent1.putExtra("Product_contents",Product_contents);
                                intent1.putExtra("ProductNo",no);
                                startActivity(intent1);
                            }
                        });

                    length = jsonArray.length();

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
                return params;
            }
        };
        queue.add(stringRequest);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        modify_tv = (TextView)findViewById(R.id.modify_tv);
        delete_tv = (TextView)findViewById(R.id.delete_tv);
        back_btn = (ImageView)findViewById(R.id.back_btn);
        Product_title_tv= (TextView)findViewById(R.id.Product_title_tv);
        Product_price_tv = (TextView)findViewById(R.id.Product_price_tv);
        Product_time_tv = (TextView)findViewById(R.id.Product_time_tv);
        Product_contents_iv = (TextView)findViewById(R.id.Product_contents_iv);
        mem_id_tv = (TextView)findViewById(R.id.mem_id_tv);
        chat_iv = (ImageView)findViewById(R.id.chat_iv);
        payment_btn = (Button)findViewById(R.id.payment_btn);
        product_img_iv = (ImageView)findViewById(R.id.product_img_iv);
        queue = Volley.newRequestQueue(this);

        mContext = this;
        linearLayout = (LinearLayout)findViewById(R.id.mapview);
        tMapView = new TMapView(this);
        linearLayout.addView(tMapView);
        tMapView.setSKTMapApiKey(mAPIKEY);

        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                Intent intent1 = new Intent(ProductDetailActivity.this,Tmap2Activity.class);
                String strlatitude = String.valueOf(latitude);
                String strlongitude = String.valueOf(longitude);
                intent1.putExtra("latitude",strlatitude);
                intent1.putExtra("longitude",strlongitude);
                intent1.putExtra("addressName",addressName);
                startActivity(intent1);
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }
        });

        payment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this,PaymentActivity2.class);
                intent.putExtra("ProductNo",no);
                intent.putExtra("Product_id",Product_id);
                intent.putExtra("mem_id",mem_id);
                intent.putExtra("GoogleId",GoogleId);
                intent.putExtra("Product_price",Product_price);
                intent.putExtra("Product_title",Product_title);
                startActivity(intent);
                finish();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("id",0);
        mem_id = sharedPreferences.getString("mem_id","");
        GoogleId = sharedPreferences.getString("GoogleId","");
        final Intent intent = getIntent();
        no = intent.getIntExtra("no",0);
        Product_id = intent.getStringExtra("Product_id");

        if(mem_id.equals(Product_id)){
            modify_tv.setVisibility(View.VISIBLE);
            delete_tv.setVisibility(View.VISIBLE);
        }else{
            modify_tv.setVisibility(View.GONE);
            delete_tv.setVisibility(View.GONE);
        }
        //아이템의 상세정보를 DB에서 가지고온다.
        ProductdetDetail();



        //상품판매유저와의 채팅.
        chat_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this,Client.class);
                intent.putExtra("ProductNo",no);
                intent.putExtra("Product_id",Product_id);
                intent.putExtra("mem_id",mem_id);
                intent.putExtra("GoolgeId",GoogleId);
                startActivity(intent);
            }
        });

        //상품 삭제버튼 클릭.
        delete_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteBtnClick();
            }
        });

        //뒤로가기 버튼 클릭시 뒤로 이동
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent BackIntent = new Intent(ProductDetailActivity.this,MainActivity.class);
                BackIntent.putExtra("ProductNo",no);
                BackIntent.putExtra("Product_id",Product_id);
                BackIntent.putExtra("mem_id",mem_id);
                BackIntent.putExtra("GoolgeId",GoogleId);
                startActivity(BackIntent);
                finish();
            }
        });

    }


    public void deleteBtnClick(){
        queue = Volley.newRequestQueue(this);
        String url = "http://13.125.107.155/SadaJura/ProductDelete.php";
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Boolean success = jsonResponse.getBoolean("success");
                    if(success){
                        Toast.makeText(ProductDetailActivity.this,"success.",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ProductDetailActivity.this,"fail_response=ok.",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProductDetailActivity.this,"fail.",Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("no", String.valueOf(no));
                return params;
            }
        };
        queue.add(stringRequest);
        Toast.makeText(ProductDetailActivity.this,"판매글이 삭제 되었습니다.",Toast.LENGTH_SHORT).show();
        Intent BoadDeleteIntent = new Intent(ProductDetailActivity.this,MainActivity    .class);
        startActivity(BoadDeleteIntent);
        finish();
    }

    public void showMarKerPoint(String addressName,double latitude , double longitude){ // 마커 빨강색 찍는곳.
        tMapView.removeAllMarkerItem();

        TMapPoint point = new TMapPoint(latitude,longitude);
        TMapMarkerItem item =  new TMapMarkerItem();
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_dot);

        item.setTMapPoint(point);
//            item.setName(m_mapPoint.get(i).getName());
        item.setCalloutTitle(addressName);
        Bitmap right = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_input_get)).getBitmap();
        item.setCalloutRightButtonImage(right);
        item.setCanShowCallout(true);
        item.setAutoCalloutVisible(true);
        item.setVisible(item.VISIBLE);
        item.setIcon(bitmap);

//            bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.poi_dot);
        //풍선뷰안에 글을 지정한다.
//            item.setCalloutTitle(m_mapPoint.get(i).getName());
//            item.setCalloutSubTitle("서울");
//            item.setCanShowCallout(true);
//            item.setAutoCalloutVisible(true);

//            Bitmap bitmap_1 = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.ic_launcher);

//            item.setCalloutRightButtonImage(bitmap_1);
        String strID = String.format("pmaker%d", mMarKerID++);

        tMapView.addMarkerItem(strID, item);
        mArrayMarkerID.add(strID);
    }

}
