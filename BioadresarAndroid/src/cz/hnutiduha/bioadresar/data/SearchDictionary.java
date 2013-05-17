package cz.hnutiduha.bioadresar.data;

import java.util.Hashtable;
import java.util.List;

public interface SearchDictionary {
	public List<String> getSearchOptionList(String prefix);
	
	// to maintain statistics for sorting
	public Hashtable<Long, FarmInfo> searchOptionSelected(String option, int lat1, int lon1, int lat2, int lon2);
	
	public void store();
	public SearchDictionary load();
}
