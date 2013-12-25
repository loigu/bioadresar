package cz.hnutiduha.bioadresar.editFarm;

import android.content.Intent;
import android.content.res.Resources;
import cz.hnutiduha.bioadresar.R;
import cz.hnutiduha.bioadresar.data.FarmInfo;
import cz.hnutiduha.bioadresar.data.HnutiduhaFarmDb;

public class EditFarmActivity extends AddFarmActivity {

	// FIXME: override connector action
	
	void updateTitle(NamedFragment fr)
	{
		Resources res = getResources();
        setTitle(res.getString(R.string.edit_farm) + ": " + res.getString(fr.getName()));
	}
	
	public EditFarmActivity() {
		super();
	}
	
	FarmInfo getFarm() {
        Intent myIntent= getIntent();
        long farmId = myIntent.getLongExtra("farmId", FarmInfo.INVALID_FARM_ID);
        
        HnutiduhaFarmDb db = HnutiduhaFarmDb.getDefaultDb(this);
        return db.getFarm(farmId);
	}

}
