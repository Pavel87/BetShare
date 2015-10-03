package com.pacmac.betshare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

public class SqlQueries {

	private BetsDBHelper betsHelper;
	private SQLiteDatabase db;

	// default constructor
	public SqlQueries(Context context) {

		betsHelper = new BetsDBHelper(context);
		db = betsHelper.getReadableDatabase();
	}

	// read test
	// projections
	private String[] projection = { BetsEntry._ID,
			BetsEntry.COLUMN_NAME_GROUPID, BetsEntry.COLUMN_NAME_DATEMS,
			BetsEntry.COLUMN_NAME_HOME, BetsEntry.COLUMN_NAME_AWAY,
			BetsEntry.COLUMN_NAME_NEWINDICATOR, BetsEntry.COLUMN_NAME_EVENTID,
			BetsEntry.COLUMN_NAME_CHOICE };

	private String[] childProjection = { BetsEntry._ID,
			BetsEntry.COLUMN_NAME_CHOICE, BetsEntry.COLUMN_NAME_TRUST,
			BetsEntry.COLUMN_NAME_COMMENT, BetsEntry.COLUMN_NAME_AUTHOR,
			BetsEntry.COLUMN_NAME_EVENT, BetsEntry.COLUMN_NAME_ODDS };

	private String[] projectionUser = { BetsEntry._ID,
			BetsEntry.COLUMN_NAME_EMAIL, BetsEntry.COLUMN_NAME_USERNAME,
			BetsEntry.COLUMN_NAME_GROUPID, BetsEntry.COLUMN_NAME_BETCOUNTER };

	private String[] projectionUserLogin = { BetsEntry._ID,
			BetsEntry.COLUMN_NAME_EMAIL, BetsEntry.COLUMN_NAME_SALTHASH,
			BetsEntry.COLUMN_NAME_USERNAME, BetsEntry.COLUMN_NAME_GROUPID };

	private String[] projectionEventIds = { BetsEntry._ID,
			BetsEntry.COLUMN_NAME_EVENTID };

	private String[] projectionComplete = { BetsEntry._ID,
			BetsEntry.COLUMN_NAME_EVENTID, BetsEntry.COLUMN_NAME_USER_ID,
			BetsEntry.COLUMN_NAME_GROUPID, BetsEntry.COLUMN_NAME_EVENT,
			BetsEntry.COLUMN_NAME_NEWINDICATOR, BetsEntry.COLUMN_NAME_HOME,
			BetsEntry.COLUMN_NAME_AWAY, BetsEntry.COLUMN_NAME_DATEMS,
			BetsEntry.COLUMN_NAME_DATEEND, BetsEntry.COLUMN_NAME_AUTHOR,
			BetsEntry.COLUMN_NAME_COMMENT, BetsEntry.COLUMN_NAME_CHOICE,
			BetsEntry.COLUMN_NAME_TRUST, BetsEntry.COLUMN_NAME_ODDS };

	// END
	private String sortOrder = BetsEntry.COLUMN_NAME_DATEMS + " ASC";

	private String whereClausuleAdd = " = ?";
	private String[] whereArgs = new String[] { "placeholder" };

	// / read user DB **********************************************************
	public Cursor readUserDb(String whereArg, String whereClausule) {
		whereArgs[0] = whereArg;
		Cursor c;
		try {
			c = db.query(BetsEntry.TABLE_USER, projectionUser, whereClausule
					+ whereClausuleAdd, whereArgs, null, null, null);
			c.moveToFirst();
		} catch (SQLiteException ex) {
			Log.d("TAG", ex.getMessage());
			return null;
		}
		return c;
	}

	// / read user DB - LOGIN
	public Cursor readUserLoginDb(String whereArg, String whereClausule) {
		whereArgs[0] = whereArg;
		Cursor c;
		try {
			c = db.query(BetsEntry.TABLE_USER, projectionUserLogin,
					whereClausule + whereClausuleAdd, whereArgs, null, null,
					null);
			c.moveToFirst();
		} catch (SQLiteException ex) {
			Log.d("TAG", ex.getMessage());
			return null;
		}
		return c;
	}

	// write userDB - create new entry in local
	public void writeUserDb(ContentValues values) {

		long newRow = db.insert(BetsEntry.TABLE_USER,
				BetsEntry.COLUMN_NAME_USERNAME, values);
		if (newRow == -1) {
			Log.d("TAG", "New Row wasn't inserted -writeLineQuery : " + newRow);
		}

	}

	// TODO DELETE BELOW METHOD
	public void updateUserGroupId(ContentValues values, String whereParam) {
		whereArgs[0] = whereParam;
		db.update(BetsEntry.TABLE_USER, values, BetsEntry.COLUMN_NAME_EMAIL
				+ whereClausuleAdd, whereArgs);
	}

	public void updateUserDetail(ContentValues values, String whereParam) {
		whereArgs[0] = whereParam;
		db.update(BetsEntry.TABLE_USER, values, BetsEntry.COLUMN_NAME_EMAIL
				+ whereClausuleAdd, whereArgs);
	}

	// END USER queries ****************************************************

	// CRUD ON BETS TABLE

	// readiding user tips only (my bets)
	public Cursor readQueryMy(String where) {
		whereArgs[0] = where;
		Cursor c = db.query(BetsEntry.TABLE_BETS, projection,
				BetsEntry.COLUMN_NAME_USER_ID + whereClausuleAdd, whereArgs,
				null, null, sortOrder);
		c.moveToFirst();
		return c;
	}

	public Cursor readAllEventIds(String where) {
		whereArgs[0] = where;
		Cursor c = db.query(BetsEntry.TABLE_BETS, projectionEventIds,
				BetsEntry.COLUMN_NAME_EVENTID + whereClausuleAdd, whereArgs,
				null, null, null);

		c.moveToFirst();

		return c;
	}

	public Cursor readQueryAll(String where) {
		whereArgs[0] = where;
		Cursor c = db.query(BetsEntry.TABLE_BETS, projection,
				BetsEntry.COLUMN_NAME_GROUPID + whereClausuleAdd, whereArgs,
				null, null, sortOrder);
		c.moveToFirst();
		return c;
	}

	public Cursor readQuery(int where, String whereCondition) {
		whereArgs[0] = "" + where;
		Cursor c = db.query(BetsEntry.TABLE_BETS, childProjection,
				whereCondition, whereArgs, null, null, sortOrder);
		c.moveToFirst();
		return c;
	}

	// update
	public void updateBetsGroupId(ContentValues values, String whereParam) {
		whereArgs[0] = whereParam;
		db.update(BetsEntry.TABLE_BETS, values, BetsEntry.COLUMN_NAME_USER_ID
				+ whereClausuleAdd, whereArgs);
	}

	public void updateBets(ContentValues values, String whereParam) {
		whereArgs[0] = whereParam;
		db.update(BetsEntry.TABLE_BETS, values, BetsEntry.COLUMN_NAME_EVENTID
				+ whereClausuleAdd, whereArgs);
	}

	// testQuery - write
	public void writeLineQuery(ContentValues values) {

		long newRow = db.insert(BetsEntry.TABLE_BETS,
				BetsEntry.COLUMN_NAME_HOME, values);
		if (newRow == -1) {
			Log.d("TAG", "New Row wasn't inserted -writeLineQuery : " + newRow);
		}
	}

	public void writeLineToTemp(ContentValues values) {

		long newRow = db.insert(BetsEntry.TABLE_TEMP,
				BetsEntry.COLUMN_NAME_HOME, values);
		if (newRow == -1) {
		//	Log.d("TAG", "New Row wasn't inserted -writeLineQuery : " + newRow);
		}
	}

	public void deleteRowInBets(String where) {
		whereArgs[0] = where;
		if (db.delete(BetsEntry.TABLE_BETS, BetsEntry.COLUMN_NAME_EVENTID
				+ whereClausuleAdd, whereArgs) == 1) {
			// Log.d("TAG", "row deleted successfully from temp db");
		} else {
			//Log.d("TAG", "row was not deleted from bets db");
		}
	}

	public void deleteRowInTmp(String where) {
		whereArgs[0] = where;
		if (db.delete(BetsEntry.TABLE_TEMP, BetsEntry.COLUMN_NAME_EVENTID
				+ whereClausuleAdd, whereArgs) == 1) {
			// Log.d("TAG", "row deleted successfully from temp db");
		} else {
			Log.d("TAG", "row was not deleted from temp db");
		}
	}

	public void clearBetsTable(String email) {
		whereArgs[0] = email;
		db.delete(BetsEntry.TABLE_BETS, BetsEntry.COLUMN_NAME_USER_ID
				+ whereClausuleAdd, whereArgs);
	}

	// read temp // TODO need to clear temp db on start

	public Cursor readTemp(String email) {
		whereArgs[0] = email;
		Cursor c;
		try {
			c = db.query(BetsEntry.TABLE_TEMP, projectionComplete,
					BetsEntry.COLUMN_NAME_USER_ID + whereClausuleAdd,
					whereArgs, null, null, null);
			c.moveToFirst();
		} catch (SQLiteException ex) {
			Log.d("TAG", ex.getMessage());
			return null;
		}
		return c;
	}

}
