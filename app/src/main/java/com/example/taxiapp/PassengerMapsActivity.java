package com.example.taxiapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PassengerMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int CHECK_SETTINGS_CODE = 111;
    private static final int REQUEST_LOCATION_PERMISSION = 222;
    private Button signOutButton;
    private Button settingsButtonMaps;
    private Button searchButton;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    private Marker driverMarker;


    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private DatabaseReference driversGeoFire;
    private DatabaseReference nearestDriverLocation;
    private  int radius = 1;
    private boolean isDriverFound = false;
    private String nearestIdOfDriver;

    private boolean isLocationUpdatesActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        searchButton = findViewById(R.id.SearchId);
        signOutButton=findViewById(R.id.SignOutId);
        settingsButtonMaps=findViewById(R.id.SettingsId);
        driversGeoFire = FirebaseDatabase.getInstance().getReference().child("driversGeoFire");

        buildLocationRequest();
        buildLocationCallBack();
        buildLocationSettingsRequest();
        startLocationUpdates();
        auth=FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser();



        // ОБРАБАТЫВАЕМ НАЖАТИЕ
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            searchButton.setText("Looking for drivers...");
            searchButton.setBackgroundColor(Color.GREEN);

            gettingNearestTaxi();
            }
        });


        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                signOutPassenger();
            }
        });

    }

// ПОЛУЧАЕМ ID БЛИЖАЙШЕГО ВОДИТЕЛЯ
    private void gettingNearestTaxi() {
GeoFire geoFire = new GeoFire(driversGeoFire);
// Теперь делаем геоЗапрос через наш объект по местоположению пассажира и радиусу
 GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(),currentLocation.getLongitude()),radius);

 geoQuery.removeAllListeners(); // Чтобы приложение не упало из за рекурсии удаляем прошлые Листнеры
 geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
     @Override
     // Метод выполняется когда уже найдена ближ.локация
     public void onKeyEntered(String key, GeoLocation location) {
        if (!isDriverFound){
            isDriverFound = true;
            nearestIdOfDriver = key;
            getNearestDriverLocation();
        }
     }

     @Override
     public void onKeyExited(String key) {

     }

     @Override
     public void onKeyMoved(String key, GeoLocation location) {

     }

     @Override
     // Если не нашли то рекурсивно вызываем заново поиск увеличивая радиус
     public void onGeoQueryReady() {
         if (!isDriverFound){
             radius++;
             gettingNearestTaxi(); // рекурсия
         }
     }

     @Override
     public void onGeoQueryError(DatabaseError error) {

     }
 });

    }

    private void getNearestDriverLocation() {
        searchButton.setText("Сontact the driver...");
        nearestDriverLocation = FirebaseDatabase.getInstance().getReference().child("driversGeoFire").child(nearestIdOfDriver).child("l");
        nearestDriverLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    // getValue возвращает список поэтому...
                    List<Object> driverLocParam = (List<Object>) snapshot.getValue();

                    double lati = 0;
                    double longi = 0;

                    if (driverLocParam.get(0) != null) {
                        lati = Double.parseDouble(driverLocParam.get(0).toString());
                    }
                    if (driverLocParam.get(1) != null) {
                        longi = Double.parseDouble(driverLocParam.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(lati,longi);
                    // Удаление старого маркера если он существует
                    if (driverMarker != null){
                        driverMarker.remove();
                    }

                    Location driverLocation = new Location("");
                    driverLocation.setLatitude(lati);
                    driverLocation.setLongitude(longi);
                    float distance = driverLocation.distanceTo(currentLocation);
                    searchButton.setText("Waiting time "+ distance/70 + " min");

                    // Добавление маркера
                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your driver"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void signOutPassenger() {
        // Получаем id текущего пользователя и убираем локейшен
        String passengerUserId = currentUser.getUid();
        DatabaseReference passengers = FirebaseDatabase.getInstance().getReference().child("passengers");
        GeoFire geoFire = new GeoFire(passengers);
        geoFire.removeLocation(passengerUserId);

        Intent intent = new Intent(PassengerMapsActivity .this,Choose_Mode_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //NEW - Activity находит свой таск, но не ищет в этом таске себя, а просто создает новое Activity сверху.
        //CLEAR - таск для вызываемого Activity будет очищен, а вызываемое Activity станет в нем корневым.
        startActivity(intent);
        finish();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(currentLocation != null) {

            LatLng passengerLocation = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(passengerLocation).title("Passenger"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(passengerLocation));

        }
    }

    private void startLocationUpdates() {
        isLocationUpdatesActive = true;
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (ActivityCompat.checkSelfPermission(PassengerMapsActivity .this,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                                .checkSelfPermission(PassengerMapsActivity .this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        fusedLocationProviderClient.
                                requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        updateLocationUI();
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode){
                    case LocationSettingsStatusCodes
                            .RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.
                                    startResolutionForResult(PassengerMapsActivity .this,CHECK_SETTINGS_CODE);
                        } catch (IntentSender.SendIntentException sie){
                            sie.printStackTrace();
                        } break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String message = "Check your settings";
                        Toast.makeText(PassengerMapsActivity .this,message,Toast.LENGTH_SHORT).show();
                        isLocationUpdatesActive = false;
                }
                updateLocationUI();
            }
        });
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_SETTINGS_CODE:
                switch (resultCode) {
                    case Activity
                            .RESULT_OK:
                        Log.d("DriverMaps", "User has agreed to change location" + "settings");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d("DriverMapsActive", "User has not agreed");
                        isLocationUpdatesActive = false;
                        updateLocationUI();
                        break;
                } break;
        }
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest=builder.build();
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
    }

    private void updateLocationUI() {
        if(currentLocation != null) {
            LatLng driverLocation = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            mMap.addMarker(new MarkerOptions().position(driverLocation).title("Passenger"));

            // Получаем id текущего пользователя
            String passengerUserId = currentUser.getUid();
            DatabaseReference passengersGeoFire = FirebaseDatabase.getInstance().getReference().child("passengersGeoFire");

            GeoFire geoFire = new GeoFire(passengersGeoFire);
            geoFire.setLocation(passengerUserId,new GeoLocation(currentLocation.getLatitude(),currentLocation.getLongitude()));
        }
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        isLocationUpdatesActive = false;
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLocationUpdatesActive && checkLocationPermission()) {
            startLocationUpdates();
        } else if (!checkLocationPermission()){
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,Manifest.permission.ACCESS_FINE_LOCATION
        );

        if(shouldProvideRationale){
            showSnackBar("Location permission is needed for" + "app functionality", "Ok", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat
                            .requestPermissions(PassengerMapsActivity .this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
                }
            });

        } else {
            ActivityCompat
                    .requestPermissions(PassengerMapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
        }
    }

    private void showSnackBar(final String mainText, final String action, View .OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                mainText,Snackbar.LENGTH_INDEFINITE).setAction(action,listener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode==REQUEST_LOCATION_PERMISSION){
            if (grantResults.length<= 0){
                Log.d("onRequestPerm","was canceled");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isLocationUpdatesActive){
                    startLocationUpdates();
                }
            }
        }
    }

    private boolean checkLocationPermission() {
        int permissionState = ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }




}