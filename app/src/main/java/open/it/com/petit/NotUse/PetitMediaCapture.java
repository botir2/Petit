package open.it.com.petit.NotUse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 2017-07-05.
 */

public class PetitMediaCapture extends MediaSave{
    //private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
    private OrientationChangeCallback mOrientationChangeCallback;
    private ImageReader imageReader;
    private Display display;
    private DisplayMetrics metrics;

    private int mRotation;
    private int mWidth;
    private int mHeight;

    public PetitMediaCapture(Context context, Handler handler, int screenDensity, DisplayMetrics metrics, Display display) {
        super(context, handler, screenDensity);
        this.metrics = metrics;
        this.display = display;
        Log.d(TAG, "screenDensity : " + screenDensity);
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable");
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    // write bitmap to a file
                    fos = new FileOutputStream(createFileName());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    Log.e(TAG, "captured image: ");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }

            mp.unregisterCallback(mpCallback);
            mp.stop();
            mp = null;

            imageReader.setOnImageAvailableListener(null, null);
            Log.d(TAG, "onImageAvailable end");
            handler.sendMessage(handler.obtainMessage(CAPTURE_STOP));
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {
        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = display.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (vd != null) vd.release();
                    if (imageReader != null) imageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void initMedia() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "외장 메모리가 마운트되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            //EXTERNAL_STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
        }
    }

    @Override
    protected VirtualDisplay createVirtualDisplay() {
        Point size = new Point();
        display.getSize(size);
        mWidth = size.x;
        mHeight = size.y;
        Log.d(TAG, mWidth +":" + mHeight +":" +screenDensity);
        // start capture reader
        imageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);
        return mp.createVirtualDisplay("Tutorial4",
                mWidth, mHeight, 420,
                VIRTUAL_DISPLAY_FLAGS,
                imageReader.getSurface(),
                null, null);
    }

    @Override
    public void start(int resultCode, Intent data) {
        Log.d(TAG, "capture");
        initMedia();
        mpCallback = new MediaProjectionCallback();
        mp = mpManager.getMediaProjection(resultCode, data);
        mp.registerCallback(mpCallback, null);
        vd = createVirtualDisplay();

        mOrientationChangeCallback = new OrientationChangeCallback(context);
        if (mOrientationChangeCallback.canDetectOrientation()) {
            mOrientationChangeCallback.enable();
        }

        mp.registerCallback(new MediaProjectionCallback(), handler);
    }

    @Override
    public void stop(String msg) {
        freeMediaObject(msg);
    }

    @Override
    protected String createFileName() {
        String fName = null;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
        if (EXTERNAL_STORAGE_PATH == null || EXTERNAL_STORAGE_PATH.equals("")) {
            fName = "Petit_" + sdf.format(date) + ".png";
        } else {
            fName = EXTERNAL_STORAGE_PATH + "/Petit_" + sdf.format(date) + ".png";
        }

        return fName;
    }
}
