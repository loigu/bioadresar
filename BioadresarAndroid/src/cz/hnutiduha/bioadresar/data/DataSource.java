package cz.hnutiduha.bioadresar.data;

import java.util.Hashtable;
import java.util.TreeSet;

import android.location.Location;

public interface DataSource {	
	public LocationInfo getLocation(long id);
	
	public int getSourceId();
	
	public Hashtable<Long, LocationInfo> getLocationsInRectangle(double lat1, double lon1, double lat2, double lon2);
	
	public Hashtable<Long, LocationInfo> getFilteredLocationsInRectangle(double lat1, double lon1, double lat2, double lon2, DataFilter filter);
	
	public TreeSet<LocationInfo> getAllLocationsSortedByDistance(Location location);
	public TreeSet<LocationInfo> getBookmarkedLocationsSortedByDistance(Location location);
}
