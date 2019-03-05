package com.example.user.sadajura;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fasterxml.jackson.databind.type.MapType;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapMarkerItem2;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.util.HttpConnect;
import com.zfdang.multiple_images_selector.SelectorSettings;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class TmapActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    private Activity activity;
    private int searchRequestCode = 78;
    private int cancelResultCode = 82;
    private int successResultCode = 81;

    private Context mContext = null;
    private boolean m_bTrackingMode = true;

    private TMapGpsManager tmapgps = null;
    private TMapView tMapView = null;
    private static String mAPIKEY = "acf3670f-d1e5-44ff-b77c-02c83c0f7280";
    private static int mMarKerID;

    private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();
    private ArrayList<MapPoint> m_mapPoint = new ArrayList<MapPoint>();

    private String locationname;

    private Button btnWhere;
    @Override
    public void onLocationChange(Location location) {
        if(m_bTrackingMode){
            tMapView.setLocationPoint(location.getLongitude(),location.getLatitude());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmap);
        mContext = this;

        btnWhere = (Button)findViewById(R.id.btnWhere);
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.mapview);
        tMapView = new TMapView(this);
        linearLayout.addView(tMapView);
        tMapView.setSKTMapApiKey(mAPIKEY);

        showMarKerPoint();

        //현위치 아이콘
        tMapView.setIconVisibility(true);
        //줌 레벨
        tMapView.setZoomLevel(15);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapgps = new TMapGpsManager(TmapActivity.this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);// 인터넷으로 위치를 받는다.

        tmapgps.OpenGps();
        //화면 중심을 단말기로.
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);


        btnWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tMapView.setTrackingMode(true); // 화면중심을 단말의 현재위치로 이동시키는 트래킹 모드 활성화
                tMapView.setSightVisible(true);
                tMapView.setIconVisibility(true);
            }
        });

       tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
           @Override
           public void onLongPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, final TMapPoint tMapPoint) {
               AlertDialog.Builder alert = new AlertDialog.Builder(TmapActivity.this);
               alert.setTitle("장소 정보")
                       .setMessage("판매할 장소의 이름을 정해주세요.");
               final EditText locationname1 = new EditText(TmapActivity.this);
               alert.setView(locationname1);
               alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                   locationname = locationname1.getText().toString();
                       double m_latitude =  tMapPoint.getLatitude();
                       double m_longitude = tMapPoint.getLongitude();
                       addPoint(locationname,m_latitude,m_longitude);
                       showMarKerPoint();
                   }
               });
               alert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {

                   }
               });alert.show();


           }
       });



        //풍선에서 우측버튼클릭시 할행동
        tMapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(final TMapMarkerItem tMapMarkerItem) {

                AlertDialog.Builder builder = new AlertDialog.Builder(TmapActivity.this);
        builder.setTitle("판매장소로 지정하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                View rootView = getWindow().getDecorView();
                File screenShot = ScreenShot(rootView);
                Uri file_path = getImageContentUri(TmapActivity.this,screenShot);
                String screenShot1 = String.valueOf(file_path);

                Intent intent = new Intent();
                intent.putExtra("addressName",   tMapMarkerItem.getCalloutTitle());
                intent.putExtra("latitude", tMapMarkerItem.getTMapPoint().getLatitude());
                intent.putExtra("longitude",tMapMarkerItem.getTMapPoint().getLongitude());
                intent.putExtra("screenShot",screenShot1);
                setResult(successResultCode,intent);
                finish();
                }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });builder.show();
        }
        });


        //검색 프래그먼트
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
                            showMarKerPoint();
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

    public void showMarKerPoint(){ // 마커 빨강색 찍는곳.
        tMapView.removeAllMarkerItem();
        for(int i =0; i<m_mapPoint.size(); i++){
            TMapPoint point = new TMapPoint(m_mapPoint.get(i).getLatitude(),m_mapPoint.get(i).getLongitude());
            TMapMarkerItem item =  new TMapMarkerItem();
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_dot);

            item.setTMapPoint(point);
//            item.setName(m_mapPoint.get(i).getName());
            item.setCalloutTitle(m_mapPoint.get(i).getName());
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
    }

    //주소검색으로 위도경도 찾아와서 그주소를 마커로 찍는다.
    public void convertToAddress(){
//        //다이얼로그 띄어서 , 검색창에 입력받는다.
//        AlertDialog.Builder builder = new AlertDialog.Builder(TmapActivity.this);
//        builder.setTitle("통합 검색");
//
//        final EditText input = new EditText(TmapActivity.this);
//        builder.setView(input);

//        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                final String strData = input.getText().toString();
//                TMapData tMapData = new TMapData();
//
//                tMapData.findAllPOI(strData, new TMapData.FindAllPOIListenerCallback() {
//                    @Override
//                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
//                     for(int i =0; i < poiItem.size(); i++){
//                         TMapPOIItem item = poiItem.get(i);
//                         String name = item.getPOIName().toString();
//                         double latitude = item.getPOIPoint().getLatitude();
//                         double longitude = item.getPOIPoint().getLongitude();
//
//                         addPoint(name,latitude,longitude);
//                         showMarKerPoint();
//                     }
//                    }
//                });
//            }
//        });builder.show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == searchRequestCode && resultCode == cancelResultCode) {

            }
            else if(requestCode == searchRequestCode){

        }
    }
    private void moveMap(double lat, double lng) {
        tMapView.setCenterPoint(lng, lat);
    }

    public Activity getActivity() {
        return activity;
    }

    //화면 캡쳐하기
    public File ScreenShot(View view){
        view.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다

        Bitmap screenBitmap = view.getDrawingCache();   //캐시를 비트맵으로 변환

        String filename = "screenshot.png";
        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures", filename);  //Pictures폴더 screenshot.png 파일
        FileOutputStream os = null;
        try{
            os = new FileOutputStream(file);
            screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   //비트맵을 PNG파일로 변환
            os.close();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        view.setDrawingCacheEnabled(false);

        return file;
    }

    //파일경로를 비트맵으로 변환
    private Bitmap DecodeBitmapFile(String strFilePath) {
        final int IMAGE_MAX_SIZE = 1024;
        File file = new File(strFilePath);
        if (file.exists() == false) {
            return null;
        }
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(strFilePath, bfo);
        if (bfo.outHeight * bfo.outWidth >= IMAGE_MAX_SIZE * IMAGE_MAX_SIZE) {
            bfo.inSampleSize = (int) Math.pow(2,
                    (int) Math.round(Math.log(IMAGE_MAX_SIZE
                            / (double) Math.max(bfo.outHeight, bfo.outWidth))
                            / Math.log(0.5)));
        }
        bfo.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(strFilePath, bfo);
        return bitmap;
    }

    //파일에서 파일 절대경로얻기
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


}
