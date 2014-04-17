package cz.hnutiduha.bioadresar.data;

import com.google.android.maps.GeoPoint;

import cz.hnutiduha.bioadresar.duhaOnline.data.LocationContact;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

public interface LocationInfo {
	
	public static long INVALID_LOCATION_ID = -1;
	
	public DataSource getSource();
	public float getDistance(Location targetLocation);
	
	public GeoPoint getGeoPoint();

	
	public String getDescription();
	public String getName();
	public long getId();
	
	public double getLatitude();
	public double getLongitude();
	
	public LocationContact getContact();
	
	public Location getLocation();
	
	public void goToMap(View parent);
	
	public void editLocation(Context context);
	public void goToDetail(View parent);
	
	public View inflateListView(ViewGroup parent, Location centerOfOurUniverse);
	public Fragment getDetailFragment(Activity parent);
	public View getBaloonView(Context context);
	public void setToMapListener(View view);
	public void setToDetailListener(View view);
	public void setBookmarked(boolean shouldBeBookmarked);
	public boolean isBookmarked();
	
}
