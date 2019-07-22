 package com.project.helloworst.firebasechatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;


 public class SettingsActivity extends AppCompatActivity {

    private ImageView mImage1;
    private ImageView mImage2;
    private ImageView mImage3;

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUserData;

    private ImageView mUserDisplayImage;
    private TextView mUserDisplayName;
    private TextView mUserDisplayStatus;

    private Button mStatusButton;
    private String oldString="OLD_STRING";
    private Toolbar mToolbar;

    private StorageReference mProfileImage;
     private DatabaseReference mUserRef;
     private FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;

    private TextView tool_bar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mUserDisplayImage=findViewById(R.id.setting_user_thumb_image);
        mUserDisplayName=findViewById(R.id.setting_user_name);
        mUserDisplayStatus=findViewById(R.id.setting_user_status);

        tool_bar_title=findViewById(R.id.tool_bar_title);
        tool_bar_title.setText("user setting");

        mToolbar=findViewById(R.id.setting_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog= new ProgressDialog(SettingsActivity.this);
        mProgressDialog.setTitle("Loading Your Data");
        mProgressDialog.setMessage("Please wait while we download your data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mCurrentUserData=FirebaseAuth.getInstance().getCurrentUser();
        String currentUserUID= mCurrentUserData.getUid();

        mProfileImage=FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserUID);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String user_image=dataSnapshot.child("image").getValue().toString();
                String user_name=dataSnapshot.child("name").getValue().toString();
                String user_status=dataSnapshot.child("status").getValue().toString();
                String user_thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                mUserDisplayName.setText(user_name);
                mUserDisplayStatus.setText(user_status);
                try {
                    GlideApp.with(SettingsActivity.this).load(user_image).apply(RequestOptions.circleCropTransform()).into(mUserDisplayImage);
                }catch (Exception e){
                    Log.d("GILDE APP", "onDataChange: fail to load image");
                }

                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mStatusButton= findViewById(R.id.setting_user_change_status_btn);
        mStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent= new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra(oldString,mUserDisplayStatus.getText().toString());
                startActivity(statusIntent);
            }
        });
        mUserDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvatarBottomSheetFragment bottomSheetFragment= new AvatarBottomSheetFragment();
                bottomSheetFragment.show(getSupportFragmentManager(),"Sample bottom sheet");

            }
        });
    }

//     @Override
//     public void onStart() {
//         super.onStart();
//         mUserRef.child("online").setValue(true);
//     }
//
//     @Override
//     protected void onStop() {
//         super.onStop();
//         mUserRef.child("online").setValue(false);
//     }
 }
