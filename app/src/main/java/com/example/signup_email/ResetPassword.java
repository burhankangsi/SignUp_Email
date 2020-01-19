package com.example.signup_email;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResetPassword extends AppCompatActivity {
    TextView tv_reset_page;
    Button btn_reset_page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        tv_reset_page = (TextView)findViewById(R.id.tv_reset_pass);
        btn_reset_page = (Button) findViewById(R.id.btn_reset_activity);
        btn_reset_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResetPassword.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
