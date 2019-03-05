package com.example.user.sadajura;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
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
import com.facebook.drawee.backends.pipeline.Fresco;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.zfdang.multiple_images_selector.SelectorSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WirtePurchaseActivity extends AppCompatActivity {
    ImageView back_btn,board_add_btn,picture_iv,T_map_iv,address_iv;
    private int ReseltCodeBoard = 1111;
    EditText product_title_et, product_price_et,product_contents_et;
    TextView mem_email_tv,email_confirm_tv;
    private RequestQueue queue;
    String mem_id,GoogleId;
    String img_list;

    Uri uri;
    private WriteRcvAdapter writeRcvAdapter;
    private ArrayList<WirteDataForm> imgList;
    private RecyclerView wirte_img_rcv;
    private int ACTIVITY_REQUEST_CODE = 3;
    private Bitmap bitmap;

    ArrayList<String> arrayList;

    private static final int REQUEST_CODE = 123;
    private ArrayList<String> mResults = new ArrayList<>();
    StringBuffer sb;
    private int TmapRequestCode = 80;
    private int successResultCode = 81;
    private int cancelResultCode = 82;
    private Bitmap bmp;

    LinearLayout linearLayout;
    private TMapView tMapView = null;
    private static String mAPIKEY = "acf3670f-d1e5-44ff-b77c-02c83c0f7280";
    private Context mContext = null;
    private static int mMarKerID;
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();
    double latitude;
    double longitude;
    String addressName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wirte_purchase);
        Fresco.initialize(getApplicationContext());
        queue = Volley.newRequestQueue(this);
        mContext = this;

        T_map_iv = (ImageView)findViewById(R.id.google_map_iv);
        picture_iv = (ImageView)findViewById(R.id.picture_iv);
        board_add_btn = (ImageView)findViewById(R.id.board_add_btn);
        back_btn = (ImageView)findViewById(R.id.back_btn);
        product_title_et = (EditText)findViewById(R.id.product_title_et);
        product_price_et = (EditText)findViewById(R.id.product_price_et);
        mem_email_tv = (TextView)findViewById(R.id.mem_email_tv);
        email_confirm_tv = (TextView)findViewById(R.id.email_confirm_tv);
        product_contents_et = (EditText)findViewById(R.id.product_contents_et);
        Intent intent = getIntent();
        mem_id = intent.getStringExtra("mem_id");
        GoogleId = intent.getStringExtra("GoogleId");

        wirte_img_rcv = (RecyclerView)findViewById(R.id.wirte_img_rcv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        wirte_img_rcv.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        wirte_img_rcv.setLayoutManager(linearLayoutManager);
        imgList = new ArrayList<>();
        writeRcvAdapter = new WriteRcvAdapter(this, imgList);
        wirte_img_rcv.setAdapter(writeRcvAdapter);


        T_map_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WirtePurchaseActivity.this,TmapActivity.class);
                startActivityForResult(intent,TmapRequestCode);
            }
        });

        board_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                    for(int i =0; i < arrayList.size(); i++){
//                        img_list = arrayList.get(i);
//                    }
                    setBoard_add_btn();


            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBack_btn();
            }
        });
        picture_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(WirtePurchaseActivity.this);
                dialog.setTitle("알림")
                        .setMessage("사진을 가져올 곳을 선택해 주세요.")
                        .setPositiveButton("카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(WirtePurchaseActivity.this,"카메라 버튼을 누르셨습니다.",Toast.LENGTH_SHORT).show();

                                boolean camera =  ContextCompat.checkSelfPermission
                                        (view.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;


                                boolean write = ContextCompat.checkSelfPermission(view.getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                                if (camera && write) {
                                    //사진찍는 인텐트 코드 넣기.

                                    takePicture();

                                }else {
                                    Toast.makeText(WirtePurchaseActivity.this,"권한이 없습니다 권한을 받아주세요",Toast.LENGTH_LONG).show();

                                }


                            }
                        })
                        .setNegativeButton("갤러리", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(WirtePurchaseActivity.this,"갤러리 버튼을 누르셨습니다",Toast.LENGTH_SHORT).show();
//                                chooseFile();
                                // start multiple photos selector
                                Intent intent = new Intent(WirtePurchaseActivity.this, com.example.user.sadajura.ImagesSelectorActivity.class);
// max number of images to be selected
                                intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER,100);
// min size of image which will be shown; to filter tiny images (mainly icons)
                                intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
// show camera or not
                                intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true);
// pass current selected images as the initial value
                                intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mResults);
// start the selector
                                startActivityForResult(intent, REQUEST_CODE);
                            }
                        }).create().show();
            }
        });

    }



    public void takePicture(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,10);
    }
    private void chooseFile() {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }



    public void setBoard_add_btn(){
        String url = "http://13.125.107.155/SadaJura/ProductWrite.php";
        Random random = new Random();
        final int x = random.nextInt(100000);
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Boolean success = jsonResponse.getBoolean("success");
                    if(success){
                        Toast.makeText(WirtePurchaseActivity.this,"success.",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(WirtePurchaseActivity.this,"fail_response=ok.",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(WirtePurchaseActivity.this,"판매글 작성이 완료 되었습니다.",Toast.LENGTH_SHORT).show();
                Intent BoadAddIntent = new Intent();
                setResult(ReseltCodeBoard);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WirtePurchaseActivity.this,"fail.",Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("product_title", product_title_et.getText().toString());
                params.put("product_price", product_price_et.getText().toString());
                params.put("product_contents",product_contents_et.getText().toString());
                if(addressName == null){

                }else{
                    params.put("latitude", String.valueOf(latitude));
                    params.put("longitude", String.valueOf(longitude));
                    params.put("addressName",addressName);
                }
                for(int i =0; i < arrayList.size(); i++){
                    params.put("send_chat_photo_path"+i,arrayList.get(i));
                }
                params.put("array_list", String.valueOf(arrayList.size()));
//                params.put("send_chat_photo_path", uri);
                params.put("random", String.valueOf(x));
                if(GoogleId == null){
                    params.put("id",mem_id);
                }else{
                    params.put("id",GoogleId);
                }
                return params;
            }
        };
        queue.add(stringRequest);

    }

    public void setBack_btn(){
        Intent BackIntent = new Intent(WirtePurchaseActivity.this,MainActivity.class);
        startActivity(BackIntent);
        finish();
    }

    public String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
        return encodedImage;
    }


    private Bitmap resize(Context context, Uri uri, int resize){
        Bitmap resizeBitmap=null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 4;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap=bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                profile_image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            UploadPicture(mem_id, getStringImage(bitmap));
        } else if (requestCode == 10) {

            Bitmap bitmap2 = (Bitmap) data.getExtras().get("data");
            arrayList.add(getStringImage(bitmap2));
//            profile_image.setImageBitmap(rotate(bitmap2,90));
//            UploadPicture(mem_id, getStringImage(bitmap2));
        } else if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                arrayList = new ArrayList<String>();
                mResults = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);

                assert mResults != null;
                // show results in textview
                sb = new StringBuffer();
//
                for (String result : mResults) {
                    sb.append(result).append("\n");
                    imgList.add(new WirteDataForm(result));
                   uri = Uri.fromFile(new File(result));
//                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        bitmap =  resize(WirtePurchaseActivity.this,uri,500);
                        arrayList.add(getStringImage(bitmap));
//                profile_image.setImageBitmap(bitmap);

                }
                writeRcvAdapter.notifyDataSetChanged();
            }
        }else if(requestCode == TmapRequestCode && resultCode == successResultCode){
            arrayList = new ArrayList<String>();
            addressName = data.getStringExtra("addressName");
            latitude = data.getDoubleExtra("latitude",0);
            longitude = data.getDoubleExtra("longitude",0);

            linearLayout = (LinearLayout)findViewById(R.id.mapview);
            tMapView = new TMapView(this);
            linearLayout.addView(tMapView);
            tMapView.setSKTMapApiKey(mAPIKEY);
            showMarKerPoint(addressName,latitude,longitude);
            tMapView.setCenterPoint(longitude,latitude);

//            String screenShot = data.getStringExtra("screenShot");
//            Uri file_path = Uri.parse(screenShot);
//            try {
//                bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file_path);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//                bmp = resize(WirtePurchaseActivity.this,file_path,1000);
//            address_iv.setImageBitmap(bmp);

        }else if(requestCode == TmapRequestCode && resultCode ==cancelResultCode){

        }
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


