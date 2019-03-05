package com.example.user.sadajura;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ChatRcvAdapter  extends RecyclerView.Adapter<ChatRcvAdapter.ViewHolder>  {
    private Activity activity;
    private ArrayList<ChatDataForm> dataList;

    public ChatRcvAdapter(Activity activity, ArrayList<ChatDataForm> dataList) {
        this.activity = activity;
        this.dataList = dataList;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView target_iv;
        TextView chat_message_tv,chat_time_tv,chat_read_message_tv;


        public ViewHolder(final View itemView) {
            super(itemView);
            target_iv = (ImageView) itemView.findViewById(R.id.chat_image_iv);
            chat_message_tv = (TextView) itemView.findViewById(R.id.chat_message_tv);
            chat_time_tv = (TextView)itemView.findViewById(R.id.chat_time_tv);
            chat_read_message_tv = (TextView)itemView.findViewById(R.id.chat_read_message_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(activity, "click " + dataList.get(getAdapterPosition()).getRoomNo(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity,Client.class);
                    intent.putExtra("ProductNo",dataList.get(getAdapterPosition()).getRoomNo());
                    intent.putExtra("nickName",dataList.get(getAdapterPosition()).getNickName());
                    intent.putExtra("recive_id",dataList.get(getAdapterPosition()).getRecive_id());
                    view.getContext().startActivity(intent);
                    activity.finish();
                }
            });

        }
    }

    public ChatRcvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
        ChatRcvAdapter.ViewHolder viewHolder = new ChatRcvAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ChatRcvAdapter.ViewHolder holder, final int position) {
        ChatDataForm tmpData = dataList.get(position);

        Glide.with(activity)
                .load(tmpData.getProduct_img())
                .into(holder.target_iv);
        holder.chat_message_tv.setText(tmpData.getChat_message());
        holder.chat_time_tv.setText(tmpData.getTime());

    }

    private void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, dataList.size()); // 지워진 만큼 다시 채워넣기.
    }
}