package cz.hnutiduha.bioadresar.data;

import java.util.Hashtable;

public interface DataSource {	
	public void detach();
	
	public FarmInfo getFarm(long id);
	
	public Hashtable<Long, FarmInfo> getFarmsInRectangle(double lat1, double lon1, double lat2, double lon2);
	
	public Hashtable<Long, FarmInfo> getFilteredFarmsInRectangle(double lat1, double lon1, double lat2, double lon2, DataFilter filter);
	
	public SearchDictionary getDict(); // or getFarms(String keyword, Filter filter);
}
