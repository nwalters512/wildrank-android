package org.wildstang.wildrank.android.database;

import android.content.ContentResolver;
import android.provider.BaseColumns;

public class DatabaseContract {

    public static abstract class Event implements BaseColumns {
        public static final String TABLE_NAME = "events";
        public static final String KEY = "key";
        public static final String NAME = "name";
        public static final String SHORT_NAME = "short_name";
        public static final String LOCATION = "location";
        public static final String START_DATE = "start_date";
        public static final String END_DATE = "end_date";
        public static final String[] ALL_COLUMNS = {_ID, KEY, NAME, SHORT_NAME, LOCATION, START_DATE, END_DATE};
        public static final String CONTENT_PATH = "events";
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/wildstang.event";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/wildstang.event";
    }

    public static abstract class Match implements BaseColumns {
        public static final String TABLE_NAME = "matches";
        public static final String KEY = "key";
        public static final String NUMBER = "number";
        public static final String RED_1 = "red_1";
        public static final String RED_2 = "red_2";
        public static final String RED_3 = "red_3";
        public static final String BLUE_1 = "blue_1";
        public static final String BLUE_2 = "blue_2";
        public static final String BLUE_3 = "blue_3";
        public static final String[] ALL_COLUMNS = {_ID, KEY, NUMBER, RED_1, RED_2, RED_3, BLUE_1, BLUE_2, BLUE_3};
        public static final String CONTENT_PATH = "matches";
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/wildstang.match";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/wildstang.match";
    }

    public static abstract class Team implements BaseColumns {
        public static final String TABLE_NAME = "teams";
        public static final String NAME = "name";
        public static final String NUMBER = "number";
        public static final String PICK_LIST_RANKING = "pick_list_ranking";
        public static final String PICK_LIST_TIER = "pick_list_tier";
        public static final String PICK_LIST_PICKED = "pick_list_picked";
        public static final String PIT_GROUP = "pit_group";
        public static final String[] ALL_COLUMNS = {_ID, NAME, NUMBER, PIT_GROUP, PICK_LIST_RANKING, PICK_LIST_TIER, PICK_LIST_PICKED};
        public static final String CONTENT_PATH = "teams";
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/wildstang.team";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/wildstang.team";
    }
}
