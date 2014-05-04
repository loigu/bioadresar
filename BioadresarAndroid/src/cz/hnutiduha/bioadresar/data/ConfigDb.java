package cz.hnutiduha.bioadresar.data;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

@SuppressLint("SdCardPath")
public class ConfigDb {
	
	private SQLiteDatabase db = null;
	// backward compatibility, move on update
	@SuppressLint("SdCardPath")
	private final static String OLD_DB_PATH = "/data/data/cz.hnutiduha.bioadresar/databases/";

	private void moveIfExists(String oldPath, String newPath)
	{
		// move on upgrade
		// if there is old one and no new one (and path is not the same)
		File to = new File(newPath);
		if (to.exists()) { return; }
		
		File from = new File(oldPath);
		if (!from.exists()) { return; }
		
		if (to.compareTo(from) == 0) { return; }
		
		from.renameTo(to);
	}
	
	public ConfigDb(Context context) {
		String newPath = context.getFilesDir().getPath() + "/config";
		String oldPath = OLD_DB_PATH + "config";
		
		moveIfExists(oldPath, newPath);
		try
		{
			db = SQLiteDatabase.openDatabase(newPath, null, SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e)
		{
			db = SQLiteDatabase.openOrCreateDatabase(newPath, null);
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
	
	static String[] valueColumn = { "value" };
	
	public String getOwnerName()
	{
		String ret = null;
		Cursor c  = db.query("local", valueColumn , " variable = 'ownerName'", null, null, null, null);
		c.moveToNext();
		if (!c.isAfterLast())
		{
			ret = c.getString(0);
		}
		c.close();
		
		return ret;
	}
	
	public String getOwnerMail()
	{
		String ret = null;
		Cursor c  = db.query("local", valueColumn, " variable = 'ownerMail'", null, null, null, null);
		c.moveToNext();
		if (!c.isAfterLast())
		{
			ret = c.getString(0);
		}
		c.close();
		
		return ret;
	}
	
	public void setOwnerInfo(String name, String email)
	{
		db.execSQL("INSERT OR REPLACE INTO local(variable, value) VALUES('ownerName', '" + name + "');");
		db.execSQL("INSERT OR REPLACE INTO local(variable, value) VALUES('ownerMail', '" + email + "');");
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
