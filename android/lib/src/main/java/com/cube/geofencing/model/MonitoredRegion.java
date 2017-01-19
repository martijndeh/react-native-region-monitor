package com.cube.geofencing.model;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.google.android.gms.location.Geofence;

/**
 * Created by tim on 19/01/2017.
 */
public class MonitoredRegion
{
	@TaggedFieldSerializer.Tag(1)
	private String id;
	@TaggedFieldSerializer.Tag(2)
	private double latitude;
	@TaggedFieldSerializer.Tag(3)
	private double longitude;
	@TaggedFieldSerializer.Tag(4)
	private int radius;

	public MonitoredRegion()
	{}

	public MonitoredRegion(String id, double latitude, double longitude, int radius)
	{
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
	}

	public Geofence createGeofence()
	{
		return new Geofence.Builder().setRequestId(id)
		                             .setCircularRegion(latitude, longitude, radius)
		                             .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
		                             .setExpirationDuration(Geofence.NEVER_EXPIRE)
		                             .build();
	}

	public String getId()
	{
		return id;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public int getRadius()
	{
		return radius;
	}
}
