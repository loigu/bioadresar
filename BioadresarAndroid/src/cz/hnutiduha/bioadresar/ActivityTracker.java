package cz.hnutiduha.bioadresar;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class ActivityTracker {
	private static Activity lastActivity = null;
	
	private static boolean resetEnabled = true;
	
	public static boolean isResetEnabled()
	{
		return resetEnabled;
	}
	
	public static void setResetEnabled(boolean enabled)
	{
		resetEnabled = enabled;
	}
	
	public static Activity getLastActivity()
	{
		return lastActivity;
	}
	
	protected static void setLastActivity(Activity activity)
	{
		lastActivity = activity;
	}
	
	protected static void clearLastActivity()
	{
		lastActivity = null;
	}
	
	public static void showToastOnActivity(int resId, int duration)
	{
		if (lastActivity == null)
		{
			Log.d("ui", String.format("Can't show toast '%d', no activity active", resId));
			
			return;
		}
		
		lastActivity.runOnUiThread(new Runnable() {
			private int resId, duration;
			public Runnable init(int resId, int duration)
			{
				this.resId = resId;
				this.duration = duration;
				return this;
			}
			public void run()
			{
				Toast.makeText(lastActivity, resId, duration).show();	
			}
		}.init(resId,  duration));
	}

}
