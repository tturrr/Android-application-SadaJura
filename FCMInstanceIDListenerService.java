package com.example.user.sadajura;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FCMInstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFiebaseIDService";

    public void onTokenRefresh(){
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"Refreshed token :" + token);
    }
}
