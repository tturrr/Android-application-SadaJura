package com.example.user.sadajura;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class WriteRcvAdapter extends RecyclerView.Adapter<WriteRcvAdapter.ViewHolder>  {
    private Activity activity;
    private ArrayList<WirteDataForm> dataList;

    public WriteRcvAdapter(Activity activity, ArrayList<WirteDataForm> dataList) {
        this.activity = activity;
        this.dataList = dataList;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView target_iv;


        public ViewHolder(final View itemView) {
            super(itemView);
            target_iv = (ImageView) itemView.findViewById(R.id.item_iv_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity,Client.class);

                    view.getContext().startActivity(intent);
                    activity.finish();
                }
            });

        }
    }

    public WriteRcvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.write_img_item, parent, false);
        WriteRcvAdapter.ViewHolder viewHolder = new WriteRcvAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final WriteRcvAdapter.ViewHolder holder, final int position) {
        WirteDataForm tmpData = dataList.get(position);

        Glide.with(activity)
                .load(tmpData.getProduct_image_path())
                .into(holder.target_iv);

    }

    private void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, dataList.size()); // 지워진 만큼 다시 채워넣기.
    }
}