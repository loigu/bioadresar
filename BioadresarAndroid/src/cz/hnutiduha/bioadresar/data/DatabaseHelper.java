/*  This file is part of BioAdresar.
	Copyright 2012 Jiri Zouhar (zouhar@trilobajt.cz), Jiri Prokop

    BioAdresar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BioAdresar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BioAdresar.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.hnutiduha.bioadresar.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import cz.hnutiduha.bioadresar.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static String DB_PATH = "/data/data/cz.hnutiduha.bioadresar/databases/";

	private static String DB_NAME = "bioadr";
	
	private static DatabaseHelper defaultDb = null;

	private static Context appContext = null;
	
	protected SQLiteDatabase db;
	
	private HashMap<Long, String> categories = null;
	private HashMap<Long, String> products = null;
	private HashMap<Long, String> activities = null;
	
	private static final int databaseVersion = 8;
	
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, databaseVersion);
	}
	
	public static DatabaseHelper getDefaultDb(Context context)
	{
		if (defaultDb == null)
		{
			try {
				appContext = context;
				defaultDb = new DatabaseHelper(context);
				defaultDb.createDb(context);
				defaultDb.openDb(SQLiteDatabase.OPEN_READWRITE);
			} catch (IOException e) {
				Log.e("db", "error opening db " + e.toString());
			}
		}
		return defaultDb; 
	}
	
	public static void closeDefaultDb()
	{
		if (defaultDb != null)
			defaultDb.close();
		defaultDb = null;
	}
	
	private void runFixtures (Context context, int fromVersion)
	{
		// we changed format of default location, reset the setting
		if (fromVersion < 7)
		{
			Log.d("db", "cleaning default location because of change of format");
			PreferenceManager.getDefaultSharedPreferences(context).edit().remove("defaultLocation").commit();
		}
	}
	
	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDb(Context context) throws IOException {
		int dbVersion = checkDb();
		if (dbVersion != databaseVersion)
		{
			File db = new File(DB_PATH + DB_NAME);
			if (db.exists())
			{
				db.delete();
				runFixtures(context, dbVersion);
			}
			
			this.getReadableDatabase();

			try {
				copyDb();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
			Log.d("db", "new database installed");
		}
		
		openDb(SQLiteDatabase.OPEN_READWRITE);
		db.execSQL("INSERT OR REPLACE INTO config(variable, value) VALUES('databaseVersion', " + String.valueOf(databaseVersion) + ");");
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time application is opened.
	 * 
	 * @return database version
	 */
	private int checkDb() {
		SQLiteDatabase checkDB = null;

		try {
			checkDB = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			Log.d("db", "database doesn't exist");
			return 0;
		}
		
		int version = 1;
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
	private void copyDb() throws IOException {
		InputStream myInput = appContext.getAssets().open(DB_NAME);
		String outFileName = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFileName);

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

	public void openDb(int flags) throws SQLException {
		String path = DB_PATH + DB_NAME;
		db = SQLiteDatabase.openDatabase(path, null, flags);
	}

	@Override
	public synchronized void close() {
		if (db != null)
			db.close();

		super.close();
	}

	public void setFilter(DataFilter filter) {
		//TODO: implement this
	}
	
	public void clearFilter() {
		//TODO: implement this
	}
	
	private static final String[] farmInfoColumns = { "_id", "name", "gpsLatitude", "gpsLongtitude", "bookmark" };
	
	// expects cursor with valid entry created with columns farmInfoColumns
	private FarmInfo fromCursor(Cursor c)
	{
		FarmInfo farmInfo = new FarmInfo();

		farmInfo.id = c.getLong(0);
		farmInfo.name = c.getString(1);
		farmInfo.lat = c.getDouble(2);
		farmInfo.lon = c.getDouble(3);
		farmInfo.bookmarked = (c.getLong(4) == 1) ? true : false;
		farmInfo.categories = getCategoriesByFarmId(farmInfo.id);
		
		return farmInfo;
	}
	
	public Hashtable<Long, FarmInfo> getFarmsInRectangle(double lat1, double lon1, double lat2, double lon2) {
		
		String selection = "gpsLatitude >= ? AND gpsLongtitude >= ? AND gpsLatitude <= ? AND gpsLongtitude <= ?";
		String[] args = new String[] {
				Double.toString(Math.min(lat1, lat2)), Double.toString(Math.min(lon1, lon2)),
				Double.toString(Math.max(lat1, lat2)), Double.toString(Math.max(lon1, lon2))
		};
		Cursor c = db.query("locations", farmInfoColumns, selection, args, null, null, "gpsLatitude, gpsLongtitude");
		Hashtable<Long, FarmInfo> result = new Hashtable<Long, FarmInfo>();
		
		c.moveToNext();
		FarmInfo farmInfo;
		while (!c.isAfterLast()) {
			farmInfo = fromCursor(c);
			
			result.put(farmInfo.id, farmInfo);
			c.moveToNext();
		}
		c.close();
		
		return result;
	}
	
	private List<FarmInfo> allFarmsList = null;
	
	private List<FarmInfo> getAllFarms()
	{
		if (allFarmsList != null)
			return allFarmsList;
		
		LinkedList<FarmInfo> res = new LinkedList<FarmInfo>();
		
		Cursor c = db.query("locations", farmInfoColumns, null, null, null, null, "gpsLatitude, gpsLongtitude");
		c.moveToNext();
		FarmInfo farmInfo;
		while (!c.isAfterLast()) {
			farmInfo = fromCursor(c);
			
			res.add(farmInfo);
			c.moveToNext();
		}
		c.close();
		
		allFarmsList = res;
		
		return res;
	}
	
	private List<FarmInfo> bookmarkedFarmsCache = null;
	
	private void loadBookmarkedFarms()
	{
		String selection = "bookmark = 1";
		Cursor c = db.query("locations", farmInfoColumns, selection, null, null, null, null);
		bookmarkedFarmsCache = new LinkedList<FarmInfo>();
		
		c.moveToNext();
		FarmInfo farmInfo;
		while (!c.isAfterLast()) {
			farmInfo = fromCursor(c);
			
			bookmarkedFarmsCache.add(farmInfo);
			c.moveToNext();
		}
		c.close();
	}
	
	public List<FarmInfo> getBookmarkedFarmsSortedByDistance(Location location)
	{
		if (bookmarkedFarmsCache == null)
			loadBookmarkedFarms();
		
		FarmInfoDistanceComparator comparator = new FarmInfoDistanceComparator(location);
		Collections.sort(bookmarkedFarmsCache, comparator);
		
		return bookmarkedFarmsCache;
	}
	
	// return state of the farm
	public void setBookmark(FarmInfo farm, boolean bookmarked)
	{
		ContentValues args = new ContentValues();
		args.put("bookmark", String.valueOf(bookmarked ? 1 : 0));
		int updated = db.update("locations", args, "_id=?", new String[] {String.valueOf(farm.id)});
		
		if (updated == 0)
		{
			Log.e("db", "can't store bookmark");
			return;
		}
		
		farm.bookmarked = bookmarked;
		
		if (bookmarkedFarmsCache == null)
			return;
		
		boolean inList = bookmarkedFarmsCache.contains(farm);
			
		if (!inList && bookmarked)
			bookmarkedFarmsCache.add(farm);
		
		if (inList && !bookmarked)
			bookmarkedFarmsCache.remove(farm);
	}
	
	public FarmInfo getFarm(long id) {
		FarmInfo ret = null;
		
		String[] columns = new String[] { "name", "gpsLatitude", "gpsLongtitude", "bookmark" };
		String[] args = { String.valueOf(id) };
		Cursor c = db.query("locations", columns, "_id = ?", args, null, null, null);
		c.moveToNext();
		if (!c.isAfterLast()) {
			ret = new FarmInfo();
			ret.id = id;
			ret.name = c.getString(0);
			ret.lat = c.getDouble(1);
			ret.lon = c.getDouble(2);
			ret.bookmarked = (c.getLong(3) == 1) ? true : false;
			ret.categories = getCategoriesByFarmId(id);
			
		}
		c.close();
		
		return ret;		
	}
	
	private Location lastLocation = null;
	private TreeSet<FarmInfo> farmsSortedFromLastLocation = null;
	
	public synchronized TreeSet<FarmInfo> getAllFarmsSortedByDistance(Location location) {
		
		if (location.equals(lastLocation) && farmsSortedFromLastLocation != null)
			return farmsSortedFromLastLocation;
		
		FarmInfoDistanceComparator comparator = new FarmInfoDistanceComparator(location);
		TreeSet<FarmInfo> result = new TreeSet<FarmInfo>(comparator);
		
		List<FarmInfo> allFarms = getAllFarms();
		for (FarmInfo farm : allFarms)
			result.add(farm);
		
		lastLocation = location;
		farmsSortedFromLastLocation = result;
		
		return result;
	}
	
	private List<Long> getCategoriesByFarmId(long id) {
		String[] columns = new String[] { "categoryId" };
		List<Long> categories = new LinkedList<Long>();
		// TODO add category "Others" (164) and join with products (and find by products too - because product has category assigned too)
		Cursor c = db.query("location_category", columns,
				"locationId = ?", new String[] { (id + "") },
				null, null, "categoryId"
		);
		
		c.moveToNext();
		while (!c.isAfterLast()) {
			categories.add(c.getLong(0));
			c.moveToNext();
		}
		c.close();
		
		return categories;
	}
	
	private void fillProducts(FarmInfo info, String[] locationIdInArray) {
		List<ProductWithComment> products = new LinkedList<ProductWithComment>();
		String[] columns = new String[] { "productId", "comment" };
		Cursor c = db.query("location_product", columns, "locationId = ?", locationIdInArray, null, null, null);
		c.moveToNext();
		while (!c.isAfterLast()) {
			String comment = null;
			if (!c.isNull(1))
				comment = c.getString(1);
			products.add(new ProductWithComment(c.getLong(0), comment));
			c.moveToNext();
		}
		c.close();
		info.products = products;
	}
	
	private void fillActivities(FarmInfo info, String[] locationIdInArray) {
		List<ActivityWithComment> activities = new LinkedList<ActivityWithComment>();
		String[] columns = new String[] { "activityId", "comment" };
		Cursor c = db.query("location_activity", columns, "locationId = ?", locationIdInArray, null, null, null);
		c.moveToNext();
		while (!c.isAfterLast()) {
			String comment = null;
			if (!c.isNull(1))
				comment = c.getString(1);
			activities.add(new ActivityWithComment(c.getLong(0), comment));
			c.moveToNext();
		}
		c.close();
		info.activities = activities;
	}
	
	// NOTE: api defined in transform.sh
	private static final int LOC_CITY = 1;
	private static final int LOC_STREET = 2;
	private static final int LOC_PHONE = 6;
	private static final int LOC_EMAIL = 3;
	private static final int LOC_WEB = 4;
	private static final int LOC_ESHOP = 5;
	
	private void fillContact(FarmInfo info, String[] locationIdInArray) {
		FarmContact farmContact = new FarmContact();
		farmContact.phoneNumbers = new LinkedList<String>();
		String[] columns = new String[] { "type", "contact" };
		Cursor c = db.query("contacts", columns, "locationId = ?", locationIdInArray, null, null, null);
		c.moveToNext();
		while (!c.isAfterLast()) {
			int type = c.getInt(0);
			String contact = c.getString(1);
			
			switch (type) {
				case LOC_CITY:
					farmContact.city = contact;
					break;
				case LOC_STREET:
					farmContact.street = contact;
					break;
				case LOC_EMAIL:
					farmContact.email = contact;
					break;
				case LOC_ESHOP:
					farmContact.eshop = contact;
					break;
				case LOC_PHONE:
					farmContact.phoneNumbers.add(contact);
					break;
				case LOC_WEB:
					farmContact.web = contact;
					break;
				default:
					Log.d("db", "unknown location type " + type);
					break;
			}
			
			c.moveToNext();
		}
		c.close();
		info.contact = farmContact;
	}
	
	private void fillDescription(FarmInfo info, String[] locationIdInArray) {
		Cursor c = db.query("locations", new String[] { "description"}, "_id = ?", locationIdInArray, null, null, null);
		c.moveToNext();
		
		if (!c.isAfterLast()) {
			info.description = c.getString(0);
		}
		c.close();
	}

	public void fillDetails(FarmInfo info) {
		if (info.contact != null)
			return;
		
		String[] args = new String[] { Long.toString(info.id) };
		
		fillDescription(info, args);
		fillProducts(info, args);
		fillActivities(info, args);
		fillContact(info, args);
	}
	
	public String getProductName(Long id) {
		if (this.products == null) {
			this.loadProductNames();
		}
		
		return products.get(id);
	}
	
	public String getActivityName(Long id) {
		if (this.activities == null) {
			this.loadActivityNames();
		}
		
		return activities.get(id);
	}
	
	private void loadProductNames() {
		String[] columns = new String[] { "_id", "name" };
		Cursor c = db.query("products", columns, null, null, null, null, "_id");
		
		products = new HashMap<Long, String>();
		c.moveToNext();
		while(!c.isAfterLast()) {
			products.put(c.getLong(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
	}
	
	public String[] getRegions() {
		String[] columns = new String[] { "name" };
		Cursor c = db.query("regions", columns, null, null, null, null, "_id");
		
		ArrayList<String> regions = new ArrayList<String>(15);
		c.moveToNext();
		while(!c.isAfterLast()) {
			regions.add(c.getString(0));
			c.moveToNext();
		}
		c.close();
		
		return regions.toArray(new String[regions.size()]);
	}
	
	/// @return {latitude, longtitude} or null
	public double[] getRegionCoordinates(String name)
	{
		String [] args = new String[] { name };
		Cursor c = db.query("regions", new String[] { "gpsLatitude", "gpsLongtitude" }, "name = ?", args, null, null, null);
		c.moveToNext();
		
		double [] ret = null;
		
		if (!c.isAfterLast()) {
			ret = new double[2];
			ret[0] = c.getDouble(0);
			ret[1] = c.getDouble(1);
		}
		c.close();
		
		return ret;
	}
	
	private void loadActivityNames() {
		String[] columns = new String[] { "_id", "name" };
		Cursor c = db.query("activities", columns, null, null, null, null, "_id");
		
		activities = new HashMap<Long, String>();
		c.moveToNext();
		while(!c.isAfterLast()) {
			activities.put(c.getLong(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
	}

	public String getCategoryName(Long id) {
		if (this.categories == null) {
			this.loadCategoryNames();
		}
		
		return categories.get(id);
	}

	private void loadCategoryNames() {
		String[] columns = new String[] { "_id", "name" };
		Cursor c = db.query("categories", columns, null, null, null, null, "_id");
		
		categories = new HashMap<Long, String>();
		c.moveToNext();
		while(!c.isAfterLast()) {
			categories.put(c.getLong(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
	}
	
	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	@Override
	public void onCreate(SQLiteDatabase db){}

}
