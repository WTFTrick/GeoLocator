package app.geolocator;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.firebase.client.Firebase;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GeoLocator extends AppCompatActivity {

    private MapView osm;
    private MapController mc;
    GPSTracker gps;
    Marker marker;
    Timer timer;
    Firebase myFirebaseRef;
    Firebase User1Ref;
    Map<String, Double> User1Map;
    int maxZoomLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        maxZoomLevel = 18;

        osm = (MapView) findViewById(R.id.map);

        gps = new GPSTracker(GeoLocator.this);
        if (gps.canGetLocation()) {
            osm.setUseDataConnection(true);
            osm.setTileSource(TileSourceFactory.MAPNIK);
            osm.getOverlays().clear();
            osm.setBuiltInZoomControls(true);
            osm.setMultiTouchControls(true);

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            GeoPoint GP = new GeoPoint(latitude, longitude);
            mc = (MapController) osm.getController();
            mc.setZoom(17);
            mc.setCenter(GP);
            osm.setMinZoomLevel(3);
            osm.setMaxZoomLevel(maxZoomLevel);

            marker = new Marker(osm);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(getResources().getDrawable(R.drawable.locmarker));
            osm.getOverlays().add(marker);
            GeoPoint GPStart = new GeoPoint(gps.getLatitude(), gps.getLongitude());
            marker.setPosition(GPStart);
            marker.setEnabled(false);

            Firebase.setAndroidContext(this);
            myFirebaseRef = new Firebase("https://locmanager.firebaseio.com/");
            Firebase newPostRef = myFirebaseRef.push();
            String Id = newPostRef.getKey();
            User1Ref = myFirebaseRef.child(String.valueOf(Id));
            User1Map = new HashMap<>();

            if (timer != null) {
                timer.cancel();
            }

            mc.animateTo(new GeoPoint(gps.getLatitude(), gps.getLongitude()));

            timer = new Timer();

            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    GeoLocator.this.runOnUiThread(new Runnable() {
                        public void run() {
                            updateLocation();
                        }
                    });
                }
            }, 500, 1000);

        } else {
            gps.showSettingsAlert();
        }
    }

    private void updateLocation() {
        //удаляем объект
        gps = null;
        System.gc();
        //инициализируем новый
        //Без переинициализации работает не корректно. Необходимо разобраться.
        gps = new GPSTracker(GeoLocator.this);

        if (gps.getLatitude() == 0.0) {
            System.out.println("0.0, 0.0");
        } else {
            GeoPoint GP = new GeoPoint(gps.getLatitude(), gps.getLongitude());
            changeMarkerPosition(GP);
            SendDataToFirebase(gps.getLatitude(), gps.getLongitude());
            //System.out.println("Location update: " + gps.getLatitude() + gps.getLongitude());
        }
    }

    public void changeMarkerPosition(GeoPoint GP) {
        marker.setEnabled(true);
        marker.setPosition(GP);
        osm.invalidate();
        //moveToUserLocation();
    }

    public void SendDataToFirebase(double lati, double longi) {
        User1Map.put("Latitude", lati);
        User1Map.put("Longitude", longi);
        User1Ref.setValue(User1Map);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.action_mtu:
                mc.setZoom(maxZoomLevel);
                mc.animateTo(new GeoPoint(gps.getLatitude(), gps.getLongitude()));
                osm.invalidate();
                return true;

            case R.id.action_sc:
                Toast.makeText(getApplicationContext(), "Your coordinates:\n" + gps.getLatitude()
                        + " , " + gps.getLongitude(), Toast.LENGTH_LONG).show();
                System.gc();
                return true;

            case R.id.action_onm:
                maxZoomLevel = 18;
                osm.setMaxZoomLevel(maxZoomLevel);
                mc.setZoom(maxZoomLevel);
                osm.setUseDataConnection(true);
                osm.setTileSource(TileSourceFactory.MAPNIK);
                osm.invalidate();
                return true;

            case R.id.action_ofm:
                maxZoomLevel = 16;
                osm.setMaxZoomLevel(maxZoomLevel);
                mc.setZoom(maxZoomLevel);
                osm.setUseDataConnection(false);
                osm.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
                osm.invalidate();
                return true;

            case R.id.action_exit:
                quit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void quit() {
        System.gc();
        User1Ref.setValue(null);
        Toast.makeText(getApplicationContext(), "Wait for exit, please", Toast.LENGTH_SHORT).show();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
        finish();
    }
}
