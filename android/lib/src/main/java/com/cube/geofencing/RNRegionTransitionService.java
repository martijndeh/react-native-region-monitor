package com.cube.geofencing;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import static com.cube.geofencing.RNRegionMonitorModule.TAG;

/**
 * Service launched when a region transition occurs
 */
public class RNRegionTransitionService extends HeadlessJsTaskService
{
	@Override
	@Nullable
	protected HeadlessJsTaskConfig getTaskConfig(Intent intent)
	{
		if (intent.getExtras() == null)
		{
			return null;
		}

		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

		if (geofencingEvent.hasError())
		{
			// Suppress geofencing event with error
			Log.d(TAG, "Suppress geocoding event with error");
			return null;
		}

        Location triggeringLocation = geofencingEvent.getTriggeringLocation();
        WritableMap location = Arguments.createMap();
        location.putDouble("latitude", triggeringLocation.getLatitude());
		location.putDouble("longitude", triggeringLocation.getLongitude());

		WritableMap region = Arguments.createMap();
		region.putString("identifier", geofencingEvent.getTriggeringGeofences().get(0).getRequestId());

		WritableArray regionIdentifiers = Arguments.createArray();
		for (Geofence triggered: geofencingEvent.getTriggeringGeofences())
		{
			regionIdentifiers.pushString(triggered.getRequestId());
		}
		region.putArray("identifiers", regionIdentifiers);

		WritableMap jsArgs = Arguments.createMap();
		jsArgs.putMap("location", location);
		jsArgs.putMap("region", region);

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        jsArgs.putBoolean("didEnter", geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER);
		jsArgs.putBoolean("didExit", geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT);
		//jsArgs.putBoolean("didDwell", geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_DWELL);
        jsArgs.putDouble("accuracy", triggeringLocation.getAccuracy());
        jsArgs.putDouble("bearing", triggeringLocation.getBearing());

        Log.d(TAG, "Report geofencing event to JS: " + jsArgs);
		return new HeadlessJsTaskConfig(RNRegionMonitorModule.TRANSITION_TASK_NAME, jsArgs, 0, true);
	}
}
