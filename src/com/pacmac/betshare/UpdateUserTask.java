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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UpdateUserTask extends AsyncTask<String, Void, Integer> {

	private Context mContext;
	private String salt, email, groupid, name;
	private Query queryRef;
	private Firebase myFirebaseRef;
	private Firebase myFirebaseRefBets;
	private int res = 0;

	public Boolean isConnected = false;
	public InterfaceASTask asyncResp = null;

	public UpdateUserTask(Context context) {
		this.mContext = context;
	}

	@Override
	protected void onPreExecute() {
		asyncResp.showDialog(true);
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {

		this.email = params[0];
		this.name = params[1];
		this.salt = params[2];
		this.groupid = params[3];

		Firebase.setAndroidContext(mContext);
		myFirebaseRef = new Firebase(
				"https://betadvisor2015.firebaseio.com/users/");
		myFirebaseRefBets = new Firebase(
				"https://betadvisor2015.firebaseio.com/bets/");

		// update firebase

		queryRef = myFirebaseRef.orderByChild("email").equalTo(email)
				.limitToFirst(1);
		Log.d("TAG", "updating user in FB");
		queryRef.addListenerForSingleValueEvent(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Log.d("TAG", "ondatachanged groupID");
				Iterator<DataSnapshot> ds = snapshot.getChildren().iterator();
				String s = ds.next().getKey().toString();
				Firebase hopperRef = myFirebaseRef.child(s);
				Map<String, Object> values = new HashMap<String, Object>();
				if (name != null)
					values.put("username", name);
				if (salt != null)
					values.put("salt", salt);
				if (groupid != null)
					values.put("groupid", groupid);
				hopperRef.updateChildren(values);

				// Updating Bets with new user info in firebase
				if (name != null || groupid != null) {
					queryRef = myFirebaseRefBets.orderByChild("userid")
							.equalTo(email);
					Log.d("TAG", "going to update bets with new details");
					queryRef.addListenerForSingleValueEvent(new ValueEventListener() {

						@Override
						public void onDataChange(DataSnapshot snapshot) {
							Log.d("TAG", "updating groupID");
							Iterator<DataSnapshot> ds = snapshot.getChildren()
									.iterator();
							Log.d("TAG", "" + ds.toString());
							while (ds.hasNext()) {
								String s = ds.next().getKey().toString();
								Firebase hopperRef = myFirebaseRefBets.child(s);
								Map<String, Object> values = new HashMap<String, Object>();
								if (name != null)
									values.put("author", name);
								if (groupid != null)
									values.put("groupid", groupid);
								hopperRef.updateChildren(values);
								Log.d("TAG", s);
							}
							Toast.makeText(mContext, mContext.getResources().getString(R.string.user_updated),
									Toast.LENGTH_SHORT).show();

							updateLocalDB();

							res = 1;
						}

						@Override
						public void onCancelled(FirebaseError arg0) {
							Log.d("TAG",
									"error during firebase query when updating user Info to remote bets storage");
							res = 2;
						}
					});
				} else {
					updateLocalDB();
					res = 1;
				}

			}

			@Override
			public void onCancelled(FirebaseError arg0) {
				Log.d("TAG",
						"error during firebase query when updating user info to remote storage");
				res = 2;
			}
		});

		while (res == 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	@Override
	protected void onPostExecute(Integer result) {

		if (res == 1) {
			asyncResp.finishRegistration();
		} else if (res == 2) {
			Log.d("TAG", "error in update");
		}
		super.onPostExecute(result);
	}

	private void updateLocalDB() {
		// updating local
		Log.d("TAG", "connected and updating local");
		SqlQueries query = new SqlQueries(mContext);

		ContentValues valuesLocal = new ContentValues();
		if (name != null) {
			valuesLocal.put(BetsEntry.COLUMN_NAME_USERNAME, name);
		}
		if (salt != null) {
			valuesLocal.put(BetsEntry.COLUMN_NAME_SALTHASH, salt);
		}
		if (groupid != null) {
			valuesLocal.put(BetsEntry.COLUMN_NAME_GROUPID, groupid);
		}
		query.updateUserDetail(valuesLocal, email);
	}
}
