package cz.hnutiduha.bioadresar.editFarm;

import android.support.v4.app.Fragment;

public interface FragmentNavigator {
	public void nextFragment(Fragment originatingFragment);
	public void previousFragment(Fragment originatingFragment);
	public void fragmentWarning(String text);
	public void fragmentWarning(int emailnonvalid);
}
