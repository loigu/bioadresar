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

package cz.hnutiduha.bioadresar.detail;


import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DataSource;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationInfo;

public class DetailActivity extends SherlockFragmentActivity{
	
	public static final String EXTRA_ID = "locationId";
	public static final String EXTRA_SOURCE = "locationSource";
	private LocationInfo currentLocation = null;	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent myIntent= getIntent();
        int sourceId = myIntent.getIntExtra(EXTRA_SOURCE, DataSourceFactory.SOURCE_INVALID);
        long locationId = myIntent.getLongExtra(EXTRA_ID, LocationInfo.INVALID_LOCATION_ID);
        
        DataSource source = DataSourceFactory.getDataSource(sourceId, this);
        if (source == null)
        {
        	Log.e("view", "unknown source for location");
        	return;
        }
        
        currentLocation = source.getLocation(locationId);
        if (currentLocation == null)
        {
        	Log.e("view", "unknown location");
        	return;
        }
        
        LinearLayout me = new LinearLayout(this);
        me.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        me.setId(R.id.fragmentContainer);
        
        Fragment content = currentLocation.getDetailFragment(this);
        
        getSupportFragmentManager().beginTransaction()
        	.add(R.id.fragmentContainer, content).commit();
        
        this.setContentView(me);

        MenuHandler.installDropDown(getSupportActionBar(), this);
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
    	MenuHandler.fillMenu(menu, getSupportActionBar().getThemedContext(), null, null);
    	menu.removeItem(R.id.mapLink);
    	menu.removeItem(R.id.locationLabel);
    	
    	return true;
    }
    
    @Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
    {	
    	return MenuHandler.idActivated(this, item.getItemId());
	}
}
