/**
 * Class, sending data to Firebase
 * @author NickKopylov
 * @version 1.0
 */

package app.geolocator;

import com.firebase.client.Firebase;
import java.util.HashMap;
import java.util.Map;

public class SendDataToFirebase extends android.app.Application {
    /** Object of class Firebase firebaseRef*/
    private Firebase firebaseRef;
    /** Contaitner with pair key, value*/
    private Map<String, Double> userMap = new HashMap<>();
    /** Object of class Firebase newPostRef*/
    private Firebase newPostRef;
    /** Object of class Firebase userRef*/
    private Firebase userRef;

    public SendDataToFirebase() {
        firebaseRef = new Firebase("https://locmanager.firebaseio.com/");

        newPostRef = firebaseRef.push();
        String Id = newPostRef.getKey();
        userRef = firebaseRef.child(String.valueOf(Id));
    }

    public void deleteDataFromFirebase() {
        userRef.setValue(null);
    }

    /**
     * @param latitude Value of latitude
     * @param longitude Value of longitude
     * */
    public void sendData(double latitude, double longitude) {
        userMap.put("Latitude", latitude);
        userMap.put("Longitude", longitude);
        userRef.setValue(userMap);
        //System.out.println("Send to firebase: " +latitude + ", " + longitude);
    }
}
