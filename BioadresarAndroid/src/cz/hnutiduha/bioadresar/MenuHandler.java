package cz.hnutiduha.bioadresar;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import cz.hnutiduha.bioadresar.editFarm.AddFarmActivity;
import cz.hnutiduha.bioadresar.list.ListActivity;
import cz.hnutiduha.bioadresar.list.BookmarksListActivity;
import cz.hnutiduha.bioadresar.map.MapActivity;

public class MenuHandler implements OnNavigationListener{
	
	private Context context = null;
	
	private MenuHandler(Context context) {
		this.context = context;
	}
	
	public static void installDropDown(ActionBar bar, Context context)
	{
		/* NOTE: this is basic dropdown menu example. needs layouts and reactions...
		if (hnd == null)
			hnd = new MenuHandler(context);
		
    	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    	
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(context, R.array.activityList,
		          android.R.layout.simple_spinner_dropdown_item);
		
		bar.setListNavigationCallbacks(mSpinnerAdapter, hnd);
		*/
	}
	
	public static boolean fillMenu(final Menu menu, Context context, SearchManager searchManager, ComponentName componentName)
	{
		MenuItem foo = menu.add(0, R.id.homeLink, 1, "");
		foo.setIcon(R.drawable.menu_icon);
		foo.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		if (searchManager != null)
		{	
			SearchView searchView = new SearchView(context);
			
	        searchView.setQueryHint(context.getText(R.string.search_hint));
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

	        
	        searchView.setSubmitButtonEnabled(true);
	        searchView.setIconifiedByDefault(false);

	        menu.add(0, Menu.NONE, 0, "")
	            .setIcon(R.drawable.search_icon)
	            .setActionView(searchView)
	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		
		/* NOTE: dropDown is used now
		SubMenu subMenu = menu.addSubMenu("");
		subMenu.setIcon(R.drawable.menu_icon);
		
		MenuItem map = subMenu.add(0, R.id.mapLink, Menu.NONE, R.string.map_tab_title);
        map.setIcon(android.R.drawable.ic_menu_mapmode);
        
        MenuItem list = subMenu.add(0, R.id.listLink, Menu.NONE, R.string.list_tab_title);
        list.setIcon(android.R.drawable.ic_menu_agenda);

        map.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
       	list.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem item;
		
		item = subMenu.add(0, R.id.bookmarkListLink, Menu.NONE, R.string.bookmarkListLabel);
		item.setIcon(android.R.drawable.btn_star_big_on);
		item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
		/*
		item = menu.add(0, R.id.configLink, Menu.NONE, R.string.config_label);
		item.setIcon(android.R.drawable.ic_menu_manage);
		item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		item = menu.add(0, R.id.locationLabel, Menu.NONE, R.string.renewLocationLabel);
		item.setIcon(android.R.drawable.ic_menu_mylocation);
		
		item = menu.add(0, R.id.aboutLink, Menu.NONE, R.string.about_label);
		item.setIcon(android.R.drawable.ic_menu_info_details);
		*/

		return true;
	}
    
    public static boolean idActivated(Context context, int id)
    {
		if (id != R.id.locationLabel)
		{
			return showActivity(context, id);
		}
		return false;
    }
    
    protected static boolean showActivity(Context context, int id)
    {
		Intent target = null;
		switch (id)
		{
			case R.id.listLink:
				target = new Intent(context, ListActivity.class);
				break;
				
			case R.id.mapLink:
				target = new Intent(context, MapActivity.class);
				break;
				
				/*
			case R.id.configLink:
				target = new Intent(context, ConfigActivity.class);
				break;
				
			case R.id.aboutLink:
				target = new Intent(context, AboutActivity.class);
				break;
				*/
				
			case R.id.bookmarkListLink:
				target = new Intent(context, BookmarksListActivity.class);
				break;
				
			case R.id.homeLink:
				target = new Intent(context, MainMenuActivity.class);
				break;
				
			case R.id.addFarmLink:
				target = new Intent(context, AddFarmActivity.class);
				break;
		
		}
		if (target != null)
		{
			context.startActivity(target);
			return true;
		}
		return false;
    }

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		return MenuHandler.showActivity(context, (int)itemId);
	}

}
