package org.wildstang.wildrank.android.tasks;


import android.app.Activity;

public class TaskFragmentSetUpFlashDrive extends TaskFragment {

    private TaskSetUpFlashDrive task;

    /**
     * Hold a reference to the parent Activity so we can report the task's
     * current progress and results. The Android framework will pass us a
     * reference to the newly created Activity after each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (TaskCallbacks) activity;
        // If our task does not exist, create it
        if (task != null) {
            task.registerProgressUpdater(callbacks);
        } else {
            task = new TaskSetUpFlashDrive();
            task.setContext(getActivity().getApplicationContext());
            task.registerProgressUpdater(callbacks);
            task.execute();
        }
    }

}
