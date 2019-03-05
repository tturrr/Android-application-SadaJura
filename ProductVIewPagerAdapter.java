package com.example.user.sadajura;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ProductVIewPagerAdapter extends PagerAdapter {

    LayoutInflater inflater;
    ArrayList dataList;

    public ProductVIewPagerAdapter(LayoutInflater inflater, ArrayList<ViewPagerDataForm> dataList) {
        this.inflater = inflater;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }



    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        //새로운 View 객체를 Layoutinflater를 이용해서 생성
        View view= inflater.inflate(R.layout.viewpager_item, null);
        // 메인에서찾는거와다르게 위에서 만들었던 View를 이용하여서 find 를 하는것을 주의하세요 :)
        ImageView img= (ImageView)view.findViewById(R.id.img_viewpager_childimage);
        //ImageView에 현재 position 번째에 해당하는 이미지를 보여주기 위한 작업
        Glide.with(view)
                .load(dataList.get(position))
                .into(img);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG", "This page was clicked: " + position);

                Intent intent1 = new Intent();
            }
        });

        //ViewPager에 만들어 낸 View 추가
        container.addView(view);
        //Image가 세팅된 View를 리턴


        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
        //ViewPager에서 보이지 않는 View는 제거
        //세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
        container.removeView((View)object);
    }
    @Override
    public boolean isViewFromObject(View v, Object obj) {
        // TODO Auto-generated method stub
        return v==((View)obj);
    }
}
