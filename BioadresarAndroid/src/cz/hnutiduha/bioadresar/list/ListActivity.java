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

package cz.hnutiduha.bioadresar.list;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.actionbarsherlock.app.SherlockActivity;

import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationCache;
import cz.hnutiduha.bioadresar.data.LocationInfo;
// FIXME: use some abstraction
import cz.hnutiduha.bioadresar.duhaOnline.forms.LocationListItem;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.hnutiduha.bioadresar.view.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

class AddAllLocations extends AsyncTask<Void, LocationInfo, Boolean> {
	ListActivity activity;
	Location loc;
	DataFilter filter;
	int locationsLoaded = 0;
	
	public AddAllLocations(ListActivity activity, DataFilter filter)
	{
		super();
		this.activity = activity;
		this.filter = filter;
		loc = activity.getUsedLocation();
	}
	
	public Location getUsedLocation()
	{
		return loc;
	}
	
	@Override
	protected void onPreExecute()
	{
		activity.showNextButton(false);
		locationsLoaded = 0;
		activity.progress.bringToFront();
		activity.progress.setVisibility(View.VISIBLE);
	}

	@Override
	protected Boolean doInBackground(Void... params) {		
		Log.d("list", "loading all farms");
        if (loc == null)
        {
        	Log.e("list", "can't get location");
        	return Boolean.FALSE;
        }

    	TreeSet<LocationInfo> allLocations =  DataSourceFactory.getGlobalDataSource(activity).getAllLocationsSortedByDistance(loc);
        for (LocationInfo location : allLocations)
        {
        	if (isCancelled())
        		return Boolean.FALSE;
        	pushLocation(location);
	    }
		return Boolean.TRUE;
	}
	
	protected void pushLocation(LocationInfo location)
	{
		if (filter != null && !filter.match(location)) { return; }
		
		locationsLoaded++;
		publishProgress(location);
	}
	protected void onProgressUpdate(LocationInfo... farms)
	{
		activity.insertFarm(farms[0], loc);
	}
	
	protected void onCancelled()
	{
		Log.d("list", "loading of farms cancelled");
		activity.showNextButton(true);
		activity.progress.setVisibility(View.GONE);
	}
	
	protected void onPostExecute(Boolean isDone) {
		activity.showNextButton(!isDone.booleanValue());
		activity.progress.setVisibility(View.GONE); }
}

class AddNext25 extends AddAllLocations
{
	
	public AddNext25(ListActivity activity, DataFilter filter)
	{
		super(activity, filter);
	}
	
	private static TreeSet<LocationInfo> allLocations = null;
	private static LocationInfo next = null;
	
	public static void reset()
	{
		allLocations = null;
		next = null;
	}
	
	protected Boolean doInBackground(Void...voids)
	{
		Log.d("list", "loading next 25 farms");
		
        if (loc == null)
        {
        	Log.e("list", "can't get location");
        	return Boolean.FALSE;
        }
        
        TreeSet<LocationInfo> currentLocations = DataSourceFactory.getGlobalDataSource(activity).getAllLocationsSortedByDistance(loc);
          
    	SortedSet<LocationInfo> tail = null;
    	if (!currentLocations.equals(allLocations))
    	{
    		allLocations = currentLocations;
    		next = allLocations.first();
    		tail = allLocations;
    	}
    	else
    		tail = allLocations.tailSet(next);
    	
    	Iterator<LocationInfo> iter = tail.iterator();
    	
    	while(locationsLoaded < 25)
    	{
    		if (iter.hasNext())
    			pushLocation(iter.next());
    		else
    			return Boolean.TRUE;
    	}
    	
    	if (iter.hasNext())
    	{
    		next = iter.next();
    		return Boolean.FALSE;
    	}
    	else
    		return Boolean.TRUE;
	}
}

class AddLocationsInRectangle extends AddAllLocations
{
	public AddLocationsInRectangle(ListActivity activity, DataFilter filter)
	{
		super(activity, filter);
	}
	@Override
	protected Boolean doInBackground(Void... params) {
		Log.d("list", "starting background task");
		
        // TODO: get location from map
        loc = LocationCache.getCenter();
        if (loc == null)
        {
        	Log.e("list", "can't get location");
        	return Boolean.FALSE;
        }
        
        // NOTE: hardcoded, maybe move somewhere
        double latOffset = -190520 / 1E6;
        double lonOffset = +219726 / 1E6;
        
        Hashtable<Long, LocationInfo> nearestFarms = DataSourceFactory.getGlobalDataSource(activity).getLocationsInRectangle(loc.getLatitude() - latOffset, loc.getLongitude() - lonOffset,
        		loc.getLatitude() + latOffset, loc.getLongitude() + lonOffset);
        
        for (LocationInfo location : nearestFarms.values())
        {
        	if (isCancelled())
        		return Boolean.FALSE;
        	pushLocation(location);
	    }
        // we just don't know...
		return Boolean.FALSE;
	}
	
	protected void onPostExecute(Boolean isDone)
	{
		super.onPostExecute(isDone);
		activity.rectangleLoaded();
	}
}

public class ListActivity extends SherlockActivity implements View.OnClickListener{
	LinearLayout view;
	Button next25Button;
	AsyncTask<Void, LocationInfo, Boolean> locationsLoader = null;
	DataFilter filter = null;
	ProgressBar progress = null;
	SearchView searchView = null;
	
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {	
    	// MenuHandler.fillMenu(menu, getSupportActionBar().getThemedContext(), (SearchManager)getSystemService(Context.SEARCH_SERVICE), getComponentName());
    	MenuHandler.fillMenu(menu, getSupportActionBar().getThemedContext(), null, null);
    	menu.removeItem(R.id.listLink);
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
    {
    	if (featureId == R.id.locationLabel)
		{
			refreshLocation();
			return true;
		}

    	return MenuHandler.idActivated(this, item.getItemId());
	}

	 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        
        view = (LinearLayout) findViewById(R.id.list_main_layout);
		progress = (ProgressBar) findViewById(R.id.marker_progress);
        next25Button = (Button)findViewById(R.id.next_25_button);
        
		
		progress.bringToFront();
		progress.setVisibility(View.VISIBLE);
		
        next25Button.setOnClickListener(this);
        
		searchView = (SearchView)findViewById(R.id.searchView);

		filter = searchView.handleQuery(this);
        
    	MenuHandler.installDropDown(getSupportActionBar(), this);
    	
    	setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    }
    
    public void onStart()
    {
    	super.onStart();
    	refreshLocation();
    	Log.d("List", "onStart()");
    }
    
    public void onStop()
    {
    	super.onStop();
    	if (locationsLoader != null)
    		locationsLoader.cancel(true);
    	Log.d("List", "onStop()");
    }
    
    @Override
    public boolean onSearchRequested()
    {
    	return searchView.requestFocus();
    }
    
    Location usedLocation = null;
    
    public Location getUsedLocation()
    {
    	return usedLocation;
    }
    
    protected synchronized void refreshLocation()
    {
    	
    	Location newLocation = LocationCache.getCenter();
    	if (usedLocation == newLocation)
    		return;
    
    	AddNext25.reset();
    	// clean list
    	view.removeAllViews();
    	// set location
    	this.usedLocation = newLocation;
    	
    	// fire first loader
    	locationsLoader = new AddLocationsInRectangle(this, filter);
    	locationsLoader.execute();
    }
    
    protected void showNextButton(boolean show)
    {
    	if (show)
    	{
    		next25Button.setVisibility(Button.VISIBLE);
    		next25Button.setEnabled(true);
    	}
    	else
    	{
    		next25Button.setVisibility(Button.GONE);
    	}
    }
    
    // backward search - hope new items will go with greater distance
    private int getFarmPos(long sourceId, long locationId, float distance)
    {
    	int childCount = view.getChildCount();
    	
    	LocationListItem childAtPos;
    	
    	while(childCount > 0)
    	{
    		childAtPos = (LocationListItem)view.getChildAt(--childCount);
    		if (childAtPos.locationId == locationId && childAtPos.sourceId == sourceId)
    			return -1;
    		
    		if (childAtPos.distance < distance)
    			return childCount + 1;
    	}
    	    	
    	return 0;
    }
    
    protected void insertFarm(LocationInfo farm, Location centerOfOurUniverse)
    {
    	int desiredPos = getFarmPos(farm.getSource().getSourceId(), farm.getId(), farm.getDistance(centerOfOurUniverse));
    	if (desiredPos == -1)
    		return;
    	
    	LinearLayout newFarm = new LocationListItem(this, farm, centerOfOurUniverse);
    	
    	view.addView(newFarm, desiredPos);
    	
    }
    
    protected void appendFarm(LocationInfo location, Location centerOfOurUniverse)
    {
		LocationListItem newLocation = new LocationListItem(this, location, centerOfOurUniverse);
		view.addView(newLocation, view.getChildCount());
    }
    
	public synchronized void onClick(View v) {
		if (v.equals(next25Button))
		{
			if (locationsLoader == null || locationsLoader.getStatus() == AsyncTask.Status.FINISHED)
			{
				// TODO: set position to last farm shown
				locationsLoader = new AddNext25(this, filter);
				locationsLoader.execute();
			}
			else
			{
				Log.d("List", "blocked by running task");
			}
		}
	}
	
	protected void rectangleLoaded()
	{
		final Context context = this;
		// preload all
    	new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				
				// preload rest
				DataSourceFactory.getGlobalDataSource(context).getAllLocationsSortedByDistance(LocationCache.getCenter());
				return null;
			}
    	}.execute();
    	
		// default rectangle is empty, load more...
		if (view.getChildCount() == 0)
		{
			Log.d("List", "still empty, loading next...");
			showNextButton(false);
			// TODO: set position to last farm shown
			locationsLoader = new AddNext25(this, filter);
			locationsLoader.execute();
		}
	}
}
