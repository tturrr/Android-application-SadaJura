package com.example.user.sadajura;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Wallet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private ImageButton btn_photo;
    private Button btn_logout,coin_btn;
    SessionManager sessionManager;
    private TextView id_tv;
    private static String URL_READ = "http://13.125.107.155/SadaJura/ProfileRead.php";
    private String mid;
    private RequestQueue queue;
    private Bitmap bitmap;
    CircleImageView profile_image;
    private static String URL_UPLOAD = "http://13.125.107.155/SadaJura/ProfileRead.php";
    int a = 0;
    private String mem_photo_title;
    private String mCurrentPhotoPath;
    SharedPreferences increment_shhared;
    private Uri selectedImage;
    private String GoogleId,GooglePhotoString ,mem_photo_path ,mem_id;
    private int opencvRequestCode = 333;
    private ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        btn_photo = (ImageButton) findViewById(R.id.btn_photo);
        id_tv = (TextView) findViewById(R.id.id_tv);
        btn_logout = (Button) findViewById(R.id.btn_logout);
        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        Intent intent = getIntent();
        GooglePhotoString = intent.getStringExtra("GooglePhotoString");
        mem_photo_path = intent.getStringExtra("mem_photo_path");
        mem_photo_title = intent.getStringExtra("mem_photo_title");
        GoogleId = intent.getStringExtra("GoogleId");
        mem_id = intent.getStringExtra("mem_id");
        back_btn = (ImageView)findViewById(R.id.back_btn);
        coin_btn = (Button)findViewById(R.id.coin_btn);


        coin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,WalletActivity.class);
                intent.putExtra("mem_id",mem_id);
                startActivity(intent);
                finish();
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                intent.putExtra("mem_id",mem_id);
                startActivity(intent);
                finish();
            }
        });

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        HashMap<String, String> user = sessionManager.getUserDetail();
        mid = user.get(sessionManager.ID);
        if(GoogleId != null){
            id_tv.setText(GoogleId);
        }else {
            id_tv.setText(mid);
        }
        increment_shhared = getSharedPreferences(mid+"increment",0);
        a = increment_shhared.getInt("increment",0);

        if(GooglePhotoString != null){
            Glide.with(this)
                    .load(GooglePhotoString)
                    .into(profile_image);
        }else if (GooglePhotoString == null && mem_photo_path != null ){
            Glide.with(this)
                    .load("http://13.125.107.155/SadaJura/uploads/" + mid +mem_photo_title+ ".jpeg")
                    .into(profile_image);
        }else {
            Glide.with(this)
                    .load(R.drawable.profile_user)
                    .into(profile_image);
        }


        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionManager.logout();
            }
        });

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ProfileActivity.this);
                dialog.setTitle("알림")
                        .setMessage("사진을 가져올 곳을 선택해 주세요.")
                        .setPositiveButton("카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(ProfileActivity.this,"카메라 버튼을 누르셨습니다.",Toast.LENGTH_SHORT).show();

                                boolean camera =  ContextCompat.checkSelfPermission
                                        (view.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;


                                boolean write = ContextCompat.checkSelfPermission(view.getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                                if (camera && write) {
                                    //사진찍는 인텐트 코드 넣기.

                                    takePicture();

                                }else {
                                    Toast.makeText(ProfileActivity.this,"권한이 없습니다 권한을 받아주세요",Toast.LENGTH_LONG).show();

                                }


                            }
                        })
                        .setNegativeButton("갤러리", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(ProfileActivity.this,"갤러리 버튼을 누르셨습니다",Toast.LENGTH_SHORT).show();
                                chooseFile();
                            }
                        })
                        .setNeutralButton("얼굴인식 카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(ProfileActivity.this,"얼굴인식 버튼을 누르셨습니다",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ProfileActivity.this,OpencvActivity.class);
                                startActivityForResult(intent,opencvRequestCode);
                            }
                        }).create().show();

            }
        });
    }

    private void chooseFile() {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profile_image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            UploadPicture(mem_id, getStringImage(bitmap));
        }
        else if(requestCode == 10){
            Bitmap bitmap2 = (Bitmap)data.getExtras().get("data");
            profile_image.setImageBitmap(bitmap2);
            rotate(bitmap2,90);
//            profile_image.setImageBitmap(rotate(bitmap2,90));
            UploadPicture(mem_id, getStringImage(bitmap2));
        }else if(requestCode == opencvRequestCode && resultCode == 100){
                      Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Glide.with(this)
                              .load(path)
                              .into(profile_image);
            UploadPicture(mem_id, getStringImage(bitmap));
        }else if (requestCode == opencvRequestCode && resultCode == 75){
            int rotate_cam = data.getIntExtra("rotate_cam",0);
            Intent intent = new Intent(ProfileActivity.this, OpencvActivity.class);
            intent.putExtra("rotate_cam",rotate_cam);
            startActivityForResult(intent,opencvRequestCode);
        }
    }
        //프로필 이미지를 등록한다.
    private void UploadPicture(final String id, final String photo) {
        increment_shhared = getSharedPreferences(mid+"increment",0);
        SharedPreferences.Editor setincrement_shhared = increment_shhared.edit();
        a++;
        setincrement_shhared.putInt("increment",a).commit();
        final String b = String.valueOf(a);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        Toast.makeText(ProfileActivity.this, "사진이 변경 되었습니다.", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ProfileActivity.this,"사진이 변경 되었습니다.",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this,"인터넷 연결을 확인 해 주세요",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("increment", b);
                params.put("id", id);
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
    public void takePicture(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,10);
    }
    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees);
            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap = null;
                    bitmap = converted;
                    converted = null;
                }
            } catch (OutOfMemoryError ex) {

            }
        }
        return bitmap;
    }
}
