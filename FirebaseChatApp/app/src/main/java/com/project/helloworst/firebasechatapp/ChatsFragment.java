package com.project.helloworst.firebasechatapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private View mView;
    private String mCurrentUserID;
    private DatabaseReference mRootRef;
    private DatabaseReference mChatData;
    private FirebaseRecyclerOptions <ChatConversation> mOptions;
    private FirebaseRecyclerAdapter<ChatConversation,ChatConversationViewHolder> mAdapter;
    private DatabaseReference mChatFriendData;

    public ChatsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.fragment_chats, container, false);
        mRecyclerView=mView.findViewById(R.id.chat_fragment_layout);
        mRecyclerView.setHasFixedSize(false);
        mCurrentUserID= FirebaseAuth.getInstance().getUid();
        mRootRef= FirebaseDatabase.getInstance().getReference();
        mChatData=mRootRef.child("chat").child(mCurrentUserID);

        mOptions= new FirebaseRecyclerOptions.Builder<ChatConversation>().setQuery(mChatData,ChatConversation.class).build();

        mAdapter= new FirebaseRecyclerAdapter<ChatConversation, ChatConversationViewHolder>(mOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ChatConversationViewHolder holder, int position, @NonNull ChatConversation model) {
                String chat_friend_id=getRef(position).getKey();
                DatabaseReference mRootChat=mRootRef.child("chat");
                mRootChat.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(mCurrentUserID).child(chat_friend_id).child("last_message_id").getValue()!=null) {
                            String mConversationID = dataSnapshot.child(mCurrentUserID).child(chat_friend_id).child("last_message_id").getValue().toString();
                            holder.chatLayout.setVisibility(View.VISIBLE);
                            String seenTheir = dataSnapshot.child(mCurrentUserID).child(chat_friend_id).child("seen").getValue().toString();
                            String seenMine = dataSnapshot.child(chat_friend_id).child(mCurrentUserID).child("seen").getValue().toString();
                            DatabaseReference mOursMessage = mRootRef.child("messages").child(mCurrentUserID).child(chat_friend_id).child(mConversationID);
                            mOursMessage.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String fromID = dataSnapshot.child("from").getValue(String.class);
                                    String last_message = dataSnapshot.child("message").getValue(String.class);
                                    Long message_time = dataSnapshot.child("time").getValue(Long.class);

                                    mChatFriendData = mRootRef.child("users").child(chat_friend_id);
                                    mChatFriendData.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            String chat_friend_image_uri = dataSnapshot.child("image").getValue(String.class);
                                            String chat_friend_name = dataSnapshot.child("name").getValue(String.class);
                                            if (dataSnapshot.hasChild("online")) {
                                                String chat_friend_isOnline = dataSnapshot.child("online").getValue().toString();
                                                holder.setOnline(chat_friend_isOnline);
                                            }

                                            if (seenMine != null
                                                    && seenTheir != null
                                                    && fromID != null
                                                    && last_message != null
                                                    && message_time != null
                                                    && chat_friend_image_uri != null
                                                    && chat_friend_name != null) {
                                                try {
                                                    holder.setImage(chat_friend_image_uri, getContext());
                                                }catch (Exception e){
                                                    Log.d("ChatFragment", e.getMessage());
                                                }
                                                holder.setName(chat_friend_name);
                                                holder.setTime(getDate(message_time));

                                                if (fromID.equals(mCurrentUserID)) {
                                                    if (seenTheir.equals("true")) {

                                                        holder.setConversation("You: " + last_message, "twice");
                                                        holder.setSeen("once",chat_friend_image_uri, getContext());
                                                    } else {
                                                        holder.setConversation("You: " + last_message, "twice");
                                                        holder.setSeen("twice",chat_friend_image_uri, getContext());
                                                    }

                                                } else if (fromID.equals(chat_friend_id)) {
                                                    if (seenMine.equals("true")) {

                                                        holder.setConversation(chat_friend_name + ": " + last_message, "twice");
                                                        holder.setSeen("no",chat_friend_image_uri, getContext());
                                                    } else {
                                                        holder.setConversation(chat_friend_name + ": " + last_message, "once");
                                                        holder.setSeen("no",chat_friend_image_uri, getContext());
                                                    }
                                                }
                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                        chatIntent.putExtra("UserID", chat_friend_id);
                                                        chatIntent.putExtra("UserName", chat_friend_name);
                                                        chatIntent.putExtra("ImageLink", chat_friend_image_uri);
                                                        startActivity(chatIntent);
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d("mChatFriendData: ", databaseError.getMessage());
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d("mOursMessage: ", databaseError.getMessage());
                                }
                            });
                        } else {
                            holder.chatLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("mRootChat: ",databaseError.getMessage() );
                    }
                });


            }

            @NonNull
            @Override
            public ChatConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_converation_tab,parent,false);
                return new ChatConversationViewHolder(view);
            }
        };


        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);
        return mView;
    }

    public static class ChatConversationViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout chatLayout;

        public ChatConversationViewHolder(View itemView) {
            super(itemView);
            chatLayout=itemView.findViewById(R.id.friend_chat_container);
        }

        public void setImage(String image, Context appContext){
            ImageView imageView=itemView.findViewById(R.id.consversation_tab_image);
            GlideApp.with(appContext).load(image).apply(RequestOptions.circleCropTransform()).into(imageView);
        }

        public void setName(String name){
            TextView nameView=itemView.findViewById(R.id.conversation_tab_name);
            nameView.setText(name);
        }

        public void setConversation(String conversation,String type){
            TextView conversationView=itemView.findViewById(R.id.conversation_tab_consersation);
            conversationView.setText(conversation);
            if(type.equals("once")){
                conversationView.setTypeface(conversationView.getTypeface(),Typeface.BOLD);
                conversationView.setTextColor(Color.BLACK);
            } else if(type.equals("twice")){
                conversationView.setTypeface(conversationView.getTypeface(),Typeface.NORMAL);
                conversationView.setTextColor(Color.GRAY);
            }
        }

        public void setTime(String time){
            TextView timeView=itemView.findViewById(R.id.conversation_tab_time);
            timeView.setText(time);
        }

        public void setOnline(String online){
            ImageView userOnline= itemView.findViewById(R.id.conversation_online_dot);
            if(online.equals("true")){
                userOnline.setVisibility(View.VISIBLE);
            }else{
                userOnline.setVisibility(View.INVISIBLE);
            }
        }

        public void setSeen(String type,String image, Context appContext){
            ImageView seenSquare=itemView.findViewById(R.id.conversation_seen_dot);
            if(type.equals("once")){
                try{
                GlideApp.with(appContext).load(image).apply(RequestOptions.circleCropTransform()).into(seenSquare);}catch (Exception e){
                    Log.e("ChatFragment", "setSeen: ",e );
                }
            } else if(type.equals("twice")){
                seenSquare.setImageResource(R.drawable.ic_circle_seen_5);
            } else{
                seenSquare.setImageResource(R.drawable.ic_square_seen_3);
            }
        }
    }
    public String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM HH:mm", cal).toString();
        return date;
    }
}
