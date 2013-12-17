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

package cz.hnutiduha.bioadresar.detail;

import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;
import cz.hnutiduha.bioadresar.data.FarmContact;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.StringifiedFromDb;

public class DetailFragment extends SherlockFragment implements OnClickListener{
	
	// we expect only one detail activity to be shown at a time
	private FarmInfo farm = null;
	View view = null;
	ImageView bookmarkView = null;
	Context context;
    
	public DetailFragment(FarmInfo farm, Context context) {
		super();
		
		this.farm = farm;
		this.context = context;
	}
	
    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	view = inflater.inflate(R.layout.detail_view, container, false);
        
        bookmarkView = (ImageView)view.findViewById(R.id.bookmarkIcon);
    	bookmarkView.setOnClickListener(this);
    	
    	ImageView map = (ImageView)view.findViewById(R.id.mapIcon);
    	farm.setToMapListener(map);
        
        return view;
    }
    
    public void onResume()
    {
    	super.onResume();
    	if (farm != null)
    		fillFarmInfo();
    	else
    	{
    		Log.e("system", "wtf: someone accessed DetailActivity without assiging farm");
    		// or exception, or stacktrace...
    	}
    	
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
    
    private void fillListFromIterator(StringBuilder out, HnutiduhaFarmDb db, Iterator it)
    {
    	while(it.hasNext())
    	{
			out.append(((StringifiedFromDb)it.next()).toString());
			if (it.hasNext())
				out.append(", ");
    	}
    }
    
    
    private void updateBookmarked()
    {	
    	if (farm.isBookmarked())
    		bookmarkView.setImageResource(R.drawable.bookmarked_icon);
    	else
    		bookmarkView.setImageResource(R.drawable.bookmark_icon);
    }
    
    private void fillFarmInfo()
    {	
    	updateBookmarked();
    	
    	TextView field = (TextView) view.findViewById(R.id.farmName);
    	field.setText(farm.name);
    	
    	setFieldTextOrHideEmpty(farm.getDescription(), NO_LINKIFY, R.id.descriptionLayout, R.id.descriptionText);
        
		HnutiduhaFarmDb db = HnutiduhaFarmDb.getDefaultDb(context);
		
        StringBuilder products = new StringBuilder();
		fillListFromIterator(products, db, farm.getProducts().iterator());
		
		
		// if there is no products, try to use categories
		Iterator<Long> it = farm.getCategories().iterator();
		if (products.length() == 0)	{
			while (it.hasNext()) {
				products.append(db.getCategoryName(it.next()));
				if (it.hasNext())
					products.append(", ");
			}
		}

        StringBuilder activities = new StringBuilder();
		fillListFromIterator(activities, db, farm.getActivities().iterator());
		
    	setFieldTextOrHideEmpty(products.toString(), NO_LINKIFY, R.id.productionLayout, R.id.productionText);
    	setFieldTextOrHideEmpty(activities.toString(), NO_LINKIFY, R.id.activitiesLayout, R.id.activitiesText);
    	
    	FarmContact contact = farm.getFarmContact();
    	setFieldTextOrHideEmpty(contact.email, Linkify.EMAIL_ADDRESSES, R.id.emailLayout, R.id.emailText);
    	setFieldTextOrHideEmpty(contact.web, Linkify.WEB_URLS, R.id.webLayout, R.id.webText);
    	setFieldTextOrHideEmpty(contact.eshop, Linkify.WEB_URLS, R.id.eshopLayout, R.id.eshopText);
        
        LinearLayout phones = (LinearLayout) view.findViewById(R.id.phoneListLayout);
        if (contact.phoneNumbers != null && contact.phoneNumbers.size() > 0)
        {
	        Iterator<String> phoneIterator = contact.phoneNumbers.iterator();
	        while(phoneIterator.hasNext())
	        {
	        	TextView phone = new TextView(phones.getContext());
	        	phone.setText(phoneIterator.next());
	        	phone.setTextColor(getResources().getColor(R.color.DuhaTextGray));
	            Linkify.addLinks(phone, Linkify.PHONE_NUMBERS);
	            phones.addView(phone);
	        }
        }
        else
        {
            View phonesLayout = view.findViewById(R.id.phonesLayout);
            phonesLayout.setVisibility(TextView.GONE);
        }
        
        String address = "";
        if (contact.street != null && contact.street.length() != 0)
        	address += contact.street;
        if (contact.city != null && contact.city.length() != 0)
        {
        	if (!address.equals(""))
        		address += ", ";
        	address += contact.city;
        }
    	setFieldTextOrHideEmpty(address, Linkify.MAP_ADDRESSES, R.id.addressLabel, R.id.addressText);
    	
    	farm.fillDeliveryOptions(view, R.id.containerLayout, R.id.containerDistributionPlacesLayout,  R.id.containerDistributionPlacesText, R.id.customConteinerDistributionText);
    }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bookmarkIcon)
		{
			farm.setBookmarked(!farm.isBookmarked());
	    	this.updateBookmarked();
		}
	}
}
