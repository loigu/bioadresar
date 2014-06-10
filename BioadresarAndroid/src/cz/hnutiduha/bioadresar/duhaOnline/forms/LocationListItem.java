package cz.hnutiduha.bioadresar.duhaOnline.forms;

import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationInfo;
import android.content.Context;
import android.location.Location;
import android.widget.LinearLayout;


public class LocationListItem extends LinearLayout {
	public float distance = -1;
	public long sourceId = DataSourceFactory.SOURCE_INVALID;
	public long locationId = LocationInfo.INVALID_LOCATION_ID;
	
	public LocationListItem(Context context, LocationInfo location, Location centerOfOurUniverse) {
		super(context);
		distance = location.getDistance(centerOfOurUniverse);
		this.locationId = location.getId();
		this.sourceId = location.getSource().getSourceId();
		
		location.inflateListView(this, centerOfOurUniverse);
	}

}
