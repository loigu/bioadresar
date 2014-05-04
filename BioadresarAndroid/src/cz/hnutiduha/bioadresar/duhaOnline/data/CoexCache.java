package cz.hnutiduha.bioadresar.duhaOnline.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.IdFilter;
import cz.hnutiduha.bioadresar.data.LocationInfoDistanceComparator;
import cz.hnutiduha.bioadresar.duhaOnline.net.ConnectionHelper;
import cz.hnutiduha.bioadresar.util.Alphabet;
import android.content.ContentValues;
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

	protected SQLiteDatabase db = null;
	private CoexDatabase parent = null;

	private static final int databaseVersion = 2;
	
	private CoexCache(Context context, CoexDatabase parent) throws IOException {
		super(context, context.getFilesDir().getPath() + File.separator
				+ DB_NAME, null, databaseVersion);
		this.parent = parent;
		
		String dbPath = context.getFilesDir().getPath()
				+ File.separator + DB_NAME;
		createDb(dbPath, context);
		openDb(dbPath, SQLiteDatabase.OPEN_READWRITE);
		
		if (ConnectionHelper.isOnline(context))
		{
			new CoexCacheUpdater(this).execute();
		}
	}

	protected static CoexCache getDefaultDb(Context context, CoexDatabase parent) {
		if (defaultDb == null) {
			try {

				appContext = context;
				defaultDb = new CoexCache(context, parent);

			} catch (IOException e) {
				Log.e("db", "error opening db " + e.toString());
			}
		}
		return defaultDb;
	}

	protected static void closeDefaultDb() {
		if (defaultDb != null)
			defaultDb.close();
		defaultDb = null;
	}

	private void runFixtures(Context context, int fromVersion) {
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	private void createDb(String dbPath, Context context) throws IOException {
		int dbVersion = checkDb(dbPath);
		if (dbVersion == 0) {
			File dbFile = new File(dbPath);
			if (dbFile.exists()) {
				dbFile.delete();
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
		db.execSQL("INSERT OR REPLACE INTO config(variable, value) VALUES('databaseVersion', "
				+ String.valueOf(databaseVersion) + ");");
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
			checkDB = SQLiteDatabase.openDatabase(dbPath, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			Log.d("db", "database doesn't exist");
			return 0;
		}

		int version = 0;
		try {
			String[] columns = new String[] { "value" };
			Cursor c = checkDB.query("config", columns,
					"variable='databaseVersion'", null, null, null, null);

			c.moveToNext();
			if (!c.isAfterLast()) {
				version = Integer.decode(c.getString(0));
				Log.d("db", "found old db version " + version);
			}

			c.close();
		} catch (Exception e) {
			Log.e("db", "can't get db version", e.fillInStackTrace());
		}

		checkDB.close();

		return version;
	}

	/**
	 * Copies database from local assets-folder to the just created empty
	 * database in the system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
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
		if (searchString == null)
			return null;

		HashSet<Long> idsFound = new HashSet<Long>();

		searchString = DatabaseUtils.sqlEscapeString(Alphabet
				.removeAccents(searchString));

		Cursor c = db.query("locations_fts", new String[] { "_id" },
				" locations_fts MATCH " + searchString, null, null, null, null);

		c.moveToNext();
		while (!c.isAfterLast()) {
			Log.d("db", "query found " + c.getLong(0));
			idsFound.add(Long.valueOf(c.getLong(0)));
			c.moveToNext();
		}
		c.close();

		return new IdFilter<CoexLocation>(idsFound);
	}

	private static final String[] locationBasicColumns = { "_id", "name",
			"gpsLatitude", "gpsLongitude", "typeId" };

	// expects cursor with valid entry created with columns CoexLocationColumns
	private CoexLocation fromCursor(Cursor c) {
		return new CoexLocation(parent, c.getLong(0), c.getString(1),
				c.getDouble(2), c.getDouble(3), c.getInt(4));
	}

	protected Hashtable<Long, CoexLocation> getLocationsInRectangle(
			double lat1, double lon1, double lat2, double lon2) {

		String selection = "gpsLatitude >= " + Math.min(lat1, lat2)
				+ " AND gpsLongitude >= " + Math.min(lon1, lon2)
				+ " AND gpsLatitude <= " + Math.max(lat1, lat2)
				+ " AND gpsLongitude <= " + Math.max(lon1, lon2);

		Cursor c = db.query("locations", locationBasicColumns, selection, null,
				null, null, "gpsLatitude, gpsLongitude");
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

	private List<CoexLocation> getAllLocations() {
		if (allLocationsList != null)
			return allLocationsList;

		allLocationsHash = new Hashtable<Long, CoexLocation>();

		LinkedList<CoexLocation> res = new LinkedList<CoexLocation>();

		Cursor c = db.query("locations", locationBasicColumns, null, null,
				null, null, "gpsLatitude, gpsLongitude");
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

		if (allLocationsHash != null) {
			ret = allLocationsHash.get(Long.valueOf(id));
			if (ret != null)
				return ret;
		}

		Cursor c = db.query("locations", locationBasicColumns, "_id = " + id,
				null, null, null, null);
		c.moveToNext();
		if (!c.isAfterLast()) {
			ret = this.fromCursor(c);
		}
		c.close();

		return ret;
	}

	private long getLastChangeTime(long locationId) {
		Cursor c = db.query("locations", new String[] { "lastChange" },
				"_id = " + locationId, null, null, null, null);
		c.moveToNext();

		long ret = 0;
		if (!c.isAfterLast()) {
			ret = c.getLong(0);
		}
		c.close();

		return ret;
	}

	private Location lastLocation = null;
	private TreeSet<CoexLocation> farmsSortedFromLastLocation = null;

	protected synchronized TreeSet<CoexLocation> getAllFarmsSortedByDistance(
			Location location) {
		if (location.equals(lastLocation)
				&& farmsSortedFromLastLocation != null)
			return farmsSortedFromLastLocation;

		LocationInfoDistanceComparator comparator = new LocationInfoDistanceComparator(
				location);
		TreeSet<CoexLocation> result = new TreeSet<CoexLocation>(comparator);

		List<CoexLocation> allFarms = getAllLocations();
		for (CoexLocation farm : allFarms)
			result.add(farm);

		lastLocation = location;
		farmsSortedFromLastLocation = result;

		return result;
	}

	private static String getStringOrNull(Cursor c, int column) {
		if (!c.isNull(column))
			return c.getString(column);

		return null;
	}

	private LocationContact getContact(long locationId) {
		LocationContact ret = new LocationContact();
		String[] columns = new String[] { "person", "street", "city", "zip",
				"phone", "email", "web", "eshop" };
		Cursor c = db.query("contacts", columns, "locationId = " + locationId,
				null, null, null, null);

		c.moveToNext();
		if (c.isAfterLast()) {
			Log.d("db", "location " + locationId + " has no contact info");
			return null;
		}

		ret.person = getStringOrNull(c, 0);
		ret.street = getStringOrNull(c, 1);
		ret.city = getStringOrNull(c, 2);
		ret.zip = getStringOrNull(c, 3);
		ret.phone = getStringOrNull(c, 4);
		ret.email = getStringOrNull(c, 5);
		ret.web = getStringOrNull(c, 6);
		ret.eshop = getStringOrNull(c, 7);

		if (!c.isLast()) {
			Log.e("db", "wtf, location " + locationId
					+ " has more than one contact info");
		}

		c.close();

		return ret;
	}

	private String getDescription(long locationId) {
		Cursor c = db.query("locations", new String[] { "description" },
				"_id = " + locationId, null, null, null, null);
		c.moveToNext();

		String ret = null;
		if (!c.isAfterLast()) {
			ret = getStringOrNull(c, 0);
		}
		c.close();

		return ret;
	}

	private List<EntityWithComment> getProducts(long locationId) {
		if (products == null) {
			loadProductNames();
		}

		LinkedList<EntityWithComment> ret = new LinkedList<EntityWithComment>();

		Cursor c = db.query("location_product", new String[] { "productId",
				"comment", "mainProduct" }, "locationId = " + locationId, null,
				null, null, null, null);

		c.moveToFirst();

		while (!c.isAfterLast()) {
			ret.add(new EntityWithComment(products.get(c.getInt(0)),
					getStringOrNull(c, 1), c.getInt(2) != 0));
		}
		c.close();

		return ret;
	}

	private List<EntityWithComment> getActivities(long locationId) {
		if (activities == null) {
			loadActivityNames();
		}

		LinkedList<EntityWithComment> ret = new LinkedList<EntityWithComment>();

		Cursor c = db.query("location_activity", new String[] { "activityId",
				"comment", "mainActivity" }, "locationId = " + locationId,
				null, null, null, null, null);

		c.moveToFirst();

		while (!c.isAfterLast()) {
			ret.add(new EntityWithComment(activities.get(c.getInt(0)),
					getStringOrNull(c, 1), c.getInt(2) != 0));
		}
		c.close();

		return ret;
	}

	protected void fillContact(CoexLocation info) {
		info.contactInfo = getContact(info.id);
	}

	protected void fillDescription(CoexLocation info) {
		info.description = getDescription(info.id);
	}

	protected void fillProducts(CoexLocation info) {
		info.products = getProducts(info.id);
	}

	protected void fillActivities(CoexLocation info) {
		info.activities = getActivities(info.id);
	}

	private SparseArray<String> products = null;
	TreeSet<EntityWithComment> sortedProducts = null;

	private synchronized void loadProductNames() {
		if (products != null) {
			return;
		}

		String[] columns = new String[] { "_id", "name" };
		Cursor c = db.query("products", columns, null, null, null, null, "_id");

		products = new SparseArray<String>();
		c.moveToNext();
		while (!c.isAfterLast()) {
			products.put(c.getInt(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
	}

	public TreeSet<EntityWithComment> getProductsSortedByName() {
		if (sortedProducts == null) {
			if (products == null)
				loadProductNames();

			if (products == null) {
				return null;
			}

			sortedProducts = new TreeSet<EntityWithComment>(
					EntityWithComment.stringComparator());
			for (int i = 0; i < products.size(); i++) {
				sortedProducts.add(new EntityWithComment(products.keyAt(i),
						products.valueAt(i), null, false));
			}
		}

		return sortedProducts;
	}

	public void fillProductId(EntityWithComment product) {
		getProductsSortedByName();
		fillIdOrCreateNewEntity(product, products, sortedProducts, "products");
	}

	private SparseArray<String> activities = null;
	TreeSet<EntityWithComment> sortedActivities = null;

	private synchronized void loadActivityNames() {
		if (activities != null) {
			return;
		}

		String[] columns = new String[] { "_id", "name" };
		Cursor c = db.query("activities", columns, null, null, null, null,
				"_id");

		activities = new SparseArray<String>();
		c.moveToNext();
		while (!c.isAfterLast()) {
			activities.put(c.getInt(0), c.getString(1));
			c.moveToNext();
		}
		c.close();
	}

	public TreeSet<EntityWithComment> getActivitiesSortedByName() {
		if (sortedActivities == null) {
			if (activities == null)
				loadActivityNames();
			if (activities == null) {
				return null;
			}

			sortedActivities = new TreeSet<EntityWithComment>(
					EntityWithComment.stringComparator());
			for (int i = 0; i < activities.size(); i++) {
				sortedActivities.add(new EntityWithComment(activities.keyAt(i),
						activities.valueAt(i), null, false));
			}
		}

		return sortedActivities;
	}

	public void fillActivityId(EntityWithComment activity) {
		getActivitiesSortedByName();
		fillIdOrCreateNewEntity(activity, activities, sortedActivities,
				"activities");
	}

	private void fillIdOrCreateNewEntity(EntityWithComment entity,
			SparseArray<String> entities,
			TreeSet<EntityWithComment> sortedEntities, String tableName) {
		// exists
		int idx = entities.indexOfValue(entity.name);
		if (idx >= 0)
		{
			entity.id = entities.keyAt(idx);
			return;
		}

		// insert to db
		ContentValues row = new ContentValues();
		row.put("name", entity.name);
		db.insert(tableName, null, row);
		// get id
		Cursor c = db.query(tableName, new String[] { "_id" }, "name='"
				+ entity.name + "'", null, null, null, null);
		c.moveToFirst();
		if (!c.isAfterLast()) {
			entity.id = c.getInt(0);
		}
		if (!c.isLast()) {
			Log.e("db", String.format("duplicate entities for %s in table %s",
					entity.name, tableName));
		}

		c.close();

		// append to entities and sortedEntities
		entities.put(entity.id, entity.name);
		sortedEntities.add(new EntityWithComment(entity.id, entity.name, "",
				false));
	}

	protected int getCacheLocationTypeId(String typeName, int coexTypeId) {
		int ret = CoexLocation.INVALID_OBJECT_TYPE;
		Cursor c = db.query("locationTypes", new String[] { "_id" }, "name='"
				+ typeName + "'", null, null, null, null);
		c.moveToFirst();
		if (!c.isAfterLast()) {
			ret = c.getInt(0);
		}
		c.close();

		// found, return
		if (ret != CoexLocation.INVALID_OBJECT_TYPE) {
			return ret;
		}

		// try coex id
		ContentValues row = new ContentValues();
		row.put("_id", coexTypeId);
		row.put("name", typeName);
		long insertedRow = db.insert("locationTypes", null, row);

		// fail, try autogen
		if (insertedRow == -1) {
			row.remove("_id");
			insertedRow = db.insert("locationTypes", null, row);
		}

		// wtf, still problem?
		if (insertedRow == -1) {
			Log.e("db", "can't create new location type");
			return CoexLocation.INVALID_OBJECT_TYPE;
		}

		// get the new id
		c = db.query("locationTypes", new String[] { "_id" }, "name='"
				+ typeName + "'", null, null, null, null);
		c.moveToFirst();
		if (!c.isAfterLast()) {
			ret = c.getInt(0);
		}
		c.close();

		return ret;
	}

	protected void updateLocationCache(CoexLocation location,
			long lastChangeTime) {
		long dbChangeTime = getLastChangeTime(location.id);
		
		// actual
		if (lastChangeTime <= dbChangeTime)
		{
			return;
		}
		
		ContentValues row = new ContentValues();
		if (location.id != CoexLocation.INVALID_LOCATION_ID)
		{
			row.put("_id", location.id);
		}
		row.put("typeId", location.type);
		row.put("name", location.name);
		row.put("gpsLatitude", location.lat);
		row.put("gpsLongitude", location.lon);
		row.put("description", location.description);
		row.put("lastChange", 0); // dirty - set to real update time at end
		db.insertWithOnConflict("locations", "description", row, SQLiteDatabase.CONFLICT_REPLACE);
		
		// get id of newly created location
		if (location.id == CoexLocation.INVALID_LOCATION_ID)
		{
			Cursor c = db.query("locations", new String[] { "_id" },
					"name = ? and typeId = ?", new String[] { location.name, String.valueOf(location.type) },  null, null, null, null);
			c.moveToNext();

			if (!c.isAfterLast()) {
				location.id = c.getLong(0);
			}
			c.close();
		}

		updateContactInDb(location.id, location.contactInfo);
		updateActivitiesInDb(location.id, location.activities);
		updateProductsInDb(location.id, location.products);
		updateDeliveryOptionsInDb(location.id, location.delivery);
		
		row.clear();
		row.put("lastChange", lastChangeTime);
		db.update("locations", row, "_id = "+ location.id, null);
	}

	private void updateContactInDb(long locationId, LocationContact newContact) {
		if (locationId != CoexLocation.INVALID_LOCATION_ID) {
			LocationContact oldContact = getContact(locationId);
			if (oldContact != null && oldContact.compareTo(newContact) == 0) {
				return;
			}
		}

		ContentValues values = new ContentValues();
		values.put("locationId", locationId);
		values.put("person", newContact.person);
		values.put("street", newContact.street);
		values.put("city", newContact.city);
		values.put("zip", newContact.zip);
		values.put("phone", newContact.phone);
		values.put("email", newContact.email);
		values.put("web", newContact.web);
		values.put("eshop", newContact.eshop);
		db.insertWithOnConflict("contacts", "eshop", values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	private void updateActivitiesInDb(long locationId,
			List<EntityWithComment> newActivities) {
		if (locationId != CoexLocation.INVALID_LOCATION_ID) {
			List<EntityWithComment> oldActivities = getActivities(locationId);
			if (oldActivities.size() == newActivities.size()
					&& oldActivities.containsAll(newActivities)
					&& newActivities.containsAll(oldActivities)) {
				return;
			}

			db.delete("location_activity", "locationId = " + locationId, null);
		}

		ContentValues values = new ContentValues();
		values.put("locationId", locationId);
		for (EntityWithComment activity : newActivities) {
			if (activity.id == EntityWithComment.INVALID_ID) {
				fillActivityId(activity);
			}

			values.put("activityId", activity.id);
			if (activity.comment != null) {
				values.put("comment", activity.comment);
			} else {
				values.putNull("comment");
			}
			values.put("mainActivity", activity.mainEntity);

			db.insert("location_activity", "comment", values);
		}
	}

	private void updateProductsInDb(long locationId,
			List<EntityWithComment> newProducts) {
		if (locationId != CoexLocation.INVALID_LOCATION_ID) {
			List<EntityWithComment> oldProducts = getProducts(locationId);

			if (newProducts.size() == oldProducts.size()
					&& oldProducts.containsAll(newProducts)
					&& newProducts.containsAll(oldProducts)) {
				return;
			}

			db.delete("location_product", "locationId = " + locationId, null);
		}

		ContentValues values = new ContentValues();
		values.put("locationId", locationId);
		for (EntityWithComment product : newProducts) {
			if (product.id == EntityWithComment.INVALID_ID) {
				fillProductId(product);
			}
			values.put("productId", product.id);
			if (product.comment != null) {
				values.put("comment", product.comment);
			} else {
				values.putNull("comment");
			}
			values.put("mainProduct", product.mainEntity);

			db.insert("location_product", "comment", values);
		}
	}

	private void updateDeliveryOptionsInDb(long locationId, DeliveryOptions opts) {
		if (opts != null)
		{
			Log.e("db", "non-null delivery options from coex, update app!!!");
		}
	}

	public List<CoexLocation> getLocations(Set<Long> ids) {
		StringBuilder bldr = new StringBuilder("_id IN (");
		boolean first = true;

		for (Long id : ids) {
			if (!first) {
				bldr.append(',');
			}
			bldr.append(id);
			first = false;
		}

		bldr.append(')');

		Cursor c = db.query("locations", locationBasicColumns, bldr.toString(),
				null, null, null, null);

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
