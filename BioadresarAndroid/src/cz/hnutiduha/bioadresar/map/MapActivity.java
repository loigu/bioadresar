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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.os.Bundle;
import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.LocationCache;
import cz.hnutiduha.bioadresar.view.SearchView;

public class MapActivity extends com.actionbarsherlock.app.SherlockMapActivity {
	
	public static final String mapNodePropertyName = "farmIdToShow";
	
	private FarmMapView mapView;
	private SearchView searchView;
	
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
    	MenuHandler.fillMenu(menu, getSupportActionBar().getThemedContext(), null, null);
    	menu.removeItem(R.id.mapLink);
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
    {
    	if (featureId == R.id.locationLabel)
		{
			mapView.centerMap();
			return true;
		}
    	
    	return MenuHandler.idActivated(this, item.getItemId());
	}
	
    static boolean centerMap = true;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view);
        
        mapView = (FarmMapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
		searchView = (SearchView)findViewById(R.id.searchView);

		mapView.setFilter(searchView.handleQuery(this));

        
        Long targetFarmId = getIntent().getLongExtra(mapNodePropertyName, FarmInfo.INVALID_FARM_ID);
        FarmInfo farm = null;
        if (targetFarmId != FarmInfo.INVALID_FARM_ID)
        	farm = HnutiduhaFarmDb.getDefaultDb(this).getFarm(targetFarmId);

        if (centerMap || farm != null)
        {
	        int zoomLevel = 11;
	       	if (farm != null)
	       	{
	       		mapView.centerOnGeoPoint(FarmInfo.getGeoPoint(farm));
	       		mapView.showFarmBalloonOnStart(targetFarmId.longValue());
	        }
	        else
	        {
	        	mapView.centerMap();
	        	if (!LocationCache.hasRealLocation())
	        		zoomLevel = 9;
	        }
	       	
	        mapView.getController().setZoom(zoomLevel);
        }
        else
        {
        	mapView.gotoLastLocation();
        }
        // center only first time
        centerMap = false;
        
        Activity parent = this.getParent();
        if (parent == null)
        	parent = this;
        
    	MenuHandler.installDropDown(getSupportActionBar(), this);
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		mapView.reinstallOurLocationMark();
	}
    
	@Override
	protected boolean isRouteDisplayed() {
		// NOTE: we don't have time to research this topic :)
		return false;
	}
}
