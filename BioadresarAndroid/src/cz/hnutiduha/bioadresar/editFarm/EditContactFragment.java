package cz.hnutiduha.bioadresar.editFarm;

import java.util.LinkedList;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.FarmContact;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.util.StringOperations;

public class EditContactFragment extends SherlockFragment implements OnClickListener, NamedFragment{
	FragmentNavigator fragmentNavigator;
	FarmInfo farm = null;
	EditText city, street, mail, web, eshop, phone, person;
	
	public EditContactFragment(FarmInfo farm) {
		super();
		this.farm = farm;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_contact, container, false);
        
        me.findViewById(R.id.nextButton).setOnClickListener(this);
        me.findViewById(R.id.backButton).setOnClickListener(this);
        
        city = (EditText) me.findViewById(R.id.city);
        street = (EditText) me.findViewById(R.id.street);
        mail = (EditText) me.findViewById(R.id.mail);
        web = (EditText) me.findViewById(R.id.web);
        eshop = (EditText) me.findViewById(R.id.eshop);
        phone = (EditText) me.findViewById(R.id.phone);
        person = (EditText) me.findViewById(R.id.contactPerson);
        
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
        person.setText(contact.person);
        
        String phoneText = "";
        if (contact.phoneNumbers != null)
        	for (String number : contact.phoneNumbers)
        	{
        		if (phoneText.length() != 0)
        			phoneText += " ";
        		phoneText += number;
        	}
       phone.setText(phoneText);
       
    }
        
    private void updateFarm() {
    	FarmContact contact = new FarmContact();
    	contact.city = StringOperations.getStringFromEditBox(city);
    	contact.street = StringOperations.getStringFromEditBox(street);
    	contact.email = StringOperations.getStringFromEditBox(mail);
    	contact.web = StringOperations.getStringFromEditBox(web);
    	contact.eshop = StringOperations.getStringFromEditBox(eshop);
    	contact.person = StringOperations.getStringFromEditBox(person);
    	
    	contact.phoneNumbers = new LinkedList<String>();
    	contact.phoneNumbers.add(phone.getText().toString());
    	
    	farm.setFarmContact(contact);
    }
    
    private boolean validate()
    {
    	String mailString = mail.getText().toString().trim();
    	String webString = web.getText().toString().trim(); 
    	String eshopString = eshop.getText().toString().trim();
    	
    	if (city.getText().toString().isEmpty() &&
    			street.getText().toString().isEmpty() &&
    			phone.getText().toString().isEmpty() &&
    			mailString.isEmpty())
    	{
    		fragmentNavigator.fragmentWarning(R.string.fillAtLeastOneContact);
    		return false;
    	}
    	
    	if (!mailString.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(mailString).matches())
    	{
    		fragmentNavigator.fragmentWarning(R.string.emailNonValid);
    		return false;
    	}
    	
    	if (!webString.isEmpty() && !android.util.Patterns.WEB_URL.matcher(webString).matches())
    	{
    		fragmentNavigator.fragmentWarning(R.string.webNonValid);
    		return false;    		
    	}
    	
    	if (!eshopString.isEmpty() && !android.util.Patterns.WEB_URL.matcher(eshopString).matches())
    	{
    		fragmentNavigator.fragmentWarning(R.string.eshopNonValid);
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
					updateFarm();
					fragmentNavigator.nextFragment(this);
				}
			}
			break;
			case R.id.backButton:
				updateFarm();
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
    
	public int getName()
	{
		return R.string.farmContact;
	}

}
