package org.wildstang.wildrank.android.utils;

/**
 * Created by Nathan on 8/29/2014.
 */
public class MatchUtils {

    public static int matchNumberFromMatchKey(String key) {
        return Integer.parseInt(key.substring(key.lastIndexOf('m') + 1));
    }

    public static String eventKeyFromKey(String key) {
        return key.substring(0, key.indexOf("_") - 1);
    }

    public static String matchKeyFromKey(String key) {
        return key.substring(key.indexOf("_") + 1);
    }
}
