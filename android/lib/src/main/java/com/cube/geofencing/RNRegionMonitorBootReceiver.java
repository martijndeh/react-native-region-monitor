package com.cube.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cube.geofencing.model.MonitoredRegion;
import com.cube.geofencing.model.PersistableData;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

import static com.cube.geofencing.RNRegionMonitorModule.TAG;

/**
 * On Android geofences are cleared after a device restart, so we restore saved geofences in this receiver
 */
public class RNRegionMonitorBootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "Restore geofences...");

		PersistableData data = PersistableData.load(context);
		GeofenceManager geofenceManager = new GeofenceManager(context);
		List<Geofence> geofences = new ArrayList<>(data.getRegions().size());

		for (MonitoredRegion region: data.getRegions())
		{
			geofences.add(region.createGeofence());
		}

		try
		{
			geofenceManager.addGeofences(geofences, new ResultCallbacks<Status>()
			{
				@Override
				public void onSuccess(@NonNull Status status)
				{
					Log.d(TAG, "Restored geofences");
				}

				@Override
				public void onFailure(@NonNull Status status)
				{
					Log.w(TAG, "Could not restore geofences");
				}
			});
		}
		catch (InterruptedException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}
}
