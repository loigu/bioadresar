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
import cz.hnutiduha.bioadresar.data.FarmContact;
import cz.hnutiduha.bioadresar.data.FarmInfo;

public class EditContactFragment extends SherlockFragment implements OnClickListener{
	FragmentNavigator fragmentNavigator;
	FarmInfo farm = null;
	EditText city, street, mail, web, eshop, phone;
	
	public EditContactFragment(FarmInfo farm) {
		super();
		this.farm = farm;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_contact, container, false);
        
        View item = me.findViewById(R.id.nextButton);
        item.setOnClickListener(this);
        
        city = (EditText) me.findViewById(R.id.city);
        street = (EditText) me.findViewById(R.id.street);
        mail = (EditText) me.findViewById(R.id.mail);
        web = (EditText) me.findViewById(R.id.web);
        eshop = (EditText) me.findViewById(R.id.eshop);
        phone = (EditText) me.findViewById(R.id.phone);
        
        fillFarmContact();
        
        return me;
    }
    
    private void fillFarmContact()
    {
        FarmContact contact = farm.getFarmContact();
        
        if (contact == null)
        {
        	return;
        }
        
        city.setText(contact.city);
        street.setText(contact.street);
        mail.setText(contact.email);
        web.setText(contact.web);
        eshop.setText(contact.eshop);
        
        // FIXME: list to multiple edit boxes
        if (contact.phoneNumbers != null)
        {
        	phone.setText(contact.phoneNumbers.toString());
        }
    }
    
    private void updateFarm() {
    	FarmContact contact = new FarmContact();
    	contact.city = city.getText().toString();
    	contact.street = street.getText().toString();
    	contact.email = mail.getText().toString();
    	contact.web = web.getText().toString();
    	contact.eshop = eshop.getText().toString();
    	contact.phoneNumbers = new LinkedList<String>();
    	
    	// FIXME: iterate all numbers
    	contact.phoneNumbers.add(phone.getText().toString());
    	
    	farm.setFarmContact(contact);
    }
    
    private boolean validate()
    {
    	if (city.getText().toString().isEmpty() ||
    			street.getText().toString().isEmpty())
    	{
    		return false;
    	}
    	
    	return true;
    }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.nextButton)
		{
			if(validate())
			{
				updateFarm();
				fragmentNavigator.nextFragment(this);
			}
			else
			{
				/// FIXME: show error
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

}
