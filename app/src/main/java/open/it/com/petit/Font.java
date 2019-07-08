package open.it.com.petit;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

/**
 * Created by user on 2017-07-10.
 */

public class Font extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Typekit.getInstance()
                .add("nanum", Typekit.createFromAsset(this, "NanumBarunGothic.ttf"))
                .add("nanumbold", Typekit.createFromAsset(this, "NanumBarunGothicBold.ttf"));
    }
}
