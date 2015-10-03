package com.pacmac.betshare;

import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BetsDBHelper extends SQLiteOpenHelper {


	//database version and name
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Bets.db";

	private String createEntries;
	private String deleteEntries;
	
	
	public BetsDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(BetsEntry.SQL_CREATE_ENTRIES_USER);
		db.execSQL(BetsEntry.SQL_CREATE_ENTRIES_BETS);
		db.execSQL(BetsEntry.SQL_CREATE_ENTRIES_BETS_TEMP);

	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	       db.execSQL("DROP TABLE IF EXISTS " + BetsEntry.TABLE_USER);
	       db.execSQL("DROP TABLE IF EXISTS " + BetsEntry.TABLE_BETS);
	       db.execSQL("DROP TABLE IF EXISTS " + BetsEntry.TABLE_TEMP);
		onCreate(db);
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}
