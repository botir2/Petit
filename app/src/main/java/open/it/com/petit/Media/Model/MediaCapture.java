package open.it.com.petit.Media.Model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import open.it.com.petit.EventBus.BusProvider;
import open.it.com.petit.Media.Event.ProjectionEvent;

/**
 * Created by user on 2018-01-16.
 */

public class MediaCapture extends BaseMediaProjection {
    private static final String TAG = MediaCapture.class.getSimpleName();

    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
    private ImageReader imageReader; // 화면에서 이미지를 읽어오는 객체
    private Display display;
    private DisplayMetrics metrics;

    private OrientationChangeCallback orientationChangeCallback; // 이미지 처리 콜백

    private int rotation;
    private int width;
    private int height;

    public MediaCapture(Context context, int screenDensity, int act, DisplayMetrics metrics, Display display) {
        super(context, screenDensity, act);
        this.metrics = metrics;
        this.display = display;
    }

    public void retrievePermission() {
        Log.d(TAG, "retrievePermission");
        // 퍼미션을 처리하는 onActivityResult를 불러오기 위한 Bus
        BusProvider.getInstance().post(new ProjectionEvent(mpManager, BaseMediaProjection.CAPTURE));
    }

    @Override
    public void start(int resultCode, Intent data) {
        // 권한요청 팝업이 꺼질때 까지 기다린다
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //@param1 : 퍼미션 결과코드, @param2 : 화면캡쳐 데이터
        mp = mpManager.getMediaProjection(resultCode, data);
        mp.registerCallback(mpCallback, null);
        virtualDisplay = createVirtualDisplay(); // 캡쳐용 가상 display 생성

        orientationChangeCallback = new OrientationChangeCallback(context);
        if (orientationChangeCallback.canDetectOrientation()) {
            orientationChangeCallback.enable();
        }
    }

    @Override
    public void stop() {
        freeMediaObject();
        Toast.makeText(context, "캡쳐 완료", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected VirtualDisplay createVirtualDisplay() {
        // Point 객체를 이용해 display의 너비 높이를 구한다
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        Log.d(TAG, width +":" + height +":" +screenDensity);
        // start capture reader
        // 이미지 리더를 할당
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        // 이미지 리더 리스너로 이미지를 만듦
        imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
        return mp.createVirtualDisplay("capture",
                width, height, 420,
                VIRTUAL_DISPLAY_FLAGS,
                imageReader.getSurface(),
                null, null);
    }

    private void freeMediaObject() {
        if (virtualDisplay == null) {
            return;
        }
        virtualDisplay.release();
        virtualDisplay = null;

        if(mp != null) {
            mp.unregisterCallback(mpCallback);
            mp.stop();
            mp = null;
        }
    }

    // 이미지 리더로 이미지를 만드는 method
    final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null; // 이미지 객체
            FileOutputStream fos = null; // 파일 아웃풋 스트림
            Bitmap bitmap = null; // 비트맵

            try {
                image = reader.acquireLatestImage(); // 이미지 리더를 넘긴다.
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    // write bitmap to a file
                    fos = new FileOutputStream(fileName);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    Log.e(TAG, "captured image: ");
                }
            } catch (Exception e) {
                e.printStackTrace(); // 에러메세지
            } finally {
                if (fos != null) {
                    try {
                        fos.close(); // 파일아웃풋스트림 close
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle(); // 비트맵 init
                }

                if (image != null) {
                    image.close(); // image close
                }
            }

            mp.unregisterCallback(mpCallback);
            mp.stop();
            mp = null;

            imageReader.setOnImageAvailableListener(null, null);
            Log.d(TAG, "onImageAvailable end");
            //handler.sendMessage(handler.obtainMessage(CAPTURE_STOP));
            stop();
        }
    };

    final MediaProjection.Callback mpCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            super.onStop();
        }
    };

    // 가로 세로가 바뀌면 다시 virtual display를 create한다
    private class OrientationChangeCallback extends OrientationEventListener {
        public OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int r = display.getRotation();
            if (r != rotation) {
                rotation = r;
                try {
                    // clean up
                    if (virtualDisplay != null) virtualDisplay.release();
                    if (imageReader != null) imageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
