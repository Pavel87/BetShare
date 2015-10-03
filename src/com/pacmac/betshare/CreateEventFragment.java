package com.pacmac.betshare;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.facebook.Session.NewPermissionsRequest;
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
import android.app.Fragment;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CreateEventFragment extends Fragment implements OnDateSetListener,
		OnTimeSetListener, DialogInterface.OnClickListener {

	private static final int ARG_SECTION_NUMBER = 3;
	Spinner spinnerSport;
	Button submitButton;
	SeekBar trustBar;
	TextView trustText, createDate, createTime, course;
	EditText homeText, awayText, commentText;
	RadioGroup radioGroup;
	ImageView calendar, clock, money;

	int mMonth = -1, mYear = -1, mDay = -1, mHour = -1, mMinute = -1;
	long dateInMs = -1;
	long dateEnd = -1; // must be set 6 hours after that we can delete this
						// record
	float mCourse;
	String dayOfWeek;
	private int REQUEST_CODE_FRAGMENT = 1001;
	private Query queryRef;
	private Firebase userFireBase;

	// fragment data collection
	private String sport, home, away, comment;
	private int trust = 0, choice = -1;
	private int betCounter = 0;
	private String name;
	private String email;
	private String groupID;
	private String eventid;

	public CreateEventFragment(String[] userDetails) {
		this.email = userDetails[0];
		this.name = userDetails[1];
		this.groupID = userDetails[2];
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_create_event,
				container, false);

		AdView mAdView = (AdView) rootView.findViewById(R.id.adCreateEvent);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		// spinner init
		spinnerSport = (Spinner) rootView.findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.sport, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSport.setAdapter(adapter);

		// date picker button init
		calendar = (ImageView) rootView.findViewById(R.id.calendar);
		createDate = (TextView) rootView.findViewById(R.id.createDate1);
		calendar.setOnClickListener(new View.OnClickListener() {
			// calling date picker
			@Override
			public void onClick(View v) {
				DatePickerFragment datePickerFragment = new DatePickerFragment();
				datePickerFragment.setTargetFragment(CreateEventFragment.this,
						REQUEST_CODE_FRAGMENT);
				datePickerFragment.show(getFragmentManager(), "datePicker");

			}
		});

		// timepicker button init
		clock = (ImageView) rootView.findViewById(R.id.clock);
		createTime = (TextView) rootView.findViewById(R.id.createTime);
		clock.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TimePickerFragment timePickerFragment = new TimePickerFragment();
				timePickerFragment.setTargetFragment(CreateEventFragment.this,
						REQUEST_CODE_FRAGMENT);
				timePickerFragment.show(getFragmentManager(), "timePicker");

			}
		});

		// course button init
		money = (ImageView) rootView.findViewById(R.id.money);
		course = (TextView) rootView.findViewById(R.id.course);
		money.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				NumberPickerFragment numberPickerFragment = new NumberPickerFragment();
				numberPickerFragment.setTargetFragment(
						CreateEventFragment.this, REQUEST_CODE_FRAGMENT);
				numberPickerFragment.show(getFragmentManager(), "numberPicker");

			}
		});

		// init editText boxes
		homeText = (EditText) rootView.findViewById(R.id.editHome);
		awayText = (EditText) rootView.findViewById(R.id.editAway);
		commentText = (EditText) rootView.findViewById(R.id.editComment);

		// init radiogroup
		radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup1);

		// init trustBar (seekbar)
		trustText = (TextView) rootView.findViewById(R.id.trustResult);
		trustBar = (SeekBar) rootView.findViewById(R.id.trustBar);
		trustBar.setProgress(100);
		trust = 100;
		trustText.setText(trust + "%");

		trustBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				progress = progress / 10;
				progress = progress * 10;
				trust = progress;
				trustText.setText(trust + "%");

			}
		});

		// submit button
		submitButton = (Button) rootView.findViewById(R.id.submit1);
		submitButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				home = homeText.getText().toString().toUpperCase();
				away = awayText.getText().toString().toUpperCase();
				sport = spinnerSport.getSelectedItem().toString().toUpperCase();
				comment = commentText.getText().toString();
				setDate();
				findChoice();

				// add checks if fields are filled
				if (home.length() > 2 && away.length() > 2 && dateInMs > 0) {

					eventid = Long.toString(System.currentTimeMillis());
					// writing in DB
					SqlQueries query = new SqlQueries(getActivity()
							.getApplicationContext());

					// forwarding data to ContentValues
					ContentValues values = new ContentValues();
					values.put(BetsEntry.COLUMN_NAME_USER_ID, email);
					values.put(BetsEntry.COLUMN_NAME_GROUPID, groupID);
					values.put(BetsEntry.COLUMN_NAME_EVENTID, eventid);
					values.put(BetsEntry.COLUMN_NAME_EVENT, sport);
					values.put(BetsEntry.COLUMN_NAME_HOME, home);
					values.put(BetsEntry.COLUMN_NAME_AWAY, away);
					values.put(BetsEntry.COLUMN_NAME_DATEMS,
							Long.toString(dateInMs));
					values.put(BetsEntry.COLUMN_NAME_DATEEND,
							Long.toString(dateEnd));
					values.put(BetsEntry.COLUMN_NAME_CHOICE, choice);
					values.put(BetsEntry.COLUMN_NAME_TRUST, trust);
					values.put(BetsEntry.COLUMN_NAME_AUTHOR, name);
					values.put(BetsEntry.COLUMN_NAME_COMMENT, comment);
					values.put(BetsEntry.COLUMN_NAME_ODDS, mCourse);
					
					if (((MainActivity) getActivity()).isConnected) {
					query.writeLineQuery(values);
					query.writeLineToTemp(values);
					// setting variables into default

					values = null;
					
						new Thread(new Runnable() {

							@Override
							public void run() {
								android.os.Process
										.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

								Firebase betsTree = new Firebase(
										"https://betadvisor2015.firebaseio.com/bets/");
								Map<String, Object> valuesBet = new HashMap<String, Object>();
								valuesBet.put(BetsEntry.COLUMN_NAME_USER_ID,
										email);
								valuesBet.put(BetsEntry.COLUMN_NAME_GROUPID,
										groupID);
								valuesBet.put(BetsEntry.COLUMN_NAME_EVENTID,
										eventid); // TODO
								valuesBet.put(BetsEntry.COLUMN_NAME_EVENT,
										sport);
								valuesBet.put(BetsEntry.COLUMN_NAME_HOME, home);
								valuesBet.put(BetsEntry.COLUMN_NAME_AWAY, away);
								valuesBet.put(BetsEntry.COLUMN_NAME_DATEEND,
										Long.toString(dateEnd));
								valuesBet.put(BetsEntry.COLUMN_NAME_DATEMS,
										Long.toString(dateInMs));
								valuesBet.put(BetsEntry.COLUMN_NAME_ODDS,
										mCourse);
								valuesBet.put(BetsEntry.COLUMN_NAME_CHOICE,
										choice);
								valuesBet.put(BetsEntry.COLUMN_NAME_TRUST,
										trust);
								valuesBet.put(BetsEntry.COLUMN_NAME_AUTHOR,
										name);
								valuesBet.put(BetsEntry.COLUMN_NAME_COMMENT,
										comment);

								getActivity().runOnUiThread(new Runnable() {

									@Override
									public void run() {
										try {
											cleanUp();
										} catch (Exception ex) {
											Log.d("TAG",
													"fragment switched before the UI was cleaned");

										}
									}
								});

								betsTree.push().setValue(valuesBet,
										new Firebase.CompletionListener() {

											@Override
											public void onComplete(
													FirebaseError arg0,
													Firebase arg1) {
												SqlQueries query = new SqlQueries(
														getActivity()
																.getApplicationContext());
												// TODO bug - when you leave
												// fragment and it is not
												// complete it will be null
												query.deleteRowInTmp(eventid);

												// updating pocet sazek
												userFireBase = new Firebase(
														"https://betadvisor2015.firebaseio.com/users/");
												ContentValues value = new ContentValues();
												Cursor c = query
														.readUserDb(
																email,
																BetsEntry.COLUMN_NAME_EMAIL);
												betCounter = c.getInt(c
														.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_BETCOUNTER)) + 1;
												value.put(
														BetsEntry.COLUMN_NAME_BETCOUNTER,
														betCounter);
												// updating pocet sazek
												query.updateUserGroupId(value,
														email);

												// update firebase users with
												// new GID
												queryRef = userFireBase
														.orderByChild(
																BetsEntry.COLUMN_NAME_EMAIL)
														.equalTo(email);
												queryRef.addListenerForSingleValueEvent(new ValueEventListener() {

													@Override
													public void onDataChange(
															DataSnapshot snapshot) {
														Log.d("TAG",
																"will update amount of bets created by me");
														Iterator<DataSnapshot> ds = snapshot
																.getChildren()
																.iterator();
														String s = ds.next()
																.getKey()
																.toString();
														Firebase hopperRef = userFireBase
																.child(s);
														Map<String, Object> value = new HashMap<String, Object>();
														value.put(
																BetsEntry.COLUMN_NAME_BETCOUNTER,
																betCounter);
														hopperRef
																.updateChildren(value);
													}

													@Override
													public void onCancelled(
															FirebaseError arg0) {
														Log.d("TAG",
																"Error during updating betCounter to firebase");

													}
												});

											}
										});
							}
						}).start();
					}
					
					else {
						Toast.makeText(
								getActivity().getApplicationContext(),
								getResources().getString(
										R.string.no_internet_connection),
								Toast.LENGTH_SHORT).show();
					}

				} else {

					Toast.makeText(
							getActivity().getApplicationContext(),
							getResources().getString(
									R.string.please_fill_all_fields),
							Toast.LENGTH_SHORT).show();

				}
			}
		});

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	// supporting methods

	// onDateSet (date picker)
	public void onDateSet(DatePicker view, int year, int month, int day) {
		this.mMonth = month;
		this.mYear = year;
		this.mDay = day;
		// update view
		updateDate();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		this.mHour = hourOfDay;
		this.mMinute = minute;
		updateTime();
	}

	public void setDate() {
		Calendar cal = new GregorianCalendar(mYear, mMonth, mDay, mHour,
				mMinute);
		dateInMs = cal.getTimeInMillis();
		cal.add(Calendar.HOUR_OF_DAY, 6);
		dateEnd = cal.getTimeInMillis();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// Course set
		mCourse = (float) which / 100;
		course.setText("" + mCourse);
		course.setBackground(getResources().getDrawable(R.drawable.commentbrd));
	}

	private void updateDate() {
		// Month is 0 based so add 1
		Calendar cal = new GregorianCalendar(mYear, mMonth, mDay);
		dayOfWeek = new SimpleDateFormat("EE").format(cal.getTime()).toString();
		createDate.setText(new StringBuilder().append(dayOfWeek).append(", ")
				.append(mDay).append(".").append(mMonth + 1));
		createDate.setBackground(getResources().getDrawable(
				R.drawable.commentbrd));
	}

	private void updateTime() {
		if (mMinute < 10) {
			createTime.setText(new StringBuilder().append(mHour).append(":")
					.append("0" + mMinute));
		} else {
			createTime.setText(new StringBuilder().append(mHour).append(":")
					.append(mMinute));
		}
		createTime.setBackground(getResources().getDrawable(
				R.drawable.commentbrd));
	}

	// check which choice has been selected from RadioGroup
	private void findChoice() {

		switch (radioGroup.getCheckedRadioButtonId()) {
		case R.id.radio0: {
			choice = 1;
			break;
		}
		case R.id.radio1: {
			choice = 2;
			break;
		}
		case R.id.radio2: {
			choice = 3;
			break;
		}
		case R.id.radio3: {
			choice = 4;
			break;
		}
		case R.id.radio4: {
			choice = 5;
			break;
		}
		default:
			choice = 1;
		}
	}

	private void cleanUp() {
		// setting default values
		radioGroup.check(R.id.radio0);
		homeText.setText("");
		awayText.setText("");
		createDate.setText(getResources().getString(R.string.enter_date_));
		createDate.setBackgroundColor(getResources().getColor(
				R.color.create_fr_bg));
		createTime.setText(getResources().getString(R.string.enter_time_));
		createTime.setBackgroundColor(getResources().getColor(
				R.color.create_fr_bg));
		course.setText(getResources().getString(R.string.enter_odds_));
		course.setBackgroundColor(getResources().getColor(R.color.create_fr_bg));
		commentText.setText("");
		sport = null;
		home = null;
		away = null;
		dateInMs = -1;
		dateEnd = -1;
		mDay = -1;
		mYear = -1;
		mMonth = -1;
		mHour = -1;
		mMinute = -1;
		mCourse = -1;
		comment = null;
		trustBar.setProgress(100);
		trust = 100;
		trustText.setText(trust + "%");
		choice = -1;

	}
	
	@Override
	public void onPause() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(commentText.getWindowToken(), 0);
		super.onPause();
	}
}