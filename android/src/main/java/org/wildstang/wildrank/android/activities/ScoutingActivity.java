package org.wildstang.wildrank.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.wildstang.wildrank.android.interfaces.IScoutingFragmentHost;
import org.wildstang.wildrank.android.utils.JSONBundleTools;

public abstract class ScoutingActivity extends FragmentActivity implements IScoutingFragmentHost {

    protected Bundle scoutingViewsState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the Bundle of scoutingViewsState
        if (savedInstanceState != null) {
            scoutingViewsState = savedInstanceState.getBundle("viewsState");
        } else {
            scoutingViewsState = new Bundle();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                displayNavigationWarning();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        displayNavigationWarning();
    }

    private void displayNavigationWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Confirm Navigation");

        // set dialog message
        alertDialogBuilder.setMessage("You will lose all scouting data if you navigate away without saving.").setCancelable(true).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ScoutingActivity.this.finish();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }

        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    public Bundle getScoutingViewStateBundle() {
        return scoutingViewsState;
    }

    public Bundle writeScoutingDataToBundle() {
        Bundle b = scoutingViewsState;
        try {
            Log.d("bundle", JSONBundleTools.writeBundleToJSONObject(b).toString(4));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putBundle("viewsState", scoutingViewsState);
    }

}
