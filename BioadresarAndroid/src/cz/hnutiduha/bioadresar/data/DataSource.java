package cz.hnutiduha.bioadresar.data;

import java.util.Hashtable;
import java.util.TreeSet;

import android.location.Location;

public interface DataSource<T extends LocationInfo> {	
	public LocationInfo getLocation(long id);
	
	public int getSourceId();
	
	public Hashtable<Long, T> getLocationsInRectangle(double lat1, double lon1, double lat2, double lon2);
	
	// public Hashtable<Long, T> getFilteredLocationsInRectangle(double lat1, double lon1, double lat2, double lon2, DataFilter filter);
	
	public TreeSet<T> getAllLocationsSortedByDistance(Location location);
	public TreeSet<T> getBookmarkedLocationsSortedByDistance(Location location);

	public DataFilter<T> getFilter(String query);
}
