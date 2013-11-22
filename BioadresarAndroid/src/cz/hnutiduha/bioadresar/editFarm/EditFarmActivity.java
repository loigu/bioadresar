package cz.hnutiduha.bioadresar.editFarm;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;
import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.FarmInfo;

public class EditFarmActivity extends SherlockFragmentActivity implements FragmentNavigator{
	private FarmInfo farm = null;
	
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
            
            farm = new FarmInfo();
            Fragment firstFragment = new EditPositionFragment(farm, this);
            
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
			next = new EditContactFragment(farm);
		else if (origin instanceof EditContactFragment)
			next = new EditProductsFragment(farm);
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
	
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      switch (requestCode) {
        case REQUEST_CODE_RECOVER_PLAY_SERVICES:
          if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Google Play Services must be installed.",
                Toast.LENGTH_SHORT).show();
            finish();
          }
          return;
      }
      super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	
    	int googlePlayInstalled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	if (googlePlayInstalled != ConnectionResult.SUCCESS)
    		GooglePlayServicesUtil.getErrorDialog(googlePlayInstalled, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }
    
    
}