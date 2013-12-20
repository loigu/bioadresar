package cz.hnutiduha.bioadresar.editFarm;


import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.ActivityWithComment;
import cz.hnutiduha.bioadresar.data.DeliveryOptions;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;
import cz.hnutiduha.bioadresar.data.ProductWithComment;
import cz.hnutiduha.bioadresar.data.StringifiedFromDb;
import cz.hnutiduha.bioadresar.layout.FlowLayout;

class SomethingHolder<T extends StringifiedFromDb> implements OnClickListener {
	Context context;
	List<T> list;
	T[] options;
	FlowLayout layout;
	Builder addDialog = null;
	
	
	class AddListener implements DialogInterface.OnClickListener {
		SomethingHolder<T> holder;
		T[] values;
		
		AddListener(SomethingHolder<T> holder, T[] values)
		{
			this.holder = holder;
			this.values = values;
		}
		

		@Override
	    public void onClick(DialogInterface dialog, int which) {
			holder.addSomething(values[which]);
			dialog.dismiss();
		}
	}
	
	private void putListToLayout()
	{
		layout.removeAllViews();
		for (StringifiedFromDb something : list)
		{
			addButton(something);
		}
	}
	
	public List<T> getList()
	{
		return list;
	}
	
	protected void buildDialog(int titleResId)
	{
		ArrayAdapter<T> adapter = new ArrayAdapter<T>(context,
				android.R.layout.simple_spinner_dropdown_item, options);
		
		addDialog = new AlertDialog.Builder(context)
			.setTitle(titleResId)
			.setAdapter(adapter, this.new AddListener(this, options));
	}

	public void showAddDialog(int titleResId)
	{
		if (addDialog == null)
			buildDialog(titleResId);
    	addDialog.show();
    }
	
	private void addButton(StringifiedFromDb something)
	{
		Button b = (Button) LayoutInflater.from(context).inflate(R.layout.item_button, null);
		b.setText(something.toString());
		b.setTag(R.id.buttonTag, something);
		b.setLayoutParams(new FlowLayout.LayoutParams(5,5));
		b.setOnClickListener(this);
		layout.addView(b);
	}
	
	SomethingHolder(Context context, List<T> list, T[] options, FlowLayout layout)
	{
		this.context = context;
		this.list = list;
		this.layout = layout;
		this.options = options;
		
		putListToLayout();
	}
	
	private void addSomething(T something)
	{ 
		for (T existing : list)
		{
			if (existing.toString().equals(something.toString()))
			{
				return;
			}
		}
		
		list.add(something);
		addButton(something);
	}

	@Override
	public void onClick(View v) {
		Object tag = v.getTag(R.id.buttonTag);
		if (tag != null)
		{
			list.remove(tag);
		}
		
		layout.removeView(v);
	}
}

public class EditDetailsFragment extends SherlockFragment implements OnClickListener, NamedFragment{
	private FragmentNavigator fragmentNavigator;
	private FarmInfo farm;
	private Context context;
	SomethingHolder<ProductWithComment> productionHolder;
	SomethingHolder<ActivityWithComment> activitiesHolder;
	private EditText description;
	private LinearLayout pickupPlacesList;
	private TextView customDeliveryYes, customDeliveryNo;
	DeliveryOptions deliveryOpts;
    
	public EditDetailsFragment(FarmInfo farm, Context context) {
		super();
		
		this.farm = farm;
		deliveryOpts = farm.getDeliveryInfo();
		if (deliveryOpts == null)
		{
			deliveryOpts = new DeliveryOptions();
		}
		this.context = context;
		
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View me = inflater.inflate(R.layout.edit_details, container, false);
        
        me.findViewById(R.id.nextButton).setOnClickListener(this);
        me.findViewById(R.id.backButton).setOnClickListener(this);
    	
    	me.findViewById(R.id.production).setOnClickListener(this);
    	me.findViewById(R.id.activities).setOnClickListener(this);
    	
    	description = (EditText) me.findViewById(R.id.description);
    	
    	customDeliveryYes = (TextView)me.findViewById(R.id.customDeliveryYes);
    	customDeliveryNo = (TextView)me.findViewById(R.id.customDeliveryNo);
    	customDeliveryYes.setOnClickListener(this);
    	customDeliveryNo.setOnClickListener(this);
    	
    	pickupPlacesList = (LinearLayout)me.findViewById(R.id.pickupPlacesList);
    	
    	me.findViewById(R.id.addButton).setOnClickListener(this);
    	
    	loadFromFarm(me);
        
        return me;
    }
    
    private boolean validate()
    {
    	return true;
    }
    
    private void loadFromFarm(View me)
    {
    	description.setText(farm.getDescription());
    	
        HnutiduhaFarmDb db = HnutiduhaFarmDb.getDefaultDb(context);
        
    	FlowLayout productsLayout = (FlowLayout) me.findViewById(R.id.productListLayout);
    	productionHolder = new SomethingHolder<ProductWithComment>(context, farm.getProducts(),
    			db.getProductsSortedByName(), productsLayout);
    	FlowLayout activitiesLayout = (FlowLayout) me.findViewById(R.id.activityListLayout);
    	activitiesHolder = new SomethingHolder<ActivityWithComment>(context, farm.getActivities(),
    			db.getActivitiesSortedByName(), activitiesLayout);
    	
    	pickupPlacesList.removeAllViews();
    	if (deliveryOpts.placesWithTime != null && deliveryOpts.placesWithTime.length > 0)
    	{
    		for (String placeAndTime : deliveryOpts.placesWithTime)
    			if (!placeAndTime.isEmpty())
    				addPickupPlace().setText(placeAndTime);
    	}
    	else
    	{
    		addPickupPlace();
    	}
    }
    
    private void updateFarm()
    {
    	farm.setDescription(description.getText().toString());
    	
    	farm.setProducts(productionHolder.getList());
    	farm.setActivities(activitiesHolder.getList());
    	
    	int childCount = pickupPlacesList.getChildCount();
    	if (childCount == 0)
    	{
    		deliveryOpts.placesWithTime = null;
    	}
    	else
    	{
    		int j = 0;
    		deliveryOpts.placesWithTime = new String[childCount];
	    	for (int i = 0; i < childCount; i++)
	    	{
	    		String placeWithTime = ((TextView)pickupPlacesList.getChildAt(i).findViewById(R.id.placeAndTime)).getText().toString();
	    		if (!placeWithTime.isEmpty())
	    			deliveryOpts.placesWithTime[j++] = placeWithTime;
	    	}
	    	
	    	if (j == 0)
	    		deliveryOpts.placesWithTime = null;
	    	else if (j < childCount)
	    		deliveryOpts.placesWithTime = java.util.Arrays.copyOf(deliveryOpts.placesWithTime, j);
    	}
    	farm.setDelieryInfo(deliveryOpts);
    }
    
    private TextView addPickupPlace()
    {
    	View p = LayoutInflater.from(context).inflate(R.layout.edit_pickup_place, null);
    	View minus = p.findViewById(R.id.removeButton);
    	minus.setOnClickListener(this);
    	pickupPlacesList.addView(p);
    	
    	return (TextView)p.findViewById(R.id.placeAndTime);
    }
    
    private void removePickupPlace(View minusButton)
    {
    	View parent = (View)minusButton.getParent();
    	if (pickupPlacesList.getChildCount() == 1)
    	{
    		((TextView)parent.findViewById(R.id.placeAndTime)).setText("");
    	}
    	else
    	{
        	pickupPlacesList.removeView(parent);	
    	}
    }
    
    private void switchCustomDelivery(boolean yes)
    {
    	deliveryOpts.customDistribution = yes;
    	
    	if (yes)
    	{
    		customDeliveryYes.setBackgroundResource(R.drawable.btn_checkbox_selected);
    		customDeliveryNo.setBackgroundResource(R.drawable.btn_checkbox_unselected);
    	}
    	else
    	{
    		customDeliveryYes.setBackgroundResource(R.drawable.btn_checkbox_unselected);
    		customDeliveryNo.setBackgroundResource(R.drawable.btn_checkbox_selected);
    	}
    }
    
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.removeButton:
				removePickupPlace(v);
			break;
				
			case R.id.addButton:
				addPickupPlace();
			break;
				
			case R.id.customDeliveryYes:
				switchCustomDelivery(true);
			break;
			case R.id.customDeliveryNo:
				switchCustomDelivery(false);
			break;
				
			case R.id.nextButton:
			{
				if (validate())
				{
					updateFarm();
					fragmentNavigator.nextFragment(this);
				}
				else
				{
					// FIXME: else write error
				}
			}
			break;
			
			case R.id.backButton:
			{
				if (validate())
					updateFarm();
				fragmentNavigator.previousFragment(this);
			}
			break;
			
			case R.id.activities:
				activitiesHolder.showAddDialog(R.string.select_activities);
			break;
			
			case R.id.production:
				productionHolder.showAddDialog(R.string.select_products);
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
		return R.string.farmDetails;
	}
}
