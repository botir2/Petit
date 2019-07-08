package open.it.com.petit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import open.it.com.petit.Main.View.MainActivity;


/**
 * Created by user on 2017-05-31.
 */

public class PetitCoverActivity extends AppCompatActivity {
    private static final String TAG = "PetitCoverActivity";

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(saveInstanceState);
        setContentView(R.layout.petit_cover_activity);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(PetitCoverActivity.this, MainActivity.class));
                finish();
            }
        }).start();
    }
}
