package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


import android.content.Context;
import android.location.Location;
import cz.hnutiduha.bioadresar.data.ConfigDb;
import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.DataSource;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationInfoDistanceComparator;

public class CoexDatabase implements DataSource<CoexLocation> {
	
	public static int SOURCE_ID = DataSourceFactory.SOURCE_DUHA_ONLINE;
	private CoexCache cache = null;
	
	// we share bookmarks with old offline database
	private static int BOOKMARK_SOURCE_ID = DataSourceFactory.SOURCE_DUHA_OFFLINE;
	private ConfigDb configDb = null;
	Set<Long> bookmarks = null;
	

	private CoexDatabase(Context context) {
		configDb = new ConfigDb(context);
		cache = CoexCache.getDefaultDb(context, this);
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
		TreeSet<EntityWithComment> activities = cache.getActivitiesSortedByName();
		EntityWithComment array[] = new EntityWithComment[activities.size()];
		return activities.toArray(array);
	}
	
	public EntityWithComment[] getProductsSortedByName()
	{
		TreeSet<EntityWithComment> products = cache.getProductsSortedByName();
		EntityWithComment array[] = new EntityWithComment[products.size()];
		return products.toArray(array);
	}
}
