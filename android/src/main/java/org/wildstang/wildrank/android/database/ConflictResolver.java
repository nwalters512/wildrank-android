package org.wildstang.wildrank.android.database;

import android.database.Cursor;

/**
 * Created by Nathan on 9/19/2014.
 */
public class ConflictResolver {

    private ConflictResolutionResult.ResolutionStrategy result;
    private boolean resultAvailable = false;

    public void requestConflictResolution(Cursor originalRecord, String requestedOriginalOperation, Cursor externalRecord, String changeSinceLastSync) {
        // Implement this
    }

    public synchronized void putConflictResolutionResult(ConflictResolutionResult.ResolutionStrategy result) {
        while (resultAvailable) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        resultAvailable = true;
        this.result = result;
        notifyAll();
    }

    public synchronized ConflictResolutionResult.ResolutionStrategy getConflictResolutionResult() {
        while (!resultAvailable) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        resultAvailable = false;
        notifyAll();
        return result;
    }
}
