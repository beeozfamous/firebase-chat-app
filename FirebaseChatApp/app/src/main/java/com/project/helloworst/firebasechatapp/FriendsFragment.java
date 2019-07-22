package com.project.helloworst.firebasechatapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private View mMainView;
    private String mCurrentUserID;
    private DatabaseReference mFriendDatabase;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerOptions<Friends>mOptions;
    private FirebaseRecyclerAdapter<Friends,FriendsViewHolder> mAdapter;
    private DatabaseReference mUserDatabase;



    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mRecyclerView = mMainView.findViewById(R.id.friend_fragment_recycle_view);
        mRecyclerView.setHasFixedSize(false);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mCurrentUserID = mAuth.getCurrentUser().getUid();
            mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends_data").child(mCurrentUserID);
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");


            mOptions = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mFriendDatabase, Friends.class).build();

            mAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(mOptions) {


                @Override
                protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Friends model) {
                    holder.setDate(model.getDate());
                    String list_userID = getRef(position).getKey();
                    mUserDatabase.child(list_userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String userName = dataSnapshot.child("name").getValue().toString();
                            String imageLink = dataSnapshot.child("image").getValue().toString();
                            if (dataSnapshot.hasChild("online")) {
                                String isOnline = dataSnapshot.child("online").getValue().toString();
                                holder.setOnline(isOnline);
                            }
                            try {
                                holder.setName(userName);
                                holder.setImage(imageLink, getContext());
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CharSequence option[] = new CharSequence[]{"Open Profile", "Send Message"};

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                        builder.setTitle("Select options");
                                        builder.setItems(option, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                if (which == 0) {
                                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                    profileIntent.putExtra("UserID", list_userID);
                                                    startActivity(profileIntent);
                                                }
                                                if (which == 1) {
                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("UserID", list_userID);
                                                    chatIntent.putExtra("UserName", userName);
                                                    chatIntent.putExtra("ImageLink", imageLink);
                                                    startActivity(chatIntent);
                                                }

                                            }
                                        });
                                        builder.show();
                                    }
                                });
                            }catch (Exception e){
                                Log.d("onDataChange: ", e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });
                }

                @NonNull
                @Override
                public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_tab_layout, parent, false);
//                FriendsViewHolder viewHolder= new FriendsViewHolder(view);
//                viewHolder.setOnClickListener(new FriendsViewHolder.ClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//
//                    }
//                    @Override
//                    public void onItemLongClick(View view, int position) {
//                        Toast.makeText(getActivity(), "Item long clicked at " + position, Toast.LENGTH_SHORT).show();
//                    }
//                });
//                return viewHolder;
                    return new FriendsViewHolder(view);
                }

            };

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(layoutManager);
            mAdapter.startListening();
            mRecyclerView.setAdapter(mAdapter);
        }
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(mAdapter!=null){
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        if(mAdapter!=null){
            mAdapter.stopListening();
        }
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter!=null){
            mAdapter.startListening();
        }
    }
    public static class FriendsViewHolder  extends RecyclerView.ViewHolder{

        View mView;
//        private FriendsViewHolder.ClickListener mClickListener;
//
//        public interface ClickListener{
//            public void onItemClick(View view, int position);
//            public void onItemLongClick(View view, int position);
//        }
//
//        public void setOnClickListener(FriendsViewHolder.ClickListener clickListener){
//            mClickListener = clickListener;
//        }

        FriendsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
//            mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mClickListener.onItemClick(v, getAdapterPosition());
//
//                }
//
//            });
//            mView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    mClickListener.onItemLongClick(v, getAdapterPosition());
//                    return true;
//                }
//            });
        }


        public void setDate(String date) {
            TextView View= mView.findViewById(R.id.user_tab_status);
            View.setText("Being friend since : "+date);
        }
        public void setName(String name){
            TextView nameView=mView.findViewById(R.id.user_tab_name);
            nameView.setText(name);
        }
        public void setImage(String image,Context appContext){
            ImageView imageView=mView.findViewById(R.id.user_tab_image);
            GlideApp.with(appContext).load(image).apply(RequestOptions.circleCropTransform()).into(imageView);
        }
        public void setOnline(String online){
            ImageView userOnline= mView.findViewById(R.id.friends_online_dot);
            if(online.equals("true")){
                userOnline.setVisibility(View.VISIBLE);
            }else{
                userOnline.setVisibility(View.INVISIBLE);
            }
        }

    }
}
