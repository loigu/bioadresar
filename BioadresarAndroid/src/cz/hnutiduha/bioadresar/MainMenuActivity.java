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

package cz.hnutiduha.bioadresar;

import com.actionbarsherlock.app.SherlockActivity;

import cz.hnutiduha.bioadresar.data.LocationCache;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class MainMenuActivity extends SherlockActivity implements OnClickListener{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    
	    // start listening for location
	    LocationCache.startListener(this);
	    
	    setContentView(R.layout.main_menu);
	    Log.w("gui", "main ui started");
	    
        View item = this.findViewById(R.id.listLink);
        item.setOnClickListener(this);
        item = this.findViewById(R.id.bookmarkListLink);
        item.setOnClickListener(this);
        item = this.findViewById(R.id.mapLink);
        item.setOnClickListener(this);
    }
    
    public void onStart()
    {
    	super.onStart();
    }
    
	@Override
	public void onClick(View v) {
		MenuHandler.idActivated(this,v.getId());
	}
		
}