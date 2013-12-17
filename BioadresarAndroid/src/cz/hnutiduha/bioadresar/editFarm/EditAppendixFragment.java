package cz.hnutiduha.bioadresar.editFarm;

import java.util.LinkedList;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.ConfigDb;
import cz.hnutiduha.bioadresar.data.FarmContact;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;

public class EditAppendixFragment extends SherlockFragment implements OnClickListener{
	FragmentNavigator fragmentNavigator;
	String commentText;
	EditText comment, person, mail;
	
	public EditAppendixFragment(String comment) {
		super();
		this.commentText = comment;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_appendix, container, false);
        
        me.findViewById(R.id.okButton).setOnClickListener(this);
        me.findViewById(R.id.backButton).setOnClickListener(this);
        
        mail = (EditText) me.findViewById(R.id.mail);
        person = (EditText) me.findViewById(R.id.name);
        comment = (EditText) me.findViewById(R.id.comments);
        
        fillFields();
        
        return me;
    }
    
    private void fillFields()
    {
    	ConfigDb db = HnutiduhaFarmDb.getDefaultDb(context).getConfigDb();
        mail.setText(db.getOwnerMail());
        person.setText(db.getOwnerName());
        comment.setText(commentText);
    }
    
    private void update() {
    	// FIXME: person, mail, comment
    	    }
    
    private boolean validate()
    {
    	if (mail.getText().toString().isEmpty() ||
    			person.getText().toString().isEmpty())
    	{
    		return false;
    	}
    	
    	return true;
    }

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.nextButton:
			{
				if(validate())
				{
					update();
					fragmentNavigator.nextFragment(this);
				}
				else
				{
					/// FIXME: show error
				}
			}
			break;
			case R.id.backButton:
				if (validate())
				{
					update();
				}
				fragmentNavigator.previousFragment(this);
				break;
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
