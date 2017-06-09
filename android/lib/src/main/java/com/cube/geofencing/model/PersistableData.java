package com.cube.geofencing.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.cube.geofencing.RNRegionMonitorModule.TAG;

/**
 * Created by tim on 19/01/2017.
 */
public class PersistableData
{
	private static final String CACHE_FILE = "geofences.bin";

	@NonNull
	private static Kryo createSerialiser()
	{
		Kryo serialiser = new Kryo();
		serialiser.setDefaultSerializer(TaggedFieldSerializer.class);
		return serialiser;
	}

	@NonNull
	public static PersistableData load(@NonNull Context context)
	{
		Kryo serialiser = createSerialiser();

		String path = context.getFilesDir().getAbsolutePath() + "/" + CACHE_FILE;
		Log.d(TAG, "Attempt to load geofencing state from " + path);

		if (new File(path).exists())
		{
			try
			{
				Input input = new Input(new FileInputStream(path));
				PersistableData data = serialiser.readObject(input, PersistableData.class);
				input.close();

				if (data != null)
				{
					Log.d(TAG, "Successfully loaded geoencing data... " + data);
					return data;
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, "Could not deserialise geofencing data", e);
			}
		}
		else
		{
			Log.i(TAG, "No geofencing data to load");
		}

		return new PersistableData();
	}

	@TaggedFieldSerializer.Tag(1)
	private Map<String, MonitoredRegion> regions = new HashMap<>();

	public void addRegion(@NonNull MonitoredRegion region)
	{
		regions.put(region.getId(), region);
	}

	public void clearRegions()
	{
		regions.clear();
	}

	public Collection<MonitoredRegion> getRegions()
	{
		return Collections.unmodifiableCollection(regions.values());
	}

	public void removeRegion(String id)
	{
		regions.remove(id);
	}

	public void save(@NonNull Context context)
	{
		Kryo serialiser = createSerialiser();

		Log.d(TAG, "Save geofencing state... ");
		String path = context.getFilesDir().getAbsolutePath() + "/" + CACHE_FILE;
		Log.d(TAG, "Saving geofencing state to" + path);
		try
		{
			Output output = new Output(new FileOutputStream(path));
			serialiser.writeObject(output, this);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			Log.e(TAG, "Could not serialise geofencing data", e);
		}
	}
}
