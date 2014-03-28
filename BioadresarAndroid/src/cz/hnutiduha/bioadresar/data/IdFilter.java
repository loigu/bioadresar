package cz.hnutiduha.bioadresar.data;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import android.util.Log;

public class IdFilter implements DataFilter {

	private HashSet<Long> matchingIds = null;
	
	public IdFilter(HashSet<Long> ids)
	{
		matchingIds = ids;
		if (ids.isEmpty())
		{
			Log.d("Filter", "no ids in filter");
		}
	}
	@Override
	public boolean match(LocationInfo info) {
		return matchingIds.contains(info.getId());
	}
	
	public Hashtable<Long, LocationInfo> prune(Hashtable<Long, LocationInfo> farms)
	{
		Hashtable<Long, LocationInfo> ret = new Hashtable<Long, LocationInfo>();
		if (matchingIds.isEmpty())
		{
			return ret;
		}
		
		for (Map.Entry<Long, LocationInfo> entry : farms.entrySet())
		{
			if (!matchingIds.contains(entry.getKey()))
			{
				Log.d("Filter", "farm " + entry.getKey() + " removed");
				continue;
			}
			ret.put(entry.getKey(), entry.getValue());
		}
		
		return ret;
	}

}
