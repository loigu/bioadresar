package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.util.ArrayList;
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
import cz.hnutiduha.bioadresar.data.LocationInfo;

public class CoexDatabase implements DataSource {
	
	// we share bookmarks with offline database
	public static int SOURCE_ID = DataSourceFactory.SOURCE_DUHA_ONLINE;
	private static int BOOKMARK_SOURCE_ID = DataSourceFactory.SOURCE_DUHA_OFFLINE;
	private ConfigDb configDb = null;
	private static String DB_PATH = "/data/data/cz.hnutiduha.bioadresar/databases/";
	Set<Long> bookmarks = null;
	

	public CoexDatabase(Context context) {
		configDb = new ConfigDb(context);
		bookmarks = configDb.getBookmarks(BOOKMARK_SOURCE_ID);
	}
	
	protected void fillDetails(CoexLocation location)
	{
		
	}
	
	private void fillDetails(CoexLocation location, JSONObject details) throws JSONException, InvalidDataException
	{
		long id = Long.parseLong(details.getString("id"));
		if (id != location.id) { throw new InvalidDataException("data not for this object"); }
		
		location.description = details.getString("description");
		location.contactInfo = new LocationContact(details);
		
		location.products = new LinkedList<EntityWithComment>();
		
		JSONObject products = details.getJSONObject("productList");
		for (Iterator<String> keys = products.keys(); keys.hasNext();)
		{
			String key = keys.next();
			JSONObject product = products.getJSONObject(key);
			
			location.products.add(new EntityWithComment(key,
					product.getString("poznamka"), 
					product.getString("predevsim").equals("ano")));
		}
		
		location.activities = new LinkedList<EntityWithComment>();
		JSONObject activities = details.getJSONObject("activitiesList");
		for (Iterator<String> keys = activities.keys(); keys.hasNext();)
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
			location.rating = Integer.parseInt(basicInfo.getString("rating"));
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
	public CoexLocation getLocation(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSourceId() {
		// TODO Auto-generated method stub
		return DataSourceFactory.SOURCE_DUHA_ONLINE;
	}

	@Override
	public Hashtable<Long, LocationInfo> getLocationsInRectangle(double lat1,
			double lon1, double lat2, double lon2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hashtable<Long, LocationInfo> getFilteredLocationsInRectangle(
			double lat1, double lon1, double lat2, double lon2,
			DataFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	// TODO: load
	private List<CoexLocation> bookmarkedFarmsCache = null;
	
	// return state of the farm
	protected void setBookmark(CoexLocation location, boolean bookmarked)
	{
		configDb.setBookmarked(BOOKMARK_SOURCE_ID, location.id, bookmarked);
		
		if (bookmarkedFarmsCache == null)
			return;
		
		boolean inList = bookmarkedFarmsCache.contains(location);
			
		if (!inList && bookmarked)
			bookmarkedFarmsCache.add(location);
		
		if (inList && !bookmarked)
			bookmarkedFarmsCache.remove(location);
	}
	
	protected boolean isBookmarked(CoexLocation location)
	{
		if (bookmarkedFarmsCache != null)
			return bookmarkedFarmsCache.contains(location);
		
		return configDb.isBookmarked(BOOKMARK_SOURCE_ID, location.id);
	}

	@Override
	public TreeSet<LocationInfo> getAllLocationsSortedByDistance(
			Location location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeSet<LocationInfo> getBookmarkedLocationsSortedByDistance(
			Location location) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public EntityWithComment[] getProductsSortedByName()
	{
		return null;
	}
	
	public EntityWithComment[] getActivitiesSortedByName()
	{
		return null;
	}
	
}
