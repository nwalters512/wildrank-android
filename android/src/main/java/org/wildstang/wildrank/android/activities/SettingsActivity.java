package org.wildstang.wildrank.android.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import org.wildstang.wildrank.android.fragments.PasswordProtectionFragment;
import org.wildstang.wildrank.android.fragments.PasswordProtectionFragment.IPasswordProtectionCallbacks;
import org.wildstang.wildrank.android.fragments.SettingsFragment;
import org.wildstang.wildrank.android.utils.Keys;

/*
 * Activity that displays the settings for the app. This includes defining
 * which tablet this is (red_1, blue_2, etc.) and who the current scouter is.
 */

public class SettingsActivity extends Activity implements IPasswordProtectionCallbacks {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		boolean superUserMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Keys.SUPER_USER_MODE, false);
		if (superUserMode) {
			passwordSuccess();
		} else {
			PasswordProtectionFragment f = new PasswordProtectionFragment();
			f.setCallbacks(this);
			f.setColorTheme(PasswordProtectionFragment.THEME_LIGHT);
			getFragmentManager().beginTransaction().replace(android.R.id.content, f).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void passwordSuccess() {
		Fragment f = new SettingsFragment();

		getFragmentManager().beginTransaction().replace(android.R.id.content, f).commit();

	}

}
