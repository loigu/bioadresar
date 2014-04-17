package cz.hnutiduha.bioadresar.list;

import java.util.TreeSet;

import com.actionbarsherlock.view.Menu;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DataFilter;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationCache;
import cz.hnutiduha.bioadresar.data.LocationInfo;

class AddBookmarkedFarms extends AddAllLocations
{
	public AddBookmarkedFarms(ListActivity activity, DataFilter filter)
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
        
        TreeSet<LocationInfo> bookmarked = DataSourceFactory.getGlobalDataSource(activity).getBookmarkedLocationsSortedByDistance(loc);
        
        for (LocationInfo location : bookmarked)
        {
        	if (isCancelled())
        		return Boolean.FALSE;
        	publishProgress(location);
	    }
        
		return Boolean.TRUE;
	}
}

public class BookmarksListActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        
        View search = findViewById(R.id.searchView);
        search.setVisibility(View.GONE);
        
        view = (LinearLayout) findViewById(R.id.list_main_layout);
        
        next25Button = (Button)findViewById(R.id.next_25_button);
        next25Button.setVisibility(View.GONE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
    	MenuHandler.fillMenu(menu, getSupportActionBar().getThemedContext(), null, null);
    	menu.removeItem(R.id.bookmarkListLink);
    	
    	return true;
    }

    protected synchronized void refreshLocation()
    {
    	
    	Location newLocation = LocationCache.getCenter();
    	
    	// clean list
    	view.removeAllViews();
    	// set location
    	this.usedLocation = newLocation;
    	
    	// fire first loader
    	showNextButton(false);
    	locationsLoader = new AddBookmarkedFarms(this, null);
    	locationsLoader.execute();
    }

}
