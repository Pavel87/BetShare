package com.pacmac.betshare;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.pacmac.betshare.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class LoginActivity extends Activity implements InterfaceASTask {

	// Layout references
	private Button signIn;
	private Button signUp;
	private Button submitSignIn;
	private Button cancelSignIn;
	private Button submitRegister;
	private Button cancelRegister;
	private LoginButton btn;
	private EditText loginText, passwordText, emailText, nameText, pass1Text,
			pass2Text;
	private final String PREF_LOGIN = "autoLogin";
	private final String ACTIVE_LOGIN = "isLoggedIn";
	private final String EMAIL_PREF = "email";
	private final String USER_NAME = "name";
	private final String GROUPID = "groupID";
	private final String FBID = "fbid";
	private Boolean isConnected = false;
	private LoadUserDataTask loadUserTask;
	private RegisterUserTask regUserTask;
	private RegLogFaceBookTask regLogFb;
	private String fbId = null;

	private Firebase connectedRef;
	private ValueEventListener conCheckListener;

	// login variables
	private String email, password, name1, groupId;

	private UiLifecycleHelper uihelper;
	private ProgressDlg prgDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.login_layout);

		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		// enabling connectivity check
		Firebase.setAndroidContext(this);
		connectedRef = new Firebase(
				"https://betadvisor2015.firebaseio.com/.info/connected");

		connectivityCheck();

		// init dialog
		prgDlg = new ProgressDlg(this);

		// fB auth part
		uihelper = new UiLifecycleHelper(this, callback);
		uihelper.onCreate(savedInstanceState);
		ArrayList<String> permission = new ArrayList<String>();
		permission.add("email");
		permission.add("public_profile");
		permission.add("user_friends");

		// loading user which is logged in already
		SharedPreferences pref = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
		// get log out message from main activity
		if (getIntent().getBooleanExtra("pushLogout", false)) {
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean(ACTIVE_LOGIN,
					getIntent().getBooleanExtra(ACTIVE_LOGIN, false));
			editor.commit();

			if (Session.getActiveSession() != null) {
				Session.getActiveSession().closeAndClearTokenInformation();
			}

			Session.setActiveSession(null);
			Log.d("TAG", "I should log out now");
		}

		if (pref.getBoolean(ACTIVE_LOGIN, false)) {
			email = pref.getString(EMAIL_PREF, "none");
			name1 = pref.getString(USER_NAME, "none");
			groupId = pref.getString(GROUPID, "none");
			if (email != "none") {
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				// put extra email/name/gid/loggedIn
				intent.putExtra("name", name1);
				intent.putExtra("email", email);
				intent.putExtra("groupID", groupId);
				intent.putExtra("isLoggedIn", true);

				if (intent.resolveActivity(getPackageManager()) != null)
					startActivity(intent);
				finish();
			}
		}
		findViewById(R.id.llButtonsLogin).setVisibility(View.VISIBLE);

		// getting lock on View elements
		signIn = (Button) findViewById(R.id.signIN);
		signUp = (Button) findViewById(R.id.signUp);
		submitSignIn = (Button) findViewById(R.id.signInSubmit);
		cancelSignIn = (Button) findViewById(R.id.signInCancel);
		submitRegister = (Button) findViewById(R.id.registerSubmit);
		cancelRegister = (Button) findViewById(R.id.registerCancel);
		loginText = (EditText) findViewById(R.id.editLogin);
		passwordText = (EditText) findViewById(R.id.editPass);

		// fblogin btn
		btn = (LoginButton) findViewById(R.id.fbbtn);
		btn.setReadPermissions(permission);
		btn.setUserInfoChangedCallback(new UserInfoChangedCallback() {

			@Override
			public void onUserInfoFetched(GraphUser user) {

				if (user != null) {
					SharedPreferences pref = getSharedPreferences(PREF_LOGIN,
							MODE_PRIVATE);
					if (!pref.getBoolean(ACTIVE_LOGIN, false)) {
						// fb id - serve to get the user picture
						fbId = user.getId();
						String userDetail[] = new String[3];
						userDetail[0] = (String) user.getProperty("email");
						userDetail[1] = user.getName();
						// starting async task
						// validate if user exists and log him in if he
						// doesn't// register first
						regLogFb = new RegLogFaceBookTask(
								getApplicationContext(), isConnected);
						regLogFb.asyncResp = LoginActivity.this;
						regLogFb.execute(userDetail);

					} else {

						Log.d("TAG", "You are not logged in.");

					}
				}
			}

		});

		signIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				findViewById(R.id.rLayoutLogin).setVisibility(View.VISIBLE);
				findViewById(R.id.llButtonsLogin).setVisibility(View.GONE);

			}
		});

		submitSignIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// DB query does email/password exist? if yes then start

				email = loginText.getText().toString();
				password = passwordText.getText().toString();
				String userDetails[] = new String[2];
				userDetails[0] = email;
				userDetails[1] = password;
				// init and execute new bg task
				loadUserTask = new LoadUserDataTask(getApplicationContext(),
						isConnected);
				loadUserTask.asyncResp = LoginActivity.this;
				loadUserTask.execute(userDetails);

			}
		});

		cancelSignIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				passwordText.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(loginText.getWindowToken(), 0);
				findViewById(R.id.rLayoutLogin).setVisibility(View.GONE);
				findViewById(R.id.llButtonsLogin).setVisibility(View.VISIBLE);

			}
		});

		// register buttons
		signUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				findViewById(R.id.llButtonsLogin).setVisibility(View.GONE);
				findViewById(R.id.rLayoutRegister).setVisibility(View.VISIBLE);

			}
		});

		// register user in DB and upload to FIREBASE:::
		submitRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// register procedure check DB if exists
				emailText = (EditText) findViewById(R.id.editEmail);
				nameText = (EditText) findViewById(R.id.editName);
				pass1Text = (EditText) findViewById(R.id.editPass1);
				pass2Text = (EditText) findViewById(R.id.editPass2);

				if (!isEmailValid(emailText.getText().toString())) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.wrong_email_address_),
							Toast.LENGTH_LONG).show();
				} else if (!pass1Text.getText().toString()
						.equals(pass2Text.getText().toString())) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.the_passwords_do_not_match_),
							Toast.LENGTH_LONG).show();
				} else if (nameText.getText().toString().length() < 4) {
					Toast.makeText(
							getApplicationContext(),
							getResources()
									.getString(
											R.string.name_needs_to_have_at_least_3_characters),
							Toast.LENGTH_SHORT).show();
				} else {
					if (isConnected) {
						String userDetail[] = new String[4];
						email = emailText.getText().toString();
						name1 = nameText.getText().toString();
						password = pass1Text.getText().toString();
						// default groupId
						groupId = "qq11QQ@@##DEMO##";

						userDetail[0] = email;
						userDetail[1] = name1;
						userDetail[2] = password;
						userDetail[3] = groupId;

						// call async task now
						regUserTask = new RegisterUserTask(
								getApplicationContext());
						regUserTask.asyncResp = LoginActivity.this;
						regUserTask.execute(userDetail);

					} else {
						Toast.makeText(
								getApplicationContext(),
								getResources()
										.getString(
												R.string.connection_to_internet_is_required),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		cancelRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				emailText = (EditText) findViewById(R.id.editEmail);
				nameText = (EditText) findViewById(R.id.editName);
				pass1Text = (EditText) findViewById(R.id.editPass1);
				pass2Text = (EditText) findViewById(R.id.editPass2);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(pass2Text.getWindowToken(), 0);
				findViewById(R.id.rLayoutRegister).setVisibility(View.GONE);
				findViewById(R.id.llButtonsLogin).setVisibility(View.VISIBLE);
				cleanUp();

			}
		});

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		uihelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uihelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {

		super.onPause();
		uihelper.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uihelper.onDestroy();
		connectedRef.removeEventListener(conCheckListener);
		Log.d("TAG", "listener removed");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		uihelper.onActivityResult(requestCode, resultCode, data);
	}

	// clean variables
	public void cleanUp() {
		emailText.setText("");
		nameText.setText("");
		pass1Text.setText("");
		pass2Text.setText("");
		emailText = null;
		nameText = null;
		pass1Text = null;
		pass2Text = null;
		email = null;
		name1 = null;
		password = null;

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

	// CHECK CONNECTIVITY ---------------------
	public void connectivityCheck() {

		conCheckListener = new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				boolean connected = snapshot.getValue(Boolean.class);
				if (connected) {
					Log.d("TAG", "connected(Login)");
					isConnected = true;

				} else {
					Log.d("TAG", "not connected(Login)");
					isConnected = false;
				}
			}

			@Override
			public void onCancelled(FirebaseError error) {
				System.err.println("Listener was cancelled");
			}
		};

		connectedRef.addValueEventListener(conCheckListener);

	}

	// CHECK CONNECTIVITY END---------------------

	// +++++++++++++++++++ FB LOGIN +++ 3rd party code++++++++++++++++=
	// check if email has valid structure || offline only
	boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	// FB login below
	private Session.StatusCallback callback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (state.isOpened()) {

				Log.d("MainActivity", "Facebook session opened.");

			} else if (state.isClosed()) {

				Log.d("MainActivity", "Facebook session closed.");
			}

			// onSessionStateChange(session, state, exception);
		}
	};

	// FB login end

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// do nothing
		}
		return true;
	}

	@Override
	public void updateMainView(String mEmail, String mName, String mGroupid) {
		// adding user to shared preferences for autologin
		prgDlg.setDlgText("Logging In");
		SharedPreferences preferences = getSharedPreferences(PREF_LOGIN,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(ACTIVE_LOGIN, true);
		editor.putString(EMAIL_PREF, mEmail);
		editor.putString(USER_NAME, mName);
		editor.putString(GROUPID, mGroupid);
		editor.putString(FBID, fbId);
		editor.commit();
		// launching main activity
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		// put extra email/name/gid/loggedIn
		intent.putExtra("name", mName);
		intent.putExtra("email", mEmail);
		intent.putExtra("groupID", mGroupid);
		intent.putExtra("isLoggedIn", true);
		prgDlg.dismiss();
		if (intent.resolveActivity(getPackageManager()) != null)
			startActivity(intent);
		finish();
	}

	@Override
	public void showDialog(Boolean mSwitch) {
		if (mSwitch) {
			prgDlg.show();
		} else {
			prgDlg.dismiss();
			Toast.makeText(
					getApplicationContext(),
					getResources().getString(
							R.string.wrong_email_please_sign_up_first),
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void registerDialog(Boolean mSwitch, String text) {
		if (mSwitch) {
			prgDlg.show();
			prgDlg.setDlgText(text);
		} else {
			prgDlg.dismiss();
			Toast.makeText(
					getApplicationContext(),
					getResources()
							.getString(
									R.string.this_email_is_already_registred_in_system_),
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void finishRegistration() {
		Log.d("TAG", "update to backend complete");
		prgDlg.dismiss();
		loginText.setText(email);
		Toast.makeText(getApplicationContext(),
				getResources().getString(R.string.new_user_created),
				Toast.LENGTH_SHORT).show();
		cleanUp();
		findViewById(R.id.rLayoutRegister).setVisibility(View.GONE);
		findViewById(R.id.rLayoutLogin).setVisibility(View.VISIBLE);

	}

	@Override
	public void showDialogNoConn() {
		prgDlg.dismiss();
		Toast.makeText(
				getApplicationContext(),
				getResources().getString(R.string.wrong_email_or_no_connection),
				Toast.LENGTH_SHORT).show();
	}

}
