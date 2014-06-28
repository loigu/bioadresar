/*  This file is part of BioAdresar.
	Copyright 2012 Jiri Zouhar (zouhar@trilobajt.cz)

    BioAdresar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BioAdresar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BioAdresar.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.hnutiduha.bioadresar.duhaOnline.forms;


import android.content.Context;
import android.os.Bundle;
import android.text.util.Linkify;

import com.actionbarsherlock.app.SherlockFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.LocationInfo;
import cz.hnutiduha.bioadresar.duhaOnline.data.CoexLocation;
import cz.hnutiduha.bioadresar.duhaOnline.data.EntityWithComment;
import cz.hnutiduha.bioadresar.duhaOnline.data.LocationContact;

public class DetailFragment extends SherlockFragment implements OnClickListener{
	
	// we expect only one detail activity to be shown at a time
	private CoexLocation location = null;
	View view = null;
	ImageView bookmarkView = null;
	ImageView mapView = null;
	Context context;
	boolean isInteractive = true;
    
	public DetailFragment(CoexLocation farm, Context context, boolean interactive) {
		super();
		
		this.location = farm;
		this.context = context;
		this.isInteractive = interactive;
	}
	
    public DetailFragment(CoexLocation farm, Context context) {
    	this(farm, context, true);
    }
    
    private void updateLinks()
    {
        if (view != null && location != null && isInteractive)
        {
        	view.findViewById(R.id.feedback_button).setOnClickListener(this);
        	bookmarkView.setOnClickListener(this);
        	location.setToMapListener(mapView);
        }
        else
        {
        	view.findViewById(R.id.feedbackView).setVisibility(View.GONE);
        	bookmarkView.setVisibility(View.GONE);
        	mapView.setVisibility(View.GONE);
        }
    }
    
    public void locationUpdated(LocationInfo location)
    {
    	this.location = (CoexLocation)location;
    	fillFarmInfo();
    	updateLinks();
    }
	
    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	view = inflater.inflate(R.layout.detail_view, container, false);
        
        bookmarkView = (ImageView)view.findViewById(R.id.bookmarkIcon);
        mapView = (ImageView)view.findViewById(R.id.mapIcon);
        
        updateLinks();
        
        return view;
    }
 
	public void onResume()
    {
    	super.onResume();
    	
    	if (location != null)
    		fillFarmInfo();
    	
    }
        
    static final int NO_LINKIFY = -1;
    
    private void setFieldTextOrHideEmpty(String text, int linkifyMask, int containerId, int fieldId)
    {
    	TextView field = (TextView)view.findViewById(fieldId);
    	if (text != null && !text.equals(""))
    	{
    		field.setText(text);
    		
    		if (linkifyMask != NO_LINKIFY)
    			Linkify.addLinks(field, linkifyMask);
    	}
    	else
    	{
    		View container = view.findViewById(containerId);
    		container.setVisibility(TextView.GONE);
    	}
    }    
    
    private void updateBookmarked()
    {	
    	if (location.isBookmarked())
    		bookmarkView.setImageResource(R.drawable.bookmarked_icon);
    	else
    		bookmarkView.setImageResource(R.drawable.bookmark_icon);
    }
    
    private void fillFarmInfo()
    {	
    	if (isInteractive)
    		updateBookmarked();
    	
    	TextView field = (TextView) view.findViewById(R.id.farmName);
    	field.setText(location.getName());
    	
    	setFieldTextOrHideEmpty(location.getDescription(), NO_LINKIFY, R.id.descriptionLayout, R.id.descriptionText);
		
        StringBuilder products = new StringBuilder();
        for(EntityWithComment product : location.getProducts())
        {
        	if (products.length() > 0) products.append(", ");
         	products.append(product.toString());
        }
		
        StringBuilder activities = new StringBuilder();
        for(EntityWithComment activity : location.getActivities())
        {
        	if (activities.length() > 0) activities.append(", ");
         	activities.append(activity.toString());
        }
		
    	setFieldTextOrHideEmpty(products.toString(), NO_LINKIFY, R.id.productionLayout, R.id.productionText);
    	setFieldTextOrHideEmpty(activities.toString(), NO_LINKIFY, R.id.activitiesLayout, R.id.activitiesText);
    	
    	LocationContact contact = location.getContact();
    	setFieldTextOrHideEmpty(contact.email, Linkify.EMAIL_ADDRESSES, R.id.emailLayout, R.id.emailText);
    	setFieldTextOrHideEmpty(contact.web, Linkify.WEB_URLS, R.id.webLayout, R.id.webText);
    	setFieldTextOrHideEmpty(contact.eshop, Linkify.WEB_URLS, R.id.eshopLayout, R.id.eshopText);
    	setFieldTextOrHideEmpty(contact.phone, Linkify.PHONE_NUMBERS, R.id.phoneLayout, R.id.phoneText);

    	setFieldTextOrHideEmpty(contact.formatAddress("\n"), Linkify.MAP_ADDRESSES, R.id.addressLabel, R.id.addressText);
    	
    	location.fillDeliveryOptions(view, R.id.containerLayout, R.id.containerDistributionPlacesLayout,  R.id.containerDistributionPlacesText, R.id.customConteinerDistributionText);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.bookmarkIcon:
				location.setBookmarked(!location.isBookmarked());
				this.updateBookmarked();
			break;
			
			case R.id.feedback_button:
				location.editLocation(this.context);
			break;
		}
	}
}
