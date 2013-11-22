/*  This file is part of BioAdresar.
	Copyright 2012 Jiri Zouhar (zouhar@trilobajt.cz), Jiri Prokop

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

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

import cz.hnutiduha.bioadresar.detail.DetailActivity;
import cz.hnutiduha.bioadresar.map.MapActivity;

public class FarmInfo implements OnClickListener{
	
	// NOTE: we ignore location type 
	public static long INVALID_FARM_ID = -1;
	// these are always present
	public long id = INVALID_FARM_ID;
	public String name = null;
	public double lat = Double.NEGATIVE_INFINITY, lon = Double.NEGATIVE_INFINITY;
	
	HnutiduhaFarmDb source = null;
	
	protected String description = null;
	protected FarmContact contact = null;
	protected List<ProductWithComment> products = null;
	protected List<ActivityWithComment> activities = null;
	protected List<Long> categories = null;
	protected Boolean bookmarked = null;
	protected ContainerDistribution containers = null;
	
	private static final int viewTagTarget = 0xdeadbeef;
	private static final String viewTargetMap = "map";
	private static final String viewTargetDetail = "detail";
	
	private Location location = null;
	
	/// crate new (empty) farm
	public FarmInfo()
	{
		
	}
	
	public FarmInfo(HnutiduhaFarmDb source, long id, String name, double lat, double lon)
	{
		this.source = source;
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
	}
	
	public static GeoPoint getGeoPoint(FarmInfo farm) {
		return new GeoPoint((int)(farm.lat * 1E6), (int)(farm.lon * 1E6));
	}
	
	public float getDistance(Location targetLocation) {
		Location destLocation = getLocation();
		return targetLocation.distanceTo(destLocation);
	}
	
	public String getDescription()
	{
		if (description == null)
			source.fillDescription(this);
		
		return description;
	}
	
	public void setFarmContact(FarmContact contact)
	{
		this.contact = contact;
	}
	
	public FarmContact getFarmContact()
	{
		if (contact == null && source != null)
			source.fillContact(this);
		
		return contact;
	}
	
	public List<ProductWithComment> getProducts()
	{
		if (products == null)
			source.fillProducts(this);
		
		return products;
	}
	
	boolean hasContainerDistribution = false;
	
	public ContainerDistribution getContainerDistributionInfo()
	{
		if (containers != null)
			return containers;
		
		getActivities();
		if (hasContainerDistribution == false)
			return null;
		
		return source.fillContainerDistributionInfo(this);
	}
	
	public List<ActivityWithComment> getActivities()
	{
		if (activities == null)
		{
			source.fillActivities(this);
			getActivities();
			for (ActivityWithComment activity : activities)
			{
				if (activity.getName(source).equals("bedýnkový prodej"))
				{
					hasContainerDistribution = true;
				}
			}
		}
		
		return activities;
	}
	
	public List<Long> getCategories()
	{
		if (categories == null)
			source.fillCategories(this);
		
		return categories;
	}
	
	public Location getLocation() {
		if (lat < -90 || lon < -180)
			return null;
		
		if (location == null) {
			this.setLocation(lat,  lon);
		}
		
		return location;
	}
	
	public void setLocation(double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
		
		location = new Location("");
		location.setLatitude(lat);
		location.setLongitude(lon);
	}
	
	public void goToMap(View parent)
	{
		Context context = parent.getContext();
		
		/* Intent map = new Intent(context, MainTabbedActivity.class);
		map.putExtra(MainTabbedActivity.defaultActivityPropertyName, MainTabbedActivity.mapActivityTag);
		*/
		Intent map = new Intent(context, MapActivity.class);
		map.putExtra(MapActivity.mapNodePropertyName, Long.valueOf(id));
		context.startActivity(map);
	}
	
	public void goToDetail(View parent)
	{
		Context context = parent.getContext();
		DetailActivity.setFarm(this);
		Intent detail = new Intent(context, DetailActivity.class);
		context.startActivity(detail);
	}
	
	/*
	public void addToContacts(View parent)
	{
		// Creates a new intent for sending to the device's contacts application
        Intent insertIntent = new Intent(ContactsContract.Intents.Insert.ACTION);

        // Sets the MIME type to the one expected by the insertion activity
        insertIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        insertIntent.putExtra(ContactsContract.Intents.Insert.NAME, this.name);
        
        if (contact.email != null)
        	insertIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email);
        if (contact.phoneNumbers != null && !contact.phoneNumbers.isEmpty())
        	
        for (String phone: contact.phoneNumbers)
        	insertIntent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        
        // TODO: eshop, web
        if (contact.eshop != null)
        	insertIntent.putExtra(, contact.eshop);
        if (contact.web)
        	
        
        lat/lon
        contact.city
        contact.street
        contact.
        
        insertIntent.putExtra(ContactsContract.Intents.Insert.COMPANY, company);
        insertIntent.putExtra(ContactsContract.Intents.Insert.POSTAL, postal);

        // Send out the intent to start the device's contacts app in its add contact activity.
        startActivity(insertIntent);
	}
	*/

	// this is little hack, it doesn't belong to data class, but it serve us nicely :)
	@Override
	public void onClick(View v) {
		Object targetTag = v.getTag(viewTagTarget);
		if (targetTag == null)
		{
			Log.e("gui", "farm set as on click listener without target tag");
			return;
		}
		if (((String)targetTag).equals(viewTargetMap))
			goToMap(v);
		
		else if (((String)targetTag).equals(viewTargetDetail))
			goToDetail(v);
		else
			Log.e("gui", "farm set as on click listener with unknown target tag " + targetTag.toString());
	}
	
	public void fillContainerDistribution(View parent, int layoutId, int placesTextId, int timeLayoutId, int timeTextId, int customTextId)
	{
		// supports?
		ContainerDistribution distr = getContainerDistributionInfo();
		if (distr == null || distr.places == null)
		{
			((LinearLayout)parent.findViewById(layoutId)).setVisibility(LinearLayout.GONE);
			return;
		}
		
		TextView places = (TextView)parent.findViewById(placesTextId);
		// places
		for (String place : distr.places)
		{
			places.append(place);
		}
		
		// time
		if (distr.time == null)
		{
			((LinearLayout)parent.findViewById(timeLayoutId)).setVisibility(LinearLayout.GONE);
		}
		else
		{
			((TextView)parent.findViewById(timeTextId)).setText(distr.time);
		}
		
		// custom
		if (distr.customDistribution)
		{
			((TextView)parent.findViewById(customTextId)).setText("ano");
		}
		else
		{
			((TextView)parent.findViewById(customTextId)).setText("ne");
		}
	}
	
	public void fillInfoToView(View parent, int nameTextId, int categoriesLayoutId, Location distanceFrom, int distanceTextId)
	{	
		// name
		TextView nameView = (TextView) parent.findViewById(nameTextId);
		nameView.setText(this.name);
		
		// icons
		LinearLayout categoryIcons = (LinearLayout) parent.findViewById(categoriesLayoutId);
		Iterator<Long> it = getCategories().iterator();
		ImageView icon;
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20, 1);
	    params.gravity = Gravity.LEFT;
	    Context context = parent.getContext();
	    Resources resources = parent.getResources();
		while (it.hasNext())
		{	
			icon = new ImageView(context);
			icon.setImageResource(resources.getIdentifier("drawable/category_" + it.next(), null, context.getPackageName()));
		    icon.setLayoutParams(params);
		    categoryIcons.addView(icon);
		}
		
		// distance, if required
		if (distanceFrom != null)
		{
			TextView distanceText = (TextView)parent.findViewById(distanceTextId);
			if (distanceText == null)
			{
				Log.e("gui", "requesting distance label but TextView id " + distanceTextId + " is invalid");
				return;
			}
			
			long distance = (long)getDistance(distanceFrom);
			long km = distance / 1000;
			long m = distance % 1000;
			if (km > 0)
				distanceText.setText(String.valueOf(km) + "." + String.valueOf(m / 10) + " km");
			else
				distanceText.setText(String.valueOf(m) + " m");
		}
		
		//FIXME: container distribution
	}
	
	public void setToMapListener(View view)
	{
    	view.setTag(viewTagTarget, viewTargetMap);
    	view.setOnClickListener(this);
	}
	public void setToDetailListener(View view)
	{
		view.setTag(viewTagTarget, viewTargetDetail);
    	view.setOnClickListener(this);
	}
	
	public void setBookmarked(boolean shouldBeBookmarked)
	{
		source.setBookmark(this, shouldBeBookmarked);
		Log.d("db", "setting bookmarked of " + this.id + " to " + shouldBeBookmarked);
		this.bookmarked = Boolean.valueOf(shouldBeBookmarked);
	}
	
	public boolean isBookmarked() {
		if (bookmarked == null)
		{
			Log.d("db", "loading bookmark for " + this.id);
			bookmarked = Boolean.valueOf(source.isBookmarked(this));
		}
		
		return bookmarked.booleanValue();
	}

}
