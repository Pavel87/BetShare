package com.pacmac.betshare;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CharSequence mTitle;
	private String userDetail[] = new String[3];
	private Boolean isLoggedIn = false;
	public boolean isConnected = false; // future - check and let user know
										// about
	// connection state
	private Firebase myFirebaseRef;
	private Firebase myFirebaseRefBets;
	private Firebase connectedRef;
	private Query queryRef;
	private Query queryUsers;
	private ChildEventListener userListener;
	private ChildEventListener betListener;
	private ValueEventListener conCheckListener;

	private int userAmount = 0;
	private int fragmentCheck = 0;
	private UserDetailFragment userDetailFragment;
	private LiveScoreFragment liveScoreFragment;
	private WebView webView;

	private SharedPreferences pref;
	private final String PREF_LOGIN = "autoLogin";
	private final String USER_NAME = "name";
	private final String GROUPID = "groupID";
	private final String ACTIVE_LOGIN = "isLoggedIn";
	private final String USER_AMOUNT = "userAmount";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		makeActionOverflowMenuShown();
		// firebase
		Firebase.setAndroidContext(this);
		myFirebaseRef = new Firebase(
				"https://betadvisor2015.firebaseio.com/users/");
		myFirebaseRefBets = new Firebase(
				"https://betadvisor2015.firebaseio.com/bets/");
		connectedRef = new Firebase(
				"https://betadvisor2015.firebaseio.com/.info/connected");

		connectivityCheck();
		pref = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
		userDetail[0] = pref.getString("email", "none");
		userDetail[1] = pref.getString(USER_NAME, "none");
		;
		userDetail[2] = pref.getString(GROUPID, "none");
		isLoggedIn = pref.getBoolean(ACTIVE_LOGIN, false);

		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(USER_AMOUNT, userAmount);
		editor.commit();

		userDetailFragment = new UserDetailFragment();
		liveScoreFragment = new LiveScoreFragment();
		if (isLoggedIn) {
			setContentView(R.layout.activity_main);

			mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
					.findFragmentById(R.id.navigation_drawer);
			mTitle = getTitle();
			// Set up the drawer.
			mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
					(DrawerLayout) findViewById(R.id.drawer_layout));
		}

		betListener = new ChildEventListener() {

			@Override
			public void onChildRemoved(DataSnapshot snapshot) {
				// deleting child if another user changed group or deleted his
				// record
				SqlQueries query = new SqlQueries(getApplicationContext()
						.getApplicationContext());
				Map<String, Object> value = (Map<String, Object>) snapshot
						.getValue();

				Cursor c = query.readAllEventIds(value.get("eventid")
						.toString());
				Log.d("TAG", "child removed");
				if (c.getCount() > 0) {
					query.deleteRowInBets(value.get("eventid").toString());
				}
				c.close();
			}

			@Override
			public void onChildMoved(DataSnapshot arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.d("TAG", "child moved");
			}

			@Override
			public void onChildChanged(DataSnapshot snapshot, String arg1) {
				SqlQueries query = new SqlQueries(getApplicationContext()
						.getApplicationContext());
				Map<String, Object> value = (Map<String, Object>) snapshot
						.getValue();

				Cursor c = query.readAllEventIds(value.get("eventid")
						.toString());
				Calendar cal = Calendar.getInstance();
				if (Long.parseLong(value.get("dateend").toString()) < cal
						.getTimeInMillis()) {

					myFirebaseRefBets.child(snapshot.getKey()).removeValue();
				}

				else if (c.getCount() > 0) {
					ContentValues values = new ContentValues();
					values.put(BetsEntry.COLUMN_NAME_GROUPID,
							(String) value.get("groupid"));
					values.put(BetsEntry.COLUMN_NAME_AUTHOR,
							(String) value.get("author"));

					query.updateBets(values, value.get("eventid").toString());

				}
				c.close();
			}

			@Override
			public void onChildAdded(DataSnapshot snapshot, String arg1) {
				SqlQueries query = new SqlQueries(getApplicationContext()
						.getApplicationContext());
				Map<String, Object> value = (Map<String, Object>) snapshot
						.getValue();
				Cursor c = query.readAllEventIds(value.get("eventid")
						.toString());
				//Log.d("TAG", "pocet radku: " + c.getCount());
				// delete from firebase and remove from local //TODO
				Calendar cal = Calendar.getInstance();
				if (Long.parseLong(value.get("dateend").toString()) < cal
						.getTimeInMillis()) {

					myFirebaseRefBets.child(snapshot.getKey()).removeValue();
					//Log.d("TAG", "" + snapshot.getKey() + "  deleted from FB");

				}

				else if (c.getCount() < 1) {
					c.close(); // closing cursor
					long choice = (long) value.get("choice");
					long trust = (long) value.get("trust");
					ContentValues values = new ContentValues();
					values.put(BetsEntry.COLUMN_NAME_USER_ID,
							(String) value.get("userid"));
					values.put(BetsEntry.COLUMN_NAME_GROUPID,
							(String) value.get("groupid"));
					values.put(BetsEntry.COLUMN_NAME_EVENTID,
							value.get("eventid").toString());
					values.put(BetsEntry.COLUMN_NAME_EVENT,
							(String) value.get("event"));
					values.put(BetsEntry.COLUMN_NAME_HOME,
							(String) value.get("home"));
					values.put(BetsEntry.COLUMN_NAME_AWAY,
							(String) value.get("away"));
					values.put(BetsEntry.COLUMN_NAME_DATEMS, value
							.get("datems").toString());
					values.put(BetsEntry.COLUMN_NAME_DATEEND,
							value.get("dateend").toString());
					values.put(BetsEntry.COLUMN_NAME_CHOICE, choice);
					values.put(BetsEntry.COLUMN_NAME_TRUST, trust);
					values.put(BetsEntry.COLUMN_NAME_ODDS, value.get("odds")
							.toString());
					values.put(BetsEntry.COLUMN_NAME_NEWINDICATOR, 0);
					values.put(BetsEntry.COLUMN_NAME_AUTHOR,
							(String) value.get("author"));
					values.put(BetsEntry.COLUMN_NAME_COMMENT,
							(String) value.get("comment"));
					query.writeLineQuery(values);
					// debug
					Log.d("TAG", value.get("home") + " vs " + value.get("away"));

				}
			}

			@Override
			public void onCancelled(FirebaseError arg0) {
			//	Log.d("TAG", "connection error");

			}
		};

		userListener = new ChildEventListener() {

			@Override
			public void onChildRemoved(DataSnapshot arg0) {
				userAmount++;
				SharedPreferences.Editor editor = pref.edit();
				editor.putInt(USER_AMOUNT, userAmount);
				editor.commit();
				userDetailFragment.updateUsersAmount();
			}

			@Override
			public void onChildMoved(DataSnapshot arg0, String arg1) {
			}

			@Override
			public void onChildChanged(DataSnapshot arg0, String arg1) {
			}

			@Override
			public void onChildAdded(DataSnapshot arg0, String arg1) {
				userAmount++;
				SharedPreferences.Editor editor = pref.edit();
				editor.putInt(USER_AMOUNT, userAmount);
				editor.commit();
				if (fragmentCheck == 0) {
					userDetailFragment.updateUsersAmount();
				}
			}

			@Override
			public void onCancelled(FirebaseError arg0) {

			}
		};

	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		// creating fragments
		switch (position) {
		case 0: {
			fragmentManager.beginTransaction()
					.replace(R.id.container, userDetailFragment).commit();
			mTitle = getString(R.string.app_name);
			fragmentCheck = 0;
			break;
		}

		case 1: {
			fragmentManager.beginTransaction()
					.replace(R.id.container, new MyBetsFragment()).commit();
			mTitle = getString(R.string.mybets);
			fragmentCheck = 1;
			break;
		}
		case 2: {
			fragmentManager.beginTransaction()
					.replace(R.id.container, new AllBetsFragment()).commit();
			mTitle = getString(R.string.allbets);
			fragmentCheck = 2;
			break;
		}
		case 3: {
			fragmentManager
					.beginTransaction()
					.replace(R.id.container,
							new CreateEventFragment(userDetail)).commit();
			mTitle = getString(R.string.createevent);
			fragmentCheck = 3;
			break;
		}
		case 4: {
			fragmentManager.beginTransaction()
					.replace(R.id.container, liveScoreFragment).commit();
			mTitle = getString(R.string.livescore);
			fragmentCheck = 4;
			break;
		}
		case 5: {
			mTitle = getString(R.string.app_name);
			appExitDialog();
			break;
		}
		}
	}
	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);

	}

	// ACTION BAR MENU +++++++++++++++++++++++++++++++++++++++++++++++++++
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_help) {

			helpDialog();
		}

		if (id == R.id.action_about) {
			aboutDialog();
		}

		if (id == R.id.action_logout) {

			Intent intent = new Intent(this, LoginActivity.class);
			intent.putExtra("isLoggedIn", false);
			intent.putExtra("pushLogout", true);
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			}
			finish();

		}

		return super.onOptionsItemSelected(item);
	}

	// CHECK CONNECTIVITY ---------------------
	public void connectivityCheck() {

		conCheckListener = new ValueEventListener() {
			
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				Log.d("TAG", "onDatachanged - connection");
				boolean connected = snapshot.getValue(Boolean.class);
				if (connected) {
					Log.d("TAG", "connected(MAIN)");
					isConnected = true;
					getActionBar().setLogo(R.drawable.icon_bs_online);
				} else {
					getActionBar().setLogo(R.drawable.icon_bs_offline);
					Log.d("TAG", "not connected(MAIN)");
					isConnected = false;

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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		webView = LiveScoreFragment.getWebView();
		if (webView != null && keyCode == KeyEvent.KEYCODE_BACK
				&& webView.canGoBack()) {
			webView.goBack();
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			appExitDialog();
		}
		return true;
	}

	private void appExitDialog() {
		final MainActivity contextAct = MainActivity.this;
		AlertDialog.Builder dialogBuilder = new Builder(contextAct);
		dialogBuilder
				.setCancelable(false)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// cancel - return to app

							}
						})
				.setPositiveButton("Exit",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();

							}
						})
				.setTitle(getResources().getString(R.string.app_exit_title))
				.setMessage(getResources().getString(R.string.app_exit_message));

		AlertDialog alertDialog = dialogBuilder.create();
		alertDialog.show();
	}

	private void aboutDialog() {
		final MainActivity contextAct = MainActivity.this;
		AlertDialog.Builder dialogBuilder = new Builder(contextAct);
		dialogBuilder
				.setCancelable(false)
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// cancel - return to app

					}
				})
				.setTitle(getResources().getString(R.string.title_about))
				.setMessage(
						getResources().getString(R.string.mesage_about) + "\n"
								+ "BetShare v1.0\nCopyright 2015 (c) pacmac");

		AlertDialog alertDialog = dialogBuilder.create();
		alertDialog.show();
	}

	private void helpDialog() {
		final MainActivity contextAct = MainActivity.this;
		AlertDialog.Builder dialogBuilder = new Builder(contextAct);
		dialogBuilder
				.setCancelable(false)
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// cancel - return to app

					}
				})
				.setTitle(getResources().getString(R.string.title_help))
				.setMessage(
						getResources().getString(R.string.message_help1)
								+ "\n"
								+ getResources().getString(
										R.string.message_help2)
								+ "\n"
								+ getResources().getString(
										R.string.message_help3));

		AlertDialog alertDialog = dialogBuilder.create();
		alertDialog.show();
	}

	@Override
	protected void onResume() {
		Log.d("TAG", "onResume");
		connectedRef.addValueEventListener(conCheckListener); 
		// need to update SP as user settings could change
		userDetail[0] = pref.getString("email", "none");
		userDetail[1] = pref.getString(USER_NAME, "none");
		;
		userDetail[2] = pref.getString(GROUPID, "none");
		userAmount = 0; // must be zero as we will be checking who is in the
						// group
		// downloading bets from Firebase

		queryRef = myFirebaseRefBets
				.orderByChild(BetsEntry.COLUMN_NAME_GROUPID).equalTo(
						userDetail[2]);
		queryUsers = myFirebaseRef.orderByChild("groupid").equalTo(
				userDetail[2]);

		queryRef.addChildEventListener(betListener);

		queryUsers.addChildEventListener(userListener);

		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d("TAG", "onPause");
		queryUsers.removeEventListener(userListener);
		queryRef.removeEventListener(betListener);
		connectedRef.removeEventListener(conCheckListener);
		super.onPause();
	}

	private void makeActionOverflowMenuShown() {
	    //devices with hardware menu button (e.g. Samsung Note) don't show action overflow menu
	    try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        java.lang.reflect.Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if (menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        Log.d("TAG", e.getLocalizedMessage());
	    }
	}
	
}
