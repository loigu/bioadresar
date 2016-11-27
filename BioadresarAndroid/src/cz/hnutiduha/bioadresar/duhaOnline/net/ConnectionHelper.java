package cz.hnutiduha.bioadresar.duhaOnline.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionHelper {

	private static NetworkInfo getNetInfo(Context context)
	{
		ConnectivityManager cm =
		        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}
	public static boolean isOnline(Context context) {
	    
		NetworkInfo netInfo = getNetInfo(context);
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	public static boolean canReadBigData(Context context)
	{
		NetworkInfo netInfo = getNetInfo(context);
		int netType = netInfo.getType();
		return (netType != ConnectivityManager.TYPE_MOBILE);
		
	}
}
