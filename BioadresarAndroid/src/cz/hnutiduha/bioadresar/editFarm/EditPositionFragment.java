package cz.hnutiduha.bioadresar.editFarm;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cz.hnutiduha.bioadresar.R;

public class EditPositionFragment extends SherlockFragment implements OnClickListener, OnMapLongClickListener, OnEditorActionListener{
	private FragmentNavigator fragmentNavigator;
	View me = null;
	SupportMapFragment smf = null;
	EditText lat = null, lon = null;
	GoogleMap map = null;
	Marker mark = null;
	boolean latSet = false, lonSet = false;
	CameraPosition pos = null;
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		if (smf != null) {
			map = smf.getMap();
			map.setOnMapLongClickListener(this);
		} else
			Log.d("map", "can't get map fragment");
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	
		me = inflater.inflate(R.layout.edit_position, container, false);

		View item = me.findViewById(R.id.nextButton);
		item.setOnClickListener(this);

		lat = (EditText) me.findViewById(R.id.latitude);
		lat.setOnEditorActionListener(this);
		lon = (EditText) me.findViewById(R.id.longitude);
		lon.setOnEditorActionListener(this);

		if (smf == null)
		{
			smf = new SupportMapFragment();
			getChildFragmentManager().beginTransaction().add(R.id.map, smf).commit();
		}
		else
		{
			getChildFragmentManager().beginTransaction().show(smf).commit();
		}
        
        return me;
    }
    
    private boolean validate()
    {
    	// FIXME: implement this
    	return true;
    }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.nextButton)
		{
			if (validate())
			{
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
		lat.setText(String.valueOf(arg.latitude));
		lon.setText(String.valueOf(arg.longitude));
		setMarker(arg, false);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		Log.d("map", String.format("editor action %d", actionId));
		if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
		{
			boolean textValid = false;
			try {
				Double.valueOf((v.getText().toString()));
				textValid = true;
			} catch (NumberFormatException e)
			{
				Log.e("map", String.format("invalid coordinate '%s' entered", v.getText().toString()));
			}
			
			switch(v.getId())
			{
			case R.id.latitude:
				latSet = textValid;
				break;
			case R.id.longitude:
				lonSet = textValid;
				break;
			}
			
			if (latSet && lonSet)
			{
				LatLng point = new LatLng(Double.valueOf(lat.getText().toString()), Double.valueOf(lon.getText().toString()));
				setMarker(point, true);
			}
		}
		return false;
	}
}
