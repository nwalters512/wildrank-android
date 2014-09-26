package org.wildstang.wildrank.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.wildstang.wildrank.android.R;
import org.wildstang.wildrank.android.tasks.TaskFragment;
import org.wildstang.wildrank.android.tasks.TaskFragmentSetUpFlashDrive;

public class SetupFlashDriveActivity extends Activity implements TaskFragment.TaskCallbacks {

    private TaskFragmentSetUpFlashDrive setUpFlashDriveTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_flash_drive);

        configureFlashDrive();
    }

    private void configureFlashDrive() {
        resetFragments();
        FragmentManager fm = getFragmentManager();
        setUpFlashDriveTaskFragment = new TaskFragmentSetUpFlashDrive();
        fm.beginTransaction().add(setUpFlashDriveTaskFragment, "setupFlashDriveFragment").commit();
    }

    private void resetFragments() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (fm.findFragmentByTag("setupFlashDriveFragment") != null) {
            ft.remove(fm.findFragmentByTag("setupFlashDriveFragment"));
        }
        ft.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setup_flash_drive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPreExecute(TaskFragment.TaskType type) {

    }

    @Override
    public void onProgressUpdate(TaskFragment.TaskType type, TaskFragment.ProgressUpdateInfo info) {

    }

    @Override
    public void onCancelled(TaskFragment.TaskType type) {

    }

    @Override
    public void onPostExecute(TaskFragment.TaskType type) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // Set title
        alertDialogBuilder.setTitle("Setup");

        // Set dialog message
        alertDialogBuilder
                .setMessage(
                        "Setup successful!")
                .setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        // Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // Show it
        alertDialog.show();
    }
}
