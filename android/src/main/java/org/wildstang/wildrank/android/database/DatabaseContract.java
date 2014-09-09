package org.wildstang.wildrank.android.database;

public class DatabaseContract {

    public static abstract class BaseColumns {
        // Unique identifier for each record
        public static final String UUID = "uuid";
        // Hash of the record
        public static final String HASH = "hash";
        // UNIX time at which the record was last modified
        public static final String LAST_MODIFIED = "last_modified";
    }

    public static abstract class User extends BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
    }

    public static abstract class Match extends BaseColumns {
        // Name of the table storing Match records
        public static final String TABLE_NAME = "matches";
        // Key of this match (in The Blue Alliance format)
        public static final String MATCH_KEY = "match_key";
        // Number of this match (mostly useful for qualifying matches)
        public static final String MATCH_NUMBER = "number";
        // Key of Red team 1
        public static final String RED_1 = "red_1";
        // Key of Red team 2
        public static final String RED_2 = "red_2";
        // Key of Red team 3
        public static final String RED_3 = "red_3";
        // Key of Blue team 1
        public static final String BLUE_1 = "blue_1";
        // Key of Blue team 2
        public static final String BLUE_2 = "blue_2";
        // Key of Blue team 3
        public static final String BLUE_3 = "blue_3";
        public static final String[] ALL_COLUMNS = {UUID, HASH, LAST_MODIFIED, MATCH_KEY, MATCH_NUMBER, RED_1, RED_2, RED_3, BLUE_1, BLUE_2, BLUE_3};
    }

    public static abstract class MatchResult extends BaseColumns {
        // Name of the table storing Match Result records
        public static final String TABLE_NAME = "match_results";
        public static final String MATCH_KEY = "match_key";
        public static final String TEAM_KEY = "team_key";
        public static final String USER_ID = "user_id";
        public static final String DATA = "data";
        public static final String[] ALL_COLUMNS = {UUID, HASH, LAST_MODIFIED, MATCH_KEY, TEAM_KEY, USER_ID, DATA};
    }

    public static abstract class PitScouting extends BaseColumns {
        public static final String TABLE_NAME = "pit_scouting";
        public static final String TEAM_KEY = "team_key";
        public static final String USER_ID = "user_id";
        public static final String DATA = "data";
    }

    public static abstract class Note extends BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String MATCH_UUID = "match_uuid";
        public static final String MATCH_KEY = "match_key";
        public static final String PIT_SCOUTING_UUID = "pit_scouting_uuid";
        public static final String PIT_SCOUTING_KEY = "pit_scouting_key";
        public static final String USER_ID = "user_id";
        public static final String DATA = "data";
    }

    public static abstract class Team extends BaseColumns {
        public static final String TABLE_NAME = "teams";
        public static final String TEAM_KEY = "team_key";
        public static final String NAME = "name";
        public static final String PIT_GROUP = "pit_group";
    }

    public static abstract class PickList extends BaseColumns {
        public static final String TABLE_NAME = "pick_lists";
        public static final String NAME = "name";
        public static final String USER_ID = "user_id";
    }

    public static abstract class PickListTeam extends BaseColumns {
        public static final String TABLE_NAME = "pick_list_teams";
        public static final String LIST_UUID = "list_uuid";
        public static final String TEAM_KEY = "team_key";
        public static final String RANKING = "ranking";
        public static final String TIER = "tier";
        public static final String PICKED = "picked";
    }
}
