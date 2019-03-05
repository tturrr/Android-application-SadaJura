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

public class ShoppingBasketRcvAdapter extends RecyclerView.Adapter<ShoppingBasketRcvAdapter.ViewHolder>  {
    private Activity activity;
    private ArrayList<ShoppingBasketDataForm> dataList;

    public ShoppingBasketRcvAdapter(Activity activity, ArrayList<ShoppingBasketDataForm> dataList) {
        this.activity = activity;
        this.dataList = dataList;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_icon_iv;
        TextView item_title_tv,item_time_tv,item_price_tv;


        public ViewHolder(final View itemView) {
            super(itemView);
            item_icon_iv = (ImageView) itemView.findViewById(R.id.item_icon_iv);
            item_title_tv = (TextView) itemView.findViewById(R.id.item_title_tv);
            item_time_tv = (TextView)itemView.findViewById(R.id.item_time_tv);
            item_price_tv = (TextView)itemView.findViewById(R.id.item_price_tv);

        }
    }

    public ShoppingBasketRcvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_baskert_item, parent, false);
        ShoppingBasketRcvAdapter.ViewHolder viewHolder = new ShoppingBasketRcvAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ShoppingBasketRcvAdapter.ViewHolder holder, final int position) {
        ShoppingBasketDataForm tmpData = dataList.get(position);

        Glide.with(activity)
                .load(tmpData.getProduct_image_path())
                .into(holder.item_icon_iv);
        holder.item_title_tv.setText(tmpData.getProduct_title());
        holder.item_time_tv.setText(tmpData.getProduct_time());
        holder.item_price_tv.setText(tmpData.getProduct_price());

    }

    private void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, dataList.size()); // 지워진 만큼 다시 채워넣기.
    }
}