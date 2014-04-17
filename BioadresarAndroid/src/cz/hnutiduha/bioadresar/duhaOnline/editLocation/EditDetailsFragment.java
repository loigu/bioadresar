package cz.hnutiduha.bioadresar.duhaOnline.editLocation;


import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.DataSourceFactory;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;
import cz.hnutiduha.bioadresar.duhaOnline.data.CoexDatabase;
import cz.hnutiduha.bioadresar.duhaOnline.data.DeliveryOptions;
import cz.hnutiduha.bioadresar.duhaOnline.data.CoexLocation;
import cz.hnutiduha.bioadresar.duhaOnline.data.EntityWithComment;
import cz.hnutiduha.bioadresar.layout.FlowLayout;
import cz.hnutiduha.bioadresar.util.StringOperations;

class SomethingHolder<T extends EntityWithComment> implements OnClickListener {
	Context context;
	List<T> list;
	T[] options;
	FlowLayout layout;
	Dialog addDialog;
	
	
	class AddListener implements OnClickListener {
		SomethingHolder<T> holder;
		Dialog dialog;
		
		AddListener(SomethingHolder<T> holder, Dialog dialog)
		{
			this.holder = holder;
			this.dialog = dialog;
		}
		

		@Override
	    public void onClick(View v) {
			holder.addSomething((T)v.getTag(R.id.listContentTag));
			dialog.dismiss();
			Log.d("gui", "dialog hidden");
		}
	}
	
	private void putListToLayout()
	{
		layout.removeAllViews();
		for (EntityWithComment something : list)
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
		LayoutInflater inflater = LayoutInflater.from(context);
		addDialog = new Dialog(context, R.style.Dialog);
		
		// your layout file
		View dialogView = inflater.inflate(R.layout.list_with_header, null);
		addDialog.setContentView(dialogView);
		((TextView)addDialog.findViewById(R.id.headerText)).setText(titleResId); // TODO: to uppercase
		
		Window window = addDialog.getWindow();
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		window.setGravity(Gravity.CENTER);
		
		AddListener addListener = new AddListener(this, addDialog);
		LinearLayout list = (LinearLayout) addDialog.findViewById(R.id.itemsLayout);
		
		for (T option : options)
		{
			LinearLayout item = (LinearLayout) inflater.inflate(R.layout.list_with_header_item, null);
			((TextView)item.findViewById(R.id.text)).setText(option.toString()); // TODO: to uppercase
			item.setOnClickListener(addListener);
			item.setTag(R.id.listContentTag, option);
			list.addView(item);
		}

	}

	public void showAddDialog(int titleResId)
	{
		if (addDialog == null)
			buildDialog(titleResId);
    	addDialog.show();
    	Log.d("gui", "dialog shown");
    }
	
	private void addButton(EntityWithComment something)
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
	private CoexLocation location;
	private Context context;
	SomethingHolder<EntityWithComment> productionHolder;
	SomethingHolder<EntityWithComment> activitiesHolder;
	private EditText description;
	private LinearLayout pickupPlacesList;
	private TextView customDeliveryYes, customDeliveryNo;
	DeliveryOptions deliveryOpts;
    
	public EditDetailsFragment(CoexLocation farm, Context context) {
		super();
		
		this.location = farm;
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
        
        return me;
    }
    
    public void onDestroyView()
    {
    	super.onDestroyView();
    	pickupPlacesList.removeAllViewsInLayout();
    }
    
    public void onResume()
    {
    	super.onResume();
    	loadFromFarm(getView());
    }
    
    private boolean validate()
    {
    	return true;
    }
    
    private void loadFromFarm(View me)
    {
    	description.setText(location.getDescription());
    	
    	CoexDatabase db = (CoexDatabase)DataSourceFactory.getDataSource(CoexDatabase.SOURCE_ID, context);
        
    	FlowLayout productsLayout = (FlowLayout) me.findViewById(R.id.productListLayout);
    	productionHolder = new SomethingHolder<EntityWithComment>(context, location.getProducts(),
    			db.getProductsSortedByName(), productsLayout);
    	FlowLayout activitiesLayout = (FlowLayout) me.findViewById(R.id.activityListLayout);
    	activitiesHolder = new SomethingHolder<EntityWithComment>(context, location.getActivities(),
    			db.getActivitiesSortedByName(), activitiesLayout);
    	
    	if (deliveryOpts.placesWithTime != null && deliveryOpts.placesWithTime.length > 0)
    	{
    		for (String placeAndTime : deliveryOpts.placesWithTime)
    			if (!TextUtils.isEmpty(placeAndTime))
    			{
    				Log.d("ui", "adding place and time " + placeAndTime);
    				addPickupPlace(placeAndTime);
    			}
    	}
    	else
    	{
    		addPickupPlace("");
    	}
    }
    
    private void updateFarm()
    {
    	location.setDescription(StringOperations.getStringFromEditBox(description));
    	
    	location.setProducts(productionHolder.getList());
    	location.setActivities(activitiesHolder.getList());
    	
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
	    		String placeWithTime = StringOperations.getStringFromEditBox((EditText)pickupPlacesList.getChildAt(i).findViewById(R.id.placeAndTime));
	    		if (!TextUtils.isEmpty(placeWithTime))
	    		{
	    			Log.d("data", "storing place " + placeWithTime + " as " + j);
	    			deliveryOpts.placesWithTime[j++] = placeWithTime;
	    		}
	    	}
	    	
	    	if (j == 0)
	    		deliveryOpts.placesWithTime = null;
	    	else if (j < childCount)
	    	{
	    		String[] tmp = new String[j];
	    		for (j--; j >= 0; j--)
	    			tmp[j] = deliveryOpts.placesWithTime[j];
	    		deliveryOpts.placesWithTime = tmp;
	    	}
    	}
    	location.setDelieryInfo(deliveryOpts);
    }
    
    private void addPickupPlace(String text)
    {	
    	View p = RelativeLayout.inflate(context, R.layout.edit_pickup_place, null);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
    	params.bottomMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
    	p.setLayoutParams(params);
    	
    	p.findViewById(R.id.removeButton).setOnClickListener(this);
    	
    	pickupPlacesList.addView(p);
    	((EditText)((ViewGroup)p).findViewById(R.id.placeAndTime)).setText(text);
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
				addPickupPlace("");
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
				activitiesHolder.showAddDialog(R.string.activities);
			break;
			
			case R.id.production:
				productionHolder.showAddDialog(R.string.production);
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
