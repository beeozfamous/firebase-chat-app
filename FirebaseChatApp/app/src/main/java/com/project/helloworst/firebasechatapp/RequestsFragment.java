package com.project.helloworst.firebasechatapp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private View mMainView;
    private DatabaseReference mRootRef;
    private DatabaseReference mRequireData;
    private DatabaseReference mUserData;
    private String mCurrentUserID;
    private FirebaseRecyclerAdapter<Requests,RequestViewHolder> mAdapter;
    private FirebaseRecyclerOptions<Requests> mOptions;
    String current_date;
    DatabaseReference mFriendListData;
    DatabaseReference mFriendRequestsData;



    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);
        mRecyclerView=mMainView.findViewById(R.id.request_fragment_recycle_view);
        mRecyclerView.setHasFixedSize(false);
        mCurrentUserID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        mRootRef= FirebaseDatabase.getInstance().getReference();
        mRequireData=mRootRef.child("friend_requests").child(mCurrentUserID);
        current_date= DateFormat.getDateTimeInstance().format(new Date());
        mFriendListData=mRootRef.child("friends_data");
        mFriendRequestsData = mRootRef.child("friend_requests");

        mOptions = new FirebaseRecyclerOptions.Builder<Requests>().setQuery(mRequireData,Requests.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Requests,RequestViewHolder>(mOptions){

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_tab,parent,false);
                return new RequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Requests model) {
                String request_user_id=getRef(position).getKey();
                mRequireData.child(request_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("request_type").getValue()!=null) {
                            if (dataSnapshot.child("request_type").getValue().toString().equals("received")) {
                                holder.requestLayout.setVisibility(View.VISIBLE);
                                mRootRef.child("users").child(request_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshots) {

                                        String requirerName = dataSnapshots.child("name").getValue().toString();
                                        String requirerImage = dataSnapshots.child("image").getValue().toString();

                                        holder.setName(requirerName);
                                        try{holder.setImage(requirerImage, getContext());}catch (Exception e){
                                            Log.d("Request Image", "onDataChange: " + e.getMessage());
                                        }

                                        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mFriendListData.child(mCurrentUserID).child(request_user_id).child("date").setValue(current_date)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                mFriendListData.child(request_user_id).child(mCurrentUserID).child("date").setValue(current_date)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                mFriendRequestsData.child(request_user_id).child(mCurrentUserID).removeValue()
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                mFriendRequestsData.child(mCurrentUserID).child(request_user_id).removeValue()
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void aVoid) {

                                                                                                                holder.acceptButton.setEnabled(false);
                                                                                                                holder.acceptButton.setVisibility(View.INVISIBLE);
                                                                                                                holder.declineButton.setEnabled(false);
                                                                                                                holder.declineButton.setVisibility(View.INVISIBLE);
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                            }
                                                        });

                                            }
                                        });
                                        holder.declineButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mFriendRequestsData.child(mCurrentUserID).child(request_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mFriendRequestsData.child(request_user_id).child(mCurrentUserID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                holder.acceptButton.setEnabled(false);
                                                                holder.acceptButton.setVisibility(View.INVISIBLE);
                                                                holder.declineButton.setEnabled(false);
                                                                holder.declineButton.setVisibility(View.INVISIBLE);

                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                holder.requestLayout.setVisibility(View.GONE);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };

        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);
        return mMainView;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView imageView;
        TextView nameView;
        Button acceptButton;
        Button declineButton;
        RelativeLayout requestLayout;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            requestLayout=itemView.findViewById(R.id.friend_request_container);
            acceptButton=itemView.findViewById(R.id.request_accept_button);
            declineButton=itemView.findViewById(R.id.request_decline_button);

        }

        public void setName(String name){
            nameView=mView.findViewById(R.id.request_tab_name);
            nameView.setText(name);
        }

        public void setImage(String image, Context appContext){
            imageView=mView.findViewById(R.id.request_tab_image);
            GlideApp.with(appContext).load(image).apply(RequestOptions.centerCropTransform()).into(imageView);
        }


    }
}
