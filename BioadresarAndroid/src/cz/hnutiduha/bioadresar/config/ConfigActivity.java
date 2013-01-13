package cz.hnutiduha.bioadresar.config;

import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.LocationCache;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class ConfigActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	protected void onCreate(Bundle savedInstanceState) {
		   super.onCreate(savedInstanceState);
		   addPreferencesFromResource(R.xml.preferences);
		   
		   this.getPreferenceManager().findPreference("defaultLocation").setOnPreferenceChangeListener(this);
		}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("defaultLocation"))
		{
			LocationCache.updateLocation(LocationCache.getDefaultLocation(this));
		}
		return false;
	}
	
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
    	MenuHandler.fillMenu(menu);
    	menu.removeItem(R.id.configLink);
    	menu.removeItem(R.id.locationLabel);
    	
    	return true;
    }
    
    @Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item)
    {
    	return MenuHandler.idActivated(this, item.getItemId());
	}
}
