package org.wildstang.wildrank.android.interfaces;

import android.os.Bundle;

/*
 * Defines an interface for activites that host ScoutingFragments.
 * Allows child fragments to retreive the bundle describing the scouting 
 * views in that activity for saving and restoring state.
 */
public interface IScoutingFragmentHost {

	public Bundle getScoutingViewStateBundle();

}
