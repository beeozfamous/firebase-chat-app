package com.project.helloworst.firebasechatapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;
    private String mFriendImageURI;

    public MessageAdapter(List<Messages> messagesList,String friendImageURI){

        this.mMessagesList=messagesList;
        this.mFriendImageURI=friendImageURI;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_tab,parent,false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        mAuth=FirebaseAuth.getInstance();
        String mCurrentUserID=mAuth.getCurrentUser().getUid();
        Messages c= mMessagesList.get(position);
        String mFromUserID=c.getFrom();

        if(mFromUserID.equals(mCurrentUserID)){
            holder.messageImage.setVisibility(View.INVISIBLE);
            holder.messageTextRecieve.setVisibility(View.INVISIBLE);
            holder.messageTextSend.setVisibility(View.VISIBLE);
            holder.messageTextSend.setText(c.getMessage());
        }
        else{
            holder.messageTextSend.setVisibility(View.INVISIBLE);
            holder.messageTextRecieve.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.VISIBLE);
            GlideApp.with(holder.messageImage).load(mFriendImageURI).apply(RequestOptions.circleCropTransform()).into(holder.messageImage);
            holder.messageTextRecieve.setText(c.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageTextRecieve;
        public TextView messageTextSend;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageTextRecieve= itemView.findViewById(R.id.message_text_recieve);
            messageTextSend= itemView.findViewById(R.id.message_text_send);
            messageImage=itemView.findViewById(R.id.message_avatae);
        }
    }


}
