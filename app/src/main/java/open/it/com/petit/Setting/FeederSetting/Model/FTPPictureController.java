package open.it.com.petit.Setting.FeederSetting.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Vector;

import open.it.com.petit.Connection.ConnectionController;
import open.it.com.petit.Connection.HttpHandler;
import open.it.com.petit.R;
import open.it.com.petit.Util.Util;

/**
 * Created by user on 2017-11-06.
 */

public class FTPPictureController {
    private final static String TAG = FTPPictureController.class.getSimpleName();
    public final static int REQ_GALLERY = 200; // 갤러리 구분
    public final static int REQ_CAMERA = 201; // 카메라 구분

    private Context context;

    private JSch jsch; // sftp object
    private Session session; // for setting
    private Channel channel; // for connection
    private ChannelSftp channelSftp; // 터미널 명령어를 사용할 수 있는 객체

    private String host;
    private int port;
    private String id;
    private String pw;

    private String imagePath;
    private String imageName;

    private ProgressBar pb;
    private ImageView image;

    public FTPPictureController(Context context) {
        this.context = context;
        this.host = context.getString(R.string.host);
        this.port = Integer.valueOf(context.getString(R.string.port));
        this.id = context.getString(R.string.host_id);
        this.pw = context.getString(R.string.host_pw);
    }

    private void connect() throws JSchException {
        jsch = new JSch();
        try {
            session = jsch.getSession(id, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(pw);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    public void upload(Bitmap bitmap, String remoteDir, String guid) throws Exception {
        connect();
        try {
            //디렉터리 확인
            String curDir = remoteDir; // ../image
            String dir = guid;
            String newDir = curDir + "/" + dir; // ../image/guid
            SftpATTRS attrs = null;
            try {
                attrs = channelSftp.stat(newDir); // 디렉터리 존재 유무 확인
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d(TAG, newDir);
            // 디렉터리가 없으면
            if (attrs == null) {
                channelSftp.cd(curDir); // cd ../image
                channelSftp.mkdir(dir); // ../image/guid
            }

            curDir = newDir; // ../image/guid
            dir = Util.getPhoneNum(context);
            newDir = curDir + "/" + dir; // ../image/[guid]/[phone number]
            Log.d(TAG, newDir);

            attrs = null;
            try {
                attrs = channelSftp.stat(newDir); // 디렉터리 존재 유무 확인
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 디렉터리가 없으면
            if (attrs == null) {
                channelSftp.cd(curDir);
                channelSftp.mkdir(dir);
            }

            channelSftp.cd(newDir);

            imageName = "petit" + Util.getDate() + ".png";
            File f = new File(context.getCacheDir(), imageName);
            f.createNewFile(); // petit[current date].png 라는 빈 파일 생성
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
            byte[] bitmapdata = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(bitmapdata);

            //기존파일 삭제
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(newDir);
            for (ChannelSftp.LsEntry file : files) {
                // .(현재디렉토리) , ..(이전디렉토리)
                if (file.getFilename().equals(".") || file.getFilename().equals(".."))
                    continue;
                channelSftp.rm(file.getFilename());
            }

            // ftp로 파일 전송
            channelSftp.put(bais, f.getName(), ChannelSftp.OVERWRITE);

            imageUploadToDatabase(newDir, guid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        disconnect();
    }

    //디비 경로 update
    private void imageUploadToDatabase(String path, String id) {
        String tmpPath[] = path.split("/");
        for (String str : tmpPath)
            Log.d(TAG, str);
        imagePath = "http://" + host + "/" + tmpPath[4] + "/" + tmpPath[5] + "/" + tmpPath[6] + "/" + tmpPath[7] + "/" + imageName;
        Log.d(TAG, imagePath);

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("F_IMG", imagePath);
        map.put("P_NUM", tmpPath[7]);
        map.put("GUID", tmpPath[6]);
        String php = "feeder_image_change.php";
        ConnectionController conn = new ConnectionController(context, handler);
        conn.setMethod("POST").setHash(map).setUrl(php);
        Thread thread = new Thread(conn);
        thread.start();
    }

    public void disconnect() {
        if(session.isConnected()){
            channelSftp.disconnect();
            channel.disconnect();
            session.disconnect();
        }
    }

    final HttpHandler handler = new HttpHandler(context) {
        @Override
        public void onHttpOK() {
            super.onHttpOK();
            Toast.makeText(context, "이미지 변경 완료", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onHttpError() {
            super.onHttpError();
        }
    };
}
