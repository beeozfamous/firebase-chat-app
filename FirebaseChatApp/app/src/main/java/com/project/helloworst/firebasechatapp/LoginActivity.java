package com.project.helloworst.firebasechatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText mLogEmail;
    private EditText mLogPassword;
    private Button mLogButton;
    private Toolbar mLogToolBar;
    private ProgressDialog mLogProgress;
    private FirebaseAuth mAuth;
    private ConstraintLayout mLayout;
    private TextInputLayout input_layout_password;
    private TextView tool_bar_title;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        input_layout_password=findViewById(R.id.textInputLayout2);
        input_layout_password.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/DancingScript-Regular.ttf"));

        tool_bar_title=findViewById(R.id.tool_bar_title);
        tool_bar_title.setText("log in");

        mLogToolBar=findViewById(R.id.login_toolbar);
        setSupportActionBar(mLogToolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogProgress= new ProgressDialog(this);

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users");

        mLogEmail= findViewById(R.id.login_email);
        mLogPassword= findViewById(R.id.login_password);
        mAuth=FirebaseAuth.getInstance();
        mLogButton=findViewById(R.id.log_done);

        mLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputEmail= mLogEmail.getEditableText().toString();
                String inputPassword= mLogPassword.getEditableText().toString();

                if(!TextUtils.isEmpty(inputEmail)||!TextUtils.isEmpty(inputPassword)){
                    mLogProgress.setTitle("Logging in");
                    mLogProgress.setMessage("Please wait while we do the credential.");
                    mLogProgress.setCanceledOnTouchOutside(false);
                    mLogProgress.show();
                    login_user(inputEmail,inputPassword);
                }
            }
        });
        mLayout=findViewById(R.id.login_contain);

        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils SIS= new Utils();
                SIS.hideKeyboard(LoginActivity.this,v);
            }
        });
    }

    private void login_user(String inputEmail, String inputPassword) {
        mAuth.signInWithEmailAndPassword(inputEmail,inputPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mLogProgress.dismiss();

                    String token = FirebaseInstanceId.getInstance().getToken();
                    Log.d("", "sendRegistrationToServer : " + token);

                    String current_userID=mAuth.getCurrentUser().getUid();

                    mUserDatabase.child(current_userID).child("device_token").setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent= new Intent(LoginActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });
                }
                else
                {
                    mLogProgress.hide();
                    Toast.makeText(LoginActivity.this,"Cannot login please check username and password again.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
