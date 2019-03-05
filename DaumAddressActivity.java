package com.example.user.sadajura;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class DaumAddressActivity extends AppCompatActivity {
    private WebView daum_webView;



    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daum_address);

        // WebView 초기화
        init_webView();
        // 핸들러를 통한 JavaScript 이벤트 반응
    }
    public void init_webView() {
        // WebView 설정
        daum_webView = (WebView) findViewById(R.id.daum_webview);
        // JavaScript 허용
        daum_webView.getSettings().setJavaScriptEnabled(true);
        // JavaScript의 window.open 허용
        daum_webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
        daum_webView.addJavascriptInterface(new AndroidBridge(), "TestApp");
        // web client 를 chrome 으로 설정
        daum_webView.setWebChromeClient(new WebChromeClient());
        // webview url load. php 파일 주소
        daum_webView.loadUrl("http://13.125.107.155/SadaJura/DaumAddress.php");
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void setAddress(final String arg1) {
            handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(handler != null){
                        handler.removeMessages(0);
                        Intent intent = new Intent(DaumAddressActivity.this,SignUpActivity.class);
                        intent.putExtra("arg1",arg1);
                        startActivity(intent);
                        finish();
                    }
                    // WebView를 초기화 하지않으면 재사용할 수 없음
                }
            });
              }
    }
}
