package com.project.helloworst.firebasechatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class UsersActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private TextView tool_bar_title;
    private DatabaseReference mUserDatabase;
    FirebaseRecyclerOptions<UserItem> mOptions;
    FirebaseRecyclerAdapter<UserItem,UserViewHolder> mAdapter;
    private ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUsersList=findViewById(R.id.user_list_recycle_view);
        mUsersList.setHasFixedSize(true);

        tool_bar_title=findViewById(R.id.tool_bar_title);
        tool_bar_title.setText("all users");

        mToolbar=findViewById(R.id.users_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog= new ProgressDialog(UsersActivity.this);
        mProgressDialog.setTitle("Loading All User Data");
        mProgressDialog.setMessage("Please wait while we download all user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mOptions= new FirebaseRecyclerOptions.Builder<UserItem>().setQuery(mUserDatabase,UserItem.class).build();

        mAdapter= new FirebaseRecyclerAdapter<UserItem, UserViewHolder>(mOptions) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserItem users) {
                holder.setName(users.getName());
                holder.setStatus(users.getStatus());
                holder.setImage(users.getImage(),getApplicationContext());

                String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent= new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("UserID",user_id);
                        startActivity(profileIntent);
                    }
                });
                mProgressDialog.dismiss();
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_tab_layout,parent,false);
                return new UserViewHolder(view);
            }

        };




        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mUsersList.setLayoutManager(layoutManager);
        mAdapter.startListening();
        mUsersList.setAdapter(mAdapter);





    }
    @Override
    protected void onStart() {
       super.onStart();
       if(mAdapter!=null){
           mAdapter.startListening();
       }
    }

    @Override
    protected void onStop() {
        if(mAdapter!=null){
            mAdapter.stopListening();
        }
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAdapter!=null){
            mAdapter.startListening();
        }
    }

    public static class UserViewHolder  extends RecyclerView.ViewHolder{

        View mView;

        UserViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }


        public void setName(String name) {
            TextView mUserName;
            mUserName=mView.findViewById(R.id.user_tab_name);
            mUserName.setText(name);
        }

        void setStatus(String status) {
            TextView mUserStatus;
            mUserStatus=mView.findViewById(R.id.user_tab_status);
            mUserStatus.setText(status);
        }

        void setImage(String image, Context appContext) {
            ImageView mUserImage;
            mUserImage=mView.findViewById(R.id.user_tab_image);
            GlideApp.with(appContext).load(image).apply(RequestOptions.circleCropTransform()).into(mUserImage);
        }
    }


}
