/*  This file is part of BioAdresar.
	Copyright 2012 Jiri Zouhar (zouhar@trilobajt.cz)

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

package cz.hnutiduha.bioadresar.map;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import cz.hnutiduha.bioadresar.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.readystatesoftware.maps.TapControlledMapView;

import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.DataSource;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationCache;
import cz.hnutiduha.bioadresar.data.LocationInfo;

public class FarmMapView extends TapControlledMapView {
	FarmsOverlay farmOverlay;
	static GeoPoint currentVisibleRectangle[] = null;
	boolean currentDrawn = false;
	static int currentZoomLevel = -1;
	DataFilter filter = null;
	

	public FarmMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
        List<Overlay> mapOverlays = this.getOverlays();
        Drawable farmMarker = this.getResources().getDrawable(R.drawable.map_marker);
        // NOTE: it should be bound 1/34 higher (center of the shadow)
        farmMarker = FarmsOverlay.boundCenterBottom(farmMarker);
        farmOverlay = new FarmsOverlay(farmMarker, this);
        mapOverlays.add(farmOverlay);
        
		setOnSingleTapListener(farmOverlay);
	}
	
	protected void setFilter(DataFilter filter)
	{
		this.filter = filter;
	}
	
	public void dispatchDraw(Canvas canvas)
	{
		super.dispatchDraw(canvas);
		
		// TODO: don't refresh points on zoom-in
		
		// don't refresh on each zoom change. there may be dozen of them during one zoom
		int lastZoomLevel = currentZoomLevel;
		currentZoomLevel = getZoomLevel();
		if (currentZoomLevel != lastZoomLevel)
			return;
		
		GeoPoint lastVisibleRectangle[] = currentVisibleRectangle;
		currentVisibleRectangle = getVisibleRectangle();
		// don't refresh on every pan, wait the position to stabilize
		if (lastVisibleRectangle != null && 
				lastVisibleRectangle[0].equals(currentVisibleRectangle[0]) &&
				lastVisibleRectangle[1].equals(currentVisibleRectangle[1]))
		{
			if (currentDrawn == false)
			{
				refreshPoints();	
				currentDrawn = true;
			}
		}
		else
		{
			currentDrawn = false;
		}
	}
	
	protected GeoPoint[] getVisibleRectangle()
    {
    	GeoPoint res[] = new GeoPoint[2];
    	res[0] = getProjection().fromPixels(0, 0);
    	res[1] = getProjection().fromPixels(getWidth(), getHeight());
    	
    	return res;
    }
	
	public GeoPoint offsetBy(GeoPoint original, int offX, int offY) {	
	    GeoPoint tl = getProjection().fromPixels(0, 0);
	    GeoPoint br = getProjection().fromPixels(getWidth(), getHeight());

	    int newLon = offX * (br.getLongitudeE6() - tl.getLongitudeE6()) /getWidth() + original.getLongitudeE6(); 
	    int newLat = offY * (br.getLatitudeE6() - tl.getLatitudeE6()) / getHeight() + original.getLatitudeE6();

	    return new GeoPoint(newLat, newLon);
	}
	
	public void centerOnGeoPoint(GeoPoint center)
	{
		getController().animateTo(center, new Runnable() {
			@Override
			public void run() {
				refreshPoints();
				// wtf. the map generate one aux click through movement
				farmOverlay.enableHiding();
			}	
		});
	}
	
	void reinstallOurLocationMark()
	{
		farmOverlay.reinstallOurLocationMark();
	}
	
	public void gotoLastLocation()
	{
		if (currentZoomLevel == -1 || currentVisibleRectangle == null)
		{
			centerMap();
			return;
		}
		
		int lat = (currentVisibleRectangle[0].getLatitudeE6() + currentVisibleRectangle[1].getLatitudeE6()) / 2;
		int lon = (currentVisibleRectangle[0].getLongitudeE6() + currentVisibleRectangle[1].getLongitudeE6()) / 2;
		centerOnGeoPoint(new GeoPoint(lat, lon));
		getController().setZoom(currentZoomLevel);
	}
	
	Location lastCenter = null;
	
	// centers on current location (from gps/cellular)
	public void centerMap()
	{
		Location newCenter = LocationCache.getCenter();
		if (newCenter != lastCenter)
			reinstallOurLocationMark();
		lastCenter = newCenter;
		
		centerOnGeoPoint(new GeoPoint((int)(newCenter.getLatitude() * 1E6), (int)(newCenter.getLongitude() * 1E6)));
	}
	
	public void showFarmBalloonOnStart(long farmId)
	{
		farmOverlay.showFarmBalloonOnStart(farmId);
	}
	
	private void refreshPoints()
	{
		@SuppressWarnings("unchecked")
		DataSource<LocationInfo> db = DataSourceFactory.getGlobalDataSource(this.getContext());
		if (db == null)
		{
			Log.e("db", "Fatal, can't get default db");
			return;
		}
		
		Log.d("gui", "visible area is " + currentVisibleRectangle[0].toString() + " x " + currentVisibleRectangle[1].toString());
		
		Hashtable <Long, LocationInfo> locations = db.getLocationsInRectangle(
				currentVisibleRectangle[0].getLatitudeE6() / 1E6, currentVisibleRectangle[0].getLongitudeE6() / 1E6,
				currentVisibleRectangle[1].getLatitudeE6() / 1E6, currentVisibleRectangle[1].getLongitudeE6() / 1E6);
		
		if (filter != null)
		{
			locations = filter.prune(locations);
		}
		
		farmOverlay.setVisiblePoints(locations);
	}


}
