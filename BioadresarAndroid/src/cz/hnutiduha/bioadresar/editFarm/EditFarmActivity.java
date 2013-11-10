package cz.hnutiduha.bioadresar.editFarm;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;

public class EditFarmActivity extends SherlockFragmentActivity implements FragmentNavigator{
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_farm);
        
    	MenuHandler.installDropDown(getSupportActionBar(), this);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragmentContainer) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            
            Fragment firstFragment = new EditPositionFragment();
            
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, firstFragment).commit();
        }
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
    
    private void commitFarm()
    {
    	// FIXME: commit
    	MenuHandler.idActivated(this,  R.id.homeLink);
    }
    
	@Override
	public void nextFragment(Fragment origin) {
		Fragment next;
		
		if (origin instanceof EditPositionFragment)
			next = new EditContactFragment();
		else if (origin instanceof EditContactFragment)
			next = new EditProductsFragment();
		else if (origin instanceof EditProductsFragment)
		{
			commitFarm();
			return;
		}
		else
		{
			Log.e("EditFarm", "unknown fragment requests advance");
			return;
		}

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack so the user can navigate back
		transaction.replace(R.id.fragmentContainer, next);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}

	@Override
	public void previousFragment(Fragment origin) {
		this.onBackPressed();
	}
    
    
}