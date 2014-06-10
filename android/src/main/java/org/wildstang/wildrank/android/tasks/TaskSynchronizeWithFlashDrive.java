package org.wildstang.wildrank.android.tasks;

import org.wildstang.wildrank.android.data.DataManager;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo;
import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo.ProgressUpdaterState;
import org.wildstang.wildrank.android.tasks.TaskFragment.TaskType;

import java.io.IOException;

/*
 * Background task to synchronize the internal storage with the flash drive.
 */
public class TaskSynchronizeWithFlashDrive extends GenericTaskWithContext {

	public TaskSynchronizeWithFlashDrive() {
		super(TaskType.TASK_SYNCHRONIZE_WITH_FLASH_DRIVE);
	}

	@Override
	protected Void doInBackground(Void... params) {
		ProgressUpdateInfo update = new ProgressUpdateInfo();
		update.state = ProgressUpdaterState.IN_PROGRESS;
		update.message = "Synchronizing internal storage with flash drive";
		publishProgress(update);
		try {
			DataManager.syncWithFlashDrive(context);
		} catch (IOException e) {
			e.printStackTrace();
		}

		update.state = ProgressUpdaterState.COMPLETE;
		publishProgress(update);

		return null;
	}

}