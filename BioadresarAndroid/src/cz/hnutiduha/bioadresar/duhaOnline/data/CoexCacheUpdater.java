package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import cz.hnutiduha.bioadresar.ActivityTracker;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.InvalidDataException;
import cz.hnutiduha.bioadresar.duhaOnline.net.CoexConnector;
import cz.hnutiduha.bioadresar.duhaOnline.net.ConnectionHelper;

public class CoexCacheUpdater extends AsyncTask<Void, CoexLocation, long[]>
{
	CoexCache cache = null;
	long updateTime = 0;
	
	protected CoexCacheUpdater(CoexCache cache)
	{
		this.cache = cache;
		updateTime = System.currentTimeMillis() / 1000L;
	}
	
	private void fillDetails(CoexLocation location, JSONObject details) throws JSONException, InvalidDataException
	{
		// NOTE: we expected basic info to be filed in from parseBasicInfo
		long id = Long.parseLong(details.getString("id"));
		if (id != location.id) { throw new InvalidDataException("data not for this object"); }
		location.type = cache.getCacheLocationTypeId(details.getString("type_title"), details.getInt("type_id"));
		
		location.description = details.getString("description");
		location.contactInfo = new LocationContact(details);
		
		location.products = new LinkedList<EntityWithComment>();
		if (details.has("productList"))
		{
			JSONObject products = details.getJSONObject("productList");
			for (@SuppressWarnings("unchecked")
			Iterator<String> keys = products.keys(); keys.hasNext();)
			{
				String key = keys.next();
				JSONObject product = products.getJSONObject(key);
				
				// TODO: reuse if without comment/predevsim
				EntityWithComment prod = new EntityWithComment(key,
						product.getString("poznamka"), 
						product.getString("predevsim").equals("ano"));
				cache.fillProductId(prod);
				location.products.add(prod);
			}
		}
		
		location.activities = new LinkedList<EntityWithComment>();
		if (details.has("activitiesList"))
		{
			JSONObject activities = details.getJSONObject("activitiesList");
			for (@SuppressWarnings("unchecked")
			Iterator<String> keys = activities.keys(); keys.hasNext();)
			{
				String key = keys.next();
				JSONObject activity = activities.getJSONObject(key);
				
				// TODO: reuse if without comment/predevsim
				EntityWithComment act = new EntityWithComment(key,
						activity.getString("poznamka"), 
						activity.getString("predevsim").equals("ano"));
				cache.fillActivityId(act);
				location.activities.add(act);
				
			}
		}
	}
	
	private static double convertCoordinateString(String raw)
	{
		if (raw.matches("[0-9]+.[0-9]+[\\s]*[EN]"))
		{
			Log.i("data", "cleaning up raw coordinate " + raw);
			return Location.convert(raw.replaceAll("[^0-9.]", ""));
		}
		else if (raw.contains("°"))
		{
			Log.i("data", "converting raw coordinate " + raw);
			String[] parts = raw.split("°");
			double ret = Integer.valueOf(parts[0]);
			parts = parts[1].split("'");
			ret += Integer.valueOf(parts[0]) / 60;
			ret += Double.valueOf(parts[1].replaceAll("[^0-9.]", "")) / 3600;
			Log.i("data", "-> " + ret);
			return ret;
		}
		
		return Location.convert(raw);
	}
	
	private CoexLocation parseBasicInfo(JSONObject basicInfo) throws JSONException
	{
		int typeId = cache.getCacheLocationTypeId(basicInfo.getString("type_title"), basicInfo.getInt("type_id"));		
		CoexLocation location = new CoexLocation(null, basicInfo.getLong("id"),
				basicInfo.getString("title"),
				convertCoordinateString(basicInfo.getString("lat")),
				convertCoordinateString(basicInfo.getString("lng")),
				typeId);
				// or detail -> gps_coords
		
		try
		{
			location.description = basicInfo.getString("description");
		} catch (JSONException ex){}

		return location;
	}
	
	Hashtable<Long, CoexLocation> changedLocations = null;
	
	
	
	protected void doCleanup()
	{
		Log.d("data", "starting cleanup...");
		
		try
		{
			Set<Long> currentCoexLocations = Collections.synchronizedSet(new HashSet<Long>(800));
			Set<Long> cachedLocations = Collections.synchronizedSet(new HashSet<Long>(800));
			
			List<NameValuePair> args = new ArrayList<NameValuePair>(2);
			args.add(new BasicNameValuePair("cmd", "locations"));
			String response = CoexConnector.get(args);
			
			
			JSONArray list = new JSONArray(response);
			Log.d("cleanup", String.format("got %d locations", list.length()));
			for(int i = 0; i < list.length(); i++)
			{
				currentCoexLocations.add(Long.valueOf(list.getJSONObject(i).getLong("id")));
			}
			
			Cursor c = cache.db.query("locations", new String[] { "_id" }, null, null,
					null, null, null);
			c.moveToNext();
			while (!c.isAfterLast())
			{
				cachedLocations.add(Long.valueOf(c.getLong(0)));
				c.moveToNext();
			}
			c.close();
			
			cachedLocations.removeAll(currentCoexLocations);
			if (!cachedLocations.isEmpty())
			{
				StringBuilder bldr = new StringBuilder("_id IN (");
				boolean first = true;

				for (Long id : cachedLocations) {
					if (!first) {
						bldr.append(',');
					}
					bldr.append(id);
					first = false;
				}

				bldr.append(')');
				cache.db.delete("locations", bldr.toString(), null);
				cache.db.delete("locations_fts", bldr.toString(), null);
			}
			
			cache.setLastCleanupTime(updateTime);
			Log.d("net", "cache cleaned with timestamp " + updateTime);
			
		} catch (Exception ex)
		{
			Log.e("net", "failure during cache cleanup", ex);
			return;
		}
	}
	
	@Override
	protected long[] doInBackground(Void... params)
	{
		ActivityTracker.showToastOnActivity(R.string.update_start, Toast.LENGTH_SHORT);
		Log.d("net", "fetching data changes starting from " + this.updateTime);
		
		changedLocations = new Hashtable<Long, CoexLocation>();
		
		if ((updateTime - cache.getLastCleanupTime()) > CoexCache.cleanupInterval && ConnectionHelper.canReadBigData(cache.appContext))
		{
			doCleanup();
		}
		
		// get location list GET http://www.adresarfarmaru.cz/connector?lang=cs&client=www&cmd=locations&changed_from=1398194242
		long[] idList = null;
		List<NameValuePair> args = new ArrayList<NameValuePair>(2);
		args.add(new BasicNameValuePair("cmd", "locations"));
		args.add(new BasicNameValuePair("changed_from", String.valueOf(cache.getLastUpdateTime())));
		try
		{
			String response = CoexConnector.get(args);
			if (response.equalsIgnoreCase("{}"))
			{
				return new long[0];
			}
			
			JSONArray list = new JSONArray(response);
			idList = new long[list.length()];
			Log.d("update", String.format("got %d locations", list.length()));
			for(int i = 0; i < list.length(); i++)
			{
				JSONObject basicInfo = list.getJSONObject(i);
				CoexLocation location = null;
				
				try
				{
					location = parseBasicInfo(basicInfo);
				} catch (Exception ex)
				{
					Log.e("data", "malformed basic info :" + basicInfo.toString(2));
					throw ex;
				}
				
				idList[i] = location.id;
				
				// we use POST, but officially it should be
				// http://www.adresarfarmaru.cz/connector?lang=cs&client=www&cmd=locations&detail=614
				
				args = new ArrayList<NameValuePair>(2);
				args.add(new BasicNameValuePair("cmd", "detail"));
				args.add(new BasicNameValuePair("locid", String.valueOf(location.id)));
				
				JSONObject details = null;
				try 
				{
					details = new JSONObject (CoexConnector.post(args));
					fillDetails(location, details);
				} catch (Exception ex)
				{
					Log.e("data", "failed to get/parse detail for location " + location.id);
					if (details != null) { Log.d("data", details.toString(2));}
					throw ex;
				}
				
				Log.d("update", String.format("updating location %d: %s", location.id, location.name));
				
				publishProgress(location);
			}
			
		} catch (Exception ex)
		{
			Log.e("net", "failure during cache update", ex);
			return null;
		}
		
		return idList;
	}
	
	protected void onProgressUpdate(CoexLocation ... locations)
	{
		// NOTE: we don't get lastChange from coex so we assume now is better than nothing
		boolean updated = cache.updateLocationCache(locations[0], updateTime);
		if (updated)
			changedLocations.put(Long.valueOf(locations[0].id), locations[0]);
	}
	
	protected void onPostExecute(long[] idList)
	{
		if (idList == null) { return; }
		
		//TODO: remove deleted
		//TODO: remove bookmarks for deleted
		
		// NOTE: deleted products/activities left in db
		
		// update timestamp
		cache.setLastUpdateTime(updateTime);
		
		if (changedLocations.size() > 0)
		{
			cache.updateMemoryCache(changedLocations);
			ActivityTracker.showToastOnActivity(R.string.update_finished, Toast.LENGTH_SHORT);
		}
		else
		{
			ActivityTracker.showToastOnActivity(R.string.data_up_to_date, Toast.LENGTH_SHORT);
		}
		
		changedLocations = null;
	}
}
