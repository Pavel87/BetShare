package com.pacmac.betshare;

import java.util.Map;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

public class LoadUserDataTask extends AsyncTask<String, Integer, Integer> {

	private Context mContext;
	private Password securePass;
	private String password, email, groupid, name;
	private Firebase userFirebaseRef;
	private Query queryRef;

	public Boolean isInFirebase = false;
	public Boolean isConnected = false;
	private int result = 0;
	public InterfaceASTask asyncResp = null;

	public LoadUserDataTask(Context context, Boolean connection) {
		this.mContext = context;
		this.isConnected = connection;
	}

	@Override
	protected void onPreExecute() {
		// launch progress dialog
		asyncResp.showDialog(true);

		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {

		this.email = params[0];
		this.password = params[1];
		Firebase.setAndroidContext(mContext);
		userFirebaseRef = new Firebase(
				"https://betadvisor2015.firebaseio.com/users/");
		SqlQueries query = new SqlQueries(mContext);
		Cursor c = query.readUserLoginDb(email, BetsEntry.COLUMN_NAME_EMAIL);
		Boolean userExists = false;

		if (c == null) {
		} else if (c.getCount() > 0) {

			do {
				String dbPassword = c.getString(c
						.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_SALTHASH));
				try {
					if (securePass.check(password, dbPassword)) {
						userExists = true;
						name = c.getString(c
								.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_USERNAME));
						groupid = c
								.getString(c
										.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_GROUPID));
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} while (c.moveToNext());

			c.close();
		}

		// if email and password match user exists and we can log him in
		if (userExists) {
			// Log.d("TAG", "local DB has this record");
			// user in local db -- addding to sharedpref and launching itent (1)
			// = log user in
			result = 1;

		} else if (isConnected) {
			// check live storage - FIREBASE

			queryRef = userFirebaseRef.orderByChild("email").startAt(email)
					.limitToFirst(1);

			queryRef.addListenerForSingleValueEvent(new ValueEventListener() {

				@Override
				public void onDataChange(DataSnapshot snapshot) {
					// receiving data from firebase
					// Log.d("TAG", "snapshot created");

					if (!snapshot.getChildren().iterator().hasNext()) {
						// no record in remote DB - firebase
						isInFirebase = false;

					} else {
						Map<String, Object> value = (Map<String, Object>) snapshot
								.getChildren().iterator().next().getValue();
						if (value.get("email") != null
								&& email.equals(value.get("email"))) {
							try {
								if (securePass.check(password,
										(String) value.get("salt"))) {
									isInFirebase = true;
									// Log.d("TAG", "user exists in FireBase");
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							isInFirebase = false;
						}
					}
					if (!isInFirebase) {
						// Log.d("TAG", "user doesn't exist in remote storage");
						// if user doesn't exist show message
						result = 2;

					} else {
						Log.d("TAG", "local DB doesn't have this record");
						// user exists in firebase but it doesn't exist in local
						// DB
						Map<String, Object> value = (Map<String, Object>) snapshot
								.getChildren().iterator().next().getValue();

						// writing to local sqlite db
						ContentValues values = new ContentValues();
						SqlQueries queryLogin = new SqlQueries(mContext);
						values.put(BetsEntry.COLUMN_NAME_EMAIL, email);
						values.put(BetsEntry.COLUMN_NAME_USERNAME,
								(String) value.get("username"));
						values.put(BetsEntry.COLUMN_NAME_SALTHASH,
								(String) value.get("salt"));
						values.put(BetsEntry.COLUMN_NAME_GROUPID,
								(String) value.get("groupid"));
						values.put(BetsEntry.COLUMN_NAME_BETCOUNTER, Integer
								.parseInt(value.get(
										BetsEntry.COLUMN_NAME_BETCOUNTER)
										.toString()));
						queryLogin.writeUserDb(values);
						Log.d("TAG", "DB updated");

						name = (String) value.get("username");
						groupid = (String) value.get("groupid");
						result = 1;
					}
				}

				@Override
				public void onCancelled(FirebaseError arg0) {
					Log.d("TAG", "ERROR during getting data from Firebase");
				}
			});

		} else {
			result = 3;
		}
		while (result == 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Integer res) {

		if (res == 1) {
			// proceed with logging in
			asyncResp.updateMainView(email, name, groupid);
		} else if (res == 2) {
			asyncResp.showDialog(false);
		} else if (res == 3) {
			asyncResp.showDialogNoConn();
		}

		super.onPostExecute(result);
	}

}
