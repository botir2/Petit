package open.it.com.petit.NotUse;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by user on 2017-06-28.
 */

public class PetitMediaAudio {
    public static final String TAG = "PetitMediaAudio";

    private String audioUrl = "211.38.86.93:1935";
    private AudioRecord recorder;
    private int rate = 8000;
    private boolean isRecording = false;
    private short channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private short audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(rate,
            channelConfig,
            audioFormat);

    public PetitMediaAudio() {

    }

    public void send_audio_start() {
        Log.d(TAG, "bufferSize : " + bufferSize);
        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, channelConfig, audioFormat, bufferSize);
            Log.d(TAG, "state : " + recorder.getState());
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                Log.d(TAG, "성공");
                recorder.startRecording();

                isRecording = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send_audio();
                    }
                }).start();
            }
        }
    }

    public void send_audio_stop() {
        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            Log.d(TAG, "audio stop");
        }
    }

    private void send_audio() {
        DatagramPacket datagramPacket;
        DatagramSocket socket;
        short sData[] = new short[bufferSize];
        while(isRecording) {
            recorder.read(sData, 0, bufferSize);
            try {
                byte bData[] = short2byte(sData);
                Log.d(TAG, "rec :" + bData);
                /*datagramPacket = new DatagramPacket(bData, bData.length, InetAddress.getByName(audioUrl.split(":")[0]),
                        Integer.valueOf(audioUrl.split(":")[1]));*/

                datagramPacket = new DatagramPacket(bData, bData.length);
                datagramPacket.setAddress(InetAddress.getByName(audioUrl));

                socket = new DatagramSocket();

                socket.send(datagramPacket);
            }catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "파일을 write할 수 없습니다.");
            }
        }
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }
}
