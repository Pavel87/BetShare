package com.pacmac.betshare;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pacmac.betshare.R;
import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class UserSettings extends Activity implements InterfaceASTask {

	private ImageView backButton;
	private ImageView submitButton;
	private LinearLayout layout;

	private EditText updateName;
	private EditText updatePass1;
	private EditText updatePass2;
	private EditText updateGroupId;

	private String email, name, password, groupid, salt = null;
	private Password securePass;
	private UpdateUserTask updateUserTask;

	private SharedPreferences pref;
	private final String PREF_LOGIN = "autoLogin";
	private final String USER_NAME = "name";
	private final String GROUPID = "groupID";

	private ProgressDlg prgDlg;
	private boolean isConnected = false;
	private boolean check = false;
	
	private ValueEventListener connCheckListener;
	private Firebase connectedRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.user_settings);

		Firebase.setAndroidContext(this);
		connectedRef = new Firebase(
				"https://betadvisor2015.firebaseio.com/.info/connected");
		connectivityCheck();

        AdView mAdView = (AdView) findViewById(R.id.adUserSetting);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
		
		pref = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
		email = pref.getString("email", "none");

		prgDlg = new ProgressDlg(this);


		backButton = (ImageView) findViewById(R.id.backButton);
		submitButton = (ImageView) findViewById(R.id.submitUserData);

		updateName = (EditText) findViewById(R.id.updateUserName);
		updatePass1 = (EditText) findViewById(R.id.updatePassword1);
		updatePass2 = (EditText) findViewById(R.id.updatePassword2);
		updateGroupId = (EditText) findViewById(R.id.updateGroupId);

		backButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		submitButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// check if fields are valid

				if (updatePass1.getText().toString().length() > 0) {
					if (updatePass1.getText().toString()
							.equals(updatePass2.getText().toString())) {
						password = updatePass1.getText().toString();

						try {
							salt = securePass.getSaltedHash(password);
						} catch (Exception e) {
							e.printStackTrace();
						}
						check = true;

					} else {
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.password_doesn_t_match_), Toast.LENGTH_SHORT)
								.show();
						salt = null;
						check =false;
					}
				}

				if (updateName.getText().toString().length() > 0) {
					name = updateName.getText().toString();
					check = true;
				} else {
					name = null;
				}

				if (updateGroupId.getText().toString().length() > 0) {
					String pattern = "^(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z])(?=.*[A-Z].*[A-Z])(?=.*[=@#$%&^+].*[=@#$%&^+])(?=\\S+$).{10,}$";
					Pattern p = Pattern.compile(pattern);
					Matcher m = p.matcher(updateGroupId.getText().toString());
					if (m.matches()) {
						groupid = updateGroupId.getText().toString();
						check = true;
					} else {
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.group_id_doesn_t_match_criterias),
								Toast.LENGTH_SHORT).show();
						check = false;
					}
				} else {
					groupid = null;
				}

				if (isConnected && check) {

					// call async task
					if (name != null || groupid != null || salt != null) {
						String userDetails[] = new String[4];
						userDetails[0] = email;
						userDetails[1] = name;
						userDetails[2] = salt;
						userDetails[3] = groupid;

						updateUserTask = new UpdateUserTask(
								getApplicationContext());
						updateUserTask.asyncResp = UserSettings.this;
						updateUserTask.execute(userDetails);
					}

				} else {
					if(check == false){
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.invalid_data_input), Toast.LENGTH_SHORT)
								.show();
					}
					else
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		super.onCreate(savedInstanceState);
	}

	// CHECK CONNECTIVITY ---------------------
	public void connectivityCheck() {

		
				
				
				connCheckListener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				boolean connected = snapshot.getValue(Boolean.class);
				if (connected) {
					//Log.d("TAG", "connected to network");
					isConnected = true;
					getActionBar().setLogo(R.drawable.icon_bs_online);
				} else {
					//Log.d("TAG", "not connected");
					isConnected = false;
					getActionBar().setLogo(R.drawable.icon_bs_offline);
				}
			}

			@Override
			public void onCancelled(FirebaseError error) {
				System.err.println("Listener was cancelled");
			}
		};

	}

	// CHECK CONNECTIVITY END---------------------

	@Override
	public void updateMainView(String mEmail, String mName, String mGroupid) {

	}

	@Override
	public void showDialog(Boolean mSwitch) {
		if (mSwitch) {
			prgDlg.show();
			prgDlg.setDlgText(getResources().getString(R.string.updating_user));
		} else
			prgDlg.dismiss();

	}

	@Override
	public void registerDialog(Boolean mSwitch, String text) {

	}

	@Override
	public void finishRegistration() {

		SharedPreferences.Editor editor = pref.edit();
		if (name != null) {
			editor.putString(USER_NAME, name);
		}
		if (groupid != null) {
			editor.putString(GROUPID, groupid);
		}
		editor.commit();
		showDialog(false);
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void showDialogNoConn() {

	}

	@Override
	protected void onResume() {
		connectedRef.addValueEventListener(connCheckListener );
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		connectedRef.removeEventListener(connCheckListener );
		super.onPause();
	}
	
}
