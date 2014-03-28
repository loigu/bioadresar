package cz.hnutiduha.bioadresar.data;

import java.util.Hashtable;

public interface DataSource {	
	public FarmInfo getLocation(long id);
	
	public int getSourceId();
	
	public Hashtable<Long, LocationInfo> getLocationsInRectangle(double lat1, double lon1, double lat2, double lon2);
	
	public Hashtable<Long, LocationInfo> getFilteredLocationsInRectangle(double lat1, double lon1, double lat2, double lon2, DataFilter filter);
	
	public SearchDictionary getDict(); // or getFarms(String keyword, Filter filter);
}
