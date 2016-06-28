/**
 * Service for getting device loc and send to Firebase;
 * @author NickKopylov
 * @version 1.0
 */
package app.geolocator;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import java.util.Timer;
import java.util.TimerTask;

public class GeoService extends Service {

    /** A var, contain device id for Firebase*/
    private String deviceID;
    /** A timer*/
    private Timer timer;
    /** An object of class SendDataToFirebase, sending data Firebase */
    private SendDataToFirebase dataToFirebase;
    /** A object of class GetLocation, allowing work with user location */
    private GetDeviceLocation locmanager;
    /** A handler for timer */
    private Handler handler;
    /** A flag for sending data to Firebase */
    private Boolean fSend = false;

    /** A constructor */
    public GeoService()
    {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceID = intent.getStringExtra("deviceID");
        System.out.println("deviceID = " + deviceID);
        dataToFirebase = new SendDataToFirebase(deviceID);
        fSend = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate()
    {
        System.out.println("Service created.");

        handler = new Handler();

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable(){
                    public void run() {
                        if (fSend) {
                            getLocation();
                        }
                    }
                });
            }
        }, 0, 3000);
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    /**
     * A function, that get device location in background
     */
    private void getLocation()
    {
        locmanager = null;
        //initialization new object.
        locmanager = new GetDeviceLocation(GeoService.this);
        if (locmanager.getLatitude() == 0.0) {
            System.out.println("0.0, 0.0");
        }
        else
        {
            dataToFirebase.sendData(locmanager.getLatitude(), locmanager.getLongitude());
        }
    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        System.out.println("Service started.");
    }

    @Override
    public void onDestroy()
    {
        System.out.println("Service destroyed.");
        dataToFirebase.deleteDataFromFirebase();
        super.onDestroy();
    }
}
