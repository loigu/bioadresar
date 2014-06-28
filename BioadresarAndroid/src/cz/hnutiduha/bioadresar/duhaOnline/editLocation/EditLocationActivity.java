package cz.hnutiduha.bioadresar.duhaOnline.editLocation;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.LocationInfo;
import cz.hnutiduha.bioadresar.duhaOnline.data.CoexLocation;
import cz.hnutiduha.bioadresar.duhaOnline.data.DeliveryOptions;
import cz.hnutiduha.bioadresar.duhaOnline.data.EntityWithComment;
import cz.hnutiduha.bioadresar.duhaOnline.data.LocationContact;

public class EditLocationActivity extends AddLocationActivity {
	
	public static final String EXTRA_ID = "locationId";
	
	void updateTitle(NamedFragment fr)
	{
		Resources res = getResources();
        setTitle(res.getString(R.string.edit_farm) + ": " + res.getString(fr.getName()));
	}
	
	public EditLocationActivity() {
		super();
	}
	
	protected static void appendIfDiffer(StringBuilder message, String prefix, String old, String changed)
	{
		if ((changed == null || changed.length() == 0) && (old != null && old.length() != 0))
		{
			message.append(prefix);
			message.append("odstraněno\n");
			return;
		}
		
		if (old != null && old.equalsIgnoreCase(changed)) { return; }
		
		message.append(prefix);
		message.append(changed);
		message.append('\n');
	}
	
	protected String formatCoexLocation(CoexLocation farm, String comment) 
	{
		
		StringBuilder message = new StringBuilder();
		
		message.append("Aktualizace lokality ID ").append(farm.getId()).append("\n");
		
		if (TextUtils.isEmpty(comment))
		{
			message.append("Vzkaz od uživatele:\n").append(comment).append("\n");
		}
		
		message.append("Změny:\n");
		
		appendIfDiffer(message, "Název lokality: ", originalLocation.getName(), farm.getName());
		appendIfDiffer(message, "Popis lokality: ", originalLocation.getDescription(), farm.getDescription());
		if (farm.getLatitude() != originalLocation.getLatitude() || farm.getLongitude() != originalLocation.getLongitude())
		{
			message.append("GPS: latitude ").append(farm.getLatitude()).append(", longitude ").append(farm.getLongitude()).append('\n');
		}
		
		LocationContact contact = farm.getContact();
		LocationContact originalContact = originalLocation.getContact();
		String changedAddress = contact.formatAddress("\n\t");
		String originalAddress = originalContact.formatAddress("\n\t");
		appendIfDiffer(message, "Kontakt: ", originalAddress, changedAddress);
		appendIfDiffer(message, "Phone: ", originalContact.phone, contact.phone);
		appendIfDiffer(message, "E-mail: ", originalContact.email, contact.email);
		appendIfDiffer(message, "Web: ", originalContact.web, contact.web);
		appendIfDiffer(message, "Eshop: ", originalContact.eshop, contact.eshop);
		
		
		String originalDelivery = null;
		if (originalLocation.getDeliveryInfo() != null)
		{
			originalDelivery = originalLocation.getDeliveryInfo().toString();
		}
		String changedDelivery = null;
		if (farm.getDeliveryInfo() != null)
		{
			changedDelivery = farm.getDeliveryInfo().toString();
		}
		appendIfDiffer(message, "Rozvoz: \n", originalDelivery, changedDelivery);
		
		message.append("Produkce:\n");
		for (EntityWithComment product : farm.getProducts())
		{
			message.append("\t").append(product.toString()).append("\n");
		}
		message.append("\n");
		
		message.append("Činnosti:\n");
		for (EntityWithComment activity : farm.getActivities())
		{
			message.append("\t").append(activity.toString()).append("\n");
		}
		message.append("\n");
		
		return message.toString();
	}

	
    protected List<NameValuePair> formatMessage(EditAppendixFragment.Cache cache) {
    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("client", "android"));
		nameValuePairs.add(new BasicNameValuePair("cmd", "add_feedback"));
		nameValuePairs.add(new BasicNameValuePair("lang", "cs"));

		nameValuePairs.add(new BasicNameValuePair("user-email", cache.mail));
		nameValuePairs.add(new BasicNameValuePair("user-name", cache.name));
		nameValuePairs.add(new BasicNameValuePair("place-id", String.valueOf(location.getId())));
		
		nameValuePairs.add(new BasicNameValuePair("place-feedback", formatCoexLocation(location, cache.comment)));
		
		return nameValuePairs;
    }
	
	CoexLocation getLocation() {
        Intent myIntent= getIntent();
        long farmId = myIntent.getLongExtra(EXTRA_ID, LocationInfo.INVALID_LOCATION_ID);
        
        return (CoexLocation)DataSourceFactory.getDataSource(DataSourceFactory.SOURCE_DUHA_ONLINE, this).getLocation(farmId);
	}

}
