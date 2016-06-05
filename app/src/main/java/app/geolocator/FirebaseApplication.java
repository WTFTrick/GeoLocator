package app.geolocator;

import com.firebase.client.Firebase;

public class FirebaseApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
