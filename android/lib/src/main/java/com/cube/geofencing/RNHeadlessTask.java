package com.cube.geofencing;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

/**
 * Created by tim on 18/01/2017.
 */
public class RNHeadlessTask extends HeadlessJsTaskService
{
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d("3SC", "RNHeadlessTask start");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	@Nullable
	protected HeadlessJsTaskConfig getTaskConfig(Intent intent)
	{
		Log.d("3SC", "RNHeadlessTask getTaskConfig");
		return new HeadlessJsTaskConfig("headless_test", Arguments.createMap(), 0, true);
	}
}
