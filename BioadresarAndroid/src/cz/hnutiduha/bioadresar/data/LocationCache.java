/*  This file is part of BioAdresar.
	Copyright 2012 Jiri Zouhar (zouhar@trilobajt.cz)

    BioAdresar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BioAdresar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BioAdresar.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.hnutiduha.bioadresar.data;

import cz.hnutiduha.bioadresar.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class LocationCache implements LocationListener {
	private static Location location = null;
	private static String locationProvider = LocationManager.PASSIVE_PROVIDER; 
	private static LocationCache listener = null;

	public static void startListener(Context context)
	{
		if (listener == null)
		{
			listener = new LocationCache(context);
			Log.d("gps", "starting gps listener");
		}
	}
	
	public static void stopListener()
	{
		if (listener != null)
			listener.unsubscribe();
		listener = null;
	}
	
	public static boolean hasRealLocation()
	{
		return ! locationProvider.equals(LocationManager.PASSIVE_PROVIDER);
	}
	
	/*
	 * @param regionName name of region to use as default location or null (in case of null, default region will be loaded from preferences)
	 */
	public static Location getDefaultLocation(Context context, String regionName)
	{
	    Log.d("location", "using default location...");
	    Location currentLocation = new Location(LocationManager.PASSIVE_PROVIDER);
	    if (regionName == null)
	    	regionName = PreferenceManager.getDefaultSharedPreferences(context).getString("defaultLocation", "Středočeský");
	    Log.d("gps", "using default location " + regionName);
	    double [] coordinates = DatabaseHelper.getDefaultDb(context).getRegionCoordinates(regionName);
	    if (coordinates == null)
	    	coordinates = new double[] {49.8142789, 14.65985};

		currentLocation.setLatitude(coordinates[0]);
		currentLocation.setLongitude(coordinates[1]);
		currentLocation.setTime(System.currentTimeMillis());
				
		return currentLocation;
	
	}
	
	public static void centerOnLocation(Location loc)
	{
		stopListener();
		LocationCache.location = loc;
		locationProvider = LocationManager.PASSIVE_PROVIDER;
	}
	
	public static String getLocationSource()
	{
		return locationProvider;
	}
		
	public static Location getCenter()
	{
		return location;
	}
	
	LocationManager usedLocationManager = null;
	
	public LocationCache(Context context)
	{
		usedLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		requestGpsIfNeeded(context, usedLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
		
		String provider;
		Criteria criteria = new Criteria();
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		provider = usedLocationManager.getBestProvider(criteria, true);
		if (provider == null)
		{
			Log.w("gps", "no location provider available");
			onLocationChanged(getDefaultLocation(context, null));
			return;
		}
		
		Location lastLocation = usedLocationManager.getLastKnownLocation(provider);
		if (lastLocation == null)
		{
			Log.w("gps", "no known location");
			lastLocation = getDefaultLocation(context, null);
		}
		onLocationChanged(lastLocation);
		
		usedLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60, 20, this);
		usedLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60, 20, this);
		
	}
	
	private void unsubscribe() {
		usedLocationManager.removeUpdates(this);
		usedLocationManager = null;
	}

	public void requestGpsIfNeeded(final Context context, boolean gpsEnabled)
	{
		
		boolean requireGps = false;
		 
		if (!requireGps || gpsEnabled)
			return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// Add the buttons
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   Intent myIntent = new Intent( Settings.ACTION_SECURITY_SETTINGS );
		        	   context.startActivity(myIntent);
		           }
		       });
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // TODO: store never show again to config
		           }
		       });
		builder.setMessage(R.string.enableGps);

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
		// FIXME: wait for the dialog answer
	}
	
	public static void updateLocation(Location newLocation)
	{
		Location oldLocation = LocationCache.location;
		
		if (oldLocation == null || oldLocation.getAccuracy() > newLocation.getAccuracy() ||
				locationProvider == LocationManager.PASSIVE_PROVIDER)
		{
			LocationCache.location = newLocation;
			LocationCache.locationProvider = newLocation.getProvider();
		}
	}

	@Override
	public void onLocationChanged(Location newLocation) {
		LocationCache.updateLocation(newLocation);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}	
}
