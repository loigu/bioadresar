package cz.hnutiduha.bioadresar.data;

import cz.hnutiduha.bioadresar.duhaOnline.data.CoexDatabase;
import android.content.Context;

public class DataSourceFactory {
	public static final int SOURCE_INVALID = -1;
	public static final int SOURCE_DUHA_OFFLINE = 1;
	public static final int SOURCE_DUHA_ONLINE = 2;
	
	public static DataSource getGlobalDataSource(Context context)
	{
		// NOTE: we will need merger with any new db
		return CoexDatabase.getDefaultDb(context);
	}
	
	
	public void enableSource(int id)
	{
		
	}
	public void disableSource(int id)
	{
		
	}
	
	public static DataSource getDataSource(int id, Context context)
	{
		switch(id)
		{
		case SOURCE_DUHA_ONLINE:
			return CoexDatabase.getDefaultDb(context);
		}
		
		return null;
	}
}
