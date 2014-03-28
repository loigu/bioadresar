package cz.hnutiduha.bioadresar.editFarm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.ActivityWithComment;
import cz.hnutiduha.bioadresar.data.DeliveryOptions;
import cz.hnutiduha.bioadresar.data.FarmContact;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.LocationInfo;
import cz.hnutiduha.bioadresar.data.ProductWithComment;
import cz.hnutiduha.bioadresar.net.CoexConnector;

public class AddFarmActivity extends SherlockFragmentActivity implements FragmentNavigator, CoexConnector.JSONReceiver{
	protected FarmInfo farm = null;
	protected FarmInfo originalFarm = null;
	ProgressBar progress;
	
	void updateTitle(NamedFragment fr)
	{
		Resources res = getResources();
        setTitle(res.getString(R.string.add_farm) + ": " + res.getString(fr.getName()));
	}
	
	FarmInfo getFarm()
	{
		return new FarmInfo();
	}
	
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
            
            originalFarm = getFarm();
            farm = new FarmInfo(originalFarm);
            
            Fragment firstFragment = new EditPositionFragment(farm, this);
            
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, firstFragment).commit();
            
            updateTitle((NamedFragment)firstFragment);
        }
        
        progress = (ProgressBar) findViewById(R.id.marker_progress);
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
    
	protected boolean networkAvailable()
	{
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null &&
		                      activeNetwork.isConnected();
	}
	
	protected void requestNetwork()
	{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Add the buttons
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   Intent myIntent = new Intent( Settings.ACTION_WIRELESS_SETTINGS );
			        	   startActivity(myIntent);
			           }
			       });
			builder.setMessage(R.string.enableNetwork);

			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();
	}
	
	protected static String formatFarmInfo(FarmInfo farm, String comment) 
	{
		
		StringBuilder message = new StringBuilder();
		long id = farm.getId();
		
		if (id == LocationInfo.INVALID_LOCATION_ID)
		{
			message.append("Není potřeba založit nový producent?\n\n");
		}
		else
		{
			message.append("Aktualizace lokality ID ").append(id).append("\n");
		}
		
		if (TextUtils.isEmpty(comment))
		{
			message.append("Vzkaz od uživatele:\n").append(comment).append("\n");
		}
		
		// FIXME: use copy to put diff only
		
		message.append("Název lokality: ").append(farm.getName()).append("\n");
		message.append("Popis lokality:\n").append(farm.getDescription()).append("\n\n");
		message.append("GPS: latitude ").append(farm.getLatitude()).append(", longitude ").append(farm.getLongitude()).append('\n');
		
		FarmContact contact = farm.getFarmContact();
		message.append("Kontaktní osoba: ").append(contact.person).append(", e-mail: ").append(contact.email).append("\n");
		message.append("Adresa: \n\tulice: ").append(contact.street).append("\n\tměsto: ").append(contact.city).append("\n");
		
		message.append("Telefon(y): ");
		boolean first = true;
		for (String phone : contact.phoneNumbers)
		{
			if (!first) message.append(", ");
			message.append(phone);
			first = false;
		}
		message.append("\n");
		
		message.append("Web: ").append(contact.web).append("\n");
		message.append("E-shop: ").append(contact.eshop).append("\n");
		
		DeliveryOptions delivery = farm.getDeliveryInfo();
		if (delivery != null)
		{
			message.append("Rozvoz domů: ").append(delivery.customDistribution ? "ano" : "ne").append("\n");
			message.append("Výdejní místa:\n");
			if (delivery.placesWithTime != null)
			{
				for (String placeAndTime : delivery.placesWithTime)
					message.append("\t").append(placeAndTime).append("\n");
			}
			message.append("\n");
		}
		
		message.append("Produkce:\n");
		for (ProductWithComment product : farm.getProducts())
		{
			message.append("\t").append(product.toString()).append("\n");
		}
		message.append("\n");
		
		message.append("Činnosti:\n");
		for (ActivityWithComment activity : farm.getActivities())
		{
			message.append("\t").append(activity.toString()).append("\n");
		}
		message.append("\n");
		
		return message.toString();
	}
	
	protected List<NameValuePair> formatMessage(EditAppendixFragment.Cache cache)
	{
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("client", "android"));
		nameValuePairs.add(new BasicNameValuePair("cmd", "add"));
		nameValuePairs.add(new BasicNameValuePair("lang", "cs"));

		nameValuePairs.add(new BasicNameValuePair("author", cache.mail));
		nameValuePairs.add(new BasicNameValuePair("author_name", cache.name));
		
		String message = formatFarmInfo(farm, cache.comment);
		Log.d("net", "with message " + message);
		nameValuePairs.add(new BasicNameValuePair("message", message));
		nameValuePairs.add(new BasicNameValuePair("place-message", formatFarmInfo(farm, cache.comment)));
		
		return nameValuePairs;
	}
		
		    /*
author	loigu@volny.cz
author_name	jirka
client	www
cmd	add
lang	cs
message	toto je testovaci zprava misto: 15.15 x 50.11111 popis: naše krásná farmička, roste na ní travička, na ní se pase kravička. jóó, to bude mlíčka
place-message	toto je testovaci zprava misto: 15.15 x 50.11111 popis: naše krásná farmička, roste na ní travička, na ní se pase kravička. jóó, to bude mlíčka
user-email	loigu@volny.cz
user-name	jirka

Source
user-name=jirka&user-email=loigu%40volny.cz&place-message=toto+je+testovaci+zprava%0A%0Amisto%3A+15.15+x+50.11111%0A%0Apopis%3A+%0Ana%C5%A1e+kr%C3%A1sn%C3%A1+farmi%C4%8Dka%2C+%0Aroste+na+n%C3%AD+travi%C4%8Dka%2C+%0Ana+n%C3%AD+se+pase+kravi%C4%8Dka.%0Aj%C3%B3%C3%B3%2C+to+bude+ml%C3%AD%C4%8Dka&lang=cs&client=www&cmd=add&author=loigu%40volny.cz&author_name=jirka&message=toto+je+testovaci+zprava%0A%0Amisto%3A+15.15+x+50.11111%0A%0Apopis%3A+%0Ana%C5%A1e+kr%C3%A1sn%C3%A1+farmi%C4%8Dka%2C+%0Aroste+na+n%C3%AD+travi%C4%8Dka%2C+%0Ana+n%C3%AD+se+pase+kravi%C4%8Dka.%0Aj%C3%B3%C3%B3%2C+to+bude+ml%C3%AD%C4%8Dka


response:
{"ok":"M\u00edsto bylo p\u0159id\u00e1no","form":{"author":"loigu@volny.cz","author_name":"jirka"},"location_id":null}
		 * 

		*/
	
	public void showProgress()
	{
		// NOTE: this doesn't work on map, youtube and other crappy fragments
		progress.bringToFront();
		progress.setVisibility(View.VISIBLE);
	}
	
	public void hideProgress()
	{
		progress.setVisibility(View.GONE);
	}
    
    protected void commitFarm()
    {
    	if (!networkAvailable())
    	{
    		requestNetwork();
    		return;
    	}
    	
		EditAppendixFragment.Cache cache = EditAppendixFragment.getCache();
		List<NameValuePair> nameValuePairs = formatMessage(cache);
		
		showProgress();
		new CoexConnector(this, nameValuePairs).execute();		
    }
    
	@Override
	public void postFailed(Exception reason)
	{
		Resources res = getResources();
		String errorMessage = res.getString(R.string.ughDeadEnd);
		if (reason instanceof IOException)
		{
			Log.e("net", "ioexception when sending", reason);
			errorMessage = res.getString(R.string.networkError);
		} else if (reason instanceof JSONException)
		{
			Log.e("net", "json wtf", reason);
			errorMessage = res.getString(R.string.invalidResponse);
		}
		
		hideProgress();
		fragmentNotification(errorMessage);
	}

	@Override
	public void readJSONResponse(JSONObject result) {
		try
		{
			if (result.has("ok"))
			{
				fragmentNotification(result.getString("ok"));
				MenuHandler.idActivated(getBaseContext(),  R.id.homeLink);
			}
			else if (result.has("error"))
			{
				fragmentNotification(result.getString("error"));
			}
		}
		catch (JSONException ex)
		{
			postFailed(ex);
		}
		hideProgress();
	}
    
	@Override
	public void nextFragment(Fragment origin) {
		Fragment next;
		
		if (origin instanceof EditPositionFragment)
			next = new EditContactFragment(farm);
		else if (origin instanceof EditContactFragment)
			next = new EditDetailsFragment(farm, this);
		else if (origin instanceof EditDetailsFragment)
		{
			next = new OverviewFragment(farm, this);
		}
		else if (origin instanceof OverviewFragment)
		{
			next = new EditAppendixFragment(this);
		}
		else if (origin instanceof EditAppendixFragment)
		{
			commitFarm();
			return;
		}
		else
		{
			Log.e("EditFarm", "unknown fragment requests advance");
			return;
		}
		showProgress();

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack so the user can navigate back
		transaction.replace(R.id.fragmentContainer, next);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
		
		updateTitle((NamedFragment)next);
		
		hideProgress();
	}
	
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		Fragment fr = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
		updateTitle((NamedFragment)fr);
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

	public void fragmentNotification(String text)
	{
		Toast.makeText(this, text,
                Toast.LENGTH_SHORT).show();
	}
	@Override
	public void fragmentNotification(int stringId) {
		Toast.makeText(this, stringId,
                Toast.LENGTH_SHORT).show();
	}
}
