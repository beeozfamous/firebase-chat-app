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
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText mRegEmail;
    private EditText mRegPassword;
    private EditText mRegUsername;
    private Button mRegDone;

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    private ProgressDialog mRegProgress;

    private ConstraintLayout mLayout;

    private TextInputLayout input_layout_password;

    private TextView tool_bar_title;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        input_layout_password=findViewById(R.id.reg_pass_layout);
        input_layout_password.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/DancingScript-Regular.ttf"));

        tool_bar_title=findViewById(R.id.tool_bar_title);
        tool_bar_title.setText("create account");

        mToolbar= findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);;
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress= new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();

        mRegEmail=findViewById(R.id.reg_email);
        mRegPassword=findViewById(R.id.reg_password);
        mRegUsername=findViewById(R.id.reg_username);
        mRegDone=findViewById(R.id.reg_done);


        mRegDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputEmail= mRegEmail.getEditableText().toString();
                String inputPassword= mRegPassword.getEditableText().toString();
                String inputUsername=mRegUsername.getEditableText().toString();

                if(!TextUtils.isEmpty(inputEmail) || !TextUtils.isEmpty(inputPassword)||!TextUtils.isEmpty(inputUsername)) {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_User(inputEmail, inputPassword, inputUsername);

                }
            }
        });
        mLayout=findViewById(R.id.register_contain);
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils SIS= new Utils();
                SIS.hideKeyboard(RegisterActivity.this,v);
            }
        });
    }

    private void register_User(String inputEmail,String inputPassword,String inputUsername){

        mAuth.createUserWithEmailAndPassword(inputEmail, inputPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser current_user =FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                    String token = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String,String> userMap= new HashMap<>();
                    userMap.put("name",inputUsername);
                    userMap.put("status","A very simple status.");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    userMap.put("device_token",token);

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mRegProgress.dismiss();
                                Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });
                }else{

                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this,"Cannot register, check the form and try again.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
