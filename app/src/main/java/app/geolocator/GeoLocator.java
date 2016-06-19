/**
 * Class of main activity
 * @author NickKopylov
 * @version 1.0
 */

package app.geolocator;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import java.util.Timer;
import java.util.TimerTask;

public class GeoLocator extends AppCompatActivity {
    /** MapView of main activity*/
    private MapView osm;
    /** Tool for work with MapView*/
    private MapController mc;
    /** Object of class GetLocation, allowing work with user location*/
    private GetLocation locmanager;
    /** Marker drawing on the map*/
    private Marker marker;
    /** Timer*/
    private Timer timer;
    /** Maximum zoom level of MapView*/
    int maxZoomLevel;
    /** Object of class SendDataToFirebase, sending data Firebase*/
    SendDataToFirebase dataToFirebase = new SendDataToFirebase();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        maxZoomLevel = 18;

        osm = (MapView) findViewById(R.id.map);

        locmanager = new GetLocation(GeoLocator.this);

        if (locmanager.canGetLocation()) {
            Toast.makeText(getApplicationContext(), "GPS works", Toast.LENGTH_SHORT).show();
        } else {
            locmanager.showSettingsAlert();
        }

        osm.setUseDataConnection(true);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.getOverlays().clear();
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);

        GeoPoint GP = new GeoPoint(locmanager.getLatitude(), locmanager.getLongitude());
        mc = (MapController) osm.getController();
        mc.setZoom(17);
        mc.setCenter(GP);
        osm.setMinZoomLevel(3);
        osm.setMaxZoomLevel(maxZoomLevel);

        createMarker(GP);

        if (timer != null) {
            timer.cancel();
        }

        mc.animateTo(new GeoPoint(locmanager.getLatitude(), locmanager.getLongitude()));

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
        }, 0, 1000);
        osm.invalidate();
    }

    /**
     * A function, that searches for the new location of the user
     */
    private void updateLocation() {
        //try to delete object
        locmanager = null;
        //initialization new object.
        locmanager = new GetLocation(GeoLocator.this);

        if (locmanager.getLatitude() == 0.0) {
            System.out.println("0.0, 0.0");
        }
        else
        {
            GeoPoint GP = new GeoPoint(locmanager.getLatitude(), locmanager.getLongitude());
            changeMarkerPosition(GP);
            dataToFirebase.sendData(locmanager.getLatitude(), locmanager.getLongitude());
        }
    }

    /**
     * A function, that create marker
     * @param StartGP Contain start latitude and longitude
     */
    public void createMarker(GeoPoint StartGP)
    {
        marker = new Marker(osm);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.locmarker));
        osm.getOverlays().add(marker);
        marker.setPosition(StartGP);
        marker.setEnabled(false);
        osm.invalidate();
    }

    /**
     * A function, that change marker position, when changed the location of the user
     * @param GP Contain new latitude and longitude
     */
    public void changeMarkerPosition(GeoPoint GP) {
        marker.setEnabled(true);
        marker.setPosition(GP);
        osm.invalidate();
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
                mc.animateTo(new GeoPoint(locmanager.getLatitude(), locmanager.getLongitude()));
                osm.invalidate();
                return true;

            case R.id.action_sc:
                Toast.makeText(getApplicationContext(), "Your coordinates:\n" + locmanager.getLatitude()
                        + " , " + locmanager.getLongitude(), Toast.LENGTH_LONG).show();
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

    /**
     * A quit function
     */
    public void quit() {
        Toast.makeText(getApplicationContext(), "Wait for exit, please", Toast.LENGTH_SHORT).show();
        dataToFirebase.deleteDataFromFirebase();
        System.gc();
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
        finish();
    }
}
