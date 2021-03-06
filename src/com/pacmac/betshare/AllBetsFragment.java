package com.pacmac.betshare;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class AllBetsFragment extends Fragment {

	private final String PREF_LOGIN = "autoLogin";
	private final String GROUPID = "groupID";

	private ExpandableListView listView = null;
	private ExtendedCursorAdapter mAdapter;
	private Cursor mCursor;
	private String groupId;

	private SqlQueries query;

	private String[] columns = new String[] { BetsEntry.COLUMN_NAME_DATEMS,
			BetsEntry.COLUMN_NAME_HOME, BetsEntry.COLUMN_NAME_AWAY,
			BetsEntry.COLUMN_NAME_CHOICE };

	private int[] target = new int[] { R.id.date1, R.id.homeText1,
			R.id.awayText1, R.id.betText };

	private String[] columnsChild = new String[] { BetsEntry.COLUMN_NAME_TRUST };
	private int[] targetChild = new int[] { R.id.trustPercent1 };

	public AllBetsFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SharedPreferences pref = getActivity().getSharedPreferences(PREF_LOGIN,
				getActivity().MODE_PRIVATE);
		groupId = pref.getString(GROUPID, "none");

		View rootView = inflater.inflate(R.layout.fragment_group_bets,
				container, false);

		AdView mAdView = (AdView) rootView.findViewById(R.id.adBetView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		listView = (ExpandableListView) rootView.findViewById(R.id.list2);
		query = new SqlQueries(getActivity().getApplicationContext());
		mCursor = query.readQueryAll(groupId);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		mAdapter = new ExtendedCursorAdapter(getActivity()
				.getApplicationContext(), mCursor, R.layout.list_item_basic,
				columns, target, R.layout.detail_item, columnsChild,
				targetChild);

		listView.setAdapter(mAdapter);
		listView.setOnGroupExpandListener(new OnGroupExpandListener() {
			int previousGroup = -1;

			@Override
			public void onGroupExpand(int groupPosition) {
				// mAdapter.notifyDataSetChanged();
				if (groupPosition != previousGroup)
					listView.collapseGroup(previousGroup);
				previousGroup = groupPosition;

				Cursor c = mAdapter.getGroup(groupPosition);

				ContentValues values = new ContentValues();
				values.put(BetsEntry.COLUMN_NAME_NEWINDICATOR, 1);
				query.updateBets(values, c.getString(c
						.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_EVENTID)));
				
			}
		});

		listView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				mAdapter.notifyDataSetChanged();

			}
		});

		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

	}

	public void updateList() {
		mAdapter.notifyDataSetChanged();
	}

}
