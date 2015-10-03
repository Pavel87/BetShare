package com.pacmac.betshare;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
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

public class RegLogFaceBookTask extends AsyncTask<String, Void, Integer> {

	private Context mContext;
	public InterfaceASTask asyncResp = null;
	private String name, email, groupid, salt;
	private Firebase userFirebaseRef;
	private Query queryRef;
	private int result = 0;
	private boolean isInFirebase = false;
	private boolean isConnected = false;

	public RegLogFaceBookTask(Context context, boolean connection) {
		this.mContext = context;
		this.isConnected = connection;
	}

	@Override
	protected void onPreExecute() {
		asyncResp.registerDialog(true, "Validating User");
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {
		email = params[0];
		name = params[1];
		Firebase.setAndroidContext(mContext);
		userFirebaseRef = new Firebase(
				"https://betadvisor2015.firebaseio.com/users/");
		SqlQueries query = new SqlQueries(mContext);
		Cursor c = query.readUserLoginDb(email, BetsEntry.COLUMN_NAME_EMAIL);
		Boolean userExists = false;

		if (c == null) {
			userExists = false;
		} else if (c.getCount() > 0) {

			userExists = true;
			name = c.getString(c
					.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_USERNAME));
			groupid = c.getString(c
					.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_GROUPID));
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

							isInFirebase = true;
							// Log.d("TAG", "user exists in FireBase");

						} else {
							isInFirebase = false;
						}
					}
					if (!isInFirebase) {
						// Log.d("TAG", "user doesn't exist in remote storage");
						// if user doesn't exist register him
						// TODO update view with message bellow

						Log.d("TAG",
								"adding new user to remote and local storage");
						// if user doesn't exist we need to register
						// him 1st to remote then to local
						addNewUserToDb();
						// Upload complete - now clean up
						
						//login
						result = 1;

					} else {
						//Log.d("TAG", "local DB doesn't have this record");
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
						//Log.d("TAG", "DB updated");

						name = (String) value.get("username");
						groupid = (String) value.get("groupid");
						result = 1;
					}
				}

				@Override
				public void onCancelled(FirebaseError arg0) {
					Log.e("TAG", "ERROR during getting data from Firebase");
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
	protected void onPostExecute(Integer res) {
		if (res == 1) {
			// proceed with logging in
			asyncResp.updateMainView(email, name, groupid);
		} else if (res == 3) {
			asyncResp.showDialogNoConn();
		}
		super.onPostExecute(result);
	}

	public void addNewUserToDb() {
		groupid = getMD5(email);
		salt = "fblogin_default_pass";
		// update Firebase with new user (remote storage)
		Map<String, Object> userData = new HashMap<String, Object>();
		userData.put(BetsEntry.COLUMN_NAME_USERNAME, name);
		userData.put("email", email);
		userData.put("salt", salt);
		userData.put("groupid", groupid);
		userData.put(BetsEntry.COLUMN_NAME_BETCOUNTER, 0);
		userFirebaseRef.push().setValue(userData,
				new Firebase.CompletionListener() {

					@Override
					public void onComplete(FirebaseError arg0, Firebase arg1) {
						// once upload is complete we will add user to local DB
						if (arg0 != null) {
							//Log.d("TAG", "backend update failure");
						} else {

							ContentValues values = new ContentValues();
							SqlQueries queryLogin = new SqlQueries(mContext);
							values.put(BetsEntry.COLUMN_NAME_EMAIL, email);
							values.put(BetsEntry.COLUMN_NAME_USERNAME, name);
							values.put(BetsEntry.COLUMN_NAME_SALTHASH, salt);
							values.put(BetsEntry.COLUMN_NAME_GROUPID, groupid);
							values.put(BetsEntry.COLUMN_NAME_BETCOUNTER, 0);
							queryLogin.writeUserDb(values);
							//Log.d("TAG", "update to local complete");
						}

					}
				});

	}

	
	// get random default group ID (MD5 from email)
	public static String getMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			// Now we need to zero pad it if you actually want the full 32
			// chars.
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
}
