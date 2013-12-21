package cz.hnutiduha.bioadresar.editFarm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cz.hnutiduha.bioadresar.MenuHandler;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.ActivityWithComment;
import cz.hnutiduha.bioadresar.data.ConfigDb;
import cz.hnutiduha.bioadresar.data.DeliveryOptions;
import cz.hnutiduha.bioadresar.data.FarmContact;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;
import cz.hnutiduha.bioadresar.data.ProductWithComment;
import cz.hnutiduha.bioadresar.net.CoexConnector;

public class EditFarmActivity extends SherlockFragmentActivity implements FragmentNavigator, OnClickListener{
	private FarmInfo farm = null;
	private TextView warningText;
	private LinearLayout warningLayout;
	
	private void updateTitle(NamedFragment fr)
	{
		Resources res = getResources();
        setTitle(res.getString(R.string.add_farm) + ": " + res.getString(fr.getName()));
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_farm);
        
    	MenuHandler.installDropDown(getSupportActionBar(), this);
    	
    	warningLayout = (LinearLayout)findViewById(R.id.warningLayout);
    	warningText = (TextView)warningLayout.findViewById(R.id.warningText);
    	warningLayout.findViewById(R.id.dismissButton).setOnClickListener(this);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragmentContainer) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            
            farm = HnutiduhaFarmDb.getDefaultDb(this).getFarm(213);
            Fragment firstFragment = new EditPositionFragment(farm, this);
            
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, firstFragment).commit();
            
            updateTitle((NamedFragment)firstFragment);
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
    
	private boolean networkAvailable()
	{
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null &&
		                      activeNetwork.isConnected();
	}
	
	private void requestNetwork()
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
	
	private static String formatMessage(FarmInfo farm, String comment) 
	{
		
		StringBuilder message = new StringBuilder();
		message.append("Není potřeba založit nový producent?\n\n");
		
		message.append("Název lokality: ").append(farm.name).append("\n");
		message.append("Popis lokality:\n").append(farm.getDescription()).append("\n\n");
		message.append("GPS: latitude ").append(farm.lat).append(", longitude ").append(farm.lon).append('\n');
		
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
			for (String placeAndTime : delivery.placesWithTime)
				message.append("\t").append(placeAndTime).append("\n");
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
		
		if (comment != null && !comment.isEmpty())
		{
			message.append("Vzkaz od přidávájícího:\n").append(comment).append("\n");
		}
		
		return message.toString();
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
    
    private boolean commitFarm()
    {
    	if (!networkAvailable())
    	{
    		requestNetwork();
    		return false;
    	}
    	
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("client", "android"));
		nameValuePairs.add(new BasicNameValuePair("cmd", "add"));
		nameValuePairs.add(new BasicNameValuePair("lang", "cs"));

		EditAppendixFragment.Cache cache = EditAppendixFragment.getCache();
		nameValuePairs.add(new BasicNameValuePair("author", cache.mail));
		nameValuePairs.add(new BasicNameValuePair("author_name", cache.name));
		
		nameValuePairs.add(new BasicNameValuePair("message", formatMessage(farm, cache.comment)));
		
		boolean success = false;
		String response = null;
		Resources res = getResources();
		try
		{
			JSONArray result = CoexConnector.post(nameValuePairs);
			// FIXME: get 'ok' object, decode content and put to res
			
			success = true;
		} catch (IOException e)
		{
			response = res.getString(R.string.networkError);
		} catch (JSONException e) {
			response = res.getString(R.string.invalidResponse);
		}
		
		// FIXME: display result
    	return true;
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
			if (commitFarm())
			{
		    	MenuHandler.idActivated(this,  R.id.homeLink);
			}
			
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
		
		updateTitle((NamedFragment)next);
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
	
	public void fragmentWarning(String warning) {
		warningText.setText(warning);
		warningLayout.setVisibility(View.VISIBLE);
	}
	public void fragmentWarning(int resid) {
		fragmentWarning(getResources().getString(resid));
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

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.dismissButton:
			warningLayout.setVisibility(View.GONE);
			break;
		}
		
	}
    
    
}