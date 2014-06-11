package org.wildstang.wildrank.android.tasks;


import android.app.Activity;

public class TaskFragmentSynchronizeWithFlashDrive extends TaskFragment {
    private TaskSynchronizeWithFlashDrive task;

    /**
     * Hold a reference to the parent Activity so we can report the task's
     * current progress and results. The Android framework will pass us a
     * reference to the newly created Activity after each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (TaskCallbacks) activity;
        if (task != null) {
            task.registerProgressUpdater(callbacks);
        } else {
            task = new TaskSynchronizeWithFlashDrive();
            task.setContext(getActivity().getApplicationContext());
            task.registerProgressUpdater(callbacks);
            task.execute();
        }
    }
}
