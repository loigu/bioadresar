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
	public boolean match(FarmInfo info) {
		return matchingIds.contains(info.id);
	}
	
	public Hashtable<Long, FarmInfo> prune(Hashtable<Long, FarmInfo> farms)
	{
		Hashtable<Long, FarmInfo> ret = new Hashtable<Long, FarmInfo>();
		if (matchingIds.isEmpty())
		{
			return ret;
		}
		
		for (Map.Entry<Long, FarmInfo> entry : farms.entrySet())
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
