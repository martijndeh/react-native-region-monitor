package com.cube.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RNRegionMonitorBootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d("3SC", "RNHeadlessTask boot receiver");
		context.startService(new Intent(context, RNHeadlessTask.class));
	}
}
