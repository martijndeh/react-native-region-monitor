package com.cube.geofencing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.cube.geofencing.RNRegionMonitorModule.TAG;

/**
 * Created by tim on 19/01/2017.
 */
public class GeofenceManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private GoogleApiClient googleApiClient;
	private PendingIntent geofencePendingIntent;
	private CountDownLatch countDownLatch = new CountDownLatch(1);

	public GeofenceManager(@NonNull Context context)
	{
		googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this)
		                                                      .addOnConnectionFailedListener(this)
		                                                      .addApi(LocationServices.API)
		                                                      .build();
		googleApiClient.connect();

		Intent intent = new Intent(context, RNRegionTransitionService.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
		geofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onConnected(@Nullable Bundle bundle)
	{
		countDownLatch.countDown();
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
		countDownLatch.countDown();
		Log.d(TAG, "RNRegionMonitor Google client failed: " + connectionResult.getErrorMessage());
	}

	public void addGeofences(@NonNull List<Geofence> geofences, @NonNull ResultCallbacks<Status> callback) throws InterruptedException
	{
		countDownLatch.await();
		GeofencingRequest request = new GeofencingRequest.Builder().addGeofences(geofences).setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER).build();
		LocationServices.GeofencingApi.addGeofences(googleApiClient, request, geofencePendingIntent).setResultCallback(callback);
	}

	public void clearGeofences(@NonNull ResultCallbacks<Status> callback) throws InterruptedException
	{
		countDownLatch.await();
		LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofencePendingIntent).setResultCallback(callback);
	}

	public void removeGeofence(@NonNull String id, @NonNull ResultCallbacks<Status> callback) throws InterruptedException
	{
		countDownLatch.await();
		LocationServices.GeofencingApi.removeGeofences(googleApiClient, Collections.singletonList(id)).setResultCallback(callback);
	}
}
