package com.pacmac.betshare;

import com.pacmac.betshare.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class ProgressDlg extends Dialog {

	public TextView dlgText;
	
	public ProgressDlg(Context context) {
		super(context);
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(false);
		setContentView(R.layout.progress_bar);
		dlgText = (TextView) findViewById(R.id.progressText);
		
		
		super.onCreate(savedInstanceState);
	}


	public void setDlgText(String text) {
		this.dlgText.setText(text);
	}
	
	
	
	
}