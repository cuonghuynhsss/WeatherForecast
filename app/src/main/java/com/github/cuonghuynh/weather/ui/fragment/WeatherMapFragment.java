package com.github.cuonghuynh.weather.ui.fragment;

import static com.github.cuonghuynh.weather.ui.activity.MainActivity.REQUEST_LOCATION;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.HttpException;
import com.github.cuonghuynh.weather.R;
import com.github.cuonghuynh.weather.model.common.WeatherItem;
import com.github.cuonghuynh.weather.model.currentweather.CurrentWeatherResponse;
import com.github.cuonghuynh.weather.service.ApiService;
import com.github.cuonghuynh.weather.utils.ApiClient;
import com.github.cuonghuynh.weather.utils.ViewWeightAnimationWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class WeatherMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnPolylineClickListener, View.OnClickListener, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "ChatBotFragment";
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Marker mSelectedMarker = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private RelativeLayout mMapContainer;
    private GeoApiContext mGeoApiContext;
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private Geocoder geocoder;
    LatLng currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    Marker userLocationMarker;
    Circle userLocationAccuracyCircle;
    Polygon polygon = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        apiService = ApiClient.getClient().create(ApiService.class);

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation());
            if (mGoogleMap != null) {
                setUserLocationMarker(locationResult.getLastLocation());
            }
        }
    };

    private void draw(List<LatLng> latLngs) {

        PolygonOptions polylineOptions = new PolygonOptions().addAll(latLngs).clickable(true);
        polygon = mGoogleMap.addPolygon(polylineOptions);
        polygon.setFillColor(Color.BLUE);
        polygon.setStrokeColor(Color.BLUE);
    }

    private void setUserLocationMarker(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLocation = latLng;
        if (userLocationMarker == null) {
            //Create a new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder));
            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float) 0.5, (float) 0.5);
            userLocationMarker = mGoogleMap.addMarker(markerOptions);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        } else {
            //use the previously created marker
            userLocationMarker.setPosition(latLng);
            userLocationMarker.setRotation(location.getBearing());
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }

        if (userLocationAccuracyCircle == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
            circleOptions.fillColor(Color.argb(32, 255, 0, 0));
            circleOptions.radius(location.getAccuracy());
            userLocationAccuracyCircle = mGoogleMap.addCircle(circleOptions);
        } else {
            userLocationAccuracyCircle.setCenter(latLng);
            userLocationAccuracyCircle.setRadius(location.getAccuracy());
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public static WeatherMapFragment newInstance() {
        return new WeatherMapFragment();
    }
    ConstraintLayout constraintLayout;
    EditText editText;
    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_map, container, false);
        mMapView = view.findViewById(R.id.user_list_map);
        view.findViewById(R.id.btn_full_screen_map).setOnClickListener(this);
        mMapContainer = view.findViewById(R.id.map_container);
        constraintLayout = view.findViewById(R.id.contaiter_search_wm);
        editText = view.findViewById(R.id.et_search_wm);
        view.findViewById(R.id.btn_current_location).setOnClickListener(view1 -> {
            try {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
            } catch (Exception e) {
                Log.e(TAG, "aa" + e);
            }
        });
        view.findViewById(R.id.draw).setOnClickListener(view1 -> {
            if (polygon != null) {
                polygon.remove();
            }
            if (latLngList != null && latLngList.size() > 0) {
                PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);
                polygon = mGoogleMap.addPolygon(polygonOptions);
                polygon.setFillColor(Color.BLUE);
                polygon.setStrokeColor(Color.BLUE);
            }

        });
        view.findViewById(R.id.clear).setOnClickListener(view1 -> {
            if (polygon != null) {
                polygon.remove();
            }
            for (Marker marker : markerList) marker.remove();
            markerList.clear();
            latLngList.clear();
        });

        view.findViewById(R.id.btn_search_map_weather).setOnClickListener(v->{
            if (!(editText.getText().length() == 0)){
                LatLng latLng= getLatLngFromAddress(editText.getText().toString());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                getCurrentWeatherForLatLon(latLng);
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(getLatLngFromAddress(editText.getText().toString()))
                        .title(editText.getText().toString()));
            }
        });
        initGoogleMap(savedInstanceState);
        // Inflate the layout for this fragment

        return view;
    }

    private CompositeDisposable disposable = new CompositeDisposable();
    private ApiService apiService;

    public void getCurrentWeatherForLatLon(LatLng latLng){
        String apiKey = getResources().getString(R.string.open_weather_map_api);
                    disposable.add(apiService.getCurrentWeatherForLatLon(latLng.latitude, latLng.longitude, apiKey).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<CurrentWeatherResponse>() {
                        @Override
                        public void onSuccess(CurrentWeatherResponse currentWeatherResponse) {

                            for (WeatherItem item : currentWeatherResponse.getWeather())
                                Log.d("cuongcuong", item.getDescription());
                        }

                        @Override
                        public void onError(Throwable e) {
                            try {
                                HttpException error = (HttpException) e;
                            } catch (Exception exception) {
                                e.printStackTrace();
                            }
                        }
                    })

            );
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    private void aaa() {
        LatLng barcelona = new LatLng(10.803505, 106.632858);
        mGoogleMap.addMarker(new MarkerOptions().position(barcelona).title("Marker in Barcelona"));

        LatLng madrid = new LatLng(10.824810, 106.602895);
        mGoogleMap.addMarker(new MarkerOptions().position(madrid).title("Marker in Madrid"));

        LatLng zaragoza = new LatLng(10.826960, 106.592981);

        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();


        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyDH32jqEJwVA0a_M3o-rxbWQOurmap5beQ")
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, "10.803505, 106.632858", "10.826960, 106.592981");
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(15);
            //opts.add(path.get(1)).color(Color.RED).width(15);
            mGoogleMap.addPolyline(opts);
        }

        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zaragoza, 6));
    }


    LocationManager locationManager;
    String latitude, longitude;

    public LatLng getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(getContext());
       List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);

            if (addressList != null) {
                double doubleLat = addressList.get(0).getLatitude();
                double doubleLong = addressList.get(0).getLongitude();

                return new LatLng(doubleLat,doubleLong);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentLocation;
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        addMapMarkers();
        mGoogleMap.setOnPolylineClickListener(this);
        getLatLngFromAddress("Tan Phu, Ho Chi Minh");
        InputMethodManager imm = (InputMethodManager)  getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        //aaa();

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                Marker marker = mGoogleMap.addMarker(markerOptions);
                latLngList.add(latLng);
                markerList.add(marker);
            }
        });


    }

    List<LatLng> sss = new ArrayList<>();

    private void addMapMarkers() {
        LatLng latLng = new LatLng(10.762622, 106.660172);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Ho Chi Minh")
                .snippet("Wonder of the world!");
        mGoogleMap.addMarker(markerOptions);
        //List<Address> addresses = geocoder.getFromLocationName("hochiminh", 1);
//            if (addresses.size() > 0) {
//                Address address = addresses.get(0);
//                LatLng saigon = new LatLng(address.getLatitude(), address.getLongitude());
//                MarkerOptions markerOptions = new MarkerOptions()
//                        .position(saigon)
//                        .title(address.getLocality());
//
//            }
        mGoogleMap.addMarker(markerOptions);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

    }

    private int mMapLayoutState = 0;
    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_full_screen_map: {
                if (mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    constraintLayout.setVisibility(View.GONE);
                } else if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    contractMapAnimation();
                    constraintLayout.setVisibility(View.VISIBLE);

                    InputMethodManager imm = (InputMethodManager)  getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                break;
            }

        }
    }

    private void contractMapAnimation() {
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                70);
        mapAnimation.setDuration(800);

//        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
//        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
//                "weight",
//                0,
//                50);
//        recyclerAnimation.setDuration(800);

        //recyclerAnimation.start();
        mapAnimation.start();
    }

    private void expandMapAnimation() {
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                70,
                100);
        mapAnimation.setDuration(800);

//        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mUserListRecyclerView);
//        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
//                "weight",
//                50,
//                0);
//        recyclerAnimation.setDuration(800);

        //recyclerAnimation.start();
        mapAnimation.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            // you need to request permissions...
        }
        // update user locations every 'LOCATION_UPDATE_INTERVAL'
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}