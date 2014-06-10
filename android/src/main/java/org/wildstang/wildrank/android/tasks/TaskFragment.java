package org.wildstang.wildrank.android.tasks;

import android.app.Fragment;

public abstract class TaskFragment extends Fragment {

	public static interface TaskCallbacks {
		void onPreExecute(TaskType type);

		void onProgressUpdate(TaskType type, ProgressUpdateInfo info);

		void onCancelled(TaskType type);

		void onPostExecute(TaskType type);
	}

	public static class ProgressUpdateInfo {
		public int total = 0;
		public int current = 0;
		public String message = "";
		public ProgressUpdaterState state = null;

		public enum ProgressUpdaterState {
			IN_PROGRESS,
			COMPLETE
		}
	}

	// For every class that extends this class, an enum should be added here
	public enum TaskType {
		TASK_LOAD_TEAM_LIST,
		TASK_LOAD_EVENT_DETAILS,
		TASK_SYNCHRONIZE_WITH_FLASH_DRIVE,
		TASK_LOAD_TEAM_DATA
	}

	protected TaskCallbacks callbacks;

	/**
	 * Set the callback to null so we don't accidentally leak the Activity
	 * instance.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		callbacks = null;
	}
}
