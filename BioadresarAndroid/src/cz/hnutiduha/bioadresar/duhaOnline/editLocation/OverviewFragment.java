package cz.hnutiduha.bioadresar.duhaOnline.editLocation;



import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cz.hnutiduha.bioadresar.duhaOnline.data.CoexLocation;
import cz.hnutiduha.bioadresar.duhaOnline.forms.DetailFragment;

import com.actionbarsherlock.app.SherlockFragment;

import cz.hnutiduha.bioadresar.R;

public class OverviewFragment extends SherlockFragment implements OnClickListener, NamedFragment{
	private FragmentNavigator fragmentNavigator;
	private CoexLocation location;
	private Context context;
	DetailFragment detail = null;
    
	public OverviewFragment(CoexLocation location, Context context) {
		super();
		
		this.location = location;
		this.context = context;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_overview, container, false);
        
        me.findViewById(R.id.okButton).setOnClickListener(this);
        me.findViewById(R.id.backButton).setOnClickListener(this);
        
        detail = new DetailFragment(location, context, false);
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
