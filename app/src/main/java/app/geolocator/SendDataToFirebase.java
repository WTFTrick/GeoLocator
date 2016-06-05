package app.geolocator;

import com.firebase.client.Firebase;
import java.util.HashMap;
import java.util.Map;

public class SendDataToFirebase extends android.app.Application
{
    private Firebase firebaseRef;
    private Map<String, Double> userMap = new HashMap<>();
    private Firebase newPostRef;
    private Firebase userRef;

    public SendDataToFirebase()
    {
        firebaseRef = new Firebase("https://locmanager.firebaseio.com/");
        newPostRef = firebaseRef.push();
        String Id = newPostRef.getKey();
        userRef = firebaseRef.child(String.valueOf(Id));
    }

    public void deleteDataFromFirebase()
    {
        userRef.setValue(null);
    }

    public void sendData(double latitude, double longitude)
    {
        userMap.put("Latitude", latitude);
        userMap.put("Longitude", longitude);
        userRef.setValue(userMap);
        //System.out.println("Send to firebase: " +latitude + ", " + longitude);
    }
}
