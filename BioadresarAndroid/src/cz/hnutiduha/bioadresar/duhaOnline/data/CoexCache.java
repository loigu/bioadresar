package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.IdFilter;
import cz.hnutiduha.bioadresar.data.LocationInfoDistanceComparator;
import cz.hnutiduha.bioadresar.util.Alphabet;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;
import android.util.SparseArray;

public class CoexCache extends SQLiteOpenHelper {
	private static String DB_NAME = "adresarfarmaruCache";
	
	private static CoexCache defaultDb = null;

	private static Context appContext = null;
	
	protected SQLiteDatabase db;
		
	private static final int databaseVersion = 1;
	
	private CoexCache(Context context) {
		super(context, context.getFilesDir().getPath() + File.separator + DB_NAME, null, databaseVersion);
	}
	
	protected static CoexCache getDefaultDb(Context context)
	{
		if (defaultDb == null)
		{
			try {
				String dbPath = context.getFilesDir().getPath() + File.separator + DB_NAME;
				appContext = context;
				defaultDb = new CoexCache(context);
				defaultDb.createDb(dbPath, context);
				defaultDb.openDb(dbPath, SQLiteDatabase.OPEN_READWRITE);
			} catch (IOException e) {
				Log.e("db", "error opening db " + e.toString());
			}
		}
		return defaultDb; 
	}
	
	protected static void closeDefaultDb()
	{
		if (defaultDb != null)
			defaultDb.close();
		defaultDb = null;
	}
	
	private void runFixtures (Context context, int fromVersion)
	{
	}
	
	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	private void createDb(String dbPath, Context context) throws IOException {
		int dbVersion = checkDb(dbPath);
		if (dbVersion == 0)
		{
			File db = new File(dbPath);
			if (db.exists())
			{
				db.delete();
				runFixtures(context, dbVersion);
			}
			
			// WTF is this? this.getReadableDatabase();

			try {
				copyDb(dbPath);
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
			Log.d("db", "coex cache database installed");
		}
		
		openDb(dbPath, SQLiteDatabase.OPEN_READWRITE);
		db.execSQL("INSERT OR REPLACE INTO config(variable, value) VALUES('databaseVersion', " + String.valueOf(databaseVersion) + ");");
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time application is opened.
	 * 
	 * @return database version
	 */
	private int checkDb(String dbPath) {
		SQLiteDatabase checkDB = null;

		try {
			checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			Log.d("db", "database doesn't exist");
			return 0;
		}
		
		int version = 0;
		try {
			String[] columns = new String[] { "value" };
			Cursor c = checkDB.query("config", columns, "variable='databaseVersion'", null, null, null, null);
			
			
			c.moveToNext();
			if(!c.isAfterLast())
			{
				version = Integer.decode(c.getString(0));
				Log.d("db", "found old db version " + version);
			}
			
			c.close();
		} catch (Exception e){
			Log.e("db", "can't get db version", e.fillInStackTrace());
		}
	
		checkDB.close();

		return version;
	}

	/**
	 * Copies database from local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDb(String targetPath) throws IOException {
		InputStream myInput = appContext.getAssets().open(DB_NAME);
		OutputStream myOutput = new FileOutputStream(targetPath);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	private void openDb(String dbPath, int flags) throws SQLException {
		db = SQLiteDatabase.openDatabase(dbPath, null, flags);
	}

	@Override
	public synchronized void close() {
		if (db != null)
			db.close();

		super.close();
	}
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	protected DataFilter<CoexLocation> getFilter(String searchString) {
		if (searchString == null) return null;

		HashSet<Long> idsFound = new HashSet<Long>();

		searchString = DatabaseUtils.sqlEscapeString(Alphabet.removeAccents(searchString));


		Cursor c = db.query("locations_fts", new String[] {"_id"}, " locations_fts MATCH " + searchString , null, null, null, null);

		c.moveToNext();
		while (!c.isAfterLast()) {
			Log.d("db", "query found " + c.getLong(0));
			idsFound.add(Long.valueOf(c.getLong(0)));
			c.moveToNext();
		}
		c.close();
		
		return new IdFilter<CoexLocation>(idsFound);
	}

	private static final String[] locationBasicColumns = { "_id", "name", "gpsLatitude", "gpsLongtitude", "type"};
	
	// expects cursor with valid entry created with columns CoexLocationColumns
	private CoexLocation fromCursor(Cursor c)
	{
		return new CoexLocation(null, c.getLong(0), c.getString(1), c.getDouble(2), c.getDouble(3), c.getInt(4));
	}
	
	protected Hashtable<Long, CoexLocation> getLocationsInRectangle(double lat1, double lon1, double lat2, double lon2) {
		
		String selection = "gpsLatitude >= " + Math.min(lat1, lat2) +
				" AND gpsLongtitude >= " + Math.min(lon1, lon2) +
				" AND gpsLatitude <= " + Math.max(lat1, lat2) +
				" AND gpsLongtitude <= " + Math.max(lon1, lon2);

		Cursor c = db.query("locations", locationBasicColumns, selection, null, null, null, "gpsLatitude, gpsLongtitude");
		Hashtable<Long, CoexLocation> result = new Hashtable<Long, CoexLocation>();
		
		c.moveToNext();
		CoexLocation locationInfo;
		while (!c.isAfterLast()) {
			locationInfo = fromCursor(c);
			
			result.put(locationInfo.id, locationInfo);
			c.moveToNext();
		}
		c.close();
		
		return result;
	}
	
	private List<CoexLocation> allLocationsList = null;
	private Hashtable<Long, CoexLocation> allLocationsHash = null;
	
	private List<CoexLocation> getAllLocations()
	{
		if (allLocationsList != null)
			return allLocationsList;
		
		allLocationsHash = new Hashtable<Long, CoexLocation>();
		
		LinkedList<CoexLocation> res = new LinkedList<CoexLocation>();
		
		Cursor c = db.query("locations", locationBasicColumns, null, null, null, null, "gpsLatitude, gpsLongtitude");
		c.moveToNext();
		CoexLocation CoexLocation;
		while (!c.isAfterLast()) {
			CoexLocation = fromCursor(c);
			
			res.add(CoexLocation);
			allLocationsHash.put(Long.valueOf(CoexLocation.id), CoexLocation);
			c.moveToNext();
		}
		c.close();
		
		allLocationsList = res;
		
		return res;
	}
	
	protected CoexLocation getLocation(long id) {
		CoexLocation ret = null;
		
		if (allLocationsHash != null)
		{
			ret = allLocationsHash.get(Long.valueOf(id));
			if (ret != null) return ret;
		}
		
		Cursor c = db.query("locations", locationBasicColumns, "_id = " + id, null, null, null, null);
		c.moveToNext();
		if (!c.isAfterLast()) {
			ret = this.fromCursor(c);
		}
		c.close();
		
		return ret;		
	}
	
	private Location lastLocation = null;
	private TreeSet<CoexLocation> farmsSortedFromLastLocation = null;
	
	protected synchronized TreeSet<CoexLocation> getAllFarmsSortedByDistance(Location location) {
		
		if (location.equals(lastLocation) && farmsSortedFromLastLocation != null)
			return farmsSortedFromLastLocation;
		
		LocationInfoDistanceComparator comparator = new LocationInfoDistanceComparator(location);
		TreeSet<CoexLocation> result = new TreeSet<CoexLocation>(comparator);
		
		List<CoexLocation> allFarms = getAllLocations();
		for (CoexLocation farm : allFarms)
			result.add(farm);
		
		lastLocation = location;
		farmsSortedFromLastLocation = result;
		
		return result;
	}
	
	private static String getStringOrNull(Cursor c, int column)
	{
		if (!c.isNull(column)) return c.getString(column);
		
		return null;
	}
	
	protected void fillContact(CoexLocation info) {
		info.contactInfo = new LocationContact();
		String[] columns = new String[] { "person", "street", "city", "zip", "phone", "email", "web", "eshop" };
		Cursor c = db.query("contacts", columns, "locationId = " + info.id, null, null, null, null);
		
		c.moveToNext();
		if (c.isAfterLast())
		{
			Log.d("db", "location " + info.id + " has no contact info");
			return;
		}
		
		info.contactInfo.person = getStringOrNull(c, 0);
		info.contactInfo.street = getStringOrNull(c, 1);
		info.contactInfo.city = getStringOrNull(c, 2);
		info.contactInfo.zip = getStringOrNull(c, 3);
		info.contactInfo.phone = getStringOrNull(c, 4);
		info.contactInfo.email = getStringOrNull(c, 5);
		info.contactInfo.web = getStringOrNull(c, 6);
		info.contactInfo.eshop = getStringOrNull(c, 7);
		
		if (!c.isLast())
		{
			Log.e("db", "wtf, location " + info.id + " has more than one contact info");
		}
		
		c.close();
	}
	
	protected void fillDescription(CoexLocation info)
	{
		
		Cursor c = db.query("locations", new String[] { "description" }, "_id = " + info.id, null, null, null, null);
		c.moveToNext();
		
		if (!c.isAfterLast()) {
			info.description = getStringOrNull(c, 0);
		}
		c.close();
	}
	
	protected void fillProducts(CoexLocation info)
	{
		if (products == null) { loadProductNames(); }
		
		info.products = new LinkedList<EntityWithComment>();
		
		Cursor c = db.query("location_product", new String[] { "productId", "comment", "mainProduct" },
				"locationId = " + info.id, null, null, null, null, null);
		
		c.moveToFirst();
		
		while(!c.isAfterLast())
		{
			info.products.add(new EntityWithComment(products.get(c.getInt(0)),
					getStringOrNull(c, 1), c.getInt(2) != 0));
		}
		c.close();
	}
	
	protected void fillActivities(CoexLocation info)
	{
		if (activities == null) { loadActivityNames(); }
		
		info.activities = new LinkedList<EntityWithComment>();
		
		Cursor c = db.query("location_activity", new String[] { "activityId", "comment", "mainActivity" },
				"locationId = " + info.id, null, null, null, null, null);
		
		c.moveToFirst();
		
		while(!c.isAfterLast())
		{
			info.activities.add(new EntityWithComment(activities.get(c.getInt(0)),
					getStringOrNull(c, 1), c.getInt(2) != 0));
		}
		c.close();
	}
	
	private SparseArray<String> products = null;
	EntityWithComment[] sortedProducts = null;
	
	private synchronized void loadProductNames() {
		if (products != null) { return; }
		
		String[] columns = new String[] { "_id", "name" };
		Cursor c = db.query("products", columns, null, null, null, null, "_id");
		
		products = new SparseArray<String>();
		c.moveToNext();
		while(!c.isAfterLast()) {
			products.put(c.getInt(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
	}
	
	public EntityWithComment[] getProductsSortedByName()
	{
		if (sortedProducts != null)
			return sortedProducts;
		
		if (products == null)
			loadProductNames();
		
		if (products == null)
		{
			return null;
		}
		
		sortedProducts = new EntityWithComment[products.size()];
		for(int i = 0; i < products.size(); i++)
		{
			sortedProducts[i++] = new EntityWithComment(products.valueAt(i), null, false);
		}
		Arrays.sort(sortedProducts, EntityWithComment.stringComparator());
		
		return sortedProducts;
	}
	
	private SparseArray<String> activities = null;
	EntityWithComment[] sortedActivities = null;
	
	private synchronized void loadActivityNames() {
		if (activities != null) { return; }
		
		String[] columns = new String[] { "_id", "name" };
		Cursor c = db.query("activities", columns, null, null, null, null, "_id");
		
		activities = new SparseArray<String>();
		c.moveToNext();
		while(!c.isAfterLast()) {
			activities.put(c.getInt(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
	}
	
	public EntityWithComment[] getActivitiesSortedByName()
	{
		if (sortedActivities != null)
			return sortedActivities;
		
		if (activities == null)
			loadActivityNames();
		if (activities == null)
		{
			return null;
		}
		
		sortedActivities = new EntityWithComment[activities.size()];
		for(int i = 0; i < products.size(); i++)
		{
			sortedActivities[i++] = new EntityWithComment(activities.valueAt(i), null, false);
		}
		Arrays.sort(sortedActivities, EntityWithComment.stringComparator());
		
		return sortedActivities;
	}
	
	// TODO: updateLocation
	
	
	public List<CoexLocation> getLocations(Set<Long> ids)
	{
		StringBuilder bldr = new StringBuilder("_id IN (");
		boolean first = true;
		
		for (Long id : ids)
		{
			if (!first) { bldr.append(','); }
			bldr.append(id);
			first = false;
		}
		
		bldr.append(')');

		Cursor c = db.query("locations", locationBasicColumns, bldr.toString() , null, null, null, null);

		c.moveToNext();
		LinkedList<CoexLocation> res = new LinkedList<CoexLocation>();
		
		while (!c.isAfterLast()) {
			res.add(fromCursor(c));
			c.moveToNext();
		}
		c.close();
		
		return res;
	}
}
