package com.pacmac.betshare;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.pacmac.betshare.R;
import com.pacmac.betshare.BetsDatabaseContract.BetsEntry;

import android.widget.LinearLayout.LayoutParams;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

public class ExtendedCursorAdapter extends SimpleCursorTreeAdapter {

	private int gLayout;
	private Context mContext;
	private SqlQueries query;
	private boolean isNew = false;
	private Cursor c = null;

	public ExtendedCursorAdapter(Context context, Cursor cursor,
			int groupLayout, String[] groupFrom, int[] groupTo,
			int childLayout, String[] childFrom, int[] childTo) {
		super(context, cursor, groupLayout, groupFrom, groupTo, childLayout,
				childFrom, childTo);
		this.gLayout = groupLayout;
		this.mContext = context;
		this.c = cursor;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		c.moveToPosition(groupPosition);
		
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(gLayout, parent, false);
			ViewHolderGroup holderGroup = new ViewHolderGroup();
			holderGroup.dateText = (TextView) convertView
					.findViewById(R.id.date1);
			holderGroup.homeText = (TextView) convertView
					.findViewById(R.id.homeText1);
			holderGroup.awayText = (TextView) convertView
					.findViewById(R.id.awayText1);
			holderGroup.betText = (TextView) convertView
					.findViewById(R.id.betText);
			holderGroup.timeText = (TextView) convertView
					.findViewById(R.id.timeEvent);
			holderGroup.newIndicator = (TextView) convertView
					.findViewById(R.id.newRecordIndicator);
			holderGroup.oldIndicator = (TextView) convertView
					.findViewById(R.id.obsoleteIndicator);
			convertView.setTag(holderGroup);
		}

		
		String date = c.getString(c
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_DATEMS));
		String home = c.getString(c
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_HOME));
		String away = c.getString(c
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_AWAY));
		int choice = c.getInt(c
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_CHOICE));
		int newInd = c.getInt(c
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_NEWINDICATOR));
		int day = 1, month = 1, hour = 0, minute = 0;
		String dayOfWeek;
		 

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(date));

		day = calendar.get(Calendar.DAY_OF_MONTH);
		month = calendar.get(Calendar.MONTH);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		dayOfWeek = new SimpleDateFormat("EE").format(calendar.getTime())
				.toString();

		ViewHolderGroup holder = (ViewHolderGroup) convertView.getTag();

		if (newInd == 1) {
			holder.newIndicator.setVisibility(View.GONE);
		}
		else {
			holder.newIndicator.setVisibility(View.VISIBLE);
		}

		// display indicator for old record for next 6 hours
		Calendar currentCal = Calendar.getInstance();
		long currentTime = currentCal.getTimeInMillis();
		if (currentTime > Long.parseLong(date)) {
			holder.newIndicator.setVisibility(View.GONE);
			holder.oldIndicator.setVisibility(View.VISIBLE);
		}
		else {
			holder.oldIndicator.setVisibility(View.GONE);
		}

		if (date != null) {
			holder.dateText.setText(new StringBuilder().append(dayOfWeek)
					.append(", ").append(day).append(".").append(month + 1));
			if (minute < 10)
				holder.timeText.setText(new StringBuilder().append(hour)
						.append(":").append("0" + minute));
			else {
				holder.timeText.setText(new StringBuilder().append(hour)
						.append(":").append(minute));
			}
		}
		if (home != null)
			holder.homeText.setText(home);
		if (away != null)
			holder.awayText.setText(away);
		holder.betText.setText(getChoice(choice));
		
		
		return convertView;
	}


	@Override
	public View newChildView(Context context, Cursor cursor,
			boolean isLastChild, ViewGroup parent) {

		View view = LayoutInflater.from(mContext).inflate(R.layout.detail_item,
				parent, false);

		return view;
	}

	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {

		int trust = cursor.getInt(cursor
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_TRUST));
		int choice = cursor.getInt(cursor
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_CHOICE));
		String comment = cursor.getString(cursor
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_COMMENT));
		String author = cursor.getString(cursor
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_AUTHOR));
		String event = cursor.getString(cursor
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_EVENT));
		double odds = Double.parseDouble(cursor.getString(cursor
				.getColumnIndexOrThrow(BetsEntry.COLUMN_NAME_ODDS)));

		if (comment.length() < 2) {
			comment = "No comment.";
		}
		TextView eventView = (TextView) view.findViewById(R.id.sportType);
		TextView oddsView = (TextView) view.findViewById(R.id.oddsText);
		TextView trustView = (TextView) view.findViewById(R.id.trustPercent1);
		TextView choiceView = (TextView) view.findViewById(R.id.choiceView);
		TextView commentView = (TextView) view.findViewById(R.id.comment);
		TextView authorView = (TextView) view.findViewById(R.id.author);
		LinearLayout graph = (LinearLayout) view.findViewById(R.id.graph1);
		choiceView.setText(getChoice(choice));
		trustView.setText(trust + "%");
		commentView.setText(comment);
		authorView.setText(author + ":");
		eventView.setText(event);
		oddsView.setText(String.format("%.3g", odds));
		// setting up ll as a graph
		LayoutParams params = (LayoutParams) graph.getLayoutParams();
		params.width = dpToPx(trust);
		graph.setLayoutParams(params);
	}

	// convertion Int to string
	private final String getChoice(int choice) {
		switch (choice) {
		case 1:
			return "1";
		case 2:
			return "10";
		case 3:
			return "0";
		case 4:
			return "02";
		case 5:
			return "2";

		}
		return "";
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		query = new SqlQueries(mContext);
		int tmp = groupCursor.getInt(groupCursor
				.getColumnIndexOrThrow(BetsEntry._ID));
		Cursor cursor = query.readQuery(tmp, BetsEntry._ID + " = ?");

		return cursor;
	}

	private int dpToPx(int dp) {
		DisplayMetrics displayMetrics = mContext.getResources()
				.getDisplayMetrics();
		int px = Math.round(dp
				* (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}	

	private static class ViewHolderGroup {
		TextView dateText;
		TextView homeText;
		TextView awayText;
		TextView betText;
		TextView timeText;
		TextView newIndicator;
		TextView oldIndicator;
	}

}
