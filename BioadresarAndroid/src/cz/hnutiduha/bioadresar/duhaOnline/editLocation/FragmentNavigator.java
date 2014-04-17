package cz.hnutiduha.bioadresar.duhaOnline.editLocation;

import android.support.v4.app.Fragment;

public interface FragmentNavigator {
	public void nextFragment(Fragment originatingFragment);
	public void previousFragment(Fragment originatingFragment);
	public void fragmentNotification(int stringId);
	public void fragmentNotification(String text);
	
	public void showProgress();
	public void hideProgress();
}
