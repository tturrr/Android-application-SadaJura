package com.example.user.sadajura;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PreCameraActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 1; //앨범에서 사진 가져오기
    private static final int CROP_FROM_IMAGE = 2; //가져온 사진을 자르기 위한 변수
    private String mCurrentPhotoPath;   //사진파일 현재 경로

    String img_name ; //파일이 저장될 이름. 이름.png
    String cropImageDiretory;//크롭된 사진이 저장될 디렉토리

    private Uri photoUri;//촬영한, 크롭된 이미지 경로를 담는 변수
    ImageView imageView;
    CameraSurfaceView cameraView;
    FrameLayout previewFrame;
    boolean crop;   //크롭사진이 생성 되었는지 여부
    int usingCamera;//전면, 후면 중 어떤 카메라를 쓰고있는가 여부. Camera.CameraInfo.CAMERA_FACING_BACK, CAMERA_FACING_FRONT
    Button crop_suc_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_camera);


        init();
    }

    private void init(){
        cropImageDiretory = "/Redwings/";//크롭된 사진이 저장될 디렉토리
        imageView = (ImageView)findViewById(R.id.cropimg);//크롭된 사진을 넣을 이미지 뷰
        crop = false;
        usingCamera = Camera.CameraInfo.CAMERA_FACING_BACK;//후면 카메라가 기본.

        initCamera();
    }

    //카메라 프리뷰 초기화
    private void initCamera(){
        cameraView = new CameraSurfaceView(getApplicationContext(), usingCamera);//카메라 프리뷰가 나올 서페이스뷰
        previewFrame = (FrameLayout) findViewById(R.id.previewFrame);   //프레임 뷰에 카메라뷰를 추가
        previewFrame.addView(cameraView);
    }

    //앞뒤 카메라 전환 이벤트
    public void onClickSwapCamera(View v){
        //현재 카메라 프리뷰 제거.
        previewFrame.removeView(cameraView);//여기서 surfaceDestroyed 실행됨
        //카메라 방향 토글
        usingCamera = (usingCamera == Camera.CameraInfo.CAMERA_FACING_BACK?Camera.CameraInfo.CAMERA_FACING_FRONT:Camera.CameraInfo.CAMERA_FACING_BACK);
        //카메라 세팅
        initCamera();
    }

    public void onClickMoveActivity(View v){
        String photoString = photoUri.toString();
        Intent intent = new Intent(PreCameraActivity.this,SignUpActivity.class);
        intent.putExtra("photoString",photoString);
        startActivity(intent);
        finish();
    }

    //촬영과 앨범 선택 두 버튼의 리스너
    public void onClickTakeImage(View v){
        switch (v.getId()) {
            case R.id.btncamera:
                takePhoto();//카메라면 사진 찍기
                break;
            case R.id.btnalbum:
                goToAlbum();//앨범이면 앨범에서 사진 가져오기
                break;
        }
    }

    //현재 보여지는 미리보기 화면을 촬영
    private void takePhoto(){
        cameraView.capture(new Camera.PictureCallback() {   //캡쳐 이벤트의 콜백함수
            public void onPictureTaken(byte[] data, Camera camera) {//사진 데이터와 카메라 객체
                try {
                    Bitmap bitmaporigin = BitmapFactory.decodeByteArray(data, 0, data.length);//원본 비트맵 파일. 왠지 90도 돌아가 있다

                    //왠지 90도 돌아가서 찍힘. 되돌려놓기. 전면 카메라의 경우 좌우반전(진짜 왤까)
                    Matrix matrix = new Matrix();
                    if (usingCamera == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        float[] mirrorY = {
                                -1, 0, 0,
                                0, 1, 0,
                                0, 0, 1
                        };
                        matrix.setValues(mirrorY);
                    }
                    matrix.postRotate(90);

                    Bitmap bitmap = Bitmap.createBitmap(bitmaporigin, 0, 0,
                            bitmaporigin.getWidth(), bitmaporigin.getHeight(), matrix, true);

                    //사진 원본 파일. 갤러리에서 보이며 /내장메모리/Pictures 에 저장.
                    img_name = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());   //이미지의 이름을 설정
                    String outUriStr = MediaStore.Images.Media.insertImage(getContentResolver(),//이미지 파일 생성
                            bitmap, img_name, "Captured Image using Camera.");

                    if (outUriStr == null) {
                        Log.d("SampleCapture", "Image insert failed.");
                        return;
                    } else {
                        photoUri = Uri.parse(outUriStr);//찍은 사진 경로를 photoUri에 저장
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, photoUri));
                    }

                    Toast.makeText(getApplicationContext(), "찍은 사진을 앨범에 저장했습니다.", Toast.LENGTH_LONG).show();

                    cropImage();//이미지 크롭
                    MediaScannerConnection.scanFile(getApplicationContext(),//앨범에 사진을 보여주기 위해 스캔
                            new String[]{photoUri.getPath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                }
                            });

                    //프레임 뷰에 보여지는 화면 바꾸기
                    imageView.setVisibility(View.VISIBLE);
                    cameraView.setVisibility(View.INVISIBLE);

                    // 아래 부분 주석을 풀 경우 사진 촬영 후에도 다시 프리뷰를 돌릴수 있음
//                    camera.startPreview();
                } catch (Exception e) {
                    Log.e("SampleCapture", "Failed to insert image.", e);
                }
            }
        });
    }

    //앨범에서 이미지 선택
    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    //이미지 파일의 밑바탕 만들기
    private File createImageFile() throws IOException {
        String imageFileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());                    //파일명
        File storageDir = new File(Environment.getExternalStorageDirectory() + cropImageDiretory);//내장메모리/폴더명 에 저장
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".png", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();  //절대경로로 URI 작성, 저장
        return image;
    }

    //이미지 크롭 함수.
    //photoUri 의 경로에 있는 사진 파일을 정사각형 모양으로 크롭, 저장하고 photoUri에 경로를 담는다.
    public void cropImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + cropImageDiretory);
            File tempFile = new File(folder.toString(), croppedFileName.getName());


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//sdk 24 이상, 누가(7.0)
                photoUri = FileProvider.getUriForFile(getApplicationContext(),// 7.0에서 바뀐 부분은 여기다.
                        BuildConfig.APPLICATION_ID + ".provider", tempFile);
            } else {//sdk 23 이하, 7.0 미만
                photoUri = Uri.fromFile(tempFile);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                grantUriPermission(res.activityInfo.packageName, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_IMAGE);
        }
    }

    //사진 크롭이나 앨범에서 사진 가져오는것의 결과 처리함수.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == PICK_FROM_ALBUM) {//앨범에서 사진 가져오기
            if (data == null) {
                return;
            }
            photoUri = data.getData();
            cropImage();
        } else if (requestCode == CROP_FROM_IMAGE) {//크롭
            imageView.setImageURI(null);//초기화? 필요한 이유를 모르겠다
            imageView.setImageURI(photoUri);//이 photoUri가 크롭된 이미지 파일의 경로

            imageView.setVisibility(View.VISIBLE);
            cameraView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 카메라 미리보기를 위한 서피스뷰 정의
     */
    private class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera camera = null;
        private int usingCamera;

        public CameraSurfaceView(Context context) {
            super(context);

            usingCamera = Camera.CameraInfo.CAMERA_FACING_BACK;//디폴트는 후면
            mHolder = getHolder();
            mHolder.addCallback(this);

        }

        public CameraSurfaceView(Context context, int cameraFacing) {
            super(context);

            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            usingCamera = cameraFacing;
        }

        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open(usingCamera);

            try {

                //오토 포커싱. 이거 없으면 초점 안맞음
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(params);

                camera.setDisplayOrientation(90);//왠지 90도 돌아가있음


                camera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                Log.e("CameraSurfaceView", "Failed to set camera preview.", e);
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        public boolean capture(Camera.PictureCallback handler) {
            if (camera != null) {
                camera.takePicture(null, null, handler);
                return true;
            } else {
                return false;
            }
        }

    }
}
