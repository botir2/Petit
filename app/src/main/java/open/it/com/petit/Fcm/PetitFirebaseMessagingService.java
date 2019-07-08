package open.it.com.petit.Fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import open.it.com.petit.Main.View.MainActivity;
import open.it.com.petit.R;

/**
 * Created by user on 2017-11-07.
 */

// fcm 메세지를 받는 class
public class PetitFirebaseMessagingService extends FirebaseMessagingService{
    private final static String TAG = PetitFirebaseMessagingService.class.getSimpleName();
    private SharedPreferences sfr;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        sfr = getSharedPreferences("system_setting", MODE_PRIVATE);
        if (sfr.getInt("isAlarm", 0) == 0)
            return;

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendNotification(remoteMessage.getData().get("message"));
        }

      /*  if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getData().get("message"));
        }*/
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }

    // 핸드폰에 notification을 나타내는 method
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 알람 소리
        // notification 빌더
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.f_icon01)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 알람을깨움
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakeLock.acquire(5000);

        notificationManager.notify(0, notificationBuilder.build()); // 알람 실행
    }
}
