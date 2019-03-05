package com.example.user.sadajura;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity2 extends AppCompatActivity {

    private ImageView back_btn;
    private EditText mEditTextName,ph_num_et,address1_et,address2_et;
    private EditText mEditTextTotal_Amounts;
    private EditText mEditTextItem_Name;
    private TextView mTextViewResult;
    public String presentURLString;
    String[] parsingToken;
    String parsedToken="unknown";
    String name;
    String mem_id,nickName,GoogleId,Product_id,Product_title,Product_price;
    int ProductNo;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final Intent intent = getIntent();
        Product_price = intent.getStringExtra("Product_price");
        Product_title = intent.getStringExtra("Product_title");
        ProductNo = intent.getIntExtra("ProductNo",0);
        Product_id = intent.getStringExtra("Product_id");
        mem_id = intent.getStringExtra("mem_id");
        GoogleId = intent.getStringExtra("GoogleId");

        address2_et = (EditText)findViewById(R.id.address2_et);
        address1_et = (EditText)findViewById(R.id.address1_et);
        ph_num_et = (EditText)findViewById(R.id.ph_num_et);
        back_btn = (ImageView)findViewById(R.id.back_btn);
        mEditTextName = (EditText)findViewById(R.id.editText_main_name);
        mEditTextTotal_Amounts = (EditText)findViewById(R.id.editText_main_total_amounts);
        mEditTextItem_Name = (EditText)findViewById(R.id.editText_main_item_name);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);

        if (mem_id == null && GoogleId.equals("") ==false ) {
            nickName = GoogleId;
        } else if(mem_id != null && GoogleId.equals("")){
            nickName = mem_id;
        }
        meminfo(nickName);


        mEditTextTotal_Amounts.setFocusable(false);
        mEditTextTotal_Amounts.setClickable(false);
        mEditTextTotal_Amounts.setText(10000+"원");
        mEditTextItem_Name.setFocusable(false);
        mEditTextItem_Name.setClickable(false);
        mEditTextItem_Name.setText("상품팜");

        //뒤로가기버튼
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaymentActivity2.this,ProductDetailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonInsert = (Button)findViewById(R.id.button_main_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = mEditTextName.getText().toString();
                String total_amounts = mEditTextTotal_Amounts.getText().toString();
                String item_name = mEditTextItem_Name.getText().toString();
                String address1 = address1_et.getText().toString();
                String address2 = address2_et.getText().toString();

                InsertData task = new InsertData();
                task.execute(name,total_amounts,item_name);


            }
        });

    }

    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(PaymentActivity2.this,
                    "잠시만 기다려주세요", null, true, true);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            mTextViewResult.setText(result);

            showResult(result);

        }

        public class MyWebViewClient extends WebViewClient {
            @Override
            public void onLoadResource (WebView view, String url)
            {
                presentURLString = view.getUrl();

                if (presentURLString.contains("pg-web")) {
                    parsingToken = presentURLString.split("/");
                    parsedToken = parsingToken[4];
//                    Toast.makeText(getApplicationContext(), parsedToken, Toast.LENGTH_SHORT).show(); // 토큰이 잘뜨고있는지 확인하는 용도

                    ApproveProcess task_a = new ApproveProcess(); // 승인요청 클래스생성
                    task_a.execute(parsedToken,name); // 승인요청 클래스 실행
                    Intent intent = new Intent(PaymentActivity2.this,PaymentHistoryAcitvity.class);
                    intent.putExtra("product_title",Product_title);
                    intent.putExtra("product_oem",mEditTextName.getText().toString());
                    intent.putExtra("product_address",address1_et.getText().toString()+" "+address2_et.getText().toString());
                    intent.putExtra("product_ph_num",ph_num_et.getText().toString());
                    intent.putExtra("product_Amount_of_payment",mEditTextTotal_Amounts.getText().toString());
                    intent.putExtra("nickName",nickName);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if (existPackage != null) {
                            startActivity(intent);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id="+intent.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (url != null && url.startsWith("market://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            startActivity(intent);
                        }
                        return true;
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                view.loadUrl(url);
                return false;
            }
        }
        protected void showResult(String result) {

            String pay_url = null;
            String TAG_ID = "next_redirect_app_url";

            setContentView(R.layout.activity_webview);
            try {
                JSONObject jsonObj = new JSONObject(result);
                pay_url = jsonObj.getString(TAG_ID);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            WebView webView = (WebView) findViewById(R.id.serverWebview);
            webView.setWebViewClient(new MyWebViewClient());
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);

            webView.loadUrl(pay_url);

        }

        @Override
        protected String doInBackground(String... params) {

            String name = (String)params[0];
            String total_amounts = (String)params[1];
            String item_name = (String)params[2];

            String serverURL= " http://13.125.107.155/SadaJura/payment.php"; // http://example.com/결제준비php이름.php
            String postParameters = "name=" + name + "&total_amounts=" + total_amounts + "&item_name=" + item_name;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();


                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {



                return new String("Error: " + e.getMessage());
            }


        }

    }
    class ApproveProcess extends  AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(PaymentActivity2.this,
                    "잠시만 기다려주세요", null, true, true);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            mTextViewResult.setText(result);

        }


        @Override
        protected String doInBackground(String... params) {

            String parsedToken = (String)params[0];
            String name = (String)params[1];

            String serverURL = "http://13.125.107.155/SadaJura/afterpayment.php"; // http://example.com/승인요청php이름.php
            String postParameters = "parsedToken=" + parsedToken + "&name=" + name;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();


                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {



                return new String("Error: " + e.getMessage());
            }


        }

    }

    public void meminfo(final String nickName){

        String url = "http://13.125.107.155/SadaJura/MemInfo.php";
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Boolean success = jsonResponse.getBoolean("success");
                    String mem_id = jsonResponse.getString("mem_id");
                    String mem_address1 =jsonResponse.getString("mem_address1");
                    String mem_address2 = jsonResponse.getString("mem_address2");
                    String mem_phone = jsonResponse.getString("mem_phone");
                    if(success){
                        mEditTextName.setText(mem_id);
                        ph_num_et.setText(mem_phone);
                        address1_et.setText(mem_address1);
                        address2_et.setText(mem_address2);
                    }else{
                        Toast.makeText(PaymentActivity2.this,"회원정보를 가져오지 못했습니다.",Toast.LENGTH_SHORT).show();
                    }
// 데이터를 스트링의 담는다 json데이터를 id라는 키값으로 value를 sd의 담는다..
// String sd = jsonResponse.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(PaymentActivity2.this,"회원정보를 가져오지 못했습니다.2",Toast.LENGTH_SHORT).show();
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
                params.put("id", nickName);
                return params;
            }
        };
        queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
