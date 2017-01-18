package com.cube.geofencing;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the interface available from Javascript in order to manage monitored regions
 */
public class RNRegionMonitorModule extends ReactContextBaseJavaModule implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	public static final String TAG = "RNRM";

	public static final String TRANSITION_TASK_NAME = "region-monitor-transition";
	public static final String REGION_SYNC_TASK_NAME = "region-monitor-sync";

	private GoogleApiClient googleApiClient;
	private PendingIntent geofencePendingIntent;

	public RNRegionMonitorModule(ReactApplicationContext reactContext)
	{
		super(reactContext);
		googleApiClient = new GoogleApiClient.Builder(reactContext).addConnectionCallbacks(this)
		                                                           .addOnConnectionFailedListener(this)
		                                                           .addApi(LocationServices.API)
		                                                           .build();
		googleApiClient.connect();

		Intent intent = new Intent(reactContext, RNRegionTransitionService.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
		geofencePendingIntent = PendingIntent.getService(getReactApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@ReactMethod
	public void addCircularRegion(ReadableMap location, int radiusMetres, String requestId, final Promise promise)
	{
		Log.d(TAG, "addCircularRegion: " + requestId);
		getReactApplicationContext().startService(new Intent(getReactApplicationContext(), RNRegionTransitionService.class));

		Geofence geofence = new Geofence.Builder().setRequestId(requestId)
		                                          .setCircularRegion(location.getDouble("latitude"), location.getDouble("longitude"), radiusMetres)
		                                          .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
		                                          .setExpirationDuration(Geofence.NEVER_EXPIRE)
		                                          .build();
		GeofencingRequest request = new GeofencingRequest.Builder().addGeofence(geofence).setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER).build();
		LocationServices.GeofencingApi.addGeofences(googleApiClient, request, geofencePendingIntent).setResultCallback(new ResultCallbacks<Status>()
		{
			@Override
			public void onSuccess(@NonNull Status status)
			{
				Log.d(TAG, "addCircularRegion: " + status);
				promise.resolve(null);
			}

			@Override
			public void onFailure(@NonNull Status status)
			{
				Log.d(TAG, "addCircularRegion: " + status);
				promise.reject(Integer.toString(status.getStatusCode()), status.getStatusMessage());
			}
		});
	}

	@javax.annotation.Nullable
	@Override
	public Map<String, Object> getConstants()
	{
		Map<String, String> constants = new HashMap<>();
		constants.put("TRANSITION_TASK_NAME", TRANSITION_TASK_NAME);
		constants.put("REGION_SYNC_TASK_NAME", REGION_SYNC_TASK_NAME);
		return super.getConstants();
	}

	@Override
	public String getName()
	{
		return "RNRegionMonitor";
	}

	@Override
	public void onConnected(@Nullable Bundle bundle)
	{
		Log.d(TAG, "RNRegionMonitor Google client connected");
	}

	@Override
	public void onConnectionSuspended(int i)
	{
		Log.d(TAG, "RNRegionMonitor Google client suspended: " + i);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{
		Log.d(TAG, "RNRegionMonitor Google client failed: " + connectionResult.getErrorMessage());
	}

	@ReactMethod
	public void removeCircularRegion(final String requestId, final Promise promise)
	{
		Log.d(TAG, "removeCircularRegion: " + requestId);
		LocationServices.GeofencingApi.removeGeofences(googleApiClient, Collections.singletonList(requestId)).setResultCallback(new ResultCallbacks<Status>()
		{
			@Override
			public void onSuccess(@NonNull Status status)
			{
				Log.d(TAG, "removeCircularRegion: " + status);
				promise.resolve(null);
			}

			@Override
			public void onFailure(@NonNull Status status)
			{
				Log.d(TAG, "removeCircularRegion: " + status);
				promise.reject(Integer.toString(status.getStatusCode()), status.getStatusMessage());
			}
		});
	}
}
