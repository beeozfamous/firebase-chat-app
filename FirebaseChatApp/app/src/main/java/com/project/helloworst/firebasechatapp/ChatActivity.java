package com.project.helloworst.firebasechatapp;


import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private String mChatUserName;
    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private TextView mDisplayName;
    private TextView mLastSeen;
    private String mChatImage;
    private ImageView mProfileImage;
    private DatabaseReference mRootRef;
    private String mCurrentUserID;
    private EditText mChatBox;
    private ImageView mSendButton;
    private ImageView mAddButton;
    private RecyclerView mMessageView;
    private final List<Messages> mMessagesList= new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter mMessageAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD = 20;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String lastKey="";
    private String prevKey="";
    private Boolean once=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mToolbar= findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        mActionBar=getSupportActionBar();

        mRootRef= FirebaseDatabase.getInstance().getReference();
        mCurrentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        mChatUserName=getIntent().getStringExtra("UserName");
        mChatUser=getIntent().getStringExtra("UserID");
        mChatImage=getIntent().getStringExtra("ImageLink");


        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");

        LayoutInflater inflater =(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view= inflater.inflate(R.layout.chat_custom_bar,null);

        mActionBar.setCustomView(action_bar_view);
        mSwipeRefreshLayout=findViewById(R.id.message_swipe_layout);
        mDisplayName=findViewById(R.id.custom_bar_name);
        mLastSeen=findViewById(R.id.custom_bar_last_seen);
        mProfileImage=findViewById(R.id.custom_bar_image);
        mChatBox=findViewById(R.id.chat_edit_text);
        mSendButton=findViewById(R.id.chat_send_button);
        mAddButton= findViewById(R.id.chat_add_button);
        mMessageAdapter= new MessageAdapter(mMessagesList,mChatImage);
        mMessageView=findViewById(R.id.message_recycler_view);
        mLinearLayoutManager= new LinearLayoutManager(this);
        mMessageView.setHasFixedSize(true);
        mMessageView.setLayoutManager(mLinearLayoutManager);
        mMessageView.setAdapter(mMessageAdapter);

        loadMessages();

        mDisplayName.setText(mChatUserName);

        GlideApp.with(ChatActivity.this).load(mChatImage).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
        mRootRef.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child("online").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                if(online.equals("true")){
                    mLastSeen.setText("Online");
                }else{
                    mLastSeen.setText("Last seen : "+getDate(Long.valueOf(online)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.d("ChatActivity","mChatUser: " + mChatUser);
        mRootRef.child("chat").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRootRef.child("chat").child(mChatUser).child(mCurrentUserID).child("seen").setValue(true).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("CHAT_ERROR", e.getMessage().toString());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getChatFriendSent();


        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendButton.setImageResource(R.drawable.ic_chat_send_hold);
                sendMessage();
                mChatBox.setText("");
                mSendButton.setImageResource(R.drawable.ic_chat_send);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;

                itemPos=0;

                loadMoreMessages();
            }
        });

        mMessageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom)
            {
                //mMessageView.scrollToPosition(mMessageView.getAdapter().getItemCount()-1);
            }
        });

    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(20);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages= dataSnapshot.getValue(Messages.class);
                String messageKey =dataSnapshot.getKey();

                mMessagesList.add(itemPos++,messages);

                if(!prevKey.equals(messageKey)){
                    mMessagesList.add(itemPos++,messages);
                }else{
                    prevKey=lastKey;
                }

                if(itemPos==1){

                    lastKey = messageKey;
                }

                Log.d("TOTAL KEYS", "Last key :"+ lastKey +"...Prev key :"+ prevKey+"...Message key :"+ messageKey);

                mMessageAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);

                mLinearLayoutManager.scrollToPositionWithOffset(20,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserID).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        mRootRef.child("messages").child(mCurrentUserID).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages= dataSnapshot.getValue(Messages.class);

                Log.d("onChildAdded: ","Chat Friend sent messages yyy" );

                itemPos++;

                if(itemPos==1){
                    String messageKey =dataSnapshot.getKey();

                    lastKey = messageKey;

                    prevKey= messageKey;
                }

                mMessagesList.add(messages);
                mMessageAdapter.notifyDataSetChanged();

                mMessageView.scrollToPosition(mMessagesList.size() - 1);

                mSwipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("onChildAdded: ","Chat Friend sent messages xxx" );
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getChatFriendSent(){
        try {
            mRootRef.child("chat").child(mCurrentUserID).child(mChatUser).child("last_message_id").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null) {
                        String lastID = dataSnapshot.getValue().toString();
                        mRootRef.child("messages").child(mCurrentUserID).child(mChatUser).child(lastID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child("from").getValue()!=null) {
                                    if (!dataSnapshot.child("from").getValue().toString().equals(mCurrentUserID)) {
                                        if(once) {
                                            final MediaPlayer sendSound = MediaPlayer.create(getApplicationContext(), R.raw.plucky);
                                            sendSound.start();
                                        }
                                        once=true;
                                    }
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
        }catch (Exception e){

        }
    }

    private void sendMessage(){

        String  message = mChatBox.getText().toString();

        if(!TextUtils.isEmpty(message)){

            final MediaPlayer sendSound= MediaPlayer.create(this,R.raw.light);
            sendSound.start();
            Log.d("onChildAdded: ","You sent messages" );

            String current_user_ref="messages/"+mCurrentUserID+"/"+mChatUser;
            String chat_user_ref="messages/"+ mChatUser+"/"+mCurrentUserID;

            DatabaseReference user_message_push= mRootRef.child("message").child(mCurrentUserID).child(mChatUser).push();

            String push_id =user_message_push.getKey();

            Map sendTime=ServerValue.TIMESTAMP;

            Map messageMap= new HashMap();
            messageMap.put("message",message );
            messageMap.put("seen",false );
            messageMap.put("type","text" );
            messageMap.put("time",sendTime);
            messageMap.put("from",mCurrentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("CHAT_ERROR", databaseError.getMessage().toString());
                    }
                }
            });

            Map chatAddMap= new HashMap();
            chatAddMap.put("seen",false);
            chatAddMap.put("timestamp", sendTime);
            chatAddMap.put("last_message_id", push_id);

            Map chatUserMap = new HashMap();
            chatUserMap.put("chat/"+mCurrentUserID+"/"+mChatUser,chatAddMap);
            chatUserMap.put("chat/"+mChatUser+"/"+mCurrentUserID,chatAddMap);

            mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("CHAT_ERROR", databaseError.getMessage().toString());
                    }
                }
            });



        }

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        return date;
    }
}
