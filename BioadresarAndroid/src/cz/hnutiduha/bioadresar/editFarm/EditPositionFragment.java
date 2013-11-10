package cz.hnutiduha.bioadresar.editFarm;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cz.hnutiduha.bioadresar.R;

public class EditPositionFragment extends SherlockFragment implements OnClickListener{
	private FragmentNavigator fragmentNavigator;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_position, container, false);
        
        View item = me.findViewById(R.id.nextButton);
        item.setOnClickListener(this);
        
        return me;
    }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.nextButton)
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
