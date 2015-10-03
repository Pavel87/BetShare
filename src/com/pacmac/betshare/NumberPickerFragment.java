package com.pacmac.betshare;


import com.pacmac.betshare.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

public class NumberPickerFragment extends DialogFragment{

	float result = 0f;
	int n1 =0, n2 =0 ,n3 = 0;
	private Dialog dialog;
	
	public NumberPickerFragment() {
		// TODO Auto-generated constructor stub
	}

	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.number_picker_dialog, null);
		
		NumberPicker nP1 = (NumberPicker) view.findViewById(R.id.numberpicker1);
		NumberPicker nP2 = (NumberPicker) view.findViewById(R.id.numberpicker2);
		NumberPicker nP3 = (NumberPicker) view.findViewById(R.id.numberpicker3);
		
		nP1.setMaxValue(20);
		nP1.setMinValue(0);
		nP1.setFocusable(true);
		nP1.setFocusableInTouchMode(true);
		nP1.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				
				n1 = newVal;
				result = n1 +(float)n2/10 + (float)n3/100;
				dialog.setTitle("Odds: " + String.format("%.2f",result));

			}
		});

		nP2.setMaxValue(9);
		nP2.setMinValue(0);
		nP2.setFocusable(true);
		nP2.setFocusableInTouchMode(true);
		nP2.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				// TODO Auto-generated method stub
				n2=newVal;
				result = n1 +(float)n2/10 + (float)n3/100;
				dialog.setTitle("Odds: " + String.format("%.2f",result));
				}
		});

		nP3.setMaxValue(9);
		nP3.setMinValue(0);
		nP3.setFocusable(true);
		nP3.setFocusableInTouchMode(true);
		nP3.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				n3=newVal;
				result = n1 +(float)n2/10 + (float)n3/100;
				dialog.setTitle("Odds: " + String.format("%.2f",result));

			}
		});

		dialog = new AlertDialog.Builder(getActivity())
				.setTitle("Course: "+result)
				.setView(view)
				.setPositiveButton("DONE",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// do something
								((OnClickListener) getTargetFragment()).onClick(dialog, (int)(result*100));
								
								//doPositiveClick();
							}
						})

				.create();

		return dialog;
		
	}
	

}
