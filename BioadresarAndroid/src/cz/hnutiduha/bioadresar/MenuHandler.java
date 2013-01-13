package cz.hnutiduha.bioadresar;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import cz.hnutiduha.bioadresar.about.AboutActivity;
import cz.hnutiduha.bioadresar.config.ConfigActivity;
import cz.hnutiduha.bioadresar.data.LocationCache;
import cz.hnutiduha.bioadresar.list.ListActivity;
import cz.hnutiduha.bioadresar.map.MapActivity;

public class MenuHandler {
	
	public static boolean fillMenu(final Menu menu)
	{
		// TODO: do this by xml
		MenuItem item;
        item = menu.add(0, R.id.mapLink, Menu.NONE, R.string.map_tab_title);
        item.setIcon(android.R.drawable.ic_menu_mapmode);
        
        item = menu.add(0, R.id.listLink, Menu.NONE, R.string.list_tab_title);
        item.setIcon(android.R.drawable.ic_menu_agenda);
        
		item = menu.add(0, R.id.locationLabel, Menu.NONE, R.string.renewLocationLabel);
		item.setIcon(android.R.drawable.ic_menu_mylocation);
		
		item = menu.add(0, R.id.configLink, Menu.NONE, R.string.config_label);
		item.setIcon(android.R.drawable.ic_menu_manage);
		
		item = menu.add(0, R.id.aboutLink, Menu.NONE, R.string.about_label);
		item.setIcon(android.R.drawable.ic_menu_info_details);

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
		case R.id.configLink:
			target = new Intent(context, ConfigActivity.class);
			break;
		case R.id.aboutLink:
			target = new Intent(context, AboutActivity.class);
			break;
		}
		
		if (target != null)
		{
			context.startActivity(target);
			return true;
		}
		return false;
    }

}
