package open.it.com.petit.Fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import open.it.com.petit.R;

/**
 * Created by user on 2017-11-07.
 */

// 토큰을 받아서 서버로 등록하는 class. not define
public class PetitFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private final static String TAG = PetitFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token : " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "sendRegistrationToServer");
    }
}
