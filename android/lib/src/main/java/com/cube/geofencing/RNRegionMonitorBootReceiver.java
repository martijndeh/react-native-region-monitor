package com.cube.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * On Android geofences are cleared after a device restart, so we need to tell the JS side to sync existing fences over to the native module
 */
public class RNRegionMonitorBootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(RNRegionMonitorModule.TAG, "BootReceiver.onReceive");
		//context.startService(new Intent(context, RNRegionTransitionService.class));
	}
}
