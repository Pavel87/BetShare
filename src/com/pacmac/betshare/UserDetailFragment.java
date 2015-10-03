package com.pacmac.betshare;

import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pacmac.betshare.R;
import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserDetailFragment extends Fragment {

	private TextView userView;
	private TextView groupView;
	private TextView emailView;
	private TextView updateUserAmount;
	private TextView betCounter;
	private ImageView settingView;
	

	private final String PREF_LOGIN = "autoLogin";
	private final String USER_EMAIL = "email";
	private final String GROUPID = "groupID";
	private final String FBID = "fbid";
	private final String USER_NAME = "name";
	private final String USER_AMOUNT = "userAmount";
	private final int REQ_CODE = 35;

	private String email, name, gid, fbId;
	private int userAmount;
	private int betsAmount = 0;
	
	private ProfilePictureView profilePictureView;
	private GraphUser user;
	
	public UserDetailFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		

        
		SharedPreferences pref = getActivity().getSharedPreferences(PREF_LOGIN,
				getActivity().MODE_PRIVATE);
		email = pref.getString(USER_EMAIL, "none");
		gid = pref.getString(GROUPID, "none");
		name = pref.getString(USER_NAME, "none");
		userAmount = pref.getInt(USER_AMOUNT, 1);
		fbId = pref.getString(FBID, null);
		
		//query to get total number of created events since beginning of time
		SqlQueries query = new SqlQueries(getActivity().getApplicationContext());
		Cursor c = query.readUserDb(email, BetsEntry.COLUMN_NAME_EMAIL);

		betsAmount = c.getInt(c.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_BETCOUNTER));
		c.close();// closing cursor
		
		View v = inflater.inflate(R.layout.user_frag_layout, null);

        AdView mAdView = (AdView) v.findViewById(R.id.adUserDetail);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
		
		userView = (TextView) v.findViewById(R.id.userName);
		groupView = (TextView) v.findViewById(R.id.gidUser);
		emailView = (TextView) v.findViewById(R.id.emailUser);
		updateUserAmount = (TextView) v.findViewById(R.id.userCounter);
		betCounter = (TextView) v.findViewById(R.id.betCounter);
		settingView = (ImageView) v.findViewById(R.id.settingsImg);
		
		profilePictureView = (ProfilePictureView) v.findViewById(R.id.profilePicture);
		profilePictureView.setProfileId(fbId);

		settingView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent settings = new Intent(getActivity(), UserSettings.class);
				if (settings.resolveActivity(getActivity().getPackageManager()) != null) {
					startActivityForResult(settings,REQ_CODE);
				}
			}
		});
		
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		userView.setText(name);
		groupView.setText(gid);
		emailView.setText(email);
		if(userAmount > 1) {
			updateUserAmount.setText(""+userAmount);
		}
		else {
		updateUserAmount.setText("Loading...");
	}
		
		
		betCounter.setText(""+betsAmount);
		

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_CODE && resultCode == getActivity().RESULT_CANCELED) {
			//Log.d("TAG", "no user modification");
		}
		else if (requestCode == REQ_CODE && resultCode == getActivity().RESULT_OK) {
			SqlQueries query = new SqlQueries(getActivity());
			query.clearBetsTable(email);
			
			SharedPreferences pref = getActivity().getSharedPreferences(PREF_LOGIN,
					getActivity().MODE_PRIVATE);
			userView.setText(pref.getString(USER_NAME, "Error"));
			groupView.setText(pref.getString(GROUPID, "Error"));
		}
	}

	public void updateUsersAmount() {
		SharedPreferences pref = getActivity().getSharedPreferences(PREF_LOGIN,
				getActivity().MODE_PRIVATE);
		userAmount = pref.getInt(USER_AMOUNT, 1);
		updateUserAmount.setText("" + userAmount);
	}

}
