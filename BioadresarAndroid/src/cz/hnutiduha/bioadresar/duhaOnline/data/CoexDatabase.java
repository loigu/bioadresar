package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import cz.hnutiduha.bioadresar.data.ConfigDb;
import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.DataSource;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.InvalidDataException;
import cz.hnutiduha.bioadresar.data.LocationInfoDistanceComparator;

public class CoexDatabase implements DataSource<CoexLocation> {
	
	public static int SOURCE_ID = DataSourceFactory.SOURCE_DUHA_ONLINE;
	private CoexCache cache = null;
	
	// we share bookmarks with offline database
	private static int BOOKMARK_SOURCE_ID = DataSourceFactory.SOURCE_DUHA_OFFLINE;
	private ConfigDb configDb = null;
	Set<Long> bookmarks = null;
	

	private CoexDatabase(Context context) {
		configDb = new ConfigDb(context);
		cache = CoexCache.getDefaultDb(context);
		// bookmarks = configDb.getBookmarks(BOOKMARK_SOURCE_ID);
	}
	
	private static CoexDatabase defaultDb = null;
	
	public static CoexDatabase getDefaultDb(Context context)
	{
		if (defaultDb == null)
		{
			defaultDb = new CoexDatabase(context);
		}
		
		return defaultDb;
	}
	
	protected void fillDetails(CoexLocation location)
	{
		if (location.description == null) { cache.fillDescription(location); }
		if (location.contactInfo == null) { cache.fillContact(location); }
		if (location.activities == null) { cache.fillActivities(location); }
		if (location.products == null) { cache.fillProducts(location); }
	}
	
	private void fillDetails(CoexLocation location, JSONObject details) throws JSONException, InvalidDataException
	{
		long id = Long.parseLong(details.getString("id"));
		if (id != location.id) { throw new InvalidDataException("data not for this object"); }
		
		location.description = details.getString("description");
		location.contactInfo = new LocationContact(details);
		
		location.products = new LinkedList<EntityWithComment>();
		
		JSONObject products = details.getJSONObject("productList");
		for (@SuppressWarnings("unchecked")
		Iterator<String> keys = products.keys(); keys.hasNext();)
		{
			String key = keys.next();
			JSONObject product = products.getJSONObject(key);
			
			location.products.add(new EntityWithComment(key,
					product.getString("poznamka"), 
					product.getString("predevsim").equals("ano")));
		}
		
		location.activities = new LinkedList<EntityWithComment>();
		JSONObject activities = details.getJSONObject("activitiesList");
		for (@SuppressWarnings("unchecked")
		Iterator<String> keys = activities.keys(); keys.hasNext();)
		{
			String key = keys.next();
			JSONObject activity = activities.getJSONObject(key);
			
			location.activities.add(new EntityWithComment(key,
					activity.getString("poznamka"), 
					activity.getString("predevsim").equals("ano")));
		}
	}
	
	private CoexLocation parseBasicInfo(JSONObject basicInfo) throws JSONException
	{
		
		CoexLocation location = new CoexLocation(this, basicInfo.getLong("id"),
				basicInfo.getString("title"),
				Double.parseDouble(basicInfo.getString("lat")),
				Double.parseDouble(basicInfo.getString("lon")),
				basicInfo.getInt("type_id"));
		
		try
		{
			location.description = basicInfo.getString("description");
		} catch (JSONException ex) {}

		return location;
	}
	
	private CoexLocation downloadLocation(long id)
	{
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("cmd", "detail"));
		nameValuePairs.add(new BasicNameValuePair("locid", String.valueOf(id)));
		
		
		return null;
	}

	@Override
	public int getSourceId() {
		return SOURCE_ID;
	}
	
	// NOTE: we get everything from cache

	@Override
	public CoexLocation getLocation(long id) {
		return cache.getLocation(id);
	}
	
	@Override
	public Hashtable<Long, CoexLocation> getLocationsInRectangle(double lat1,
			double lon1, double lat2, double lon2) {
		return cache.getLocationsInRectangle(lat1, lon1, lat2, lon2);
	}
	
	@Override
	public TreeSet<CoexLocation> getAllLocationsSortedByDistance(
			Location location) {
		return cache.getAllFarmsSortedByDistance(location);
	}
	
	@Override
	public DataFilter<CoexLocation> getFilter(String query) {
		return cache.getFilter(query);
	}
	
	private TreeSet<CoexLocation> bookmarkedLocationsCache = null;
	Location sortingLocation = null;
	
	// return state of the farm
	protected void setBookmark(CoexLocation location, boolean bookmarked)
	{
		configDb.setBookmarked(BOOKMARK_SOURCE_ID, location.id, bookmarked);
		
		if (bookmarkedLocationsCache == null)
			return;
		
		boolean inList = bookmarkedLocationsCache.contains(location);
			
		if (!inList && bookmarked)
			bookmarkedLocationsCache.add(location);
		
		if (inList && !bookmarked)
			bookmarkedLocationsCache.remove(location);
	}
	
	protected boolean isBookmarked(CoexLocation location)
	{
		if (bookmarkedLocationsCache != null)
			return bookmarkedLocationsCache.contains(location);
		
		return configDb.isBookmarked(BOOKMARK_SOURCE_ID, location.id);
	}
	
	
	@Override
	public TreeSet<CoexLocation> getBookmarkedLocationsSortedByDistance(
			Location location) {
		
		if (bookmarkedLocationsCache == null)
		{
			List<CoexLocation> bookmarkedLocations = cache.getLocations(configDb.getBookmarks(BOOKMARK_SOURCE_ID));
			LocationInfoDistanceComparator comparator = new LocationInfoDistanceComparator(location);
			
			bookmarkedLocationsCache = new TreeSet<CoexLocation>(comparator);
			for (CoexLocation bookmarkedLocation : bookmarkedLocations)
			{
				bookmarkedLocationsCache.add(bookmarkedLocation);
			}
			
			sortingLocation = location;
		}
		
		if (location != sortingLocation)
		{
			LocationInfoDistanceComparator comparator = new LocationInfoDistanceComparator(location);
			TreeSet<CoexLocation> newCache = new TreeSet<CoexLocation>(comparator);
			for (CoexLocation bookmarkedLocation : bookmarkedLocationsCache)
			{
				newCache.add(bookmarkedLocation);
			}
			
			sortingLocation = location;
			bookmarkedLocationsCache = newCache;
		}
		
		return bookmarkedLocationsCache;
	}
	
	public EntityWithComment[] getActivitiesSortedByName()
	{
		return cache.getActivitiesSortedByName();
	}
	
	public EntityWithComment[] getProductsSortedByName()
	{
		return cache.getProductsSortedByName();
	}
}
