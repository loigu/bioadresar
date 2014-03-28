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

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.detail.DetailActivity;
import cz.hnutiduha.bioadresar.editFarm.EditFarmActivity;
import cz.hnutiduha.bioadresar.map.FarmOverlayView;
import cz.hnutiduha.bioadresar.map.MapActivity;

public class FarmInfo implements OnClickListener, LocationInfo{
	
	// NOTE: we ignore location type 
	// these are always present
	protected long id = INVALID_LOCATION_ID;
	protected String name = null;
	protected double lat = Double.NEGATIVE_INFINITY, lon = Double.NEGATIVE_INFINITY;
	
	protected HnutiduhaFarmDb source = null;
	
	protected String description = null;
	protected FarmContact contact = null;
	protected List<ProductWithComment> products = null;
	protected List<ActivityWithComment> activities = null;
	protected LinkedList<Long> categories = null;
	protected Boolean bookmarked = null;
	protected DeliveryOptions delivery = null;
	
	private static final int viewTagTarget = 0xdeadbeef;
	private static final String viewTargetMap = "map";
	private static final String viewTargetDetail = "detail";
	
	private Location location = null;
	
	public FarmInfo(FarmInfo origin)
	{
		if (origin == null) return;
		
		id = origin.id;
		name = origin.name;
		setLocation(lat, lon);
		source = origin.source;
		description = origin.description;

		if (origin.contact != null)
			contact = new FarmContact(origin.contact);
		
		if (origin.products != null)
		{
			products = new LinkedList<ProductWithComment>();
			for (ProductWithComment product : origin.products)
				products.add(new ProductWithComment(product));
		}
		
		if (origin.activities != null)
		{
			activities = new LinkedList<ActivityWithComment>();
			for (ActivityWithComment activity : origin.activities)
				activities.add(new ActivityWithComment(activity));
		}
		
		if (origin.categories != null)
			categories = (LinkedList<Long>)origin.categories.clone(); 
		
		delivery = new DeliveryOptions(origin.delivery);	
	}
	
	public DataSource getSource()
	{
		return source;
	}
	
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
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public long getId()
	{
		return id;
	}
	
	@Override
	public GeoPoint getGeoPoint() {
		if (location == null) { return null; }
		
		return new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
	}
	
	public float getDistance(Location targetLocation) {
		Location destLocation = getLocation();
		return targetLocation.distanceTo(destLocation);
	}
	
	public String getDescription()
	{
		if (description == null && source != null)
			source.fillDescription(this);
		
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
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
		{
			if (source != null)
			{
				source.fillProducts(this);
			}
			else
			{
				this.products = new LinkedList<ProductWithComment>();
			}
		}
		
		return products;
	}
	
	public void setProducts(List<ProductWithComment> products)
	{
		this.products = products;
	}
	
	boolean hasContainerDistribution = false;
	
	public DeliveryOptions getDeliveryInfo()
	{
		if (delivery != null)
			return delivery;
		
		getActivities();
		if (hasContainerDistribution == false)
			return null;
		
		return source.fillDeliveryOptions(this);
	}
	
	public void setDelieryInfo(DeliveryOptions opts)
	{
		this.delivery = opts;
	}
	
	public List<ActivityWithComment> getActivities()
	{
		if (activities == null)
		{
			if (source != null)
			{
				source.fillActivities(this);
				// FIXME: move this to db
				for (ActivityWithComment activity : activities)
				{
					if (activity.getName(source).equals("bedýnkový prodej"))
					{
						hasContainerDistribution = true;
					}
				}
			}
			else
			{
				activities = new LinkedList<ActivityWithComment>();
			}
		}
		
		return activities;
	}
	
	public void setActivities(List<ActivityWithComment> activities)
	{
		this.activities = activities;
	}
	
	public List<Long> getCategories()
	{
		if (categories == null && source != null)
			source.fillCategories(this);
		
		return categories;
	}
	
	public double getLatitude()
	{
		return lat;
	}
	
	public double getLongitude()
	{
		return lon;
	}
	
	public Location getLocation() {
		if (lat < -90 || lat > 90 || lon < -180 || lon > 180)
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
		map.putExtra(MapActivity.EXTRA_LOCATION_ID, id);
		map.putExtra(MapActivity.EXTRA_SOURCE, source.getSourceId());
		
		context.startActivity(map);
	}
	
	@Override
	public void editLocation(Context context)
	{
		Intent detail = new Intent(context, EditFarmActivity.class);
		detail.putExtra(EditFarmActivity.EXTRA_ID, this.id);
		context.startActivity(detail);
	}
	
	public void goToDetail(View parent)
	{
		Context context = parent.getContext();
		Intent detail = new Intent(context, DetailActivity.class);
		detail.putExtra(DetailActivity.EXTRA_ID, this.id);
		detail.putExtra(DetailActivity.EXTRA_SOURCE, source.getSourceId());
		context.startActivity(detail);
	}

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
	
	public void fillDeliveryOptions(View parent, int layoutId, int placesLayoutId, int placesTextId, int customTextId)
	{
		// supports?
		DeliveryOptions distr = getDeliveryInfo();
		if (distr == null || (distr.placesWithTime == null && distr.customDistribution == false))
		{
			((LinearLayout)parent.findViewById(layoutId)).setVisibility(LinearLayout.GONE);
			return;
		}
		
		if (distr.placesWithTime != null && distr.placesWithTime.length != 0)
		{
			TextView places = (TextView)parent.findViewById(placesTextId);
			// places
			boolean first = true;
			for (String place : distr.placesWithTime)
			{
				if (!first)
					places.append("\n");
				places.append(place);
				first = false;
			}
		}
		else
		{
			((LinearLayout)parent.findViewById(placesLayoutId)).setVisibility(LinearLayout.GONE);
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
	
	private void fillListView(View parent, int nameTextId, Location centerOfOurUniverse, int distanceTextId)
	{	
		// name
		TextView nameView = (TextView) parent.findViewById(nameTextId);
		nameView.setText(this.name);
				
		// distance, if required
		if (centerOfOurUniverse != null)
		{
			TextView distanceText = (TextView)parent.findViewById(distanceTextId);
			if (distanceText == null)
			{
				Log.e("gui", "requesting distance label but TextView id " + distanceTextId + " is invalid");
				return;
			}
			
			float distanceMeters = getDistance(centerOfOurUniverse);
			long km = (long) (distanceMeters / 1000);
			long m = (long) (distanceMeters % 1000);
			if (km > 0)
				distanceText.setText(String.valueOf(km) + "." + String.valueOf(m / 10) + " km");
			else
				distanceText.setText(String.valueOf(m) + " m");
		}
	}


	@Override
	public View inflateListView(ViewGroup parent, Location centerOfOurUniverse) {
    	LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View v = inflater.inflate(R.layout.farm_list_item_layout, null);
		
		LinearLayout toDetail = (LinearLayout)v.findViewById(R.id.toDetailArea);
		setToDetailListener(toDetail);
		LinearLayout toMap = (LinearLayout)v.findViewById(R.id.toMapArea);
		setToMapListener(toMap);
		
		fillListView(v, R.id.farmName, centerOfOurUniverse, R.id.distance);
		
		return v;
	}

	@Override
	public Fragment getDetailFragment(Activity parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getBaloonView(Context context) {
		View baloon = new FarmOverlayView(context, this);
		
		if (baloon != null)
		{
			TextView nameView = (TextView) baloon.findViewById(R.id.balloon_item_title);
			nameView.setText(name);
		}
		
		return baloon;
	}

}
