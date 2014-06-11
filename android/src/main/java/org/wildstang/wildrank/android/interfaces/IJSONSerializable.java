package org.wildstang.wildrank.android.interfaces;

import android.os.Bundle;

public interface IJSONSerializable {

    public void writeContentsToBundle(Bundle b);

    public void restoreFromJSONObject(Object object);

    public String getKey();

    public boolean isComplete(boolean highlight);

}
