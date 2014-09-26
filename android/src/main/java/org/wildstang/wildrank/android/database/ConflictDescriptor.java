package org.wildstang.wildrank.android.database;

import android.database.Cursor;

/**
 * Created by Nathan on 9/25/2014.
 */
public class ConflictDescriptor {

    public Cursor originalRecord;
    public String originalRequestedOperation;
    public Cursor incomingRecord;
    public String incomingRequestedOperation;

    public ConflictDescriptor(Cursor or, String oro, Cursor ir, String iro) {
        originalRecord = or;
        originalRequestedOperation = oro;
        incomingRecord = ir;
        incomingRequestedOperation = iro;
    }
}
