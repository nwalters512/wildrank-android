package org.wildstang.wildrank.android.database;

/**
 * Created by Nathan on 9/19/2014.
 */
public class ConflictResolutionResult {

    public enum ResolutionStrategy {
        ACCEPT_ORIGINAL,
        ACCEPT_INCOMING,
        ABORT_SYC
    }

    public ResolutionStrategy getStrategy() {
        return null;
    }
}
