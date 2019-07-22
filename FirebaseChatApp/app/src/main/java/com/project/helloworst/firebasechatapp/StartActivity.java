package com.project.helloworst.firebasechatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class StartActivity extends AppCompatActivity {

    private Button mRegButton;
    private Button mLogButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        mLogButton= findViewById(R.id.start_login_btn);

        mLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent log_intent= new Intent(StartActivity.this,LoginActivity.class);
                startActivity(log_intent);
            }
        });

        mRegButton= findViewById(R.id.start_reg_btn);

        mRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent= new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(reg_intent);
            }
        });
    }
}
