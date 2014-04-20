package cz.hnutiduha.bioadresar.duhaOnline.editLocation;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.ConfigDb;
import cz.hnutiduha.bioadresar.util.StringOperations;

public class EditAppendixFragment extends SherlockFragment implements OnClickListener, NamedFragment{
	FragmentNavigator fragmentNavigator;
	EditText comment, person, mail;
	private static Cache cache = null;
	Context context;
	
	public static class Cache {
		String name, mail, comment;
		ConfigDb db;
		public Cache(Context context) {
			db = new ConfigDb(context);
			
			name = db.getOwnerName();
			mail = db.getOwnerMail();
		}
		
		public void save()
		{
			db.setOwnerInfo(name,  mail);
		}
	}
	
	public static Cache getCache()
	{
		return cache;
	}
	
	public EditAppendixFragment(Context context) {
		super();
		this.context = context;
		if (cache == null)
			cache = new Cache(context);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_appendix, container, false);
        
        me.findViewById(R.id.sendButton).setOnClickListener(this);
        me.findViewById(R.id.backButton).setOnClickListener(this);
        
        mail = (EditText) me.findViewById(R.id.mail);
        person = (EditText) me.findViewById(R.id.name);
        comment = (EditText) me.findViewById(R.id.comments);
        
        fillFields();
        
        return me;
    }
    
    private void fillFields()
    {
        mail.setText(cache.mail);
        person.setText(cache.name);
        comment.setText(cache.comment);
    }
    
    private void update() {
    	cache.comment = StringOperations.getStringFromEditBox(comment);
    	cache.mail = StringOperations.getStringFromEditBox(mail);
    	cache.name = StringOperations.getStringFromEditBox(person);
    }
    
    private boolean validate(boolean alerts)
    {
    	
    	if (!android.util.Patterns.EMAIL_ADDRESS.matcher(StringOperations.getStringFromEditBox(mail)).matches())
    	{
    		Log.d("ui", "mail is " + StringOperations.getStringFromEditBox(mail));
    		if (alerts)
    			fragmentNavigator.fragmentNotification(R.string.emailNonValid);
    		return false;
    	}
    	
    	if (TextUtils.isEmpty(StringOperations.getStringFromEditBox(person)))
    	{
    		if (alerts)
    			fragmentNavigator.fragmentNotification(R.string.fillInName);
    		return false;
    	}
    	
    	return true;
    }

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.sendButton:
			{
				if(validate(true))
				{
					update();
			    	cache.save();
					fragmentNavigator.nextFragment(this);
				}
			}
			break;
			case R.id.backButton:
				update();
				if (validate(false))
			    	cache.save();
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
		return R.string.farmSend;
	}
}
