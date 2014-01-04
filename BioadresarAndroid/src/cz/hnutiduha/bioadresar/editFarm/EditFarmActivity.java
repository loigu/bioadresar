package cz.hnutiduha.bioadresar.editFarm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;
import cz.hnutiduha.bioadresar.net.CoexConnector;

public class EditFarmActivity extends AddFarmActivity {
	
	void updateTitle(NamedFragment fr)
	{
		Resources res = getResources();
        setTitle(res.getString(R.string.edit_farm) + ": " + res.getString(fr.getName()));
	}
	
	public EditFarmActivity() {
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
		nameValuePairs.add(new BasicNameValuePair("cmd", "add_comment"));
		nameValuePairs.add(new BasicNameValuePair("lang", "cs"));

		nameValuePairs.add(new BasicNameValuePair("user-email", cache.mail));
		nameValuePairs.add(new BasicNameValuePair("user-name", cache.name));
		nameValuePairs.add(new BasicNameValuePair("place-id", String.valueOf(farm.id)));
		
		nameValuePairs.add(new BasicNameValuePair("place-comment-text", formatFarmInfo(farm, cache.comment)));
		
		return nameValuePairs;
    }
	
	FarmInfo getFarm() {
        Intent myIntent= getIntent();
        long farmId = myIntent.getLongExtra("farmId", FarmInfo.INVALID_FARM_ID);
        
        HnutiduhaFarmDb db = HnutiduhaFarmDb.getDefaultDb(this);
        return db.getFarm(farmId);
	}

}
