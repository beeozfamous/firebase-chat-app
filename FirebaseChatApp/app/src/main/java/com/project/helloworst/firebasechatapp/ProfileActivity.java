package com.project.helloworst.firebasechatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private static final int NOT_FRIEND= 0X0;
    private static final int REQUEST_SENT= 0X1;
    private static final int REQUEST_RECEIVED=0X3;
    private static final int FRIEND=0X4;

    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private TextView mName,mStatus,mFriends;
    private ImageView mProfileImage;
    private Button mFriendRequestBTN;
    private Button mDelinceBTN;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mNotificationsData;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mFriendRequestsData;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mFriendListData;

    private int current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String user_id = getIntent().getStringExtra("UserID");
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mFriendRequestsData = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendListData = FirebaseDatabase.getInstance().getReference().child("friends_data");
        mNotificationsData = FirebaseDatabase.getInstance().getReference().child("notifications");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());


        mName = findViewById(R.id.profile_name);
        mStatus = findViewById(R.id.profile_status);
        mFriends = findViewById(R.id.profile_total_friend);
        mProfileImage = findViewById(R.id.profile_image);
        mFriendRequestBTN = findViewById(R.id.profile_sent_btn);
        mDelinceBTN = findViewById(R.id.profile_sent_btn2);

        mDelinceBTN.setVisibility(View.INVISIBLE);
        mDelinceBTN.setEnabled(false);


        current_state = NOT_FRIEND;


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please waith while we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profile_name = dataSnapshot.child("name").getValue().toString();
                String profile_status = dataSnapshot.child("status").getValue().toString();
                String profile_image = dataSnapshot.child("image").getValue().toString();

                mName.setText(profile_name);
                mStatus.setText(profile_status);

                RequestOptions options = new RequestOptions();
                options.centerCrop();
                try {
                    GlideApp.with(ProfileActivity.this).load(profile_image).apply(options).into(mProfileImage);
                } catch (Exception E) {
                    Log.d("onDataChange: ", E.getMessage());
                }
                mProgressDialog.dismiss();
                mFriendRequestsData.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {

                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (request_type.equals("received")) {

                                current_state = REQUEST_RECEIVED;
                                mFriendRequestBTN.setText("ACCEPT FRIEND REQUEST");

                                mDelinceBTN.setVisibility(View.VISIBLE);
                                mDelinceBTN.setEnabled(true);
                            } else if (request_type.equals("sent")) {

                                current_state = REQUEST_SENT;
                                mFriendRequestBTN.setText("CANCEL FRIEND REQUEST");

                                mDelinceBTN.setVisibility(View.INVISIBLE);
                                mDelinceBTN.setEnabled(false);
                            }

                        } else {
                            mFriendListData.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {
                                        current_state = FRIEND;
                                        mFriendRequestBTN.setText("UNFRIEND :'<");
                                        mFriendRequestBTN.setBackgroundResource(R.drawable.button_shape_5);

                                        mDelinceBTN.setVisibility(View.INVISIBLE);
                                        mDelinceBTN.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (mCurrentUser.getUid().equals(user_id)) {
            mFriendRequestBTN.setEnabled(false);
            mDelinceBTN.setEnabled(false);
        }
        {
            mDelinceBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFriendRequestsData.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestsData.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendRequestBTN.setEnabled(true);
                                    current_state = NOT_FRIEND;
                                    mFriendRequestBTN.setText("SEND FRIEND REQUEST");

                                    mFriendRequestBTN.setBackgroundResource(R.drawable.button_shape_4);
                                    mDelinceBTN.setVisibility(View.INVISIBLE);
                                    mDelinceBTN.setEnabled(false);

                                }
                            });
                        }
                    });
                }
            });

            mFriendRequestBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mFriendRequestBTN.setEnabled(false);
                    if (current_state == NOT_FRIEND) {

                        mFriendRequestsData.child(mCurrentUser.getUid())
                                .child(user_id)
                                .child("request_type")
                                .setValue("sent")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            mFriendRequestsData.child(user_id)
                                                    .child(mCurrentUser.getUid())
                                                    .child("request_type")
                                                    .setValue("received")
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            HashMap<String, String> notificationData = new HashMap<>();
                                                            notificationData.put("from", mCurrentUser.getUid());
                                                            notificationData.put("type", "request");

                                                            mNotificationsData.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    current_state = REQUEST_SENT;
                                                                    mFriendRequestBTN.setText("CANCEL FRIEND REQUEST");
                                                                    Toast.makeText(ProfileActivity.this, "Successful sent friend request", Toast.LENGTH_LONG).show();

                                                                }
                                                            });
                                                        }
                                                    });

                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Cannot sent friends request.", Toast.LENGTH_LONG).show();
                                        }

                                        mFriendRequestBTN.setEnabled(true);

                                    }
                                });

                    }
                    if (current_state == FRIEND) {
                        mFriendListData.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendListData.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mFriendRequestBTN.setEnabled(true);
                                        current_state = NOT_FRIEND;
                                        mFriendRequestBTN.setText("SEND FRIEND REQUEST");

                                        mFriendRequestBTN.setBackgroundResource(R.drawable.button_shape_4);
                                        mDelinceBTN.setVisibility(View.INVISIBLE);
                                        mDelinceBTN.setEnabled(false);
                                    }
                                });
                            }
                        });
                    }
                    if (current_state == REQUEST_SENT) {
                        mFriendRequestsData.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendRequestsData.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                        mFriendRequestBTN.setEnabled(true);
                                        current_state = NOT_FRIEND;
                                        mFriendRequestBTN.setText("SEND FRIEND REQUEST");

                                        mDelinceBTN.setVisibility(View.INVISIBLE);
                                        mDelinceBTN.setEnabled(false);

                                    }
                                });
                            }
                        });
                    }
                    if (current_state == REQUEST_RECEIVED) {

                        String current_date = DateFormat.getDateTimeInstance().format(new Date());
                        mFriendListData.child(mCurrentUser.getUid()).child(user_id).child("date").setValue(current_date)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mFriendListData.child(user_id).child(mCurrentUser.getUid()).child("date").setValue(current_date)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mFriendRequestsData.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mFriendRequestsData.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {

                                                                                        mFriendRequestBTN.setEnabled(true);
                                                                                        current_state = FRIEND;
                                                                                        mFriendRequestBTN.setText("UNFRIEND :'<");
                                                                                        mFriendRequestBTN.setBackgroundResource(R.drawable.button_shape_5);

                                                                                        mDelinceBTN.setVisibility(View.INVISIBLE);
                                                                                        mDelinceBTN.setEnabled(false);
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }

                }
            });
        }
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        mUserRef.child("online").setValue(true);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mUserRef.child("online").setValue(false);
//    }

}
