package cz.hnutiduha.bioadresar.list;

import java.util.List;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DatabaseHelper;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.LocationCache;

class AddBookmarkedFarms extends AddAllFarms
{
	public AddBookmarkedFarms(ListActivity activity)
	{
		super(activity);
	}
	@Override
	protected Boolean doInBackground(Void... params) {
		Log.d("list", "starting background task");
        DatabaseHelper defaultDb = DatabaseHelper.getDefaultDb(activity);
		
        // TODO: get location from map
        loc = LocationCache.getCenter();
        if (loc == null)
        {
        	Log.e("list", "can't get location");
        	return Boolean.FALSE;
        }
        
        List<FarmInfo> bookmarked = defaultDb.getBookmarkedFarmsSortedByDistance(loc);
        
        for (FarmInfo farm : bookmarked)
        {
        	if (isCancelled())
        		return Boolean.FALSE;
        	publishProgress(farm);
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
        
        view = (LinearLayout) findViewById(R.id.list_main_layout);
        context = view.getContext();
        
        next25Button = (Button)findViewById(R.id.next_25_button);
        next25Button.setVisibility(View.GONE);
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
    	farmsLoader = new AddBookmarkedFarms(this);
    	farmsLoader.execute();
    }

}
