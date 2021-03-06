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

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.LocationInfo;

public class FarmOverlayItem extends OverlayItem implements OnClickListener{
	protected LocationInfo data;
	FarmMapView map;
	View balloon = null;
	
	public FarmOverlayItem(GeoPoint point, LocationInfo data, FarmMapView map)
	{
		super(point, data.getName(), data.getName());
		this.data = data;
		this.map = map;
	}
	
	public void showDetail()
	{
		data.goToDetail(balloon);
	}
	
	@Override
	public void onClick(View v) {
		hideBalloon();
		showDetail();
	}
	
	private void createBalloon(Context context)
	{
		balloon = data.getBaloonView(context);
		
		balloon.setOnClickListener(this);
		View closeRegion = (View) balloon.findViewById(R.id.balloon_close);
		if (closeRegion != null)
			closeRegion.setVisibility(View.GONE);
		
	}
	
	public boolean showBalloon()
	{
		boolean isRecycled = true;
		if (balloon == null)
		{
			createBalloon(map.getContext());
			isRecycled = false;
		}
		
		// make sure the balloon won't be too big
		TextView header = (TextView)balloon.findViewById(R.id.balloon_item_title);
		header.setMaxWidth(map.getWidth() / 2);
		
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, getPoint(),
				0, FarmsOverlay.balloonYOffset, MapView.LayoutParams.RIGHT);
		params.mode = MapView.LayoutParams.MODE_MAP;
		
		
		balloon.setVisibility(View.VISIBLE);
		
		if (isRecycled) {
			balloon.setLayoutParams(params);
		} else {
			map.addView(balloon, params);
		}
		
		map.centerOnGeoPoint(map.offsetBy(getPoint(), - map.getWidth() / 4 - FarmsOverlay.markerWidth , 0));
		
		return true;
	}
	
	public void hideBalloon()
	{
		if (balloon != null)
			balloon.setVisibility(View.GONE);
	}
}
