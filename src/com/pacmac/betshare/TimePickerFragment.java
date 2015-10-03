package com.pacmac.betshare;


import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;


public class TimePickerFragment  extends DialogFragment implements OnTimeSetListener {

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		
		return new TimePickerDialog(getActivity(), this, hour, minutes, DateFormat.is24HourFormat(getActivity()));
		
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// ((OnDateSetListener) getTargetFragment()).onDateSet(view,year,monthOfYear,dayOfMonth);
		((OnTimeSetListener) getTargetFragment()).onTimeSet(view, hourOfDay, minute);
	}
	
}

