package com.pacmac.betshare;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.pacmac.betshare.R;
import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class RegisterUserTask extends AsyncTask<String, Integer, Integer> {

	private Context mContext;
	private Password securePass;
	private String password, email, groupid, name, salt;
	private Firebase userFirebaseRef;
	private Query queryRef;
	public Boolean isInFirebase = false;
	private int res = 0;
	
	public InterfaceASTask asyncResp = null;
	
	public RegisterUserTask(Context context) {
	
		this.mContext = context;
	
	}

	@Override
	protected void onPreExecute() {
		asyncResp.registerDialog(true, mContext.getResources().getString(R.string.registring_user));
		super.onPreExecute();
	}
	
	@Override
	protected Integer doInBackground(String... params) {
		
		this.email = params[0];
		this.name = params[1];
		this.password = params[2];
		this.groupid = params[3];
		
		
		Firebase.setAndroidContext(mContext);
		userFirebaseRef = new Firebase(
				"https://betadvisor2015.firebaseio.com/users/");
		
		// check if online record exist and then proceed
		queryRef = userFirebaseRef.orderByChild("email")
				.startAt(email).limitToFirst(1);
		queryRef.addListenerForSingleValueEvent(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Iterator<DataSnapshot> iteratorDs = snapshot
						.getChildren().iterator();
				//Log.d("TAG", "firebase snapshot created");
				if (!iteratorDs.hasNext()) {
					// no record in remote DB - firebase
					isInFirebase = false;

				} else {
					Map<String, Object> value = (Map<String, Object>) iteratorDs
							.next().getValue();
					if (value.get("email") != null
							&& email.equals(value.get("email"))) {
						isInFirebase = true;
						//Log.d("TAG", "user exists in FireBase");
						res=2;
						
					} else {
						isInFirebase = false;
					}
				}
				if (!isInFirebase) {
					//Log.d("TAG","adding new user to remote and local storage");
					// if user doesn't exist we need to register
					// him 1st to remote then to local
					addNewUserToDb();
				}
			}

			@Override
			public void onCancelled(FirebaseError arg0) {
				// TODO Auto-generated method stub

			}
		});
		
		
		while (res == 0){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if (result == 1) {
			asyncResp.finishRegistration();
		}
		else if(result ==2) {
			asyncResp.registerDialog(false, "Cancelation");
		}
		super.onPostExecute(result);
	}
	
	
	public void addNewUserToDb() {

		try {
			salt = securePass.getSaltedHash(password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// update Firebase with new user (remote storage)
		Map<String, Object> userData = new HashMap<String, Object>();
		userData.put(BetsEntry.COLUMN_NAME_USERNAME, name);
		userData.put("email", email);
		userData.put("salt", salt);
		userData.put("groupid", groupid);
		userData.put(BetsEntry.COLUMN_NAME_BETCOUNTER,0);
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
							// Upload complete - now clean up
							res = 1;
						}

					}
				});

	}
	
}
