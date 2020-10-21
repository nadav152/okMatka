package com.example.okmatka.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.okmatka.MyFireBase;
import com.example.okmatka.MySignal;
import com.example.okmatka.R;
import com.example.okmatka.User;
import com.example.okmatka.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private final int DELAY = 1000;
    private String userISpeakWithId;
    private User userISpeakWith;
    private FirebaseUser firebaseUser;
    private DatabaseReference usersLocationRef;
    private LatLng currentUserPos;
    private String matchRef;
    private double lat = 0.0;
    private double lon = 0.0;
    private boolean iCreatedTheRoom;

    public Map_Fragment() {
    }

    public Map_Fragment(User user, String matchRef,boolean iCreatedTheRoom) {
        this.userISpeakWithId = user.getId();
        this.userISpeakWith = user;
        this.matchRef = matchRef;
        this.iCreatedTheRoom = iCreatedTheRoom;
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
        setFireBaseListener();
        return v;
    }

    private void setFireBaseListener() {
        usersLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(userISpeakWithId)) {
                    //user gave me his location to the location room
                    UserLocation location = snapshot.child(userISpeakWithId).getValue(UserLocation.class);
                    assert location != null;
                    currentUserPos = new LatLng(location.getLatitude(), location.getLongitude());
                    setMarkerOnCurrentPos();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    //this method sets the map new marker location
    public void setMarkerOnCurrentPos() {
        mapView.getMapAsync(mapReadyCallback);
    }

    private OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // Clears the previously touched position
            googleMap.clear();
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);
            placeMarker(googleMap);
        }
    };

    private void placeMarker(GoogleMap googleMap) {
        MarkerOptions marker = new MarkerOptions()
                    .position(currentUserPos)
                    .icon(bitmapDescriptorFromVector(getActivity(),R.drawable.user_location));
        googleMap.addMarker(marker);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Runnable updateMyLocation = new Runnable() {
        @Override
        public void run() {
            updateMyLocation();
            handler.postDelayed(this, DELAY);
        }
    };

    private void updateMyLocation() {
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
                            UserLocation myLocation = new UserLocation(currentLat, currentLon,true);
                            usersLocationRef.child(firebaseUser.getUid()).setValue(myLocation);
                            lat = currentLat;
                            lon = currentLon;
                        }
                    }
                }
            });
        } else
            MySignal.getInstance().showToast("This location was not captured");
    }

    private boolean isLocationChange(double myLat, double lat, double myLon, double lon) {
        /* making the location less sensitive
        if case the user stay in the same spot for long time
         */
        myLat = Math.floor(myLat * 100000) / 100000;
        lat = Math.floor(lat * 100000) / 100000;
        myLon = Math.floor(myLon * 100000) / 100000;
        lon = Math.floor(lon * 100000) / 100000;

        return myLat != lat && myLon != lon;
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

    private void initFireBase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersLocationRef = MyFireBase.getInstance().
                getReference(MyFireBase.KEYS.USERS_LOCATIONS).child(matchRef);
        if (iCreatedTheRoom)
            usersLocationRef.child(MyFireBase.KEYS.ROOM_CREATOR).setValue(firebaseUser.getUid());
    }

    private void initLPosition() {
        currentUserPos = new LatLng(32.079994 ,34.767316);
    }

}
