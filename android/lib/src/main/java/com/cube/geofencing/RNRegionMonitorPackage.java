package com.cube.geofencing;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Collections;
import java.util.List;

public class RNRegionMonitorPackage implements ReactPackage
{
	@Override
	public List<NativeModule> createNativeModules(ReactApplicationContext reactContext)
	{
		return Collections.<NativeModule>singletonList(new RNRegionMonitorModule(reactContext));
	}

	@Override
	public List<Class<? extends JavaScriptModule>> createJSModules()
	{
		return Collections.emptyList();
	}

	@Override
	public List<ViewManager> createViewManagers(ReactApplicationContext reactContext)
	{
		return Collections.emptyList();
	}
}


