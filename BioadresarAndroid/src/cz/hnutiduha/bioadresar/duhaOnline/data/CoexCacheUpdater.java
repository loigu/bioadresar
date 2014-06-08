package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import cz.hnutiduha.bioadresar.data.InvalidDataException;
import cz.hnutiduha.bioadresar.duhaOnline.net.CoexConnector;

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
	
	private CoexLocation parseBasicInfo(JSONObject basicInfo) throws JSONException
	{
		
		int typeId = cache.getCacheLocationTypeId(basicInfo.getString("type_title"), basicInfo.getInt("type_id"));
		
		CoexLocation location = new CoexLocation(null, basicInfo.getLong("id"),
				basicInfo.getString("title"),
				Double.parseDouble(basicInfo.getString("lat")),
				Double.parseDouble(basicInfo.getString("lng")),
				typeId);
		
		try
		{
			location.description = basicInfo.getString("description");
		} catch (JSONException ex) {}

		return location;
	}

	@Override
	protected long[] doInBackground(Void... params)
	{
		long lastUpdate = 0;
		Cursor c = cache.db.query("config", new String[] { "value" },
				"variable = 'lastUpdated'", null, null, null, null);
		c.moveToNext();

		if (!c.isAfterLast()) {
			lastUpdate = c.getLong(0);
		}
		c.close();
		
		long[] idList = null;
		List<NameValuePair> args = new ArrayList<NameValuePair>(2);
		args.add(new BasicNameValuePair("cmd", "locations"));
		args.add(new BasicNameValuePair("changed_from", String.valueOf(lastUpdate)));
		try
		{
			
			JSONArray list = new JSONArray(CoexConnector.post(args));
			idList = new long[list.length()];
			Log.d("update", String.format("got %d locations", list.length()));
			for(int i = 0; i < list.length(); i++)
			{
				CoexLocation location = parseBasicInfo(list.getJSONObject(i));
				idList[i] = location.id;
				
				args = new ArrayList<NameValuePair>(2);
				args.add(new BasicNameValuePair("cmd", "detail"));
				args.add(new BasicNameValuePair("locid", String.valueOf(location.id)));
				fillDetails(location, new JSONObject (CoexConnector.post(args)));
				Log.d("update", String.format("updating location %d: %s", location.id, location.name));
				
				//publishProgress(location);
				cache.updateLocationCache(location, updateTime);
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
		cache.updateLocationCache(locations[0], updateTime);
	}
	
	protected void onPostExecute(long[] idList)
	{
		if (idList == null) { return; }
		//TODO: remove deleted
		//remove bookmarks for deleted
		
		// NOTE: deleted products/activities left in db
		
		// update timestamp
		ContentValues row = new ContentValues();
		row.put("variable", "lastUpdated");
		row.put("value", updateTime);
		cache.db.insertWithOnConflict("config", null, row, SQLiteDatabase.CONFLICT_REPLACE);
	}
}
