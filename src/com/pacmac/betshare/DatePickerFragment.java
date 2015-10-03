package com.pacmac.betshare;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements OnDateSetListener{

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{

		final Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		return new DatePickerDialog(getActivity(),this, year, month, day);
		
	}
	
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		//providing date to create event view
		((OnDateSetListener) getTargetFragment()).onDateSet(view,year,monthOfYear,dayOfMonth);

	}

}
