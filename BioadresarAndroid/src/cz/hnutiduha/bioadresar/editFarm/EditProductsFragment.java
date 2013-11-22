package cz.hnutiduha.bioadresar.editFarm;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.FarmInfo;


public class EditProductsFragment extends SherlockFragment implements OnClickListener{
	private FragmentNavigator fragmentNavigator;
	private FarmInfo farm;
	
	public EditProductsFragment(FarmInfo farm) {
		super();
		
		this.farm = farm;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_products, container, false);
        
        View item = me.findViewById(R.id.okButton);
        item.setOnClickListener(this);
        
        return me;
    }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.okButton)
		{
			fragmentNavigator.nextFragment(this);
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
}
