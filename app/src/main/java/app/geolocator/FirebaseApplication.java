/**
 * Class FirebaseApplication for setting Android Context
 * @author NickKopylov
 * @version 1.0
 */

package app.geolocator;

import com.firebase.client.Firebase;

public class FirebaseApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
