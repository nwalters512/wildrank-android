package org.wildstang.wildrank.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	// Database info
	public static final String DATABASE_NAME = "wildrank.db";
	public static final int DATABASE_VERSION = 15;

	public static final String CREATE_TABLE_EVENTS = "create table " + DatabaseContract.Event.TABLE_NAME + "(" + DatabaseContract.Event._ID + " integer primary key autoincrement, "
			+ DatabaseContract.Event.KEY + " text not null, " + DatabaseContract.Event.NAME + " text not null, " + DatabaseContract.Event.SHORT_NAME + " text not null, "
			+ DatabaseContract.Event.LOCATION + " text not null, " + DatabaseContract.Event.START_DATE + " text not null, " + DatabaseContract.Event.END_DATE + " text not null);";

	public static final String CREATE_TABLE_MATCHES = "create table " + DatabaseContract.Match.TABLE_NAME + "(" + DatabaseContract.Match._ID + " integer primary key autoincrement, "
			+ DatabaseContract.Match.KEY + " text not null, " + DatabaseContract.Match.NUMBER + " integer not null, " + DatabaseContract.Match.RED_1 + " integer not null, "
			+ DatabaseContract.Match.RED_2 + " integer not null, " + DatabaseContract.Match.RED_3 + " integer not null, " + DatabaseContract.Match.BLUE_1 + " integer not null, "
			+ DatabaseContract.Match.BLUE_2 + " integer not null, " + DatabaseContract.Match.BLUE_3 + " integer not null);";

	public static final String CREATE_TABLE_TEAMS = "create table " + DatabaseContract.Team.TABLE_NAME + "(" + DatabaseContract.Team._ID + " integer primary key autoincrement, "
			+ DatabaseContract.Team.NAME + " text not null, " + DatabaseContract.Team.NUMBER + " integer not null, " + DatabaseContract.Team.PIT_GROUP + " integer not null, "
			+ DatabaseContract.Team.PICK_LIST_RANKING + " integer, " + DatabaseContract.Team.PICK_LIST_TIER + " integer, " + DatabaseContract.Team.PICK_LIST_PICKED + " integer);";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_EVENTS);
		database.execSQL(CREATE_TABLE_MATCHES);
		database.execSQL(CREATE_TABLE_TEAMS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		if(oldVersion == 13 && newVersion == 14) {
			database.execSQL("alter table " + DatabaseContract.Team.TABLE_NAME + " add " + DatabaseContract.Team.PICK_LIST_TIER + " integer");
		}
		if(oldVersion < 15 && newVersion == 15) {
			database.execSQL("alter table " + DatabaseContract.Team.TABLE_NAME + " add " + DatabaseContract.Team.PICK_LIST_PICKED + " integer default 0");
		}
	}
}
