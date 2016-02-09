package com.byteshaft.namaztime.fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.byteshaft.namaztime.R;
import com.byteshaft.namaztime.services.GeoFenceService;
import com.byteshaft.namaztime.utils.AppGlobals;
import com.byteshaft.namaztime.utils.Helpers;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, ResultCallback<Status>,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMyLocationChangeListener {

    private GoogleMap mMap;
    public PendingIntent mGeofencePendingIntent;
    private MarkerOptions markerOptions;
    private final long sixMonthsInMilliSeconds = 15778476000L;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private GoogleApiClient.Builder mGoogleApiClient;

    public MapsFragment() {
    }

    public static MapsFragment newInstance(int sectionNumber) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity(), GeoFenceService.class);
        return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(AppGlobals.sGeoFenceList);
        return builder.build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMyLocationChangeListener(this);
        mGoogleApiClient = new GoogleApiClient.Builder(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .title("Marked Location")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        final Marker marker = mMap.addMarker(markerOptions);
        if (!marker.isVisible()) {
            marker.setVisible(true);
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.layout_for_dialog, null);
        final EditText editText = (EditText) promptView.findViewById(R.id.editText);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        final EditText nameEditText = (EditText) promptView.findViewById(R.id.edit_text_name);
        alertDialog.setView(promptView);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Enter Radius", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (nameEditText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getContext(), "Enter name for this location",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                AppGlobals.sGeoFenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(nameEditText.getText().toString())

                        .setCircularRegion(
                                latLng.latitude,
                                latLng.longitude,
                                Integer.valueOf(editText.getText().toString())
                        )
                        .setExpirationDuration(sixMonthsInMilliSeconds)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                LocationServices.GeofencingApi.addGeofences(mGoogleApiClient.build(), getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(MapsFragment.this);
                CircleOptions circle = new CircleOptions()
                        .center(new LatLng(latLng.latitude, latLng.longitude))
                        .radius(Integer.valueOf(editText.getText().toString()))
                        .strokeColor(getResources().getColor(R.color.colorPrimary))
                        .fillColor(getResources().getColor(R.color.colorAccent));
                mMap.addCircle(circle);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (marker.isVisible()) {
                    marker.setVisible(false);
                    marker.remove();
                }
                dialog.dismiss();

            }
        });
        alertDialog.create();
        alertDialog.show();


    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.isVisible()) {
            Helpers.removeMarkerDialog(getActivity(), marker, AppGlobals.sGeoFenceList);
            return true;
        }

        return false;
    }

    @Override
    public void onResult(Status status) {

    }

    @Override
    public void onMyLocationChange(Location location) {
        System.out.println(location.getLongitude());
        System.out.println(location.getLatitude());
//        mCurrentLocation = location;
//        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//        Log.i("Longitude", "" + location.getLongitude());
//        Log.i("Latitude", "" + location.getLatitude());
//        if (mMarker != null) {
//            mMarker.remove();
//        }
//        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
//        mMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location").flat(true).
//                icon(BitmapDescriptorFactory.fromResource(R.drawable.location_icon)).anchor(0.5f, 0.5f));
//
//        if (!mapZoom) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
//            Helpers.animateMarker(mMarker, location);
//            mapZoom = true;
//        }

    }
}
