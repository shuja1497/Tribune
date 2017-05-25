package p2_vaio.signin;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by shuja reshi on 3/19/2017.
 */

public class signin extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
