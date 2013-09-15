package cz.hnutiduha.bioadresar.data;

import java.util.HashSet;
import java.util.Hashtable;

public class IdFilter implements DataFilter {

	private HashSet<Long> matchingIds = null;
	
	public IdFilter(HashSet<Long> ids)
	{
		matchingIds = ids;
	}
	@Override
	public boolean match(FarmInfo info) {
		return matchingIds.contains(info.id);
	}
	
	public void prune(Hashtable<Long, FarmInfo> farms)
	{
		for (Long id : matchingIds)
			farms.remove(id);
	}

}
