package com.example.user.sadajura;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PaymentHistoryAcitvity extends AppCompatActivity {

    private Button product_confirm_btn;
    private TextView product_oem_tv,product_title_tv,product_address_tv,product_ph_num_tv,product_Amount_of_payment_tv;
    private String product_title,product_oem,product_address,product_ph_num,product_Amount_of_payment,nickName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history_acitvity);

        product_confirm_btn  = (Button)findViewById(R.id.product_confirm_btn);
        product_oem_tv = (TextView)findViewById(R.id.product_oem_tv);
        product_title_tv= (TextView)findViewById(R.id.product_title_tv);
        product_address_tv = (TextView)findViewById(R.id.product_address_tv);
        product_ph_num_tv = (TextView)findViewById(R.id.product_ph_num_tv);
        product_Amount_of_payment_tv = (TextView)findViewById(R.id.product_Amount_of_payment_tv);

        Intent intent = getIntent();
        product_title = intent.getStringExtra("product_title");
        product_oem = intent.getStringExtra("product_oem");
        product_address = intent.getStringExtra("product_address");
        product_ph_num = intent.getStringExtra("product_ph_num");
        product_Amount_of_payment = intent.getStringExtra("product_Amount_of_payment");
        nickName = intent.getStringExtra("nickName");

        product_oem_tv.setText(product_oem);
        product_title_tv.setText("상품팜");
        product_address_tv.setText(product_address);
        product_ph_num_tv.setText(product_ph_num);
        product_Amount_of_payment_tv.setText(product_Amount_of_payment);

        product_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaymentHistoryAcitvity.this,MainActivity.class);

                startActivity(intent);
                finish();
            }
        });

    }
}
