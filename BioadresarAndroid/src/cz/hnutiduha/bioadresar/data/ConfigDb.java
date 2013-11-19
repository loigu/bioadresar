package cz.hnutiduha.bioadresar.data;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class ConfigDb {
	
	private SQLiteDatabase db = null;

	public ConfigDb(String storageDir) {
		String dbPath = storageDir + "config";
		
		try
		{
			db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e)
		{
			db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
			onCreate(db);
		}
		
	}
	
	public void close()
	{
		db.close();
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE bookmarks (objectId INTEGER NOT NULL, source INTEGER NOT NULL, PRIMARY KEY (objectId, source));");
		db.execSQL("CREATE TABLE local (variable STRING NOT NULL, value STRING NOT NULL, PRIMARY KEY (variable, value));");
		db.execSQL("INSERT INTO local(variable, value) VALUES('version', '1')");
	}

	
	public void setBookmarked(int source, long objectId, boolean bookmarked)
	{
		if (bookmarked)
		{
			ContentValues values = new ContentValues();
			values.put("objectId", objectId);
			values.put("source", source);
			db.insert("bookmarks", null, values);
		}
		else
		{
			db.delete("bookmarks", "objectId = ? AND source = ?", new String[] {String.valueOf(objectId), String.valueOf(source)} );
		}
	}
	
	public boolean isBookmarked(int source, long objectId)
	{
		boolean isThere = false;
		Cursor c  = db.query("bookmarks", new String [] {  }, " objectId = ? AND source = ?", new String[] {String.valueOf(objectId), String.valueOf(source)} , null, null, null);
		if (c != null && ! c.isAfterLast())
		{
			isThere = true;
		}
		c.close();
		
		return isThere;
	}
	
	public  Set<Long> getBookmarks(int source)
	{
		HashSet<Long> out = new HashSet<Long>();
		
		Cursor c = db.query("bookmarks",  new String [] { "objectId" },  "source = " + source, null, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast())
		{
			out.add(Long.valueOf(c.getLong(0)));
			c.moveToNext();
		}
		
		c.close();
		
		
		return out;
	}

}
