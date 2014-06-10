package org.wildstang.wildrank.android.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;

public class DatabaseContentProvider extends ContentProvider {

	// public constants for client development
	private static final String AUTHORITY = "org.wildstang.wildrank.database.DatabaseContentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	// helper constants for use with the UriMatcher
	private static final int EVENT_LIST = 100;
	private static final int EVENT_KEY = 200;
	private static final int EVENT_ID = 300;
	private static final int MATCH_LIST = 400;
	private static final int MATCH_KEY = 500;
	private static final int MATCH_ID = 600;
	private static final int PIT_LIST = 700;
	private static final int TEAM_LIST = 800;
	private static final int TEAM_ID = 900;
	private static final UriMatcher URI_MATCHER;

	// prepare the UriMatcher
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, "event/#", EVENT_ID);
		URI_MATCHER.addURI(AUTHORITY, "match/#", MATCH_ID);
		URI_MATCHER.addURI(AUTHORITY, "event", EVENT_LIST);
		URI_MATCHER.addURI(AUTHORITY, "event/*", EVENT_KEY);
		URI_MATCHER.addURI(AUTHORITY, "event/*/match", MATCH_LIST);
		URI_MATCHER.addURI(AUTHORITY, "match/*", MATCH_KEY);
		URI_MATCHER.addURI(AUTHORITY, "pit", PIT_LIST);
		URI_MATCHER.addURI(AUTHORITY, "team", TEAM_LIST);
		URI_MATCHER.addURI(AUTHORITY, "team/#", TEAM_ID);
	}

	private DatabaseHelper db;

	@Override
	public boolean onCreate() {
		db = new DatabaseHelper(this.getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = URI_MATCHER.match(uri);
		SQLiteDatabase database = db.getWritableDatabase();
		int rowsAffected = 0;
		switch (uriType) {
		case EVENT_LIST:
			rowsAffected += database.delete(DatabaseContract.Event.TABLE_NAME, null, null);
			rowsAffected += database.delete(DatabaseContract.Match.TABLE_NAME, null, null);
			break;
		case EVENT_KEY:
			throw new IllegalArgumentException("Deleting individual items not supported yet.");
		case EVENT_ID:
			throw new IllegalArgumentException("Operations with event IDs are not supported yet.");
		case MATCH_LIST:
			rowsAffected += database.delete(DatabaseContract.Match.TABLE_NAME, DatabaseContract.Match.KEY + " like '" + uri.getPathSegments().get(1) + "%'", null);
			break;
		case MATCH_KEY:
			throw new IllegalArgumentException("Deleting with match keys not supported yet.");
		case MATCH_ID:
			throw new IllegalArgumentException("Operations with match IDs are not supported yet.");
		case PIT_LIST:
		case TEAM_LIST:
			rowsAffected += database.delete(DatabaseContract.Team.TABLE_NAME, null, null);
			break;
		case TEAM_ID:
			throw new IllegalArgumentException("Operations with team IDs are not supported yet.");
		}
		return rowsAffected;
	}

	@Override
	public String getType(Uri uri) {
		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case EVENT_LIST:
			return DatabaseContract.Event.CONTENT_TYPE;
		case EVENT_KEY:
			return DatabaseContract.Event.CONTENT_ITEM_TYPE;
		case EVENT_ID:
			return DatabaseContract.Event.CONTENT_ITEM_TYPE;
		case MATCH_LIST:
			return DatabaseContract.Match.CONTENT_TYPE;
		case MATCH_KEY:
			return DatabaseContract.Match.CONTENT_ITEM_TYPE;
		case MATCH_ID:
			return DatabaseContract.Match.CONTENT_ITEM_TYPE;
		case PIT_LIST:
		case TEAM_LIST:
			return DatabaseContract.Team.CONTENT_TYPE;
		case TEAM_ID:
			return DatabaseContract.Team.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri.toString());
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case EVENT_LIST:
			throw new IllegalArgumentException("insert() is not supported for URI type EVENT_LIST");
		case EVENT_KEY:
			db.getWritableDatabase().insert(DatabaseContract.Event.TABLE_NAME, null, values);
			break;
		case EVENT_ID:
			break;
		case MATCH_LIST:
			break;
		case MATCH_KEY:
			db.getWritableDatabase().insert(DatabaseContract.Match.TABLE_NAME, null, values);
			break;
		case MATCH_ID:
			break;
		case PIT_LIST:
		case TEAM_LIST:
			db.getWritableDatabase().insert(DatabaseContract.Team.TABLE_NAME, null, values);
			break;
		case TEAM_ID:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri.toString());
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return uri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case EVENT_LIST:
			builder.setTables(DatabaseContract.Event.TABLE_NAME);
			break;
		case EVENT_KEY:
			builder.setTables(DatabaseContract.Event.TABLE_NAME);
			builder.appendWhere(DatabaseContract.Event.KEY + " = " + uri.getLastPathSegment());
			break;
		case EVENT_ID:
			builder.setTables(DatabaseContract.Event.TABLE_NAME);
			builder.appendWhere(DatabaseContract.Event._ID + " = " + uri.getLastPathSegment());
		case MATCH_LIST:
			builder.setTables(DatabaseContract.Match.TABLE_NAME);
			builder.appendWhere(DatabaseContract.Match.KEY + " like '" + uri.getPathSegments().get(1) + "%'");
			break;
		case MATCH_KEY:
			builder.setTables(DatabaseContract.Match.TABLE_NAME);
			builder.appendWhere(DatabaseContract.Match.KEY + "='" + uri.getLastPathSegment() + "'");
			break;
		case MATCH_ID:
			throw new IllegalArgumentException("Operations with match IDs are not supported yet.");
		case PIT_LIST:
			builder.setTables(DatabaseContract.Team.TABLE_NAME);
			String pitGroup = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pitGroup", "0");
			builder.appendWhere(DatabaseContract.Team.PIT_GROUP + " = '" + pitGroup + "'");
			break;
		case TEAM_LIST:
			builder.setTables(DatabaseContract.Team.TABLE_NAME);
			break;
		case TEAM_ID:
			builder.setTables(DatabaseContract.Team.TABLE_NAME);
			builder.appendWhere(DatabaseContract.Team._ID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri.toString());
		}
		Cursor cursor = builder.query(db.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int rowsAffected = 0;
		int uriType = URI_MATCHER.match(uri);
		switch (uriType) {
		case EVENT_LIST:
			throw new IllegalArgumentException("insert() is not supported for URI type EVENT_LIST");
		case EVENT_KEY:
			rowsAffected += db.getWritableDatabase().update(DatabaseContract.Event.TABLE_NAME, values, DatabaseContract.Event.KEY + "=?", new String[] { uri.getLastPathSegment() });
			break;
		case EVENT_ID:
			break;
		case MATCH_LIST:
			break;
		case MATCH_KEY:
			rowsAffected += db.getWritableDatabase().update(DatabaseContract.Match.TABLE_NAME, values, DatabaseContract.Match.KEY + "=?", new String[] { uri.getLastPathSegment() });

			// Required to notify listeners of MATCH_LIST changes
			getContext().getContentResolver().notifyChange(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "event/"), null);
			break;
		case MATCH_ID:
			break;
		case PIT_LIST:
		case TEAM_LIST:
			break;
		case TEAM_ID:
			rowsAffected += db.getWritableDatabase().update(DatabaseContract.Team.TABLE_NAME, values, DatabaseContract.Team._ID + "=?", new String[] { uri.getLastPathSegment() });

			// Required to notify listeners of TEAM_LIST changes
			getContext().getContentResolver().notifyChange(Uri.withAppendedPath(DatabaseContentProvider.CONTENT_URI, "team/"), null);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri.toString());
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

}
