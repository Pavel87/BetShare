package com.pacmac.betshare;

import android.provider.BaseColumns;

public class BetsDatabaseContract {

	public BetsDatabaseContract() {
		// empty
	}

	public static abstract class BetsEntry implements BaseColumns {
		
		//table name
		public static final String TABLE_BETS = "myBets";
		public static final String TABLE_USER = "userBets";
		public static final String TABLE_TEMP = "tempBets";
		//columns
		public static final String COLUMN_NAME_USER_ID = "userid";
		public static final String COLUMN_NAME_EVENT = "event";
		public static final String COLUMN_NAME_EVENTID = "eventid";
		public static final String COLUMN_NAME_HOME = "home";
		public static final String COLUMN_NAME_AWAY = "away";
		public static final String COLUMN_NAME_DATEMS = "datems";
		public static final String COLUMN_NAME_DATEEND = "dateend";
		public static final String COLUMN_NAME_ODDS = "odds";
		public static final String COLUMN_NAME_CHOICE = "choice";
		public static final String COLUMN_NAME_TRUST = "trust";
		public static final String COLUMN_NAME_AUTHOR = "author";
		public static final String COLUMN_NAME_COMMENT = "comment";
		public static final String COLUMN_NAME_NEWINDICATOR = "newind";
		
		//user columns
		public static final String COLUMN_NAME_USERNAME = "username";
		public static final String COLUMN_NAME_EMAIL = "email";
		public static final String COLUMN_NAME_SALTHASH = "salt";
		public static final String COLUMN_NAME_GROUPID = "groupid";
		public static final String COLUMN_NAME_FB = "fblogin";
		public static final String COLUMN_NAME_BETCOUNTER = "betcounter";
		
		
		//DB definitions
		private static final String TEXT_TYPE = " TEXT";
	    private static final String INTEGER_TYPE = " INTEGER";
	    private static final String BOOLEAN_TYPE = " BOOLEAN";
		private static final String COMMA_SEP = ",";
		
		public static final String SQL_CREATE_ENTRIES_BETS =
		    "CREATE TABLE " + TABLE_BETS + " (" +
		    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    COLUMN_NAME_EVENTID + TEXT_TYPE + COMMA_SEP +		
		    COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_GROUPID + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_EVENT + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_HOME + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_AWAY + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_DATEMS + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_DATEEND + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_AUTHOR + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_COMMENT + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_CHOICE + INTEGER_TYPE + COMMA_SEP +
		    COLUMN_NAME_TRUST + INTEGER_TYPE + COMMA_SEP +
		    COLUMN_NAME_NEWINDICATOR + INTEGER_TYPE + COMMA_SEP +
		    COLUMN_NAME_ODDS + TEXT_TYPE +
		    " );";
		
		public static final String SQL_CREATE_ENTRIES_BETS_TEMP =
			    "CREATE TABLE " + TABLE_TEMP + " (" +
			    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			    COLUMN_NAME_EVENTID + TEXT_TYPE + COMMA_SEP +		
			    COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_GROUPID + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_EVENT + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_HOME + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_AWAY + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_DATEMS + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_DATEEND + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_AUTHOR + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_COMMENT + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_CHOICE + INTEGER_TYPE + COMMA_SEP +
			    COLUMN_NAME_TRUST + INTEGER_TYPE + COMMA_SEP +
			    COLUMN_NAME_ODDS + TEXT_TYPE +
			    " );";
		

		public static final String SQL_DELETE_ENTRIES_BETS =
		    "DROP TABLE IF EXISTS " + TABLE_BETS;
				
		
		public static final String SQL_CREATE_ENTRIES_USER =
			    "CREATE TABLE " + TABLE_USER + " (" +
			    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			    COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_SALTHASH + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_GROUPID + TEXT_TYPE + COMMA_SEP +
			    COLUMN_NAME_BETCOUNTER + INTEGER_TYPE +COMMA_SEP+
			    COLUMN_NAME_FB + INTEGER_TYPE + 
			    " );";

	}
}
