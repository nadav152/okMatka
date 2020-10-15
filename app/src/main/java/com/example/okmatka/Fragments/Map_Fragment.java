package com.example.okmatka.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Map_Fragment extends Fragment {

    private MapView mapView;
    private Handler handler = new Handler();
    private final int DELAY = 5000;
    private String userISpeakWithId;
    private User userISpeakWith;
    private FirebaseUser firebaseUser;
    private DatabaseReference usersLocationRef;
    private LatLng currentUserPos;
    private LatLng myPos;
    private Bitmap bitmap;
    private double lat = 0.0;
    private double lon = 0.0;

    public Map_Fragment() {
    }

    public Map_Fragment(User user) {
        this.userISpeakWithId = user.getId();
        this.userISpeakWith = user;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = v.findViewById(R.id.map_MV_mapView);
        mapView.onCreate(savedInstanceState);
        initFireBase();
        initLPosition();
        setBitMap();
        setFireBaseListener();
        setMarkerOnLocation();
        return v;
    }

    private void setBitMap(){

        if (!userISpeakWith.getImageURL().equals("default")) {
            getBitmapFromURL(userISpeakWith.getImageURL());
        }
    }

    public  void getBitmapFromURL(final String src){
        //todo add image to marker
    }

    private void initFireBase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersLocationRef = MyFireBase.getInstance().
                getReference(MyFireBase.KEYS.USERS_LOCATIONS);
    }

    private void initLPosition() {
        currentUserPos = new LatLng(32.079994 ,34.767316);
    }

    private void setFireBaseListener() {
        usersLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(userISpeakWithId)) {
                    LatLng userLocation = snapshot.child(userISpeakWithId).getValue(LatLng.class);
                    if(userLocation != null)
                        currentUserPos = userLocation;
                    setMarkerOnLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setMyLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());
        //checking permission
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double currentLat = location.getLatitude();
                        double currentLon = location.getLongitude();
                        if (isLocationChange(currentLat,lat,currentLon,lon)) {
                            myPos = new LatLng(currentLat, currentLon);
                            usersLocationRef.child(firebaseUser.getUid()).setValue(myPos);
                            lat = currentLat;
                            lon = currentLon;
                        }
                    }
                }
            });
        } else
            MySignal.getInstance().showToast("This game location was not captured");
    }

    private boolean isLocationChange(double myLat, double lat, double myLon, double lon) {
        myLat = Math.floor(myLat * 100) / 100;
        lat = Math.floor(lat * 100) / 100;
        myLon = Math.floor(myLon * 100) / 100;
        lon = Math.floor(lon * 100) / 100;

        return myLat != lat && myLon != lon;
    }

    //this method sets the map new marker location
    public void setMarkerOnLocation() {
        mapView.getMapAsync(mapReadyCallback);
    }

    private Runnable updateMyLocation = new Runnable() {
        @Override
        public void run() {
            setMyLocation();
            handler.postDelayed(this, DELAY);
        }
    };

    private OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // Clears the previously touched position
            googleMap.clear();
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(true);

            //todo change to zoom on my location
            // Animating to the touched position
            CameraPosition cameraPosition = new CameraPosition.Builder().target(currentUserPos).zoom(13).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // Placing a marker on the touched position
            placeMarker(googleMap);
        }
    };

    private void placeMarker(GoogleMap googleMap) {
        MarkerOptions marker;
        if (bitmap!=null) {
             marker = new MarkerOptions()
                    .position(currentUserPos)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }else
            Log.d("ppp","bitmap is null");
            marker = new MarkerOptions()
                    .position(currentUserPos);
        googleMap.addMarker(marker);
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.postDelayed(updateMyLocation, DELAY);
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateMyLocation);
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
