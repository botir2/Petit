package open.it.com.petit.Media.Model;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import open.it.com.petit.Util.Util;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

/**
 * Created by user on 2018-01-15.
 */

// 캡쳐, 녹화를 위한 BaseMediaProjection
public abstract class BaseMediaProjection {
    private static final String TAG = BaseMediaProjection.class.getSimpleName();

    protected static final int DISPLAY_WIDTH = 720;
    protected static final int DISPLAY_HEIGHT = 1280;

    protected String EXTERNAL_STORAGE_PATH; // 저장 경로
    protected String extension; // 확장자
    public static final int RECORD = 0x10; // 레코드 구분
    public static final int CAPTURE = 0x11; // 캡쳐 구분

    protected Context context;
    protected String fileName; // 파일이름

    protected MediaProjectionManager mpManager; // mediaProjection 사용을 위한 manager
    protected MediaProjection mp;
    protected VirtualDisplay virtualDisplay;

    protected int screenDensity;

    public BaseMediaProjection(Context context, int screenDensity, int act) {
        this.context = context;
        this.screenDensity = screenDensity;
        this.extension = act == RECORD ? ".mp4" : ".png"; // 확장자 선택
        // sdk 버전확인. mediaProjection은 api21 이상에서 동작한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            // media projection을 가져옴
            this.mpManager = (MediaProjectionManager) context.getSystemService(MEDIA_PROJECTION_SERVICE);
        } else {
            this.mpManager = null;
        }
        getExternalPath();
    }

    // 저장경로를 가져온다.
    private void getExternalPath() {
        String state = Environment.getExternalStorageState();
        // 외장메모리가 없을 때 내부에 저장
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "외장 메모리가 마운트되지 않았습니다.", Toast.LENGTH_SHORT).show();
            fileName = "Petit_" + Util.getDate() + extension;
        }
        // 외장메모리가 있을 때
        else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            fileName = EXTERNAL_STORAGE_PATH + "/Petit" + Util.getDate() + extension;
        }
    }

    public abstract void start(int resultCode, Intent data);
    public abstract void stop();
    protected abstract VirtualDisplay createVirtualDisplay();
}
