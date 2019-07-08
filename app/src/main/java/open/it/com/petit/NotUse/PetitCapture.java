package open.it.com.petit.NotUse;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.muddzdev.viewshotlibrary.Viewshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 2017-06-28.
 */

public class PetitCapture implements Viewshot.OnSaveResultListener{
    public static final String TAG = "PetitCapture";
    private final int MEDIA_CAPTURE_START = 2;

    private Context context;
    private Handler handler;
    private SurfaceView sv;

    private MediaProjectionManager mpManager;
    private MediaProjection mp;
    private MediaProjectionCallback mpCallback;
    private VirtualDisplay vd;

    public PetitCapture(Context context, Handler handler, SurfaceView sv) {
        this.context = context;
        this.handler = handler;
        this.sv = sv;
        this.mpManager = (MediaProjectionManager)context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public void prepareRecord() {
        Viewshot.of(sv).setOnSaveResultListener(this).toPNG().save();
    }

    public File BitmapToPng() {
        Bitmap bitmap = null;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-HHmmss");
        String capture_img = "Petit_" + sdf.format(date) + ".png";
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), capture_img);

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, os);
            os.close();
        }catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "capture exception");
            return null;
        }
        return file;
    }

    @Override
    public void onSaveResult(boolean isSaved, String path) {
        if (isSaved) {
            Toast.makeText(context, "Saved to " + path, Toast.LENGTH_SHORT).show();
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
        }
    }
}
