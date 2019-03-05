package com.example.user.sadajura;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService{


    private static final String TAG = "MyFirebaseIIDService";
    private RequestQueue queue;
    Context context;

    public MyFirebaseInstanceIDService(Activity context){
        this.context = context;
    }
    // [START refresh_token]
    public void onTokenRefresh(String mem_id) {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        // 생성등록된 토큰을 개인 앱서버에 보내 저장해 두었다가 추가 뭔가를 하고 싶으면 할 수 있도록 한다.
        sendRegistrationToServer(token,mem_id);
    }
    private void sendRegistrationToServer(final String token, final String mem_id) {
        // Add custom implementation, as needed.

        queue = Volley.newRequestQueue(context);
                String url = "http://13.125.107.155/SadaJura/TokenSave.php";
                final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            Boolean success = jsonResponse.getBoolean("success");
                            String mem_id = jsonResponse.getString("mem_id");
                            String mem_photo_path =jsonResponse.getString("mem_photo_path");
                            String mem_photo_title = jsonResponse.getString("mem_photo_title");
                            if(success){
                                Toast.makeText(context,"토큰을 보냈습니다. 성공..",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(context,"토큰을 못보냄. 힝..",Toast.LENGTH_SHORT).show();
                            }
// 데이터를 스트링의 담는다 json데이터를 id라는 키값으로 value를 sd의 담는다..
// String sd = jsonResponse.getString("id");
                        } catch (JSONException e) {
                            Toast.makeText(context,"토큰을 못보냈습니다 실패..",Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,"통신 아예 실패..",Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("token",token);
                        params.put("id",mem_id);
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
}
