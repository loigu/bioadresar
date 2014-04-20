package cz.hnutiduha.bioadresar.duhaOnline.editLocation;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.content.res.Resources;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationInfo;
import cz.hnutiduha.bioadresar.duhaOnline.data.CoexLocation;

public class EditLocationActivity extends AddLocationActivity {
	
	public static final String EXTRA_ID = "locationId";
	
	void updateTitle(NamedFragment fr)
	{
		Resources res = getResources();
        setTitle(res.getString(R.string.edit_farm) + ": " + res.getString(fr.getName()));
	}
	
	public EditLocationActivity() {
		super();
	}
	
	/*
	 * 
client	www
cmd	add_comment
lang	cs
place-comment-text	test test test is the best
place-id	668
place-rating	5
user-email	loigu@volny.cz
user-name	jirka
	 */
	
    protected List<NameValuePair> formatMessage(EditAppendixFragment.Cache cache) {
    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("client", "android"));
		nameValuePairs.add(new BasicNameValuePair("cmd", "add_feedback"));
		nameValuePairs.add(new BasicNameValuePair("lang", "cs"));

		nameValuePairs.add(new BasicNameValuePair("user-email", cache.mail));
		nameValuePairs.add(new BasicNameValuePair("user-name", cache.name));
		nameValuePairs.add(new BasicNameValuePair("place-id", String.valueOf(location.getId())));
		
		nameValuePairs.add(new BasicNameValuePair("place-feedback", formatCoexLocation(location, cache.comment)));
		
		return nameValuePairs;
    }
	
	CoexLocation getLocation() {
        Intent myIntent= getIntent();
        long farmId = myIntent.getLongExtra(EXTRA_ID, LocationInfo.INVALID_LOCATION_ID);
        
        return (CoexLocation)DataSourceFactory.getDataSource(DataSourceFactory.SOURCE_DUHA_ONLINE, this).getLocation(farmId);
	}

}
