package nmct.howest.be.jellyb;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends ActionBarActivity implements LocationListener{

    LocationManager locationManager ;
    String provider;

    double oldLon=0;
    double oldLat=0;

    public static TextView tvLatitudeT=null;
    public static TextView tvLongitudeT=null;
    public static TextView tvAltitudeT=null;
    public static TextView textViewKilometers=null;

    // All static variables
    static final String URL = "http://geowebservice.azurewebsites.net/Api/Geo";

    // XML node keys

    static final String KEY_LAT = "lat";
    static final String KEY_LON = "lon";
    static final String KEY_ALT = "alt";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Execute XML READER
        new loadmore().execute();

        // Getting LocationManager object
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, false);

        if(provider!=null && !provider.equals("")){

            // Get the location from the given provider
            Location location = locationManager.getLastKnownLocation(provider);

            locationManager.requestLocationUpdates(provider, 500, (float)0.01, this);

            if(location!=null)
                onLocationChanged(location);
            else
                Toast.makeText(getBaseContext(), "Location can't be retrieved", Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(getBaseContext(), "No Provider Found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        new loadmore().execute();

        // Getting reference to TextView tv_longitude
        TextView tvLongitude = (TextView)findViewById(R.id.tv_longitude);

        // Getting reference to TextView tv_latitude
        TextView tvLatitude = (TextView) findViewById(R.id.tv_latitude);

        // Getting referene to TextView tv_altitude
        TextView tvAltitude = (TextView) findViewById(R.id.tv_altitude);

        //GRADEN
        TextView textViewDegrees = (TextView) findViewById(R.id.textViewDegrees);

        //DIRECTION
        TextView textViewDirection = (TextView) findViewById(R.id.textViewDirection);

        // Setting Current Longitude
        tvLongitude.setText(""+location.getLongitude());

        // Setting Current Latitude
        tvLatitude.setText(""+location.getLatitude());

        tvAltitude.setText(""+location.getAltitude());

        //textViewDirection.setText(""+location.getBearing()+"-"+location.getSpeed());

        //String cityName = null;
        String address = null;
        String city = null;
        String country = null;
        List<Address> addresses;
        Geocoder mGeocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            //cityName=addresses.get(0).getLocality();

            address = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getAddressLine(1);
            country = addresses.get(0).getAddressLine(2);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(address == null){address="Unknown street and number!";}
        if(city == null){city="Unknown zipcode and city!";}
        if(country == null){country="Unknown country!";}

        TextView textViewStreetNumber = (TextView) findViewById(R.id.textViewStreetNumber);
        textViewStreetNumber.setText(address);
        TextView textViewZipcodeCity = (TextView) findViewById(R.id.textViewZipcodeCity);
        textViewZipcodeCity.setText(city);
        TextView textViewCountry = (TextView) findViewById(R.id.textViewCountry);
        textViewCountry.setText(country);

        // show The Image
        if(tvLongitudeT!=null && tvLatitudeT!=null) {
            new DownloadImageTask((ImageView) findViewById(R.id.imageViewMap))
                    .execute("https://maps.googleapis.com/maps/api/staticmap?center=" + location.getLatitude() + "," + location.getLongitude() + "&zoom=17&size=475x275&scale=2" +
                            "&markers=color:green%7Clabel:YOU%7C" + location.getLatitude() + "," + location.getLongitude()+
                    "&markers=color:red%7Clabel:TAR%7C" + tvLatitudeT.getText().toString() + "," + tvLongitudeT.getText().toString());

            double dist = distance(location.getLatitude(), location.getLongitude(), Double.parseDouble(tvLatitudeT.getText().toString()), Double.parseDouble(tvLongitudeT.getText().toString()), "K");
            double factor = 1e5; // = 1 * 10^5 = 100000.
            dist = Math.round(dist * factor) / factor;
            textViewKilometers = (TextView) findViewById(R.id.textViewKilometers);
            textViewKilometers.setText(""+dist);

            double degree = bearing(location.getLatitude(), location.getLongitude(), Double.parseDouble(tvLatitudeT.getText().toString()), Double.parseDouble(tvLongitudeT.getText().toString()));
            degree = Math.round(degree * factor) / factor;
            textViewDegrees.setText(""+degree);

            ImageView imageViewArrow = (ImageView) findViewById(R.id.imageViewArrow);
            imageViewArrow.setRotation((float) degree);

            if(oldLon!=0 && oldLat!=0) {
                double degree2 = bearing(oldLat, oldLon, location.getLatitude(), location.getLongitude());
                degree2 = Math.round(degree2 * factor) / factor;
                textViewDirection.setText("" + degree2);

                ImageView imageViewArrow2 = (ImageView) findViewById(R.id.imageViewArrow2);
                imageViewArrow2.setRotation((float) degree2);
            }

        }
        else{
            new DownloadImageTask((ImageView) findViewById(R.id.imageViewMap))
                    .execute("https://maps.googleapis.com/maps/api/staticmap?center=" + location.getLatitude() + "," + location.getLongitude() + "&zoom=17&size=475x275&scale=2" +
                            "&markers=color:green%7Clabel:YOU%7C" + location.getLatitude() + "," + location.getLongitude());
        }

        oldLat = location.getLatitude();
        oldLon =  location.getLongitude();

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public class loadmore extends AsyncTask<String, Integer, ArrayList<HashMap<String, String>>> {

        ArrayList<HashMap<String, String>> coordinatesTarget = new ArrayList<HashMap<String, String>>();

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(
                String... params) {
            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(URL); // getting XML
            Document doc = null; // getting DOM element

            // Creating new HashMap
            HashMap<String, String> map = parser.getDomElement(xml);

            // Adding HashList to ArrayList
            coordinatesTarget.add(map);

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) {

            // Adding coordinates to TextViews
            tvLatitudeT = (TextView)findViewById(R.id.tv_latitudeT);
            tvLongitudeT = (TextView)findViewById(R.id.tv_longitudeT);
            tvAltitudeT = (TextView)findViewById(R.id.tv_altitudeT);

            tvLatitudeT.setText(coordinatesTarget.get(0).get(KEY_LAT));
            tvLongitudeT.setText(coordinatesTarget.get(0).get(KEY_LON));
            tvAltitudeT.setText(coordinatesTarget.get(0).get(KEY_ALT));

        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit.equals("K")) {
           dist = dist * 1.609344;
        } else if (unit.equals("N")) {
        dist = dist * 0.8684;
        }
        return (dist);
        }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
      return (rad * 180 / Math.PI);
    }

    public static double bearing(double lat1, double lon1, double lat2, double lon2){

        double longDiff= lon2-lon1;
        double y = Math.sin(longDiff)*Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2)-Math.sin(lat1)*Math.cos(lat2)*Math.cos(longDiff);

        double result = (Math.toDegrees(Math.atan2(y, x))+360)%360;

        return result;
    }

}
