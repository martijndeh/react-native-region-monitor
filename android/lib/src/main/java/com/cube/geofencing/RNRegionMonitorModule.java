package com.cube.geofencing;

import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

/**
 * Defines the interface available from Javascript in order to manage monitored regions
 */
public class RNRegionMonitorModule extends ReactContextBaseJavaModule
{
	public RNRegionMonitorModule(ReactApplicationContext reactContext)
	{
		super(reactContext);
	}

	@Override
	public String getName()
	{
		return "RNRegionMonitor";
	}

	@ReactMethod
	public void addCircularRegion()
	{
		Log.d("3SC", "addCircularRegion Native");
		getReactApplicationContext().startService(new Intent(getReactApplicationContext(),RNHeadlessTask.class));
	}
}
