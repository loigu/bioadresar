package cz.hnutiduha.bioadresar.data;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import android.util.Log;

public class IdFilter<T extends LocationInfo> implements DataFilter<T> {

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
	
	public Hashtable<Long, T> prune(Hashtable<Long, T> locations)
	{
		Hashtable<Long, T> ret = new Hashtable<Long, T>();
		if (matchingIds.isEmpty())
		{
			return ret;
		}
		
		for (Map.Entry<Long, T> entry : locations.entrySet())
		{
			if (!matchingIds.contains(entry.getKey()))
			{
				continue;
			}
			ret.put(entry.getKey(), entry.getValue());
		}
		
		return ret;
	}

}
