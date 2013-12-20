package cz.hnutiduha.bioadresar.editFarm;



import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.detail.DetailFragment;

import com.actionbarsherlock.app.SherlockFragment;

import cz.hnutiduha.bioadresar.R;

public class OverviewFragment extends SherlockFragment implements OnClickListener, NamedFragment{
	private FragmentNavigator fragmentNavigator;
	private FarmInfo farm;
	private Context context;
	DetailFragment detail = null;
    
	public OverviewFragment(FarmInfo farm, Context context) {
		super();
		
		this.farm = farm;
		this.context = context;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_overview, container, false);
        
        me.findViewById(R.id.okButton).setOnClickListener(this);
        me.findViewById(R.id.backButton).setOnClickListener(this);
        
        detail = new DetailFragment(farm, context, false);
        getChildFragmentManager().beginTransaction().add(R.id.detailHolder, detail).commit();
        
        return me;
    }
    
    public void onBackPressed()
    {
    	getChildFragmentManager().beginTransaction().remove(detail).commit();
    	fragmentNavigator.previousFragment(this);
    }
        
        
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{				
			case R.id.okButton:
			{
				fragmentNavigator.nextFragment(this);
			}
			break;
			
			case R.id.backButton:
			{
				fragmentNavigator.previousFragment(this);
			}
			break;
			
		}
	}
	
	public int getName()
	{
		return R.string.farmOverview;
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
