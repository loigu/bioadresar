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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.maps.OnSingleTapListener;

import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.LocationCache;

public class FarmsOverlay extends ItemizedOverlay<OverlayItem> implements OnSingleTapListener{
	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private FarmOverlayItem lastSelected = null;
	private FarmMapView map;
	private boolean isPinch = false;
	private long firstBalloon = FarmInfo.INVALID_FARM_ID;
	public static int markerWidth = 21;
	public static int balloonYOffset = - 25;
	
	public FarmsOverlay(Drawable defaultMarker, FarmMapView map) {
		super(defaultMarker);
		markerWidth = defaultMarker.getIntrinsicWidth();
		balloonYOffset = - defaultMarker.getIntrinsicHeight() * 25 / 34;
		this.map = map;
		populate();
	}
	
	public static Drawable boundCenterBottom(Drawable drawable) {
		return ItemizedOverlay.boundCenterBottom(drawable);
	}

	public void addOverlay(OverlayItem overlay) {
	    overlays.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	@Override
	public int size() {
		return overlays.size();
	}
	
	public void hideBalloon()
	{
		if (lastSelected != null)
			lastSelected.hideBalloon();
	}
	
	public void showFarmBalloonOnStart(long farmId)
	{
		Log.d("debug", "setting next farm to " + farmId);
		firstBalloon = farmId;
	}
		
	@Override
	protected boolean onTap(int index) {
		// don't show detail on pinch
		if (isPinch)
			return false;
		
		// hide old balloon
		/* TODO: maybe we could use(reuse) only one static balloon to save memory & cpu
		 *       and only change title and show/hide particular category icons
		 */
		hideBalloon();
		Log.d("d", "show balloon");
		OverlayItem item = overlays.get(index);
		if (!(item instanceof FarmOverlayItem))
			return false;
		
		// wtf. the map sends one aux click through the movement
		disableHiding();
		lastSelected = (FarmOverlayItem)item;
		return lastSelected.showBalloon();
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView)
	{
		// detect pinch to prevent firing baloons on it
	    int fingers = e.getPointerCount();
	    if( e.getAction()==MotionEvent.ACTION_DOWN ){
	        isPinch=false;  // Touch DOWN, don't know if it's a pinch yet
	    }
	    if( e.getAction()==MotionEvent.ACTION_MOVE && fingers==2 ){
	        isPinch=true;   // Two fingers, def a pinch
	    }
	    return super.onTouchEvent(e,mapView);
	}
	
	/* TODO: maybe remove the old ones?
	 * TODO: maybe faster join?
	 */
	protected void setVisiblePoints(Hashtable<Long, FarmInfo> farms)
	{
		Iterator<OverlayItem> overlaysIterator = overlays.iterator();
		OverlayItem last;
		// remove existing from hashtable
		while (overlaysIterator.hasNext())
		{
			last = overlaysIterator.next();
			// ignore other overlays
			if (!(last instanceof FarmOverlayItem))
					continue;
			FarmOverlayItem lastFarm = (FarmOverlayItem)last;
			if (firstBalloon == lastFarm.data.id)
			{
				hideBalloon();
				lastSelected = lastFarm;
				lastSelected.showBalloon();
				firstBalloon = FarmInfo.INVALID_FARM_ID;
			}
			farms.remove(Long.valueOf(lastFarm.data.id));
		}
		
		Collection<FarmInfo> newFarms= farms.values();
		Iterator<FarmInfo> farmIterator = newFarms.iterator();
		FarmOverlayItem toAdd;
		FarmInfo nextFarm;
		while (farmIterator.hasNext())
		{
			nextFarm = farmIterator.next();
			toAdd = new FarmOverlayItem(FarmInfo.getGeoPoint(nextFarm), nextFarm, map);
			overlays.add(toAdd);
			if (firstBalloon == nextFarm.id)
			{
				hideBalloon();
				lastSelected = toAdd;
				lastSelected.showBalloon();
				firstBalloon = FarmInfo.INVALID_FARM_ID;
			}
		}
		
		populate();
	}
	
	static final String locationTitle = "zde stojíte";
	OverlayItem lastCenter = null;
	
	void reinstallOurLocationMark() {
		Location currentLocation = LocationCache.getCenter();
		Log.d("Map", "location mark on " + currentLocation.toString());
		
		if (lastCenter != null)
		{
			GeoPoint last = lastCenter.getPoint();
			if (last.getLatitudeE6() == currentLocation.getLatitude() * 1E6 && 
					last.getLongitudeE6() == currentLocation.getLongitude() * 1E6)
			{
				Log.d("Map", "location still the same, doing nothing");
				return;
			}
			
			overlays.remove(lastCenter);
			lastCenter = null;
		}
		
		lastCenter = new OverlayItem(new GeoPoint((int)(currentLocation.getLatitude() * 1E6), (int)(currentLocation.getLongitude() * 1E6)), locationTitle, locationTitle);
		lastCenter.setMarker(map.getResources().getDrawable(R.drawable.here_i_am_center));
		//overlays.add(lastCenter);
	}
	
	/* NOTE: animating to point generates for some mysterious reason one excessive click
	 *       so we ignore all clicks between start & stop of animation
	 */
	private boolean hidingEnabled = true; 
	public void enableHiding()
	{
		hidingEnabled = true;
	}
	public void disableHiding()
	{
		hidingEnabled = false;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// we don't want shadow
		super.draw(canvas, mapView, false);
	}

	@Override
	public boolean onSingleTap(MotionEvent e) {
		if (lastSelected == null || !hidingEnabled)
			return false;

		hideBalloon();
		
		return true;
	}

}
