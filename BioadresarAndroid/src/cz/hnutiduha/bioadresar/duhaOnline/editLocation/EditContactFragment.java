package cz.hnutiduha.bioadresar.duhaOnline.editLocation;


import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.duhaOnline.data.LocationContact;
import cz.hnutiduha.bioadresar.duhaOnline.data.CoexLocation;
import cz.hnutiduha.bioadresar.util.StringOperations;

public class EditContactFragment extends SherlockFragment implements OnClickListener, NamedFragment{
	FragmentNavigator fragmentNavigator;
	CoexLocation location = null;
	EditText city, street, mail, web, eshop, phone, person;
	
	public EditContactFragment(CoexLocation farm) {
		super();
		this.location = farm;
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
        LocationContact contact = location.getContact();
        
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
        phone.setText(contact.phone);
    }
        
    private void updateFarm() {
    	LocationContact contact = new LocationContact();
    	contact.city = StringOperations.getStringFromEditBox(city);
    	contact.street = StringOperations.getStringFromEditBox(street);
    	contact.email = StringOperations.getStringFromEditBox(mail);
    	contact.web = StringOperations.getStringFromEditBox(web);
    	contact.eshop = StringOperations.getStringFromEditBox(eshop);
    	contact.person = StringOperations.getStringFromEditBox(person);
    	contact.phone = StringOperations.getStringFromEditBox(phone);
    	
    	location.setContact(contact);
    }
    
    private boolean validate()
    {
    	String mailString = mail.getText().toString().trim();
    	String webString = web.getText().toString().trim(); 
    	String eshopString = eshop.getText().toString().trim();
    	
    	if (TextUtils.isEmpty(city.getText().toString()) &&
    			TextUtils.isEmpty(street.getText().toString()) &&
    			TextUtils.isEmpty(phone.getText().toString()) &&
    			TextUtils.isEmpty(mailString))
    	{
    		fragmentNavigator.fragmentNotification(R.string.fillAtLeastOneContact);
    		return false;
    	}
    	
    	if (!TextUtils.isEmpty(mailString) && !android.util.Patterns.EMAIL_ADDRESS.matcher(mailString).matches())
    	{
    		fragmentNavigator.fragmentNotification(R.string.emailNonValid);
    		return false;
    	}
    	
    	if (!TextUtils.isEmpty(webString) && !android.util.Patterns.WEB_URL.matcher(webString).matches())
    	{
    		fragmentNavigator.fragmentNotification(R.string.webNonValid);
    		return false;    		
    	}
    	
    	if (!TextUtils.isEmpty(eshopString) && !android.util.Patterns.WEB_URL.matcher(eshopString).matches())
    	{
    		fragmentNavigator.fragmentNotification(R.string.eshopNonValid);
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
