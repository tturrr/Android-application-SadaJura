package com.example.user.sadajura;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.util.HttpConnect;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import static java.lang.Math.floor;

public class Tmap2Activity extends AppCompatActivity implements View.OnClickListener,Dialog.OnCancelListener,TMapGpsManager.onLocationChangedCallback {


    LinearLayout linearLayout;
    private TMapView tMapView = null;
    private static String mAPIKEY = "acf3670f-d1e5-44ff-b77c-02c83c0f7280";
    private Context mContext = null;
    private static int mMarKerID;
    private TMapGpsManager tmapgps = null;
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();
    private ArrayList<MapPoint> m_mapPoint = new ArrayList<MapPoint>();
    private boolean m_bTrackingMode = true;
    double latitude;
    double longitude;
    String addressName,strlatitude,strlongitude;
    Button navi_btn,btnWhere;

    LinearLayout auto_search_linear;
    PlaceAutocompleteFragment fragment;
    /*Dialog에 관련된 필드*/
    LayoutInflater dialogLayoutInflater; //LayoutInflater
    View dialogLayout; //layout을 담을 View
    Dialog authDialog; //dialog 객체
    double startToPlaceLongitude,startToPlaceLatitude,endToPlaceLongitude,endToPlaceLatitude;
    TMapPoint startpoing,endpoint;

    @Override
    public void onLocationChange(Location location) {
        if(m_bTrackingMode){
            tMapView.setLocationPoint(location.getLongitude(),location.getLatitude());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmap2);


        Intent intent = getIntent();
        addressName = intent.getStringExtra("addressName");
        strlatitude = intent.getStringExtra("latitude");
        strlongitude = intent.getStringExtra("longitude");
        navi_btn = (Button)findViewById(R.id.navi_btn);
        btnWhere = (Button)findViewById(R.id.btnWhere);

        auto_search_linear = (LinearLayout)findViewById(R.id.auto_search_linear);

        latitude = Double.parseDouble(strlatitude);
        longitude = Double.parseDouble(strlongitude);
        mContext = this;

        linearLayout = (LinearLayout)findViewById(R.id.mapview);
        tMapView = new TMapView(this);
        linearLayout.addView(tMapView);
        tMapView.setSKTMapApiKey(mAPIKEY);
        showMarKerPoint(addressName,latitude,longitude);
        tMapView.setCenterPoint(longitude,latitude);

        tMapView.setSightVisible(true);
        tMapView.setIconVisibility(true);

        tmapgps = new TMapGpsManager(Tmap2Activity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);// 인터넷으로 위치를 받는다.

        tmapgps.OpenGps();

        btnWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tMapView.setTrackingMode(true); // 화면중심을 단말의 현재위치로 이동시키는 트래킹 모드 활성화
                tMapView.setSightVisible(true);
                tMapView.setIconVisibility(true);
            }
        });

        //검색 프래그먼트
        fragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) { // Handle the selected Place

                TMapData tMapData = new TMapData();
                tMapData.findTitlePOI(String.valueOf(place.getName()), new TMapData.FindTitlePOIListenerCallback() {
                    @Override
                    public void onFindTitlePOI(ArrayList<TMapPOIItem> poiItem) {

                        for(int i = 0; i < poiItem.size(); i++) {
                            TMapPOIItem item = poiItem.get(i);
                            item.getPOIPoint().getLongitude();
                            item.getPOIPoint().getLatitude();
                            addPoint(item.getPOIName().toString(),item.getPOIPoint().getLatitude(),item.getPOIPoint().getLongitude());
                            showMarKerPoint(item.getPOIName().toString(),item.getPOIPoint().getLatitude(),item.getPOIPoint().getLongitude());
                            moveMap(item.getPOIPoint().getLatitude(),item.getPOIPoint().getLongitude());
                        }

                    }
                });
            }
            @Override
            public void onError(Status status) { // Handle the error
            }
        });

        //풍선에서 우측버튼클릭시 할행동
        tMapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(final TMapMarkerItem tMapMarkerItem) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Tmap2Activity.this);
                builder.setTitle("출발지와 목적지를 정해주세요.");
                builder.setPositiveButton("출발지", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        View rootView = getWindow().getDecorView();
                       startToPlaceLongitude = tMapMarkerItem.getTMapPoint().getLongitude();
                       startToPlaceLatitude = tMapMarkerItem.getTMapPoint().getLatitude();
                     startpoing = tMapMarkerItem.getTMapPoint();
                        naviToSearch();
                    }
                }).setNegativeButton("목적지", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        endToPlaceLongitude = tMapMarkerItem.getTMapPoint().getLongitude();
                        endToPlaceLatitude = tMapMarkerItem.getTMapPoint().getLatitude();
                        endpoint = tMapMarkerItem.getTMapPoint();
                        naviToSearch();
                    }
                });builder.show();
            }
        });




        navi_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Tmap2Activity.this);
                dialog.setTitle("알림")
                        .setMessage("길을 찾으려는 곳을 선택해주세요.")
                        .setPositiveButton("현재위치", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder dialog1 = new AlertDialog.Builder(Tmap2Activity.this);
                                dialog1.setTitle("운송수단 선택")
                                        .setMessage("운송 수단을 선택해주세요.")
                                        .setPositiveButton("자동차", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                TMapPoint point = tmapgps.getLocation();

                                                navitocar(point.getLatitude(),point.getLongitude(),latitude,longitude);
                                            }
                                        }).setNegativeButton("뚜벅이", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        TMapPoint point1 = tmapgps.getLocation();
                                        navitopedestrian(point1.getLatitude(),point1.getLongitude(),latitude,longitude);
                                    }
                                }).setNeutralButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).create().show();

                            }
                        })
                        .setNegativeButton("출발지 정하기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                dialogLayoutInflater = LayoutInflater.from(mContext);
//                                dialogLayout = dialogLayoutInflater.inflate(R.layout.search_to_info, null); // LayoutInflater를 통해 XML에 정의된 Resource들을 View의 형태로 반환 시켜 줌
//                                authDialog = new Dialog(Tmap2Activity.this); //Dialog 객체 생성
//                                authDialog.setContentView(dialogLayout); //Dialog에 inflate한 View를 탑재 하여줌
//                                authDialog.show(); //Dialog를 나타내어 준다.
                                Toast.makeText(Tmap2Activity.this,"검색어를 입력해주세요",Toast.LENGTH_SHORT).show();
                                auto_search_linear.setVisibility(View.VISIBLE);
                            }
                        }).setNeutralButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();
            }
        });

    }

    public void showMarKerPoint(String addressName,double latitude , double longitude){ // 마커 빨강색 찍는곳.

        TMapPoint point = new TMapPoint(latitude,longitude);
        TMapMarkerItem item =  new TMapMarkerItem();
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_dot);

        item.setTMapPoint(point);
//            item.setName(m_mapPoint.get(i).getName());
        item.setCalloutTitle(addressName);
        Bitmap right = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_input_get)).getBitmap();
        item.setCalloutRightButtonImage(right);
        item.setCanShowCallout(true);
        item.setAutoCalloutVisible(true);
        item.setVisible(item.VISIBLE);
        item.setIcon(bitmap);

//            bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.poi_dot);
        //풍선뷰안에 글을 지정한다.
//            item.setCalloutTitle(m_mapPoint.get(i).getName());
//            item.setCalloutSubTitle("서울");
//            item.setCanShowCallout(true);
//            item.setAutoCalloutVisible(true);

//            Bitmap bitmap_1 = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.ic_launcher);

//            item.setCalloutRightButtonImage(bitmap_1);
        String strID = String.format("pmaker%d", mMarKerID++);

        tMapView.addMarkerItem(strID, item);
        mArrayMarkerID.add(strID);
    }

    public void navitocar(double startTolatitude,double startTolongitude, double endTolatitude, double endTolongitude){
        TMapPoint tMapPointStart = new TMapPoint(startTolatitude, startTolongitude); // (출발지)
        TMapPoint tMapPointEnd = new TMapPoint(endTolatitude,  endTolongitude); //(목적지)
        TMapData tmapdata = new TMapData();
        tmapdata.findPathDataWithType(TMapData.TMapPathType.CAR_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                tMapView.addTMapPath(polyLine);
            }
        });
    }
    public void navitopedestrian(double startTolatitude,double startTolongitude,double endtolatitude, double endtolongitude){
        TMapPoint tMapPointStart = new TMapPoint(startTolatitude, startTolongitude); // (출발지)
        TMapPoint tMapPointEnd = new TMapPoint(endtolatitude,  endtolongitude); //(목적지)
        TMapData tmapdata = new TMapData();
        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPointStart, tMapPointEnd, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                tMapView.addTMapPath(polyLine);
            }
        });

    }

    public void naviToSearch(){
        if(startToPlaceLatitude != 0 && startToPlaceLongitude != 0 && endToPlaceLongitude != 0 && endToPlaceLatitude != 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(Tmap2Activity.this);
            builder.setTitle("길찾기를 실행할까요?");
            builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    View rootView = getWindow().getDecorView();

                    navitocar(startToPlaceLatitude,startToPlaceLongitude,endToPlaceLatitude,endToPlaceLongitude);
                }
            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });builder.show();
        }
    }

    public void searchToAll(){

        PlaceAutocompleteFragment fragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) { // Handle the selected Place

                TMapData tMapData = new TMapData();
                m_mapPoint.clear();
                tMapData.findTitlePOI(String.valueOf(place.getName()), new TMapData.FindTitlePOIListenerCallback() {
                    @Override
                    public void onFindTitlePOI(ArrayList<TMapPOIItem> poiItem) {

                        for(int i = 0; i < poiItem.size(); i++) {
                            TMapPOIItem item = poiItem.get(i);
                            item.getPOIPoint().getLongitude();
                            item.getPOIPoint().getLatitude();
                            addPoint(item.getPOIName().toString(),item.getPOIPoint().getLatitude(),item.getPOIPoint().getLongitude());
                            showMarKerPoint(item.getPOIName().toString(),item.getPOIPoint().getLatitude(),item.getPOIPoint().getLongitude());
                            moveMap(item.getPOIPoint().getLatitude(),item.getPOIPoint().getLongitude());
                        }
                    }
                });
            }
            @Override
            public void onError(Status status) { // Handle the error
            }
        });
    }

    public void addPoint(String markerName,double latitude , double longitude){ //여기에 핀을 꼽을 포인트들을 add한다.
        m_mapPoint.add(new MapPoint(markerName,latitude,longitude));
    }

    private void moveMap(double lat, double lng) {
        tMapView.setCenterPoint(lng, lat);
    }


    @Override
    public void onCancel(DialogInterface dialogInterface) {

    }

    @Override
    public void onClick(View view) {

    }




}
