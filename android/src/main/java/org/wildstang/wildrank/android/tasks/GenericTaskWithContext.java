package org.wildstang.wildrank.android.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.wildstang.wildrank.android.tasks.TaskFragment.ProgressUpdateInfo;
import org.wildstang.wildrank.android.tasks.TaskFragment.TaskCallbacks;
import org.wildstang.wildrank.android.tasks.TaskFragment.TaskType;

public abstract class GenericTaskWithContext extends AsyncTask<Void, ProgressUpdateInfo, Void> {

	TaskType taskType;
	Context context;
	TaskCallbacks callbacks;

	public GenericTaskWithContext(TaskType taskType) {
		this.taskType = taskType;
	}

	public void registerProgressUpdater(TaskCallbacks callbacks) {
		this.callbacks = callbacks;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		callbacks.onPreExecute(taskType);
	}

	@Override
	protected void onProgressUpdate(ProgressUpdateInfo... values) {
		super.onProgressUpdate(values);
		callbacks.onProgressUpdate(taskType, values[0]);
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		callbacks.onPostExecute(taskType);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		callbacks.onCancelled(taskType);
	}

}
