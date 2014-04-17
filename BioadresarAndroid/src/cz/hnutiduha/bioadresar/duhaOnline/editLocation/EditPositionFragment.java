package cz.hnutiduha.bioadresar.duhaOnline.editLocation;

import java.io.IOException;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.LocationCache;
import cz.hnutiduha.bioadresar.duhaOnline.data.LocationContact;
import cz.hnutiduha.bioadresar.duhaOnline.data.CoexLocation;
import cz.hnutiduha.bioadresar.util.StringOperations;

public class EditPositionFragment extends SherlockFragment implements OnClickListener, OnMapLongClickListener, OnEditorActionListener, NamedFragment{
	private FragmentNavigator fragmentNavigator;
	View me = null;
	SupportMapFragment smf = null;
	EditText latView = null, lonView = null, locationNameView = null;
	GoogleMap map = null;
	Marker mark = null;
	CoexLocation location;
	Activity parent;
	boolean tapHintShown = false;
	
	public EditPositionFragment(CoexLocation location, Activity parent)
	{
		super();
		this.location = location;
		this.parent = parent;
	}
	
	private void centerMap()
	{
        // expect this to be loaded in loadFarm()
        if (validateCoordinates())
        {
        	LatLng point = new LatLng(Double.valueOf(latView.getText().toString()), Double.valueOf(lonView.getText().toString()));
        	map.moveCamera(CameraUpdateFactory.zoomTo(11));
        	setMarker(point, true);
        }
        else
        {
        	int zoomLevel = 11;
        	Location loc = LocationCache.getCenter();        	
	        if (!LocationCache.hasRealLocation())
	        	zoomLevel = 9;
	        
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), zoomLevel));
        }
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		if (smf != null && map == null) {
			map = smf.getMap();
		}
		
		if (map != null)
		{
			map.setOnMapLongClickListener(this);
			centerMap();	
		}
		
		if (!tapHintShown)
		{
			Toast hint = Toast.makeText(parent, R.string.positionByClick, Toast.LENGTH_LONG);
			hint.setGravity(Gravity.CENTER, 0, 0);
			hint.show();
			tapHintShown = true;
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	
		me = inflater.inflate(R.layout.edit_position, container, false);

		View item = me.findViewById(R.id.nextButton);
		item.setOnClickListener(this);

		latView = (EditText) me.findViewById(R.id.latitude);
		latView.setOnEditorActionListener(this);
		lonView = (EditText) me.findViewById(R.id.longitude);
		lonView.setOnEditorActionListener(this);
		
		locationNameView = (EditText) me.findViewById(R.id.farmName);

		if (smf == null)
		{
			smf = new SupportMapFragment();
			getChildFragmentManager().beginTransaction().add(R.id.map, smf).commit();
		}
		else
		{
			getChildFragmentManager().beginTransaction().show(smf).commit();
		}
		
		loadFromFarm();
        
        return me;
    }
    
    private boolean validateCoordinates()
    {
    	try
    	{
        	double latValue = Double.valueOf(latView.getText().toString());
        	double lonValue = Double.valueOf(lonView.getText().toString());
        	
        	if ((latValue > -90 && latValue < 90) &&
        			(lonValue > -180 && lonValue < 180))
        		return true;
        	
    	} catch (NumberFormatException e) { }
    	
    	return false;
    }
    
    private boolean validate()
    {
    	if (TextUtils.isEmpty(StringOperations.getStringFromEditBox(locationNameView)))
    	{
    		fragmentNavigator.fragmentNotification(R.string.fillInName);
    		//fragmentNavigator.fragmentWarning(R.string.fillInName);
    		return false;
    	}
    	
    	if (!validateCoordinates())
    	{
    		fragmentNavigator.fragmentNotification(R.string.invalidCoordinates);
    		return false;
    	}
    	
    	return true;
    }
    
    private void loadFromFarm()
    {
    	locationNameView.setText(location.getName());
    	double lat = location.getLatitude();
    	double lon = location.getLongitude();
    	
    	if (lat > -90 && lat < 90)
    	{
    		latView.setText(String.valueOf(lat));
    	}
    	if (lon > -180 && lon < 180)
    	{
    		lonView.setText(String.valueOf(lon));
    	}
    }
    
    /// only call when validate returns true
    private void updateFarm()
    {
    	fragmentNavigator.showProgress();
    	location.setName(StringOperations.getStringFromEditBox(locationNameView));
    			
    	double latValue = Double.valueOf(latView.getText().toString());
    	double lonValue = Double.valueOf(lonView.getText().toString());
    	location.setLocation(latValue, lonValue);
    	
    	// if there is no address, try to get it from location
    	if (location.getContact() == null)
    	{
	        Geocoder geocoder = new Geocoder(parent);
	        try {
	            List<Address> addresses = geocoder.getFromLocation(latValue, lonValue, 1);
	            if (addresses != null && addresses.size() > 0)
	            {
		            Address address = addresses.get(0);
		            if (address != null)
		            {
		            	LocationContact contact = new LocationContact();
		            	contact.street = address.getAddressLine(0);
		            	contact.city = address.getAddressLine(1);
		            	location.setContact(contact);
		            }
	            }
	        } catch(IOException e) {}
    	}
    	
    	fragmentNavigator.hideProgress();
    }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.nextButton)
		{
			if (validate())
			{
				updateFarm();
				
				getChildFragmentManager().beginTransaction().hide(smf).commit();
				fragmentNavigator.nextFragment(this);
			}
		}
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            fragmentNavigator = (FragmentNavigator)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement FragmentNavigator");
        }
    }
    
    private void setMarker(LatLng arg, boolean animateTo)
    {
    	if (map == null)
    		return;
    	
		if (mark != null)
		{
			mark.setPosition(arg);
		}
		else
		{
			MarkerOptions options = new MarkerOptions().position(arg);	
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
			mark = map.addMarker(options);
		}
		
		if (animateTo)
		{
			map.animateCamera(CameraUpdateFactory.newLatLng(arg));
		}
    }

	@Override
	public void onMapLongClick(LatLng arg) {
		Log.d("map", String.format("lat %s, lon %f touched", arg.latitude, arg.longitude));
		latView.setText(String.valueOf(arg.latitude));
		lonView.setText(String.valueOf(arg.longitude));
		setMarker(arg, false);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		Log.d("map", String.format("editor action %d", actionId));
		if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
		{
			if (validateCoordinates())
			{
				LatLng point = new LatLng(Double.valueOf(latView.getText().toString()), Double.valueOf(lonView.getText().toString()));
				setMarker(point, true);
			}
		}
		return false;
	}
	
	public int getName()
	{
		return R.string.farmPosition;
	}
}
