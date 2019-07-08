package open.it.com.petit.Setting.FeederSetting.Popup;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;

import open.it.com.petit.Setting.FeederSetting.Model.FTPPictureController;
import open.it.com.petit.R;

import static open.it.com.petit.Setting.FeederSetting.Model.FTPPictureController.REQ_CAMERA;
import static open.it.com.petit.Setting.FeederSetting.Model.FTPPictureController.REQ_GALLERY;

/**
 * Created by user on 2017-07-26.
 */

public class ChangePicturePopup extends Activity {
    private static final String TAG = "ChangePicturePopup";
    private Button camera;
    private Button cameraRoll;
    private Button cancel;

    private ImageView test;
    private FTPPictureController ftp;

    @Override
    protected void onCreate(Bundle savaInstanceState) {
        super.onCreate(savaInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.petit_feeder_setting_picture_popup);

        camera = (Button) findViewById(R.id.setting_picture_camera);
        cameraRoll = (Button) findViewById(R.id.setting_picture_cameraRoll);
        cancel = (Button) findViewById(R.id.setting_picture_cancel);
        test = (ImageView) findViewById(R.id.testimage);

        ftp = new FTPPictureController(this);

        // 카메라
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQ_CAMERA);
            }
        });

        // 갤러리
        cameraRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("content://media/external/images/media");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQ_GALLERY);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQ_CAMERA) {
            if (data != null) {
                Log.d(TAG, "카메라 사진파일");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 받아온 data를 bitmap으로 변환
                            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                            ftp.upload(bitmap, "/var/www/html/petit/image", getIntent().getStringExtra("GUID"));
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        if (requestCode == REQ_GALLERY) {
            if (data != null) {
                Log.d(TAG, "갤러리파일");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 리 사이징
                            AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                            BitmapFactory.Options opt = new BitmapFactory.Options();
                            opt.inSampleSize = 4;
                            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(afd.getFileDescriptor(), null, opt);

                            ftp.upload(bitmap, "/var/www/html/petit/image", getIntent().getStringExtra("GUID"));
                            afd.close();
                            finish();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        return ;
    }
}
