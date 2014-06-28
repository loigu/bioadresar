package cz.hnutiduha.bioadresar;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public abstract class TrackablePreferenceActivity extends SherlockPreferenceActivity{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTracker.setLastActivity(this);
    }
    protected void onResume() {
        super.onResume();
        ActivityTracker.setLastActivity(this);
    }
    protected void onPause() {
        ActivityTracker.clearLastActivity();
        super.onPause();
    }
    protected void onDestroy() {        
    	ActivityTracker.clearLastActivity();
        super.onDestroy();
    }
}
