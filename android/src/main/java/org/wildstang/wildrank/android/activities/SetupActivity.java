package org.wildstang.wildrank.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.tasks.TaskFragment;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo.ProgressUpdaterState;
import org.wildstang.wildrank.android.tasks.TaskFragment.TaskType;
import org.wildstang.wildrank.android.tasks.TaskFragmentLoadEventDetails;
import org.wildstang.wildrank.android.tasks.TaskFragmentLoadTeamList;
import org.wildstang.wildrank.android.tasks.TaskFragmentSynchronizeWithFlashDrive;

import java.io.DataOutputStream;

public class SetupActivity extends Activity implements TaskFragment.TaskCallbacks {

	public static final int REQUEST_CODE_FINISHED = 78;

	public static final int RESULT_CODE_MOUNT = 34;
	public static final int RESULT_CODE_UNMOUNT = 45;

	private TaskFragmentLoadEventDetails loadEventDetailsTaskFragment;
	private TaskFragmentSynchronizeWithFlashDrive synchronizeWithUSBTaskFragment;
	private TaskFragmentLoadTeamList loadPitScoutingTeamsListTaskFragment;
	private ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Request root access
		try {
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("exit\n");
			os.flush();
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		promptForDataSource();
	}

	@Override
	public void onPreExecute(TaskType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProgressUpdate(TaskType type, ProgressUpdateInfo info) {
		if (progress == null) {
			progress = new ProgressDialog(this);
			progress.setCancelable(false);
			progress.setTitle("Loading...");
			progress.setMessage(info.message);
		}
		if (type == TaskType.TASK_LOAD_EVENT_DETAILS || type == TaskType.TASK_LOAD_TEAM_LIST || type == TaskType.TASK_SYNCHRONIZE_WITH_FLASH_DRIVE) {
			progress.setIndeterminate(true);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
		if (info.state == ProgressUpdaterState.COMPLETE) {
			progress.dismiss();
			progress = null;
		} else {
			progress.show();
		}

	}

	@Override
	public void onCancelled(TaskType type) {
		// Nothing to do here
	}

	@Override
	public void onPostExecute(TaskType type) {
		if (type == TaskType.TASK_LOAD_EVENT_DETAILS) {
			loadTeamList();
		} else if (type == TaskType.TASK_LOAD_TEAM_LIST) {
			startActivityForResult(new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS), RESULT_CODE_UNMOUNT);
			Toast.makeText(this, "Scroll down, press \"Unmount\", press back button.", Toast.LENGTH_LONG).show();
		} else if (type == TaskType.TASK_SYNCHRONIZE_WITH_FLASH_DRIVE) {
			loadEventDetails();
			DataManager.prepareForEject();
			resetFragments();
		}
	}

	private void promptForDataSource() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// Set title
		alertDialogBuilder.setTitle("Load tablet data");

		// Set dialog message
		alertDialogBuilder.setMessage("Do you wish to load tablet data from a USB drive?").setCancelable(false).setPositiveButton("USB Drive", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				synchronizeWithFlashDrive();
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		// Create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// Show it
		alertDialog.show();
	}

	private void resetFragments() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		if (fm.findFragmentByTag("downloadMatchList") != null) {
			ft.remove(fm.findFragmentByTag("downloadMatchList"));
		}
		if (fm.findFragmentByTag("loadTeamsFragment") != null) {
			ft.remove(fm.findFragmentByTag("loadTeamsFragment"));
		}
		if (fm.findFragmentByTag("synchronizeWithUSB") != null) {
			ft.remove(fm.findFragmentByTag("synchronizeWithUSB"));
		}
		ft.commit();
	}

	private void loadTeamList() {
		resetFragments();
		FragmentManager fm = getFragmentManager();
		loadPitScoutingTeamsListTaskFragment = new TaskFragmentLoadTeamList();
		fm.beginTransaction().add(loadPitScoutingTeamsListTaskFragment, "loadTeamsFragment").commit();
	}

	/*
	 * Initializes a TaskFragment that loads event details from the local event
	 * details files
	 */
	private void loadEventDetails() {
		resetFragments();
		FragmentManager fm = getFragmentManager();
		loadEventDetailsTaskFragment = new TaskFragmentLoadEventDetails();
		fm.beginTransaction().add(loadEventDetailsTaskFragment, "downloadMatchList").commit();
	}

	/*
	 * Initializes a TaskFragment that synchronizes internal storage with the
	 * flash drive. Used to load any previously collected scouting/match data
	 * from the flash drive after loading match and pit lists
	 */
	private void synchronizeWithFlashDrive() {
		resetFragments();
		if (DataManager.isFlashDriveConnected(this)) {
			FragmentManager fm = getFragmentManager();
			synchronizeWithUSBTaskFragment = new TaskFragmentSynchronizeWithFlashDrive();
			fm.beginTransaction().add(synchronizeWithUSBTaskFragment, "synchronizeWithUSB").commit();
		} else {
			displayFlashDriveWarning();
		}
	}

	/*
	 * Displays a warning if the flash drive is not connected and prompts the
	 * user to connect one
	 */
	private void displayFlashDriveWarning() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// Set title
		alertDialogBuilder.setTitle("Connect a Flash Drive");

		// Set dialog message
		alertDialogBuilder
				.setMessage(
						"Please connect a flash drive and try again.\n\nIf the flash drive is already conntected and you still can't sync, you may have to manually remount. Click \"Open Settings\" below, scroll to the bottom, and unmount and remount the USB storage device. Then press the back button.")
				.setCancelable(false).setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						synchronizeWithFlashDrive();
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).setNeutralButton("Open settings", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						startActivityForResult(new Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS), RESULT_CODE_MOUNT);
					}
				});
		// Create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// Show it
		alertDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_CODE_MOUNT) {
			promptForDataSource();
		} else if (requestCode == RESULT_CODE_UNMOUNT) {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

}
