package com.github.cuonghuynh.weather.ui.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.cuonghuynh.weather.R;
import com.github.cuonghuynh.weather.utils.ViewWeightAnimationWrapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ChatBotFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnPolylineClickListener, View.OnClickListener, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapFragment";
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static ChatBotFragment newInstance() {
        return new ChatBotFragment();
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_bot, container, false);
        mMapView = view.findViewById(R.id.user_list_map);
        view.findViewById(R.id.btn_full_screen_map).setOnClickListener(this);
        mMapContainer = view.findViewById(R.id.map_container);
        initGoogleMap(savedInstanceState);
        // Inflate the layout for this fragment
        return view;
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

    private void aaa(){
        LatLng barcelona = new LatLng(10.803505, 106.632858);
        mGoogleMap.addMarker(new MarkerOptions().position(barcelona).title("Marker in Barcelona"));

        LatLng madrid = new LatLng(10.824810, 106.602895);
        mGoogleMap.addMarker(new MarkerOptions().position(madrid).title("Marker in Madrid"));

        LatLng zaragoza = new LatLng(10.826960, 106.592981);

        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();


        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyBCHk19VPoZrGO4gA-Az0f4I4s65tnx308")
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, "10.803505, 106.632858", "10.826960, 106.592981");
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
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
        } catch(Exception ex) {
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

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        addMapMarkers();
        mGoogleMap.setOnPolylineClickListener(this);
        aaa();
    }


    private void addMapMarkers() {
                    LatLng latLng = new LatLng(10.762622,  106.660172);
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
        switch (v.getId()){
            case R.id.btn_full_screen_map:{

                if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
                    expandMapAnimation();
                }
                else if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
                    contractMapAnimation();
                }
                break;
            }

        }
    }

    private void contractMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                50);
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

    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                50,
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}