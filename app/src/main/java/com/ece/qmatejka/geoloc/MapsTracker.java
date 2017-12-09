package com.ece.qmatejka.geoloc;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by qmatejka on 09/12/2017.
 */

public class MapsTracker implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng home = new LatLng(48.866667, 2.333333);
        mMap.addMarker(new MarkerOptions().position(home).title("Marker in Paris"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
    }

    public void searchLocation(double lat, double lon){
        LatLng pos = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(pos).title("Your Phone"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
    }

}
